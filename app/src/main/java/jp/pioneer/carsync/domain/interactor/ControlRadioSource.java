package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.component.RadioSourceController;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PCHManualSetting;
import jp.pioneer.carsync.domain.model.PtySearchSetting;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TunerFunctionSetting;
import jp.pioneer.carsync.domain.model.TunerStatus;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ラジオ操作.
 */
public class ControlRadioSource {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    private RadioSourceController mSourceController;

    /**
     * コンストラクタ
     *
     * @param carDevice CarDevice
     */
    @Inject
    public ControlRadioSource(CarDevice carDevice) {
        mSourceController = (RadioSourceController) carDevice.getSourceController(MediaSourceType.RADIO);
    }

    /**
     * バンド切り替え.
     */
    public void toggleBand() {
        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.toggleBand();
            } else {
                Timber.w("toggleBand() not active.");
            }
        });
    }

    /**
     * プリセット呼び出し.
     *
     * @param presetNo プリセットNo.
     * @throws IllegalArgumentException {@code presetNo} の値が不正.
     */
    public void callPreset(@IntRange(from = 1, to = 6) int presetNo) {
        checkArgument(presetNo >= 1);
        checkArgument(presetNo <= 6);

        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.callPreset(presetNo);
            } else {
                Timber.w("callPreset() not active.");
            }
        });
    }

    /**
     * チャンネルアップ.
     */
    public void channelUp() {
        mHandler.post(() -> {
            RadioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().radioInfo;
            TunerFunctionSetting setting = mStatusHolder.getTunerFunctionSetting();

            if(info.tunerStatus == TunerStatus.BSM){
                Timber.w("channelUp() bsm running.");
                return;
            }

            if (mSourceController.isActive()) {
                if(setting.pchManualSetting == PCHManualSetting.MANUAL||isSphCarDevice()) {
                    mSourceController.manualUp();
                } else {
                    mSourceController.presetUp();
                }
            } else {
                Timber.w("channelUp() not active.");
            }
        });
    }

    /**
     * チャンネルダウン.
     */
    public void channelDown() {
        mHandler.post(() -> {
            RadioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().radioInfo;
            TunerFunctionSetting setting = mStatusHolder.getTunerFunctionSetting();

            if(info.tunerStatus == TunerStatus.BSM){
                Timber.w("channelDown() bsm running.");
                return;
            }

            if (mSourceController.isActive()) {
                if(setting.pchManualSetting == PCHManualSetting.MANUAL||isSphCarDevice()) {
                    mSourceController.manualDown();
                } else {
                    mSourceController.presetDown();
                }
            } else {
                Timber.w("channelDown() not active.");
            }
        });
    }
    private boolean isSphCarDevice(){
        return mStatusHolder.getProtocolSpec().isSphCarDevice();
    }

    /**
     * Seek UP.
     */
    public void seekUp() {
        mHandler.post(() -> {
            RadioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().radioInfo;
            TunerFunctionSetting setting = mStatusHolder.getTunerFunctionSetting();

            if(info.tunerStatus == TunerStatus.BSM){
                Timber.w("seekUp() bsm running.");
                return;
            }

            if (mSourceController.isActive()) {
                mSourceController.seekUp();
            } else {
                Timber.w("seekUp() not active.");
            }
        });
    }

    /**
     * BSM.
     * <p>
     * BSM設定が無効な場合、何もしない。
     *
     * @param isStart true:開始 false:終了
     */
    public void setBsm(boolean isStart) {
        mHandler.post(() -> {
            if (!mStatusHolder.getTunerFunctionSettingStatus().bsmSettingEnabled) {
                Timber.w("setBsm() Disabled.");
                return;
            }

            if (mSourceController.isActive()) {
                mSourceController.startBsm(isStart);
            } else {
                Timber.w("setBsm() not active.");
            }
        });
    }

    /**
     * PTY Search開始.
     * <p>
     * PTY Search設定が無効な場合、何もしない。
     *
     * @param setting 設定内容
     * @throws NullPointerException {@code setting} がnull
     */
    public void startPtySearch(@NonNull PtySearchSetting setting){
        checkNotNull(setting);

        mHandler.post(() -> {
            RadioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().radioInfo;

            if (!mStatusHolder.getTunerFunctionSettingStatus().ptySearchSettingEnabled) {
                Timber.w("startPtySearch() Disabled.");
                return;
            }

            if (mSourceController.isActive()) {
                mSourceController.startPtySearch(setting);
            } else {
                Timber.w("startPtySearch() not active.");
            }
        });
    }

    /**
     * マニュアルUP.
     * <p>
     * SeekUpやSeekDown中の解除に使用する
     */
    public void manualUp(){
        mHandler.post(() -> {
            RadioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().radioInfo;

            if(info.tunerStatus == TunerStatus.BSM){
                Timber.w("channelDown() bsm running.");
                return;
            }

            if (mSourceController.isActive()) {
                mSourceController.manualDown();
            } else {
                Timber.w("manualUp() not active.");
            }
        });
    }

    /**
     * ボリュームアップ.
     */
    public void volumeUp() {
        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.volumeUp();
            } else {
                Timber.w("volumeUp() not active.");
            }
        });
    }

    /**
     * ボリュームダウン.
     */
    public void volumeDown() {
        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.volumeDown();
            } else {
                Timber.w("volumeDown() not active.");
            }
        });
    }
}
