package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.viewpagerindicator.PageIndicator;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;

/**
 * HOME用CustomLinePageIndicator
 */

public class CustomLinePageIndicator extends LinearLayout implements PageIndicator {

    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mListener;
    private int mCurrentPage;
    private float mFrameWidth;
    private float mFrameHeight;
    private float mLineWidth;
    private float mLineHeight;
    private float mLineBackMarginLeft;
    private float mLineBackMarginTop;
    private int mColor;

    public CustomLinePageIndicator(Context context) {
        this(context, null);
    }

    public CustomLinePageIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Resources res = getResources();
        mFrameWidth = res.getDimension(R.dimen.custom_line_indicator_frame_width);
        mFrameHeight = res.getDimension(R.dimen.custom_line_indicator_frame_height);
        mLineWidth = res.getDimension(R.dimen.custom_line_indicator_line_width);
        mLineHeight = res.getDimension(R.dimen.custom_line_indicator_stroke_width);
        mLineBackMarginLeft = res.getDimension(R.dimen.custom_line_indicator_line_back_margin_left);
        mLineBackMarginTop = res.getDimension(R.dimen.custom_line_indicator_line_back_margin_top);
        mCurrentPage = 0;
        this.setOrientation(HORIZONTAL);
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.uiColor, outValue, true);
        mColor = outValue.resourceId;
    }

    public void setColor(int color) {
        mColor = color;
    }

    private void updateView() {

        if (mViewPager == null) {
            return;
        }
        final int count = mViewPager.getAdapter().getCount();
        if (count == 0) {
            return;
        }

        if (mCurrentPage >= count) {
            setCurrentItem(count - 1);
            return;
        }
        this.removeAllViews();

        for (int i = 0; i < count; i++) {
            FrameLayout frameLayout = new FrameLayout(getContext());
            LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams((int) mFrameWidth, (int) mFrameHeight, Gravity.CENTER);
            frameLayout.setLayoutParams(frameParams);

            View line = new View(getContext());
            FrameLayout.LayoutParams lineParams = new FrameLayout.LayoutParams((int) mLineWidth, (int) mLineHeight, Gravity.CENTER);
            line.setLayoutParams(lineParams);
            line.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.drawable_white_color));
            if (i == mCurrentPage) {
                ImageView backImage = new ImageView(getContext());
                MarginLayoutParams mlp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                mlp.setMargins((int) mLineBackMarginLeft, (int) mLineBackMarginTop, 0, 0);
                backImage.setLayoutParams(mlp);
                backImage.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0170_pageselect, mColor));
                backImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                frameLayout.addView(backImage);
                frameLayout.addView(line);
            } else {
                line.setAlpha(0.2f);
                frameLayout.addView(line);
            }
            this.addView(frameLayout);
        }
    }

    @Override
    public void setViewPager(ViewPager view) {
        if (mViewPager == view) {
            return;
        }
        if (mViewPager != null) {
            //Clear us from the old pager.
            mViewPager.clearOnPageChangeListeners();
        }
        if (view.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        mViewPager = view;
        mViewPager.addOnPageChangeListener(this);
        updateView();
    }

    @Override
    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(view);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int item) {
        if (mViewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        mViewPager.setCurrentItem(item);
        mCurrentPage = item;
        updateView();
    }

    @Override
    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void notifyDataSetChanged() {
        updateView();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mListener != null) {
            mListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPage = position;
        updateView();

        if (mListener != null) {
            mListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (mListener != null) {
            mListener.onPageScrollStateChanged(state);
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        CustomLinePageIndicator.SavedState savedState = (CustomLinePageIndicator.SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPage = savedState.currentPage;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        CustomLinePageIndicator.SavedState savedState = new CustomLinePageIndicator.SavedState(superState);
        savedState.currentPage = mCurrentPage;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPage;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPage = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPage);
        }

        @SuppressWarnings("UnusedDeclaration")
        public static final Parcelable.Creator<CustomLinePageIndicator.SavedState> CREATOR = new Parcelable.Creator<CustomLinePageIndicator.SavedState>() {
            @Override
            public CustomLinePageIndicator.SavedState createFromParcel(Parcel in) {
                return new CustomLinePageIndicator.SavedState(in);
            }

            @Override
            public CustomLinePageIndicator.SavedState[] newArray(int size) {
                return new CustomLinePageIndicator.SavedState[size];
            }
        };
    }
}
