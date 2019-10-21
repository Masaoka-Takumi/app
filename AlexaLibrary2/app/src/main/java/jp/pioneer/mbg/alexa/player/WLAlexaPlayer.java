package jp.pioneer.mbg.alexa.player;

import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.abaltatech.weblink.core.audioconfig.EAudioCodec;
import com.abaltatech.wlmediamanager.EAudioFocusState;
import com.abaltatech.wlmediamanager.interfaces.WLAudioFormat;
import com.google.android.exoplayer2.audio.WLAudioStreamEx;
import com.google.android.exoplayer2.audio.WeblinkAudioSink;
import com.google.android.exoplayer2.extractor.Extractor;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;

import jp.pioneer.mbg.alexa.AmazonAlexaManager;
import jp.pioneer.mbg.alexa.util.LogUtil;

/**
 * 音声再生用のプレーヤー
 */
public class WLAlexaPlayer implements IAlexaPlayer{

    private static final String TAG = WLAlexaPlayer.class.getSimpleName();
    private static final boolean DBG = true;
    private static final boolean ToastDBG = false;

    /** 再生完了を判定する為のマージン(単位はマイクロ秒). */
    private static final int MARGIN_US = 50 * 1000;

    private Thread mAudioStreamThread = null;
    private WLAudioStreamEx mAudioStream = null;
    private MediaExtractor mExtractor = null;
    private int mDuration = 0;

    private boolean mIsPlaying = false;
    private boolean mIsPaused = true;
    private boolean mIsFocusLoss = false;

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
     * 一時停止状態でMediaPlayerはリリースしないが、再生再開はさせない場合にtrue
     */
    private boolean mIsForcedStopping = false;

    /**
     * コンストラクター
     * @param context
     */
    public WLAlexaPlayer(Context context) {
        this(context, null);
    }

    /**
     * コンストラクター
     * @param context
     * @param callback
     */
    public WLAlexaPlayer(Context context, IAlexaPlayer.PlaybackCallback callback){
        mContext = context;
        mCallback = callback;
        setIsFocusLoss(false);
    }

    /**
     * コールバックインスタンス登録
     * @param callback
     */
    public void setCallback(IAlexaPlayer.PlaybackCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void release() {
        releasePlayer();
    }

    /**
     * MediaPlayer生成
     * @param path
     * @throws IOException
     */
    @Override
    public synchronized void createPlayer(String path, boolean isDoFocusWaiting) throws IOException {
        LogUtil.d(TAG, "createPlayer(String), path = " + path);
    }

    /**
     * MediaPlayer生成
     * @param fd
     * @throws IOException
     */
    @Override
    public synchronized void createPlayer(FileDescriptor fd, boolean isDoFocusWaiting) throws IOException {
        this.createPlayer(fd, 0, 0, isDoFocusWaiting);
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
        LogUtil.d(TAG, "createPlayer(FileDescriptor) start");
        // 破棄処理
        releasePlayer();

        MediaExtractor extractor = new MediaExtractor();
        if (length > 0) {
            extractor.setDataSource(fd, offset, length);
        } else {
            extractor.setDataSource(fd);
        }
        boolean hasAudio = false;
        String srcType = null;
        MediaFormat format = null;

        int numTracks = extractor.getTrackCount();

        for (int i = 0; !hasAudio && i < numTracks; ++i) {
            format = extractor.getTrackFormat(i);
            srcType = format.getString(MediaFormat.KEY_MIME);

            Log.d(TAG, "srcType: "+ srcType);

            if (srcType != null && srcType.indexOf("audio/") == 0) {
                extractor.selectTrack(i);
                hasAudio = true;
                break;
            }
        }

        Log.d(TAG, "create: hasAudio:" + hasAudio);

        if(hasAudio) {
            WLAudioStreamEx stream = null;
            EAudioCodec codec;

            switch (srcType) {
                case "audio/mpeg":
                    codec = EAudioCodec.AC_MP3;
                    break;
                case "audio/mp4a-latm":
                    codec = EAudioCodec.AC_AAC;
                    break;
                case "audio/raw":
                case "audio/pcm":
                    codec = EAudioCodec.AC_PCM; // 砂嵐音になる。AbaltaにQA中
                    break;
                default:
                    codec = EAudioCodec.AC_None;
                    break;
            }

            Log.d(TAG, " - createPlayer: format = " + format + ", codec = " + codec);

            stream = createAudioStream(format, codec, isDoFocusWaiting, mWLAudioFocusCallback);

            if (stream != null) {
                mAudioStream = stream;
                mExtractor = extractor;
                if (format.containsKey(MediaFormat.KEY_DURATION)) {
                    mDuration = (int) format.getLong(MediaFormat.KEY_DURATION) / 1000;
                }
                Log.d(TAG, String.format("create: duration: %d", mDuration));
                Log.d(TAG, "create: success");

                if(mCallback != null){
                    mCallback.onPrepare();
                }
                // TODO:再生開始のタイミングについて要検証
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        boolean isPlay = false;
                        AmazonAlexaManager amazonAlexaManager = AmazonAlexaManager.getInstance();
                        if(mCallback != null && amazonAlexaManager.isWebLinkConnection()) {
                            isPlay = mCallback.onPrepared();
                        }
                        if (isPlay) {
                            setIsPaused(false);
                            setIsFocusLoss(false);
                            doPlay();
                        }
                    }
                });
                return;
            }
        }
        if (extractor != null) {
            extractor.release();
        }
        // TODO:ここに来る場合はエラーとして処理する必要がある？
        LogUtil.d(TAG, "createPlayer(FileDescriptor) end");
    }

    private WLAudioStreamEx createAudioStream(MediaFormat format, EAudioCodec codec, boolean isDoFocusWaiting, WeblinkAudioSink.IWLAudioFocusCallback callback) {
        if (codec == EAudioCodec.AC_None) {
            return null;
        }
        int channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        int sampleRate   = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);

        WLAudioFormat audioFormat = WLAudioFormat.getDefaultWLAudioFormat();
        audioFormat.setAudioCodec(codec);
        audioFormat.setChannelCount(channelCount);
        audioFormat.setSampleRate(sampleRate);

        WLAudioStreamEx stream = WLAudioStreamEx.createStream(audioFormat, isDoFocusWaiting, callback);
        return stream;
    }


    /**
     * MediaPlayer停止
     */
    private void internalStopPlayer() {
        LogUtil.d(TAG, "internalStopPlayer start");
        if (mAudioStreamThread != null) {
            Log.d(TAG, "release: mAudioStreamThread");
            mAudioStreamThread.interrupt();
            mAudioStreamThread = null;
        }
        setIsPlaying(false);
    }

    /**
     * MediaPlayer停止
     */
    @Override
    public synchronized void stopPlayer() {
        internalStopPlayer();
    }

    /**
     * MediaPlayerインスタンス破棄
     */
    @Override
    public synchronized void releasePlayer() {
        if (mAudioStreamThread != null) {
            Log.d(TAG, "release: mAudioStreamThread");
            mAudioStreamThread.interrupt();
            mAudioStreamThread = null;
        }
        setIsPlaying(false);
    }

    @Override
    public void setVolume(float volume) {}

    /**
     * コールバックだけ送る
     * @param volume
     */
    @Override
    public void setVolumeCallback(float volume) {
        if (mCallback != null) {
            mCallback.onSetVolume(volume);
        }
    }

    @Override
    public void adjustVolume(float volume) {}

    /**
     * コールバックだけ送る
     * @param volume
     */
    @Override
    public void adjustVolumeCallback(float volume) {
        if (mCallback != null) {
            mCallback.onAdjustVolume(volume);
        }
    }

    /**
     * ミュート状態を設定する
     * @param isMute
     */
    @Override
    public void setMute(boolean isMute) {
        mIsMute = isMute;
    }

    /**
     * コールバックだけ送る
     * @param isMute
     */
    @Override
    public void setMuteCallback(boolean isMute) {
        if(mCallback != null){
            mCallback.onSetMute(isMute);
        }
    }

    @Override
    public void setPause() {}

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
    @Override
    public void setAttenuate(boolean attenuate) {
        if (attenuate) {
            mAttenuateRatio = 0.2f;
        } else {
            mAttenuateRatio = 1.0f;
        }
        updateVolume();
    }

    /**
     * MediaPlayerのボリュームを取得する
     * @return 0～100
     */
    @Override
    public float getVolume() {
        return mCurrentVolume * 100.0f;
    }

    /**
     * ミュート状態を取得する
     * @return
     */
    @Override
    public boolean isMute() {
        return mIsMute;
    }

    /**
     * 再生開始
     */
    private void doPlay() {
        // TODO:楽曲の途中から再生する場合、MediaExtractor#setDataSource()で指定する？

        final WLAudioStreamEx audioStream = mAudioStream;
        final MediaExtractor extractor = mExtractor;

        if(isMute()) {
            setMuteCallback(mIsMute);
        }
        if (mAudioStreamThread == null) {
            mAudioStreamThread = new Thread() {
                private Long prevPlaybackPositionUs = null;
                private Long prevCurrentTimeMillis = null;

                @Override
                public void run() {
                    boolean isComp = false;
                    try {
                        byte byteBuffer[] = null;
                        ByteBuffer readBuffer = ByteBuffer.allocate(64 * 1024);
                        int readBufferLen;
                        long readSampleTimeUs = 0;
                        long playbackPositionUs = 0;
                        while (!isInterrupted() && audioStream != null && extractor != null) {
                            if (mIsFocusLoss) {
                                break;
                            }
                            if (mIsPaused) {
                                audioStream.pause();
                                continue;
                            }

                            audioStream.play();

                            if (audioStream.getPlayState() != WLAudioStreamEx.WL_PLAYSTATE_PLAYING) {
                                continue;
                            }
                            if (audioStream.isFocusWaiting()) {
                                if (audioStream.isDoFocusWaiting()) {
                                Thread.sleep(1000);
                                continue;
                                } else {
                                    break;
                                }
                            }
                            readBufferLen = extractor.readSampleData(readBuffer, 0);

                            if (readBufferLen > 0) {
                                long tmpReadSampleTimeUs = extractor.getSampleTime();
                                if (tmpReadSampleTimeUs > 0) {
                                    readSampleTimeUs = tmpReadSampleTimeUs;
                                }
                                readBuffer.limit(readBufferLen);
                                readBuffer.position(0);

                                if (byteBuffer == null || byteBuffer.length < readBufferLen) {
                                    byteBuffer = new byte[readBufferLen];
                                }
                                readBuffer.get(byteBuffer, 0, readBufferLen);
                                readBuffer.clear();

                                audioStream.writeData(byteBuffer, 0, readBufferLen, readSampleTimeUs);
                                extractor.advance();
                            } else {
                                if (audioStream.getPlayState() == WLAudioStreamEx.WL_PLAYSTATE_STOPPED) {
                                    break;
                                }
                                playbackPositionUs = audioStream.getPlaybackPositionUs();
                                if (playbackPositionUs >= readSampleTimeUs) {
                                    Log.d(TAG, "正常終了(終端まで再生された)");
                                    isComp = true;
                                    break;
                                } else {
                                    if (isAbnormalOccurred(playbackPositionUs, readSampleTimeUs)) {
                                        Log.w(TAG, "強制終了");
                                        isComp = true;
                                        break;
                                    }
                                }
                                Thread.sleep(100);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } finally {
                        mAudioStream = null;
                        mExtractor = null;
                        audioStream.close();
                        extractor.release();
                        if (mCallback != null && isComp) {
                            mCallback.onCompletion();
                        }
                    }

                    mDuration = 0;
                }

                // 異常が発生したかどうかの判定
                // 例) Pixel3にて、playbackPositionが終端まで行かない（終端の数msec手前まで停止）という不具合があった
                private boolean isAbnormalOccurred(long currentPlaybackPositionUs, long readSampleTimeUs) {
                    long currentTimeMillis = System.currentTimeMillis();

                    // 初回
                    if (prevPlaybackPositionUs == null || prevCurrentTimeMillis == null) {
                        prevPlaybackPositionUs = currentPlaybackPositionUs;
                        prevCurrentTimeMillis = currentTimeMillis;
                        return false;
                    }
                    Log.v(TAG, String.format(
                            " readSampleTimeUs:       %d \n" +
                            " currplaybackPositionUs: %d \n" +
                            " prevPlaybackPositionUs: %d \n" +
                            " currTimeMillis:         %d \n" +
                            " prevCurrentTimeMillis:  %d ",
                            readSampleTimeUs, currentPlaybackPositionUs,
                            prevPlaybackPositionUs, currentTimeMillis, prevCurrentTimeMillis));

                    // 再生位置が変化しているかを判定
                    if (currentPlaybackPositionUs != prevPlaybackPositionUs) {
                        // 前回値を更新
                        prevPlaybackPositionUs = currentPlaybackPositionUs;
                        prevCurrentTimeMillis = currentTimeMillis;
                    } else {
                        if (currentTimeMillis - prevCurrentTimeMillis >= 200) {
                            // 再生位置が変化しないまま、一定時間が経過
                            Log.w(TAG, "異常発生");
                            return true;
                        } else {
                            // 再生位置は変化してないが、もう暫く様子を見る
                        }
                    }
                    return false;
                }
            };
            mAudioStreamThread.start();
            this.mIsForcedStopping = false;

            // 再生開始をコールバック
            if (mCallback != null) {
                mCallback.onPlaybackStarted();
            }
            setIsPlaying(true);
        } else {
            if (!mIsPlaying) {
                setIsPlaying(true);
                setIsPaused(false);
            }
        }
    }

    /**
     *  Speechの再生位置を最初に戻す(未使用)
     */
    @Override
    public synchronized void resetSpeech() {
        if(mCallback != null){
            mCallback.onPaused();
        }
    }

    /**
     * 再生を開始
     * @return true:再生開始成功　false:再生開始失敗
     */
    @Override
    public synchronized boolean start() {
        if (DBG) android.util.Log.d(TAG, "start(): mIsForcedStopping = " + mIsForcedStopping);
        if (this.mIsForcedStopping != true && !mIsPlaying) {
            setIsPaused(false);
            setIsFocusLoss(false);
            doPlay();
            if(mCallback != null){
                mCallback.onResumed();
            }
            return true;
        }
        return false;
    }

    /**
     * 一時停止
     * @return true:一時停止成功　false:一時停止失敗
     */
    @Override
    public synchronized boolean pause() {
        if (DBG) android.util.Log.d(TAG, "pause()");
        if (mAudioStreamThread != null && mIsPlaying) {
            setIsPlaying(false);
            setIsPaused(true);
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
    @Override
    public synchronized int getDuration() {
        return mDuration;
    }

    /**
     * 再生位置取得
     * @return
     */
    @Override
    public synchronized int getCurrentPosition() {
        if (mAudioStream != null && mAudioStream.getPlayState() != WLAudioStreamEx.WL_PLAYSTATE_STOPPED) {
            return (int) mAudioStream.getPlaybackPositionUs() / 1000;
        } else {
            return 0;
        }
    }

    /**
     * 再生再開ブロックフラグ(未使用)
     * @return  true:再生再開させない
     */
    @Override
    public boolean isForcedStopping() {
        return this.mIsForcedStopping;
    }

    /**
     * MediaPlayerを一時停止状態で保持するが、再生再開を行わせないフラグを設定
     * @param isForcedStopping true:再生再開をさせない
     */
    @Override
    public void setForcedStopping(boolean isForcedStopping) {
        this.mIsForcedStopping = isForcedStopping;
    }

    /**
     * 再生状態取得(WLAlexaPlayerでは未使用)
     * @return
     */
    @Override
    public IAlexaPlayer.PlaybackState getPlaybackState() {
        return PlaybackState.STOP;
    }

    /**
     * 再生状態取得(WLAlexaPlayer用)
     * @return
     */
    @Override
    @WLAudioStreamEx.WLPLAYSTATE
    public int getWLPlaybackState() {
        return mAudioStream.getPlayState();
    }

    /**
     * 再生有無取得
     * @return
     */
    @Override
    public boolean isPlaying() {
        return mIsPlaying;
    }

    private final WeblinkAudioSink.IWLAudioFocusCallback mWLAudioFocusCallback = new WeblinkAudioSink.IWLAudioFocusCallback() {
        @Override
        public void onWLAudioFocusLoss(final EAudioFocusState state) {
            setIsPlaying(false);
            setIsFocusLoss(true);
            if (mCallback != null) {
                mCallback.onWLAudioFocusLoss();
            }
        }
    };

    private void setIsPlaying(boolean isPlaying) {
        Log.d(TAG, "setIsPlaying(): "+ isPlaying);
        mIsPlaying = isPlaying;
    }

    private void setIsPaused(boolean isPaused) {
        Log.d(TAG, "setIsPaused(): "+ isPaused);
        mIsPaused = isPaused;
    }

    private void setIsFocusLoss(boolean isFocusLoss) {
        Log.d(TAG, "setIsFocusLoss(): "+ isFocusLoss);
        mIsFocusLoss = isFocusLoss;
    }

}