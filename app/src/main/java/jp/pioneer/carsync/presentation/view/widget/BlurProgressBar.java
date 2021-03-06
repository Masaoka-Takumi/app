package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;

import jp.pioneer.carsync.R;

/**
 * Created by NSW00_007906 on 2017/10/13.
 */

public class BlurProgressBar extends ProgressBar {
    Paint mPaintLine;
    Paint mPaintBlur;
    Paint mPaintProgress;
    Paint mPaintSecondaryProgress;
    Paint mPaintBackground;
    BlurMaskFilter mFilter;
    int mColor;
    int mPaddingTop;
    int mProgressHeight;
    public BlurProgressBar(Context context) {
        super(context);
    }

    public BlurProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.uiColor, outValue, true);
        mColor = ContextCompat.getColor(context,outValue.resourceId);
        init();
    }

    public BlurProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        Resources res = getResources();
        // Disable hardware acceleration for this view
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mPaintBlur = new Paint();
        mFilter = new BlurMaskFilter(res.getDimension(R.dimen.progressbar_blur_radius), BlurMaskFilter.Blur.OUTER);
        mPaintBlur.setColor(mColor);
        mPaintBlur.setStyle(Paint.Style.FILL);
        mPaintBlur.setMaskFilter(mFilter);
        mPaintLine = new Paint();
        mPaintLine.setColor(mColor);
        mPaintLine.setStyle(Paint.Style.STROKE);
        mPaintLine.setStrokeWidth(res.getDimension(R.dimen.progressbar_line));
        mPaintLine.setMaskFilter(new BlurMaskFilter(res.getDimension(R.dimen.progressbar_line), BlurMaskFilter.Blur.SOLID));
        mPaintBackground = new Paint();
        mPaintBackground.setARGB((int)(0.3*255),0,0,0);
        mPaintBackground.setStyle(Paint.Style.FILL);
        mPaintProgress = new Paint();
        mPaintProgress.setColor(Color.WHITE);
        mPaintProgress.setStyle(Paint.Style.FILL);
        mPaintSecondaryProgress = new Paint();
        mPaintSecondaryProgress.setColor(mColor);
        mPaintSecondaryProgress.setStyle(Paint.Style.FILL);
        mPaintSecondaryProgress.setAlpha((int)(0.75*255));
        mProgressHeight = (int)res.getDimension(R.dimen.progressbar_player_height);
        mPaddingTop = (int)res.getDimension(R.dimen.progressbar_player_padding);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        int progress = getProgress();
        int secondaryProgress = getSecondaryProgress();
        int max = getMax();
        int progressLength = (int)(((float)progress/max)*measuredWidth);
        int secondaryProgressLength = (int)(((float)secondaryProgress/max)*measuredWidth);
        canvas.drawRect(0, measuredHeight - mProgressHeight, measuredWidth, measuredHeight, mPaintBackground);
        if(secondaryProgress > 0){
            canvas.drawRect(0, measuredHeight - mProgressHeight, secondaryProgressLength, measuredHeight, mPaintSecondaryProgress);
        }
        if(progress > 0) {
            canvas.drawRect(-10, measuredHeight - mProgressHeight, progressLength, measuredHeight, mPaintBlur);
            canvas.drawRect(-10, measuredHeight - mProgressHeight, progressLength, measuredHeight + 4, mPaintLine);
            canvas.drawRect(0, measuredHeight - mProgressHeight, progressLength, measuredHeight, mPaintProgress);
        }
    }

}
