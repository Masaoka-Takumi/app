package jp.pioneer.carsync.presentation.view.activity;

import android.Manifest;
import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import jp.pioneer.carsync.BuildConfig;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.ActivityComponent;
import jp.pioneer.carsync.domain.model.CarDeviceErrorType;
import jp.pioneer.carsync.presentation.controller.MainFragmentController;
import jp.pioneer.carsync.presentation.model.SimCountryIso;
import jp.pioneer.carsync.presentation.model.UiColor;
import jp.pioneer.carsync.presentation.presenter.MainPresenter;
import jp.pioneer.carsync.presentation.presenter.ReadingMessageDialogPresenter;
import jp.pioneer.carsync.presentation.util.IabHelper;
import jp.pioneer.carsync.presentation.util.IabResult;
import jp.pioneer.carsync.presentation.util.Inventory;
import jp.pioneer.carsync.presentation.util.Purchase;
import jp.pioneer.carsync.presentation.util.SkuDetails;
import jp.pioneer.carsync.presentation.view.MainView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AbstractDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AccidentDetectDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AdasWarningDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AlexaFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.CautionDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.ParkingSensorDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.PromptAuthorityPermissionDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.SpeechRecognizerDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.PlayerContainerFragment;
import jp.pioneer.carsync.presentation.view.widget.AdasView;
import jp.pioneer.carsync.presentation.view.widget.CustomSurfaceView;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderPlayerInfoItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderTemplateItem;
import jp.pioneer.mbg.alexa.AlexaLoginManager;
import jp.pioneer.mbg.alexa.AmazonAlexaManager;
import jp.pioneer.mbg.alexa.manager.AlexaQueueManager;
import jp.pioneer.mbg.alexa.manager.GeolocationManager;
import jp.pioneer.mbg.logmanager.LogManager;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.PermissionUtils;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

/**
 * CarSyncアプリケーションのActivity
 * <p>
 * 画面遷移の軸になるクラスで、Controllerを通してFragmentの差し替えを行う。
 * サービスからintentを受信した場合は、intentのアクション毎に処理を行う。
 *
 * @see MainFragmentController
 * @see jp.pioneer.carsync.presentation.view.service.ResourcefulService
 */
@RuntimePermissions
public class MainActivity extends AbstractActivity<MainPresenter, MainView>
        implements MainView, SurfaceHolder.Callback, AbstractDialogFragment.Callback, SpeechRecognizerDialogFragment.Callback,
        CautionDialogFragment.Callback, ParkingSensorDialogFragment.Callback, AdasWarningDialogFragment.Callback,StatusPopupDialogFragment.Callback, AccidentDetectDialogFragment.Callback, AlexaFragment.Callback, PromptAuthorityPermissionDialogFragment.Callback {
    public static final String ACTION_SHOW_ACCIDENT_DETECT = "action.show_accident_detect";
    public static final String ACTION_SHOW_PARKING_SENSOR = "action.show_parking_sensor";
    public static final String ACTION_SHOW_ADAS_WARNING = "action.show_adas_warning";
    public static final String ACTION_APP_COMMAND = "action.app_command";
    public static final String ACTION_NAVI_COMMAND = "action.navi_command";
    public static final String ACTION_PHONE_COMMAND = "action.phone_command";
    public static final String ACTION_AV_COMMAND = "action.av_command";
    public static final String ACTION_VOICE_COMMAND = "action.voice_command";
    public static final String ACTION_READING_MESSAGE = "action.reading_message";
    public static final String ACTION_CAR_DEVICE_ERROR = "action.car_device_error";
    public static final String ACTION_SUBSCRIPTION_UPDATE = "action.subscription_update";
    public static final String ACTION_ENTER_LIST = "action.enter_list";
    public static final String ACTION_ALEXA_NAVI_COMMAND = "action.alexa_navi_command";
    public static final String ACTION_PERMISSION_REQUEST = "action.permissionRequest";
    private static final int REQUEST_RECOGNIZE = 1; //音声認識のリクエストコード
    private static final String TAG_DIALOG_CAR_DEVICE_ERROR = "car_device_error";
    private static final int REQUEST_CODE_ASK_FOR_PERMISSION = CautionDialogFragment.REQUEST_CODE_ASK_FOR_PERMISSION;
    private static final String TAG = "Billing";
    private static final String BILLING_BASE64_ENCODED_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAy3HJt4psnaSejbUcVOHUcl9/WgzmGBOVbfiKtAHd9wND/o1vaWvIOJFIHdQiCtvE4zeURCcHgu1BbSgGmQ89bYicoXdKxnFZGObBerJLS+I5tsMocLhVT5Tzzfr4vC0QIvuKPP59aDCkBpXn/+9Uz4RA1yAN4E1wqyWx/3Hk7OcMuoV0eWMN156bUQWuNo9d5I1W5jRmBGLq6cJb14FzWUuPy3Y+9BiwzQpO4d4L+u2O3PhRHiWhv3nSKCRPGpD4b8qSzrd51s6qr7BY9V5YoVFzhHI5mNYaNTQQOfqHInC74j2BpF50Sep2OgRhzYvqaYxO6aKPDA/MW6zcSgGTgQIDAQAB"; //Google Play Developer Consoleからアプリのライセンスキーを入手
    private static final String PRODUCT_ID = "jp.pioneer.carsync.drivingsupporteye";   // Google Play Dev Console上で作成した課金商品のID
    private static final int REQUEST_PERMISSION_DIRECT_CALL = 102; // ダイレクトコール用パーミッション許可リクエストコード;
    private static final int REQUEST_PERMISSION_WRITE_PERMISSION = 104;//通信ログ出力前の外部ストレージ書き込みパーミッション許可リクエストコード
    private static final int RC_REQUEST = 10001; // 購入のリクエストコード;
    private static final int REQUEST_GALLERY = 300; //ギャラリーのリクエストコード
    private static final int REQUEST_CODE_OVERLAY = 10101;//「他のアプリの上に重ねて表示」の権限
    @Inject MainPresenter mPresenter;
    @Inject MainFragmentController mFragmentController;
    private Intent mRecognizeResultIntent;
    @BindView(R.id.background_video_view) CustomSurfaceView mBackgroundVideo;
    @BindView(R.id.adas_view) AdasView mAdas;
    @BindView(R.id.background_image_view) ImageView mBackgroundImage;
    @BindView(R.id.background_image_black) View mBackgroundBlack;
    @BindView(R.id.blurView) BlurView mBlurView;
    private int mOrientation;
    private SurfaceHolder mSurfaceHolder;
    private MediaPlayer mMediaPlayer;
    private Unbinder mUnbinder;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mIsPremium = false;
    private boolean mIsResumed = false;
    private IabHelper mHelper;
    private AmazonAlexaManager mAmazonAlexaManager;
    private AlexaLoginManager mAmazonAlexaLoginManager;
    private AlexaCallback mAlexaCallback = new AlexaCallback();
    private final static boolean mIsDebug = BuildConfig.DEBUG;
    @Override
    protected void doInject(ActivityComponent activityComponent) {
        activityComponent.inject(this);
    }

    @Override
    protected void doCreate(@Nullable Bundle savedInstanceState) {
        Timber.d("doCreate");

/*        if (mIsDebug && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectNonSdkApiUsage()
                    .penaltyLog()
                    .build());
        }*/
        setContentView(R.layout.activity_main);

        // not sleep
        if(getPresenter().isSessionConnected()){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        UiColor uiColor = getPresenter().getUiColor();
        int theme;
        switch (uiColor) {
            case BLUE:
                theme = R.style.AppTheme_Blue;
                break;
            case AQUA:
                theme = R.style.AppTheme_Aqua;
                break;
            case GREEN:
                theme = R.style.AppTheme_Green;
                break;
            case YELLOW:
                theme = R.style.AppTheme_Yellow;
                break;
            case AMBER:
                theme = R.style.AppTheme_Amber;
                break;
            case RED:
                theme = R.style.AppTheme_Red;
                break;
            case PINK:
                theme = R.style.AppTheme_Pink;
                break;
            default:
                theme = R.style.AppTheme_Blue;
                break;
        }
        setTheme(theme);
        mUnbinder = ButterKnife.bind(this, this);
        mFragmentController.setContainerViewId(R.id.container);

        // SurfaceViewにコールバックを設定
        mSurfaceHolder = mBackgroundVideo.getHolder();
        mSurfaceHolder.addCallback(this);

        Configuration config = getResources().getConfiguration();
        mOrientation = config.orientation;

        // ブラーの設定
        final View decorView = getWindow().getDecorView();
        final ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        final Drawable windowBackground = decorView.getBackground();
        mBlurView.setupWith(rootView)
                .windowBackground(windowBackground)
                .blurAlgorithm(new RenderScriptBlur(this))
                .blurRadius(25);

        Intent intent = getIntent();
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_SHOW_ACCIDENT_DETECT:
                    getPresenter().addCommandIntent(ACTION_SHOW_ACCIDENT_DETECT, new Intent(intent));
                    intent.setAction("");
                    setIntent(intent);
                    break;
                case ACTION_SHOW_PARKING_SENSOR:
                    getPresenter().addCommandIntent(ACTION_SHOW_PARKING_SENSOR, new Intent(intent));
                    intent.setAction("");
                    setIntent(intent);
                    break;
                case ACTION_SHOW_ADAS_WARNING:
                    getPresenter().addCommandIntent(ACTION_SHOW_ADAS_WARNING, new Intent(intent));
                    intent.setAction("");
                    setIntent(intent);
                    break;
                case ACTION_APP_COMMAND:
                    getPresenter().addCommandIntent(ACTION_APP_COMMAND, new Intent(intent));
                    intent.setAction("");
                    setIntent(intent);
                    break;
                case ACTION_PHONE_COMMAND:
                    getPresenter().addCommandIntent(ACTION_PHONE_COMMAND, new Intent(intent));
                    intent.setAction("");
                    setIntent(intent);
                    break;
                case ACTION_AV_COMMAND:
                    getPresenter().addCommandIntent(ACTION_AV_COMMAND, new Intent(intent));
                    intent.setAction("");
                    setIntent(intent);
                    break;
                case ACTION_READING_MESSAGE:
                    getPresenter().addCommandIntent(ACTION_READING_MESSAGE, new Intent(intent));
                    intent.setAction("");
                    setIntent(intent);
                    break;
                case ACTION_VOICE_COMMAND:
                    getPresenter().addCommandIntent(ACTION_VOICE_COMMAND, new Intent(intent));
                    intent.setAction("");
                    setIntent(intent);
                    break;
                case ACTION_CAR_DEVICE_ERROR:
                    getPresenter().addCommandIntent(ACTION_CAR_DEVICE_ERROR, new Intent(intent));
                    intent.setAction("");
                    setIntent(intent);
                    break;
                case ACTION_SUBSCRIPTION_UPDATE:
                    getPresenter().addCommandIntent(ACTION_SUBSCRIPTION_UPDATE, new Intent(intent));
                    intent.setAction("");
                    setIntent(intent);
                    break;
                case ACTION_ENTER_LIST:
                    getPresenter().addCommandIntent(ACTION_ENTER_LIST, new Intent(intent));
                    intent.setAction("");
                    setIntent(intent);
                    break;
                case ACTION_ALEXA_NAVI_COMMAND:
                    getPresenter().addCommandIntent(ACTION_ALEXA_NAVI_COMMAND, new Intent(intent));
                    intent.setAction("");
                    setIntent(intent);
                    break;
                case ACTION_PERMISSION_REQUEST:
                    getPresenter().addCommandIntent(ACTION_PERMISSION_REQUEST, new Intent(intent));
                    intent.setAction("");
                    setIntent(intent);
                    break;
            }
        }

        mAdas.setListener(new AdasView.AdasListener() {
            @Override
            public boolean isConfiguredCalibration() {
                return getPresenter().checkConfiguredCalibration();
            }

            @Override
            public boolean onInitialize(int cameraImageHeight, int cameraImageWidth) {
                return getPresenter().initAdas(cameraImageHeight, cameraImageWidth);
            }

            @Override
            public void onRelease() {
                getPresenter().releaseAdas();
            }

            @Override
            public void onError() {
                showToast("Adas Camera Start Error");
                getPresenter().finishAdas();
            }

            @Override
            public void onProcess(byte[] data) {
                if(mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    getPresenter().processAdas(data);
                }
            }
        });

        if (!SpeechRecognizer.isRecognitionAvailable(getApplicationContext())) {
            //SpeechRecognizerが使えない環境
            Timber.e("音声認識が使えません。");

        } else {
            //SpeechRecognizer生成
            getPresenter().initSpeechRecognizer();
        }
        // Logger初期化
        LogManager.getInstance().setup(this);
        mAmazonAlexaManager = AmazonAlexaManager.getInstance();
        mAmazonAlexaManager.setActivity(this);

    }

    @Override
    protected void onStart() {
        Timber.d("onStart");
		//Activity復帰時
        Fragment screen = mFragmentController.getScreenInContainer();
        if (screen instanceof PlayerContainerFragment) {
            ((PlayerContainerFragment) screen).setActionCall(false);
        }
        if (mAmazonAlexaManager != null) {
            mAmazonAlexaManager.autoLoginAlexa();
        }
        super.onStart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case ACTION_SHOW_ACCIDENT_DETECT:
                    // ここではViewの表示ができないため保留
                    getPresenter().addCommandIntent(ACTION_SHOW_ACCIDENT_DETECT, intent);
                    break;
                case ACTION_SHOW_PARKING_SENSOR:
                    // ここではViewの表示ができないため保留
                    getPresenter().addCommandIntent(ACTION_SHOW_PARKING_SENSOR, intent);
                    break;
                case ACTION_VOICE_COMMAND:
                    // ここではViewの表示ができないため保留
                    getPresenter().addCommandIntent(ACTION_VOICE_COMMAND, intent);
                    break;
                case ACTION_SHOW_ADAS_WARNING:
                    // ここではViewの表示ができないため保留
                    getPresenter().addCommandIntent(ACTION_SHOW_ADAS_WARNING, intent);
                    break;
                case ACTION_APP_COMMAND:
                    // ここではViewの表示ができないため保留
                    getPresenter().addCommandIntent(ACTION_APP_COMMAND, intent);
                    break;
                case ACTION_PHONE_COMMAND:
                    // ここではViewの表示ができないため保留
                    getPresenter().addCommandIntent(ACTION_PHONE_COMMAND, intent);
                    break;
                case ACTION_AV_COMMAND:
                    // ここではViewの表示ができないため保留
                    getPresenter().addCommandIntent(ACTION_AV_COMMAND, intent);
                    break;
                case ACTION_READING_MESSAGE:
                    getPresenter().addCommandIntent(ACTION_READING_MESSAGE, intent);
                    break;
                case ACTION_CAR_DEVICE_ERROR:
                    getPresenter().addCommandIntent(ACTION_CAR_DEVICE_ERROR, intent);
                    break;
                case ACTION_SUBSCRIPTION_UPDATE:
                    getPresenter().addCommandIntent(ACTION_SUBSCRIPTION_UPDATE, intent);
                    break;
                case ACTION_ENTER_LIST:
                    getPresenter().addCommandIntent(ACTION_ENTER_LIST, intent);
                    break;
                case ACTION_ALEXA_NAVI_COMMAND:
                    getPresenter().addCommandIntent(ACTION_ALEXA_NAVI_COMMAND, intent);
                    break;
                case ACTION_PERMISSION_REQUEST:
                    getPresenter().addCommandIntent(ACTION_PERMISSION_REQUEST, intent);
                    break;
                default:
                    break;
            }
        }
        super.onNewIntent(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RECOGNIZE) {
            if (resultCode == RESULT_OK) {
                mRecognizeResultIntent = data;
            } else {
                mRecognizeResultIntent = null;
            }
        }else if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            if(data.getData() != null){
                setBackgroundMyPhoto(data.getData());
            }
        }else if (requestCode == REQUEST_CODE_OVERLAY) {
            if (MainPresenter.sIsVersionQ) {
				Timber.d("Overlay:REQUEST_CODE_OVERLAY=" + Settings.canDrawOverlays(this));
                if (Settings.canDrawOverlays(this)) {
                    //ここで権限促し画面を表示すると切断ダイアログを上に出せない
                    //getPresenter().checkOverlayAuthority();
                }
            }
        }

        // 購入結果をActivityが受け取るための設定
        if (mHelper == null) return;
        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent;

        // 事故通知画面表示
        intent = getPresenter().getCommandIntent(ACTION_SHOW_ACCIDENT_DETECT);
        if (intent != null) {
            //Caution表示中は無効
            if(!isShowCaution()) {
                getPresenter().onShowAccidentDetectAction(intent.getExtras());
            }
        }

        // パーキングセンサー画面表示
        intent = getPresenter().getCommandIntent(ACTION_SHOW_PARKING_SENSOR);
        if (intent != null) {
            //Caution表示中は表示しない
            if(!isShowCaution()) {
            	getPresenter().onShowParkingSensorAction(intent.getExtras());
            }
        }

        //Caution表示中は無効
        if(isShowCaution()) {
            getPresenter().removeCommandIntent(ACTION_CAR_DEVICE_ERROR);
        }

        // 車載器エラー画面表示
        intent = getPresenter().getCommandIntent(ACTION_CAR_DEVICE_ERROR);
        if (intent != null) {
            //衝突検知表示中、パーキングセンサー表示中は表示しない
            if (!isShowAccidentDetect() && !isShowParkingSensor()) {
                getPresenter().onShowCarDeviceErrorAction(intent.getExtras());
            }
        }

        //Caution表示中は無効
        if(isShowCaution()) {
            getPresenter().removeCommandIntent(ACTION_SUBSCRIPTION_UPDATE);
        }

        // Subscription Update画面表示
        intent = getPresenter().getCommandIntent(ACTION_SUBSCRIPTION_UPDATE);
        if (intent != null) {
            //衝突検知表示中、パーキングセンサー表示中は表示しない
            if (!isShowAccidentDetect() && !isShowParkingSensor()) {
                getPresenter().onShowSubscriptionUpdateAction(intent.getExtras());
            }
        }

        // ADAS警告画面表示
        intent = getPresenter().getCommandIntent(ACTION_SHOW_ADAS_WARNING);
        if (intent != null) {
            //Caution表示中は表示しない
            if(!isShowCaution()) {
                //上位のダイアログ表示中は無効
                if (!isShowAccidentDetect() && !isShowParkingSensor()&& !isShowCarDeviceErrorDialog()) {
                    if(getScreenId().isAdasWarnVisible()&&!mFragmentController.isShowListDialog()) {
                        getPresenter().onShowAdasWarningAction(intent.getExtras());
                    }
                }
            }
        }

        // ReadingMessageアクション
        intent = getPresenter().getCommandIntent(ACTION_READING_MESSAGE);
        if (intent != null) {
            //Caution表示中、衝突検知表示中、パーキングセンサー表示中は無効
            if (!isShowCaution() && !isShowAccidentDetect() && !isShowParkingSensor() && !isShowCarDeviceErrorDialog()) {
                Bundle bundle = new Bundle();
                bundle.putString(ReadingMessageDialogPresenter.TYPE, ReadingMessageDialogPresenter.TAG_READING);
                getPresenter().onReadingMessageAction(bundle);
            }
        }

        // 音声認識アクション
        intent = getPresenter().getCommandIntent(ACTION_VOICE_COMMAND);
        if (intent != null) {
            //Caution表示中、衝突検知表示中、パーキングセンサー表示中は無効
            if (!isShowCaution() && !isShowAccidentDetect() && !isShowParkingSensor() && !isShowCarDeviceErrorDialog()) {
                getPresenter().prepareRecognizer();
            }
        }

        // Appコマンドアクション
        intent = getPresenter().getCommandIntent(ACTION_APP_COMMAND);
        if (intent != null) {
            //Caution表示中、衝突検知表示中、パーキングセンサー表示中は無効
            if (!isShowCaution() && !isShowAccidentDetect() && !isShowParkingSensor()) {
                getPresenter().onAppCommandAction();
            }
        }

        // Phoneコマンドアクション
        intent = getPresenter().getCommandIntent(ACTION_PHONE_COMMAND);
        if (intent != null) {
            //上位のダイアログ表示中は無効
            if (!isShowCaution() && !isShowAccidentDetect() && !isShowParkingSensor() && !isShowSpeechRecognizerDialog()
                    && !isShowCarDeviceErrorDialog()&&!isShowReadMessageDialog()
                    &&!isShowAlexaDialog()) {
                getPresenter().onPhoneCommandAction();
            }
        }

        // AVコマンドアクション
        intent = getPresenter().getCommandIntent(ACTION_AV_COMMAND);
        if (intent != null) {
            //Caution表示中、衝突検知表示中、パーキングセンサー表示中は無効
            if (!isShowCaution() && !isShowAccidentDetect() && !isShowParkingSensor()) {
                getPresenter().onAvCommandAction();
            }
        }

        // Enter Listコマンドアクション
        intent = getPresenter().getCommandIntent(ACTION_ENTER_LIST);
        if (intent != null) {
            //Caution表示中、衝突検知表示中、パーキングセンサー表示中は無効
            if (!isShowCaution() && !isShowAccidentDetect() && !isShowParkingSensor()) {
                getPresenter().onEnterListCommandAction();
            }
        }
        // ナビ目的地起動コマンドアクション
        intent = getPresenter().getCommandIntent(ACTION_ALEXA_NAVI_COMMAND);
        if (intent != null) {
            //Caution表示中、衝突検知表示中、パーキングセンサー表示中は無効
            if (!isShowCaution() && !isShowAccidentDetect() && !isShowParkingSensor()) {
            	getPresenter().onSetNaviDestination(intent.getExtras());
   			}
        }

        // パーミッション許可リクエストコマンドアクション
        intent = getPresenter().getCommandIntent(ACTION_PERMISSION_REQUEST);
        if (intent != null) {
            String[] PERMISSIONS = {
                    android.Manifest.permission.READ_CONTACTS,
                    android.Manifest.permission.CALL_PHONE,
            };
            ActivityCompat.requestPermissions(this, getDenyPermissions(PERMISSIONS), REQUEST_PERMISSION_DIRECT_CALL);
        }

        //最前面のPopUpのみ表示
        mFragmentController.hideCarDeviceErrorDialog(getPresenter().getStackPopUp());

        getPresenter().showErrorDialog();
        if (mAmazonAlexaManager != null) {
            mAmazonAlexaManager.addAlexaCallback(mAlexaCallback);
            mAmazonAlexaManager.onActivityResume();
        }
    }

    private String[] getDenyPermissions(final String... permissions) {
        final List<String> denyPermissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (permission != null && ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                denyPermissionList.add(permission);
            }
        }
        // 追加したPermissionがない場合でも、空のリストを返す
        return denyPermissionList.toArray(new String[denyPermissionList.size()]);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAmazonAlexaManager != null) {
            mAmazonAlexaManager.removeAlexaCallback(mAlexaCallback);
        }
    }

    @Override
    public void onDestroy() {
        Timber.d("onDestroy");
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mUnbinder.unbind();
        closeBillingHelper();

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);

        if ((requestCode & 0xffff) == REQUEST_CODE_ASK_FOR_PERMISSION) {
            getPresenter().onFinishRequestPermissions();
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                    ||ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                getPresenter().onImpactDetectionChange(false);
            }
        }else if(requestCode == REQUEST_PERMISSION_DIRECT_CALL){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                    &&ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                Timber.d("onRequestPermissionsResult:directCall");
                getPresenter().directCall();
            }else{
                Timber.d("onRequestPermissionsResult:REQUEST_PERMISSION_DIRECT_CALL:DENYED");
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void getWriteExternalPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Timber.d("getWriteExternalPermission:request");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_PERMISSION);
        }
    }

    @Override
    public void checkSim(){
        MainActivityPermissionsDispatcher.setCountryCodeWithCheck(MainActivity.this);
    }

    @NeedsPermission(Manifest.permission.READ_PHONE_STATE)
    public void setCountryCode(){
        Timber.d("setCountryCode");
        getPresenter().setAlexaAvailable(getCountryIso(true));
        getPresenter().setAdasAvailable(getCountryIso(false));
    }

    /**
     * SIM状態の確認
     * MainActivity#getCountryIsoで利用されることを想定
     * @return {@code: true}:SIMが入っている {@code: false}:SIMが入っていない
     */
    private boolean canCheckSim() {
        final TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if(tm == null) {
            return false;
        }

        int simState = tm.getSimState();
        Timber.d("simState=" + simState);
        //SIMが刺さっていない場合は問答無用で使用不可
        //GalaxyJ3ProでSIMが刺さっていない場合SIM_STATE_UNKNOWNが返る
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT: //SimState = “No Sim Found!”;
            case TelephonyManager.SIM_STATE_UNKNOWN: //SimState = “Unknown SIM State!”;
                return false;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED: //SimState = “Network Locked!”;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED: //SimState = “PIN Required to access SIM!”;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED: //SimState = “PUK Required to access SIM!”; // Personal Unblocking Code
            case TelephonyManager.SIM_STATE_READY:
            default:
                return true;
        }
    }

    /**
     * SIM判定処理
     * SIMが挿入されていない場合({@link MainActivity#canCheckSim}で判定)は{@link SimCountryIso#NO_AVAILABLE}
     * 未定義の国情報の場合は{@link SimCountryIso#NO_DEFINE}を返す
     * @param retryGetSimCountryIso
     *  {@link SubscriptionInfo#getCountryIso}が空文字列の場合、{@link TelephonyManager#getSimCountryIso()}での取得を試みるかどうか
     * @return SimCountryIso 対応した値、対応値がない場合は{@link SimCountryIso#NO_DEFINE}
     */
    private SimCountryIso getCountryIso(boolean retryGetSimCountryIso) {
        if(!canCheckSim()) {
            return SimCountryIso.NO_AVAILABLE;
        }

        String countryCode="";

        if(Build.VERSION.SDK_INT >= 22) {
            final SubscriptionManager subscriptionManager;

            if(Build.VERSION.SDK_INT >= 23) {
                subscriptionManager = getSystemService(SubscriptionManager.class);
            }else{
                // API 22以下のために使用
                subscriptionManager = (SubscriptionManager) getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE);
            }

            try {
                final List<SubscriptionInfo> activeSubscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();

                if (activeSubscriptionInfoList != null) {
                    int simCount = activeSubscriptionInfoList.size();
                    Log.d("MainActivity: ", "simCount:" + simCount);

                    for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
                        countryCode = subscriptionInfo.getCountryIso();
                        Log.d("MainActivity: ", "simSlotIndex=" + subscriptionInfo.getSimSlotIndex() + ", carrierName=" + subscriptionInfo.getCarrierName()
                                + ", countryIso=" + countryCode + ", iccId=" + subscriptionInfo.getIccId() + " , displayName=" + subscriptionInfo.getDisplayName());
                    }

                    // SubscriptionInfoから取得できなかった場合はTelephonyManagerから取得を試みる
                    if(retryGetSimCountryIso && TextUtils.isEmpty(countryCode)) {
                        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                        if(tm != null) {
                            countryCode = tm.getSimCountryIso();
                            Log.d("MainActivity: ", "simState=" + tm.getSimState() + ", simCountryIso=" + countryCode);
                        }
                    }
                }
            }catch (SecurityException e){
                Timber.e("getActiveSubscriptionInfoList:SecurityException:" + e.getMessage());
            }

        }else{
            final TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            if(tm!=null) {
                countryCode = tm.getSimCountryIso();
            }
        }
        Log.d("MainActivity: ", "CountryIso : " + countryCode);

        return SimCountryIso.getEnum(countryCode);
    }

    /**
     * ステータスバーを非表示状態にします。
     */
    public void hideStatusBar() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    /**
     * ステータスバーを表示状態にします。
     */
    public void showStatusBar() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        // Show Status Bar.
        int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Timber.d("onKeyDown_KEYCODE_BACK");
            goBack();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @NonNull
    @Override
    protected MainPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void navigate(ScreenId screenId, Bundle args) {
        mFragmentController.navigate(screenId, args);

        setOrientation(screenId);
        getPresenter().suppressDeviceConnection(screenId);
        getPresenter().analyticsActiveScreenByNavigate(screenId);
    }

    @Override
    public void goBack() {
        ScreenId screen = mFragmentController.getScreenIdInContainer();
        if (screen==ScreenId.OPENING||screen==ScreenId.OPENING_EULA||screen==ScreenId.OPENING_PRIVACY_POLICY) {
            return;
        }

        if (!mFragmentController.goBack()) {
            super.onBackPressed();
            //finishAndRemoveTask();
            return;
        }

        setOrientation(mFragmentController.getScreenIdInContainer());
        getPresenter().suppressDeviceConnection(mFragmentController.getScreenIdInContainer());
    }

    @Override
    public void exitSetting() {
        if (getScreenId().isSettings()) {
            mFragmentController.exitSetting();
        }
    }

    @Override
    public void closeDialog(ScreenId screenId) {
        mFragmentController.closeDialog(screenId);

        setOrientation(screenId);
    }

    @Override
    public ScreenId getScreenId() {
        return mFragmentController.getScreenIdInContainer();
    }

    @Override
    public Fragment getScreenInContainer() {
        return mFragmentController.getScreenInContainer();
    }

    @Override
    public void reloadBackground() {
//        mMediaPlayer.reset();
    }

    @Override
    public void changeBackgroundBlur(boolean isBlur) {
        if (mBlurView == null) {
            return;
        }
        if (isBlur) {
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mBlurView.setBlurEnabled(true);
                    mBlurView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            //ダイアログ表示中でも再アニメーション必要な場合がある。
            //if (!mFragmentController.isShowListDialog()) {
            mBlurView.clearAnimation();
            mBlurView.startAnimation(anim);
            //}
        } else {
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (!mFragmentController.isShowListDialog()) {
                        if(mBlurView != null) {
                            mBlurView.setBlurEnabled(false);
                            mBlurView.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mBlurView.startAnimation(anim);
        }
    }

    @Override
    public void setCaptureImage(boolean isEnabled, Uri uri) {
        if (mBackgroundImage == null || mBlurView == null) {
            return;
        }
        if (isEnabled) {
            /*
             * MediaMetadataRetriever#getFrameAtTimeに動画のキーフレームを取得する。
             * 動画のキーフレームが少ない場合は、FFmpegMediaMetadataRetrieverを使用する必要性がある。
             */
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(getApplicationContext(), uri);
            final Bitmap frame = mmr.getFrameAtTime(mMediaPlayer.getCurrentPosition() * 1000);

            Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mBackgroundImage.setImageBitmap(frame);
                    mBackgroundImage.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            if (!mFragmentController.isShowListDialog()) {
                mBackgroundImage.startAnimation(anim);
            }
        } else {
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_out);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (!mFragmentController.isShowListDialog()) {
                        mBackgroundImage.setVisibility(View.GONE);
                        mBackgroundImage.setImageBitmap(null);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mBackgroundImage.startAnimation(anim);
        }
    }

    @Override
    public void changeBackgroundType(boolean isVideo) {
        if (isVideo) {
            mBackgroundVideo.setVisibility(View.VISIBLE);
            mBackgroundImage.setVisibility(View.INVISIBLE);
            mBackgroundBlack.setVisibility(View.GONE);
        } else {
            mBackgroundVideo.setVisibility(View.INVISIBLE);
            mBackgroundImage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void changeBackgroundImage(int id) {
        mBackgroundImage.setImageResource(id);
        mBackgroundBlack.setVisibility(View.GONE);
        // リスト表示されていた場合、ブラー効果付与
        if (mFragmentController.isShowListDialog()) {
            mBlurView.setBlurEnabled(true);
            mBlurView.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void changeBackgroundBitmap(Bitmap img) {
        mBackgroundImage.setImageBitmap(img);
        mBackgroundBlack.setVisibility(View.VISIBLE);

        // リスト表示されていた場合、ブラー効果付与
        if (mFragmentController.isShowListDialog()) {
            mBlurView.setBlurEnabled(true);
            mBlurView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void changeBackgroundVideo(Uri uri) {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }

        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(this, uri);
            mMediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            mMediaPlayer.setLooping(false);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                //LightingEffect再生開始要求
                getPresenter().startFlashPattern();
            });
            //再生終了
            mMediaPlayer.setOnCompletionListener(mp -> {
                mp.start();
                //LightingEffect再生開始要求
                getPresenter().startFlashPattern();
            });
            mMediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // リスト表示されていた場合、ブラー効果付与
        if (mFragmentController.isShowListDialog()) {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(getApplicationContext(), uri);
            final Bitmap frame = mmr.getFrameAtTime(mMediaPlayer.getCurrentPosition() * 1000);

            mBlurView.setBlurEnabled(true);
            mBlurView.setVisibility(View.VISIBLE);
            mBackgroundImage.setImageBitmap(frame);
            mBackgroundImage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void startRecognizer() {
        MainActivityPermissionsDispatcher.voiceRecognizeWithCheck(MainActivity.this);
    }

    @NeedsPermission({Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE})
    public void voiceRecognize() {
        getPresenter().startSpeechRecognizer();
    }

    @SuppressWarnings("unused")
    @OnPermissionDenied({Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE})
    public void deniedPermission() {
        finishSpeechRecognizer();
    }

    @Override
    public void startMarin(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex){
            showToast(getString(R.string.err_035));
        }
    }

    @Override
    public void startNavigation(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex){
            showToast(getString(R.string.err_007));
        }
    }

    @Override
    public void showCaution(Bundle args) {
        mFragmentController.showCaution(args);
    }

    @Override
    public void dismissCaution() {
        mFragmentController.dismissCaution();
    }

    @Override
    public boolean isShowCaution() {
        return mFragmentController.isShowCaution();
    }

    @Override
    public void showSpeechRecognizerDialog(Bundle args) {
        mFragmentController.showSpeechRecognizerDialog(args);
    }

    @Override
    public void dismissSpeechRecognizerDialog() {
        Timber.d("dismissSpeechRecognizerDialog");
        mFragmentController.dismissSpeechRecognizerDialog();
        getPresenter().getAppStatus().isShowSpeechRecognizerDialog = false;
    }

    @Override
    public boolean isShowSpeechRecognizerDialog() {
        return mFragmentController.isShowSpeechRecognizerDialog();
    }

    @Override
    public void finishSpeechRecognizer() {
        stopSpeechRecognizer();
    }

    @Override
    public void showSearchContainer(Bundle args) {
        changeBackgroundBlur(true);
        mFragmentController.showSearchContainer(args);
    }

    @Override
    public void dismissSearchContainer() {
        mFragmentController.dismissSearchContainer();
    }

    @Override
    public boolean isShowSearchContainer() {
        return mFragmentController.isShowSearchContainer();
    }

    @Override
    public void showContactContainer(Bundle args) {
        mFragmentController.showContactContainer(args);
    }

    @Override
    public void dismissContactContainer() {
        mFragmentController.dismissContactContainer();
    }

    @Override
    public boolean isShowContactContainer() {
        return mFragmentController.isShowContactContainer();
    }

    @Override
    public void showAccidentDetect(Bundle args) {
        mFragmentController.showAccidentDetect(args);
    }

    @Override
    public void dismissAccidentDetect() {
        mFragmentController.dismissAccidentDetect();
    }

    @Override
    public boolean isShowAccidentDetect() {
        return mFragmentController.isShowAccidentDetect();
    }

    @Override
    public void showParkingSensor(Bundle args) {
        mFragmentController.showParkingSensor(args);
    }

    @Override
    public void dismissParkingSensor() {
        mFragmentController.dismissParkingSensor();
    }

    @Override
    public boolean isShowParkingSensor() {
        return mFragmentController.isShowParkingSensor();
    }

    @Override
    public void showAdasWarning(Bundle args) {
        mFragmentController.showAdasWarning(args);
    }

    @Override
    public void dismissAdasWarning() {
        mFragmentController.dismissAdasWarning();
    }

    @Override
    public boolean isShowAdasWarning() {
        return mFragmentController.isShowAdasWarning();
    }

    @Override
    public void showReadMessageDialog(Bundle args) {
        mFragmentController.showReadingMessage(args);
    }

    @Override
    public void dismissReadMessageDialog() {
        mFragmentController.dismissReadingMessage();
    }

    @Override
    public boolean isShowReadMessageDialog() {
        return mFragmentController.isShowReadingMessage();
    }

    @Override
    public void dismissAlexaDialog() {
        mFragmentController.dismissAlexaDialog();
    }

    @Override
    public boolean isShowAlexaDialog() {
        return mFragmentController.isShowAlexaDialog();
    }

    @Override
    public void showCarDeviceErrorDialog(Bundle args, String tag) {
        mFragmentController.showCarDeviceErrorDialog(args,tag);
        mFragmentController.hideCarDeviceErrorDialog(tag);
    }

    @Override
    public void dismissCarDeviceErrorDialog(String tag) {
        mFragmentController.dismissCarDeviceErrorDialog(tag);
    }

    @Override
    public void dismissCarDeviceErrorDialog() {
        mFragmentController.dismissCarDeviceErrorDialog();
        getPresenter().clearStackPopUp();
    }

    @Override
    public boolean isShowCarDeviceErrorDialog(String tag) {
        return mFragmentController.isShowCarDeviceErrorDialog(tag);
    }

    @Override
    public boolean isShowCarDeviceErrorDialog() {
        return mFragmentController.isShowCarDeviceErrorDialog();
    }

    @Override
    public void showPromptAuthorityPermissionDialog(Bundle args) {
        mFragmentController.showPromptAuthorityPermissionDialog(args);
    }

    @Override
    public void dismissPromptAuthorityPermissionDialog() {
        mFragmentController.dismissPromptAuthorityPermissionDialog();
    }

    @Override
    public boolean isShowPromptAuthorityPermissionDialog() {
        return mFragmentController.isShowPromptAuthorityPermissionDialog();
    }

    @Override
    public MainFragmentController getController() {
        return mFragmentController;
    }

    @Override
    public void showSessionStopped(Bundle args) {
        mFragmentController.showSessionStopped(args);
    }

    @Override
    public boolean isShowSessionStopped() {
        return mFragmentController.isShowSessionStopped();
    }


    @Override
    public void showAppConnectMethodDialog(Bundle args) {
        mFragmentController.showAppConnectMethodDialog(args);
    }

    @Override
    public boolean isShowAppConnectMethodDialog() {
        return mFragmentController.isShowAppConnectMethodDialog();
    }

    @Override
    public void startAdas() {
        MainActivityPermissionsDispatcher.startAdasPermissionWithCheck(MainActivity.this);
    }

    @NeedsPermission({Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,})
    protected void startAdasPermission() {
        if(getPresenter().getAppStatus().adasCameraView) {
            final ViewGroup relativeLayout = findViewById(R.id.layout);
            if(!relativeLayout.getChildAt(2).equals(mAdas)) {
                Timber.d("removeView");
                relativeLayout.removeView(mAdas);
                relativeLayout.addView(mAdas, 2);
                mAdas.setAlpha(1.0f);
            }
            mAdas.setCameraView(true);
        }
        mAdas.setDebugFps(getPresenter().getAppStatus().adasFps);
        mAdas.startAdas();
    }

    @OnPermissionDenied({Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,})
    protected void onPermissionDeniedStartAdas() {
        if (PermissionUtils.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                ||PermissionUtils.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                ||PermissionUtils.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                ||PermissionUtils.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ||PermissionUtils.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //Toast.makeText(this, "リクエストが拒否されました。", Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(this, "パーミッションが拒絶されています。", Toast.LENGTH_SHORT).show();
        }
        getPresenter().checkAdasPermissionError();
     }

    @Override
    public void stopAdas() {
        mAdas.stopAdas();
    }

    @Override
    public void updateAdas() {
        mAdas.updateAdas();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        getPresenter().setBackground();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // no action
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onClose(AbstractDialogFragment fragment) {
        getPresenter().onCloseDialogAction();
        //Containerダイアログが表示中であれば再度背景ぼかしを行う
        mHandler.post(() -> {
            if(mFragmentController.isShowListDialog()){
                getPresenter().changeBackground();
            }
        });

        ScreenId screenId = mFragmentController.getScreenIdInContainer();
        if(screenId!=null) {
            setOrientation(screenId);
        }
    }

    @Override
    public void onClose(SpeechRecognizerDialogFragment fragment) {
        getPresenter().closeSpeechRecognizer(false);
    }

    @Override
    public void onClose(AlexaFragment fragment) {
        ScreenId screenId = mFragmentController.getScreenIdInContainer();
        if(screenId!=null) {
            setOrientation(screenId);
        }
    }

    @Override
    public void onClose(AdasWarningDialogFragment fragment) {
    }

    @Override
    public void onClose(ParkingSensorDialogFragment fragment) {
        //衝突検知再表示判定
        getPresenter().onReShowAccidentDetectAction(Bundle.EMPTY);

        Intent intent;
        intent = getPresenter().getCommandIntent(ACTION_CAR_DEVICE_ERROR);
        if(intent!=null){
            getPresenter().onShowCarDeviceErrorAction(intent.getExtras());
        }

        intent = getPresenter().getCommandIntent(ACTION_SUBSCRIPTION_UPDATE);
        if(intent!=null){
            getPresenter().onShowSubscriptionUpdateAction(intent.getExtras());
        }

        ScreenId screenId = mFragmentController.getScreenIdInContainer();
        if(screenId!=null) {
            setOrientation(screenId);
        }
    }

    @Override
    public void onClose(CautionDialogFragment fragment) {
        //Caution閉幕時
        //ADAS確認ダイアログ表示
        getPresenter().showAdasConfirmDialog();
        //Alexa開始判定
        //getPresenter().startAlexa();

        // パーキングセンサー再表示判定
        getPresenter().onReShowParkingSensorAction(Bundle.EMPTY);

    }

    @Override
    public void onClose(StatusPopupDialogFragment fragment, String tag) {
        if(tag!=null) {
            if (tag.equals(MainPresenter.TAG_DIALOG_SXM_SUBSCRIPTION_UPDATE)) {
                getPresenter().removeStackPopUp(tag);
                mFragmentController.reshowCarDeviceErrorDialog(getPresenter().getStackPopUp());
            } else if(tag.equals(CarDeviceErrorType.AMP_ERROR.toString())||tag.equals(CarDeviceErrorType.CHECK_USB.toString())
            ||tag.equals(CarDeviceErrorType.CHECK_TUNER.toString())||tag.equals(CarDeviceErrorType.CHECK_ANTENNA.toString())){
                getPresenter().removeStackPopUp(tag);
                mFragmentController.reshowCarDeviceErrorDialog(getPresenter().getStackPopUp());
            } else if(tag.equals(MainPresenter.TAG_DIALOG_ERROR)){

            } else if(tag.equals(MainPresenter.TAG_DIALOG_ADAS_TRIAL_END)){

            } else if(tag.equals(MainPresenter.TAG_DIALOG_ADAS_RE_CALIBRATION)){

            }else{

            }
        }

    }

    @Override
    public void onPositiveClick(StatusPopupDialogFragment fragment, String tag) {
        if(tag!=null) {
            if (tag.equals(MainPresenter.TAG_DIALOG_SXM_SUBSCRIPTION_UPDATE)) {
                getPresenter().onReleaseSubscription();
            } else if(tag.equals(MainPresenter.TAG_DIALOG_ERROR)){
                android.os.Process.killProcess(android.os.Process.myPid());
            } else if(tag.equals(MainPresenter.TAG_DIALOG_ADAS_TRIAL_END)){
                getPresenter().goAdasBilling();
            } else if(tag.equals(MainPresenter.TAG_DIALOG_ADAS_RE_CALIBRATION)){
               getPresenter().goCalibrationSetting();
            }else if(tag.equals(MainPresenter.TAG_DIALOG_ADAS_BILLING_STATUS_ERROR)){
                getPresenter().showAdasBillingStatusFailureDialog();
            }else if(tag.equals(MainPresenter.TAG_DIALOG_ADAS_BILLING_STATUS_FAILURE)){
                getPresenter().finishDeviceConnectionSuppress();
            } else if(tag.equals(MainPresenter.TAG_DIALOG_ALEXA_AVAILABLE_CONFIRM)) {
                getPresenter().onAlexaAvailableConfirm();
                getPresenter().finishDeviceConnectionSuppress();
            }
        }
    }

    @Override
    public void onNegativeClick(StatusPopupDialogFragment fragment, String tag) {
        if(tag!=null) {
            if (tag.equals(MainPresenter.TAG_DIALOG_ADAS_BILLING_STATUS_ERROR)) {
                setupBillingHelper();
            } else if(tag.equals(MainPresenter.TAG_DIALOG_ADAS_RE_CALIBRATION)){
                startAdas();
            }
        }
    }

    @Override
    public void onClose(AccidentDetectDialogFragment fragment) {
        getPresenter().setAccidentDetectShow(false);
        //衝突検知ダイアログ閉幕時パーキングセンサー再表示判定
        getPresenter().onReShowParkingSensorAction(Bundle.EMPTY);

        Intent intent;
        intent = getPresenter().getCommandIntent(ACTION_CAR_DEVICE_ERROR);
        if(intent!=null){
            getPresenter().onShowCarDeviceErrorAction(intent.getExtras());
        }

        intent = getPresenter().getCommandIntent(ACTION_SUBSCRIPTION_UPDATE);
        if(intent!=null){
            getPresenter().onShowSubscriptionUpdateAction(intent.getExtras());
        }
    }

    @Override
    public void onClose(PromptAuthorityPermissionDialogFragment fragment) {
        getPresenter().checkAdasPurchase();
    }

    @Override
    public void onActionCall(AccidentDetectDialogFragment fragment) {
		//電話発話開始時
        Fragment screen = mFragmentController.getScreenInContainer();
        if (screen instanceof PlayerContainerFragment) {
            ((PlayerContainerFragment) screen).setActionCall(true);
        }
    }

    @Override
    public void onTimerFinished(AccidentDetectDialogFragment fragment) {
        //衝突検知ダイアログタイマー終了時
        getPresenter().setAccidentDetectShow(false);
    }


    private void stopSpeechRecognizer() {
        getPresenter().stopSpeechRecognizer();
    }

    @Override
    public void changeRsm(float rmsdB) {
        Fragment fragment = mFragmentController.getSpeechRecognizerDialog();
        if (fragment != null && fragment instanceof SpeechRecognizerDialogFragment) {
            ((SpeechRecognizerDialogFragment) fragment).changeSpeechVolume(rmsdB);
        }
    }

    @Override
    public void setSpeechRecognizerText(String text){
        Fragment fragment = mFragmentController.getSpeechRecognizerDialog();
        if (fragment != null && fragment instanceof SpeechRecognizerDialogFragment) {
            ((SpeechRecognizerDialogFragment) fragment).setText(text);
        }
    }

    @Override
    public void setSpeechRecognizerState(SpeechRecognizerDialogFragment.StateType state){
        Fragment fragment = mFragmentController.getSpeechRecognizerDialog();
        if (fragment != null && fragment instanceof SpeechRecognizerDialogFragment) {
            ((SpeechRecognizerDialogFragment) fragment).setState(state);
        }
    }

    @Override
    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    public void setOrientation(ScreenId id){
        if(!id.isDialog()) {
            if (id.isPortrait()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
            } else if (id.isLandscape()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
            } else if (id.isLocked()) {
                //現在の横画面の向きで固定（SCREEN_ORIENTATION_LOCKEDはBackground遷移で崩れる）
                setRequestedOrientation(getScreenOrientation());
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        }
    }
    private int getScreenOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    Log.e(TAG, "Unknown screen orientation. Defaulting to " +
                            "portrait.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch(rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    Log.e(TAG, "Unknown screen orientation. Defaulting to " +
                            "landscape.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }
    // MARK - ADAS課金

    private boolean mIsBillingSetupFinished = false;

    public boolean isBillingSetupFinished() {
        return mIsBillingSetupFinished;
    }

    /**
     * 課金Helper設定
     */
    @Override
    public void setupBillingHelper(){
        mIsBillingSetupFinished = false;
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, BILLING_BASE64_ENCODED_PUBLIC_KEY);

        mHelper.enableDebugLogging(true);
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    Log.d(TAG,"Problem setting up in-app billing");
                    getPresenter().finishDeviceConnectionSuppress();
                    return;
                }
                if (mHelper == null) {
                    getPresenter().finishDeviceConnectionSuppress();
                    return;
                }
                mIsBillingSetupFinished = true;
                Log.d(TAG, "Setup successful. Querying inventory.");
                // 取得したいアイテムのサービスID一覧を生成
                ArrayList<String> skuList = new ArrayList<String>();
                skuList.add(PRODUCT_ID); // プロダクトのサービスID
                try {
                    mHelper.queryInventoryAsync(true,skuList,skuList,mGotInventoryListener);
                }catch (IabHelper.IabAsyncInProgressException e){
                    complain("Error querying inventory. Another async operation in progress.");
                }catch (IllegalStateException e){
                    complain(e.getMessage());
                }
            }
        });
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            if (mHelper == null) {
                getPresenter().finishDeviceConnectionSuppress();
                return;
            }
            if (result.isFailure()) {
                Log.d(TAG, "Error Query inventory: " + result);
                getPresenter().showAdasBillingStatusErrorDialog();
                getPresenter().setPurchase(mIsPremium);
                return;
            }
            Log.d(TAG, "Query inventory was successful.");
            // ここでその課金IDが保有されているかチェックを行います。
            // mIsPremiumにbooleanで結果が返ってくるので、それに応じて変化、分岐させればいいです。
            Purchase premiumPurchase = inventory.getPurchase(PRODUCT_ID);
            if (premiumPurchase == null) {
                getPresenter().showAdasBillingStatusErrorDialog();
                mIsPremium = false;
            }else{
                mIsPremium = true;
                getPresenter().finishDeviceConnectionSuppress();
            }
            getPresenter().setPurchase(mIsPremium);
            Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
        }
    };

    /**
     * 課金Helper設定 価格取得用
     */
    public void setupBillingHelperNotQuery(){
        mIsBillingSetupFinished=false;
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, BILLING_BASE64_ENCODED_PUBLIC_KEY);

        mHelper.enableDebugLogging(true);
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if(result.isSuccess()){
                    mIsBillingSetupFinished = true;
                    Log.d(TAG, "Setup finished.");
                    if (mHelper == null) return;
                    // 取得したいアイテムのサービスID一覧を生成
                    ArrayList<String> skuList = new ArrayList<String>();
                    skuList.add(PRODUCT_ID); // プロダクトのサービスID
                    try {
                        mHelper.queryInventoryAsync(true,skuList,skuList,new IabHelper.QueryInventoryFinishedListener() {
                            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                                Log.d(TAG, "Query inventory finished.");

                                if (mHelper == null) return;
                                String price = getString(R.string.set_377);
                                if (result.isFailure()) {
                                    Log.d(TAG, "Error Query inventory: " + result);
                                    getPresenter().showAdasGetPriceErrorDialog();
                                    getPresenter().setAdasPrice(price);
                                    return;
                                }
                                Log.d(TAG, "Query inventory was successful.");
                                SkuDetails skuDetails = inventory.getSkuDetails(PRODUCT_ID);
                                if (skuDetails != null) {
                                    price = skuDetails.getPrice();
                                }else{
                                    getPresenter().showAdasGetPriceErrorDialog();
                                }
                                getPresenter().setAdasPrice(price);
                            }
                        });
                    }catch (IabHelper.IabAsyncInProgressException e){
                        complain("Error querying inventory. Another async operation in progress.");
                    }
                }
            }
        });
    }
    /**
     * 課金Restore
     */
    public void queryInventoryAsyncRestore(){
        if (mHelper == null) return;
        // 取得したいアイテムのサービスID一覧を生成
        ArrayList<String> skuList = new ArrayList<String>();
        skuList.add(PRODUCT_ID); // プロダクトのサービスID
        try {
            mHelper.queryInventoryAsync(true,skuList,skuList,new IabHelper.QueryInventoryFinishedListener() {
                public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                    Log.d(TAG, "Query inventory finished.");

                    if (mHelper == null) return;

                    if (result.isFailure()) {
                        Log.d(TAG, "Error Query inventory: " + result);
                        getPresenter().showAdasBillingRestoreFailureDialog();
                        getPresenter().setPurchase(mIsPremium);
                        return;
                    }

                    Log.d(TAG, "Query inventory was successful.");
                    // ここでその課金IDが保有されているかチェックを行います。
                    // mIsPremiumにbooleanで結果が返ってくるので、それに応じて変化、分岐させればいいです。
                    Purchase premiumPurchase = inventory.getPurchase(PRODUCT_ID);
                    if (premiumPurchase == null) {
                        mIsPremium = false;
                        getPresenter().showAdasBillingRestoreFailureDialog();
                    }else{
                        mIsPremium = true;
                        getPresenter().showAdasBillingRestoreSuccessDialog();
                        getPresenter().setAdasBillingCheck(true);
                    }
                    getPresenter().setPurchase(mIsPremium);
                    Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
                }
            });
        }catch (IabHelper.IabAsyncInProgressException e){
            complain("Error querying inventory. Another async operation in progress.");
        }
    }

    public void closeBillingHelper(){
        // very important:
        if (mHelper != null) {
            try {
                mHelper.dispose();
            }catch (IabHelper.IabAsyncInProgressException e){

            }
            mHelper = null;
        }
    }


    // 購入後のコールバック関数
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            if (mHelper == null) return;

            if (result.isFailure()) {
                Log.d(TAG, "Error purchasing: " + result);
                if(!result.isUserCancel()){
                    getPresenter().showAdasPurchaseErrorDialog();
                }
                return;
            }

            Log.d(TAG, "Purchase successful.");
            if (purchase.getSku().equals(PRODUCT_ID)) {
                // 購入後の処理はここ。
                // 購入された課金IDを保存などの処理。
                getPresenter().setPurchase(true);
                getPresenter().setAdasBillingCheck(true);
                Log.d(TAG, "商品：" + PRODUCT_ID + "を購入しました。");
                Log.d(TAG,"orderIdは：" + purchase.getOrderId());
                Log.d(TAG,"INAPP_PURCHASE_DATAのJSONは：" + purchase.getOriginalJson());
            }
        }
    };

    void complain(String message) {
        Log.e(TAG, "**** TrivialDrive Error: " + message);
        showToast("Error: " + message);
    }

    public void purchaseLauncher(){
        try {
            mHelper.launchPurchaseFlow(this, PRODUCT_ID, RC_REQUEST, mPurchaseFinishedListener, null);
        }catch (IabHelper.IabAsyncInProgressException e){
            complain("Error launching purchase flow. Another async operation in progress.");
        }
    }

    // MARK - Alexa

    @Override
    public void setAlexaCapabilities(){
        if(mAmazonAlexaManager!=null){
            mAmazonAlexaManager.sendCapabilities();
        }
    }

    @Override
    public void startAlexaConnection(){
        Timber.d("startAlexaConnection");
        mAmazonAlexaManager = AmazonAlexaManager.getInstance();
        if(mAmazonAlexaManager!=null){
            mAmazonAlexaManager.init();
            GeolocationManager geolocationManager = GeolocationManager.getInstance();
            geolocationManager.init(this);
            mAmazonAlexaManager.onActivityResume();
            mAmazonAlexaManager.onAvsConnect();
            mAmazonAlexaManager.openDownChannel();
        }
    }

    @Override
    public void finishAlexaConnection(){
        Timber.d("finishAlexaConnection");
        mAmazonAlexaManager=AmazonAlexaManager.getInstance();
        // Alexa 切断処理
        if (mAmazonAlexaManager != null) {
            Timber.d("finishAlexaConnectionInner");
            //mAmazonAlexaManager.finishAmazonAlexa(this);
            mAmazonAlexaManager.onActivityPause();
        }
    }

    /**
     * Alexa機能利用可能ダイアログ表示(Tips画面用 UnconnectedContainerFragment)
     */
    public void showAlexaAvailableConfirmDialog() {
        getPresenter().showAlexaAvailableConfirmDialog();
    }

    /**
     * アレクサのイベントのコールバックを受けるメソッド.
     */
    private class AlexaCallback implements AmazonAlexaManager.IAlexaCallback {
        @Override
        public void onLoginSuccess() {
            Timber.d("onLoginSuccess");
            getPresenter().onLogIn();
        }

        @Override
        public void onLogout() {
            Timber.d("onLoginFailed");
            getPresenter().onLogOut();
        }

        @Override
        public void onConnect() {

        }

        @Override
        public void onDisConnect() {

        }

        @Override
        public void onNetworkConnect() {

        }

        @Override
        public void onNetworkDisconnect() {

        }

        @Override
        public void onRecordingStart() {

        }

        @Override
        public void onRecordingMonitor(double db, int hertz) {

        }

        @Override
        public void onRecordingStop(boolean isCancel) {

        }

        @Override
        public void onSpeakingPrepare() {

        }

        @Override
        public void onSpeakingPrepared() {

        }

        @Override
        public void onSpeakingStart() {

        }

        @Override
        public void onSpeakingResume() {

        }

        @Override
        public void onSpeakingPause() {

        }

        @Override
        public void onSpeakingStop() {

        }

        @Override
        public void onSpeakingComplete() {

        }

        @Override
        public void onReceiveRenderPlayerInfo(RenderPlayerInfoItem playerInfoItem) {

        }

        @Override
        public void onReceiveRenderTemplate(RenderTemplateItem templateItem) {

        }

        @Override
        public void onAudioPrepare() {

        }

        @Override
        public void onAudioPrepared() {

        }

        @Override
        public void onAudioStart() {
            Timber.d("onAudioStart");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //非連携中に受け取ったらAlexaを終了する
                    if(!getPresenter().isSessionConnected()){
                        finishAlexaConnection();
                    }else if(getPresenter().isSessionConnected()&&mFragmentController.getScreenIdInContainer()!=ScreenId.ANDROID_MUSIC){
                        navigate(ScreenId.PLAYER_CONTAINER,Bundle.EMPTY);
                    }
                }
            });
        }

        @Override
        public void onAudioResume() {
            Timber.d("onAudioResume");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    //非連携中に受け取ったらAlexaを終了する
                    if(!getPresenter().isSessionConnected()){
                        finishAlexaConnection();
                    }else if(mFragmentController.getScreenIdInContainer()!=ScreenId.ANDROID_MUSIC){
                        navigate(ScreenId.PLAYER_CONTAINER,Bundle.EMPTY);
                    }
                }
            });
        }

        @Override
        public void onAudioPause() {

        }

        @Override
        public void onAudioStop() {

        }

        @Override
        public void onAudioError() {

        }

        @Override
        public void onAudioComplete() {

        }

        @Override
        public void onAudioUpdateProgress(int current, int duration) {

        }

        @Override
        public void onSystemError() {

        }

        @Override
        public void onAlertStarted() {

        }

        @Override
        public void onShortAlertStarted() {

        }

        @Override
        public void onAlertStopped() {

        }

        @Override
        public void onSetAlert() {

        }

        @Override
        public void onStopAlertAll() {

        }

        @Override
        public void onPersistVisualIndicator() {

        }

        @Override
        public void onClearVisualIndicator() {

        }

        @Override
        public void onAudioIndicatorStarted() {

        }

        @Override
        public void onAudioIndicatorStopped() {

        }

        @Override
        public void onSetVolume(float volume) {

        }

        @Override
        public void onAdjustVolume(float volume) {

        }

        @Override
        public void onSetMute(boolean isMute) {

        }

        @Override
        public void onNoResponse() {

        }

        @Override
        public void onChannelActiveChange(AlexaQueueManager.AlexaChannel channel, boolean isActive) {

        }

        @Override
        public void onMicrophonePermission(int state) {

        }

        @Override
        public void onNoDirectiveAtSendEventResponse() {

        }

        @Override
        public void onLoginFailed() {
            Timber.d("onLoginFailed");
            getPresenter().onLogOut();
        }




        @Override
        public void onCapabilitiesSendSuccess() {
            Timber.d("onCapabilitiesSendSuccess");
            getPresenter().onCapabilitiesSendSuccess();
        }

        @Override
        public void onSetNaviDestination(Double latitude, Double longitude, String name) {

        }

        @Override
        public void onRecordingNotAvailable() {

        }

        @Override
        public void onWLAudioFocusLoss() {

        }

        @Override
        public void onDecodeStart() {

        }

        @Override
        public void onDecodeFinish() {

        }
    }

    /// MARK - ユーザー画像背景設定

    private void setBackgroundMyPhoto(Uri uri){
        int orientation=0;
        InputStream inputStream=null;
        FileOutputStream out =null;
        try {
            inputStream = getContentResolver().openInputStream(uri);
            if(inputStream==null)return;
            ExifInterface exifInterface = new ExifInterface(inputStream);
            orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            inputStream.close();
            inputStream = getContentResolver().openInputStream(uri);
            if(inputStream==null)return;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream,null, options);
            Timber.v( "Original Image Size: " + options.outWidth + " x " + options.outHeight);
            inputStream.close();

            Bitmap bitmap;
            int imageSizeMax = 1334;
            //imageSizeMax = 750;
            inputStream = getContentResolver().openInputStream(uri);
            if(inputStream==null)return;
            out = openFileOutput("myPhotoPreview.jpg",MODE_PRIVATE);

            /*横の縮尺を求める */
            float imageScaleWidth = (float)options.outWidth / imageSizeMax;
            /* 縦の縮尺を求める */
            float imageScaleHeight = (float)options.outHeight / imageSizeMax;

            float scale = Math.max(imageScaleWidth, imageScaleHeight);

            // もしも、縮小できるサイズならば、縮小して読み込む
            if (imageScaleWidth > 2 || imageScaleHeight > 2) {
                BitmapFactory.Options imageOptions2 = new BitmapFactory.Options();

                // 縦横、大きい方に縮小するスケールを合わせる
                int imageScale = (int)Math.floor((imageScaleWidth > imageScaleHeight ? imageScaleWidth : imageScaleHeight));

                // inSampleSizeには2のべき上が入るべきなので、imageScaleに最も近く、かつそれ以下の2のべき上の数を探す
                for (int i = 2; i <= imageScale; i *= 2) {
                    imageOptions2.inSampleSize = i;
                }

                bitmap = BitmapFactory.decodeStream(inputStream, null, imageOptions2);
                Timber.v( "Sample Size: 1/" + imageOptions2.inSampleSize);
            } else {
                bitmap = BitmapFactory.decodeStream(inputStream);
            }

            inputStream.close();
            Matrix transformMatrix = getRotatedMatrix(orientation);
            if(bitmap!=null) {
                bitmap = transformBitmap(bitmap, transformMatrix);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                getPresenter().previewBackground();
            }
            Timber.d("Exif:orientation" + orientation);

        } catch (IOException e) {
            Timber.e(e.getMessage());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ignored) {
            }
        }

    }

    /**
     * 画像の回転後のマトリクスを取得
     */
    private Matrix getRotatedMatrix(int exifOrientation){
        Matrix matrix = new Matrix();
        // 画像を回転させる処理をマトリクスに追加
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_UNDEFINED:
                break;
            case ExifInterface.ORIENTATION_NORMAL:
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                // 水平方向にリフレクト
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                // 180度回転
                matrix.postRotate(180f);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                // 垂直方向にリフレクト
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                // 反時計回り90度回転
                matrix.postRotate(90f);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                // 時計回り90度回転し、垂直方向にリフレクト
                matrix.postRotate(-90f);
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                // 反時計回り90度回転し、垂直方向にリフレクト
                matrix.postRotate(90f);
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                // 反時計回りに270度回転（時計回りに90度回転）
                matrix.postRotate(-90f);
                break;
        }
        return matrix;
    }

    private Bitmap transformBitmap(@NonNull Bitmap bitmap, @NonNull Matrix transformMatrix) {
        try {
            Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), transformMatrix, true);
            if (!bitmap.sameAs(converted)) {
                bitmap = converted;
            }
        } catch (OutOfMemoryError error) {
            Log.e("", "transformBitmap: ", error);
        }
        return bitmap;
    }

    public void getGalleryImage() {

        // ギャラリー呼び出し
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    public void manageDrawOverlayPermission(){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE_OVERLAY);
    }
}
