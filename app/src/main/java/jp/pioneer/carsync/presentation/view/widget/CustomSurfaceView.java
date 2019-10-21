package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * カスタムsurface
 * <p>
 * アスペクト比1:1の動画専用のsurfaceView
 * 縦画面なら縦サイズ、横画面なら横サイズを一辺として生成される。
 */
public class CustomSurfaceView extends SurfaceView {

    public CustomSurfaceView(Context context) {
        super(context);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int size = widthSize >= heightSize ? widthSize : heightSize;

        setMeasuredDimension(size, size);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(size, widthMode);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(size, heightMode);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
