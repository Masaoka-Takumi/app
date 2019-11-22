package jp.pioneer.carsync.presentation.view.widget;
import android.animation.Animator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import jp.pioneer.carsync.presentation.util.FilterDrawingUtil;

public class FilterPathView extends View {

    public FilterPathView (Context context) {
        super(context);
        init();
    }

    public FilterPathView (Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init () {
        setLayerType(LAYER_TYPE_SOFTWARE, null); // needed for setShadowLayer

        mBlinkAnimator = FilterDrawingUtil.createAnimatorToBlinkFilterPassView(this);
        mDensity = getContext().getResources().getDisplayMetrics().density;
        mPathDrawer = FilterDrawingUtil.createPassFilterPathDrawer();
    }

    private Path mPath;
    private int mPathColor;
    private boolean mCurrent;
    private Animator mBlinkAnimator;
    private float mDensity;
    private FilterDrawingUtil.PassFilterPathDrawer mPathDrawer;

    public Path getPath () {
        return mPath;
    }

    public void setPath (Path path) {
        if (mPath == path)
            return;

        mPath = path;
        invalidate();
    }

    public void setPath (Path path, @ColorInt int color, boolean isCurrent) {
        setPath(path);
        setPathColor(color);
        setCurrent(isCurrent);
    }

    @ColorInt
    public int getPathColor () {
        return mPathColor;
    }

    public void setPathColor (@ColorInt int pathColor) {
        if (mPathColor == pathColor)
            return;

        mPathColor = pathColor;
        invalidate();
    }

    public boolean isCurrent () {
        return mCurrent;
    }

    public void setCurrent (boolean current) {
        if (mCurrent == current)
            return;

        mCurrent = current;
        invalidate();
    }

    public Animator getBlinkAnimator () {
        return mBlinkAnimator;
    }

    @Override
    protected void onDraw (Canvas canvas) {
        super.onDraw(canvas);
        if (mPath != null && mPathColor != 0) {
            canvas.scale(mDensity, mDensity);
            mPathDrawer.draw(canvas, mPath, mPathColor, mCurrent);
        }
    }
}
