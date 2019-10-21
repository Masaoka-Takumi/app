package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

final public class FilterGraphView extends RelativeLayout {
    final private static int FRONT = 0;
    final private static int REAR = 1;
    final private static int SUBWOOFER = 2;

    public FilterGraphView (Context context) {
        super(context);
        init();
    }

    public FilterGraphView (Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private int dpToPx (int dp) {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void init () {
        mBackgroundView = addBackgroundView();
        mPathView = new FilterPathView[] {
                addPathView(1010),
                addPathView(1011),
                addPathView(1012),
        };
    }

    private FilterGraphBackgroundView addBackgroundView () {
        FilterGraphBackgroundView view = new FilterGraphBackgroundView(getContext());
        //noinspection ResourceType
        view.setId(1000);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(view, lp);
        return view;
    }

    private FilterPathView addPathView (int id) {
        int refId = getBackgroundView().getId();
        FilterPathView view = new FilterPathView(getContext());
        view.setId(id);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(ALIGN_LEFT, refId);
        lp.addRule(ALIGN_RIGHT, refId);
        lp.addRule(ALIGN_TOP, refId);
        lp.addRule(ALIGN_BOTTOM, refId);
        int margin = dpToPx(1);
        lp.setMargins(margin, margin, margin, margin);
        addView(view, lp);
        return view;
    }

    private FilterGraphBackgroundView mBackgroundView;
    private FilterPathView mPathView [];

    @NonNull
    public FilterGraphBackgroundView getBackgroundView () {
        return mBackgroundView;
    }

    @NonNull
    public FilterPathView getFilterViewForFrontSpeaker () {
        return mPathView[FRONT];
    }

    @NonNull
    public FilterPathView getFilterViewForRearSpeaker () {
        return mPathView[REAR];
    }

    @NonNull
    public FilterPathView getFilterViewForSubwooferSpeaker () {
        return mPathView[SUBWOOFER];
    }
}
