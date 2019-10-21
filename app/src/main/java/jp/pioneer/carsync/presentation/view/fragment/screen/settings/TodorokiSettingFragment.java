package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.presentation.presenter.TodorokiSettingPresenter;
import jp.pioneer.carsync.presentation.view.TodorokiSettingView;
import jp.pioneer.carsync.presentation.view.adapter.TodorokiSettingAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * Super轟Sound設定の画面
 */

public class TodorokiSettingFragment extends AbstractScreenFragment<TodorokiSettingPresenter, TodorokiSettingView> implements TodorokiSettingView {
    @Inject TodorokiSettingPresenter mPresenter;

    @BindView(R.id.list_view) ListView mListView;
    @BindView(R.id.preset_view) ImageView mPresetView;
    private Unbinder mUnbinder;
    private TodorokiSettingAdapter mAdapter;

    public TodorokiSettingFragment() {
    }

    public static TodorokiSettingFragment newInstance(Bundle args) {
        TodorokiSettingFragment fragment = new TodorokiSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_todoroki, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        GlideDrawableImageViewTarget target = new GlideDrawableImageViewTarget(mPresetView);
        Glide.with(this).load(R.raw.p1151_todoroki_off).into(target);
        mPresetView.setVisibility(View.VISIBLE);
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
    protected TodorokiSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.TODOROKI_SETTING;
    }

    /**
     * アダプター設定
     *
     * @param types タイプリスト
     */
    @Override
    public void setAdapter(ArrayList<SuperTodorokiSetting> types) {
        mAdapter = new TodorokiSettingAdapter(getContext(), types);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener((parent, view, position, id) -> mPresenter.onSelectTodorokiTypeAction(position));
    }

    /**
     * 選択中轟設定
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
        GlideDrawableImageViewTarget target = new GlideDrawableImageViewTarget(mPresetView);
        Glide.with(this).load(resId).into(target);
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

    @Override
    public void setEnable(boolean isEnabled) {
        mListView.setEnabled(isEnabled);
        mAdapter.setEnabled(isEnabled);
    }
}
