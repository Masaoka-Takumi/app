package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.viewpagerindicator.PageIndicator;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;

/**
 * RadioPreset用CustomLinePageIndicator
 */

public class CustomLinePageIndicatorPreset extends LinearLayout implements PageIndicator {

    private ViewPager mViewPager;
    private ViewPager.OnPageChangeListener mListener;
    private int mCurrentPage;
    private float mFrameWidth;
    private float mFrameHeight;
    private float mShortCutKeyFrameWidth;
    private float mFrameMarginRight;
    private int mColor;
    private boolean mShortCutKeyOn = false;
    public CustomLinePageIndicatorPreset(Context context) {
        this(context, null);
    }

    public CustomLinePageIndicatorPreset(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Resources res = getResources();
        mShortCutKeyFrameWidth = res.getDimension(R.dimen.custom_line_indicator_preset_frame_width);
        mFrameWidth = res.getDimension(R.dimen.custom_line_indicator_preset_frame2_width);
        mFrameHeight = res.getDimension(R.dimen.custom_line_indicator_preset_frame_height);
        mFrameMarginRight = res.getDimension(R.dimen.custom_line_indicator_preset_margin_right);
        mCurrentPage = 0;
        this.setOrientation(HORIZONTAL);
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.uiColor, outValue, true);
        mColor = outValue.resourceId;
        this.setClipChildren(false);
        this.setClipToPadding(false);
    }

    public void setShortCutKeyOn(boolean shortCutKeyOn) {
        mShortCutKeyOn = shortCutKeyOn;
    }

    public boolean isShortCutKeyOn() {
        return mShortCutKeyOn;
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
        //最後のDummyPageは表示しない
        for (int i = 0; i < count-1; i++) {
            FrameLayout frameLayout = new FrameLayout(getContext());
            LinearLayout.LayoutParams frameParams;
            ImageView image1 = new ImageView(getContext());
            MarginLayoutParams mlp1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            image1.setScaleType(ImageView.ScaleType.FIT_XY);
            ImageView image2 = new ImageView(getContext());
            MarginLayoutParams mlp2 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            image2.setScaleType(ImageView.ScaleType.FIT_XY);
            if(mShortCutKeyOn&&i == 0) {
                frameParams = new LinearLayout.LayoutParams((int) mShortCutKeyFrameWidth, (int) mFrameHeight);
                if (i == mCurrentPage) {
                    image1.setImageResource(R.drawable.p0521_pager1_on1);
                    image2.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0521_pager1_on2, mColor));
                }else{
                    image1.setImageResource(R.drawable.p0521_pager1_off);
                }
            }else{
                frameParams = new LinearLayout.LayoutParams((int) mFrameWidth, (int) mFrameHeight);
                if (i == mCurrentPage) {
                    image1.setImageResource(R.drawable.p0522_pager2_on1);
                    image2.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0522_pager2_on2, mColor));
                }else{
                    if(mShortCutKeyOn&&mCurrentPage == 0){
                        image1.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0522_pager2_off1, R.color.drawable_white_color));
                    }else {
                        image1.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0522_pager2_off1, mColor));
                    }
                    image2.setImageResource(R.drawable.p0522_pager2_off2);
                }
                mlp1.setMargins(0, 0, (int)mFrameMarginRight, 0);
                mlp2.setMargins(0, 0, (int)mFrameMarginRight, 0);
                image1.setLayoutParams(mlp1);
                image2.setLayoutParams(mlp2);
            }
            frameLayout.addView(image2);
            frameLayout.addView(image1);
            frameLayout.setLayoutParams(frameParams);

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
        CustomLinePageIndicatorPreset.SavedState savedState = (CustomLinePageIndicatorPreset.SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPage = savedState.currentPage;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        CustomLinePageIndicatorPreset.SavedState savedState = new CustomLinePageIndicatorPreset.SavedState(superState);
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
        public static final Parcelable.Creator<CustomLinePageIndicatorPreset.SavedState> CREATOR = new Parcelable.Creator<CustomLinePageIndicatorPreset.SavedState>() {
            @Override
            public CustomLinePageIndicatorPreset.SavedState createFromParcel(Parcel in) {
                return new CustomLinePageIndicatorPreset.SavedState(in);
            }

            @Override
            public CustomLinePageIndicatorPreset.SavedState[] newArray(int size) {
                return new CustomLinePageIndicatorPreset.SavedState[size];
            }
        };
    }
}
