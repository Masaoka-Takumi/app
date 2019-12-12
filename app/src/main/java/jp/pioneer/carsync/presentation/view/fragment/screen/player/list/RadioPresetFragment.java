package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.DabBandType;
import jp.pioneer.carsync.domain.model.HdRadioBandType;
import jp.pioneer.carsync.domain.model.HdRadioPresetItem;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.SxmBandType;
import jp.pioneer.carsync.presentation.model.AbstractPresetItem;
import jp.pioneer.carsync.presentation.model.DabPresetItem;
import jp.pioneer.carsync.presentation.model.RadioPresetItem;
import jp.pioneer.carsync.presentation.model.SxmPresetItem;
import jp.pioneer.carsync.presentation.presenter.RadioPresetPresenter;
import jp.pioneer.carsync.presentation.view.RadioPresetView;
import jp.pioneer.carsync.presentation.view.adapter.RadioPresetPageAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.widget.CustomLinePageIndicator;

/**
 * ラジオプリセットリスト画面
 */

public class RadioPresetFragment extends AbstractScreenFragment<RadioPresetPresenter, RadioPresetView>
        implements RadioPresetView {
    private static final int PAGE_PRESET_ITEMS = 6;
    @Inject RadioPresetPresenter mPresenter;
    @BindView(R.id.viewPager) ViewPager mViewPager;
    @BindView(R.id.line_indicator) CustomLinePageIndicator mLineIndicator;
    private RadioPresetPageAdapter mAdapter;
    private Unbinder mUnbinder;
    private ArrayList<String> mBandTypeList = new ArrayList<>();
    /**
     * コンストラクタ
     */
    public RadioPresetFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return RadioPresetFragment
     */
    public static RadioPresetFragment newInstance(Bundle args) {
        RadioPresetFragment fragment = new RadioPresetFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dialog_preset_channel, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mAdapter = new RadioPresetPageAdapter(getActivity()) {
            @Override
            protected void onClickKey(int pch) {
                getPresenter().onSelectPresetNumber(pch);
            }
        };
        mAdapter.setSphCarDevice(getPresenter().isSphCarDevice());
        mViewPager.setAdapter(mAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (getParentFragment() != null) {
                    ((RadioTabContainerFragment) getParentFragment()).setTitle(mBandTypeList.get(position));
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getPresenter().setLoaderManager(LoaderManager.getInstance(this));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.RADIO_PRESET_LIST;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected RadioPresetPresenter getPresenter() {
        return mPresenter;
    }

    /**
     * プリセットリストの設定
     *
     * @param presetList プリセットリスト
     */
    @Override
    public void setPresetList(ArrayList<AbstractPresetItem> presetList) {
        mAdapter.setRadioPresetItems(presetList);
        mLineIndicator.setViewPager(mViewPager);
        //単一ページの時はインジケータ非表示
        if(mAdapter.getCount() <= 1) {
            mLineIndicator.setVisibility(View.INVISIBLE);
        }else{
            mLineIndicator.setVisibility(View.VISIBLE);
        }
        mBandTypeList.clear();
        if(presetList.size()>0) {
            if (presetList.get(0) instanceof RadioPresetItem) {
                for (int i = 0; i < presetList.size(); i = i + PAGE_PRESET_ITEMS) {
                    RadioBandType band = null;
                    StringBuilder buf = new StringBuilder();
                    for(int h = 0;h < PAGE_PRESET_ITEMS;h++){
                        if(band != ((RadioPresetItem) presetList.get(i+h)).bandType){
                            if(h!=0){
                                buf.append("/");
                            }
                            band = ((RadioPresetItem) presetList.get(i+h)).bandType;
                            buf.append(getString(band.getLabel()));
                            if((band==RadioBandType.LW||band==RadioBandType.MW)&&h!=0)break;
                        }
                    }
                    mBandTypeList.add(buf.toString());
                }
            }
            if (presetList.get(0) instanceof SxmPresetItem) {
                for (int i = 0; i < presetList.size(); i = i + PAGE_PRESET_ITEMS) {
                    SxmBandType band = null;
                    StringBuilder buf = new StringBuilder();
                    for(int h = 0;h < PAGE_PRESET_ITEMS;h++){
                        if(band != ((SxmPresetItem) presetList.get(i+h)).bandType){
                            if(h!=0){
                                buf.append("/");
                            }
                            band = ((SxmPresetItem) presetList.get(i+h)).bandType;
                            buf.append(getString(band.getLabel()));
                        }
                    }
                    mBandTypeList.add(buf.toString());
                }
            }
            if (presetList.get(0) instanceof HdRadioPresetItem) {
                for (int i = 0; i < presetList.size(); i = i + PAGE_PRESET_ITEMS) {
                    HdRadioBandType band = null;
                    StringBuilder buf = new StringBuilder();
                    for(int h = 0;h < PAGE_PRESET_ITEMS;h++){
                        if(band != ((HdRadioPresetItem) presetList.get(i+h)).bandType){
                            if(h!=0){
                                buf.append("/");
                            }
                            band = ((HdRadioPresetItem) presetList.get(i+h)).bandType;
                            buf.append(getString(band.getLabel()));
                        }
                    }
                    mBandTypeList.add(buf.toString());
                }
            }
            if (presetList.get(0) instanceof DabPresetItem) {
                for (int i = 0; i < presetList.size(); i = i + PAGE_PRESET_ITEMS) {
                    DabBandType band = null;
                    StringBuilder buf = new StringBuilder();
                    for(int h = 0;h < PAGE_PRESET_ITEMS;h++){
                        if(band != ((DabPresetItem) presetList.get(i+h)).bandType){
                            if(h!=0){
                                buf.append("/");
                            }
                            band = ((DabPresetItem) presetList.get(i+h)).bandType;
                            buf.append(getString(band.getLabel()));
                        }
                    }
                    mBandTypeList.add(buf.toString());
                }
            }
        }

    }

    @Override
    public void setSelectedPosition(int position) {
        mAdapter.setSelectedPosition(position);
        int selectedPage = position / PAGE_PRESET_ITEMS;
        if(selectedPage < mBandTypeList.size()) {
            mViewPager.setCurrentItem(selectedPage);
            if (getParentFragment() != null) {
                ((RadioTabContainerFragment) getParentFragment()).setTitle(mBandTypeList.get(selectedPage));
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
        mLineIndicator.setColor(color);
        if(mAdapter!=null) {
            mAdapter.setColor(color);
        }
    }

}
