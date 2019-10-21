package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.SoundFxSettingEqualizerType;
import jp.pioneer.carsync.presentation.presenter.EqSettingPresenter;
import jp.pioneer.carsync.presentation.view.EqSettingView;
import jp.pioneer.carsync.presentation.view.adapter.EqSettingAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.widget.EqSettingGraphView;
import timber.log.Timber;

/**
 * PresetEQ設定の画面
 */

public class EqSettingFragment extends AbstractScreenFragment<EqSettingPresenter, EqSettingView> implements EqSettingView {

    @Inject EqSettingPresenter mPresenter;

    @BindView(R.id.quick_set) RelativeLayout mQuickSet;
    @BindView(R.id.pro_set) RelativeLayout mProSet;
    @BindView(R.id.list_view) ListView mListView;
    @BindView(R.id.graph_view) EqSettingGraphView mGraphView;
    @BindView(R.id.custom_view) RelativeLayout mCustomView;
    @BindView(R.id.preset_view) ImageView mPresetView;
    private Unbinder mUnbinder;
    private EqSettingAdapter mAdapter;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private boolean mIsFirstDrawn = false;

    public EqSettingFragment() {
    }

    public static EqSettingFragment newInstance(Bundle args) {
        EqSettingFragment fragment = new EqSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_eq, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mIsFirstDrawn = false;
        mGlobalLayoutListener = () -> {
            Timber.i("OnGlobalLayoutListener#onGlobalLayout() " +
                    "Width = " + String.valueOf(mGraphView.getWidth()) + ", " +
                    "Height = " + String.valueOf(mGraphView.getHeight()));
            if (!mIsFirstDrawn) {
                Resources res = getResources();
                mGraphView.setSize(mGraphView.getWidth(), mGraphView.getHeight());
                mIsFirstDrawn = true;
            }
            // removeOnGlobalLayoutListener()の削除
            mGraphView.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        };

        mGraphView.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
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
    protected EqSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.EQ_SETTING;
    }

    /**
     * アダプター設定
     *
     * @param types タイプリスト
     */
    @Override
    public void setAdapter(List<SoundFxSettingEqualizerType> types) {
        mAdapter = new EqSettingAdapter(getContext(), types);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener((parent, view, position, id) -> mPresenter.onSelectEqTypeAction(position));
    }

    /**
     * 選択中EQ設定
     *
     * @param selected 選択中インデックス
     */
    @Override
    public void setSelectedItem(int selected) {
        mAdapter.setSelectedIndex(selected);
    }

    /**
     * Presetイメージ設定
     *
     * @param resId リソース
     */
    @Override
    public void setPresetView(int resId) {
        if(resId==-1){
            mPresetView.setImageDrawable(null);
        }else {
            mPresetView.setImageResource(resId);
        }
        mCustomView.setVisibility(View.INVISIBLE);
        mPresetView.setVisibility(View.VISIBLE);
    }

    /**
     * CustomView設定
     *
     * @param bands Band配列
     */
    @Override
    public void setCustomView(float[] bands) {
        mGraphView.setBandData(bands);
        mCustomView.setVisibility(View.VISIBLE);
        mPresetView.setVisibility(View.INVISIBLE);
    }

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    @Override
    public void setColor(@ColorRes int color) {
        mAdapter.setColor(color);
    }

    @OnClick(R.id.quick_set)
    public void onClickQuickSet() {
        getPresenter().onQuickSetAction();
    }

    @OnClick(R.id.pro_set)
    public void onClickProSet() {
        getPresenter().onProSetAction();
    }

    @Override
    public void setEnable(boolean isEnabled) {
        mListView.setEnabled(isEnabled);
        mAdapter.setEnabled(isEnabled);
        mQuickSet.setEnabled(isEnabled);
        mProSet.setEnabled(isEnabled);
        if(isEnabled){
            mQuickSet.setAlpha(1.0f);
            mProSet.setAlpha(1.0f);
        }else{
            mQuickSet.setAlpha(0.5f);
            mProSet.setAlpha(0.5f);
        }
    }
}
