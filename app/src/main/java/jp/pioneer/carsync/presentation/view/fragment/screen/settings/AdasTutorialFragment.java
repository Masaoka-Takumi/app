package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.AdasTutorialPresenter;
import jp.pioneer.carsync.presentation.view.AdasTutorialView;
import jp.pioneer.carsync.presentation.view.adapter.AdasTutorialPagerAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * AdasTutorialFragment
 */

public class AdasTutorialFragment extends AbstractScreenFragment<AdasTutorialPresenter, AdasTutorialView>
        implements AdasTutorialView {
    @Inject AdasTutorialPresenter mPresenter;
    @BindView(R.id.viewPager) ViewPager mViewPager;
    @BindView(R.id.back_button) ImageView BackBtn;
    @BindView(R.id.indicator) CirclePageIndicator mCircleIndicator;
    @BindView(R.id.skip_btn) TextView mSkipBtn;
    @BindView(R.id.next_btn) TextView mNextBtn;
    private Unbinder mUnbinder;
    private AdasTutorialPagerAdapter mAdapter;
    public AdasTutorialFragment() {
    }

    public static AdasTutorialFragment newInstance(Bundle args) {
        AdasTutorialFragment fragment = new AdasTutorialFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adas_tutorial, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mAdapter = new AdasTutorialPagerAdapter(getActivity());
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(mViewPager.getCurrentItem()==mAdapter.getCount()-1){
                    mSkipBtn.setVisibility(View.INVISIBLE);
                }else{
                    mSkipBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mCircleIndicator.setViewPager(mViewPager);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.ADAS_TUTORIAL;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected AdasTutorialPresenter getPresenter() {
        return mPresenter;
    }

    /**
     * Back押下イベント
     */
    @OnClick(R.id.back_button)
    public void onClickBackBtn() {
        getPresenter().onBackAction();
    }

    /**
     * Skip押下イベント
     */
    @OnClick(R.id.skip_btn)
    public void onClickSkipBtn() {
        getPresenter().onSkipAction();
    }

    /**
     * Next押下イベント
     */
    @OnClick(R.id.next_btn)
    public void onClickNextBtn() {
        if(mViewPager.getCurrentItem()==mAdapter.getCount()-1) {
            getPresenter().onSkipAction();
        }else {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem()+1);
        }
    }
}
