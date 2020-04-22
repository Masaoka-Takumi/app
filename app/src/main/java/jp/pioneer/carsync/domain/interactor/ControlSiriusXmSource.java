package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.IntRange;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.component.SiriusXmSourceController;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;
import jp.pioneer.carsync.domain.model.TunerStatus;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Sirius XM操作.
 */
public class ControlSiriusXmSource {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    private SiriusXmSourceController mSourceController;

    /**
     * コンストラクタ
     *
     * @param carDevice CarDevice
     */
    @Inject
    public ControlSiriusXmSource(CarDevice carDevice) {
        mSourceController = (SiriusXmSourceController) carDevice.getSourceController(MediaSourceType.SIRIUS_XM);
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
     * <p>
     * サブスクリプションアップデート表示中の場合は実施しない。
     * リプレイモードの場合は実施しない。
     * スキャン中の場合は実施しない。
     */
    public void channelUp(){
        mHandler.post(() -> {
            SxmMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().sxmMediaInfo;

            if (info.subscriptionUpdatingShowing) {
                Timber.w("channelUp() is showing subscription.");
                return;
            } else if (info.inReplayMode) {
                Timber.w("channelUp() is replay mode.");
                return;
            } else if (info.tunerStatus == TunerStatus.SCAN){
                Timber.w("channelUp() is scanning.");
                return;
            }

            if (mSourceController.isActive()) {
                mSourceController.channelUp();
            } else {
                Timber.w("channelUp() not active.");
            }
        });
    }

    /**
     * チャンネルダウン.
     * <p>
     * サブスクリプションアップデート表示中の場合は実施しない。
     * リプレイモードの場合は実施しない。
     * スキャン中の場合は実施しない。
     */
    public void channelDown(){
        mHandler.post(() -> {
            SxmMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().sxmMediaInfo;

            if (info.subscriptionUpdatingShowing) {
                Timber.w("channelDown() is showing subscription.");
                return;
            } else if (info.inReplayMode) {
                Timber.w("channelDown() is replay mode.");
                return;
            } else if (info.tunerStatus == TunerStatus.SCAN){
                Timber.w("channelDown() is scanning.");
                return;
            }

            if (mSourceController.isActive()) {
                mSourceController.channelDown();
            } else {
                Timber.w("channelDown() not active.");
            }
        });
    }

    /**
     * Scanアップ.
     * <p>
     * サブスクリプションアップデート表示中の場合は実施しない。
     * リプレイモードの場合は実施しない。
     * スキャン中ではない場合は実施しない。
     */
    public void scanUp(){
        mHandler.post(() -> {
            SxmMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().sxmMediaInfo;

            if (info.subscriptionUpdatingShowing) {
                Timber.w("scanUp() is showing subscription.");
                return;
/*            } else if (info.inReplayMode) {
                Timber.w("scanUp() is replay mode.");
                return;*/
            } else if (info.tunerStatus != TunerStatus.SCAN){
                Timber.w("channelUp() is not scanning.");
                return;
            }

            if (mSourceController.isActive()) {
                mSourceController.scanUp();
            } else {
                Timber.w("channelUp() not active.");
            }
        });
    }

    /**
     * Scanダウン.
     * <p>
     * サブスクリプションアップデート表示中の場合は実施しない。
     * リプレイモードの場合は実施しない。
     * スキャン中ではない場合は実施しない。
     */
    public void scanDown(){
        mHandler.post(() -> {
            SxmMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().sxmMediaInfo;

            if (info.subscriptionUpdatingShowing) {
                Timber.w("scanDown() is showing subscription.");
                return;
/*            } else if (info.inReplayMode) {
                Timber.w("scanDown() is replay mode.");
                return;*/
            } else if (info.tunerStatus != TunerStatus.SCAN){
                Timber.w("scanDown() is not scanning.");
                return;
            }

            if (mSourceController.isActive()) {
                mSourceController.scanDown();
            } else {
                Timber.w("channelDown() not active.");
            }
        });
    }

    /**
     * トグルPlay.
     * <p>
     * PlayとPauseを切り替える
     * <p>
     * サブスクリプションアップデート表示中の場合は実施しない。
     * チャンネルモード且つPause状態以外の場合は実施しない。
     */
    public void togglePlay(){
        mHandler.post(() -> {
            SxmMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().sxmMediaInfo;

            if (info.subscriptionUpdatingShowing) {
                Timber.w("togglePlay() is showing subscription.");
                return;
            } else if (!info.inReplayMode && info.playbackMode != PlaybackMode.PAUSE) {
                Timber.w("togglePlay() is not replay mode and is not pause mode.");
                return;
            }

            if (mSourceController.isActive()) {
                mSourceController.togglePlay();
            } else {
                Timber.w("togglePlay() not active.");
            }
        });
    }

    /**
     * P.CHアップ.
     * <p>
     * 以下状態の場合は実行しない
     * ・BSM中
     * ・購読更新ポップアップ表示中
     * ・リプレイモード
     * ・ポーズ中
     */
    public void presetUp() {
        mHandler.post(() -> {
            SxmMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().sxmMediaInfo;

            if (info.subscriptionUpdatingShowing) {
                Timber.w("presetUp() is showing subscription.");
                return;
            } else if (info.inReplayMode) {
                Timber.w("presetUp() is replay mode.");
                return;
            }

            if (mSourceController.isActive()) {
                mSourceController.presetUp();
            } else {
                Timber.w("presetUp() not active.");
            }
        });
    }

    /**
     * P.CHダウン.
     * <p>
     * 以下状態の場合は実行しない
     * ・BSM中
     * ・購読更新ポップアップ表示中
     * ・リプレイモード
     * ・ポーズ中
     */
    public void presetDown() {
        mHandler.post(() -> {
            SxmMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().sxmMediaInfo;

            if (info.subscriptionUpdatingShowing) {
                Timber.w("presetDown() is showing subscription.");
                return;
            } else if (info.inReplayMode) {
                Timber.w("presetDown() is replay mode.");
                return;
            }

            if (mSourceController.isActive()) {
                mSourceController.presetDown();
            } else {
                Timber.w("presetDown() not active.");
            }
        });
    }

    /**
     * ライブモード切り替え.
     * <p>
     * サブスクリプションアップデート表示中の場合は実施しない。
     * チャンネルモードの場合は実施しない。
     * スキャン中の場合は実施しない。
     */
    public void toggleLiveMode() {
        mHandler.post(() -> {
            SxmMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().sxmMediaInfo;
            if (info.subscriptionUpdatingShowing) {
                Timber.w("toggleLiveMode() is showing subscription.");
                return;
            }

            if (!info.inReplayMode) {
                Timber.w("toggleLiveMode() is not replay mode.");
                return;
            }

            if (info.tunerStatus == TunerStatus.SCAN) {
                Timber.w("toggleLiveMode() is scanning.");
                return;
            }

            if (mSourceController.isActive()) {
                mSourceController.toggleLiveMode();
            } else {
                Timber.w("toggleLiveMode() not active.");
            }
        });
    }

    /**
     * チャンネルモード切り替え.
     * <p>
     * サブスクリプションアップデート表示中の場合は実施しない。
     * チャンネルモードの場合は実施しない。
     * スキャン中の場合は実施しない。
     */
    public void toggleChannelMode() {
        mHandler.post(() -> {
            SxmMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().sxmMediaInfo;
            if (info.subscriptionUpdatingShowing) {
                Timber.w("toggleChannelMode() is showing subscription.");
                return;
            }

            if (!info.inReplayMode) {
                Timber.w("toggleChannelMode() is not replay mode.");
                return;
            }

            if (info.tunerStatus == TunerStatus.SCAN) {
                Timber.w("toggleChannelMode() is scanning.");
                return;
            }

            if (mSourceController.isActive()) {
                mSourceController.toggleChannelModeOrReplayMode();
            } else {
                Timber.w("toggleChannelMode() not active.");
            }
        });
    }

    /**
     * チューンミックス切り替え.
     * <p>
     * サブスクリプションアップデート表示中の場合は実施しない。
     * リプレイモードの場合は実施しない。//2018.10.2 リプレイモード中も実施
     * チューンミックスが非対応の場合は実施しない。
     * スキャン中の場合は実施しない。
     */
    public void toggleTuneMix() {
        mHandler.post(() -> {
            SxmMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().sxmMediaInfo;
            if (info.subscriptionUpdatingShowing) {
                Timber.w("toggleTuneMix() is showing subscription.");
                return;
            }

/*            if (info.inReplayMode) {
                Timber.w("toggleTuneMix() is replay mode.");
                return;
            }*/

            if (!info.tuneMixAvailable) {
                Timber.w("toggleTuneMix() is tune mix unavailable.");
                return;
            }

            if (info.tunerStatus == TunerStatus.SCAN) {
                Timber.w("toggleTuneMix() is scanning.");
                return;
            }

            if (mSourceController.isActive()) {
                mSourceController.toggleTuneMix();
            } else {
                Timber.w("toggleTuneMix() not active.");
            }
        });
    }

    /**
     * リプレイモード切り替え.
     * <p>
     * サブスクリプションアップデート表示中の場合は実施しない。
     * リプレイモードの場合は実施しない。
     * スキャン中の場合は実施しない。
     */
    public void toggleReplayMode() {
        mHandler.post(() -> {
            SxmMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().sxmMediaInfo;
            if (info.subscriptionUpdatingShowing) {
                Timber.w("toggleReplayMode() is showing subscription.");
                return;
            }

            if (info.inReplayMode) {
                Timber.w("toggleReplayMode() is replay mode.");
                return;
            }

            if (info.tunerStatus == TunerStatus.SCAN) {
                Timber.w("toggleReplayMode() is scanning.");
                return;
            }
            if (mSourceController.isActive()) {
                mSourceController.toggleChannelModeOrReplayMode();
            } else {
                Timber.w("toggleReplayMode() not active.");
            }
        });
    }

    /**
     * SubscriptionUpdate解除.
     */
    public void releaseSubscriptionUpdating() {
        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.releaseSubscriptionUpdating();
            } else {
                Timber.w("releaseSubscriptionUpdating() not active.");
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
