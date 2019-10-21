package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import jp.pioneer.carsync.presentation.util.FilterDrawingUtil;
import jp.pioneer.carsync.presentation.util.FilterGraphGeometry;
import jp.pioneer.carsync.presentation.util.FilterGraphSpec;
import jp.pioneer.carsync.presentation.util.FilterPathBuilder;

public class FilterGraphBackgroundView extends View {
    public FilterGraphBackgroundView (Context context) {
        super(context);
        init();
    }

    public FilterGraphBackgroundView (Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init () {
        mDensity = getContext().getResources().getDisplayMetrics().density;
        mGridDrawer = FilterDrawingUtil.createGridAndBackgroundDrawer();
    }

    private Path mGridPathPrimary;
    private Path mGridPathAux;
    private FilterGraphSpec mGraphSpec;
    private float mDensity;
    private FilterDrawingUtil.GridAndBackgroundDrawer mGridDrawer;

    public void setGridPath (@NonNull Path primary, @NonNull Path aux, @NonNull FilterGraphSpec graphSpec) {
        this.mGridPathPrimary = primary;
        this.mGridPathAux = aux;
        this.mGraphSpec = graphSpec;
        invalidate();
    }

    public void setGridPath (@NonNull FilterPathBuilder.GridPathSpec spec, @NonNull FilterGraphGeometry geometry) {
        FilterPathBuilder builder = new FilterPathBuilder(geometry);
        Path [] paths = builder.createGridPath(spec);
        setGridPath(paths[0], paths[1], geometry.getGraphSpec());
    }

    @Override
    protected void onDraw (Canvas canvas) {
        super.onDraw(canvas);

        if (mGraphSpec != null && mGridPathPrimary != null && mGridPathAux != null) {
            canvas.scale(mDensity, mDensity);
            mGridDrawer.draw(canvas, mGraphSpec, mGridPathPrimary, mGridPathAux);
        }
    }
}
