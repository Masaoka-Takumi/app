package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.acbelter.directionalcarousel.CarouselPagerAdapter;
import com.acbelter.directionalcarousel.CarouselViewPager;
import com.acbelter.directionalcarousel.page.OnPageClickListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.model.LiveSimulationItem;
import jp.pioneer.carsync.presentation.model.VisualEffectItem;
import jp.pioneer.carsync.presentation.presenter.LiveSimulationSettingPresenter;
import jp.pioneer.carsync.presentation.view.LiveSimulationSettingView;
import jp.pioneer.carsync.presentation.view.adapter.VisualEffectAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * LiveSimulationの画面
 */

public class LiveSimulationSettingFragment extends AbstractScreenFragment<LiveSimulationSettingPresenter, LiveSimulationSettingView>
        implements LiveSimulationSettingView{
    private static final float SOURCE_ICON_SELECT_ALPHA_MIN = 0.0f;
    private static final float SOURCE_ICON_SELECT_ALPHA_MAX = 1.0f;
    private static final int REQUEST_GALLERY = 10;
    private static final float POSITION_OFFSET_CURRENT_MIN = 0.0f;
    private static final float POSITION_OFFSET_CURRENT_MAX = 0.1f;
    private static final float POSITION_OFFSET_NEXT_MIN = 0.9f;
    private static final float POSITION_OFFSET_NEXT_MAX = 1.0f;

    @Inject LiveSimulationSettingPresenter mPresenter;
    @BindView(R.id.carousel_pager) CarouselViewPager mViewPager;
    private CarouselPagerAdapter<LiveSimulationItem> mPagerAdapter;
    private ArrayList<LiveSimulationItem> mItems = new ArrayList<>();
    @BindView(R.id.effect_list) RecyclerView mEffect;
    @BindView(R.id.disable_layer) View mDisableLayer;
    private Unbinder mUnbinder;
    private VisualEffectAdapter mVisualEffectAdapter;
    private int mCurrentPosition;
    private int mCurrentIndex;
    private int mSelectedPosition;
    /**
     * コンストラクタ
     */
    public LiveSimulationSettingFragment() {
    }

    /**
     * 新規インスタンス
     *
     * @param args 引き継ぎ情報
     * @return LiveSimulationSettingFragment
     */
    public static LiveSimulationSettingFragment newInstance(Bundle args) {
        LiveSimulationSettingFragment fragment = new LiveSimulationSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_livesimulation, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        Configuration config = getResources().getConfiguration();
        if(config.orientation==Configuration.ORIENTATION_LANDSCAPE){
            int contentSize = (int)(width*0.30);
            if(contentSize>getResources().getDimensionPixelSize(R.dimen.theme_select_page_content_height)){
                contentSize = getResources().getDimensionPixelSize(R.dimen.theme_select_page_content_height);
            }
            mViewPager.setPageContentHeight(contentSize);
            mViewPager.setPageContentWidth(contentSize);
        }else{
            int contentSize = (int)(width*0.78);
            if(contentSize>getResources().getDimensionPixelSize(R.dimen.theme_select_page_content_height_portrait)){
                contentSize = getResources().getDimensionPixelSize(R.dimen.theme_select_page_content_height_portrait);
            }
            mViewPager.setPageContentHeight(contentSize);
            mViewPager.setPageContentWidth(contentSize);
        }
        // RecyclerViewの横表示を実現するために、LinearLayoutManagerを使い設定を行う。
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mEffect.setLayoutManager(manager);

        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected LiveSimulationSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.LIVE_SIMULATION_SETTING;
    }

    @Override
    public void setLiveSimulationAdapter(ArrayList<LiveSimulationItem> items) {
        mItems = items;
        FragmentManager fragmentManager = getChildFragmentManager();
        mPagerAdapter = new CarouselPagerAdapter<>(fragmentManager,
                LiveSimuPageFragment.class, R.layout.element_carousel_layout_theme, mItems);
        mViewPager.setAdapter(mPagerAdapter);
        mPagerAdapter.setOnPageClickListener(new OnPageClickListener<LiveSimulationItem>() {
            @Override
            public void onSingleTap(View view, LiveSimulationItem item) {
                int position = item.number;
                setNextItem(position);
                getPresenter().onSelectLiveSimulationAction(mCurrentIndex);
            }
            @Override
            public void onDoubleTap(View view, LiveSimulationItem item) {

            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                int index = position % mItems.size();
                if(index!=mCurrentIndex) {
                    mCurrentPosition= position;
                    mCurrentIndex = index;
                    getPresenter().onSelectLiveSimulationAction(mCurrentIndex);
                }
            }
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels){
                LiveSimuPageFragment current = (LiveSimuPageFragment)mPagerAdapter.getPageFragment(position);
                LiveSimuPageFragment next = (LiveSimuPageFragment)mPagerAdapter.getPageFragment(position + 1);
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                //ページスクロール完了
                if(state == CarouselViewPager.SCROLL_STATE_IDLE){
                }
            }
        });
    }

    @Override
    public void setVisualEffectEnabled(boolean isEnabled) {
        mEffect.setEnabled(isEnabled);
        if(isEnabled){
            mEffect.setAlpha(1.0f);
        }else{
            mEffect.setAlpha(0.5f);
        }
    }

    @Override
    public void setVisualEffectAdapter(final List<VisualEffectItem> items) {
        mVisualEffectAdapter = new VisualEffectAdapter(getContext(), items, (v, p) -> getPresenter().onSelectVisualEffectAction(p));
        mEffect.setAdapter(mVisualEffectAdapter);
    }

    @Override
    public void setVisualEffectSelectedIndex(int position) {
        mVisualEffectAdapter.setSelectedIndex(position);
    }

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    @Override
    public void setColor(@ColorRes int color) {
        mVisualEffectAdapter.setColor(color);
    }

    @Override
    public void setNextItem(int position){
        int currentPosition = mViewPager.getCurrentItem();
        int nextPosition;
        if(position > currentPosition%mItems.size()){
            if(position==mItems.size()-1 && currentPosition%mItems.size() == 0){
                nextPosition = currentPosition - 1;
            }else {
                nextPosition = currentPosition + position - currentPosition%mItems.size();
            }
        }else if(position < currentPosition%mItems.size()){
            if(position == 0 && currentPosition%mItems.size()==mItems.size()-1){
                nextPosition = currentPosition + 1;
            }else {
                nextPosition = currentPosition- (currentPosition%mItems.size() - position);
            }
        }else{
            mCurrentIndex = position;
            mCurrentPosition=currentPosition;
            return;
        }
        mCurrentIndex = position;
        mCurrentPosition=nextPosition;
        mViewPager.setCurrentItem(nextPosition,false);
    }

    @Override
    public void setCurrentPosition(int position){
        int currentPosition = mViewPager.getCurrentItem();
        int nextPosition;
        if(position > currentPosition%mItems.size()){
            nextPosition = currentPosition + position - currentPosition%mItems.size();
        }else if(position < currentPosition%mItems.size()){
            nextPosition = currentPosition- (currentPosition%mItems.size() - position);
        }else{
            nextPosition=currentPosition;
        }
        LiveSimuPageFragment prev = (LiveSimuPageFragment)mPagerAdapter.getPageFragment(mSelectedPosition);
        if(prev!=null)prev.setItemSelectAlpha(SOURCE_ICON_SELECT_ALPHA_MIN);

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                LiveSimuPageFragment current = (LiveSimuPageFragment)mPagerAdapter.getPageFragment(nextPosition);
                if(current!=null)current.setItemSelectAlpha(SOURCE_ICON_SELECT_ALPHA_MAX);
            }
        });
        mSelectedPosition = nextPosition;
    }

    @Override
    public void setEnable(boolean isEnabled) {
        if(isEnabled) {
            mDisableLayer.setVisibility(View.GONE);
            mDisableLayer.setOnTouchListener(null);
        }else{
            mDisableLayer.setVisibility(View.VISIBLE);
            mDisableLayer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
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
                }
            });
        }
    }
}
