package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.model.UiColor;
import jp.pioneer.carsync.presentation.presenter.UiColorSettingPresenter;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;
import jp.pioneer.carsync.presentation.view.UiColorSettingView;
import jp.pioneer.carsync.presentation.view.adapter.UiColorAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * UIカラー設定画面
 */

public class UiColorSettingFragment extends AbstractScreenFragment<UiColorSettingPresenter, UiColorSettingView>
        implements UiColorSettingView {

    @Inject UiColorSettingPresenter mPresenter;
    @BindView(R.id.color_list) RecyclerView mColor;
    @Nullable @BindView(R.id.ui_home) ImageView mUiHome;
    @BindView(R.id.ui_player) ImageView mUIPlayer;
    private UiColorAdapter mItemAdapter;
    private Unbinder mUnbinder;
    private int mOrientation;
    /**
     * コンストラクタ
     */
    public UiColorSettingFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return UiColorSettingFragment
     */
    public static UiColorSettingFragment newInstance(Bundle args) {
        UiColorSettingFragment fragment = new UiColorSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_ui_color, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        Configuration config = getContext().getResources().getConfiguration();
        mOrientation = config.orientation;
        // RecyclerViewの横表示を実現するために、LinearLayoutManagerを使い設定を行う。
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mColor.setLayoutManager(manager);
        mColor.setOverScrollMode(View.OVER_SCROLL_NEVER);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.UI_COLOR_SETTING;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected UiColorSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setColor(List<UiColor> colors) {
        mItemAdapter = new UiColorAdapter(getContext(), colors,
                (v, position) -> getPresenter().onSelectColorItemAction(position));
        mColor.setAdapter(mItemAdapter);

    }

    @Override
    public void setPosition(int position, @ColorRes int color) {
        mItemAdapter.setPosition(position);
        mItemAdapter.notifyDataSetChanged();
        if(mOrientation==Configuration.ORIENTATION_LANDSCAPE) {
            if (mUiHome != null) mUiHome.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0421_c, color));
            mUIPlayer.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0431_c, color));
        }else{
            mUIPlayer.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p1331_c, color));
        }
    }

    @Override
    public void setTheme(int theme){
        getActivity().setTheme(theme);
    }

}
