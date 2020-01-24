package jp.pioneer.mbg.alexa;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jp.pioneer.mbg.android.vozsis.R;

/**
 * VoiceChromeを描画するViewクラス.
 */
public class CustomVoiceChromeView extends View {

    /** ログ出力用タグ. */
    private static final String TAG = CustomVoiceChromeView.class.getSimpleName();
    /** ログの出力フラグ. */
    private static final boolean DEBUG = false;

    /** 更新回数 */
    private static final double MAX_UPDATE_COUNT = 8;
    private static final int LISTENING_START_TIME = 600;
    private int mUpdateCount = 0;
    private double mUpdateDeg = 0;
    private double mTargetDeg = 0;
    private double mCurrentDeg = 0;

    /** 音声レベルの最大値(dB) */
    private static final double MAX_VOICE_LEVEL = -20.0;
    /** 音声レベルの最小値(dB) */
    private static final double MIN_VOICE_LEVEL = -60.0;

    public boolean isNotificationAnimated = false;
    public boolean isSystemErrorAnimated = false;
    /** ループ回数. */
    private int mLoopCnt = 0;
    /** 描画の更新間隔. */
    private static final int UPDATE_TIME = 20;
    /** 前回の更新時間(ms). */
    private long mBeforeUpdateTime = 0;
    /** アニメーションの時間を管理する. */
    private long mCurAnimPosition = 0;
    /** 音声レベル(Listeningの時のみ参照). */
    private double mVoiceLevel = -100.0;

    /** 描画更新用のExecutorService. */
    private ScheduledExecutorService mExecutorService = null;

    public CustomVoiceChromeView.VoiceChromeType mTempVoiceChromeType = null;
    /** 現在のVoiceChromeタイプ. */
    private CustomVoiceChromeView.VoiceChromeType mVoiceChromeType = CustomVoiceChromeView.VoiceChromeType.IDLE;
    /** GIF画像描画用(API28未満向け) */
    private static Movie sThinkingMovie;
    private static Movie sSpeakingMovie;
    private static Movie sNotificationArrivedMovie;
    private static Movie sNotificationQueuedMovie;
    private static Movie sSystemErrorMovie;
    /** GIF画像描画用(API28以上向け) */
    private static AnimatedImageDrawable sThinkingAnimatedImage;
    private static AnimatedImageDrawable sSpeakingAnimatedImage;
    private static AnimatedImageDrawable sNotificationArrivedAnimatedImage;
    private static AnimatedImageDrawable sNotificationQueuedAnimatedImage;
    private static AnimatedImageDrawable sSystemErrorAnimatedImage;
    /** View描画共通 */
    private static Bitmap sListeningImage;
    private static NinePatchDrawable sNinePatchDrawable;
    private static long sMovieStart;
    private static long sListeningStartTime;
    private static float sWidth;
    private static float sHeight;
    private static double sRange;

    /**
     * VoiceChromeタイプ.
     */
    public enum VoiceChromeType {
        /**
         * アイドル状態.
         */
        IDLE(false, 0, -1, null),
        /** 録音中. */
        LISTENING(true, 1000, -1, "vc_listening.gif"),
        /** 考え中. */
        THINKING(true, 3660, -1, "vc_thinking.gif"),
        /** 音声再生中. */
        SPEAKING(true, 5100, -1, "vc_talking.gif"),
        /** マイク使用不可. */
        PRIVACY(false, 0, -1, null),
        /** システムエラー. */
        SYSTEM_ERROR(true, 3250, 1, "vc_system_error.gif"),
        /** 通知受信. */
        NOTIFICATIONS(true, 5130, 2, "vc_notification_arrives.gif"),
        /** 通知あり. */
        NOTIFICATIONS_QUEUED(true, 5130, 2, "vc_notification_queued.gif");
        /** アニメーション有無. */
        private boolean mIsAnimation;
        /** アニメーション更新間隔. */
        private long mAnimationLoopTime;
        /** アニメーションループ回数. */
        private long mAnimationLoop;
        /** GIFアニメーションファイル名. */
        private String mAnimationFileName;

        /**
         * コンストラクタ.
         *
         * @param isAnimation       アニメーション有無
         * @param animationLoopTime アニメーション間隔
         * @param animationLoop     アニメーションループ回数
         * @param animationFile     GIFアニメーションファイル名
         */
        VoiceChromeType(boolean isAnimation, long animationLoopTime, int animationLoop, String animationFile) {
            mIsAnimation = isAnimation;
            mAnimationLoopTime = animationLoopTime;
            mAnimationLoop = animationLoop;
            mAnimationFileName = animationFile;
        }

        /**
         * アニメーションを行うかどうか.
         *
         * @return true:アニメーションあり, false:アニメーションなし
         */
        public boolean isAnimation() {
            return mIsAnimation;
        }

        /**
         * アニメーションループ間隔.
         *
         * @return ループ間隔(ms)
         */
        public long getAnimationLoopTime() {
            return mAnimationLoopTime;
        }

        /**
         * アニメーションループ回数.
         *
         * @return ループ回数
         */
        public long getAnimationLoop() {
            return mAnimationLoop;
        }

        /**
         * GIFアニメーションファイル名
         */
        public String getAnimationFileName() {
            return mAnimationFileName;
        }
    }

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     */
    public CustomVoiceChromeView(Context context) {
        super(context);
        init();
    }

    /**
     * コンストラクタ.
     *
     * @param context コンテキスト
     * @param attrs   Attribute
     */
    public CustomVoiceChromeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * コンストラクタ.
     *
     * @param context      コンテキスト
     * @param attrs        Attribute
     * @param defStyleAttr defStyleAttr
     */
    public CustomVoiceChromeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        startImageDecode();
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    /**
     * GIF画像のdecode処理を行い、クラス変数に格納する
     */
    private void startImageDecode() {

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            // API28未満はMovieクラスを利用
            decodeVoiceChromeMovie();
        } else {
            // API28以上はImageDecoder、AnimatedImageDrawableクラスを利用
            decodeVoiceChromeAnimatedImage();
        }

        if(sListeningImage == null) {
            sListeningImage = BitmapFactory.decodeResource(getResources(), R.drawable.vc);
        }
        if(sNinePatchDrawable == null) {
            sNinePatchDrawable = (NinePatchDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.vc, null);
        }
    }

    /**
     * Movieクラスを使ったGIF画像のdecode処理
     * API28でdeprecateになった
     */
    @SuppressWarnings("deprecation")
    private void decodeVoiceChromeMovie() {
        try {
            if (sThinkingMovie == null) {
                sThinkingMovie = Movie.decodeStream(getResources().getAssets().open(VoiceChromeType.THINKING.getAnimationFileName()));
            }
            if (sSpeakingMovie == null) {
                sSpeakingMovie = Movie.decodeStream(getResources().getAssets().open(VoiceChromeType.SPEAKING.getAnimationFileName()));
            }
            if (sNotificationArrivedMovie == null) {
                sNotificationArrivedMovie = Movie.decodeStream(getResources().getAssets().open(VoiceChromeType.NOTIFICATIONS.getAnimationFileName()));
            }
            if (sNotificationQueuedMovie == null) {
                sNotificationQueuedMovie = Movie.decodeStream(getResources().getAssets().open(VoiceChromeType.NOTIFICATIONS_QUEUED.getAnimationFileName()));
            }
            if (sSystemErrorMovie == null) {
                sSystemErrorMovie = Movie.decodeStream(getResources().getAssets().open(VoiceChromeType.SYSTEM_ERROR.getAnimationFileName()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (DEBUG)
            Log.d(TAG, "VoiceChromeType sNotificationArrivedMovie" + sNotificationArrivedMovie.duration() + " sNotificationQueuedMovie" + sNotificationQueuedMovie.duration()
                    + " sSpeakingMovie" + sSpeakingMovie.duration() + " sThinkingMovie" + sThinkingMovie.duration() + " sSystemErrorMovie" + sSystemErrorMovie.duration());
    }

    /**
     * ImageDecoderを使ったGIF画像のdecode処理
     * UI Threadでの実行は非推奨なのでAsyncTaskを使って非同期に行う
     * decode後、ImageDecoderTask.Listenerインターフェースの実装で利用側処理を行う
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private void decodeVoiceChromeAnimatedImage() {
        if (DEBUG) Log.d(TAG, "Start ImageDecoderTask");
        if(sThinkingAnimatedImage == null) {
            new ImageDecoderTask(getResources().getAssets(), createImageDecoderListener()).execute(VoiceChromeType.THINKING);
        }
        if(sSpeakingAnimatedImage == null) {
            new ImageDecoderTask(getResources().getAssets(), createImageDecoderListener()).execute(VoiceChromeType.SPEAKING);
        }
        if(sSystemErrorAnimatedImage == null) {
            new ImageDecoderTask(getResources().getAssets(), createImageDecoderListener()).execute(VoiceChromeType.SYSTEM_ERROR);
        }
        if(sNotificationArrivedAnimatedImage == null) {
            new ImageDecoderTask(getResources().getAssets(), createImageDecoderListener()).execute(VoiceChromeType.NOTIFICATIONS);
        }
        if(sNotificationQueuedAnimatedImage == null) {
            new ImageDecoderTask(getResources().getAssets(), createImageDecoderListener()).execute(VoiceChromeType.NOTIFICATIONS_QUEUED);
        }
    }

    /**
     * ImageDecoderTask.Listenerの生成
     * decode完了後、VoiceChromeTypeに応じて、各変数に格納する
     * ImageDecoderTaskのdecode処理が長引き、View#onDrawが先に呼ばれてしまう場合の対策で
     * ImageDecoderTaskのdecodeが完了次第、View再描画する
     * ImageDecoderTaskを使ってdecodeする各変数の使用時はnullチェックを行うこと
     *
     * @return ImageDecoderTask.Listener
     */
    private ImageDecoderTask.Listener createImageDecoderListener() {
        return new ImageDecoderTask.Listener() {
            @Override
            public void onPostExecute(AnimatedImageDrawable animatedImageDrawable, VoiceChromeType voiceChromeType) {

                switch (voiceChromeType) {
                    case LISTENING:
                        break;
                    case THINKING:
                        sThinkingAnimatedImage = animatedImageDrawable;
                        break;
                    case SPEAKING:
                        sSpeakingAnimatedImage = animatedImageDrawable;
                        break;
                    case SYSTEM_ERROR:
                        sSystemErrorAnimatedImage = animatedImageDrawable;
                        break;
                    case NOTIFICATIONS:
                        sNotificationArrivedAnimatedImage = animatedImageDrawable;
                        break;
                    case NOTIFICATIONS_QUEUED:
                        sNotificationQueuedAnimatedImage = animatedImageDrawable;
                        break;
                }

                // decodeが完了次第、View再描画指示
                if(DEBUG) Log.d(TAG, "ImageDecoderTask Listener onPostExecute invalidate");
                invalidate();
            }
        };
    }


    /**
     * アニメーション描画タスクを開始する.
     */
    public void startDrawTask() {
        if (mExecutorService == null || mExecutorService.isShutdown()) {
            // ループ回数を初期化.
            mLoopCnt = 0;
            if (mVoiceChromeType == CustomVoiceChromeView.VoiceChromeType.NOTIFICATIONS) {
                isNotificationAnimated = true;
            }
            if (mVoiceChromeType == VoiceChromeType.SYSTEM_ERROR) {
                isSystemErrorAnimated = true;
            }
            // 音声レベルを初期化する.
            mVoiceLevel = -100.0;
            // 前回描画時間の初期化.
            mBeforeUpdateTime = System.currentTimeMillis();
            // ExecutorServiceの生成
            mExecutorService = Executors.newSingleThreadScheduledExecutor();

            // 一定間隔毎にごとにRunnableの処理を実行する
            mExecutorService.scheduleAtFixedRate(new DrawTimerTask(this), 0L, UPDATE_TIME, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * アニメーション描画タスクを停止する.
     */
    public void stopDrawTask() {
        if (mExecutorService != null) {
            isNotificationAnimated = false;
            isSystemErrorAnimated = false;
            // ループ回数を初期化.
            mLoopCnt = 0;
            // アニメーションポジションの初期化
            mCurAnimPosition = 0;
            // 前回描画時間の初期化.
            mBeforeUpdateTime = 0;
            // ExecutorServiceの停止.
            mExecutorService.shutdown();
            mExecutorService = null;
        }
    }

    /**
     * サイズを決定する
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (DEBUG) Log.d(TAG, "onMeasure start");

        int spec_width = MeasureSpec.getSize(widthMeasureSpec);
        int spec_height = MeasureSpec.getSize(heightMeasureSpec);

        //一番小さいのを使う
        int spec_size = Math.min(spec_width, spec_height);

        //サイズ設定
        setMeasuredDimension(spec_size, spec_size);

        if (DEBUG) Log.d(TAG, "onMeasure end");
    }

    /**
     * 描画処理
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (DEBUG) Log.d(TAG, "onDraw start");
        super.onDraw(canvas);
        drawVoiceChrome(canvas);
        if (DEBUG) Log.d(TAG, "onDraw end");
    }

    @Override
    protected void onAttachedToWindow() {
        if (DEBUG) Log.d(TAG, "onAttachedToWindow start");
        super.onAttachedToWindow();
        if (DEBUG) Log.d(TAG, "onAttachedToWindow end");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopDrawTask();
    }


    /**
     * タイプごとのVoiceChromeの描画を行う
     *
     * @param canvas
     */
    private void drawVoiceChrome(Canvas canvas) {
        if (DEBUG) Log.d(TAG, "drawVoiceChrome start");
        sWidth = getWidth();
        sHeight = getHeight();
        switch (mVoiceChromeType) {
            case IDLE:
                CustomVoiceChromeView.DrawVoiceChromeManager.drawVoiceChromeIdle(canvas);
                break;
            case LISTENING:
                CustomVoiceChromeView.DrawVoiceChromeManager.drawVoiceChromeListeningStart(canvas);
                double absMin = Math.abs(MIN_VOICE_LEVEL);
                double absMax = Math.abs(MAX_VOICE_LEVEL);
                sRange = Math.max(absMax, absMin) - Math.min(absMax, absMin);

                if (DEBUG) Log.d(TAG, "mUpdateCount = " + mUpdateCount);
                if (0 == mUpdateCount) {
                    if (DEBUG) Log.d(TAG, "0 == mUpdateCount");

                    if (mVoiceLevel > MIN_VOICE_LEVEL) {
                        double absDb = Math.abs(mVoiceLevel);
                        mTargetDeg = Math.max(absMin, absDb) - Math.min(absMin, absDb);
                    } else {
                        mTargetDeg = 0;
                    }
                    mUpdateDeg = (mTargetDeg - mCurrentDeg) / MAX_UPDATE_COUNT;
                    mUpdateCount++;
                } else if (mUpdateCount >= MAX_UPDATE_COUNT) {
                    if (DEBUG) Log.d(TAG, "mUpdateCount >= MAX_UPDATE_COUNT");
                    mUpdateCount = 0;
                } else {
                    mUpdateCount++;
                }
                mCurrentDeg += mUpdateDeg;
                if (0 > mCurrentDeg) {
                    mCurrentDeg = 0;
                }
                if (DEBUG) Log.d(TAG, "mTargetDeg = " + mTargetDeg);
                if (DEBUG) Log.d(TAG, "mUpdateDeg = " + mUpdateDeg);
                if (DEBUG) Log.d(TAG, "mCurrentDeg = " + mCurrentDeg);
                // 計算
                float deg = (float) (135 * (float) (mCurrentDeg * 135) / (sRange * 135));
                CustomVoiceChromeView.DrawVoiceChromeManager.drawVoiceChromeListening(canvas, mCurrentDeg);
                break;
            case THINKING:
                CustomVoiceChromeView.DrawVoiceChromeManager.drawVoiceChromeThinking(canvas, mCurAnimPosition);
                break;
            case SPEAKING:
                CustomVoiceChromeView.DrawVoiceChromeManager.drawVoiceChromeSpeaking(canvas, mCurAnimPosition);
                break;
            case PRIVACY:
                CustomVoiceChromeView.DrawVoiceChromeManager.drawVoiceChromePrivacy(canvas);
                break;
            case SYSTEM_ERROR:
                CustomVoiceChromeView.DrawVoiceChromeManager.drawVoiceChromeSystemError(canvas,mCurAnimPosition, mLoopCnt, new CustomVoiceChromeView.DrawVoiceChromeManager.IVoiceChromeCallback() {
                    @Override
                    public void onNotificationAnimationEnd() {
                        isSystemErrorAnimated = false;
                        if (mTempVoiceChromeType != null) {
                            CustomVoiceChromeView.VoiceChromeType nextType = mTempVoiceChromeType;
                            mTempVoiceChromeType = null;
                            setVoiceChromeType(nextType);
                        }
                    }
                });
                break;
            case NOTIFICATIONS:
            case NOTIFICATIONS_QUEUED:
                CustomVoiceChromeView.DrawVoiceChromeManager.drawVoiceChromeNotifications(canvas, mCurAnimPosition, mLoopCnt, new CustomVoiceChromeView.DrawVoiceChromeManager.IVoiceChromeCallback() {
                    @Override
                    public void onNotificationAnimationEnd() {
                        isNotificationAnimated = false;
                        if (mTempVoiceChromeType != null) {
                            CustomVoiceChromeView.VoiceChromeType nextType = mTempVoiceChromeType;
                            mTempVoiceChromeType = null;
                            setVoiceChromeType(nextType);
                        }
                    }
                });
                break;
        }
        if (DEBUG) Log.d(TAG, "drawVoiceChrome end");
    }

    /**
     * VoiceChromeのタイプを設定する.
     *
     * @param voiceChromeType
     */
    public void setVoiceChromeType(CustomVoiceChromeView.VoiceChromeType voiceChromeType) {
        if (DEBUG) Log.d(TAG, "VoiceChromeType start");
        if (mVoiceChromeType == CustomVoiceChromeView.VoiceChromeType.NOTIFICATIONS && isNotificationAnimated) {
            mTempVoiceChromeType = voiceChromeType;
            return;
        }
        if (mVoiceChromeType == VoiceChromeType.SYSTEM_ERROR && isSystemErrorAnimated) {
            mTempVoiceChromeType = voiceChromeType;
            return;
        }
        // タイプを保持する.
        mVoiceChromeType = voiceChromeType;
        // 現在実行しているDrawTaskを停止する.
        stopDrawTask();

        // アニメーションを行うタイプの場合はDrawTaskを開始する.
        if (mVoiceChromeType.isAnimation()) {
            startDrawTask();
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        });
        if (DEBUG) Log.d(TAG, "VoiceChromeType end");
    }

    public VoiceChromeType getVoiceChromeType() {
        return mVoiceChromeType;
    }

    /**
     * 音声レベルを指定する.
     *
     * @param db
     */
    public void setVoiceLevel(double db) {
        mVoiceLevel = db;
    }

    /**
     * VoiceChromeの描画を行う処理をまとめたクラス.
     */
    private static class DrawVoiceChromeManager {

        public interface IVoiceChromeCallback {
            public void onNotificationAnimationEnd();
        }

        /**
         * VoiceChromeの色.
         */
        private enum VoiceChromeColor {
            BASE(24, 24, 24),
            BLUE(16, 80, 248),
            CYAN(112, 232, 232),
            RED(255, 0, 0),
            ORANGE(255, 144, 0),
            YELLOW(240, 255, 56);

            private int mRed;
            private int mGreen;
            private int mBlue;

            VoiceChromeColor(int red, int green, int blue) {
                mRed = red;
                mGreen = green;
                mBlue = blue;
            }

            /**
             * カラーの取得.
             *
             * @return rgb
             */
            public int getColor() {
                return Color.rgb(mRed, mGreen, mBlue);
            }

            /**
             * α値を指定してカラーの取得.
             *
             * @param alpha α値
             * @return argb
             */
            public int getColor(int alpha) {
                return Color.argb(alpha, mRed, mGreen, mBlue);
            }
        }

        /**
         * 円を描画する.
         *
         * @param canvas Canvasインスタンス
         */
        private static void drawBaseCircle(Canvas canvas) {
            if (DEBUG) Log.d(TAG, "drawBaseCircle start");
            // 中央値の計算
            float xc = (float)canvas.getWidth() / 2;
            float yc = (float)canvas.getHeight() / 2;

            // 半径
            float r = Math.min(xc, yc);

            Paint paint = new Paint();
            paint.setColor(CustomVoiceChromeView.DrawVoiceChromeManager.VoiceChromeColor.BASE.getColor());
            paint.setStyle(Paint.Style.FILL);

            // アンチエイリアスの円を描画
            paint.setAntiAlias(true);
            canvas.drawCircle(xc, yc, r, paint);

            if (DEBUG) Log.d(TAG, "xc = " + xc);
            if (DEBUG) Log.d(TAG, "yc = " + yc);
            if (DEBUG) Log.d(TAG, "r = " + r);
            if (DEBUG) Log.d(TAG, "drawBaseCircle end");
        }

        /**
         * 円の周りに沿って線を描画する.
         *
         * @param canvas Canvasインスタンス
         * @param color
         */
        private static void drawCircleLine(Canvas canvas, int color) {
            if (DEBUG) Log.d(TAG, "drawBaseCircle start");
            // 中央値の計算
            float xc = (float)canvas.getWidth() / 2;
            float yc = (float)canvas.getHeight() / 2;

            // 線の太さ
            float strokeWidth = (canvas.getWidth() * 0.05f);

            // 半径
            float r = Math.min(xc, yc) - strokeWidth / 2f;

            // 描画設定を行う
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);

            // 描画
            paint.setAntiAlias(true);
            canvas.drawCircle(xc, yc, r, paint);

            if (DEBUG) Log.d(TAG, "color = " + color);
            if (DEBUG) Log.d(TAG, "xc = " + xc);
            if (DEBUG) Log.d(TAG, "yc = " + yc);
            if (DEBUG) Log.d(TAG, "r = " + r);
            if (DEBUG) Log.d(TAG, "drawBaseCircle end");
        }

        /**
         * 6時の方向から指定した角度の線を描画する.
         *
         * @param canvas Canvasインスタンス
         * @param color
         */
        private static void drawLine(Canvas canvas, int color, float degree) {
            if (DEBUG) Log.d(TAG, "drawBaseCircle start");
            // 中央値の計算
            float xc = (float)canvas.getWidth() / 2;
            float yc = (float)canvas.getHeight() / 2;

            // 線の太さ
            float strokeWidth = (canvas.getWidth() * 0.05f);

            // 半径
            float r = Math.min(xc, yc) - strokeWidth / 2f;

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(color);
            paint.setStrokeWidth(strokeWidth);

            paint.setAntiAlias(true);
            canvas.drawArc(new RectF((strokeWidth / 2f), (strokeWidth / 2f), (canvas.getHeight() - (strokeWidth / 2f)), (canvas.getHeight() - (strokeWidth / 2f))), 90, degree, false, paint);

            if (DEBUG) Log.d(TAG, "color = " + color);
            if (DEBUG) Log.d(TAG, "xc = " + xc);
            if (DEBUG) Log.d(TAG, "yc = " + yc);
            if (DEBUG) Log.d(TAG, "r = " + r);
            if (DEBUG) Log.d(TAG, "drawBaseCircle end");
        }

        /**
         * 画像を描画する.
         *
         * @param canvas    Canvasインスタンス
         * @param alexaLogo
         */
        private static void drawBitmap(Canvas canvas, Bitmap alexaLogo) {
            Rect src = new Rect(0, 0, alexaLogo.getWidth(), alexaLogo.getHeight());
            Rect dst = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
            canvas.drawBitmap(alexaLogo, src, dst, null);
        }

        /**
         * IDLE状態を描画する.
         *
         * @param canvas Canvasインスタンス
         */
        private static void drawVoiceChromeIdle(Canvas canvas) {
            if (DEBUG) Log.d(TAG, "drawVoiceChromeIdle start");
            // なにもしない
            canvas.drawColor(Color.TRANSPARENT);
            if (DEBUG) Log.d(TAG, "drawVoiceChromeIdle end");
        }

        /**
         * LISTENING STARTを描画する.
         *
         * @param canvas Canvasインスタンス
         */
        private static void drawVoiceChromeListeningStart(Canvas canvas) {
            if (DEBUG) Log.d(TAG, "drawVoiceChromeListeningStart start");

            long now = android.os.SystemClock.uptimeMillis();
            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setColor(VoiceChromeColor.BLUE.getColor());
            if (sMovieStart == 0) sMovieStart = now;
            sListeningStartTime = (int)(now - sMovieStart);

            float moveX = 0;
            moveX = sListeningStartTime * sWidth/2/LISTENING_START_TIME;
            canvas.drawRect(new RectF(0,0, sWidth,sHeight),p);

            if(moveX>sWidth/2){
                return;
            }
            int xc = canvas.getWidth() / 2;
            int left = (int)moveX- sListeningImage.getWidth()/2;
            int right = (int)(sWidth-moveX + sListeningImage.getWidth()/2);
            Rect tbounds = new Rect(left, 0, right,(int)sHeight );
            sNinePatchDrawable.setColorFilter(new PorterDuffColorFilter(VoiceChromeColor.CYAN.getColor(), PorterDuff.Mode.SRC_IN));
            sNinePatchDrawable.setBounds(tbounds);
            sNinePatchDrawable.draw(canvas);
            if (DEBUG) Log.d(TAG, "drawVoiceChromeListeningStart sListeningStartTime" + sListeningStartTime);
            // なにもしない
            if (DEBUG) Log.d(TAG, "drawVoiceChromeListeningStart end");
        }

        /**
         * Listening状態を描画する.
         *
         * @param canvas Canvasインスタンス
         * @param db
         */
        private static void drawVoiceChromeListening(Canvas canvas, double db) {
            if (DEBUG) Log.d(TAG, "drawVoiceChromeListening start");
            // 中央値の計算
            int xc = canvas.getWidth() / 2;

            int level = (int)(sListeningImage.getWidth() + (float)canvas.getWidth()*db/sRange);
            if(level< sListeningImage.getWidth()){
                level = sListeningImage.getWidth();
            }
            int left = xc - level;
            int right = xc + level;
            if (DEBUG) Log.d(TAG, "drawVoiceChromeListening left" + left + " right" + right);
            canvas.save();
            canvas.translate((float)(canvas.getWidth()-level)/2, 0);
            Rect tbounds = new Rect(0, 0, level,(int)sHeight );
            sNinePatchDrawable.setColorFilter(new PorterDuffColorFilter(VoiceChromeColor.CYAN.getColor(), PorterDuff.Mode.SRC_IN));
            sNinePatchDrawable.setBounds(tbounds);
            sNinePatchDrawable.draw(canvas);
            canvas.restore();
            if (DEBUG) Log.d(TAG, "drawVoiceChromeListening end");
        }

        /**
         * Thinking状態を描画する.
         *
         * @param canvas  Canvasインスタンス
         * @param current
         */
        @SuppressWarnings("deprecation")
        private static void drawVoiceChromeThinking(Canvas canvas, long current) {
            if (DEBUG) Log.d(TAG, "drawVoiceChromeThinking start");
            canvas.save();
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P){
                float xScale = sWidth / sThinkingMovie.width();
                float yScale = sHeight/ sThinkingMovie.height();
                canvas.scale(xScale, yScale);
                sThinkingMovie.setTime((int)current);
                sThinkingMovie.draw(canvas, 0, 0);
            } else if(sThinkingAnimatedImage != null) {
                float xScale = sWidth / sThinkingAnimatedImage.getIntrinsicWidth();
                float yScale = sHeight/ sThinkingAnimatedImage.getIntrinsicHeight();
                canvas.scale(xScale, yScale);
                sThinkingAnimatedImage.draw(canvas);
                sThinkingAnimatedImage.start();
            }
            canvas.restore();
            if (DEBUG) Log.d(TAG, "drawVoiceChromeThinking end");
        }

        /**
         * Speaking状態を描画する.
         *
         * @param canvas  Canvasインスタンス
         * @param current
         */
        @SuppressWarnings("deprecation")
        private static void drawVoiceChromeSpeaking(Canvas canvas, long current) {
            if (DEBUG) Log.d(TAG, "drawVoiceChromeSpeaking start");
            canvas.save();
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                float xScale = sWidth / sSpeakingMovie.width();
                float yScale = sHeight/ sSpeakingMovie.height();
                canvas.scale(xScale, yScale);
                sSpeakingMovie.setTime((int)current);
                sSpeakingMovie.draw(canvas, 0, 0);
            } else if(sSpeakingAnimatedImage != null) {
                float xScale = sWidth/ sSpeakingAnimatedImage.getIntrinsicWidth();
                float yScale = sHeight/ sSpeakingAnimatedImage.getIntrinsicHeight();
                canvas.scale(xScale, yScale);
                sSpeakingAnimatedImage.draw(canvas);
                sSpeakingAnimatedImage.start();
            }
            canvas.restore();
            if (DEBUG) Log.d(TAG, "drawVoiceChromeSpeaking end");
        }

        /**
         * Privacy状態を描画する.
         *
         * @param canvas Canvasインスタンス
         */
        private static void drawVoiceChromePrivacy(Canvas canvas) {
            if (DEBUG) Log.d(TAG, "drawVoiceChromePrivacy start");
            // 描画
            //drawCircleLine(canvas, CustomVoiceChromeView.DrawVoiceChromeManager.VoiceChromeColor.RED.getColor());
            Paint p = new Paint();
            p.setAntiAlias(true);
            p.setColor(VoiceChromeColor.RED.getColor());
            canvas.drawRect(new RectF(0,0, sWidth,sHeight),p);
            if (DEBUG) Log.d(TAG, "drawVoiceChromePrivacy end");
        }

        /**
         * システムエラー状態を描画する.
         *
         * @param canvas Canvasインスタンス
         */
        @SuppressWarnings("deprecation")
        private static void drawVoiceChromeSystemError(Canvas canvas, long current, int loopCnt, CustomVoiceChromeView.DrawVoiceChromeManager.IVoiceChromeCallback voiceChrome) {
            if (DEBUG) Log.d(TAG, "drawVoiceChromeSystemError start");
            if(loopCnt<VoiceChromeType.SYSTEM_ERROR.getAnimationLoop()) {
                canvas.save();
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    float xScale = sWidth / sSystemErrorMovie.width();
                    float yScale = sHeight / sSystemErrorMovie.height();
                    canvas.scale(xScale, yScale);
                    sSystemErrorMovie.setTime((int) current);
                    sSystemErrorMovie.draw(canvas, 0, 0);
                } else if(sSystemErrorAnimatedImage != null) {
                    float xScale = sWidth / sSystemErrorAnimatedImage.getIntrinsicWidth();
                    float yScale = sHeight / sSystemErrorAnimatedImage.getIntrinsicHeight();
                    canvas.scale(xScale, yScale);
                    sSystemErrorAnimatedImage.draw(canvas);
                    sSystemErrorAnimatedImage.start();
                }
                canvas.restore();
            }else{
                // 描画
                Paint p = new Paint();
                p.setAntiAlias(true);
                p.setColor(VoiceChromeColor.RED.getColor());
                canvas.drawRect(new RectF(0, 0, sWidth, sHeight), p);
                if (voiceChrome != null) {
                    voiceChrome.onNotificationAnimationEnd();
                }
            }
            if (DEBUG) Log.d(TAG, "drawVoiceChromeSystemError end");
        }

        /**
         * Notification状態を描画する.
         *
         * @param canvas  Canvasインスタンス
         * @param current
         */
        @SuppressWarnings("deprecation")
        private static void drawVoiceChromeNotifications(Canvas canvas, long current, int loopCnt, CustomVoiceChromeView.DrawVoiceChromeManager.IVoiceChromeCallback voiceChrome) {
            if (DEBUG) Log.d(TAG, "drawVoiceChromeNotifications start");


            if (1 > loopCnt) {
                canvas.save();
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    float xScale = sWidth / sNotificationArrivedMovie.width();
                    float yScale = sHeight/ sNotificationArrivedMovie.height();
                    canvas.scale(xScale, yScale);
                    sNotificationArrivedMovie.setTime((int)current);
                    sNotificationArrivedMovie.draw(canvas, 0, 0);
                } else if(sNotificationArrivedAnimatedImage != null ){
                    float xScale = sWidth / sNotificationArrivedAnimatedImage.getIntrinsicWidth();
                    float yScale = sHeight/ sNotificationArrivedAnimatedImage.getIntrinsicHeight();
                    canvas.scale(xScale, yScale);
                    sNotificationArrivedAnimatedImage.draw(canvas);
                    sNotificationArrivedAnimatedImage.start();
                }
                canvas.restore();
            } else {
                canvas.save();
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    float xScale = sWidth / sNotificationQueuedMovie.width();
                    float yScale = sHeight/ sNotificationQueuedMovie.height();
                    canvas.scale(xScale, yScale);
                    sNotificationQueuedMovie.setTime((int)current);
                    sNotificationQueuedMovie.draw(canvas, 0, 0);
                } else if(sNotificationQueuedAnimatedImage != null) {
                    float xScale = sWidth / sNotificationQueuedAnimatedImage.getIntrinsicWidth();
                    float yScale = sHeight/ sNotificationQueuedAnimatedImage.getIntrinsicHeight();
                    canvas.scale(xScale, yScale);
                    sNotificationQueuedAnimatedImage.draw(canvas);
                    sNotificationQueuedAnimatedImage.start();
                }
                canvas.restore();
                if (voiceChrome != null) {
                    voiceChrome.onNotificationAnimationEnd();
                }
            }

            if (DEBUG) Log.d(TAG, "drawVoiceChromeNotifications end");
        }
    }

    /**
     * 描画更新用Runnable.
     */
    private static class DrawTimerTask implements Runnable {

        private WeakReference<CustomVoiceChromeView> mViewWeakRef;

        DrawTimerTask(CustomVoiceChromeView customVoiceChromeView) {
            mViewWeakRef = new WeakReference<>(customVoiceChromeView);
        }

        @Override
        public void run() {
            if(mViewWeakRef == null) {
                return;
            }
            CustomVoiceChromeView view = mViewWeakRef.get();
            if (view != null) {
                // 現在の時刻を取得.
                long time = System.currentTimeMillis();

                // 前回の更新時刻が初期値の場合は現在値を代入.
                if (0 == view.mBeforeUpdateTime) {
                    view.mBeforeUpdateTime = time;
                }

                if (DEBUG) Log.d(TAG, "time = " + time);
                if (DEBUG) Log.d(TAG, "mBeforeUpdateTime = " + view.mBeforeUpdateTime);

                // 前回時刻からの差分を計算する.
                long diff = time - view.mBeforeUpdateTime;

                // 今回の更新時間を保持する.
                view.mBeforeUpdateTime = time;
                if (DEBUG) Log.d(TAG, "diff = " + diff);
                long loopTime = view.mVoiceChromeType.getAnimationLoopTime();
                if (view.mVoiceChromeType == VoiceChromeType.NOTIFICATIONS && view.mLoopCnt == 0) {
                    loopTime = 4200;
                }
                // ループ間隔の範囲かを計算する.
                if ((diff + view.mCurAnimPosition) > view.mVoiceChromeType.getAnimationLoopTime()) {
                    // ループ回数を増加.
                    view.mLoopCnt++;
                    // 余剰分を保持する.
                    view.mCurAnimPosition = (diff + view.mCurAnimPosition) % view.mVoiceChromeType.getAnimationLoopTime();
                } else {
                    // 加算する.
                    view.mCurAnimPosition = diff + view.mCurAnimPosition;
                }
                if (DEBUG) Log.d(TAG, "mCurAnimPosition = " + view.mCurAnimPosition);

                // 描画の更新を行う.
                view.postInvalidate();
            }
        }
    }

    /**
     * API28以上のGIF画像decode用Task
     * ImageDecoder#decodeDrawableがUI Threadでの実行が非推奨なため作成
     * VoiceChromeTypeに応じたGIF画像のAnimatedImageDrawableを取得する
     * VoiceChromeTypeのmAnimationFileNameがnullの場合やファイルが開けない場合など
     * 何らかの原因で取得できない場合はnullとなる
     * 利用側ではImageDecoder.Listenerインターフェースを使う
     */
    @RequiresApi(api = Build.VERSION_CODES.P)
    private static class ImageDecoderTask extends AsyncTask<VoiceChromeType, Void, AnimatedImageDrawable> {

        // ImageDecoder#createSourceで必要
        private AssetManager mAssetManager;
        // クラス変数への格納で使いたいため保持
        private VoiceChromeType mVoiceChromeType;
        private WeakReference<ImageDecoderTask.Listener> mListenerWeakRef;

        private ImageDecoderTask(AssetManager assetManager, ImageDecoderTask.Listener listener) {
            mAssetManager = assetManager;
            mListenerWeakRef = new WeakReference<>(listener);
        }

        @Override
        protected AnimatedImageDrawable doInBackground(VoiceChromeType... voiceChromeType) {
            if(voiceChromeType[0] == null || voiceChromeType[0].getAnimationFileName() == null) {
                return null;
            }
            mVoiceChromeType = voiceChromeType[0];

            try {
                return (AnimatedImageDrawable) ImageDecoder.decodeDrawable(ImageDecoder.createSource(mAssetManager, voiceChromeType[0].getAnimationFileName()));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(AnimatedImageDrawable animatedImageDrawable) {
            if (DEBUG) Log.d(TAG, "ImageDecoderTask onPostExecute VoiceChromeType=" + mVoiceChromeType);
            if(animatedImageDrawable == null || mListenerWeakRef == null) {
                return;
            }

            Listener listener = mListenerWeakRef.get();
            if(listener != null) {
                listener.onPostExecute(animatedImageDrawable, mVoiceChromeType);
            }
        }

        // 利用側通知リスナ
        private interface Listener {
            void onPostExecute(AnimatedImageDrawable animatedImageDrawable, VoiceChromeType voiceChromeType);
        }
    }
}
