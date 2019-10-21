package jp.pioneer.carsync.presentation.controller;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.pioneer.carsync.presentation.util.FilterDrawingUtil;
import jp.pioneer.carsync.presentation.util.FilterPathBuilder;
import jp.pioneer.carsync.presentation.view.widget.FilterGraphView;
import jp.pioneer.carsync.presentation.view.widget.FilterPathView;

public class FilterGraphViewController {

    public enum Speaker {
        /** front or high speaker */
        FrontLeft,
        /** front or high speaker */
        FrontRight,
        /** rear or middle speaker */
        RearLeft,
        /** rear or middle speaker */
        RearRight,
        /** subwoofer speaker */
        Subwoofer,
    }

    @NonNull
    private FilterPathBuilder mPathBuilder;
    @NonNull
    private FilterGraphView mLeft;
    @NonNull
    private FilterGraphView mRight;

    public FilterGraphViewController(@NonNull FilterPathBuilder pathBuilder, @NonNull FilterGraphView left, @NonNull FilterGraphView right) {
        mPathBuilder = pathBuilder;
        mLeft = left;
        mRight = right;
    }

    /**
     * @param front     front/high speaker color
     * @param rear      rear/middle speaker color
     * @param subwoofer subwoofer speaker color
     */
    public void setSpeakerColors(@ColorInt int front, @ColorInt int rear, @ColorInt int subwoofer) {
        setSpeakerColors(mLeft, front, rear, subwoofer);
        setSpeakerColors(mRight, front, rear, subwoofer);
    }

    private void setSpeakerColors(@NonNull FilterGraphView view, @ColorInt int front, @ColorInt int rear, @ColorInt int subwoofer) {
        view.getFilterViewForFrontSpeaker().setPathColor(front);
        view.getFilterViewForRearSpeaker().setPathColor(rear);
        view.getFilterViewForSubwooferSpeaker().setPathColor(subwoofer);
    }

    @Nullable
    private Speaker mCurrentSpeaker;

    /**
     * 設定中の speaker を指定
     *
     * @param speaker 設定中の speaker / 設定中の speaker が存在しない場合は null
     */
    public void setCurrentSpeaker(@Nullable Speaker speaker) {
        Speaker previous = mCurrentSpeaker;
        mCurrentSpeaker = speaker;
        makeCurrent(mLeft, previous, false);
        makeCurrent(mLeft, speaker, true);
        makeCurrent(mRight, previous, false);
        makeCurrent(mRight, speaker, true);
    }

    private void makeCurrent(@NonNull FilterGraphView graphView, @Nullable Speaker speaker, boolean isCurrent) {
        FilterPathView view = getPathView(graphView, speaker);
        if (view != null) {
            Animator animator = view.getBlinkAnimator();
            if (isCurrent)
                animator.start();
            else
                animator.end();
            view.setCurrent(isCurrent);
        }

        if (view == null && isCurrent && speaker != null) {
            view = getPathView(graphView, getOppositeSpeaker(speaker));
        }
        if (view != null && isCurrent)
            graphView.bringChildToFront(view);
    }

    @NonNull
    private Speaker getOppositeSpeaker(@NonNull Speaker speaker) {
        switch (speaker) {
            case FrontLeft:
                return Speaker.FrontRight;
            case FrontRight:
                return Speaker.FrontLeft;
            case RearLeft:
                return Speaker.RearRight;
            case RearRight:
                return Speaker.RearLeft;
            default:
                return speaker;
        }
    }

    @Nullable
    private FilterPathView getPathView(@NonNull FilterGraphView graphView, @Nullable Speaker speaker) {
        if (speaker == null)
            return null;
        switch (speaker) {
            case FrontLeft:
                if (graphView != mLeft)
                    return null;
                return graphView.getFilterViewForFrontSpeaker();
            case FrontRight:
                if (graphView != mRight)
                    return null;
                return graphView.getFilterViewForFrontSpeaker();

            case RearLeft:
                if (graphView != mLeft)
                    return null;
                return graphView.getFilterViewForRearSpeaker();
            case RearRight:
                if (graphView != mRight)
                    return null;
                return graphView.getFilterViewForRearSpeaker();

            case Subwoofer:
                return graphView.getFilterViewForSubwooferSpeaker();
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
     * {@link #(Speaker, int, Crossover, Crossover)} の内容を default animation を使って反映する
     */
    public void update() {
        update(getDefaultAnimationDuration());
    }

    private Animator mCurrentAnimation;

    /**
     * {@link #(Speaker, int, Crossover, Crossover)} の内容を反映する
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

            pathView = getPathView(mLeft, speaker);
            state = getFilterStateForSpeaker(speaker, false);
            animator = update(pathView, state, animated);
            if (animator != null) {
                animators.add(animator);
            } else {
            }

            pathView = getPathView(mRight, speaker);
            state = getFilterStateForSpeaker(speaker, false);
            animator = update(pathView, state, animated);
            if (animator != null) {
            } else {

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
        mLeft.getFilterViewForSubwooferSpeaker().setVisibility(on ? View.VISIBLE : View.GONE);
        mRight.getFilterViewForSubwooferSpeaker().setVisibility(on ? View.VISIBLE : View.GONE);
    }

}
