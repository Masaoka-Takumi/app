package jp.pioneer.carsync.presentation.util;

import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;

import static java.lang.Math.log10;

/**
 * 周波数グラフの描画に使う path の座標等を計算するためのクラス。
 */
public class FilterGraphGeometry {

    @NonNull
    final private FilterGraphSpec graphSpec;

    public FilterGraphGeometry(@NonNull FilterGraphSpec spec) {
        this.graphSpec = new FilterGraphSpec(spec);
    }

    /**
     * @return The FilterGraphSpec object owned by the receiver.  Never modify this object.
     */
    @NonNull
    public FilterGraphSpec getGraphSpec() {
        return graphSpec;
    }

    /**
     * グラフの描画領域を返す。Rect の(0, 0) は {@link FilterGraphSpec#origin} と一致する。
     * 枠線(外周)部分は含まれないため、bitmap や view はこの rect より枠線部分の幅だけ余計にとる必要がある
     *
     * @return
     */
    @NonNull
    public RectF canvasRect() {
        RectF rect = new RectF(0, 0, graphSpec.canvasWidth, graphSpec.canvasHeight);
        rect.offsetTo(-graphSpec.origin.x, -graphSpec.origin.y);
        return rect;
    }

    /**
     * 与えられた周波数に対応する x 座標を返す。
     * (座標原点は {@link FilterGraphSpec#origin})
     *
     * @param frequency 周波数(Hz)
     * @return
     */
    public double computeX(double frequency) {
        return graphSpec.pointsPerDecade * (log10(frequency) - 1);
    }

    /**
     * @param speakerLevel スピーカレベル (dB, -24 .. +10)
     * @return
     */
    public double computeY(int speakerLevel) {
        return 0 - ((speakerLevel - graphSpec.decibelAtOrigin) * graphSpec.pointsPer10dB / 10);
    }

    final private static double log10_20 = log10(20);
    final private static double log10_10 = log10(10);

    @NonNull
    private PointF computePerOctaveOffset(int slopeRate, @NonNull FilterType filterType) {
        double widthPerOctave = graphSpec.pointsPerDecade * (log10_20 - log10_10);
        double x = widthPerOctave * (filterType == FilterType.HighPass ? -1 : 1);
        float y = -slopeRate * graphSpec.pointsPer10dB / 10;
        return new PointF((float) x, y);
    }

    /**
     * PassFilter の線が曲がる座標を返す。
     *
     * @param speakerLevel    スピーカレベル (dB, -24 .. +10)
     * @param cutoffFrequency カットオッフ周波数 (Hz)
     * @param slopeRate       スロープ値 (dB/oct, -6, -12, -18, -24, ...)
     * @param filterType
     * @return
     */
    @NonNull
    public android.graphics.PointF computeCutoffPoint(int speakerLevel, double cutoffFrequency, int slopeRate, @NonNull FilterType filterType) {
        double x = computeX(cutoffFrequency);
        double y = computeY(speakerLevel);
        // #365 No.2 の指定通りにずらす
        double adjustedY = computeY(speakerLevel - 4);

        PointF pointF = computePerOctaveOffset(slopeRate, filterType);
        double height = y - adjustedY;
        double xDelta = height * pointF.x / pointF.y;

        return new android.graphics.PointF((float) (x + xDelta), (float) y);
    }

    /**
     * PassFilter が ON の時の終点座標 (線が曲がった後で外枠にぶつかる直前の座標) を計算する
     *
     * @param cutoffPoint {@link #computeCutoffPoint(int, double, int, FilterType)} で返されたカットオフ座標
     * @param slopeRate   スロープ値。{@link #computeCutoffPoint(int, double, int, FilterType)} で使用したものと同じ値を使うこと。
     * @param filterType  フィルタの種類。{@link #computeCutoffPoint(int, double, int, FilterType)} で使用したものと同じ値を使うこと。
     * @return
     */
    public android.graphics.PointF computeEndPoint(@NonNull android.graphics.PointF cutoffPoint, int slopeRate, @NonNull FilterType filterType) {
        PointF pointF = computePerOctaveOffset(slopeRate, filterType);
        double maxY = canvasRect().bottom;
        double height = maxY - cutoffPoint.y;
        double xDelta = height * pointF.x / pointF.y;
        return new android.graphics.PointF((float) (cutoffPoint.x + xDelta), (float) maxY);
    }
}
