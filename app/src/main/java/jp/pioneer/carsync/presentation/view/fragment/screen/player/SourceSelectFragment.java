package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.acbelter.directionalcarousel.CarouselPagerAdapter;
import com.acbelter.directionalcarousel.CarouselViewPager;
import com.acbelter.directionalcarousel.page.OnPageClickListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.model.SourceSelectItem;
import jp.pioneer.carsync.presentation.presenter.SourceSelectPresenter;
import jp.pioneer.carsync.presentation.view.SourceSelectView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import timber.log.Timber;

/**
 * ソース選択の画面
 */

public class SourceSelectFragment extends AbstractScreenFragment<SourceSelectPresenter, SourceSelectView>
        implements SourceSelectView {
    private static final float SOURCE_ICON_ALPHA_MIN = 0.8f;
    private static final float SOURCE_ICON_ALPHA_MAX = 1.0f;
    private static final float SOURCE_ICON_ALPHA_DIFF = 0.2f;
    private static final float SOURCE_ICON_OVAL_ALPHA_MIN = 0.6f;
    private static final float SOURCE_ICON_OVAL_ALPHA_MAX = 0.9f;
    private static final float SOURCE_ICON_OVAL_ALPHA_DIFF = 0.3f;
    private static final float SOURCE_ICON_SELECT_ALPHA_MIN = 0.0f;
    private static final float SOURCE_ICON_SELECT_ALPHA_MAX = 1.0f;
    private static final int DELAY_TIME = 3000;
    private static final float POSITION_OFFSET_CURRENT_MIN = 0.0f;
    private static final float POSITION_OFFSET_CURRENT_MAX = 0.1f;
    private static final float POSITION_OFFSET_NEXT_MIN = 0.9f;
    private static final float POSITION_OFFSET_NEXT_MAX = 1.0f;
    @Inject SourceSelectPresenter mPresenter;
    @BindView(R.id.carousel_pager) CarouselViewPager mViewPager;
    @BindView(R.id.text_source) TextView mTextSource;
    private Unbinder mUnbinder;
    private CarouselPagerAdapter<SourceSelectItem> mPagerAdapter;
    private List<SourceSelectItem> mItems = new ArrayList<>();
    private HandlerThread mHandlerThread = null;
    private Handler mHandler = null;
    private int mCurrentPosition;
    private Runnable mDelayFunc = new Runnable() {
        @Override
        public void run() {
            getPresenter().setScrolled(false);
            if (mItems.get(mCurrentPosition).sourceType != null) {
                getPresenter().onChangeSourceAction(mItems.get(mCurrentPosition).sourceType);
            } else if (mItems.get(mCurrentPosition).appLabelName != null) {
                startMusicApp();
            }
            mHandlerThread.quitSafely();
        }
    };

    public SourceSelectFragment() {
    }

    public static SourceSelectFragment newInstance(Bundle args) {
        SourceSelectFragment fragment = new SourceSelectFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_source_select, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        Configuration config = getResources().getConfiguration();
        if(config.orientation==Configuration.ORIENTATION_LANDSCAPE){
            mViewPager.setPageContentHeight(getResources().getDimensionPixelSize(R.dimen.source_select_page_content_height));
            mViewPager.setPageContentWidth(getResources().getDimensionPixelSize(R.dimen.source_select_page_content_width));
        }else{
            mViewPager.setPageContentHeight(getResources().getDimensionPixelSize(R.dimen.source_select_page_content_height));
            mViewPager.setPageContentWidth(getResources().getDimensionPixelSize(R.dimen.source_select_page_content_width_portrait));
        }
        view.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                getPresenter().onBackAction();
                return true;
            }
            return false;
        });
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        mHandlerThread = new HandlerThread("background-thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        //画面回転後に3秒後選択する対応
        if(getPresenter().isScrolled()){
            //ここでRunnableをnewしないと動かない
            mHandler.postDelayed(() -> {
                getPresenter().setScrolled(false);
                if (mItems.get(mCurrentPosition).sourceType != null) {
                    getPresenter().onChangeSourceAction(mItems.get(mCurrentPosition).sourceType);
                } else if (mItems.get(mCurrentPosition).appLabelName != null) {
                    startMusicApp();
                }
                mHandlerThread.quitSafely();
            }, DELAY_TIME);
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandlerThread.quitSafely();
        mHandler.removeCallbacks(mDelayFunc);
        mUnbinder.unbind();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected SourceSelectPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.SOURCE_SELECT;
    }

    /**
     * ソース選択リストの設定
     *
     * @param selectItems ソース選択リスト
     */
    @Override
    public void setAdapter(List<SourceSelectItem> selectItems) {
        mItems = selectItems;
        FragmentManager fragmentManager = getChildFragmentManager();
        mPagerAdapter = new CarouselPagerAdapter<>(fragmentManager,
                SourcePageFragment.class, R.layout.element_carousel_layout, mItems);

        mViewPager.setAdapter(mPagerAdapter);
        mCurrentPosition = mViewPager.getCurrentItem();
        mPagerAdapter.setOnPageClickListener(new OnPageClickListener<SourceSelectItem>() {
            @Override
            public void onSingleTap(View view, SourceSelectItem item) {
                int position = 0;
                if (item.sourceType != null) {
                    for (int i = 0; i < mItems.size(); i++) {
                        if (mItems.get(i).sourceType == item.sourceType) {
                            position = i;
                            break;
                        }
                    }
                    if (position == mCurrentPosition) {
                        getPresenter().setScrolled(false);
                        getPresenter().onChangeSourceAction(item.sourceType);
                    }else{
                        getPresenter().setScrolled(true);
                    }
                } else if (item.appLabelName != null) {
                    for (int i = 0; i < mItems.size(); i++) {
                        if (mItems.get(i).appLabelName != null) {
                            if (mItems.get(i).appLabelName.equals(item.appLabelName)) {
                                position = i;
                                break;
                            }
                        }
                    }
                    if (position == mCurrentPosition) {
                        getPresenter().setScrolled(false);
                        startMusicApp();
                    }else{
                        getPresenter().setScrolled(true);
                    }
                }
                setCurrentSource(position);
            }

            @Override
            public void onDoubleTap(View view, SourceSelectItem item) {

            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (mItems.get(position).sourceType != null) {
                    mTextSource.setText(mItems.get(position).sourceTypeName);
                } else if (mItems.get(position).appLabelName != null) {
                    mTextSource.setText(mItems.get(position).appLabelName);
                }
                mCurrentPosition = position;
                getPresenter().setCurrentPosition(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

                SourcePageFragment current = (SourcePageFragment) mPagerAdapter.getPageFragment(position);
                SourcePageFragment next = (SourcePageFragment) mPagerAdapter.getPageFragment(position + 1);

                if (current != null) {
                    current.setIconAlpha(SOURCE_ICON_ALPHA_MAX - SOURCE_ICON_ALPHA_DIFF * positionOffset);
                    current.setIconOvalAlpha(SOURCE_ICON_OVAL_ALPHA_MAX - SOURCE_ICON_OVAL_ALPHA_DIFF * positionOffset);
                    if (positionOffset >= POSITION_OFFSET_CURRENT_MIN && positionOffset <= POSITION_OFFSET_CURRENT_MAX) {
                        current.setIconSelectAlpha(SOURCE_ICON_SELECT_ALPHA_MAX);
                    } else {
                        current.setIconSelectAlpha(SOURCE_ICON_SELECT_ALPHA_MIN);
                    }
                }

                if (next != null) {
                    next.setIconAlpha(SOURCE_ICON_ALPHA_MIN + SOURCE_ICON_ALPHA_DIFF * positionOffset);
                    next.setIconOvalAlpha(SOURCE_ICON_OVAL_ALPHA_MIN + SOURCE_ICON_OVAL_ALPHA_DIFF * positionOffset);
                    if (positionOffset >= POSITION_OFFSET_NEXT_MIN && positionOffset <= POSITION_OFFSET_NEXT_MAX) {
                        next.setIconSelectAlpha(SOURCE_ICON_SELECT_ALPHA_MAX);
                    } else {
                        next.setIconSelectAlpha(SOURCE_ICON_SELECT_ALPHA_MIN);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //ページスクロール完了
                if (state == CarouselViewPager.SCROLL_STATE_IDLE) {
                    //3秒後に実行
                    mHandler.postDelayed(mDelayFunc, DELAY_TIME);
                }
                //ドラッグ開始
                else if (state == CarouselViewPager.SCROLL_STATE_DRAGGING) {
                    mHandler.removeCallbacks(mDelayFunc);
                    getPresenter().setScrolled(true);
                }
            }
        });
    }

    /**
     * 現在のソース設定
     *
     * @param position 選択ソースの位置
     */
    @Override
    public void setCurrentSource(int position) {
        if (mViewPager != null) {
            if (mViewPager.getCurrentItem() != position) {
                mHandler.removeCallbacks(mDelayFunc);
                //smoothScrollがTrueだとスクロール位置がずれることがある
                mViewPager.setCurrentItem(position,false);
                for(int i = 0;i<mPagerAdapter.getCount();i++){
                    SourcePageFragment page = (SourcePageFragment) mPagerAdapter.getPageFragment(i);
                    if(page!=null) {
                        page.setIconSelectAlpha(SOURCE_ICON_SELECT_ALPHA_MIN);
                        page.setIconAlpha(SOURCE_ICON_ALPHA_MIN);
                        page.setIconOvalAlpha(SOURCE_ICON_OVAL_ALPHA_MIN);
                    }
                }
                SourcePageFragment current = (SourcePageFragment) mPagerAdapter.getPageFragment(position);
                if (current != null) {
                    current.setIconSelectAlpha(SOURCE_ICON_SELECT_ALPHA_MAX);
                    current.setIconAlpha(SOURCE_ICON_ALPHA_MAX);
                    current.setIconOvalAlpha(SOURCE_ICON_OVAL_ALPHA_MAX);
                }
                //3秒後に実行
                if(getPresenter().isScrolled()) {
                    mHandler.postDelayed(mDelayFunc, DELAY_TIME);
                }
            }
        }
        if(mTextSource != null) {
            if (mItems.get(position).sourceType != null) {
                mTextSource.setText(mItems.get(position).sourceTypeName);
            } else if (mItems.get(position).appLabelName != null) {
                mTextSource.setText(mItems.get(position).appLabelName);
            }
        }
    }

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    @Override
    public void setColor(@ColorRes int color) {
        SourcePageFragment.setColor(color);
    }

    /**
     * ダイアログ消去
     */
    @Override
    public void dismissDialog() {
        getPresenter().setScrolled(false);
        if (isResumed() && getParentFragment() != null) ((SourceSelectContainerFragment) getParentFragment()).dismiss();
    }

    @OnClick(R.id.icon_customize)
    public void onClickCustomizeButton() {
        getPresenter().onCustomizeAction();
    }

    @OnClick(R.id.close_button)
    public void onClickCloseButton() {
        dismissDialog();
    }

    private void startMusicApp(){
        String packageName = mItems.get(mCurrentPosition).appPackageName;
        try {
            PackageManager pm = getContext().getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(packageName);
            startActivity(intent);
            getPresenter().onSelectAudioAppAction();
        } catch (ActivityNotFoundException | NullPointerException ex){
            Timber.w("Invalid music app. package name:%s" + packageName);
            Toast.makeText(getContext(), getString(R.string.err_017), Toast.LENGTH_LONG).show();
            getPresenter().onBackAction();
        }
    }
}
