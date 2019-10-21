package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import jp.pioneer.carsync.R;

/**
 * 丸描画View.
 */
public class CircleView extends View {
    private Paint paint;

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(ContextCompat.getColor(context,R.color.drawable_white_color));
    }

    public CircleView(Context context) {
        super(context);
        paint = new Paint();
        paint.setColor(ContextCompat.getColor(context,R.color.drawable_white_color));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        paint.setAntiAlias(true);
        canvas.drawCircle(canvas.getHeight() / 2, canvas.getHeight() / 2, (canvas.getWidth() / 2) - 2, paint);
    }
}
