package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.acbelter.directionalcarousel.CarouselPagerAdapter;
import com.acbelter.directionalcarousel.CarouselViewPager;
import com.acbelter.directionalcarousel.page.OnPageClickListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.model.IlluminationColorModel;
import jp.pioneer.carsync.presentation.model.ThemeSelectItem;
import jp.pioneer.carsync.presentation.presenter.ThemeSetPresenter;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;
import jp.pioneer.carsync.presentation.view.ThemeSetView;
import jp.pioneer.carsync.presentation.view.activity.MainActivity;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.BackgroundImagePreviewFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import timber.log.Timber;

/**
 * テーマセット設定画面
 */

public class ThemeSetFragment extends AbstractScreenFragment<ThemeSetPresenter, ThemeSetView>
        implements ThemeSetView, BackgroundImagePreviewFragment.Callback {
    private static final float SOURCE_ICON_SELECT_ALPHA_MIN = 0.0f;
    private static final float SOURCE_ICON_SELECT_ALPHA_MAX = 1.0f;
    private static final float POSITION_OFFSET_CURRENT_MIN = 0.0f;
    private static final float POSITION_OFFSET_CURRENT_MAX = 0.1f;
    private static final float POSITION_OFFSET_NEXT_MIN = 0.9f;
    private static final float POSITION_OFFSET_NEXT_MAX = 1.0f;
    private static final int DELAY_TIME = 500;
    @Inject ThemeSetPresenter mPresenter;
    @BindView(R.id.disp_color_group) RelativeLayout mDispColorGroup;
    @BindView(R.id.key_color_group) RelativeLayout mKeyColorGroup;
    @BindView(R.id.disp_color_image_group) RelativeLayout mDispColorImageGroup;
    @BindView(R.id.key_color_image_group) RelativeLayout mKeyColorImageGroup;
    @BindView(R.id.ui_color_image_group) RelativeLayout mUiColorImageGroup;
    @BindView(R.id.carousel_pager) CarouselViewPager mViewPager;
    @BindView(R.id.disable_layer) View mDisableLayer;
    private CarouselPagerAdapter<ThemeSelectItem> mPagerAdapter;
    private ArrayList<ThemeSelectItem> mItems = new ArrayList<>();
    private int mCurrentPosition;
    private int mCurrentIndex;
    private Unbinder mUnbinder;
    private boolean mIsCustom;
    private int mColor;
    private int mState;
    private Handler mHandler = new Handler();
    private Runnable mDelayFunc = new Runnable() {
        @Override
        public void run() {
            if (mState == CarouselViewPager.SCROLL_STATE_IDLE) {
                if(mDisableLayer.getVisibility()!=View.VISIBLE) {
                    getPresenter().onSelectThemeAction(mCurrentIndex);
                }
                ThemePageFragment current = (ThemePageFragment) mPagerAdapter.getPageFragment(mCurrentPosition);
                if (current != null) {
                    current.setUiColor(mColor);
                    current.setItemSelectAlpha(SOURCE_ICON_SELECT_ALPHA_MAX);
                }
            } else {
                mHandler.postDelayed(this, DELAY_TIME);
            }
        }
    };

    /**
     * コンストラクタ
     */
    public ThemeSetFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return ThemeSetFragment
     */
    public static ThemeSetFragment newInstance(Bundle args) {
        ThemeSetFragment fragment = new ThemeSetFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_theme_set, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            int contentSize = (int) (width * 0.315);
            if (contentSize > getResources().getDimensionPixelSize(R.dimen.theme_select_page_content_height)) {
                contentSize = getResources().getDimensionPixelSize(R.dimen.theme_select_page_content_height);
            }
            mViewPager.setPageContentHeight(contentSize);
            mViewPager.setPageContentWidth(contentSize);
        } else {
            int contentSize = (int) (width * 0.78);
            if (contentSize > getResources().getDimensionPixelSize(R.dimen.theme_select_page_content_height_portrait)) {
                contentSize = getResources().getDimensionPixelSize(R.dimen.theme_select_page_content_height_portrait);
            }
            mViewPager.setPageContentHeight(contentSize);
            mViewPager.setPageContentWidth(contentSize);
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacks(mDelayFunc);
        mUnbinder.unbind();
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.THEME_SET_SETTING;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected ThemeSetPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setAdapter(ArrayList<ThemeSelectItem> items) {
        mItems = items;
        FragmentManager fragmentManager = getChildFragmentManager();
        mPagerAdapter = new CarouselPagerAdapter<>(fragmentManager,
                ThemePageFragment.class, R.layout.element_carousel_layout_theme, mItems);
        mViewPager.setAdapter(mPagerAdapter);
        mPagerAdapter.setOnPageClickListener(new OnPageClickListener<ThemeSelectItem>() {
            @Override
            public void onSingleTap(View view, ThemeSelectItem item) {
                int position = item.number;
                mIsCustom = false;
                setNextItem(position);
            }

            @Override
            public void onDoubleTap(View view, ThemeSelectItem item) {

            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                int index = position % mItems.size();
                if (index != mCurrentIndex) {
                    mIsCustom = false;
                    ThemePageFragment current = (ThemePageFragment) mPagerAdapter.getPageFragment(mCurrentPosition);
                    if (current != null) {
                        current.setCustomText(mIsCustom);
                        current.setItemSelectAlpha(SOURCE_ICON_SELECT_ALPHA_MIN);
                    }
                    mCurrentPosition = position;
                    mCurrentIndex = index;
                    mHandler.removeCallbacks(mDelayFunc);
                    mHandler.postDelayed(mDelayFunc, DELAY_TIME);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
                // nothing to do
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mState = state;
            }
        });
    }

    public void setNextItem(int position) {
        int currentPosition = mViewPager.getCurrentItem();
        int nextPosition;
        if (position > currentPosition % mItems.size()) {
            if (position == mItems.size() - 1 && currentPosition % mItems.size() == 0) {
                nextPosition = currentPosition - 1;
            } else {
                nextPosition = currentPosition + position - currentPosition % mItems.size();
            }
        } else if (position < currentPosition % mItems.size()) {
            if (position == 0 && currentPosition % mItems.size() == mItems.size() - 1) {
                nextPosition = currentPosition + 1;
            } else {
                nextPosition = currentPosition - (currentPosition % mItems.size() - position);
            }
        } else {
            //真ん中のItemタップでは何もしない
            mCurrentIndex = position;
            mCurrentPosition = currentPosition;
            return;
        }
        ThemePageFragment prev2 = (ThemePageFragment) mPagerAdapter.getPageFragment(nextPosition - 2);
        ThemePageFragment prev1 = (ThemePageFragment) mPagerAdapter.getPageFragment(nextPosition - 1);
        ThemePageFragment current = (ThemePageFragment) mPagerAdapter.getPageFragment(nextPosition);
        ThemePageFragment next1 = (ThemePageFragment) mPagerAdapter.getPageFragment(nextPosition + 1);
        ThemePageFragment next2 = (ThemePageFragment) mPagerAdapter.getPageFragment(nextPosition + 2);
        if (prev2 != null) prev2.setItemSelectAlpha(SOURCE_ICON_SELECT_ALPHA_MIN);
        if (prev1 != null) prev1.setItemSelectAlpha(SOURCE_ICON_SELECT_ALPHA_MIN);
        if (current != null) current.setItemSelectAlpha(SOURCE_ICON_SELECT_ALPHA_MAX);
        if (next1 != null) next1.setItemSelectAlpha(SOURCE_ICON_SELECT_ALPHA_MIN);
        if (next2 != null) next2.setItemSelectAlpha(SOURCE_ICON_SELECT_ALPHA_MIN);
        ThemePageFragment first = (ThemePageFragment) mPagerAdapter.getPageFragment(mCurrentPosition);
        if (first != null) first.setCustomText(mIsCustom);
        mCurrentIndex = position;
        mCurrentPosition = nextPosition;
        mViewPager.setCurrentItem(nextPosition, false);
        getPresenter().onSelectThemeAction(mCurrentIndex);
    }

    @Override
    public void setCurrentItem(int position) {
        int currentPosition = mViewPager.getCurrentItem();
        int nextPosition;

        if (position > currentPosition % mItems.size()) {
            nextPosition = currentPosition + position - currentPosition % mItems.size();
        } else if (position < currentPosition % mItems.size()) {
            nextPosition = currentPosition - (currentPosition % mItems.size() - position);
        } else {
            nextPosition = currentPosition;
        }
        mCurrentIndex = position;
        mCurrentPosition = nextPosition;
        mViewPager.setCurrentItem(nextPosition, false);
        Handler handler = new Handler();
        handler.post(() -> {
            ThemePageFragment current = (ThemePageFragment) mPagerAdapter.getPageFragment(mCurrentPosition);
            if (current != null) {
                current.setUiColor(mColor);
                current.setItemSelectAlpha(SOURCE_ICON_SELECT_ALPHA_MAX);
            }
        });
    }

    @Override
    public void setDispColorSettingEnabled(boolean enabled) {
        if (enabled) {
            mDispColorGroup.setAlpha(1.0f);
        } else {
            mDispColorGroup.setAlpha(0.4f);
        }
    }

    @Override
    public void setKeyColorSettingEnabled(boolean enabled) {
        if (enabled) {
            mKeyColorGroup.setAlpha(1.0f);
        } else {
            mKeyColorGroup.setAlpha(0.4f);
        }
    }

    @Override
    public void setDispColor(IlluminationColorModel disp) {
        ((ImageView) mDispColorImageGroup.getChildAt(1)).setImageDrawable(ImageViewUtil.setTintColor(getContext(),
                R.drawable.p0136_colobtnrlineselect_1nrm, disp.red.getValue() * 255 / 60, disp.green.getValue() * 255 / 60, disp.blue.getValue() * 255 / 60));
        ((ImageView) mDispColorImageGroup.getChildAt(2)).setImageDrawable(ImageViewUtil.setTintColor(getContext(),
                R.drawable.p0135_colorbtnselect_1nrm, disp.red.getValue() * 255 / 60, disp.green.getValue() * 255 / 60, disp.blue.getValue() * 255 / 60));
    }

    @Override
    public void setKeyColor(IlluminationColorModel key) {
        ((ImageView) mKeyColorImageGroup.getChildAt(1)).setImageDrawable(ImageViewUtil.setTintColor(getContext(),
                R.drawable.p0136_colobtnrlineselect_1nrm, key.red.getValue() * 255 / 60, key.green.getValue() * 255 / 60, key.blue.getValue() * 255 / 60));
        ((ImageView) mKeyColorImageGroup.getChildAt(2)).setImageDrawable(ImageViewUtil.setTintColor(getContext(),
                R.drawable.p0135_colorbtnselect_1nrm, key.red.getValue() * 255 / 60, key.green.getValue() * 255 / 60, key.blue.getValue() * 255 / 60));
    }

    @Override
    public void setUIColor(int ui) {
        ((ImageView) mUiColorImageGroup.getChildAt(1)).setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0136_colobtnrlineselect_1nrm, ui));
        ((ImageView) mUiColorImageGroup.getChildAt(2)).setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0135_colorbtnselect_1nrm, ui));
        mColor = ui;
        ThemePageFragment current = (ThemePageFragment) mPagerAdapter.getPageFragment(mCurrentPosition);
        if (current != null) current.setUiColor(ui);
    }

    @Override
    public void setCustom(boolean isCustom) {
        mIsCustom = isCustom;
        Handler handler = new Handler();
        handler.post(() -> {
            ThemePageFragment current = (ThemePageFragment) mPagerAdapter.getPageFragment(mCurrentPosition);
            if (current != null) current.setCustomText(mIsCustom);
        });
    }

    @Override
    public void setTheme(int theme) {
        getActivity().setTheme(theme);
    }

    @Override
    public void setEnable(boolean isEnabled) {
        if (isEnabled) {
            mDisableLayer.setVisibility(View.GONE);
            mDisableLayer.setOnTouchListener(null);
        } else {
            mDisableLayer.setVisibility(View.VISIBLE);
            mDisableLayer.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //some code....
                        break;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        break;
                    default:
                        break;
                }
                return true;
            });
        }
    }

    @Override
    public void onSet(BackgroundImagePreviewFragment fragment) {
        if (getPresenter().setMyPhotoEnabled()) {
            int currentPosition = mViewPager.getCurrentItem();
            int myPhotoIndex = getPresenter().getMyPhotoIndex();
            if (currentPosition % mItems.size() != myPhotoIndex) {
                if (mDisableLayer.getVisibility() != View.VISIBLE) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setNextItem(myPhotoIndex);
                        }
                    });
                }
            }
            InputStream in = null;
            try {
                Timber.d("changeBackgroundBitmap");
                in = getActivity().openFileInput("myPhoto.jpg");
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                ((MainActivity) getActivity()).changeBackgroundBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public void onClose(BackgroundImagePreviewFragment fragment) {

    }
}
