package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Range;
import android.util.Size;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.util.CpuInfo;
import jp.pioneer.carsync.presentation.util.CpuUtils;
import jp.pioneer.carsync.presentation.view.AdasUsageCautionView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;
import timber.log.Timber;

/**
 * AdasUsageCautionPresenter
 */
@PresenterLifeCycle
public class AdasUsageCautionPresenter extends Presenter<AdasUsageCautionView> {
    public static final String TAG_ADAS_SPEC_CHECK = "tag_adas_spec_check";
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private static final int CAMERA_PREVIEW_WIDTH = 1280;
    private static final int CAMERA_PREVIEW_HEIGHT = 720;
    private static final int CAMERA_FPS_RANGE_MAX = 30;//fps
    private static final int CPU_CORE_NUM = 4;//Core
    private static final int CPU_FREQUENCY_MAX = 1500000;//kHz
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    private boolean mIsScrollBottom;
    private Bundle mArguments;
    private int mPage=0;
    /**
     * コンストラクタ.
     */
    @Inject
    public AdasUsageCautionPresenter() {
    }

    @Override
    void onTakeView() {
        setPage();
    }

    @Override
    void onResume() {
        SettingsParams params = SettingsParams.from(mArguments);
        Optional.ofNullable(getView()).ifPresent(view -> {
            boolean isVisible = true;
            if (params.mScreenId == ScreenId.SETTINGS_ADAS) {
                //isVisible = false;
            }
            //常に確認ボタン表示
            view.setVisibleAgreeBtn(isVisible);
        });
    }

    @Override
    public void saveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("agree", mIsScrollBottom);
        outState.putInt("page", mPage);
    }

    @Override
    public void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        setEnabledAgreeBtn(savedInstanceState.getBoolean("agree"));
        mPage = savedInstanceState.getInt("page");
    }

    public void setArgument(Bundle args) {
        mArguments = args;
    }

    public void onScrollBottomAction() {
        mIsScrollBottom = true;
        setEnabledAgreeBtn(true);
    }

    private void setPage(){
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setPage(mPage);
        });
    }
    /**
     * 同意ボタン押下時の処理
     */
    public void onAcceptAction() {
        if (mPage == 0) {
            mIsScrollBottom = false;
            mPage =1;
            setEnabledAgreeBtn(false);
            setPage();
        }else {
            SettingsParams params = SettingsParams.from(mArguments);
            if(params.mScreenId == ScreenId.SETTINGS_ADAS) {
                mEventBus.post(new NavigateEvent(ScreenId.ADAS_MANUAL, createSettingsParams(ScreenId.ADAS_USAGE_CAUTION, mContext.getString(R.string.set_341))));
            }else if(params.mScreenId == ScreenId.ADAS_TUTORIAL){
                if (checkDeviceSpec()) {
                    goAdasBilling();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString(StatusPopupDialogFragment.TAG, TAG_ADAS_SPEC_CHECK);
                    bundle.putString(StatusPopupDialogFragment.MESSAGE, mContext.getString(R.string.set_333));
                    bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
                    mEventBus.post(new NavigateEvent(ScreenId.STATUS_DIALOG, bundle));
                }
            }
        }
    }

    public void goAdasBilling() {
        mEventBus.post(new NavigateEvent(ScreenId.ADAS_BILLING, createSettingsParams(ScreenId.ADAS_TUTORIAL, mContext.getString(R.string.set_289))));
    }

    private boolean checkDeviceSpec() {
        boolean isFpsOk = false;
        boolean isCameraSizeOk = false;
        boolean isCpuSpecOk = false;
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
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
            CameraCharacteristics characteristics
                    = manager.getCameraCharacteristics(backCameraId);
            //カメラのフレームレート：30fps以上の判定
            Range<Integer>[] fpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            if (fpsRanges != null && fpsRanges.length > 0) {
                int max = fpsRanges[0].getUpper();
                int min = fpsRanges[0].getLower();
                for (int i = 1; i < fpsRanges.length; i++) {
                    max = fpsRanges[i].getUpper();
                    min = fpsRanges[i].getLower();
                    if (max >= CAMERA_FPS_RANGE_MAX) {
                        isFpsOk = true;
                        break;
                    }
                }
                Timber.d("fpsRange:min=%d, max=%d", min, max);
            }
            //カメラ画素数：1280*720以上の判定
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size aspectSize = new Size(CAMERA_PREVIEW_WIDTH, CAMERA_PREVIEW_HEIGHT);
            Size previewSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.YUV_420_888),
                    CAMERA_PREVIEW_WIDTH, CAMERA_PREVIEW_HEIGHT, MAX_PREVIEW_WIDTH,
                    MAX_PREVIEW_HEIGHT, aspectSize);
            if (previewSize.getWidth() >= CAMERA_PREVIEW_WIDTH && previewSize.getHeight() >= CAMERA_PREVIEW_HEIGHT) {
                isCameraSizeOk = true;
            }
            Timber.d("previewSize:width=%d, height=%d", previewSize.getWidth(), previewSize.getHeight());
        } catch (CameraAccessException e) {
            //例外処理を記述
        }
        //プロセッサ能力：1.5Ghz *4 core以上の判定
        isCpuSpecOk = checkCpuSpec();
        return isFpsOk && isCameraSizeOk && isCpuSpecOk;
    }

    private boolean checkCpuSpec() {
        boolean result = false;
        int coreNum = CpuUtils.getCPUCoreNumber();
        boolean freqResult = false;

        ArrayList<CpuInfo> cpuInfos = new ArrayList<>();
        for (int i = 0; i < coreNum; i++) {
            cpuInfos.add(new CpuInfo(CpuUtils.readMaxCPUFrequency(i),
                    CpuUtils.readMinCPUFrequency(i),
                    CpuUtils.readCurrentCPUFrequency(i)));
            Timber.d("cpu" + i + " MaxCPUFrequency:" + cpuInfos.get(i).maxFreq);
        }
        try {
            for(CpuInfo cpu : cpuInfos){
                int freqInt = Integer.parseInt(cpu.maxFreq.trim());
                if (freqInt >= CPU_FREQUENCY_MAX) {
                    freqResult = true;
                    break;
                }
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        result = coreNum >= CPU_CORE_NUM && freqResult;
        return result;
    }

    private String getProcessResult(String[] args) {
        ProcessBuilder cmd;
        String result = "";
        try {
            cmd = new ProcessBuilder(args);

            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[32768];
            int read = 0;
            while ((read = in.read(re, 0, 32768)) != -1) {
                String string = new String(re, 0, read);
                Timber.d(string);
                result += string;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private void setEnabledAgreeBtn(boolean isEnabled) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setEnabledAgreeBtn(isEnabled));
    }

    private Bundle createSettingsParams(ScreenId screenId, String pass) {
        SettingsParams params = new SettingsParams();
        params.mScreenId = screenId;
        params.pass = pass;
        return params.toBundle();
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
            Timber.e("Couldn't find any suitable preview size");
            return choices[0];
        }
    }
}
