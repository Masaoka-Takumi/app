package jp.pioneer.mbg.alexa.player;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.WLAudioStreamEx;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.offline.FilteringManifestParser;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.source.dash.manifest.RepresentationKey;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.HlsPlaylistParser;
import com.google.android.exoplayer2.source.hls.playlist.RenditionKey;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.manifest.SsManifestParser;
import com.google.android.exoplayer2.source.smoothstreaming.manifest.StreamKey;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.RandomTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import jp.pioneer.mbg.alexa.AmazonAlexaManager;
import jp.pioneer.mbg.alexa.manager.AlexaAudioManager;
import jp.pioneer.mbg.alexa.util.LogUtil;
import jp.pioneer.mbg.android.vozsis.R;

/**
 * 音楽再生用プレーヤー（WebLinkAudio対応）
 */
public class ExoAlexaPlayer implements IAlexaPlayer {

    private static final String TAG = ExoAlexaPlayer.class.getSimpleName();
    private static final boolean DBG = true;
    private static final boolean ToastDBG = false;

    private LinearLayout debugRootView;
    private TextView debugTextView;

    private DataSource.Factory mediaDataSourceFactory;

    private FrameworkMediaDrm mediaDrm;
    private MediaSource mediaSource;
    private DefaultTrackSelector trackSelector;
    private DefaultTrackSelector.Parameters trackSelectorParameters;
    private TrackGroupArray lastSeenTrackGroupArray;
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    /**
     * 再生処理開始後、最初にSTATE_READになったかどうかを判定するフラグ
     */
    private boolean isPrepared = false;

    private SimpleExoPlayer mMediaPlayer = null;
    /**
     * ExoPlayerにアクセスする際に使用するハンドラー
     */
    private Handler mHandler = null;

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
    public ExoAlexaPlayer(Context context, View view) {
        this(context, null, view);
    }

    /**
     * コンストラクター
     * @param context
     * @param callback
     * @param view
     */
    public ExoAlexaPlayer(Context context, IAlexaPlayer.PlaybackCallback callback, View view){
        this.mContext = context;
        this.mCallback = callback;

        init();
    }

    /**
     * 初期化処理
     */
    private void init(){
        trackSelectorParameters = new DefaultTrackSelector.ParametersBuilder().build();
        mediaDataSourceFactory = buildDataSourceFactory(true);
    }

    /**
     * コールバックインスタンス登録
     * @param callback
     */
    public void setCallback(IAlexaPlayer.PlaybackCallback callback){
        this.mCallback = callback;
    }

    /**
     * ExoAlexaPlayerの破棄処理
     */
    public void release() {
        // ExoPlayerの破棄
        this.releasePlayer();

        if (downloadCache != null) {
            // キャッシュファイルのクローズ処理
            try {
                downloadCache.release();
            } catch (Cache.CacheException e) {
                e.printStackTrace();
            }
            finally {
                downloadCache = null;
            }
        }
    }

    /**
     * MediaPlayer初期化
     * @return
     */
    private SimpleExoPlayer initPlayer() {
        if (mMediaPlayer == null) {
            boolean preferExtensionDecoders = false;

            @DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode = useExtensionRenderers()
                    ? (preferExtensionDecoders ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                    : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                    : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
            DefaultRenderersFactory renderersFactory =
                    new DefaultRenderersFactory(mContext, extensionRendererMode);


            TrackSelection.Factory trackSelectionFactory = new RandomTrackSelection.Factory();;
            trackSelector = new DefaultTrackSelector(trackSelectionFactory);
            trackSelector.setParameters(trackSelectorParameters);
            lastSeenTrackGroupArray = null;

            Looper myLooper = Looper.myLooper();
            if (myLooper == null) {
                android.util.Log.w(TAG, "[Fail-Safe] UnExpected Pass");
                if (false/*! BuildConfig.IS_GOOGLE_STORE_RELEASE*/) {
                    throw new RuntimeException();
                }

                myLooper = Looper.getMainLooper();
            }
            mHandler = new Handler(myLooper);

            DefaultDrmSessionManager<FrameworkMediaCrypto>  drmSessionManager = null;
            mMediaPlayer = ExoPlayerFactory.newSimpleInstance("", renderersFactory, trackSelector, drmSessionManager);
            mMediaPlayer.addListener(new PlayerEventListener());
            mMediaPlayer.addAnalyticsListener(new EventLogger(trackSelector));

            mPlaybackState = IAlexaPlayer.PlaybackState.STOP;
        }
        return mMediaPlayer;
    }

    public boolean useExtensionRenderers() {
        return "withExtensions".equals("noExtensions");
    }

    private DefaultDrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManagerV18(
            UUID uuid, String licenseUrl, String[] keyRequestPropertiesArray, boolean multiSession)
            throws UnsupportedDrmException {
        HttpDataSource.Factory licenseDataSourceFactory = buildHttpDataSourceFactory(/* listener= */ null);
        HttpMediaDrmCallback drmCallback =
                new HttpMediaDrmCallback(licenseUrl, licenseDataSourceFactory);
        if (keyRequestPropertiesArray != null) {
            for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
                drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i],
                        keyRequestPropertiesArray[i + 1]);
            }
        }
        releaseMediaDrm();
        mediaDrm = FrameworkMediaDrm.newInstance(uuid);
        return new DefaultDrmSessionManager<>(uuid, mediaDrm, drmCallback, null, multiSession);
    }

    /** Returns a {@link HttpDataSource.Factory}. */
    public HttpDataSource.Factory buildHttpDataSourceFactory(
            TransferListener<? super DataSource> listener) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(mContext, "ExoPlayerDemo"), listener);
    }

    private void releaseMediaDrm() {
        if (mediaDrm != null) {
            mediaDrm.release();
            mediaDrm = null;
        }
    }
    private class PlayerEventListener implements Player.EventListener {

        /**
         * TimelineかManifestが更新されたときにコールされる(よく分からん)
         * @param timeline The latest timeline. Never null, but may be empty.
         * @param manifest The latest manifest. May be null.
         * @param reason The {@link Player.TimelineChangeReason} responsible for this timeline change.
         */
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
            android.util.Log.d("TEST_20181026", "POINT_101, onTimelineChanged()");
        }

        /**
         * トラックが切り替わったときにコールバックされる
         *
         * @param trackGroups The available tracks. Never null, but may be of length zero.
         * @param trackSelections The track selections for each renderer. Never null and always of
         *     length {@link Player#getRendererCount()}, but may contain null elements.
         */
        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            android.util.Log.d("TEST_20181026", "POINT_102, onTracksChanged()");
        }

        /**
         * ローカルファイルはHLSの取得状態が変化したときにコールバックされる
         * @param isLoading Whether the source is currently being loaded.
         */
        @Override
        public void onLoadingChanged(boolean isLoading) {
            android.util.Log.d("TEST_20181026", "POINT_103, onLoadingChanged()");
        }

        /**
         * ExoPlayerのステータスが変化したときにコールバックされる
         * @param playWhenReady Whether playback will proceed when ready.
         * @param playbackState One of the {@code STATE} constants.
         */
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
           switch (playbackState) {
                case Player.STATE_BUFFERING : {
                    // バッファリング中
                    break;
                }
                case Player.STATE_ENDED : {
                    // 最後まで再生終了
                    mPlaybackState = IAlexaPlayer.PlaybackState.FINISHED;
                    if(mCallback != null){
                        mCallback.onCompletion();
                    }
                    break;
                }
                case Player.STATE_IDLE : {
                    // MediaSourceを持っていない（MediaSourceはprepareメソッドで設定するので...）
                    break;
                }
                case Player.STATE_READY : {
                    // 再生可能な状態
                    if (!isPrepared && playWhenReady) {
                        // 再生処理開始後、最初に再生可能状態になった時のみ処理を行う
                        isPrepared = true;
                        if (mCallback != null) {
                            LogUtil.d(TAG, "onPrepared start");
                            boolean isPlay = false;
                            AmazonAlexaManager amazonAlexaManager = AmazonAlexaManager.getInstance();
                            if(amazonAlexaManager.isWebLinkConnection()) {
                                isPlay = mCallback.onPrepared();
                            }
                            if (isPlay && amazonAlexaManager.isWebLinkConnection()) {
                                // 車載機接続中であれば再生する
                                doPlay();
                                mCallback.onPlaybackStarted();
                            } else if(!amazonAlexaManager.isWebLinkConnection()) {
                                // 再生準備が完了しても再生させない
                                mMediaPlayer.setPlayWhenReady(false);
                                mPlaybackState = IAlexaPlayer.PlaybackState.STOP;
                            }
                        }
                    }
                    break;
                }
                default: {
                    break;
                }
            }
        }

        /**
         *  {@link Player.RepeatMode } の値が変化したときにコールバックする
         *
         * @param repeatMode The {@link Player.RepeatMode} used for playback.
         */
        @Override
        public void onRepeatModeChanged(int repeatMode) {
            android.util.Log.d("TEST_20181026", "POINT_105, onRepeatModeChanged()");
        }

        /**
         * {@link Player#getShuffleModeEnabled()} の値が変化した時にコールバックする
         *
         * @param shuffleModeEnabled Whether shuffling of windows is enabled.
         */
        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            android.util.Log.d("TEST_20181026", "POINT_106, onShuffleModeEnabledChanged()");
        }

        /**
         * エラー発生時にコールバックされる
         * @param error The error.
         */
        @Override
        public void onPlayerError(ExoPlaybackException error) {
            android.util.Log.d("TEST_20181026", "POINT_107, onPlayerError()");

            releasePlayer();

            if (error.getCause() != null) {
                String cause = error.getCause().getMessage();
                if (cause.contains(WLAudioStreamEx.EXCEPTION_CAUSE_IS_FOCUSLOST_BY_OUTSIDE)) {
                    AlexaAudioManager.getInstance().onWLAudioFocusLoss();
                    return;
                }
            }

            if(mCallback != null){
                IAlexaPlayer.PlaybackErrorType type = IAlexaPlayer.PlaybackErrorType.ERROR_UNKNOWN;
                String message = null;
                Resources r = mContext.getResources();

                switch (error.type) {
                    case ExoPlaybackException.TYPE_SOURCE : {
                        // MediaSourceでエラーが発生
                        IOException ioException = error.getSourceException();

                        // TODO:暫定
                        type = IAlexaPlayer.PlaybackErrorType.ERROR_INVALID_REQUEST;
                        if (r != null) {
                            message = r.getString(R.string.alexa_playback_failed_network_error);
                        }
                        break;
                    }
                    case ExoPlaybackException.TYPE_RENDERER : {
                        // Rendererでエラーが発生
                        Exception exception = error.getRendererException();
                        // TODO:暫定
                        type = IAlexaPlayer.PlaybackErrorType.ERROR_INVALID_REQUEST;
                        if (r != null) {
                            message = r.getString(R.string.alexa_playback_failed_network_error);
                        }
                        break;
                    }
                    case ExoPlaybackException.TYPE_UNEXPECTED : {
                        // 予期しないエラーが発生
                        RuntimeException runtimeException = error.getUnexpectedException();
                        // TODO:暫定
                        type = IAlexaPlayer.PlaybackErrorType.ERROR_INVALID_REQUEST;
                        if (r != null) {
                            message = r.getString(R.string.alexa_playback_failed_network_error);
                        }
                        break;
                    }
                    default: {
                        break;
                    }
                }

                mCallback.onError(type, message);
            }
        }

        /**
         * 再生位置が不連続になるとコールバックされる
         * (エラーではなく、シークバーの操作やFF／RWの操作でコールバックされるらしい)
         * @param reason The {@link Player.DiscontinuityReason} responsible for the discontinuity.
         */
        @Override
        public void onPositionDiscontinuity(int reason) {
            android.util.Log.d("TEST_20181026", "POINT_108, onPositionDiscontinuity()");
        }

        /**
         * Called when the current playback parameters change. The playback parameters may change due to
         * a call to {@link Player#setPlaybackParameters(PlaybackParameters)}, or the player itself may change
         * them (for example, if audio playback switches to passthrough mode, where speed adjustment is
         * no longer possible).
         *
         * @param playbackParameters The playback parameters.
         */
        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            android.util.Log.d("TEST_20181026", "POINT_109, onPlaybackParametersChanged()");
        }

        /**
         * Called when all pending seek requests have been processed by the player. This is guaranteed
         * to happen after any necessary changes to the player state were reported to
         * {@link #onPlayerStateChanged(boolean, int)}.
         */
        @Override
        public void onSeekProcessed() {
            android.util.Log.d("TEST_20181026", "POINT_110, onSeekProcessed()");
        }
    }

    /**
     * MediaPlayer生成
     * @param url
     * @throws IOException
     */
    public synchronized void createPlayer(String url, boolean isDoFocusWaiting) throws IOException {
        LogUtil.d(TAG, "createPlayer(String) start");
        LogUtil.d(TAG, " - createPlayer(String), url = " + url);
        stopPlayer();
        initPlayer();

        if(URLUtil.isValidUrl(url) && mContext != null){
            // 楽曲が変わるのでフラグを初期化
            isPrepared = false;

            String overrideExtension = "";
            mediaSource = buildMediaSource(Uri.parse(url), overrideExtension);
            // TODO:ここをFalseにすると再生開始後、すぐに終わることがある。
            // TODO:再生位置などを覚えている？
            boolean haveStartPosition = true;
            mMediaPlayer.prepare(mediaSource, haveStartPosition, false);
            mMediaPlayer.setPlayWhenReady(true);

            AmazonAlexaManager amazonAlexaManager = AmazonAlexaManager.getInstance();
            LogUtil.d(TAG, " - createPlayer(String), isWebLinkConnection = " + amazonAlexaManager.isWebLinkConnection());
            mPlaybackState = IAlexaPlayer.PlaybackState.PREPARE;
            long offsetInMilliseconds = 0;
            if(mCallback != null){
                offsetInMilliseconds = mCallback.onPrepare();
            }

            final SimpleExoPlayer player = mMediaPlayer;
            final long offset = offsetInMilliseconds;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (offset > 0) {
                        player.seekTo((int) offset);
                    }
                }
            });
        }
        else {
            throw new IOException();
        }
        LogUtil.d(TAG, "createPlayer(String) end");
    }

    @SuppressWarnings("unchecked")
    private MediaSource buildMediaSource(Uri uri, @Nullable String overrideExtension) {
        @C.ContentType int type = Util.inferContentType(uri, overrideExtension);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false))
                        .setManifestParser(
                                new FilteringManifestParser<>(
                                        new DashManifestParser(), (List<RepresentationKey>) getOfflineStreamKeys(uri)))
                        .createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        buildDataSourceFactory(false))
                        .setManifestParser(
                                new FilteringManifestParser<>(
                                        new SsManifestParser(), (List<StreamKey>) getOfflineStreamKeys(uri)))
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory)
                        .setPlaylistParser(
                                new FilteringManifestParser<>(
                                        new HlsPlaylistParser(), (List<RenditionKey>) getOfflineStreamKeys(uri)))
                        .createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private List<?> getOfflineStreamKeys(Uri uri) {
//        return getDownloadTracker().getOfflineStreamKeys(uri);
        return null;
    }

    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return buildDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    /** Returns a {@link DataSource.Factory}. */
    public DataSource.Factory buildDataSourceFactory(TransferListener<? super DataSource> listener) {
        DefaultDataSourceFactory upstreamFactory =
                new DefaultDataSourceFactory(mContext, listener, buildHttpDataSourceFactory(listener));
        return buildReadOnlyCacheDataSource(upstreamFactory, getDownloadCache());
    }

    private Cache downloadCache = null;
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";
    private synchronized Cache getDownloadCache() {
        if (downloadCache == null) {
            File downloadContentDirectory = new File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY);
            downloadCache = new SimpleCache(downloadContentDirectory, new NoOpCacheEvictor());
        }
        return downloadCache;
    }

    private File downloadDirectory = null;
    private File getDownloadDirectory() {
        if (downloadDirectory == null) {
            downloadDirectory = mContext.getExternalFilesDir(null);
            if (downloadDirectory == null) {
                downloadDirectory = mContext.getFilesDir();
            }
        }
        return downloadDirectory;
    }

    private static CacheDataSourceFactory buildReadOnlyCacheDataSource(
            DefaultDataSourceFactory upstreamFactory, Cache cache) {
        return new CacheDataSourceFactory(
                cache,
                upstreamFactory,
                new FileDataSourceFactory(),
                /* cacheWriteDataSinkFactory= */ null,
                CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR,
                /* eventListener= */ null);
    }

    /**
     * MediaPlayer生成
     * @param fd
     * @throws IOException
     */
    public synchronized void createPlayer(FileDescriptor fd, boolean isDoFocusWaiting) throws IOException {
        LogUtil.d(TAG, "createPlayer(FileDescriptor)...");
        // FileDescriptorの再生は、WLAlexaPlayerで行う

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
    /**
     * MediaPlayer停止
     */
    private void internalStopPlayer() {
        LogUtil.d(TAG, "internalStopPlayer start");
        if(mMediaPlayer != null){
            boolean isPlaying = isPlaying();
            if (isPlaying) {
                final SimpleExoPlayer player = mMediaPlayer;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        player.stop();
                    }
                });
            }
            mPlaybackState = IAlexaPlayer.PlaybackState.STOP;
        }
    }

    /**
     * MediaPlayer停止
     */
    public synchronized void stopPlayer() {
        internalStopPlayer();
    }

    /**
     * MediaPlayerインスタンス破棄
     */
    public synchronized void releasePlayer() {
        if(mMediaPlayer != null){
            final SimpleExoPlayer player = mMediaPlayer;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    player.release();
                }
            });
        }
        mMediaPlayer = null;
    }

    /**
     * MediaPlayerに音量を設定する
     * @param volume 0～100
     */
    public void setVolume(float volume) {
        if (mMediaPlayer != null) {

            float v = volume /100.0f;
            if(mCallback != null){
                mCallback.onSetVolume(volume);
            }
            mCurrentVolume = v;
            if (mIsMute == true) {
                // ミュート状態のときは設定しない
            } else {
                if(mPlaybackState== IAlexaPlayer.PlaybackState.PLAYING || mPlaybackState== IAlexaPlayer.PlaybackState.PAUSE) {
                    //mMediaPlayer.setVolume(v * mAttenuateRatio, v * mAttenuateRatio);
                }
            }
        }
    }

    //コールバックだけ送る
    public void setVolumeCallback(float volume) {
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

                if(mPlaybackState== IAlexaPlayer.PlaybackState.PLAYING || mPlaybackState== IAlexaPlayer.PlaybackState.PAUSE) {
                    //mMediaPlayer.setVolume(v * mAttenuateRatio, v * mAttenuateRatio);
                }
            }
        }
    }

    //コールバックだけ送る
    public void adjustVolumeCallback(float volume) {
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
                if(mPlaybackState== IAlexaPlayer.PlaybackState.PLAYING || mPlaybackState== IAlexaPlayer.PlaybackState.PAUSE) {
//                    mMediaPlayer.pause();
                    if(mCallback != null){
                        mCallback.onSetMute(isMute);
                    }
                    //mMediaPlayer.setVolume(0.0f,0.0f);
                }
            }
            else {
                if(mPlaybackState== IAlexaPlayer.PlaybackState.PLAYING || mPlaybackState== IAlexaPlayer.PlaybackState.PAUSE) {
                    //mMediaPlayer.setVolume(mCurrentVolume * mAttenuateRatio, mCurrentVolume * mAttenuateRatio);
                    if(mCallback != null){
                        mCallback.onSetMute(isMute);
                    }
                }
            }
        }
    }

    //コールバックだけ送る
    public void setMuteCallback(boolean isMute) {
        if(mCallback != null){
            mCallback.onSetMute(isMute);
        }
    }

    public void setPause(){
        if (mMediaPlayer != null) {
            if(mPlaybackState== IAlexaPlayer.PlaybackState.PLAYING || mPlaybackState== IAlexaPlayer.PlaybackState.PAUSE) {
                final SimpleExoPlayer player = mMediaPlayer;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        player.setPlayWhenReady(false);
                    }
                });
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
            final SimpleExoPlayer player = mMediaPlayer;
            final long offset = offsetInMilliseconds;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (offset > 0) {
                        player.seekTo((int) offset);
                    }
                    player.setPlayWhenReady(true);
                }
            });
            this.mIsForcedStopping = false;
        }
    }

    /**
     * 再生を開始
     * @return true:再生開始成功　false:再生開始失敗
     */
    public synchronized boolean start() {
        if (DBG) android.util.Log.d(TAG, "start(), mIsForcedStopping = " + mIsForcedStopping);

        if (this.mIsForcedStopping != true) {

            if(mMediaPlayer != null){
                int state = mMediaPlayer.getPlaybackState();
                boolean playWhenReady = mMediaPlayer.getPlayWhenReady();
                if (state == Player.STATE_READY && !playWhenReady) {
                    final SimpleExoPlayer player = mMediaPlayer;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            player.setPlayWhenReady(true);
                        }
                    });
                    mPlaybackState = IAlexaPlayer.PlaybackState.PLAYING;
                    if(mCallback != null){
                        mCallback.onResumed();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 一時停止
     * @return true:一時停止成功　false:一時停止失敗
     */
    public synchronized boolean pause() {
        if(mMediaPlayer != null ){
            final SimpleExoPlayer player = mMediaPlayer;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    player.setPlayWhenReady(false);
                }
            });
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
            return (int) mMediaPlayer.getDuration();
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
                return (int) mMediaPlayer.getCurrentPosition();
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
    @Override
    @WLAudioStreamEx.WLPLAYSTATE
    public int getWLPlaybackState() {
        return WLAudioStreamEx.WL_PLAYSTATE_STOPPED;
    }

    /**
     * 再生有無取得
     * @return
     */
    public boolean isPlaying() {
//        return mMediaPlayer != null && mMediaPlayer.isPlaying();
        boolean isPlaying = false;
        if (mMediaPlayer != null) {
            isPlaying = mMediaPlayer.getPlaybackState() == Player.STATE_READY && mMediaPlayer.getPlayWhenReady();
        }
        return isPlaying;
    }

    @Override
    public void resetSpeech() {

    }
}
