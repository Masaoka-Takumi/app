package jp.pioneer.carsync.presentation.util;

import android.graphics.Paint;
import android.text.TextPaint;
import android.widget.TextView;

/**
 * Note that this class does <strong>NOT</strong> support multi-line text.
 */
public class TextSizeUtil {

    private static TextPaint textPaint(TextPaint paint, float textSize) {
        if (paint.getTextSize() != textSize) {
            paint = new TextPaint(paint);
            paint.setTextSize(textSize);
        }
        return paint;
    }

    private static TextPaint textPaint(TextView tv, float textSize) {
        return textPaint(tv.getPaint(), textSize);
    }

    public static int getIntegralHeight(TextView tv, float textSize) {
        TextPaint paint = textPaint(tv, textSize);
        Paint.FontMetricsInt fm = paint.getFontMetricsInt();

		/*
		 * どんな glyph であっても収まる高さにするならば top と bottom を使うべきだが、
		 * (一部の特殊な glyph のせいか) 実際そうすると値が大きすぎてレイアウトがおかしくなる。
		 * ARC の autoshrink 対象となる文字列であれば ascent と descent で問題ないはず。
		 */
        return -fm.ascent + fm.descent;
    }
}
