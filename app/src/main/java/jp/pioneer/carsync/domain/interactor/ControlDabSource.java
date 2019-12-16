package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.component.DabSourceController;
import jp.pioneer.carsync.domain.model.DabInfo;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * DAB操作.
 */
public class ControlDabSource {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    private DabSourceController mSourceController;

    /**
     * コンストラクタ
     *
     * @param carDevice CarDevice
     */
    @Inject
    public ControlDabSource(CarDevice carDevice) {
        mSourceController = (DabSourceController) carDevice.getSourceController(MediaSourceType.DAB);
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
     * ライブモード/タイムシフトモード切り替え.
     */
    public void toggleMode() {
        mHandler.post(() -> {
            final DabInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().dabInfo;
            if (info.timeShiftMode || !info.timeShiftModeAvailable) {
                mSourceController.toggleLiveMode();
            } else {
                mSourceController.toggleTimeShiftMode();
            }

        });
    }

    /**
     * トグルPlay.
     * <p>
     * PlayとPauseを切り替える
     */
    public void togglePlay() {
        mHandler.post(() -> {
            if (!mSourceController.isActive()) {
                Timber.w("togglePlay() not active.");
                return;
            }

            final DabInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().dabInfo;
            if (info.isErrorStatus()) {
                Timber.w("togglePlay() info.isErrorStatus()");
                return;
            }
            if (!info.timeShiftMode || !info.timeShiftModeAvailable) {
                mSourceController.togglePlay();
            }
        });
    }

    /**
     * Seek Up/Seek Cancel.
     */
    public void toggleSeek() {
        mHandler.post(() -> {
            if (!mSourceController.isActive()) {
                Timber.w("toggleSeek() not active.");
                return;
            }

            final DabInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().dabInfo;
            switch (info.tunerStatus) {
                case LIST_UPDATE:
                case ERROR:
                    Timber.w("toggleSeek(): tunerStatus=%s", info.tunerStatus);
                    return;
            }

            if(info.timeShiftMode){
                return;
            }

            if (info.isSearchStatus()) {
                mSourceController.seekCancel();
            } else {
                mSourceController.seekUp();
            }
        });
    }


    /**
     * P.CHアップ.
     */
    public void presetUp() {
        mHandler.post(() -> {
            if (!mSourceController.isActive()) {
                Timber.w("presetUp() not active.");
                return;
            }

            final DabInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().dabInfo;
            switch (info.tunerStatus) {
                case LIST_UPDATE:
                case ERROR:
                    Timber.w("presetUp(): tunerStatus=%s", info.tunerStatus);
                    return;
            }
            mSourceController.presetUp();
        });
    }

    /**
     * P.CHダウン.
     */
    public void presetDown() {
        mHandler.post(() -> {
            if (!mSourceController.isActive()) {
                Timber.w("presetDown() not active.");
                return;
            }

            final DabInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().dabInfo;
            switch (info.tunerStatus) {
                case LIST_UPDATE:
                case ERROR:
                    Timber.w("presetUp(): tunerStatus=%s", info.tunerStatus);
                    return;
            }
            mSourceController.presetDown();
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
     * リスト更新
     */
    public void updateList() {
        mHandler.post(() -> {
            if (!mSourceController.isActive()) {
                Timber.w("updateList() not active.");
                return;
            }

            mSourceController.updateList();
        });
    }

    /**
     * ABCサーチ実行
     *
     * @param word サーチ文字
     */
    public void executeAbsSearch(@NonNull String word) {
        mHandler.post(() -> {
            if (!mSourceController.isActive()) {
                Timber.w("executeAbsSearch() not active.");
                return;
            }

            mSourceController.executeAbcSearch(word);
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
