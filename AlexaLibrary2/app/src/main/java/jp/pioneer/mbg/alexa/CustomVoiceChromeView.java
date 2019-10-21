package jp.pioneer.mbg.alexa;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.IOException;
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
    private static Movie mListeningStartMovie;
    private static  Movie mThinkingMovie;
    private static  Movie mSpeakingMovie;
    private static  Movie mNotificationArrivedMovie;
    private static  Movie mNotificationQueuedMovie;
    private static  Movie mSystemErrorMovie;
    private static  Bitmap listeningImage;
    private static NinePatchDrawable sNinePatchDrawable;
    private static long moviestart;
    private static long listeningStartTime;
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
        IDLE(false, 0, -1),
        /** 録音中. */
        LISTENING(true, 1000, -1),
        /** 考え中. */
        THINKING(true, 3660, -1),
        /** 音声再生中. */
        SPEAKING(true, 5100, -1),
        /** マイク使用不可. */
        PRIVACY(false, 0, -1),
        /** システムエラー. */
        SYSTEM_ERROR(true, 3250, 1),
        /** 通知受信. */
        NOTIFICATIONS(true, 5130, 2),
        /** 通知あり. */
        NOTIFICATIONS_QUEUED(true, 5130, 2);
        /** アニメーション有無. */
        private boolean mIsAnimation;
        /** アニメーション更新間隔. */
        private long mAnimationLoopTime;
        /** アニメーションループ回数. */
        private long mAnimationLoop;

        /**
         * コンストラクタ.
         *
         * @param isAnimation       アニメーション有無
         * @param animationLoopTime アニメーション間隔
         * @param animationLoop     アニメーションループ回数
         */
        VoiceChromeType(boolean isAnimation, long animationLoopTime, int animationLoop) {
            mIsAnimation = isAnimation;
            mAnimationLoopTime = animationLoopTime;
            mAnimationLoop = animationLoop;
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
     * @param defStyleAttr defStyleattr
     */
    public CustomVoiceChromeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        try {
            mListeningStartMovie = Movie.decodeStream(getResources().getAssets().open("vc_listening.gif"));
            mThinkingMovie = Movie.decodeStream(getResources().getAssets().open("vc_thinking.gif"));
            mSpeakingMovie = Movie.decodeStream(getResources().getAssets().open("vc_talking.gif"));
            mNotificationArrivedMovie = Movie.decodeStream(getResources().getAssets().open("vc_notification_arrives.gif"));
            mNotificationQueuedMovie = Movie.decodeStream(getResources().getAssets().open("vc_notification_queued.gif"));
            mSystemErrorMovie = Movie.decodeStream(getResources().getAssets().open("vc_system_error.gif"));
            listeningImage = BitmapFactory.decodeResource(getResources(), R.drawable.vc);
            sNinePatchDrawable =  (NinePatchDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.vc, null);
            if (DEBUG) Log.d(TAG, "VoiceChromeType mNotificationArrivedMovie" + mNotificationArrivedMovie.duration() +" mNotificationQueuedMovie" + mNotificationQueuedMovie.duration()
                    +" mSpeakingMovie" + mSpeakingMovie.duration()+" mThinkingMovie" + mThinkingMovie.duration()+" mSystemErrorMovie" + mSystemErrorMovie.duration());
        } catch (IOException e) {
        }
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
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
            mExecutorService.scheduleAtFixedRate(new CustomVoiceChromeView.DrewTimerTask(), 0L, UPDATE_TIME, TimeUnit.MILLISECONDS);
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
        //CustomVoiceChromeView.DrawVoiceChromeManager.drawBaseCircle(canvas);
        drawVoiceChrome(canvas);
        //CustomVoiceChromeView.DrawVoiceChromeManager.drawBitmap(canvas, mAlexaLogo);
        invalidate();
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
            float xc = canvas.getWidth() / 2;
            float yc = canvas.getHeight() / 2;

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
            float xc = canvas.getWidth() / 2;
            float yc = canvas.getHeight() / 2;

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
            float xc = canvas.getWidth() / 2;
            float yc = canvas.getHeight() / 2;

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
            if (moviestart == 0) moviestart = now;
            listeningStartTime = (int)(now - moviestart);
            float scale = (float)sWidth / mListeningStartMovie.width();

            float moveX = 0;
            moveX = listeningStartTime * sWidth/2/LISTENING_START_TIME;
            canvas.drawRect(new RectF(0,0, sWidth,sHeight),p);
/*            Rect leftBounds = new Rect((int)(-listeningImage.getWidth()/2 + moveX), 0, (int)(listeningImage.getWidth()/2 + moveX), (int)sHeight);
            Rect rightBounds = new Rect((int)(sWidth-listeningImage.getWidth()/2 - moveX), 0, (int)(sWidth + listeningImage.getWidth()/2 - moveX), (int)sHeight);

            sNinePatchDrawable.setColorFilter(new PorterDuffColorFilter(VoiceChromeColor.CYAN.getColor(), PorterDuff.Mode.SRC_IN));
            sNinePatchDrawable.setBounds(leftBounds);
            sNinePatchDrawable.draw(canvas);
            sNinePatchDrawable.setBounds(rightBounds);
            sNinePatchDrawable.draw(canvas);*/
            if(moveX>sWidth/2){
                return;
            }
            int xc = canvas.getWidth() / 2;
            int left = (int)moveX-listeningImage.getWidth()/2;
            int right = (int)(sWidth-moveX + listeningImage.getWidth()/2);
            Rect tbounds = new Rect(left, 0, right,(int)sHeight );
            sNinePatchDrawable.setColorFilter(new PorterDuffColorFilter(VoiceChromeColor.CYAN.getColor(), PorterDuff.Mode.SRC_IN));
            sNinePatchDrawable.setBounds(tbounds);
            sNinePatchDrawable.draw(canvas);
            //if (DEBUG) Log.d(TAG, "drawVoiceChromeListeningStart scale" + scale +" translate" + translate);
            if (DEBUG) Log.d(TAG, "drawVoiceChromeListeningStart listeningStartTime" + listeningStartTime);
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

            //if(listeningStartTime<=LISTENING_START_TIME)return;
            int level = (int)(listeningImage.getWidth() + (float)canvas.getWidth()*db/sRange);
            if(level<listeningImage.getWidth()){
                level = listeningImage.getWidth();
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
        private static void drawVoiceChromeThinking(Canvas canvas, long current) {
            if (DEBUG) Log.d(TAG, "drawVoiceChromeThinking start");
            canvas.save();
            float xScale = sWidth / mThinkingMovie.width();
            float yScale = sHeight/ mThinkingMovie.height();
            canvas.scale(xScale, yScale);
            mThinkingMovie.setTime((int)current);
            mThinkingMovie.draw(canvas, 0, 0);
            canvas.restore();
            if (DEBUG) Log.d(TAG, "drawVoiceChromeThinking end");
        }

        /**
         * Speaking状態を描画する.
         *
         * @param canvas  Canvasインスタンス
         * @param current
         */
        private static void drawVoiceChromeSpeaking(Canvas canvas, long current) {
            if (DEBUG) Log.d(TAG, "drawVoiceChromeSpeaking start");
            canvas.save();
            float xScale = sWidth / mSpeakingMovie.width();
            float yScale = sHeight/ mSpeakingMovie.height();
            canvas.scale(xScale, yScale);
            mSpeakingMovie.setTime((int)current);
            mSpeakingMovie.draw(canvas, 0, 0);
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
        private static void drawVoiceChromeSystemError(Canvas canvas, long current, int loopCnt, CustomVoiceChromeView.DrawVoiceChromeManager.IVoiceChromeCallback voiceChrome) {
            if (DEBUG) Log.d(TAG, "drawVoiceChromeSystemError start");
            if(loopCnt<VoiceChromeType.SYSTEM_ERROR.getAnimationLoop()) {
                canvas.save();
                float xScale = sWidth / mSystemErrorMovie.width();
                float yScale = sHeight / mSystemErrorMovie.height();
                canvas.scale(xScale, yScale);
                mSystemErrorMovie.setTime((int) current);
                mSystemErrorMovie.draw(canvas, 0, 0);
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
        private static void drawVoiceChromeNotifications(Canvas canvas, long current, int loopCnt, CustomVoiceChromeView.DrawVoiceChromeManager.IVoiceChromeCallback voiceChrome) {
            if (DEBUG) Log.d(TAG, "drawVoiceChromeNotifications start");


            if (1 > loopCnt) {
                canvas.save();
                float xScale = sWidth / mNotificationArrivedMovie.width();
                float yScale = sHeight/ mNotificationArrivedMovie.height();
                canvas.scale(xScale, yScale);
                mNotificationArrivedMovie.setTime((int)current);
                mNotificationArrivedMovie.draw(canvas, 0, 0);
                canvas.restore();
            } else {
                canvas.save();
                float xScale = sWidth / mNotificationQueuedMovie.width();
                float yScale = sHeight/ mNotificationQueuedMovie.height();
                canvas.scale(xScale, yScale);
                mNotificationQueuedMovie.setTime((int)current);
                mNotificationQueuedMovie.draw(canvas, 0, 0);
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
    private class DrewTimerTask implements Runnable {

        @Override
        public void run() {
            // 現在の時刻を取得.
            long time = System.currentTimeMillis();

            // 前回の更新時刻が初期値の場合は現在値を代入.
            if (0 == mBeforeUpdateTime) {
                mBeforeUpdateTime = time;
            }

            if (DEBUG) Log.d(TAG, "time = " + time);
            if (DEBUG) Log.d(TAG, "mBeforeUpdateTime = " + mBeforeUpdateTime);

            // 前回時刻からの差分を計算する.
            long diff = time - mBeforeUpdateTime;

            // 今回の更新時間を保持する.
            mBeforeUpdateTime = time;
            if (DEBUG) Log.d(TAG, "diff = " + diff);
            long loopTime = mVoiceChromeType.getAnimationLoopTime();
            if(mVoiceChromeType==VoiceChromeType.NOTIFICATIONS&&mLoopCnt==0){
                loopTime = 4200;
            }
            // ループ間隔の範囲かを計算する.
            if ((diff + mCurAnimPosition) > mVoiceChromeType.getAnimationLoopTime()) {
                // ループ回数を増加.
                mLoopCnt++;
                // 余剰分を保持する.
                mCurAnimPosition = (diff + mCurAnimPosition) % mVoiceChromeType.getAnimationLoopTime();
            } else {
                // 加算する.
                mCurAnimPosition = diff + mCurAnimPosition;
            }
            if (DEBUG) Log.d(TAG, "mCurAnimPosition = " + mCurAnimPosition);

            // 描画の更新を行う.
            postInvalidate();
        }
    }
}
