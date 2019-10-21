package jp.pioneer.mbg.alexa.player;

import com.google.android.exoplayer2.audio.WLAudioStreamEx;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Playerクラス共通の公開メソッド、及び、コールバックをまとめる
 */
public interface IAlexaPlayer {

    /**
     * 再生状態管理enum.
     * TODO ExoAlexaPlayerでは再生終了時にCloseしないため、共通化は難しい
     */
    public enum PlaybackState {
        PLAYING,
        PREPARE,
        PAUSE,
        STOP,
        FINISHED,
    }

    /**
     * 再生状態のコールバック.
     */
    public interface PlaybackCallback {
        void onCompletion();
        long onPrepare();
        boolean onPrepared();
        void onError(PlaybackErrorType type, String message);
        long onPlaybackReady();
        void onPlaybackStarted();
        void onBufferedFinish();
        void onResumed();
        void onPaused();
        void onAdjustVolume(float volume);
        void onSetVolume(float volume);
        void onSetMute(boolean isMute);
        void onNoResponse();      // 元のソースでも未使用のため削除予定
        void onWLAudioFocusLoss();
    }

    /**
     * エラータイプ
     */
    public enum PlaybackErrorType {
        ERROR_UNKNOWN("MEDIA_ERROR_UNKNOWN"),                      // 基本
        ERROR_INVALID_REQUEST("MEDIA_ERROR_INVALID_REQUEST"),              // 無効なリクエスト、認証エラー
        ERROR_SERVICE_UNAVAILABLE("MEDIA_ERROR_SERVICE_UNAVAILABLE"),          // タイムアウト
        ERROR_INTERNAL_SERVER_ERROR("MEDIA_ERROR_INTERNAL_SERVER_ERROR"),        // サーバーエラー
        ERROR_INTERNAL_DEVICE_ERROR("MEDIA_ERROR_INTERNAL_DEVICE_ERROR");        // 内部エラー（未使用)

        public String name = null;

        PlaybackErrorType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name == null ? "" : this.name;
        }
    }


    public void setCallback(IAlexaPlayer.PlaybackCallback callback);
    public void release();
    public void createPlayer(String url, boolean isDoFocusWaiting) throws IOException;
    public void createPlayer(FileDescriptor fd, boolean isDoFocusWaiting) throws IOException;
    public void createPlayer(FileDescriptor fd, long offset, long length, boolean isDoFocusWaiting) throws IOException;
    public void stopPlayer();
    public void releasePlayer();
    public void setVolume(float volume);
    public void setVolumeCallback(float volume);
    public void adjustVolume(float volume);
    public void adjustVolumeCallback(float volume);
    public void setMute(boolean isMute);
    public void setMuteCallback(boolean isMute);
    public void setPause();
    public void setAttenuate(boolean attenuate);
    public float getVolume();
    public boolean isMute();
    public boolean start();
    public boolean pause();
    public int getDuration();
    public int getCurrentPosition();
    public boolean isForcedStopping();
    public void setForcedStopping(boolean isForcedStopping);
    public IAlexaPlayer.PlaybackState getPlaybackState();
    public @WLAudioStreamEx.WLPLAYSTATE int getWLPlaybackState();
    public boolean isPlaying();
    public void resetSpeech();
}
