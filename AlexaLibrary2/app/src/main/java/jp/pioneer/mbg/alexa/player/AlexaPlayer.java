package jp.pioneer.mbg.alexa.player;

import android.content.Context;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.google.android.exoplayer2.audio.WLAudioStreamEx;

import java.io.FileDescriptor;
import java.io.IOException;

import jp.pioneer.mbg.alexa.AmazonAlexaManager;
import jp.pioneer.mbg.alexa.util.LogUtil;
import jp.pioneer.mbg.android.vozsis.R;

/**
 * 音声／音楽再生用プレーヤー（AndroidOS標準MediaPlayer使用）
 */
public class AlexaPlayer implements
        IAlexaPlayer,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener,
        MediaPlayer.OnBufferingUpdateListener {

    private static final String TAG = AlexaPlayer.class.getSimpleName();
    private static final boolean DBG = true;
    private static final boolean ToastDBG = false;

    /**
     * MediaPlayerインスタンス
     */
    private MediaPlayer mMediaPlayer = null;

    /**
     * V1.5.0問題点リストNo.1 ガベージコレクション対策
     */
    private MediaPlayer mTempPlayer = null;

    /**
     * コンテキスト
     */
    private Context mContext = null;

    /**
     * 再生状態通知用コールバックリスナー
     */
    private IAlexaPlayer.PlaybackCallback mCallback;

    /**
     * マスターボリューム
     */
    private float mCurrentVolume = 1.0f;

    /**
     * アテネートボリューム
     */
    private float mAttenuateRatio = 1.0f;

    /**
     * ミュート状態
     */
    private boolean mIsMute = false;

    /**
     * 再生状態
     */
    private IAlexaPlayer.PlaybackState mPlaybackState = IAlexaPlayer.PlaybackState.STOP;

    /**
     * 一時停止状態でMediaPlayerはリリースしないが、再生再開はさせない場合にtrue
     */
    private boolean mIsForcedStopping = false;

    /**
     * コンストラクター
     * @param context
     * @param view
     */
    public AlexaPlayer(Context context, View view) {
        this(context, null, view);
    }

    /**
     * コンストラクター
     * @param context
     * @param callback
     * @param view
     */
    public AlexaPlayer(Context context, IAlexaPlayer.PlaybackCallback callback, View view){
        this.mContext = context;
        this.mCallback = callback;

        init();
    }

    /**
     * 初期化処理
     */
    private void init(){

    }

    /**
     * コールバックインスタンス登録
     * @param callback
     */
    public void setCallback(IAlexaPlayer.PlaybackCallback callback){
        this.mCallback = callback;
    }

    @Override
    public void release() {

    }

    /**
     * MediaPlayer初期化
     * @return
     */
    private MediaPlayer initPlayer() {
        if (mMediaPlayer != null) {
            stopPlayer();
        }
        mTempPlayer = new MediaPlayer();
        mTempPlayer.setOnInfoListener(this);
        mTempPlayer.setOnCompletionListener(this);
        mTempPlayer.setOnErrorListener(this);
        mTempPlayer.setOnPreparedListener(this);
        mTempPlayer.setOnBufferingUpdateListener(this);

        mPlaybackState = IAlexaPlayer.PlaybackState.STOP;

        return mTempPlayer;
    }

    /**
     * MediaPlayer生成
     * @param url
     * @throws IOException
     */
    public synchronized void createPlayer(String url, boolean isDoFocusWaiting) throws IOException {
        LogUtil.d(TAG, "createPlayer(String) start");
        LogUtil.d(TAG, " - createPlayer(String), url = " + url);

        if (! URLUtil.isValidUrl(url) || mContext == null) {
            throw new IOException();
        }

        MediaPlayer tempPlayer = initPlayer();

        if (Build.VERSION.SDK_INT > 26) {
            setAudioStreamType_NewApi(tempPlayer);
        } else {
            setAudioStreamType_OldApi(tempPlayer);
        }

        tempPlayer.setDataSource(mContext, Uri.parse(url));

        if (AmazonAlexaManager.getInstance().isWebLinkConnection()) {
            tempPlayer.prepareAsync();  // 再生準備する
        }
        mPlaybackState = IAlexaPlayer.PlaybackState.PREPARE;
        if (mCallback != null) {
            mCallback.onPrepare();
        }
        LogUtil.d(TAG, "createPlayer(String) end");
    }

    /**
     * MediaPlayer生成
     * @param fileDescriptor
     * @throws IOException
     */
    public synchronized void createPlayer(FileDescriptor fileDescriptor, boolean isDoFocusWaiting) throws IOException {
        LogUtil.d(TAG, "createPlayer(FileDescriptor) start");
        MediaPlayer tempPlayer = initPlayer();

        if (fileDescriptor != null) {
            tempPlayer.setDataSource(fileDescriptor);
        }
        tempPlayer.prepareAsync();

        mPlaybackState = IAlexaPlayer.PlaybackState.PREPARE;
        if(mCallback != null){
            mCallback.onPrepare();
        }
        LogUtil.d(TAG, "createPlayer(FileDescriptor) end");
    }

    /**
     * MediaPlayer生成(マイク音声でAssetFileDescriptorを使用した際に必要)
     * @param fd
     * @param offset
     * @param length
     * @throws IOException
     */
    @Override
    public synchronized void createPlayer(FileDescriptor fd, long offset, long length, boolean isDoFocusWaiting) throws IOException {
        // NOP
    }

    @RequiresApi(26)
    private void setAudioStreamType_NewApi(MediaPlayer player) {
        player.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build());
    }

    @SuppressWarnings("deprecation")
    private void setAudioStreamType_OldApi(MediaPlayer player) {
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }


    /**
     * MediaPlayer停止
     */
    private void internalStopPlayer() {
        LogUtil.d(TAG, "internalStopPlayer start");
        if(mMediaPlayer != null){
            // TODO:Kindle再生時などに、ここでseekToを行うとエラー(-2147483648, 0)が発生する
//            if(isPlaying()){
//                mMediaPlayer.stop();
//            }
//            mMediaPlayer.release();
//            mMediaPlayer = null;
            boolean isPlaying = isPlaying();
            MediaPlayer tempPlayer = mMediaPlayer;
            mMediaPlayer = null;
            if(isPlaying){
                tempPlayer.stop();
            }
            tempPlayer.release();

            mPlaybackState = IAlexaPlayer.PlaybackState.STOP;
        }
    }

    /**
     * MediaPlayer停止
     */
    public synchronized void stopPlayer(){
        internalStopPlayer();
    }

    /**
     * MediaPlayerインスタンス破棄
     */
    public synchronized void releasePlayer(){
        if(mMediaPlayer != null){
            mMediaPlayer.release();
        }
        mMediaPlayer = null;
    }

    /**
     * MediaPlayerに音量を設定する
     * @param volume 0～100
     */
    public void setVolume(float volume) {
        if (mMediaPlayer != null) {

            float v = volume / 100.0f;
            if(mCallback != null){
                mCallback.onSetVolume(volume);
            }
            mCurrentVolume = v;
            if (mIsMute == true) {
                // ミュート状態のときは設定しない
            } else {
                if(mPlaybackState==IAlexaPlayer.PlaybackState.PLAYING || mPlaybackState==IAlexaPlayer.PlaybackState.PAUSE) {
                    //mMediaPlayer.setVolume(v * mAttenuateRatio, v * mAttenuateRatio);
                }
            }
        }
    }

    //コールバックだけ送る
    public void setVolumeCallback(float volume){
        if(mCallback != null){
            mCallback.onSetVolume(volume);
        }
    }


    /**
     * MediaPlayerに音量を設定する
     * @param volume
     */
    public void adjustVolume(float volume) {
        if (mMediaPlayer != null) {
            float v = mCurrentVolume + volume  / 100.0f;
            if(mCallback != null){
                mCallback.onAdjustVolume(volume);
            }
            mCurrentVolume = v;
            if (mIsMute == true) {
                // ミュート状態のときは設定しない
            } else {

                if(mPlaybackState==IAlexaPlayer.PlaybackState.PLAYING || mPlaybackState==IAlexaPlayer.PlaybackState.PAUSE) {
                    //mMediaPlayer.setVolume(v * mAttenuateRatio, v * mAttenuateRatio);
                }
            }
        }
    }

    //コールバックだけ送る
    public void adjustVolumeCallback(float volume){
        if(mCallback != null){
            mCallback.onAdjustVolume(volume);
        }
    }

    /**
     * ミュート状態を設定する
     * @param isMute
     */
    public void setMute(boolean isMute) {
        mIsMute = isMute;

        if (mMediaPlayer != null) {
            if (mIsMute) {
                if(mPlaybackState==IAlexaPlayer.PlaybackState.PLAYING || mPlaybackState==IAlexaPlayer.PlaybackState.PAUSE) {
//                    mMediaPlayer.pause();
                    if(mCallback != null){
                        mCallback.onSetMute(isMute);
                    }
                    //mMediaPlayer.setVolume(0.0f,0.0f);
                }
            }
            else {
                if(mPlaybackState==IAlexaPlayer.PlaybackState.PLAYING || mPlaybackState==IAlexaPlayer.PlaybackState.PAUSE) {
                    //mMediaPlayer.setVolume(mCurrentVolume * mAttenuateRatio, mCurrentVolume * mAttenuateRatio);
                    if(mCallback != null){
                        mCallback.onSetMute(isMute);
                    }
                }
            }
        }
    }

    //コールバックだけ送る
    public void setMuteCallback(boolean isMute){
        if(mCallback != null){
            mCallback.onSetMute(isMute);
        }
    }

    public void setPause(){
        if (mMediaPlayer != null) {
            if(mPlaybackState==IAlexaPlayer.PlaybackState.PLAYING || mPlaybackState==IAlexaPlayer.PlaybackState.PAUSE) {
                mMediaPlayer.pause();
            }
        }
    }

    /**
     * ボリューム設定をMediaPlayerに反映する
     */
    private void updateVolume() {
        this.setMuteCallback(mIsMute);
    }

    /**
     * アテネートを設定する
     * @param attenuate
     */
    public void setAttenuate(boolean attenuate) {
        if (attenuate) {
            mAttenuateRatio = 0.2f;
        }
        else {
            mAttenuateRatio = 1.0f;
        }
        updateVolume();
    }

    /**
     * MediaPlayerのボリュームを取得する
     * @return 0～100
     */
    public float getVolume() {
        return mCurrentVolume * 100.0f;
    }

    /**
     * ミュート状態を取得する
     * @return
     */
    public boolean isMute() {
        return mIsMute;
    }

    /**
     * 再生完了
     * @param mediaPlayer
     */
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        LogUtil.d(TAG, "onCompletion start");
        LogUtil.d(TAG, "onCompletion " + mediaPlayer);
        if (mediaPlayer == mMediaPlayer) {
            mPlaybackState = IAlexaPlayer.PlaybackState.FINISHED;

            if(mCallback != null){
                mCallback.onCompletion();
            }
        }
    }

    /**
     * エラー
     * @param mediaPlayer
     * @param what
     * @param extra
     * @return
     */
    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        LogUtil.d(TAG, "onError start");
        LogUtil.d(TAG, "onError"+mediaPlayer);
        LogUtil.d(TAG, "onError"+what);
        LogUtil.d(TAG, "onError"+extra);
        if (ToastDBG) {
            Toast.makeText(mContext, "AlexaPlayer onError(" + what + ", " + extra + ")", Toast.LENGTH_LONG).show();
        }
        releasePlayer();
        if(mCallback != null){
            IAlexaPlayer.PlaybackErrorType type = IAlexaPlayer.PlaybackErrorType.ERROR_UNKNOWN;
            String message = null;
            Resources r = mContext.getResources();
            switch (extra) {
                case MediaPlayer.MEDIA_ERROR_IO : {
                    // ファイルまたはネットワーク関連の操作エラー
                    type = IAlexaPlayer.PlaybackErrorType.ERROR_INVALID_REQUEST;
                    if (r != null) {
                        message = r.getString(R.string.alexa_playback_failed_network_error);
                    }
                    break;
                }
                case MediaPlayer.MEDIA_ERROR_MALFORMED : {
                    // ビットストリームは、関連するコーディング標準またはファイル仕様に準拠していません
                    type = IAlexaPlayer.PlaybackErrorType.ERROR_UNKNOWN;
                    if (r != null) {
                        message = r.getString(R.string.alexa_playback_failed_malformed_error);
                    }
                    break;
                }
                case MediaPlayer.MEDIA_ERROR_UNSUPPORTED : {
                    // ビットストリームは関連するコーディング標準またはファイル仕様に準拠していますが、メディアフレームワークはこの機能をサポートしていません
                    type = IAlexaPlayer.PlaybackErrorType.ERROR_UNKNOWN;
                    if (r != null) {
                        message = r.getString(R.string.alexa_playback_failed_unsupported_error);
                    }
                    break;
                }
                case MediaPlayer.MEDIA_ERROR_TIMED_OUT : {
                    // いくつかの操作は完了するのに時間がかかり、通常は3～5秒以上かかります
                    type = IAlexaPlayer.PlaybackErrorType.ERROR_SERVICE_UNAVAILABLE;
                    if (r != null) {
                        message = r.getString(R.string.alexa_playback_failed_time_out);
                    }
                    break;
                }
                default : {
                    // デフォルト
                    message = "";
                }
            }
            mCallback.onError(type, message);
        }
        return false;
    }

    /**
     * 情報通知
     * @param mp
     * @param what
     * @param extra
     * @return
     */
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {

        if (what == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
            // シークできない。（ライブストリームなど）
        }

        return false;
    }

    /**
     * バッファリング完了
     * @param mediaPlayer
     */
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        AmazonAlexaManager amazonAlexaManager = AmazonAlexaManager.getInstance();
        LogUtil.d(TAG, "onPrepared start");
        if (mTempPlayer != mediaPlayer){
            mediaPlayer.release();
            return;
        }
        mMediaPlayer = mediaPlayer;

        // V1.5.0問題点リストNo.1 Start
        // もう使わないのでnull
        mTempPlayer = null;
        // V1.5.0問題点リストNo.1 End

        boolean isPlay = false;

        if(mCallback != null && amazonAlexaManager.isWebLinkConnection()) {
            isPlay = mCallback.onPrepared(); 
        }
        if (isPlay && amazonAlexaManager.isWebLinkConnection()) {
            // Load中にバックに行くと再生開始してしまうので、バック中は再生させない。
            doPlay();
        }
        else if (!amazonAlexaManager.isWebLinkConnection()){
            stopPlayer();
        }
    }

    /**
     * 再生開始
     */
    private void doPlay() {
        if (mPlaybackState != IAlexaPlayer.PlaybackState.FINISHED) {
            long offsetInMilliseconds = 0;
            if (mCallback != null) {
                offsetInMilliseconds = mCallback.onPlaybackReady();
            }

            mPlaybackState = IAlexaPlayer.PlaybackState.PLAYING;

            //Mute中に新しくPlayDirectiveがきたらMute解除するようになってゐる
            if(isMute()) {
                setMuteCallback(mIsMute);
            }
            if (offsetInMilliseconds > 0) {
                mMediaPlayer.seekTo((int) offsetInMilliseconds);
            }
            mMediaPlayer.start();
            this.mIsForcedStopping = false;

            if (mCallback != null) {
                mCallback.onPlaybackStarted();
            }
        }
    }

    /**
     * バッファ済みデータ量
     */
    private int mBufferPercentage = 0;

    /**
     * バッファ状態通知
     * @param mediaPlayer
     * @param percentage
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percentage) {
        LogUtil.d(TAG, "onBufferingUpdate:"+percentage);
        mBufferPercentage = percentage;

        if(percentage >= 100){
            if(mMediaPlayer != null){
                mMediaPlayer.setOnBufferingUpdateListener(null);
            }
            if(mCallback != null){
                mCallback.onBufferedFinish();
            }
        }
    }


    /* Speechの再生位置を最初に戻す
    *  Speech以外では使っちゃだめよ
    **/
    public synchronized void resetSpeech() {
        if(mMediaPlayer != null){
           //TODO:ここで戻すのやめる
            //mMediaPlayer.seekTo(0);
            if(mCallback != null){
               mCallback.onPaused();
            }
         }
    }
    /*
    * 再生を開始
    * speak専用
    * */
    public synchronized boolean speechStart() {
        if (this.mIsForcedStopping != true) {
            if(mMediaPlayer != null && !mMediaPlayer.isPlaying()){
                mMediaPlayer.seekTo(0);
                mMediaPlayer.start();
                mPlaybackState = IAlexaPlayer.PlaybackState.PLAYING;

                if(mCallback != null){
                    mCallback.onResumed();
                }
                return true;
            }
        }
        return false;
    }
    /**
     * 再生を開始
     * @return true:再生開始成功　false:再生開始失敗
     */
    public synchronized boolean start() {
        if (DBG) android.util.Log.d(TAG, "start(), mIsForcedStopping = " + mIsForcedStopping);

        if (this.mIsForcedStopping != true) {
            if(mMediaPlayer != null && !mMediaPlayer.isPlaying()){
                mMediaPlayer.start();
                mPlaybackState = IAlexaPlayer.PlaybackState.PLAYING;

                if(mCallback != null){
                    mCallback.onResumed();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 一時停止
     * @return true:一時停止成功　false:一時停止失敗
     */
    public synchronized boolean pause() {
        //if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
        if(mMediaPlayer != null ){
            mMediaPlayer.pause();
            mPlaybackState = IAlexaPlayer.PlaybackState.PAUSE;

            if(mCallback != null){
                mCallback.onPaused();
            }
            return true;
        }
        return false;
    }

    /**
     * 再生時間取得
     * @return
     */
    public synchronized int getDuration() {
        if(mMediaPlayer != null){
            int tempDuration = mMediaPlayer.getDuration();
            if(tempDuration < 0){
                tempDuration = 0;
            }
            return tempDuration;
        } else {
            return 0;
        }
    }

    /**
     * 再生位置取得
     * @return
     */
    public synchronized int getCurrentPosition() {
            if (mMediaPlayer != null) {
                return mMediaPlayer.getCurrentPosition();
            } else {
                return 0;
            }
    }

    /**
     * 再生再開ブロックフラグ
     * @return  true:再生再開させない
     */
    public boolean isForcedStopping() {
        return this.mIsForcedStopping;
    }

    /**
     * MediaPlayerを一時停止状態で保持するが、再生再開を行わせないフラグを設定
     * @param isForcedStopping true:再生再開をさせない
     */
    public void setForcedStopping(boolean isForcedStopping) {
        this.mIsForcedStopping = isForcedStopping;
    }

    /**
     * 再生状態取得
     * @return
     */
    public IAlexaPlayer.PlaybackState getPlaybackState() {
        return mPlaybackState;
    }

    /**
     * 再生状態取得
     * @return
     */
    @WLAudioStreamEx.WLPLAYSTATE
    public int getWLPlaybackState() {
        return WLAudioStreamEx.WL_PLAYSTATE_STOPPED;
    }

    /**
     * 再生有無取得
     * @return
     */
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

}
