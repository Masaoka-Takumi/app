package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.Manifest;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.ImpactDetectionContactSettingPresenter;
import jp.pioneer.carsync.presentation.view.ImpactDetectionContactSettingView;
import jp.pioneer.carsync.presentation.view.adapter.SettingContactAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * 衝突検知機能設定 緊急連絡先設定の画面
 */
@RuntimePermissions
public class ImpactDetectionContactSettingFragment extends AbstractScreenFragment<ImpactDetectionContactSettingPresenter, ImpactDetectionContactSettingView>
        implements ImpactDetectionContactSettingView {

    @Inject ImpactDetectionContactSettingPresenter mPresenter;
    @BindView(R.id.list_view) ExpandableListView mListView;
    private SettingContactAdapter mAdapter;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ
     */
    public ImpactDetectionContactSettingFragment() {
    }

    /**
     * 新規インスタンス取得
     * @param args 引き継ぎ情報
     * @return ImpactDetectionSettingsFragment
     */
    public static ImpactDetectionContactSettingFragment newInstance(Bundle args) {
        ImpactDetectionContactSettingFragment fragment = new ImpactDetectionContactSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_common_contact, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mAdapter = new SettingContactAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setFastScrollEnabled(true);
        mListView.setGroupIndicator(null);
        mListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            getPresenter().onSelectGroup(mAdapter.getGroup(groupPosition));
            return false;
        });
        mListView.setOnChildClickListener((expandableListView, view1, groupPosition, childPosition, id) -> {
            getPresenter().onNumberAction(mAdapter.getChild(groupPosition, childPosition));
            return false;
        });
        mListView.setOnGroupCollapseListener(groupPosition -> getPresenter().onGroupCollapseAction(groupPosition));
        mListView.setDivider(null);
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
        ImpactDetectionContactSettingFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ImpactDetectionContactSettingFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.READ_CONTACTS)
    public void setLoaderManager() {
        getPresenter().setLoaderManager(getLoaderManager());
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected ImpactDetectionContactSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.IMPACT_DETECTION_CONTACT_SETTING;
    }

    /**
     * 現在の緊急連絡先設定
     * @param name 現在の緊急連絡先のlookupキー
     */
    @Override
    public void setTargetContact(String name) {
        mAdapter.setTargetKey(name);
    }

    /**
     * 緊急連絡先親項目表示
     * @param data 緊急連絡先親項目のカーソル
     */
    @Override
    public void setGroupCursor(Cursor data, Bundle args) {
        mAdapter.setGroupCursor(data, args);
    }

    /**
     * 緊急連絡先子項目表示
     * @param position 親項目のポジション
     * @param data 緊急連絡先子項目のカーソル
     */
    @Override
    public void setChildrenCursor(int position, Cursor data) {
        mAdapter.setChildrenCursor(position, data);
    }
}
