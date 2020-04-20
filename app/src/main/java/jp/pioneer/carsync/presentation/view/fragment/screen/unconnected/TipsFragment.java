package jp.pioneer.carsync.presentation.view.fragment.screen.unconnected;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.model.TipsItem;
import jp.pioneer.carsync.presentation.presenter.TipsPresenter;
import jp.pioneer.carsync.presentation.view.TipsView;
import jp.pioneer.carsync.presentation.view.activity.MainActivity;
import jp.pioneer.carsync.presentation.view.adapter.TipsAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * Tips画面
 */

public class TipsFragment extends AbstractScreenFragment<TipsPresenter, TipsView> implements TipsView{
    @Inject TipsPresenter mPresenter;
    @BindView(R.id.viewPager) ViewPager mViewPager;
    @BindView(R.id.tab_layout) TabLayout mTabLayout;
    @BindView(R.id.list_view) ListView mListView;
    private Unbinder mUnbinder;
    private TipsAdapter mAdapter;
    private List<TipsItem> mItems = new ArrayList<>();

    /**
     * コンストラクタ
     */
    public TipsFragment() {
    }

    /**
     * 新規インスタンス取得
     * @param args 引き継ぎ情報
     * @return TipsFragment
     */
    public static TipsFragment newInstance(Bundle args) {
        TipsFragment fragment = new TipsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(getActivity() instanceof MainActivity){
            ((MainActivity)getActivity()).showStatusBar();
        }
        View view = inflater.inflate(R.layout.fragment_tips, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        getPresenter().setArgument(getArguments());
        mTabLayout.addTab(mTabLayout.newTab().setCustomView(R.layout.element_tab_layout));
        mTabLayout.addTab(mTabLayout.newTab().setCustomView(R.layout.element_tab_layout));
        mTabLayout.addTab(mTabLayout.newTab().setCustomView(R.layout.element_tab_layout));
        mTabLayout.addTab(mTabLayout.newTab().setCustomView(R.layout.element_tab_layout));
        //socialは現バージョンでは未対応
        //mTabLayout.addTab(mTabLayout.newTab().setCustomView(R.layout.element_tab_layout));
        ImageView icon1 = (ImageView)(mTabLayout.getTabAt(0).getCustomView().findViewById(R.id.tab_icon));
        ImageView icon2 = (ImageView)(mTabLayout.getTabAt(1).getCustomView().findViewById(R.id.tab_icon));
        ImageView icon3 = (ImageView)(mTabLayout.getTabAt(2).getCustomView().findViewById(R.id.tab_icon));
        ImageView icon4 = (ImageView)(mTabLayout.getTabAt(3).getCustomView().findViewById(R.id.tab_icon));
        //ImageView icon5 = (ImageView)(mTabLayout.getTabAt(4).getCustomView().findViewById(R.id.tab_icon));
        mTabLayout.getTabAt(0).setTag("manual");
        mTabLayout.getTabAt(1).setTag("tips");
        mTabLayout.getTabAt(2).setTag("all");
        mTabLayout.getTabAt(3).setTag("information");
        icon1.setImageResource(R.drawable.tab_tips_selecter_manual);
        icon2.setImageResource(R.drawable.tab_tips_selecter_guidance);
        icon3.setImageResource(R.drawable.tab_tips_selecter_clock);
        //icon4.setImageResource(R.drawable.tab_tips_selecter_sns);
        icon4.setImageResource(R.drawable.tab_tips_selecter_information);
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mAdapter.setTagType((String)tab.getTag());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        mAdapter = new TipsAdapter(getContext());
        mAdapter.setAlexaAvailableCountry(getPresenter().isAlexaAvailableCountry());
        mListView.setAdapter(mAdapter);
       // mAdapter.setItems(mItems);
        //mTabLayout.getTabAt(2).select();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        if(getActivity() instanceof MainActivity){
            ((MainActivity)getActivity()).hideStatusBar();
        }
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected TipsPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.TIPS;
    }

    @Override
    public void setAdapter(ArrayList<TipsItem> items){
        mItems = items;
        mAdapter.setItems(mItems);
    }
    /**
     * 項目リスト選択
     *
     * @param parent   AdapterView
     * @param view     View
     * @param position int
     * @param id       選択ID
     */
    @OnItemClick(R.id.list_view)
    public void onClickListItem(AdapterView<?> parent, View view, int position, long id) {
        getPresenter().showTips(position);
    }

    @OnClick(R.id.set_button)
    public void onClickSettingButton() {
        getPresenter().onSettingAction();
    }

    @OnClick(R.id.bt_button)
    public void onClickBtButton() {
        getPresenter().onBtAction();
    }

    @Override
    public void showError(String str) {
        //Toast.makeText(getActivity(), str, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setSelectedTab(int position) {
        mTabLayout.getTabAt(position).select();
    }
    @Override
    public int getSelectedTab(){
        return mTabLayout.getSelectedTabPosition();
    }
    @Override
    public void setDisabled(boolean disabled) {
        if(disabled) {
            //mListView.setAlpha(0.4f);
        }else{
            //mListView.setAlpha(1.0f);
        }
    }
}