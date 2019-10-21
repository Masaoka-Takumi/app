package jp.pioneer.carsync.presentation.util;

import android.graphics.PointF;
import android.support.annotation.NonNull;

/**
 * 周波数グラフのデザインを指定するためのクラス。通常は {@link FilterDesignDefaults#defaultFilterGraphSpec()} を使えばよい。
 */
public class FilterGraphSpec {

    /** canvas (表示領域, 外枠を含まない) の幅 (dp) */
    public int canvasWidth;
    /** canvas (表示領域, 外枠を含まない) の幅 (dp) */
    public int canvasHeight;

    /** 原点 (10hz, decibelAtOrigin dB) の canvas 左上からの offset (dp) */
    public PointF origin;
    /** 周波数10倍 (e.g. 10hz->100hz) 間の距離 (x軸方向, dp) */
    public float pointsPerDecade;
    /** 10dB 毎の距離 (y軸方向, dp) */
    public float pointsPer10dB;
    /** y軸原点の dB 値 */
    public int decibelAtOrigin;

    /**
     * 何も設定されていない instance を作成する。通常はこの constructor を直接使わず、{@link FilterDesignDefaults#defaultFilterGraphSpec()} を使えばよい。
     */
    public FilterGraphSpec () {
    }

    /**
     * The copy constructor.
     * @param spec The spec to be copied.
     */
    public FilterGraphSpec (@NonNull FilterGraphSpec spec) {
        this();
        this.canvasWidth = spec.canvasWidth;
        this.canvasHeight = spec.canvasHeight;
        this.origin = spec.origin;
        this.pointsPerDecade = spec.pointsPerDecade;
        this.pointsPer10dB = spec.pointsPer10dB;
        this.decibelAtOrigin = spec.decibelAtOrigin;
    }
}
