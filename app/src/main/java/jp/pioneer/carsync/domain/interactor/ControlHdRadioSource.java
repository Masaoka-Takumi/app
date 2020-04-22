package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.IntRange;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.component.HdRadioSourceController;
import jp.pioneer.carsync.domain.model.HdRadioFunctionSetting;
import jp.pioneer.carsync.domain.model.HdRadioInfo;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TunerStatus;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * HD Radio操作.
 */
public class ControlHdRadioSource {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    private HdRadioSourceController mSourceController;

    /**
     * コンストラクタ
     *
     * @param carDevice CarDevice
     */
    @Inject
    public ControlHdRadioSource(CarDevice carDevice) {
        mSourceController = (HdRadioSourceController) carDevice.getSourceController(MediaSourceType.HD_RADIO);
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
     * プリセットCH呼び出し.
     * <p>
     * 引数のプリセットCH番号に変更する。
     *
     * @param presetNo プリセット番号.
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
     * プリセット登録.
     *
     * @param listIndex プリセットリストインデックス.
     * @throws IllegalArgumentException {@code presetNo} の値が不正.
     */
    public void registerPreset(@IntRange(from = 1) int listIndex) {
        checkArgument(listIndex >= 1);

        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.registerPreset(listIndex);
            } else {
                Timber.w("registerPreset() not active.");
            }
        });
    }

    /**
     * チャンネルアップ.
     */
    public void channelUp() {
        mHandler.post(() -> {
            HdRadioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().hdRadioInfo;
            HdRadioFunctionSetting setting = mStatusHolder.getHdRadioFunctionSetting();

            if(info.tunerStatus == TunerStatus.BSM){
                Timber.w("channelUp() bsm running.");
                return;
            }

            if (mSourceController.isActive()) {
                mSourceController.presetUp();
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
            HdRadioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().hdRadioInfo;
            HdRadioFunctionSetting setting = mStatusHolder.getHdRadioFunctionSetting();

            if(info.tunerStatus == TunerStatus.BSM){
                Timber.w("channelDown() bsm running.");
                return;
            }

            if (mSourceController.isActive()) {
                mSourceController.presetDown();
            } else {
                Timber.w("channelDown() not active.");
            }
        });
    }

    /**
     * Seek UP.
     */
    public void seekUp() {
        mHandler.post(() -> {
            HdRadioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().hdRadioInfo;
            HdRadioFunctionSetting setting = mStatusHolder.getHdRadioFunctionSetting();

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
            if (!mStatusHolder.getHdRadioFunctionSettingStatus().bsmSettingEnabled) {
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
     * マニュアルUP.
     * <p>
     * SeekUpやSeekDown中の解除に使用する
     */
    public void manualUp(){
        mHandler.post(() -> {
            HdRadioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().hdRadioInfo;

            if(info.tunerStatus == TunerStatus.BSM){
                Timber.w("channelDown() bsm running.");
                return;
            }

            if (mSourceController.isActive()) {
                mSourceController.manualUp();
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
