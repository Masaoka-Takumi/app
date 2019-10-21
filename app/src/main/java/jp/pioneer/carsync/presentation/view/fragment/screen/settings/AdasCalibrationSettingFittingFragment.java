package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Point;
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
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import jp.pioneer.carsync.presentation.model.UiColor;
import jp.pioneer.carsync.presentation.presenter.AdasCalibrationSettingFittingPresenter;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;
import jp.pioneer.carsync.presentation.util.TextViewUtil;
import jp.pioneer.carsync.presentation.view.AdasCalibrationSettingFittingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;
import timber.log.Timber;

/**
 * Created by NSW00_007906 on 2018/07/17.
 */
@RuntimePermissions
public class AdasCalibrationSettingFittingFragment extends AbstractScreenFragment<AdasCalibrationSettingFittingPresenter, AdasCalibrationSettingFittingView>
        implements AdasCalibrationSettingFittingView {
    private static final int ADAS_UP_DOWN_BUTTON_INTERVAL = 50;
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
    @Inject AdasCalibrationSettingFittingPresenter mPresenter;
    @BindView(R.id.title) TextView mTitle;
    @BindView(R.id.line) RelativeLayout mLine;
    @BindView(R.id.line_back) ImageView mLineBack;
    @BindView(R.id.line_point) View mLinePoint;
    @BindView(R.id.back_btn) ImageButton mBackBtn;
    @BindView(R.id.next_btn) ImageButton mNextBtn;
    @BindView(R.id.narrow_caution) ImageView mNarrowCaution;
    @BindView(R.id.up_btn) ImageView mUpBtn;
    @BindView(R.id.down_btn) ImageView mDownBtn;
    @BindView(R.id.surfaceView) SurfaceView mSurfaceView;
    private View mView;
    private Unbinder mUnbinder;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private static final String TAG = "AdasSettingFitting";
    private CameraDevice backCameraDevice = null;
    private CameraCaptureSession backCameraSession = null;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private int mWidth; //画面の横幅
    private int mHeight; //画面の高さ
    private int mHalfHeight; //画面の半分の高さ
    private int mQuarterHeight; //画面の1/4の高さ
    private int mLineHeight; //操作ポイントの高さ
    private int mOrientation;
    private int mCalibrationHeight = -1;
    private boolean mBackCameraSessionStarted = false;

    /**
     * コンストラクタ
     */
    public AdasCalibrationSettingFittingFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return AdasCalibrationSettingFittingFragment
     */
    public static AdasCalibrationSettingFittingFragment newInstance(Bundle args) {
        AdasCalibrationSettingFittingFragment fragment = new AdasCalibrationSettingFittingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adas_calibration, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        getPresenter().setArgument(getArguments());
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        mWidth = point.x;
        mHeight = point.y;
        mHalfHeight = mHeight / 2;
        mQuarterHeight = (int) (mHalfHeight * 0.3);
        mView = view;
        mOrientation = getOrientation();
        mNarrowCaution.setVisibility(View.GONE);
        mUpBtn.setVisibility(View.VISIBLE);
        mDownBtn.setVisibility(View.VISIBLE);
        setTitle(getString(R.string.set_296));
        setLineVisible(false);
        mGlobalLayoutListener = () -> {
            mWidth = view.getWidth();
            mHeight = view.getHeight();
            mHalfHeight = mHeight / 2;
            mQuarterHeight = (int) (mHalfHeight * 0.3);

            mLine.setY(mHeight - mLine.getHeight());
            mLineHeight = mLine.getHeight();
            ConstraintLayout.LayoutParams params =
                    (ConstraintLayout.LayoutParams) mLinePoint.getLayoutParams();
            ConstraintLayout.LayoutParams newParams = new ConstraintLayout.LayoutParams(mLineHeight, mLineHeight);
            newParams.leftToLeft = params.leftToLeft;
            newParams.rightToRight = params.rightToRight;
            newParams.topToTop = params.topToTop;
            mLinePoint.setLayoutParams(newParams);
            mLinePoint.requestLayout();
            mLinePoint.post(() -> {
                if (mLinePoint == null) return;
                int top;
                if (mCalibrationHeight >= 0 && mCalibrationHeight <= mHalfHeight) {
                    top = mHeight - mCalibrationHeight - mLineHeight / 2;
                } else {
                    top = mHeight - mQuarterHeight - mLineHeight / 2;
                }
                mLine.setY(top);
                mLinePoint.setY(top);
                setAdjustBtnEnabled(top - mLineHeight / 2);
                if (mCalibrationHeight > mQuarterHeight) {
                    setTitle(getString(R.string.set_295));
                    mNarrowCaution.setVisibility(View.VISIBLE);
                    if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                        mLineBack.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p1238_adasline_p, UiColor.RED.getResource()));
                    } else {
                        mLineBack.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p1236_adasline_p, UiColor.RED.getResource()));
                    }
                } else {
                    setTitle(getString(R.string.set_296));
                    mNarrowCaution.setVisibility(View.GONE);
                    if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                        mLineBack.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p1238_adasline_p, UiColor.GREEN.getResource()));
                    } else {
                        mLineBack.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p1236_adasline_p, UiColor.GREEN.getResource()));
                    }
                }
                setLineVisible(true);
            });
            AdasCalibrationSettingFittingFragment.DragViewListener listener = new AdasCalibrationSettingFittingFragment.DragViewListener(mLinePoint);
            mLinePoint.setOnTouchListener(listener);
            // removeOnGlobalLayoutListener()の削除
            view.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        };
        view.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);

        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        mView.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        super.onDestroyView();
    }

    //ホームボタンを押した際などにカメラを開放する（開放しないと裏で動き続ける・・・）
    @Override
    public void onPause() {
        // カメラセッションの終了
        if (backCameraSession != null && mBackCameraSessionStarted) {
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        AdasCalibrationSettingFittingFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void startCamera(){
        AdasCalibrationSettingFittingFragmentPermissionsDispatcher.setCameraWithCheck(this);
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    public void setCamera() {
        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        if(mOrientation==Configuration.ORIENTATION_PORTRAIT)return;
        try {
            //カメラIDを取得（背面カメラを選択）
            String backCameraId = "";
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
                    new AdasCalibrationSettingFragment.CompareSizesByArea());

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
            manager.openCamera(backCameraId, new AdasCalibrationSettingFittingFragment.OpenCameraCallback(), null);
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
            if (!isResumed()) return;
            backCameraDevice = cameraDevice;
            // プレビュー用のSurfaceViewをリストに登録
            ArrayList<Surface> surfaceList = new ArrayList<Surface>();
            surfaceList.add(mSurfaceView.getHolder().getSurface());

            try {
                // プレビューリクエストの設定（SurfaceViewをターゲットに）
                mPreviewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mPreviewRequestBuilder.addTarget(mSurfaceView.getHolder().getSurface());

                // キャプチャーセッションの開始(セッション開始後に第2引数のコールバッククラスが呼ばれる)
                cameraDevice.createCaptureSession(surfaceList, new AdasCalibrationSettingFittingFragment.CameraCaptureSessionCallback(), null);

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
            if (!isResumed()) return;
            mBackCameraSessionStarted = true;
            backCameraSession = session;
            try {
                // オートフォーカスの設定
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                        CameraMetadata.CONTROL_AF_TRIGGER_START);

                // プレビューの開始(撮影時に第2引数のコールバッククラスが呼ばれる)
                session.setRepeatingRequest(mPreviewRequestBuilder.build(), new AdasCalibrationSettingFittingFragment.CaptureCallback(), null);

            } catch (CameraAccessException e) {
                Timber.e( e.toString());
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
        return ScreenId.CALIBRATION_SETTING_FITTING;
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
    protected AdasCalibrationSettingFittingPresenter getPresenter() {
        return mPresenter;
    }

    @OnClick(R.id.back_btn)
    public void onClickBackButton() {
        getPresenter().onBackAction();
    }

    @OnClick(R.id.next_btn)
    public void onClickNextButton() {
        getPresenter().onNextAction();
    }

    @OnClick(R.id.up_btn)
    public void onClickUpButton() {
        int settingValue;
        float top =  mLinePoint.getY();
        float positionY = top + (float)mLineHeight / 2;
        settingValue =  (int)Math.round((ADAS_UP_DOWN_BUTTON_INTERVAL) * ((positionY-mHalfHeight) / mHalfHeight));
        settingValue--;
        float newPos = mHalfHeight + settingValue * ((float)mHalfHeight/ADAS_UP_DOWN_BUTTON_INTERVAL);
        top = newPos - (float)mLineHeight / 2;
        if (newPos >= mHalfHeight && newPos <= mHeight) {
            mLine.setY(top);
            mLinePoint.setY(top);
            mCalibrationHeight = (int) (mHeight - newPos);
        }else if(newPos< mHalfHeight){
            mLine.setY(mHalfHeight - (float)mLineHeight / 2);
            mLinePoint.setY(mHalfHeight - (float)mLineHeight / 2);
            mCalibrationHeight = mHalfHeight;
        }
        judgeNarrow(newPos);
        setAdjustBtnEnabled(newPos);
        getPresenter().setCalibrationHeight(mCalibrationHeight);
    }

    @OnClick(R.id.down_btn)
    public void onClickDownButton() {
        int settingValue;
        float top =  mLinePoint.getY();
        float positionY = top + (float)mLineHeight / 2;
        settingValue =  (int)(Math.round((ADAS_UP_DOWN_BUTTON_INTERVAL) * ((positionY-mHalfHeight) / mHalfHeight)));
        settingValue++;
        float newPos = mHalfHeight + settingValue * ((float)mHalfHeight/ADAS_UP_DOWN_BUTTON_INTERVAL);
        top = newPos - (float)mLineHeight / 2;
        if (newPos >= mHalfHeight && newPos <= mHeight) {
            mLine.setY(top);
            mLinePoint.setY(top);
            mCalibrationHeight = (int) (mHeight - newPos);
        }else if(newPos> mHeight){
            mLine.setY(mHeight - (float)mLineHeight / 2);
            mLinePoint.setY(mHeight - (float)mLineHeight / 2);
            mCalibrationHeight = 0;
        }
        judgeNarrow(newPos);
        setAdjustBtnEnabled(newPos);
        getPresenter().setCalibrationHeight(mCalibrationHeight);
    }

    private void setAdjustBtnEnabled(float positionY){
        if(positionY <= mHalfHeight){
            mUpBtn.setEnabled(false);
            mDownBtn.setEnabled(true);
            mUpBtn.setAlpha(0.5f);
            mDownBtn.setAlpha(1.0f);
        }else if(positionY >= mHeight){
            mUpBtn.setEnabled(true);
            mDownBtn.setEnabled(false);
            mUpBtn.setAlpha(1.0f);
            mDownBtn.setAlpha(0.5f);
        }else{
            mUpBtn.setEnabled(true);
            mDownBtn.setEnabled(true);
            mUpBtn.setAlpha(1.0f);
            mDownBtn.setAlpha(1.0f);
        }
    }

    private void judgeNarrow(float position){
        if (position < mHeight - mQuarterHeight) {
            setTitle(getString(R.string.set_295));
            mNarrowCaution.setVisibility(View.VISIBLE);
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                mLineBack.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p1238_adasline_p, UiColor.RED.getResource()));
            } else {
                mLineBack.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p1236_adasline_p, UiColor.RED.getResource()));
            }
        } else {
            setTitle(getString(R.string.set_296));
            mNarrowCaution.setVisibility(View.GONE);
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                mLineBack.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p1238_adasline_p, UiColor.GREEN.getResource()));
            } else {
                mLineBack.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p1236_adasline_p, UiColor.GREEN.getResource()));
            }
        }
    }

    @Override
    public int getOrientation() {
        Configuration config = getResources().getConfiguration();
        return config.orientation;
    }

    @Override
    public int getHalfHeight() {
        return mHalfHeight;
    }

    @Override
    public void setCalibrationHeight(int height) {
        mCalibrationHeight = height;
        int top;
        if (height >= 0 && height <= mHalfHeight) {
            top = mHeight - mCalibrationHeight - mLineHeight / 2;
        } else {
            top = mHeight - mLineHeight / 2;
        }
        mLine.setY(top);
        mLinePoint.setY(top);
        setAdjustBtnEnabled(top + mLineHeight / 2);
        if (mCalibrationHeight > mQuarterHeight) {
            setTitle(getString(R.string.set_295));
            mNarrowCaution.setVisibility(View.VISIBLE);
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                mLineBack.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p1238_adasline_p, UiColor.RED.getResource()));
            } else {
                mLineBack.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p1236_adasline_p, UiColor.RED.getResource()));
            }
        } else {
            setTitle(getString(R.string.set_296));
            mNarrowCaution.setVisibility(View.GONE);
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                mLineBack.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p1238_adasline_p, UiColor.GREEN.getResource()));
            } else {
                mLineBack.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p1236_adasline_p, UiColor.GREEN.getResource()));
            }
        }
    }

    private void setTitle(String text) {
        TextViewUtil.setTextIfChanged(mTitle, text);
    }

    private void setLineVisible(boolean isVisible) {
        if (isVisible) {
            mLine.setVisibility(View.VISIBLE);
            mLinePoint.setVisibility(View.VISIBLE);
        } else {
            mLine.setVisibility(View.INVISIBLE);
            mLinePoint.setVisibility(View.INVISIBLE);
        }
    }

    private class DragViewListener implements View.OnTouchListener {
        // ドラッグ対象のView
        private View dragView;
        // ドラッグ中に移動量を取得するための変数
        private int mOldX;
        private int mOldY;

        public DragViewListener(View dragView) {
            this.dragView = dragView;
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            // タッチしている位置取得
            //int x = (int) event.getRawX();
            int y = (int) event.getRawY();
            // 今回イベントでのView移動先の位置
            //int left = dragView.getLeft();// + (x - mOldX);
            int top;
            if (mOldY == 0) {
                top = (int) dragView.getY();
            } else {
                top = (int) dragView.getY() + (y - mOldY);
            }
            float positionY = top + mLineHeight / 2;
            mCalibrationHeight = (int) (mHeight - positionY);
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if (positionY >= mHalfHeight && positionY <= mHeight) {
                        mLine.setY(top);
                        mLinePoint.setY(top);
                    }
                    judgeNarrow(positionY);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    setAdjustBtnEnabled(positionY);
                    getPresenter().setCalibrationHeight(mCalibrationHeight);
                    break;
                default:
                    break;
            }

            // 今回のタッチ位置を保持
            //mOldX = x;
            mOldY = y;
            // イベント処理完了
            return true;
        }
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
            return Collections.min(bigEnough, new AdasCalibrationSettingFragment.CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new AdasCalibrationSettingFragment.CompareSizesByArea());
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