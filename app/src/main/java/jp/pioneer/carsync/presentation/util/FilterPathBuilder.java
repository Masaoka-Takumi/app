package jp.pioneer.carsync.presentation.util;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * 周波数グラフの描画に使用する Path を作成するためのクラス
 */
public class FilterPathBuilder {

    @NonNull
    final private FilterGraphGeometry geometry;
    @NonNull
    final private FilterGraphSpec graphSpec;

    public FilterPathBuilder (@NonNull FilterGraphGeometry geometry) {
        this.geometry = geometry;
        this.graphSpec = geometry.getGraphSpec();
    }

    public static class GridPathSpec {
        /** 最初の縦線を引く周波数 (Hz) */
        public int minFrequency;
        /** 最後の縦線を引く周波数 (Hz) */
        public int maxFrequency;
        /** 主線で縦線を引く周波数の配列 */
        public int[] primaryFrequencies;

        /**
         * 何も設定されていない instance を作成する。通常はこの constructor ではなく {@link FilterDesignDefaults#defaultPathBuilderGridPathSpec()}  を使えばよい。
         */
        public GridPathSpec () {
        }

        public GridPathSpec (@NonNull GridPathSpec spec) {
            this.minFrequency = spec.minFrequency;
            this.maxFrequency = spec.maxFrequency;
            this.primaryFrequencies = spec.primaryFrequencies;
        }
    }

    /**
     * <p>周波数グラフの格子描画に使う path を作成し、二要素の配列として返す</p>
     * <p>最初の path は主線用 (外枠、0dB 位置の横線、および 20, 100, 1k, 10k, 20kHz の位置の縦線)
     二番目のpath は補助線用</p>
     * @param spec 通常は {@link FilterDesignDefaults#defaultPathBuilderGridPathSpec()} の値を使う
     * @return
     *
     */
    @NonNull
    public Path[] createGridPath (@NonNull GridPathSpec spec) {
        RectF canvasRect = geometry.canvasRect();

        Path primary = new Path();
        Path aux = new Path();

        // 枠線は描画領域より外側に書く
        RectF borderRect = new RectF(canvasRect);
        borderRect.inset(-0.5f, -0.5f);
        primary.addRect(borderRect, Path.Direction.CW);
        primary.moveTo(canvasRect.left - 1, 0);
        primary.lineTo(canvasRect.right + 1, 0);

        int primaryFreqIndex = 0;
        int n = 10;
        float minY = canvasRect.top;
        float maxY = canvasRect.bottom;

        for (; ; n = n * 10) {
            for (int i = 0; i < 10; i += 1) {
                int frequency = n * (i + 1);
                if (frequency < spec.minFrequency)
                    continue;

                if (frequency > spec.maxFrequency)
                    return new Path [] { primary, aux };

                boolean isPrimary = false;
                if (primaryFreqIndex < spec.primaryFrequencies.length && spec.primaryFrequencies[primaryFreqIndex] == frequency) {
                    isPrimary = true;
                    primaryFreqIndex += 1;
                }

                Path path = isPrimary ? primary : aux;
                float x = (float)geometry.computeX(frequency);
                path.moveTo(x, minY + (isPrimary ? -1f : -0.5f));
                path.lineTo(x, maxY + (isPrimary ? 1f : 0.5f));
            }
        }
    }

    @NonNull
    public Path[] createGridPath (@NonNull GridPathSpec spec, boolean translated) {
        Path [] paths = createGridPath(spec);
        if (translated) {
            translatePath(paths[0]);
            translatePath(paths[1]);
        }

        return paths;
    }

    public static class FilterSpec {
        public double cutoffFrequency;
        public int slopeRate;
        public boolean on;

        /**
         *
         * @param on filter の on/off (on = true, off = false)
         * @param cutoffFrequency カットオフ周波数 (Hz)
         * @param slopeRate スロープ (dB/oct, -6, -12, -18, -24, …)
         */
        public FilterSpec (boolean on, double cutoffFrequency, int slopeRate) {
            this.on = on;
            this.cutoffFrequency = cutoffFrequency;
            this.slopeRate = slopeRate;
        }
    }

    /**
     *
     * @param high high pass filter の spec。スピーカに high pass filter が存在しない場合は null を指定する
     * @param low low pass filter の spec。スピーカに low pass filter が存在しない場合は null を指定する
     * @param speakerLevel スピーカレベル (dB)
     * @return
     */
    public PointF[] createFilterPathControlPoints (@Nullable FilterSpec high, @Nullable FilterSpec low, int speakerLevel) {
		/*
		 * control points と ValueAnimator の組み合わせで path animation を実現させるために、
		 * 同一の speaker に対する lcontrol point 数は filter の on/off に関係なく常に同じ数にする。
		 * (直線だろうと折れ線だろうと常に 3 点、 2way Network の Mid speaker の場合は常に 4 点)
		 */

        int n = 2 + (high != null ? 1 : 0) + (low != null ? 1 : 0);
        PointF [] points = new PointF[n];

        RectF canvasRect = geometry.canvasRect();

        float y = (float)geometry.computeY(speakerLevel);
        PointF offStartPoint = new PointF(canvasRect.left, y);
        PointF offEndPoint = new PointF(canvasRect.right, y);

        int i = 0;

        if (high != null) {
            FilterType filterType = FilterType.HighPass;

            PointF cutoffPoint = geometry.computeCutoffPoint(speakerLevel, high.cutoffFrequency, high.slopeRate, filterType);
            PointF startPoint;
            if (high.on)
                startPoint = geometry.computeEndPoint(cutoffPoint, high.slopeRate, filterType);
            else
                startPoint = offStartPoint;

            points[i++] = startPoint;
            points[i++] = cutoffPoint;
        } else {
            points[i++] = offStartPoint;
        }

        if (low != null) {
            FilterType filterType = FilterType.LowPass;

            PointF cutoffPoint = geometry.computeCutoffPoint(speakerLevel, low.cutoffFrequency, low.slopeRate, filterType);
            PointF endPoint;
            if (low.on)
                endPoint = geometry.computeEndPoint(cutoffPoint, low.slopeRate, filterType);
            else
                endPoint = offEndPoint;

            points[i++] = cutoffPoint;
            points[i] = endPoint;
        } else {
            points[i] = offEndPoint;
        }

        return points;
    }

    /**
     * <p>filter の描画に使う path を作成する。Pathの原点は (10Hz,0dB) </p>
     *
     * @param high high pass filter の spec。スピーカに high pass filter が存在しない場合は null を指定する
     * @param low low pass filter の spec。スピーカに low pass filter が存在しない場合は null を指定する
     * @param speakerLevel スピーカレベル (dB)
     * @return
     */
    public Path createFilterPath (@Nullable FilterSpec high, @Nullable FilterSpec low, int speakerLevel) {
        PointF [] points = createFilterPathControlPoints(high, low, speakerLevel);
        Path path = new Path();
        return createFilterPath(path, points);
    }

    /**
     * <p>filter の描画に使う path を作成する</p>
     *
     * @param high high pass filter の spec。スピーカに high pass filter が存在しない場合は null を指定する
     * @param low low pass filter の spec。スピーカに low pass filter が存在しない場合は null を指定する
     * @param speakerLevel スピーカレベル (dB)
     * @param translated true なら path の (0, 0) がグラフの (0, 0) となる path を返す。
     *                   false であれば path の (0, 0) はグラフの (10Hz, 0dB)
     * @return
     */
    public Path createFilterPath (@Nullable FilterSpec high, @Nullable FilterSpec low, int speakerLevel, boolean translated) {
        Path path = createFilterPath(high, low, speakerLevel);
        if (translated)
            translatePath(path);
        return path;
    }

    @NonNull
    private Path createFilterPath (@NonNull Path path, @NonNull PointF [] points) {
        path.moveTo(points[0].x, points[0].y);
        for (int i = 1, n = points.length; i < n; i += 1) {
            PointF p = points[i];
            path.lineTo(p.x, p.y);
        }
        return path;
    }

    /**
     * <p>{@link #createFilterPathControlPoints(FilterSpec, FilterSpec, int)} で作成した control points から path を作成する</p>
     *
     * @param path A Path instance, which should be empty.
     * @param controlPoints {@link #createFilterPathControlPoints(FilterSpec, FilterSpec, int)} の返り値、あるいは同等の値
     * @param translated true なら path の (0, 0) がグラフの (0, 0) となる path を返す。
     *                   false であれば path の (0, 0) はグラフの (10Hz, 0dB)
     */
    public Path createFilterPath (@NonNull Path path, @NonNull PointF [] controlPoints, boolean translated) {
        createFilterPath(path, controlPoints);
        if (translated)
            translatePath(path);
        return path;
    }

    private void translatePath (@NonNull Path path) {
        Matrix m = new Matrix();
        m.setTranslate(graphSpec.origin.x, graphSpec.origin.y);
        path.transform(m);
    }
}

