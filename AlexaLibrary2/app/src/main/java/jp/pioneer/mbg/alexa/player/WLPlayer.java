package jp.pioneer.mbg.alexa.player;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.net.rtp.AudioStream;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.util.Log;

import com.abaltatech.weblink.core.audioconfig.EAudioCodec;
import com.abaltatech.wlmediamanager.EAudioFocusState;
import com.abaltatech.wlmediamanager.interfaces.WLAudioFormat;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;

import jp.pioneer.mbg.alexa.manager.AlexaSpeakManager;

import com.google.android.exoplayer2.audio.WLAudioStreamEx;
import com.google.android.exoplayer2.audio.WeblinkAudioSink;

/**
 * MediaPlayer相当のWeblinkAudio版プレーヤー.
 */
public class WLPlayer {
    private static final String TAG = WLPlayer.class.getSimpleName();
    private static final boolean DBG = true;

    /**
     * コールバック用ハンドラー
     */
    private Handler mHandler = null;

//    private WLAudioStreamEx mAudioStream = null;
    private Thread mAudioStreamThread = null;
//    private MediaExtractor mExtractor = null;

    private boolean mIsPaused;
    private boolean mIsReleased;

    private int mDuration = 0;

    private IOnPreparedListener mOnPreparedListener = null;
    private IOnCompletionListener mOnCompletionListener = null;

    // 自身を保持
    private WLPlayer mPlayer = null;

    /**
     * バッファリング完了通知リスナー
     */
    public interface IOnPreparedListener {
        public void onPrepared(WLPlayer player);
    }

    /**
     * 再生完了通知リスナー
     */
    public interface IOnCompletionListener {
        public void onCompletion(WLPlayer player);
    }

    /**
     * コンストラクタ
     */
    public WLPlayer() {
        if (DBG) android.util.Log.d(TAG, "WLPlayer()");
        mPlayer = this;
        mIsPaused = true;
        mIsReleased = false;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void setOnPreparedListener(IOnPreparedListener listener) {
        if (DBG) android.util.Log.d(TAG, "setOnPreparedListener(): listener = " + listener);
        this.mOnPreparedListener = listener;
    }

    public void setOnCompletionListener(IOnCompletionListener listener) {
        if (DBG) android.util.Log.d(TAG, "setOnCompletionListener(): listener = " + listener);
        this.mOnCompletionListener = listener;
    }

    /**
     * プレーヤーの生成（リソースファイル再生用）
     * @param context
     * @param resourceId
     * @return
     */
    public static WLPlayer create(@NonNull Context context, @RawRes int resourceId, boolean isDoFocusWaiting) {
        if (DBG) android.util.Log.d(TAG, "create(): context = " + context + ", resourceId = " + resourceId);
        WLPlayer player = new WLPlayer();
        AssetFileDescriptor assetFileDescriptor = context.getResources().openRawResourceFd(resourceId);
        try {
            player.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength(), isDoFocusWaiting);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return player;
    }

    /**
     * DataSource設定（リソースファイル再生用）
     * @param context
     * @param resourceId
     * @throws IOException
     */
    public void setDataSource(@NonNull Context context, @RawRes int resourceId, boolean isDoFocusWaiting) throws IOException {
        if (DBG) android.util.Log.d(TAG, "setDataSource(context, resourceId): context = " + context + ", resourceId = " + resourceId);
        AssetFileDescriptor assetFileDescriptor = context.getResources().openRawResourceFd(resourceId);
        setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength(), isDoFocusWaiting);
    }

    /**
     * DataSource設定（バイナリ再生用）
     * @param fd
     * @throws IOException
     */
    public void setDataSource(FileDescriptor fd, boolean isDoFocusWaiting) throws IOException  {
        if (DBG) android.util.Log.d(TAG, "setDataSource(fd): fd = " + fd);
        this.setDataSource(fd, 0, 0, isDoFocusWaiting);
    }

    /**
     * DataSource設定（バイナリ再生用）
     * @param fd
     * @param offset
     * @param length
     * @throws IOException
     */
    public void setDataSource(FileDescriptor fd, long offset, long length, boolean isDoFocusWaiting) throws IOException {
        if (DBG) android.util.Log.d(TAG, "setDataSource(fd, int, int): fd = " + fd + ", offset = " + offset + ", length = " + length);
        MediaExtractor extractor = new MediaExtractor();
        if (length > 0) {
            extractor.setDataSource(fd, offset, length);
        } else {
            extractor.setDataSource(fd);
        }
        this.setDataSource(extractor, isDoFocusWaiting);
    }

    /**
     * DataSource設定（ネットワーク上のファイル再生用）
     * @param context
     * @param url
     * @throws IOException
     */
    public void setDataSource(Context context, String url, boolean isDoFocusWaiting) throws IOException {
        if (DBG) android.util.Log.d(TAG, "setDataSource(context, url): context = " + context + ", url = " + url);
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(context, Uri.parse(url), null);
        setDataSource(extractor, isDoFocusWaiting);
    }

    /**
     * DataSource設定
     * @param extractor
     * @throws IOException
     */
    public void setDataSource(final MediaExtractor extractor, boolean isDoFocusWaiting) throws IOException {
        if (DBG) android.util.Log.d(TAG, "setDataSource(extractor): extractor = " + extractor);
        boolean hasAudio = false;
        String srcType = null;
        MediaFormat format = null;
        int numTracks = extractor.getTrackCount();

        for (int i = 0; !hasAudio && i < numTracks; ++i) {
            format    = extractor.getTrackFormat(i);
            srcType   = format.getString(MediaFormat.KEY_MIME);
            if (srcType != null && srcType.indexOf("audio/") == 0) {
                extractor.selectTrack(i);
                hasAudio = true;
                break;
            }
        }
        if(hasAudio) {
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
            if (DBG) android.util.Log.d(TAG, " - setDataSource(extractor): format = " + format + ", codec = " + codec);

            final WLAudioStreamEx stream = createAudioStream(format, codec, isDoFocusWaiting, mWLAudioFocusCallback);

            if (stream != null) {
//                mAudioStream = stream;
//                WLAudioStreamEx stream =
//                mExtractor = extractor;
                if (format.containsKey(MediaFormat.KEY_DURATION)) {
                    mDuration = (int) format.getLong(MediaFormat.KEY_DURATION) / 1000;
                }
                if (DBG) android.util.Log.d(TAG, " - setDataSource(extractor): mDuration = " + mDuration);

                mAudioStreamThread = new Thread() {
                    private Long prevPlaybackPositionUs = null;
                    private Long prevCurrentTimeMillis = null;

                    @Override
                    public void run() {
                        if (DBG) android.util.Log.d(TAG, "AudioStreamThread#run()");
                        try {
                            byte byteBuffer[] = null;
                            ByteBuffer readBuffer = ByteBuffer.allocate(64 * 1024);
                            int readBufferLen;
                            long readSampleTimeUs = 0;
                            long playbackPositionUs = 0;

                            while (!isInterrupted() && stream != null && extractor != null) {
                                if(mIsReleased) {
                                    break;
                                }
                                if (mIsPaused) {
                                    stream.pause();
                                    continue;
                                }
                                stream.play();

                                if(stream.getPlayState() != WLAudioStreamEx.WL_PLAYSTATE_PLAYING) {
                                    continue;
                                }
                                if (stream.isFocusWaiting()) {
                                    if (stream.isDoFocusWaiting()) {
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

                                    stream.writeData(byteBuffer, 0, readBufferLen, readSampleTimeUs);
                                    extractor.advance();
                                } else {
                                    if (stream.getPlayState() == WLAudioStreamEx.WL_PLAYSTATE_STOPPED) {
                                        break;
                                    }
                                    playbackPositionUs = stream.getPlaybackPositionUs();
                                    if (playbackPositionUs >= readSampleTimeUs) {
                                        Log.d(TAG, "正常終了(終端まで再生された)");
                                        break;
                                    } else {
                                        if (isAbnormalOccurred(playbackPositionUs, readSampleTimeUs)) {
                                            Log.w(TAG, "強制終了");
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
//                            WLAudioStreamEx stream = mAudioStream;
//                            MediaExtractor extractor = extractor;
//                            mAudioStream = null;
//                            mExtractor = null;
                            stream.close();
                            extractor.release();

                            if (mOnCompletionListener != null) {
                                mOnCompletionListener.onCompletion(mPlayer);
                            }
                        }
                        mDuration = 0;
                    }

                    //異常が発生したかどうか
                    // 例) Pixel3にて、playbackPositionが終端まで行かない（終端の数msec手前まで停止）、という現状があった
                    private boolean isAbnormalOccurred (long currentPlaybackPositionUs, long readSampleTimeUs) {
                        long currentTimeMillis = System.currentTimeMillis();

                        // 初回
                        if (prevPlaybackPositionUs == null || prevCurrentTimeMillis == null) {
                            prevPlaybackPositionUs = currentPlaybackPositionUs;
                            prevCurrentTimeMillis = currentTimeMillis;
                            return false;
                        }

                        Log.v(TAG,String.format(
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
                                Log.w(TAG,"異常発生");
                                return true;
                            } else {
                                // 再生位置は変化してないが、もう暫く様子を見る
                            }
                        }
                        return false;
                    }
                };

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mOnPreparedListener != null) {
                            mOnPreparedListener.onPrepared(mPlayer);
                        }
                    }
                });
            } else {
                if (DBG) android.util.Log.d(TAG, " - setDataSource(extractor): stream is NULL");
                if (extractor != null) {
                    extractor.release();
                }
            }
        }
    }

    /**
     * 再生開始
     */
    public synchronized void start() {
        if (DBG) android.util.Log.d(TAG, "start()");
        if (mAudioStreamThread != null) {
            mIsPaused = false;
            if (mAudioStreamThread.getState() == Thread.State.NEW) {
                try {
                    mAudioStreamThread.start();
                } catch (RuntimeException e) {
                    // 既にstart()実行している場合など
                }
            }
        }
    }

    /**
     * 一時停止
     */
    public synchronized void pause() {
        if (DBG) android.util.Log.d(TAG, "pause()");
        if (mAudioStreamThread != null) {
            mIsPaused = true;
        }
    }

    /**
     * 再生停止
     */
    public synchronized void stop() {
        if (DBG) android.util.Log.d(TAG, "stop()");
        if (mAudioStreamThread != null) {
            mAudioStreamThread.interrupt();
            mAudioStreamThread = null;
        }
    }

    /**
     * リリース
     */
    public synchronized void release() {
        if (DBG) android.util.Log.d(TAG, "release()");
        if (mAudioStreamThread != null) {
            mAudioStreamThread.interrupt();
            mAudioStreamThread = null;
        }
        mPlayer = null;
        mIsReleased = true;
    }

    /**
     * AudioStream生成
     * @param format
     * @param codec
     * @param callback
     * @return
     */
    private WLAudioStreamEx createAudioStream(MediaFormat format, EAudioCodec codec, boolean isDoFocusWaiting, WeblinkAudioSink.IWLAudioFocusCallback callback) {
        if (DBG) android.util.Log.d(TAG, "createAudioStreamEx(): format = " + format + ", codec = " + codec);
        if (codec == EAudioCodec.AC_None) {
            if (DBG) android.util.Log.d(TAG, " - createAudioStreamEx(): Return NULL");
            return null;
        }
        int channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        int sampleRate   = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);

        WLAudioFormat audioFormat = WLAudioFormat.getDefaultWLAudioFormat();

        audioFormat.setAudioCodec(codec);
        audioFormat.setChannelCount(channelCount);
        audioFormat.setSampleRate(sampleRate);

        WLAudioStreamEx stream = WLAudioStreamEx.createStream(audioFormat, isDoFocusWaiting, callback);

        if (DBG) android.util.Log.d(TAG, " - createAudioStream(): Return " + stream);
        return stream;
    }

    private final WeblinkAudioSink.IWLAudioFocusCallback mWLAudioFocusCallback = new WeblinkAudioSink.IWLAudioFocusCallback() {
        @Override
        public void onWLAudioFocusLoss(final EAudioFocusState state) {
            AlexaSpeakManager.getInstance().onWLAudioFocusLoss();
        }
    };

}