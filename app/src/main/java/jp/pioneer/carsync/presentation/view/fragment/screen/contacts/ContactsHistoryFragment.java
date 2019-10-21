package jp.pioneer.carsync.presentation.view.fragment.screen.contacts;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.model.ContactsHistoryItem;
import jp.pioneer.carsync.presentation.presenter.ContactsHistoryPresenter;
import jp.pioneer.carsync.presentation.view.ContactsHistoryView;
import jp.pioneer.carsync.presentation.view.adapter.ContactsHistoryAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * 電話帳 発着信履歴リストの画面
 */
@RuntimePermissions
public class ContactsHistoryFragment extends AbstractScreenFragment<ContactsHistoryPresenter, ContactsHistoryView>
        implements ContactsHistoryView {

    @Inject ContactsHistoryPresenter mPresenter;
    @BindView(R.id.list_view) ListView mListView;
    private ContactsHistoryAdapter mContactsHistoryAdapter;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ
     */
    @Inject
    public ContactsHistoryFragment() {
    }

    /**
     * 新規インスタンス取得
     * @param args 引き継ぎ情報
     * @return ContactsHistoryFragment
     */
    public static ContactsHistoryFragment newInstance(Bundle args) {
        ContactsHistoryFragment fragment = new ContactsHistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_music, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ContactsHistoryFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ContactsHistoryFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
        return ScreenId.CONTACTS_HISTORY;
    }

    @NonNull
    @Override
    protected ContactsHistoryPresenter getPresenter() {
        return mPresenter;
    }

    @NeedsPermission(Manifest.permission.READ_CALL_LOG)
    public void setLoaderManager() {
        getPresenter().setLoaderManager(getLoaderManager());
    }

    /**
     * 発着信履歴表示
     *
     * @param list 発着信履歴のリスト
     */
    @Override
    public void setHistoryList(ArrayList<ContactsHistoryItem> list) {
        mContactsHistoryAdapter = new ContactsHistoryAdapter(getContext(), list);
        mListView.setAdapter(mContactsHistoryAdapter);
    }

    /**
     * リスト項目押下処理
     */
    @OnItemClick(R.id.list_view)
    public void onClickListItem(AdapterView<?> parent, View view, int position, long id) {
        ListView listview = (ListView) parent;
        ContactsHistoryItem item = (ContactsHistoryItem)listview.getItemAtPosition(position);
        getPresenter().onContactsHistoryAction(item);
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
