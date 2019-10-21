package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.component.SpotifySourceController;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.SpotifyMediaInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import timber.log.Timber;

/**
 * Spotify操作.
 */
public class ControlSpotifySource {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    private SpotifySourceController mSourceController;

    /**
     * コンストラクタ.
     *
     * @param carDevice CarDevice
     */
    @Inject
    public ControlSpotifySource(CarDevice carDevice) {
        mSourceController = (SpotifySourceController) carDevice.getSourceController(MediaSourceType.SPOTIFY);
    }

    /**
     * 再生状態変更.
     */
    public void togglePlay() {
        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.togglePlay();
            } else {
                Timber.w("togglePlay() not active.");
            }
        });
    }

    /**
     * 次のトラック再生.
     */
    public void skipNextTrack() {
        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.skipNextTrack();
            } else {
                Timber.w("skipNextTrack() not active.");
            }
        });
    }

    /**
     * 前のトラック再生.
     */
    public void skipPreviousTrack() {
        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.skipPreviousTrack();
            } else {
                Timber.w("skipPreviousTrack() not active.");
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

    /**
     * リピートモード変更.
     * <p>
     * {@link SpotifyMediaInfo#radioPlaying} がfalseの場合は実行しない。
     */
    public void toggleRepeatMode() {

        mHandler.post(() -> {
            SpotifyMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().spotifyMediaInfo;

            if (!info.radioPlaying) {
                if (mSourceController.isActive()) {
                    mSourceController.toggleRepeatMode();
                } else {
                    Timber.w("toggleRepeatMode() not active.");
                }
            } else {
                Timber.w("toggleRepeatMode() not browsePlaying.");
            }
        });
    }

    /**
     * シャッフルモード変更.
     * <p>
     * * {@link SpotifyMediaInfo#radioPlaying} がfalseの場合は実行しない。
     */
    public void toggleShuffleMode() {
        mHandler.post(() -> {
            SpotifyMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().spotifyMediaInfo;

            if (!info.radioPlaying) {
                if (mSourceController.isActive()) {
                    mSourceController.toggleShuffleMode();
                } else {
                    Timber.w("toggleShuffleMode() not active.");
                }
            } else {
                Timber.w("toggleShuffleMode() not browsePlaying.");
            }
        });
    }

    /**
     * ThumbUp設定.
     * <p>
     * {@link SpotifyMediaInfo#radioPlaying} がfalseの場合は実行しない。
     */
    public void setThumbUp() {
        mHandler.post(() -> {
            SpotifyMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().spotifyMediaInfo;

            if (info.radioPlaying) {
                if (mSourceController.isActive()) {
                    mSourceController.setThumbUp();
                } else {
                    Timber.w("setThumbUp() not active.");
                }
            } else {
                Timber.w("setThumbUp() not radioPlaying.");
            }
        });
    }

    /**
     * ThumbDown設定.
     * <p>
     * * {@link SpotifyMediaInfo#radioPlaying} がfalseの場合は実行しない。
     */
    public void setThumbDown() {

        mHandler.post(() -> {
            SpotifyMediaInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().spotifyMediaInfo;

            if (info.radioPlaying) {
                if (mSourceController.isActive()) {
                    mSourceController.setThumbDown();
                } else {
                    Timber.w("setThumbDown() not active.");
                }
            } else {
                Timber.w("setThumbUp() not radioPlaying.");
            }
        });
    }
}
