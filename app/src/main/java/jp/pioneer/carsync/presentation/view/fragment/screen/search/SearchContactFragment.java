package jp.pioneer.carsync.presentation.view.fragment.screen.search;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.SearchContactPresenter;
import jp.pioneer.carsync.presentation.view.SearchContactView;
import jp.pioneer.carsync.presentation.view.adapter.SearchContactAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

/**
 * 連絡先検索画面
 */
@RuntimePermissions
public class SearchContactFragment extends AbstractScreenFragment<SearchContactPresenter, SearchContactView>
        implements SearchContactView {

    @Inject SearchContactPresenter mPresenter;
    @BindView(R.id.list_view) ExpandableListView mListView;
    @BindView(R.id.empty_view) TextView mEmptyView;
    private SearchContactAdapter mAdapter;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ
     */
    public SearchContactFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return SearchContactFragment
     */
    public static SearchContactFragment newInstance(Bundle args) {
        SearchContactFragment fragment = new SearchContactFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_contact_search, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        getPresenter().setArguments(getArguments());

        mAdapter = new SearchContactAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(view.findViewById(R.id.empty_view));
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SearchContactFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        SearchContactFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
        return ScreenId.SEARCH_CONTACT_RESULTS;
    }

    @NonNull
    @Override
    protected SearchContactPresenter getPresenter() {
        return mPresenter;
    }

    /**
     * LoaderManagerの設定
     * <p>
     * presenterへLoaderManagerを譲渡する。
     */
    @NeedsPermission(Manifest.permission.READ_CONTACTS)
    void setLoaderManager() {
        getPresenter().setLoaderManager(LoaderManager.getInstance(this));
    }

    @OnPermissionDenied(Manifest.permission.READ_CONTACTS)
    void onPermissionDenied() {
        mEmptyView.setText(R.string.rec_028);
    }

    @Override
    public void setGroupCursor(Cursor data) {
        mAdapter.setGroupCursor(data);
        if(data.getCount() <= 0){
            mEmptyView.setText(R.string.rec_011);
        }
    }

    @Override
    public void setChildrenCursor(int position, Cursor data) {
        mAdapter.setChildrenCursor(position, data);
    }

    @Override
    public void dial(String number) {
        Uri uri = Uri.parse("tel:" + number);
        startActivity(new Intent(Intent.ACTION_DIAL, uri));
    }

    /**
     * 検索結果ダイアログを閉幕
     */
    @Override
    public void closeDialog() {
        ((SearchContainerFragment) getParentFragment()).dismiss();
    }
}
