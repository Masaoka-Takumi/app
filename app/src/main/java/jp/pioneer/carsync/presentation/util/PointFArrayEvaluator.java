package jp.pioneer.carsync.presentation.util;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

public class PointFArrayEvaluator implements TypeEvaluator<PointF []> {
    private float floatValue (float fraction, float startValue, float endValue) {
        // (endValue - startValue) * fraction + startValue;
        return endValue * fraction + startValue * (1 - fraction);
    }

    @Override
    public PointF [] evaluate (float fraction, PointF [] startValue, PointF [] endValue) {
        int n = startValue.length;
        PointF [] currentValue = new PointF[n];
        for (int i = 0; i < n; i += 1) {
            PointF s = startValue[i];
            PointF e = endValue[i];
            currentValue[i] = new PointF(floatValue(fraction, s.x, e.x), floatValue(fraction, s.y, e.y));
        }
        return currentValue;
    }
}
