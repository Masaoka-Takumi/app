package jp.pioneer.carsync.presentation.view.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;

import jp.pioneer.carsync.R;

/**
 * Created by tsuyosh on 2016/02/03.
 */
public class FaderBalanceGraphView extends RelativeLayout {

    private static class PartsView extends View {
        protected Bitmap mBmpPoint = BitmapFactory.decodeResource(getResources(), R.drawable.p0293_fbpoint);
        protected Bitmap mBmpPointSelect = BitmapFactory.decodeResource(getResources(), R.drawable.p0294_fbpointselect);
        protected Bitmap mBmpLineHorizontal = BitmapFactory.decodeResource(getResources(), R.drawable.p0296_fbline);
        protected Bitmap mBmpLineVertical = BitmapFactory.decodeResource(getResources(), R.drawable.p0295_fbline);
        protected Paint mPaint;
        protected Paint mPaintColor;
        protected Matrix mMatrix = new Matrix();
        protected int mWidth = getResources().getDimensionPixelSize(R.dimen.fader_balance_setting_landscape_faderBalanceGraph_width);
        protected int mHeight = getResources().getDimensionPixelSize(R.dimen.fader_balance_setting_landscape_faderBalanceGraph_height);

        public PartsView(Context context) {
            super(context);
            init();
        }

        protected void init() {
            mPaint = new Paint();
            //アンチエイリアスがあると薄く見えたりする
            mPaint.setAntiAlias(false);
            mPaint.setColor(Color.WHITE);
            mPaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.faderBalanceGraphicView_lineStrokeWidth));
            mPaint.setAlpha((int) (0.8 * 255));
            mPaintColor = new Paint();
            TypedValue outValue = new TypedValue();
            getContext().getTheme().resolveAttribute(R.attr.uiColor, outValue, true);
            mPaintColor.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(),outValue.resourceId), PorterDuff.Mode.SRC_IN));
        }

    }

    private static class CircleView extends PartsView {
        public CircleView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = getWidth();
            int height = getHeight();

            canvas.translate((float) width / 2, (float) height / 2);

            // 円(線のみ)
            canvas.drawBitmap(mBmpPointSelect, -(float) mBmpPointSelect.getWidth() / 2, -(float) mBmpPointSelect.getHeight() / 2, mPaintColor);

            // 円(塗りつぶし)
            canvas.drawBitmap(mBmpPoint, -(float) mBmpPoint.getWidth() / 2, -(float) mBmpPoint.getHeight() / 2, mPaint);
        }
    }

    private static class HorizontalCrossLineView extends PartsView {

        public HorizontalCrossLineView(Context context) {
            super(context);
        }

        @Override
        protected void init() {
            super.init();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float sx = (float) mWidth / mBmpLineHorizontal.getWidth();
            mMatrix.postScale(sx, 1);

            canvas.concat(mMatrix);

            canvas.drawBitmap(mBmpLineHorizontal, 0, 0, mPaintColor);
            canvas.drawLine(0, mBmpLineHorizontal.getHeight()/2, mBmpLineHorizontal.getWidth(), mBmpLineHorizontal.getHeight()/2, mPaint);
        }
    }

    private static class VerticalCrossLineView extends PartsView {

        public VerticalCrossLineView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float sy = (float) mHeight / mBmpLineVertical.getHeight();
            mMatrix.postScale(1, sy);

            canvas.concat(mMatrix);

            canvas.drawBitmap(mBmpLineVertical, 0, 0, mPaintColor);
            canvas.drawLine(mBmpLineVertical.getWidth()/2, 0, mBmpLineVertical.getWidth()/2, mBmpLineVertical.getHeight(), mPaint);

        }
    }

    private Point mCenter = new Point();

    private int mBalance;
    private int mMinBalance;
    private int mMaxBalance;

    private int mFader;
    private int mMinFader;
    private int mMaxFader;

    private boolean mFaderEnabled;
    private boolean mHighlighted;


    private CircleView mCircleView;
    private HorizontalCrossLineView mHorizontalCrossLineView;
    private VerticalCrossLineView mVerticalCrossLineView;

    private OnFaderBalanceChangedListener mListener;

    public FaderBalanceGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FaderBalanceGraphView(Context context) {
        super(context);
        init();
    }

    private void init() {
        /*
         * FIXME
		 *
		 * この setClipChildren() だけだと FaderBalanceGraphView 自体の領域で clip されてしまう
		 * 親の viewGroup でも setClipChildren(false) しないと意味がない
		 *
		 * 今のレイアウトだと親が fragment の root で影響範囲が広過ぎるので、
		 * 必要範囲の大きさの viewGroup を親として追加するべきだが、Layout file を変更するのが大変なので保留
		 * ※ ただし今の状態だと performance 的によろしくなさそうので対処したほうがよい
		 *
		 */
        setClipChildren(false);
        setWillNotDraw(false);
        setOnTouchListener(new OnTouchListenerImpl());

        Resources rs = getResources();
        addSubviews();
        applyHighlighted(true, false);
    }

    private void addSubviews() {
        Context context = getContext();
        mCircleView = new CircleView(context);
        mHorizontalCrossLineView = new HorizontalCrossLineView(context);
        mVerticalCrossLineView = new VerticalCrossLineView(context);
        //Backgroundから復帰で線がViewからはみ出る
        mHorizontalCrossLineView.setClipToOutline(true);
        mVerticalCrossLineView.setClipToOutline(true);

        // CENTER_IN_PARENT で中央に固定して translateX/Y で位置を変える方法を使う
        {
            ///LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)Math.ceil(mHorizontalCrossLineView.mBmpLineHorizontal.getHeight()));
            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)Math.ceil(mHorizontalCrossLineView.mBmpLineHorizontal.getHeight()));
            lp.addRule(CENTER_IN_PARENT, 1);
            addView(mHorizontalCrossLineView, lp);
        }
        {
            //LayoutParams lp = new LayoutParams((int)Math.ceil(mVerticalCrossLineView.mBmpLineVertical.getWidth()), ViewGroup.LayoutParams.MATCH_PARENT);
            LayoutParams lp = new LayoutParams((int)Math.ceil(mVerticalCrossLineView.mBmpLineVertical.getWidth()), ViewGroup.LayoutParams.MATCH_PARENT);
            lp.addRule(CENTER_IN_PARENT, 1);
            addView(mVerticalCrossLineView, lp);
        }
        {
            int size = mCircleView.mBmpPoint.getWidth();
            LayoutParams lp = new LayoutParams(size, size);
            lp.addRule(CENTER_IN_PARENT, 1);
            addView(mCircleView, lp);
        }
    }

    private void invalidatePointerViews() {
        if (mCircleView != null)
            mCircleView.invalidate();
        if (mHorizontalCrossLineView != null)
            mHorizontalCrossLineView.invalidate();
        if (mVerticalCrossLineView != null)
            mVerticalCrossLineView.invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenter.set(w / 2, h / 2);
        movePointer(w, h, false);
    }

    private void animatePointer() {
        if (isEditing()) {
            mHasUpdates = true;
            return;
        }
        movePointer(getWidth(), getHeight(), true);
    }

    private AnimatorSet currentAnimator;

    private void movePointer(int width, int height, boolean animated) {
        float tx;
        float ty;

        int nBalance = mMaxBalance - mMinBalance;
        int nFader = mMaxFader - mMinFader;

        if (nBalance > 0) {
            float gridSizeX = (float) width / nBalance;
            tx = gridSizeX * mBalance;
        } else {
            tx = 0;
        }

        // Fader無効の場合は中央固定
        if (nFader > 0 && mFaderEnabled) {
            float gridSizeY = (float) height / nFader;
            // mFaderが大きいほど上に移動
            ty = -gridSizeY * mFader;
        } else {
            ty = 0;
        }

        if (currentAnimator != null) {
            if (currentAnimator.isRunning())
                currentAnimator.end();
            currentAnimator = null;
        }

        if (!animated) {
            mCircleView.setTranslationX(tx);
            mCircleView.setTranslationY(ty);

            mHorizontalCrossLineView.setTranslationY(ty);
            mVerticalCrossLineView.setTranslationX(tx);
        } else {
            ObjectAnimator circleX = ObjectAnimator.ofFloat(mCircleView, "translationX", tx);
            ObjectAnimator circleY = ObjectAnimator.ofFloat(mCircleView, "translationY", ty);
            ObjectAnimator crossX = ObjectAnimator.ofFloat(mVerticalCrossLineView, "translationX", tx);
            ObjectAnimator crossY = ObjectAnimator.ofFloat(mHorizontalCrossLineView, "translationY", ty);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(circleX, circleY, crossX, crossY);
            set.setInterpolator(new LinearInterpolator());
            set.setDuration(100);
            set.start();

            currentAnimator = set;
        }
    }

    private int mEditingLevel = 0;
    private boolean mHasUpdates = false;

    private boolean isEditing() {
        return mEditingLevel > 0;
    }

    @UiThread
    public void beginUpdates() {
        mEditingLevel += 1;
    }

    @UiThread
    public void endUpdates() {
        if (mEditingLevel == 0) {
            throw new IllegalStateException("delected unbalanced beginUpdates/endUpdates");
        }
        mEditingLevel -= 1;
        if (mEditingLevel == 0) {
            if (mHasUpdates) {
                mHasUpdates = false;
                animatePointer();
            }
        }
    }

    public void setFader(int fader) {
        if (mFader == fader) return;
        mFader = fader;
        animatePointer();
    }

    public void setMinFader(int fader) {
        if (mMinFader == fader) return;
        mMinFader = fader;
        animatePointer();
    }

    public void setMaxFader(int fader) {
        if (mMaxFader == fader) return;
        mMaxFader = fader;
        animatePointer();
    }

    public void setBalance(int balance) {
        if (mBalance == balance) return;
        mBalance = balance;
        animatePointer();
    }

    public void setMinBalance(int balance) {
        if (mMinBalance == balance) return;
        mMinBalance = balance;
        animatePointer();
    }

    public void setMaxBalance(int balance) {
        if (mMaxBalance == balance) return;
        mMaxBalance = balance;
        animatePointer();
    }

    public void setFaderEnabled(boolean enabled) {
        if (mFaderEnabled == enabled) return;
        mFaderEnabled = enabled;
        animatePointer();
    }

    public void setOnFaderBalanceChangedListener(OnFaderBalanceChangedListener listener) {
        mListener = listener;
    }

    private void setHighlighted(boolean highlighted) {
        setHighlighted(highlighted, true);
    }

    private void setHighlighted(boolean highlighted, boolean animated) {
        if (mHighlighted == highlighted)
            return;
        mHighlighted = highlighted;
        applyHighlighted(mHighlighted, animated);
    }

    private void applyHighlighted(boolean highlighted, boolean animated) {
        float alpha = highlighted ? 1.0f : 0.0f;
        if (!animated) {
            mHorizontalCrossLineView.setAlpha(alpha);
            mVerticalCrossLineView.setAlpha(alpha);
        } else {
            AnimatorSet set = new AnimatorSet();
            set.playTogether(
                    ObjectAnimator.ofFloat(mHorizontalCrossLineView, "alpha", alpha),
                    ObjectAnimator.ofFloat(mVerticalCrossLineView, "alpha", alpha)
            );
            set.setDuration(60);
            set.start();
        }
    }

    /**
     * x座標からbalanceの値を計算
     *
     * @param x
     * @return
     */
    private int calculateBalanceValue(float x) {
        int balanceLength = mMaxBalance - mMinBalance;
        if (balanceLength == 0) return 0;
        x = Math.max(0, Math.min(getWidth(), x));
        float gridSizeX = ((float) getWidth()) / balanceLength;
        return Math.round((x - mCenter.x) / gridSizeX);
    }

    /**
     * y座標からFaderの値を計算
     *
     * @param y
     * @return
     */
    private int calculateFaderValue(float y) {
        int faderLength = mMaxFader - mMinFader;
        if (faderLength == 0) return 0;
        y = Math.max(0, Math.min(getHeight(), y)); // はみ出さないようにするため
        float gridSizeY = ((float) getHeight()) / faderLength;
        // Faderの場合は(y - mCenter.y)の符号が逆転する
        return Math.round(-(y - mCenter.y) / gridSizeY);
    }

    private boolean mDragging = false;

    public boolean isDragging() {
        return mDragging;
    }

    private class OnTouchListenerImpl implements OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (!isEnabled()) {
                // このViewがEnabledでない場合は何もしない
                return true;
            }

            beginUpdates();
            setBalance(calculateBalanceValue(motionEvent.getX()));
            if (mFaderEnabled) setFader(calculateFaderValue(motionEvent.getY()));
            endUpdates();

            int action = motionEvent.getActionMasked();
            if (action == MotionEvent.ACTION_UP) {
                mDragging = false;
                setHighlighted(true);
                if (mListener != null) {
                    mListener.onFaderBalanceChanged(mFader, mBalance);
                }
            } else if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                mDragging = true;
                setHighlighted(true);
                if (mListener != null) {
                    mListener.onFaderBalanceMoved(mFader, mBalance);
                }
            }

            return true;
        }
    }

    public interface OnFaderBalanceChangedListener {
        void onFaderBalanceChanged(int fader, int balance);

        void onFaderBalanceMoved(int fader, int balance);
    }
}
