package jp.pioneer.carsync.presentation.view.fragment.screen.contacts;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
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
import jp.pioneer.carsync.presentation.presenter.ContactsFavoritePresenter;
import jp.pioneer.carsync.presentation.view.ContactsFavoriteView;
import jp.pioneer.carsync.presentation.view.adapter.ContactsFavoriteAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * 電話帳 お気に入りリストの画面
 */
@RuntimePermissions
public class ContactsFavoriteFragment extends AbstractScreenFragment<ContactsFavoritePresenter, ContactsFavoriteView>
        implements ContactsFavoriteView {

    @Inject ContactsFavoritePresenter mPresenter;
    @BindView(R.id.list_view) ExpandableListView mListView;
    private ContactsFavoriteAdapter mAdapter;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ
     */
    @Inject
    public ContactsFavoriteFragment() {
    }

    /**
     * 新規インスタンス取得
     * @param args 引き継ぎ情報
     * @return ContactsFavoriteFragment
     */
    public static ContactsFavoriteFragment newInstance(Bundle args) {
        ContactsFavoriteFragment fragment = new ContactsFavoriteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_common_contact, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mAdapter = new ContactsFavoriteAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setFastScrollEnabled(true);
        mListView.setGroupIndicator(null);
        mListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            getPresenter().setGroupCursor(mAdapter.getGroup(groupPosition));
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ContactsFavoriteFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ContactsFavoriteFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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

    @Override
    public ScreenId getScreenId() {
        return ScreenId.CONTACTS_FAVORITE;
    }

    @NonNull
    @Override
    protected ContactsFavoritePresenter getPresenter() {
        return mPresenter;
    }

    @NeedsPermission(Manifest.permission.READ_CONTACTS)
    public void setLoaderManager() {
        getPresenter().setLoaderManager(LoaderManager.getInstance(this));
    }

    /**
     * お気に入りリスト親項目表示
     * @param data リスト親項目のカーソル
     */
    @Override
    public void setGroupCursor(Cursor data, Bundle args) {
        mAdapter.setGroupCursor(data, args);
    }

    /**
     * お気に入りリスト子項目表示
     * @param position 親項目のポジション
     * @param data 子項目のカーソル
     */
    @Override
    public void setChildrenCursor(int position, Cursor data) {
        mAdapter.setChildrenCursor(position, data);
    }

    /**
     * 連絡先に発信
     * @param intent インテント
     */
    @Override
    public void dial(Intent intent) {
        startActivity(intent);
    }
}
