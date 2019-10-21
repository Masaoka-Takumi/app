package jp.pioneer.carsync.presentation.view.fragment.screen.contacts;

import android.Manifest;
import android.content.Intent;
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
import jp.pioneer.carsync.presentation.presenter.ContactsPresenter;
import jp.pioneer.carsync.presentation.view.ContactsView;
import jp.pioneer.carsync.presentation.view.adapter.ContactsAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * 電話帳 電話帳リストの画面
 */
@RuntimePermissions
public class ContactsFragment extends AbstractScreenFragment<ContactsPresenter, ContactsView>
        implements ContactsView {

    @Inject ContactsPresenter mPresenter;
    @BindView(R.id.list_view) ExpandableListView mListView;
    private ContactsAdapter mAdapter;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ
     */
    @Inject
    public ContactsFragment() {
    }

    /**
     * 新規インスタンス取得
     * @param args 引き継ぎ情報
     * @return ContactsFragment
     */
    public static ContactsFragment newInstance(Bundle args) {
        ContactsFragment fragment = new ContactsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_common_contact, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mAdapter = new ContactsAdapter(getActivity()) {
            @Override
            public void onClickStarAction(Cursor cursor) {
                ContactsFragmentPermissionsDispatcher.onClickFavoritesWithCheck(ContactsFragment.this, cursor);
            }
        };
        mListView.setAdapter(mAdapter);
        mListView.setFastScrollEnabled(true);
        mListView.setGroupIndicator(null);
        mListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            getPresenter().setGroupCursor(mAdapter.getGroup(groupPosition));
            return false;
        });
        mListView.setOnChildClickListener((expandableListView, view1, groupPosition, childPosition, id) -> {
            getPresenter().onNumberAction(mAdapter.getChild(groupPosition, childPosition));
            return true;
        });
        mListView.setOnGroupCollapseListener(groupPosition -> getPresenter().onGroupCollapseAction(groupPosition));
        mListView.setDivider(null);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ContactsFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ContactsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
        return ScreenId.CONTACTS_LIST;
    }

    @NonNull
    @Override
    protected ContactsPresenter getPresenter() {
        return mPresenter;
    }

    @NeedsPermission(Manifest.permission.READ_CONTACTS)
    public void setLoaderManager() {
        getPresenter().setLoaderManager(getLoaderManager());
    }

    @NeedsPermission(Manifest.permission.WRITE_CONTACTS)
    public void onClickFavorites(Cursor cursor) {
        getPresenter().onFavoritesAction(cursor);
    }

    /**
     * 連絡先親項目表示
     * @param data 連絡先親項目のカーソル
     */
    @Override
    public void setGroupCursor(Cursor data, Bundle args) {
        mAdapter.setGroupCursor(data, args);
    }

    /**
     * 連絡先子項目表示
     * @param position 親項目のポジション
     * @param data 連絡先子項目のカーソル
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
