package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.AppMusicSourceController;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.content.AppMusicContract;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * AppMusic操作
 */
public class ControlAppMusicSource {
    @Inject @ForInfrastructure Handler mHandler;
    private AppMusicSourceController mSourceController;
    private Integer pendingFastForwardPlaybackTime = null;
    private Integer pendingRewindPlaybackTime = null;

    /**
     * コンストラクタ.
     *
     * @param carDevice CarDevice
     */
    @Inject
    public ControlAppMusicSource(CarDevice carDevice) {
        mSourceController = (AppMusicSourceController) carDevice.getSourceController(MediaSourceType.APP_MUSIC);
    }

    /**
     * 再生.
     *
     * @param params 再生内容
     * @throws NullPointerException {@code params}がnull
     */
    public void play(@NonNull AppMusicContract.PlayParams params) {
        checkNotNull(params);

        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.play(params);
            } else {
                Timber.w("play() not active.");
            }
        });
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
     * リピートモード変更.
     */
    public void toggleRepeatMode() {
        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.toggleRepeatMode();
            } else {
                Timber.w("toggleRepeatMode() not active.");
            }
        });
    }

    /**
     * シャッフルモード変更.
     */
    public void toggleShuffleMode() {
        mHandler.post(() -> {
            if (mSourceController.isActive()) {
                mSourceController.toggleShuffleMode();
            } else {
                Timber.w("toggleShuffleMode() not active.");
            }
        });
    }

    /**
     * 次の楽曲再生.
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
     * 前の楽曲再生.
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
     * スペアナデータリスナー設定.
     * <p>
     * 現在のソース種別に関係なく設定可能であるが、スペアナのデータが通知されるのは{@link MediaSourceType#APP_MUSIC}
     * の時だけである。
     *
     * @param listener スペアナデータリスナー。nullを指定すると設定解除。不要になったら
     *                 {@link #deleteSpeAnaDataListener(AppMusicSourceController.OnSpeAnaDataListener)}にて
     *                 解除すること。
     * @throws NullPointerException {@code listener}がnull
     */
    public void addSpeAnaDataListener(@NonNull AppMusicSourceController.OnSpeAnaDataListener listener) {
        checkNotNull(listener);

        mHandler.post(() -> mSourceController.addSpeAnaDataListener(listener));
    }

    /**
     * スペアナデータリスナー設定解除.
     *
     * @param listener {@link #addSpeAnaDataListener(AppMusicSourceController.OnSpeAnaDataListener)}
     *                 で設定したスペアナデータリスナー
     * @throws NullPointerException {@code listener}がnull
     */
    public void deleteSpeAnaDataListener(@NonNull AppMusicSourceController.OnSpeAnaDataListener listener) {
        checkNotNull(listener);

        mHandler.post(() -> mSourceController.deleteSpeAnaDataListener(listener));
    }

    /**
     * 早送り.
     * <p>
     * 指定された時間分早送りを実施する
     *
     * @param time 早送り時間(ミリ秒)
     */
    public void fastForward(int time) {
        boolean isNeedDispatch = false;
        synchronized (this){
            if(pendingFastForwardPlaybackTime == null){
                isNeedDispatch = true;
            }
            pendingFastForwardPlaybackTime = time;
        }

        if(!isNeedDispatch){
            return;
        }

        mHandler.post(() -> {
            synchronized (this) {
                if (mSourceController.isActive()) {
                    mSourceController.fastForwardForPlayer(pendingFastForwardPlaybackTime);
                } else {
                    Timber.w("fastForward() not active.");
                }

                pendingFastForwardPlaybackTime = null;
            }
        });
    }

    /**
     * 巻き戻し.
     * <p>
     * 指定された時間分巻き戻しを実施する
     *
     * @param time 巻き戻し時間(ミリ秒)
     */
    public void rewind(int time) {
        boolean isNeedDispatch = false;
        synchronized (this){
            if(pendingRewindPlaybackTime == null){
                isNeedDispatch = true;
            }
            pendingRewindPlaybackTime = time;
        }

        if(!isNeedDispatch){
            return;
        }

        mHandler.post(() -> {
            synchronized (this) {
                if (mSourceController.isActive()) {
                    mSourceController.rewindForPlayer(pendingRewindPlaybackTime);
                } else {
                    Timber.w("rewind() not active.");
                }

                pendingRewindPlaybackTime = null;
            }
        });
    }

    /**
     * フォーカス破棄.
     */
    public void abandonFocus(){
        if (mSourceController.isActive()) {
            mSourceController.abandonAudioFocus();
        } else {
            Timber.w("abandonFocus() not active.");
        }
    }

    /**
     * 再生時間送信.
     */
    public void sendPlaybackTime(int durationInSec, int positionInSec){
        if (mSourceController.isActive()) {
            mSourceController.sendPlaybackTime(durationInSec,positionInSec);
        } else {
            Timber.w("sendPlaybackTime() not active.");
        }
    }
    /**
     * 曲情報送信.
     */
    public void sendMusicInfo(){
        if (mSourceController.isActive()) {
            mSourceController.sendMusicInfo();
        } else {
            Timber.w("sendPlaybackTime() not active.");
        }
    }
}
