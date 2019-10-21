package jp.pioneer.carsync.presentation.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.WindowManager;

import com.neusoft.adas.DasAppParam;
import com.neusoft.adas.DasCameraFixedInfo;
import com.neusoft.adas.DasCameraParam;
import com.neusoft.adas.DasEgoCarInfo;
import com.neusoft.adas.DasEgoStatus;
import com.neusoft.adas.DasEngine;
import com.neusoft.adas.DasEvents;
import com.neusoft.adas.DasFCWParam;
import com.neusoft.adas.DasImageProcessingInfo;
import com.neusoft.adas.DasLDWParam;
import com.neusoft.adas.DasLaneMarkings;
import com.neusoft.adas.DasPCWParam;
import com.neusoft.adas.DasStatInfo;
import com.neusoft.adas.DasTrafficEnvironment;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.AdasDetectingEvent;
import jp.pioneer.carsync.domain.event.AdasErrorEvent;
import jp.pioneer.carsync.domain.event.AdasWarningUpdateEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferAdas;
import jp.pioneer.carsync.domain.model.AdasCameraSetting;
import jp.pioneer.carsync.domain.model.AdasErrorType;
import jp.pioneer.carsync.domain.model.AdasFunctionSetting;
import jp.pioneer.carsync.domain.model.AdasFunctionType;
import jp.pioneer.carsync.domain.model.AdasWarningEvent;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.AudioOutputMode;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarRunningStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import timber.log.Timber;

/**
 * ADAS.
 * <p>
 * ADASに関する処理をまとめたクラス
 */
public class Adas {
    public static final int NO_DETECTION_ERROR_COUNT = 900; // 900sec(15minutes)
    public static final int ADAS_FRAME_RATE_MIN = 15; //fps
    public static final float INVALID = -9999f;
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject PreferAdas mPreferAdas;
    @Inject GetStatusHolder mStatusCase;
    private EnumSet<AdasWarningEvent> mAdasWarningEvents = EnumSet.noneOf(AdasWarningEvent.class);
    private boolean mIsAdasError = false;
    private Timer mTimer = null;
    private Handler mHandler = new Handler();
    private TimerTask task = new TimeOutTask();
    private MediaSourceType mPreviousSourceType;
    private static boolean isAdasRelease = false;
    private static String FCWisopen = "";
    private static String LDWisopen = "";
    private static String PDWisopen = "";
    private static String LKWisopen = "";
    /** ADAS LDW最小動作速度（km/h） */
    private static int adasLdwMinSpeed = 30;
    /** ADAS LDW最大動作速度（km/h） */
    private static int adasLdwMaxSpeed = 251;
    /** ADAS PCW最小動作速度（km/h） */
    private static int adasPcwMinSpeed = 1;
    /** ADAS PCW最大動作速度（km/h） */
    private static int adasPcwMaxSpeed = 30;
    /** ADAS FCW最小動作速度（km/h） */
    private static int adasFcwMinSpeed = 5;
    /** ADAS FCW最大動作速度（km/h） */
    private static int adasFcwMaxSpeed = 40;
    private static boolean mLeftLaneDetecting = false;
    private static boolean mRightLaneDetecting = false;
    private static boolean mVehicleDetecting = false;
    private static boolean mPedestrianDetecting = false;
    private static float mSensorY = INVALID;
    private static float mSensorZ = INVALID;
    private static float mSpeed = INVALID;
    private static String mFpsString = "CapFps:\nAlFps:\nAvFps:\nApFps:";
    private static float mDashboardRate = INVALID;
    private static float mDashboardRateOffset = INVALID;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private TestSensorListener mSensorListener;
    private int mTimeToDetectionError = 0;
    private class TimeOutTask extends TimerTask {
        public void run() {
            AppStatus appStatus = mStatusCase.execute().getAppStatus();
            Set<AdasErrorType> errors = appStatus.adasErrorList;
            mIsAdasError = errors.size() > 0;
            mTimeToDetectionError++;
            if (mTimeToDetectionError >= NO_DETECTION_ERROR_COUNT) {
                errors.add(AdasErrorType.LOW_ILLUMINANCE_OR_POOR_VISIBILITY);
                if ((errors.size() > 0) != mIsAdasError) {
                    mEventBus.post(new AdasErrorEvent());
                }
            }
        }
    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            AppStatus status = mStatusCase.execute().getAppStatus();
            status.adasWarningEvents = EnumSet.noneOf(AdasWarningEvent.class);
            mEventBus.post(new AdasWarningUpdateEvent());
        }
    };
    /**
     * コンストラクタ.
     */
    @Inject
    public Adas(){

    }
    public boolean isLeftLaneDetecting() {
        return mLeftLaneDetecting;
    }

    public boolean isRightLaneDetecting() {
        return mRightLaneDetecting;
    }

    public boolean isVehicleDetecting() {
        return mVehicleDetecting;
    }

    public boolean isPedestrianDetecting() {
        return mPedestrianDetecting;
    }

    public float getSensorY() {
        return mSensorY;
    }
    public float getSensorZ() {
        return mSensorZ;
    }
    public float getSpeed() {
        return mSpeed;
    }
    public String getFpsString() {
        return mFpsString;
    }
    public float getDashboardRate() {
        return mDashboardRate;
    }
    public float getDashboardRateOffset() {
        return mDashboardRateOffset;
    }

    /**
     * ADAS 初期化.
     *
     * @param cameraImageHeight カメラ解像度高さ
     * @param cameraImageWidth カメラ解像度幅
     * @return 結果
     */
    public boolean init(int cameraImageHeight, int cameraImageWidth) {
        Timber.d("init");
        AdasCameraSetting cameraSetting = mPreferAdas.getAdasCameraSetting();
        int calibrationSetting = mPreferAdas.getAdasCalibrationSetting();

        DasAppParam param = new DasAppParam();
        DasEgoCarInfo carInfo = param.getEgoCarInfo();
        carInfo.setEgoHeight(1500);
        carInfo.setEgoLength(2500);
        carInfo.setEgoWidth(cameraSetting.vehicleWidth);

        DasCameraParam cameraParam = param.getCameraParam();
        cameraParam.setCx(640);
        cameraParam.setCy(360);
        cameraParam.setFx(1959.08f);
        cameraParam.setFy(1956.06f);
        cameraParam.setPitch(0.0f);
        cameraParam.setYaw(0.0f);
        cameraParam.setRoll(0.0f);

        DasImageProcessingInfo processingInfo = param.getImageProcessingInfo();
        processingInfo.setImageWidth(cameraImageWidth);
        processingInfo.setImageHeight(cameraImageHeight);

        DasCameraFixedInfo fixedInfo = param.getCameraFixedInfo();
        fixedInfo.setFixedHeight(cameraSetting.cameraHeight);
        fixedInfo.setFixedCenterOffset(0);
        fixedInfo.setFixedDistanceFromHead(cameraSetting.frontNoseDistance * 10);
        fixedInfo.setVanishingLineRowInPixel(cameraImageHeight / 2);
        mDashboardRate = convertToPixel(cameraImageHeight, calibrationSetting);
        mDashboardRateOffset = getDashBoardOffset(mDashboardRate);
        fixedInfo.setDashboardRowInPixel(mDashboardRateOffset);
        //Timber.d("AdasClass:init:vehicleWidth %s,cameraHeight %s,setFixedDistanceFromHead %s,setVanishingLineRowInPixel %s,setDashboardRowInPixel %s",
        //        cameraSetting.vehicleWidth,cameraSetting.cameraHeight,cameraSetting.frontNoseDistance * 10,cameraImageHeight / 2,convertToPixel(cameraImageHeight, calibrationSetting));
        boolean result = DasEngine.init(mContext, param) == 0;
        if(result) {
            isAdasRelease = false;
            mSensorListener = new TestSensorListener();
            mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            // 注册传感器监听函数
            mSensorManager.registerListener(mSensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
            mTimer= new Timer();
            task = new TimeOutTask();
            mTimer.schedule(task, 0, 1000); // 1000ms間隔で実行
        }
        checkAdasError();
        setAdasWarningSpeedRange();
        return result;
    }

    private float getDashBoardOffset(float dashboardRate){
        if(dashboardRate>=0f&&dashboardRate<0.15f){
            //オフセットなし
        }else if(dashboardRate>=0.15f&&dashboardRate<0.20f){
            dashboardRate = dashboardRate + 0.05f;
        }else if(dashboardRate>=0.20f&&dashboardRate<0.25f){
            dashboardRate = dashboardRate + 0.10f;
        }else if(dashboardRate>=0.25f&&dashboardRate<0.30f){
            dashboardRate = dashboardRate + 0.15f;
        }else if(dashboardRate>=0.30f){
            dashboardRate = 0.50f;
        }
        return dashboardRate;
    }

    private void setAdasWarningSpeedRange(){
        AppStatus appStatus = mStatusCase.execute().getAppStatus();
        adasLdwMinSpeed = appStatus.adasLdwMinSpeed;
        adasLdwMaxSpeed = appStatus.adasLdwMaxSpeed;
        adasPcwMinSpeed = appStatus.adasPcwMinSpeed;
        adasPcwMaxSpeed = appStatus.adasPcwMaxSpeed;
        adasFcwMinSpeed = appStatus.adasFcwMinSpeed;
        adasFcwMaxSpeed = appStatus.adasFcwMaxSpeed;
    }

    public boolean checkAdasPermission(){
        boolean result= true;
        AppStatus appStatus = mStatusCase.execute().getAppStatus();
        Set<AdasErrorType> errors = appStatus.adasErrorList;
        mIsAdasError = errors.size() > 0;
        //位置情報のアクセス許可がOFFの場合、アクセス許可OFFエラー表示
        int fineLocationAccess = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocationAccess = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION);
        if(fineLocationAccess!=PackageManager.PERMISSION_GRANTED||coarseLocationAccess!=PackageManager.PERMISSION_GRANTED){
            errors.add(AdasErrorType.PERMISSION_DENIED_ACCESS_LOCATION);
            result = false;
        }else{
            errors.remove(AdasErrorType.PERMISSION_DENIED_ACCESS_LOCATION);
        }
        //カメラのアクセス許可がOFFの場合、アクセス許可OFFエラー表示
        int cameraAccess = ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
        if (cameraAccess != PackageManager.PERMISSION_GRANTED) {
            errors.add(AdasErrorType.PERMISSION_DENIED_CAMERA);
            result = false;
        }else{
            errors.remove(AdasErrorType.PERMISSION_DENIED_CAMERA);
        }
        if ((errors.size()>0)!=mIsAdasError) {
            mEventBus.post(new AdasErrorEvent());
        }
        return result;
    }

    public void checkAdasError(){
        //車載機がネットワークモード中の場合、車載機から音が出ない異常系エラー表示
        CarDeviceSpec spec = mStatusCase.execute().getCarDeviceSpec();
        AudioOutputMode mode = spec.audioSettingSpec.audioOutputMode;
        AppStatus appStatus = mStatusCase.execute().getAppStatus();
        Set<AdasErrorType> errors = appStatus.adasErrorList;
        mIsAdasError = errors.size() > 0;
        Configuration config = mContext.getResources().getConfiguration();
        int orientation = config.orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT){
            errors.add(AdasErrorType.ORIENTATION_PORTRAIT);
        }else{
            errors.remove(AdasErrorType.ORIENTATION_PORTRAIT);
        }
        if (mPreferAdas.getAdasAlarmEnabled()&&mode == AudioOutputMode.TWO_WAY_NETWORK) {
            errors.add(AdasErrorType.ALARM_ERROR_DURING_NETWORK_MODE);
        } else {
            errors.remove(AdasErrorType.ALARM_ERROR_DURING_NETWORK_MODE);
        }
        //ソースOFFの場合、車載機から音が出ない異常系エラー表示
        MediaSourceType currentSourceType = mStatusCase.execute().getCarDeviceStatus().sourceType;
        if (mPreferAdas.getAdasAlarmEnabled()&&currentSourceType == MediaSourceType.OFF) {
            errors.add(AdasErrorType.ALARM_ERROR_SOURCE_OFF);
        } else {
            errors.remove(AdasErrorType.ALARM_ERROR_SOURCE_OFF);
        }
        if ((errors.size()>0)!=mIsAdasError) {
            mEventBus.post(new AdasErrorEvent());
        }
    }

    /**
     * ADAS 解放.
     */
    public void release(){
        Timber.d("release");
        isAdasRelease = true;
        //タイマーの停止処理
        if(mTimer!=null) {
            mTimer.cancel();
            mTimer = null;
        }
        // 注销监听函数
        mSensorManager.unregisterListener(mSensorListener);
        mFpsString = "CapFps:\nAlFps:\nAvFps:\nApFps:";
        mSpeed = INVALID;
        mSensorY = INVALID;
        mSensorZ = INVALID;
        mDashboardRate = INVALID;
        mDashboardRateOffset = INVALID;
        DasEngine.release();
    }

    /**
     * ADAS 解析処理.
     *
     * @param data 画像データ
     */
    public void process(byte[] data) {
        //Timber.d("process");
        if(isAdasRelease)return;
        DasEgoStatus egoStatus = createDasEgoStatus();
        DasEvents event = new DasEvents();
        DasTrafficEnvironment env = new DasTrafficEnvironment();
        DasStatInfo info = new DasStatInfo();

        DasEngine.process(data, egoStatus);
        DasEngine.getResults(event, env);

        DasEngine.getStatInfo(info);
        //TODO フレームレートが下がることでADASの解析がうまくいかない様に感じる(Fps 0~15:解析されない 15~30:解析される)ため、Fpsに関するログを出力。
        //FPSが＊＊以下の場合、認識率低下エラー表示
        AppStatus appStatus = mStatusCase.execute().getAppStatus();
        Set<AdasErrorType> errors = appStatus.adasErrorList;
        mIsAdasError = errors.size() > 0;
        if(info.getCapFps()<=ADAS_FRAME_RATE_MIN) {
            errors.add(AdasErrorType.DECLINE_IN_RECOGNITION_RATE);
        }else{
            errors.remove(AdasErrorType.DECLINE_IN_RECOGNITION_RATE);
        }
        //Timber.d(String.format("[CapFps:%d, AlFps:%d, AvFps:%d, ApFps:%d]", info.getCapFps(), info.getAlFps(), info.getAvFps(), info.getApFps()));
        mFpsString =String.format(Locale.ENGLISH,"CapFps:%d\nAlFps:%d\nAvFps:%d\nApFps:%d", info.getCapFps(), info.getAlFps(), info.getAvFps(), info.getApFps());
        if ((errors.size()>0)!=mIsAdasError) {
            mIsAdasError = errors.size()>0;
            mEventBus.post(new AdasErrorEvent());
        }
        EnumSet<AdasWarningEvent> adasWarningEvents = parseAdasWarningEvents(event);
        if (!mAdasWarningEvents.equals(adasWarningEvents)) {
            // 警告イベント状態に変化がある
            mAdasWarningEvents = adasWarningEvents;
            AppStatus status = mStatusCase.execute().getAppStatus();

            //フラグが一瞬しか立たないため、warning無し時の変更の通知を遅らせる
            if(!mIsAdasError&&adasWarningEvents.size()>0) {
                Timber.d("Adas Warning Event");
                status.adasWarningEvents = adasWarningEvents;
                mHandler.removeCallbacks(mRunnable);
                mEventBus.post(new AdasWarningUpdateEvent());
                mHandler.postDelayed(mRunnable,2000);
            }
            //Timber.d("AdasClass:mAdasWarningEvents.size() %s",adasWarningEvents.size());
        }
        //カメラの視界が白線/車/人を検知できない時間が15分以上続いた場合、低照度・あるいは視界不良エラー表示
        //TODO:警告がすべて有効になっている時のみ？
        if((mPreferAdas.getFunctionSetting(AdasFunctionType.FCW).settingEnabled)||(mPreferAdas.getFunctionSetting(AdasFunctionType.LDW).settingEnabled)
                ||(mPreferAdas.getFunctionSetting(AdasFunctionType.PCW).settingEnabled)){
            isADASWork(env);
        }

    }

    private void isADASWork(DasTrafficEnvironment env){
        int mLaneMarkingsNum = env.getLaneMarkings().getNums();
        int	mVehicleNum = env.getVehicles().getNums();
        int mPedestrainNum =  env.getPedestrians().getNums();
        //TODO:PCWを塞ぐ
        mPedestrainNum =0 ;
        DasLaneMarkings.DasEgoLane dasEgoLane = env.getLaneMarkings().getDasEgoLane();
        int leftLaneIndex = dasEgoLane.getLeftIndex();
        int rightLaneIndex = dasEgoLane.getRightIndex();
        boolean adasDetected;
        AppStatus appStatus = mStatusCase.execute().getAppStatus();
        if(leftLaneIndex == -1 &&rightLaneIndex == -1&& mVehicleNum == 0 &&mPedestrainNum == 0){
            adasDetected = false;
        }else{
            //Timber.d("AdasClass:isADASWork:mLaneMarkingsNum %s,mVehicleNum %s,mPedestrainNum %s",mLaneMarkingsNum,mVehicleNum,mPedestrainNum);
            adasDetected = true;
            task.cancel();

            mTimeToDetectionError = 0;
            Set<AdasErrorType> errors = appStatus.adasErrorList;
            mIsAdasError = errors.size() > 0;
            errors.remove(AdasErrorType.LOW_ILLUMINANCE_OR_POOR_VISIBILITY);
            task = new TimeOutTask();
            if(mTimer!=null) {
                mTimer.schedule(task, 0, 1000); // 1000ms間隔で実行
            }
        }
        if(adasDetected!=appStatus.adasDetected){
            appStatus.adasDetected = adasDetected;
            mEventBus.post(new AdasErrorEvent());
        }

        if(appStatus.homeViewAdas&&((leftLaneIndex>=0)!=mLeftLaneDetecting||(rightLaneIndex>=0)!=mRightLaneDetecting
                ||(mVehicleNum>0)!=mVehicleDetecting||(mPedestrainNum>0)!=mPedestrianDetecting)){

            Timber.d("mLaneMarkingsNum = %d, leftLaneIndex = %d, rightLaneIndex = %d",mLaneMarkingsNum, leftLaneIndex, rightLaneIndex);
            mLeftLaneDetecting = leftLaneIndex>=0;
            mRightLaneDetecting = rightLaneIndex>=0;
            mVehicleDetecting = mVehicleNum>0;
            mPedestrianDetecting = mPedestrainNum>0;
            mEventBus.post(new AdasDetectingEvent());

        }
    }

    /**
     * LDW設定.
     */
    public void setLdw() {
        AdasFunctionSetting setting = mPreferAdas.getFunctionSetting(AdasFunctionType.LDW);

        DasLDWParam ldwParam = new DasLDWParam();
        ldwParam.setSensitivityOfSolidMode(setting.getSensitivity().code);
        ldwParam.setSensitivityOfDashMode(setting.getSensitivity().code);
        ldwParam.setmSensitivityOfLKWMode(mPreferAdas.getFunctionSetting(AdasFunctionType.LKW).getSensitivity().code);
        ldwParam.setWarningStartSpeedOfSolidMode(30);
        ldwParam.setWarningStartSpeedOfDashMode(30);
        ldwParam.setWarningStartSpeedOfWanderingAcross(30);
        ldwParam.setWanderingAcrossLaneTimeThresh(5.0f);
        DasEngine.setLDWParam(ldwParam);
    }

    /**
     * FCW設定.
     */
    public void setFcw() {
        AdasFunctionSetting setting = mPreferAdas.getFunctionSetting(AdasFunctionType.FCW);

        DasFCWParam fcwParam = new DasFCWParam();
        fcwParam.setSensitivityOfTTC(setting.getSensitivity().code);
        fcwParam.setSensitivityOfHeadway(setting.getSensitivity().code);
        fcwParam.setSensitivityOfVirtualBumper(DasFCWParam.WARNING_SENSITIVITY_MIDDLE);
        fcwParam.setWarningStartSpeedOfTTC(5);
        fcwParam.setWarningStartSpeedOfHeadway(30);
        fcwParam.setFrontVehicleMovingDistanceThresh(5000);
        DasEngine.setFCWParam(fcwParam);
    }

    /**
     * PCW設定.
     */
    public void setPcw() {
        AdasFunctionSetting setting = mPreferAdas.getFunctionSetting(AdasFunctionType.PCW);

        DasPCWParam pcwParam = new DasPCWParam();
        pcwParam.setSensitivity(setting.getSensitivity().code);
        pcwParam.setWarningEndSpeed(30);
        DasEngine.setPCWParam(pcwParam);
    }

    /**
     * 警告解除.
     */
    public void releaseWarning() {
        mAdasWarningEvents = EnumSet.noneOf(AdasWarningEvent.class);
        SmartPhoneStatus status = mStatusCase.execute().getSmartPhoneStatus();
        status.adasWarningEvents = mAdasWarningEvents;
        mEventBus.post(new AdasWarningUpdateEvent());
    }

    /**
     * エラー状態リセット.
     */
    public void resetAdasError(){
        AppStatus appStatus = mStatusCase.execute().getAppStatus();
        appStatus.adasErrorList = EnumSet.noneOf(AdasErrorType.class);
        mTimeToDetectionError = 0;
        mEventBus.post(new AdasErrorEvent());
    }

    /**
     * 設定完了済か否か
     *
     * @return 設定完了済か否か
     */
    public boolean isSettingFinished() {
        return mPreferAdas.isAdasSettingConfigured();
    }

    private float convertToPixel(int cameraImageHeight, int value) {
        Point point = new Point();
        WindowManager wm = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display disp = wm.getDefaultDisplay();
        disp.getSize(point);

        float fYRatio = (float) (cameraImageHeight) / (float) (point.y);
        //return Math.round(value * fYRatio);
		//画面の高さに対する車先端高さの比率
        return (float)value/ (point.y);
    }

    private DasEgoStatus createDasEgoStatus() {
        CarRunningStatus status = mStatusCase.execute().getCarRunningStatus();
        DasEgoStatus egoStatus = new DasEgoStatus();
        egoStatus.setTimestamp(System.currentTimeMillis());
        int speed = (int) Math.round(status.speed);
        int debugSpeed = mStatusCase.execute().getAppStatus().adasCarSpeed;
        if(debugSpeed > 0) {
            speed = debugSpeed;
        }
        mSpeed = speed;
        if(speed<0)speed=0;
        setspeedFunction(speed);
        egoStatus.setModuleRunTable(getRunTable(speed));
        egoStatus.setSpeed(speed); // GPSで取得されるスピード(時速)
        egoStatus.setSpeedStatus(DasEgoStatus.SPEED_STATUS_GPS);
        egoStatus.setYawRateStatus(1);
        egoStatus.setSteeringStatus(DasEgoStatus.STEERING_STATUS_STRAIGHT);
        if(mPreferAdas.getFunctionSetting(AdasFunctionType.LDW).settingEnabled){
            egoStatus.setmDsmStatus(1);
            egoStatus.setmGsensorY(mSensorY);
        }else{
            egoStatus.setmDsmStatus(0);
            egoStatus.setmGsensorY(0);
        }
        egoStatus.setSteeringAngle(0);
        //Timber.d("AdasClass:createDasEgoStatus:speed %s ,setModuleRunTable %s , y %s", speed,getRunTable(speed),y);
        egoStatus.setAccelerationStatus(DasEgoStatus.ACCELERATION_STATUS_INVALID);
        egoStatus.setCarLampStatus(DasEgoStatus.CAR_LAMP_STATUS_INVALID);
        egoStatus.setWindscreenWiperStatus(DasEgoStatus.WINDSCREEN_WIPER_STATUS_INVALID);
        egoStatus.setWeatherCondition(DasEgoStatus.WEATHER_CONDITION_INVALID);
        egoStatus.setLightCondition(DasEgoStatus.LIGHT_CONDITION_INVALID);
        return egoStatus;
    }

    class TestSensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                mSensorY = (float)(Math.round(event.values[1]*1000000))/1000000;
                mSensorZ = (float)(Math.round(event.values[2]*1000000))/1000000;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //Log.i(TAG, "onAccuracyChanged");
        }

    }

    /**
     * ADAS警告イベント変換.
     * <p>
     * ADASライブラリのDasEventsから{@link AdasWarningEvent}のEnumSetに変換する。
     *
     * @param event ADAS警告イベント集
     * @return EnumSetに変換されたADAS警告イベント集
     */
    private EnumSet<AdasWarningEvent> parseAdasWarningEvents(DasEvents event) {
        EnumSet<AdasWarningEvent> events = EnumSet.noneOf(AdasWarningEvent.class);

        if (event.parseEventCode(DasEvents.OFF_ROAD_LEFT_SOLID_EVENT)) {
            events.add(AdasWarningEvent.OFF_ROAD_LEFT_SOLID_EVENT);
        }
        if (event.parseEventCode(DasEvents.OFF_ROAD_RIGHT_SOLID_EVENT)) {
            events.add(AdasWarningEvent.OFF_ROAD_RIGHT_SOLID_EVENT);
        }
//        if (event.parseEventCode(DasEvents.WANDERING_ACROSS_SOLID_LANE_EVENT)) {
//            events.add(AdasWarningEvent.WANDERING_ACROSS_SOLID_LANE_EVENT);
//        }
        if (event.parseEventCode(DasEvents.OFF_ROAD_LEFT_DASH_EVENT)) {
            events.add(AdasWarningEvent.OFF_ROAD_LEFT_DASH_EVENT);
        }
        if (event.parseEventCode(DasEvents.OFF_ROAD_RIGHT_DASH_EVENT)) {
            events.add(AdasWarningEvent.OFF_ROAD_RIGHT_DASH_EVENT);
        }
//        if (event.parseEventCode(DasEvents.WANDERING_ACROSS_DASH_LANE_EVENT)) {
//            events.add(AdasWarningEvent.WANDERING_ACROSS_DASH_LANE_EVENT);
//        }
        if (event.parseEventCode(DasEvents.FORWARD_TTC_COLLISION_EVENT)) {
            events.add(AdasWarningEvent.FORWARD_TTC_COLLISION_EVENT);
        }
        if (event.parseEventCode(DasEvents.FORWARD_HEADWAY_COLLISION_EVENT)) {
            events.add(AdasWarningEvent.FORWARD_HEADWAY_COLLISION_EVENT);
        }
//        if (event.parseEventCode(DasEvents.VIRTUAL_BUMPER_COLLISION_EVENT)) {
//            events.add(AdasWarningEvent.VIRTUAL_BUMPER_COLLISION_EVENT);
//        }
//        if (event.parseEventCode(DasEvents.FRONT_VEHICLE_MOVING_EVENT)) {
//            events.add(AdasWarningEvent.FRONT_VEHICLE_MOVING_EVENT);
//        }
        //TODO:PCWを塞ぐ
/*        if (event.parseEventCode(DasEvents.PEDESTRIAN_WARNING_EVENT)) {
            events.add(AdasWarningEvent.PEDESTRIAN_WARNING_EVENT);
        }
        if (event.parseEventCode(DasEvents.PEDESTRIAN_CAREFUL_EVENT)) {
            events.add(AdasWarningEvent.PEDESTRIAN_CAREFUL_EVENT);
        }*/
//        if (event.parseEventCode(DasEvents.PEDESTRIAN_SAFE_EVENT)) {
//            events.add(AdasWarningEvent.PEDESTRIAN_SAFE_EVENT);
//        }
        //TODO:LKWを塞ぐ
/*        if (event.parseLKWEventCode()){
            events.add(AdasWarningEvent.LANE_KEEP_WARNING_EVENT);
        }*/
        return events;
    }

    private int getRunTable(int speed) {
        final int LDW_RUN = 0x00000001;
        final int FCW_RUN = 0x00000002;
        final int PDW_RUN_DEF = 0x00000004;
        final int PDW_RUN_TRA = 0x00000008;
        final int LKW_RUN = 0x00000010;

        int runTable = 0;

        ArrayList<Integer> lstOpen = new ArrayList<>();
        if (mPreferAdas.getFunctionSetting(AdasFunctionType.FCW).settingEnabled) {
            if(FCWisopen.equals("open")) {
                lstOpen.add(FCW_RUN);
            }
        }
        if (mPreferAdas.getFunctionSetting(AdasFunctionType.LDW).settingEnabled) {
            if(LDWisopen.equals("open")) {
                lstOpen.add(LDW_RUN);
            }
        }
        if (mPreferAdas.getFunctionSetting(AdasFunctionType.PCW).settingEnabled) {
            if(PDWisopen.equals("open")) {
                lstOpen.add(PDW_RUN_DEF);
                lstOpen.add(PDW_RUN_TRA);
            }
        }
//		if (!TextUtils.isEmpty(lkw_open) && lkw_open.equals("open")) {
//			lstOpen.add(LKW_RUN);
//		}
        if (lstOpen.size() > 0) {
            for (int i = 0; i < lstOpen.size(); i++) {
                if (i == 0) {
                    runTable = lstOpen.get(i);
                } else {
                    runTable = runTable | lstOpen.get(i);
                }
            }
        }

        return runTable;
    }

    private void setspeedFunction(int speed) {
        AppStatus appStatus = mStatusCase.execute().getAppStatus();
        if (mPreferAdas.getFunctionSetting(AdasFunctionType.FCW).settingEnabled) {
            if (speed < adasFcwMinSpeed) {
                FCWisopen = "";
            } else if (speed >= adasFcwMaxSpeed) {
                FCWisopen = "";
            } else {
                FCWisopen =  "open";
            }
        }
        if(mPreferAdas.getFunctionSetting(AdasFunctionType.LDW).settingEnabled){
            if (speed < adasLdwMinSpeed) {
                LDWisopen = "";
            } else if (speed >= adasLdwMaxSpeed) {
                LDWisopen = "";
            } else {
                LDWisopen =  "open";
            }
        }
        if(mPreferAdas.getFunctionSetting(AdasFunctionType.PCW).settingEnabled){
            //TODO:PCWを塞ぐ
            mPreferAdas.setFunctionEnabled(AdasFunctionType.PCW,false);
            if (speed < adasPcwMinSpeed) {
                PDWisopen = "";
            } else if (speed >= adasPcwMaxSpeed) {
                PDWisopen = "";
            } else {
                PDWisopen =  "open";
            }
        }
    }

}
