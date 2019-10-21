package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.model.SettingEntrance;
import jp.pioneer.carsync.presentation.presenter.SettingsEntrancePresenter;
import jp.pioneer.carsync.presentation.view.SettingsEntranceView;
import jp.pioneer.carsync.presentation.view.adapter.SettingsAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

/**
 * 設定入口の画面
 */
public class SettingsEntranceFragment extends AbstractScreenFragment<SettingsEntrancePresenter, SettingsEntranceView> implements SettingsEntranceView {
    @Inject SettingsEntrancePresenter mPresenter;
    @BindView(R.id.root_view) RelativeLayout mRootView;
    private ViewGroup mListView;
    private SettingsAdapter mAdapter;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ
     */
    public SettingsEntranceFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return OpeningEulaFragment
     */
    public static SettingsEntranceFragment newInstance(Bundle args) {
        SettingsEntranceFragment fragment = new SettingsEntranceFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_variable, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        view.setBackgroundResource(R.color.setting_container_background_color);
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
    protected SettingsEntrancePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.SETTINGS_ENTRANCE;
    }

    /**
     * アダプター設定
     *
     * @param icons  アイコン
     * @param titles タイトル
     */
    @SuppressLint("InflateParams")
    @Override
    public void setAdapter(ArrayList<Integer> icons, ArrayList<SettingEntrance> titles, ArrayList<Boolean> enable) {
        mAdapter = new SettingsAdapter(getContext(), icons, titles, enable);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        GridView gridview = (GridView) inflater.inflate(R.layout.element_list_gridview, mListView, false);
        Configuration config = getResources().getConfiguration();
        gridview.setNumColumns(config.orientation == ORIENTATION_LANDSCAPE ? 4 : 2);

        gridview.setAdapter(mAdapter);
        gridview.setOnItemClickListener((parent, view, position, id) -> mPresenter.onClickAction(position));
        mListView = gridview;
        mRootView.removeAllViews();
        mRootView.addView(mListView);
    }

    /**
     * メッセージ表示
     *
     * @param message メッセージ
     */
    @Override
    public void onShowMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * アンドロイド設定表示
     *
     * @param action アクション
     */
    @Override
    public void onShowAndroidSettings(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        startActivity(intent);
    }
}
