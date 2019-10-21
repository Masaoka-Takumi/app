package jp.pioneer.carsync.presentation.view.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.pioneer.carsync.presentation.util.FilterDrawingUtil;
import jp.pioneer.carsync.presentation.util.FilterGraphGeometry;
import jp.pioneer.carsync.presentation.util.FilterPathBuilder;
import timber.log.Timber;

/**
 * Created by NSW00_906320 on 2017/07/28.
 */

public class JasperFilterGraphViewController {
    public enum Speaker {
        FrontRear,
        Subwoofer,
    }

    @NonNull
    private Context mContext;
    @NonNull
    private FilterPathBuilder mPathBuilder;
    @NonNull
    private FilterGraphGeometry mGraphGeometry;
    @NonNull
    private FilterGraphView mGraphView;


    public JasperFilterGraphViewController(@NonNull Context context, @NonNull FilterPathBuilder pathBuilder, @NonNull FilterGraphGeometry graphGeometry, @NonNull FilterGraphView graphView) {
        mContext = context;
        mPathBuilder = pathBuilder;
        mGraphGeometry = graphGeometry;
        mGraphView = graphView;

    }

    /**
     * @param frontRear front/rear (HPF) speaker color
     * @param subwoofer subwoofer speaker color
     */
    public void setSpeakerColors(@ColorInt int frontRear, @ColorInt int subwoofer) {
        setSpeakerColors(mGraphView, frontRear, subwoofer);
    }

    private void setSpeakerColors(@NonNull FilterGraphView view, @ColorInt int frontRear, @ColorInt int subwoofer) {
        view.getFilterViewForFrontSpeaker().setPathColor(frontRear);
        view.getFilterViewForSubwooferSpeaker().setPathColor(subwoofer);
    }

    @Nullable
    private FilterPathView getPathView(@Nullable Speaker speaker) {
        if (speaker == null)
            return null;
        switch (speaker) {
            case FrontRear:
                return mGraphView.getFilterViewForFrontSpeaker();
            case Subwoofer:
                return mGraphView.getFilterViewForSubwooferSpeaker();
        }

        return null;
    }

    public static class Crossover {
        /** HPF/LPF が on なら true */
        final public boolean on;
        /** 1/1000 Hz 単位での cutoff 周波数 */
        final public long cutoffFrequency;
        /** スロープ設定値 (index ではなく値そのもの e.g. -6, -12...) */
        final public int slopeRate;

        /**
         * @param on              HPF/LPF が on なら true
         * @param cutoffFrequency 1/1000 Hz 単位での cutoff 周波数
         * @param slopeRate       スロープ設定値 (index ではなく値そのもの e.g. -6, -12...)
         */
        public Crossover(boolean on, long cutoffFrequency, int slopeRate) {
            this.on = on;
            this.cutoffFrequency = cutoffFrequency;
            this.slopeRate = slopeRate;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Crossover) {
                Crossover other = (Crossover) obj;
                return equals(other.on, other.cutoffFrequency, other.slopeRate);
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        public boolean equals(boolean on, long cutoffFrequency, int slopeRate) {
            return this.on == on && this.cutoffFrequency == cutoffFrequency && this.slopeRate == slopeRate;
        }

        public FilterPathBuilder.FilterSpec toFilterSpec() {
            return new FilterPathBuilder.FilterSpec(on, (double) cutoffFrequency / 1000, slopeRate);
        }
    }

    private static class FilterState {
        int speakerLevel;
        @Nullable
        Crossover hpf;
        @Nullable
        Crossover lpf;

        @Nullable
        PointF[] currentPathControlPoints;

        public boolean equals(int speakerLevel, @Nullable Crossover hpf, @Nullable Crossover lpf) {
            return this.speakerLevel == speakerLevel && crossOverEquals(this.hpf, hpf) && crossOverEquals(this.lpf, lpf);
        }

        private boolean crossOverEquals(@Nullable Crossover xover1, @Nullable Crossover xover2) {
            if (xover1 == xover2)
                return true;
            if (xover1 == null || xover2 == null)
                return false;
            return xover1.equals(xover2);
        }
    }

    @NonNull
    private Map<Speaker, FilterState> states = new HashMap<>();
    @NonNull
    private Set<Speaker> mChangedSpeakers = new HashSet<>();

    public void setFilter(@NonNull Speaker speaker,
                          int speakerLevel,
                          @Nullable Crossover hpf,
                          @Nullable Crossover lpf,
                          boolean forceUpdate) {
        if ((hpf == null) && (lpf == null)) {
            return;
        }

        FilterState state = getFilterStateForSpeaker(speaker);
        if (state.equals(speakerLevel, hpf, lpf)) {
            return;
        }
        state.speakerLevel = speakerLevel;
        state.hpf = hpf;
        state.lpf = lpf;
        mChangedSpeakers.add(speaker);

        if (forceUpdate) {
            update();
        }
    }

    @NonNull
    private FilterState getFilterStateForSpeaker(@NonNull Speaker speaker) {
        //noinspection ConstantConditions
        return getFilterStateForSpeaker(speaker, true);
    }

    @Nullable
    private FilterState getFilterStateForSpeaker(@NonNull Speaker speaker, boolean create) {
        FilterState state = states.get(speaker);
        if (state == null && create) {
            state = new FilterState();
            states.put(speaker, state);
        }
        return state;
    }

    public int getDefaultAnimationDuration() {
        return 0;
    }

    /**
     * efault animation を使って反映する
     */
    public void update() {
        update(getDefaultAnimationDuration());
    }

    private Animator mCurrentAnimation;

    /**
     * 内容を反映する
     *
     * @param animationDuration ≧0 指定された値を animation duration (ミリ秒)として使用 /
     *                          &lt;0 default の animation duration を使用
     */
    public void update(int animationDuration) {
        Speaker[] changedSpeakers = mChangedSpeakers.toArray(new Speaker[mChangedSpeakers.size()]);
        mChangedSpeakers.clear();

        if (mCurrentAnimation != null) {
            if (mCurrentAnimation.isRunning())
                mCurrentAnimation.end();
            mCurrentAnimation = null;
        }

        List<Animator> animators = new ArrayList<>();
        boolean animated = animationDuration != 0;
        for (Speaker speaker : changedSpeakers) {
            Animator animator;

            FilterPathView pathView;
            FilterState state;

            pathView = getPathView(speaker);
            state = getFilterStateForSpeaker(speaker, false);
            animator = update(pathView, state, animated);
            if (animator != null) {
                animators.add(animator);
            } else {
                Timber.d("FilterGraphViewController: GOT null animator on L speaker");
            }

//            float translationY = getSpeakerLabelTranslationY(speaker, state.speakerLevel);
            if (animated) {
                animators.add(animator);
            }
        }

        if (!animators.isEmpty()) {
            AnimatorSet set = new AnimatorSet();
            set.playTogether(animators);
            if (animationDuration >= 0)
                set.setDuration(animationDuration);
            else
                set.setDuration(getDefaultAnimationDuration());
            set.start();
            mCurrentAnimation = set;
        }
    }

    @Nullable
    private Animator update(@Nullable FilterPathView pathView, @Nullable FilterState state, boolean animated) {
        if (pathView == null || state == null) {
            return null;
        }

        PointF[] currentPoints = state.currentPathControlPoints;
        PointF[] newPoints = mPathBuilder.createFilterPathControlPoints(crossoverToSpec(state.hpf), crossoverToSpec(state.lpf), state.speakerLevel);

        state.currentPathControlPoints = newPoints;

        if (currentPoints == null || !animated) {
            Path path = new Path();
            pathView.setPath(mPathBuilder.createFilterPath(path, newPoints, true));
            return null;
        } else {
            return FilterDrawingUtil.createPathAnimation(mPathBuilder, pathView, null, currentPoints, newPoints);
        }
    }

    @Nullable
    private FilterPathBuilder.FilterSpec crossoverToSpec(@Nullable Crossover xover) {
        if (xover == null)
            return null;
        return xover.toFilterSpec();
    }

    public void setSubwooferOn(boolean on) {
        mGraphView.getFilterViewForSubwooferSpeaker().setVisibility(on ? View.VISIBLE : View.GONE);
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mContext.getResources().getDisplayMetrics());
    }

    private float getSpeakerLabelTranslationY(@NonNull Speaker speaker, int speakerLevel) {
        float translation = -dpToPx(9.0f); // 画像の中央付近と線の位置をあわせる。面倒なのでサイズ決め打ち。
        translation += dpToPx(mGraphGeometry.getGraphSpec().origin.y);
        translation += dpToPx((float) mGraphGeometry.computeY(speakerLevel));
        return translation;
    }
}
