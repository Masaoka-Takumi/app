package jp.pioneer.carsync.presentation.util;


import android.graphics.PointF;
import android.support.annotation.NonNull;

/**
 * デザインに沿った各種 spec を提供するためのクラス
 */
public class FilterDesignDefaults {

    @NonNull
    final private static FilterPathBuilder.GridPathSpec defaultPathBuilderGridPathSpec;
    static {
        FilterPathBuilder.GridPathSpec spec = new FilterPathBuilder.GridPathSpec();
        spec.minFrequency = 20;
        spec.maxFrequency = 20000;
        spec.primaryFrequencies = new int [] { 20, 100, 1000, 10000, 20000 };
        defaultPathBuilderGridPathSpec = spec;
    }

    @NonNull
    public static FilterPathBuilder.GridPathSpec defaultPathBuilderGridPathSpec () {
        return new FilterPathBuilder.GridPathSpec(defaultPathBuilderGridPathSpec);
    }

    final private static FilterGraphSpec defaultFilterGraphSpec;
    static {
        FilterGraphSpec spec = new FilterGraphSpec();
        spec.canvasWidth = 276;
        spec.canvasHeight = 56;
        spec.origin = new PointF(-3f, 17.5f);
        spec.pointsPerDecade = 80;
        spec.pointsPer10dB = 15;
        spec.decibelAtOrigin = 0;
        defaultFilterGraphSpec = spec;
    }

    @NonNull
    public static FilterGraphSpec defaultFilterGraphSpec () {
        return new FilterGraphSpec(defaultFilterGraphSpec);
    }
}
