package jp.pioneer.carsync.presentation.util;

import android.animation.Animator;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.LinearInterpolator;

import jp.pioneer.carsync.presentation.view.widget.FilterPathView;

/**
 * 描画補助のためのクラス
 */
public class FilterDrawingUtil {

    public interface GridAndBackgroundDrawer {
        /**
         * 周波数グラフの背景 (半透明黒) と格子を canvas に描画する
         *
         * @param canvas
         * @param spec
         * @param primary 格子主線用の Path。{@link FilterPathBuilder#createGridPath(FilterPathBuilder.GridPathSpec)} の最初の要素を使う。
         * @param aux     格子補助線用の Path。{@link FilterPathBuilder#createGridPath(FilterPathBuilder.GridPathSpec)} の二番目の要素を使う。
         */
        void draw(@NonNull Canvas canvas, @NonNull FilterGraphSpec spec, @NonNull Path primary, @NonNull Path aux);
    }

    static class GridAndBackgroundDrawerImpl implements GridAndBackgroundDrawer {

        @NonNull
        private Paint mPaint;

        GridAndBackgroundDrawerImpl() {
            mPaint = createDefaultPaint();
        }

        @NonNull
        private Paint createDefaultPaint() {
            Paint paint = new Paint();
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.STROKE);
            return paint;
        }

        @Override
        public void draw(@NonNull Canvas canvas, @NonNull FilterGraphSpec spec, @NonNull Path primary, @NonNull Path aux) {
            canvas.drawARGB(128, 0, 0, 0);

            float w = spec.canvasWidth + 2; // +2 は外枠の分
            float h = spec.canvasHeight + 2; // ditto
            canvas.saveLayerAlpha(0, 0, w, h, (int) (255 * 0.3));
            canvas.translate(spec.origin.x + 1, spec.origin.y + 1);

            Paint paint = mPaint;
            paint.setColor(Color.argb((int) (0.25 / 0.6 * 255), 255, 255, 255));
            canvas.drawPath(aux, paint);

            paint.setColor(Color.WHITE);
            canvas.drawPath(primary, paint);

            canvas.restore();
        }
    }

    public static GridAndBackgroundDrawer createGridAndBackgroundDrawer() {
        return new GridAndBackgroundDrawerImpl();
    }

    public interface PassFilterPathDrawer {
        /**
         * Pass filter の線を canvas に描画する
         *
         * @param canvas
         * @param spec
         * @param path      {@link FilterPathBuilder#createFilterPath(FilterPathBuilder.FilterSpec, FilterPathBuilder.FilterSpec, int)} で作成された path を指定する
         * @param color     線の色
         * @param isCurrent true であれば線の周囲をぼかす効果を追加する
         */
        void draw(@NonNull Canvas canvas, @NonNull FilterGraphSpec spec, @NonNull Path path, @ColorInt int color, boolean isCurrent);

        /**
         * Pass filter の線を canvas に描画する。
         * この method は canvas か path のどちらかがあらかじめ {@link FilterGraphSpec#origin} だけ translate されていることを前提とする。
         *
         * @param canvas
         * @param path      {@link FilterPathBuilder#createFilterPath(FilterPathBuilder.FilterSpec, FilterPathBuilder.FilterSpec, int)} で作成された path を指定する
         * @param color     線の色
         * @param isCurrent true であれば線の周囲をぼかす効果を追加する
         */
        void draw(@NonNull Canvas canvas, @NonNull Path path, @ColorInt int color, boolean isCurrent);
    }

    static class PassFilterPathDrawerImpl implements PassFilterPathDrawer {

        @NonNull
        private Paint mPaint;

        PassFilterPathDrawerImpl() {
            mPaint = createDefaultPaint();
        }

        @NonNull
        private Paint createDefaultPaint() {
            Paint paint = new Paint();
            paint.setStrokeWidth(2);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.SQUARE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void draw(@NonNull Canvas canvas, @NonNull FilterGraphSpec spec, @NonNull Path path, @ColorInt int color, boolean isCurrent) {
            canvas.translate(spec.origin.x, spec.origin.y);
            draw(canvas, path, color, isCurrent);
        }

        @Override
        public void draw(@NonNull Canvas canvas, @NonNull Path path, @ColorInt int color, boolean isCurrent) {
            Paint paint = mPaint;
            paint.setColor(color);
            if (isCurrent) {
                paint.setShadowLayer(3, 0, 0, color);
            } else {
                paint.setShadowLayer(0, 0, 0, color);
            }
            canvas.drawPath(path, paint);
        }
    }

    @NonNull
    public static PassFilterPathDrawer createPassFilterPathDrawer() {
        return new PassFilterPathDrawerImpl();
    }

    /**
     * Pass Filter を描画している view にデザイン指定の「選択中の点滅 animation」を適用するための animator を作成する
     *
     * @param view
     * @return
     */
    public static ObjectAnimator createAnimatorToBlinkFilterPassView(@NonNull View view) {
        Keyframe[] keyFrames = new Keyframe[]{
                Keyframe.ofFloat(0.0f, 1.0f),
                Keyframe.ofFloat(0.5f, 1.0f),
                Keyframe.ofFloat(0.75f, 0.0f),
                Keyframe.ofFloat(1.0f, 1.0f),
        };
        PropertyValuesHolder holder = PropertyValuesHolder.ofKeyframe(View.ALPHA, keyFrames);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, holder);
        animator.setDuration(2000);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        return animator;
    }

    /**
     * 与えられた周波数の配列に対応する x 座標の配列を返す。
     * (座標原点は {@link FilterGraphSpec#origin})
     *
     * @param geom
     * @param frequencies
     * @return
     */
    public static int[] frequencyLocations(@NonNull FilterGraphGeometry geom, int[] frequencies) {
        int[] locations = new int[frequencies.length];
        for (int i = 0, n = frequencies.length; i < n; i += 1) {
            locations[i] = (int) (geom.computeX(frequencies[i]) + geom.getGraphSpec().origin.x);
        }

        return locations;
    }

    public static Animator createPathAnimation(@NonNull final FilterPathBuilder pathBuilder, @NonNull final FilterPathView view, @Nullable Path workPath, @NonNull PointF[] start, @NonNull PointF[] end) {
        final Path path = workPath != null ? workPath : new Path();
        ValueAnimator animator = ValueAnimator.ofObject(new PointFArrayEvaluator(), start, end);
        animator.addUpdateListener(animation -> {
            path.rewind();
            pathBuilder.createFilterPath(path, (PointF[]) animation.getAnimatedValue(), true);
            view.setPath(new Path(path));
        });

        animator.setDuration(150);
        animator.setInterpolator(new LinearInterpolator());
        return animator;
    }
}
