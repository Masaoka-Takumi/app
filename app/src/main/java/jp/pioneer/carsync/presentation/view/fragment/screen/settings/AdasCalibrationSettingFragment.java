package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.AdasCalibrationSettingPresenter;
import jp.pioneer.carsync.presentation.view.AdasCalibrationSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

import static android.content.Context.SENSOR_SERVICE;

/**
 * ADAS Calibration設定画面.
 */
@RuntimePermissions
public class AdasCalibrationSettingFragment extends AbstractScreenFragment<AdasCalibrationSettingPresenter, AdasCalibrationSettingView>
        implements AdasCalibrationSettingView, SensorEventListener {
    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    /**
     * Orientation of the camera sensor
     */
    private int mSensorOrientation;

    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;
    private static final float MAX_ACCELERATE = 9.8f;
    public static final float ACCELERATE_Z_OK_RANGE_MIN = -0.032f;//G
    public static final float ACCELERATE_Z_OK_RANGE_MAX = 0.0f;//G
    public static final float ACCELERATE_Y_OK_RANGE_MAX = 0.025f;//G
    private static float accelerateZRangeMin = ACCELERATE_Z_OK_RANGE_MIN*MAX_ACCELERATE;
    private static float accelerateZRangeMax = ACCELERATE_Z_OK_RANGE_MAX*MAX_ACCELERATE;
    private static float accelerateYRangeMin = -ACCELERATE_Y_OK_RANGE_MAX*MAX_ACCELERATE;
    private static float accelerateYRangeMax = ACCELERATE_Y_OK_RANGE_MAX*MAX_ACCELERATE;
    @Inject AdasCalibrationSettingPresenter mPresenter;
    @BindView(R.id.title) TextView mTitle;
    @BindView(R.id.back_btn) ImageButton mBackBtn;
    @BindView(R.id.next_btn) ImageButton mNextBtn;
    @BindView(R.id.cross_ball) ImageView mCrossBall;
    @BindView(R.id.ok_button) ImageView mOkBtn;
    @BindView(R.id.skip_btn) RelativeLayout mSkipBtn;
    @BindView(R.id.surfaceView) SurfaceView mSurfaceView;
    @BindView(R.id.text_x) TextView xTextView;
    @BindView(R.id.text_y) TextView yTextView;
    @BindView(R.id.text_z) TextView zTextView;
    private View mView;
    private Unbinder mUnbinder;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private static final String TAG = "AdasCalibrationSetting";
    private CameraDevice backCameraDevice=null;
    private CameraCaptureSession backCameraSession=null;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private boolean  mBackCameraSessionStarted = false;
    private int mWidth; //画面の横幅
    private int mHeight; //画面の高さ
    private float mBallX; //ボールのX位置
    private float mBallY; //ボールのY位置
    private boolean  mIsFirst = true;
    private float mGravity[] = new float[3];
    private int mRotation;//画面の向き
    private int mOrientation;//画面の向き
    /**
     * コンストラクタ
     */
    public AdasCalibrationSettingFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return AdasCalibrationSettingFragment
     */
    public static AdasCalibrationSettingFragment newInstance(Bundle args) {
        AdasCalibrationSettingFragment fragment = new AdasCalibrationSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adas_calibration_sensor, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        getPresenter().setArgument(getArguments());
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        mRotation = display.getRotation();
        mWidth = point.x;
        mHeight = point.y;
        mView = view;
        mOrientation = getResources().getConfiguration().orientation;
        mSensorManager = (SensorManager)getActivity().getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerateYRangeMin = -getPresenter().getAppStatus().adasAccelerateYRange*MAX_ACCELERATE;
        accelerateYRangeMax = getPresenter().getAppStatus().adasAccelerateYRange*MAX_ACCELERATE;
        accelerateZRangeMin = getPresenter().getAppStatus().adasAccelerateZRangeMin*MAX_ACCELERATE;
        accelerateZRangeMax = getPresenter().getAppStatus().adasAccelerateZRangeMax*MAX_ACCELERATE;
        mGlobalLayoutListener = () -> {
            mWidth = view.getWidth();
            mHeight = view.getHeight();
            Timber.i("OnGlobalLayoutListener#onGlobalLayout() " +
                    "Width = " + String.valueOf(mWidth) + ", " +
                    "Height = " + String.valueOf(mHeight) + ", " +
                    "Rotation = " + String.valueOf(mRotation));
            // removeOnGlobalLayoutListener()の削除
            view.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        };
        view.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
        xTextView.setVisibility(View.INVISIBLE);
        yTextView.setVisibility(View.INVISIBLE);
        zTextView.setVisibility(View.INVISIBLE);
        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        mView.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        mSensor=null;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
    }

    //ホームボタンを押した際などにカメラを開放する（開放しないと裏で動き続ける・・・）
    @Override
    public void onPause() {
        // カメラセッションの終了
        if (backCameraSession != null&&mBackCameraSessionStarted) {
            try {
                backCameraSession.stopRepeating();
                backCameraSession.close();
            } catch (CameraAccessException e) {
                Timber.e(e.toString());
            } catch (IllegalStateException e) {
                Timber.e(e.toString());
            }
            backCameraSession = null;
        }

        // カメラデバイスとの切断
        if (backCameraDevice != null) {
            backCameraDevice.close();
            backCameraDevice = null;
        }
        mBackCameraSessionStarted = false;
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        AdasCalibrationSettingFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void startCamera(){
        AdasCalibrationSettingFragmentPermissionsDispatcher.setCameraWithCheck(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()){
            //加速度に変化があったら
            case Sensor.TYPE_ACCELEROMETER :
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                mRotation = display.getRotation();
                display = null;
                float x =0, y = 0, z = 0;
                //ローパスフィルター
                // alpha is calculated as t / (t + dT)
                // with t, the low-pass filter's time-constant
                // and dT, the event delivery rate
                final float alpha = 0.8f;
                if(mIsFirst){
                    mGravity[0] = event.values[0];//x軸の加速度
                    mGravity[1] = event.values[1];//y軸の加速度
                    mGravity[2] = event.values[2];//z軸の加速度
                    mIsFirst = false;
                }else{
                    mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0];
                    mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1];
                    mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2];
                }
                x = mGravity[0];
                y = mGravity[1];
                z = mGravity[2];
                xTextView.setText(String.valueOf(event.values[0]));
                yTextView.setText(String.valueOf(event.values[1]));
                zTextView.setText(String.valueOf(event.values[2]));
                float xLevel = 0, yLevel = 0;
                if(mRotation==Surface.ROTATION_0||mRotation==Surface.ROTATION_90){
                    xLevel = (float)mWidth/2 + y*(mWidth/(MAX_ACCELERATE*2));
                }else{
                    xLevel = (float)mWidth/2 - y*(mWidth/(MAX_ACCELERATE*2));
                }
                yLevel = (float)mHeight/2 + z*(mHeight/(MAX_ACCELERATE*2));
                mBallX = xLevel - (float)mCrossBall.getWidth()/2;
                mBallY = yLevel - (float)mCrossBall.getHeight()/2;
                mCrossBall.setX(mBallX);
                mCrossBall.setY(mBallY);
                if(y>=accelerateYRangeMin&&y<=accelerateYRangeMax
                        &&z>=accelerateZRangeMin&&z<=accelerateZRangeMax){
                    mOkBtn.setX(mBallX);
                    mOkBtn.setY(mBallY);
                    if(mOkBtn.getVisibility()!=View.VISIBLE) {
                        mOkBtn.setVisibility(View.VISIBLE);
                    }
                }else{
                    if(mOkBtn.getVisibility()==View.VISIBLE) {
                        mOkBtn.setVisibility(View.INVISIBLE);
                    }
                }
                //Timber.d("Acceleration:x = %f, y = %f, z = %f",x, y, z);
                //Timber.d("Acceleration:xLevel = %f, yLevel= %f",xLevel, yLevel);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION})
    public void setCamera() {
        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        if(mOrientation==Configuration.ORIENTATION_PORTRAIT)return;
        try {
            //カメラIDを取得（背面カメラを選択）
            String backCameraId="";
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics chars
                        = manager.getCameraCharacteristics(cameraId);
                Integer facing = chars.get(CameraCharacteristics.LENS_FACING);

                if (facing != null && facing ==
                        CameraCharacteristics.LENS_FACING_BACK) {
                    backCameraId = cameraId;
                }
            }
            mSurfaceView.setVisibility(View.VISIBLE);
            // SurfaceViewにプレビューサイズを設定する
            CameraCharacteristics characteristics
                    = manager.getCameraCharacteristics(backCameraId);
            StreamConfigurationMap map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            // For still image captures, we use the largest available size.
            Size largest = Collections.max(
                    Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                    new CompareSizesByArea());

            WindowManager winMan = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);

            // Find out if we need to swap dimension to get the preview size relative to sensor
            // coordinate.
            int displayRotation = winMan.getDefaultDisplay().getRotation();
            //noinspection ConstantConditions
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            boolean swappedDimensions = false;
            switch (displayRotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                        swappedDimensions = true;
                    }
                    break;
                default:
                    Log.e(TAG, "Display rotation is invalid: " + displayRotation);
            }

            Point displaySize = new Point();
            winMan.getDefaultDisplay().getSize(displaySize);
            int width = displaySize.x;
            int height = displaySize.y;
            int rotatedPreviewWidth = width;
            int rotatedPreviewHeight = height;
            int maxPreviewWidth = displaySize.x;
            int maxPreviewHeight = displaySize.y;

            if (swappedDimensions) {
                rotatedPreviewWidth = height;
                rotatedPreviewHeight = width;
                maxPreviewWidth = displaySize.y;
                maxPreviewHeight = displaySize.x;
            }

            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                maxPreviewWidth = MAX_PREVIEW_WIDTH;
            }

            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                maxPreviewHeight = MAX_PREVIEW_HEIGHT;
            }
            Size aspectSize = new Size(MAX_PREVIEW_WIDTH,MAX_PREVIEW_HEIGHT);
            // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
            // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
            // garbage capture data.
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.JPEG),
                    rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                    maxPreviewHeight, aspectSize);
            ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) mSurfaceView.getLayoutParams();

            // We fit the aspect ratio of TextureView to the size of preview we picked.
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                lp.width = mPreviewSize.getWidth(); //横幅
                lp.height = mPreviewSize.getHeight(); //縦幅*/
            } else {
                lp.width = mPreviewSize.getHeight(); //縦幅*/
                lp.height = mPreviewSize.getWidth(); //横幅
            }
            //SurfaceViewは画面サイズ
            lp.width = width;
            lp.height = height;
            mSurfaceView.setLayoutParams(lp);
            mSurfaceView.getHolder().setFixedSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            // カメラオープン(オープンに成功したときに第2引数のコールバッククラスが呼ばれる)
            manager.openCamera(backCameraId, new OpenCameraCallback(), null);
        } catch (CameraAccessException e) {
            //例外処理を記述
            Timber.e(e.toString());
        }
    }

    /**
     * カメラデバイス接続完了後のコールバッククラス
     */
    private class OpenCameraCallback extends CameraDevice.StateCallback {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            if(!isResumed())return;
            backCameraDevice = cameraDevice;
            // プレビュー用のSurfaceViewをリストに登録
            ArrayList<Surface> surfaceList = new ArrayList<Surface>();
            surfaceList.add(mSurfaceView.getHolder().getSurface());

            try {
                // プレビューリクエストの設定（SurfaceViewをターゲットに）
                mPreviewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mPreviewRequestBuilder.addTarget(mSurfaceView.getHolder().getSurface());

                // キャプチャーセッションの開始(セッション開始後に第2引数のコールバッククラスが呼ばれる)
                cameraDevice.createCaptureSession(surfaceList, new CameraCaptureSessionCallback(), null);

            } catch (CameraAccessException e) {
                // エラー時の処理を記載
                Timber.e(e.toString());
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            // 切断時の処理を記載
            Timber.e("CameraDevice onDisconnected" );
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            // エラー時の処理を記載
            Timber.e("CameraDevice onError" );
        }
    }

    /**
     * カメラが起動し使える状態になったら呼ばれるコールバック
     */
    private class CameraCaptureSessionCallback extends CameraCaptureSession.StateCallback {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            if(!isResumed())return;
            mBackCameraSessionStarted = true;
            backCameraSession = session;
            try {
                // オートフォーカスの設定
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_START);

                // プレビューの開始(撮影時に第2引数のコールバッククラスが呼ばれる)
                session.setRepeatingRequest(mPreviewRequestBuilder.build(), new CaptureCallback(), null);

            } catch (CameraAccessException e) {
                Timber.e(e.toString());
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            //失敗時の処理を記載
            Timber.e("CameraCaptureSession onConfigureFailed" );

        }
    }

    /**
     * カメラ撮影時に呼ばれるコールバック関数
     */
    private class CaptureCallback extends CameraCaptureSession.CaptureCallback {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScreenId getScreenId() {
        return ScreenId.CALIBRATION_SETTING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    protected AdasCalibrationSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setBackButtonVisible(boolean visible) {
        if(visible){
            mBackBtn.setVisibility(View.VISIBLE);
        }else{
            mBackBtn.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setSkipButtonVisible(boolean visible) {
        if(visible){
            mSkipBtn.setVisibility(View.VISIBLE);
        }else{
            mSkipBtn.setVisibility(View.INVISIBLE);
        }
    }

    @OnClick(R.id.back_btn)
    public void onClickBackButton() {
        getPresenter().onBackAction();
    }

    @OnClick(R.id.next_btn)
    public void onClickNextButton() {
        getPresenter().onNextAction();
    }

    @OnClick(R.id.ok_button)
    public void onClickOkButton() {
        getPresenter().onNextAction();
    }

    @OnClick(R.id.skip_btn)
    public void onClickSkipButton() {
        getPresenter().onSkipAction();
    }
    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }
}

