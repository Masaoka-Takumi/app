package jp.pioneer.carsync.presentation.view.fragment.screen.search;

import android.Manifest;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.commonsware.cwac.merge.MergeAdapter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.SearchMusicPresenter;
import jp.pioneer.carsync.presentation.view.SearchMusicView;
import jp.pioneer.carsync.presentation.view.adapter.SearchAlbumAdapter;
import jp.pioneer.carsync.presentation.view.adapter.SearchArtistAdapter;
import jp.pioneer.carsync.presentation.view.adapter.SearchSongAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

/**
 * 音楽検索画面
 * <p>
 * 音楽に関連する検索結果を表示する画面
 * アダプターは{@link MergeAdapter}を使用し表示する。
 */
@RuntimePermissions
public class SearchMusicFragment extends AbstractScreenFragment<SearchMusicPresenter, SearchMusicView>
        implements SearchMusicView {

    @Inject SearchMusicPresenter mPresenter;
    @BindView(R.id.search_list) ListView mSearchList;
    private Unbinder mUnbinder;
    private MergeAdapter mMergeAdapter;
    private View mArtistHeader;
    private SearchArtistAdapter mArtistAdapter;
    private View mAlbumHeader;
    private SearchAlbumAdapter mAlbumAdapter;
    private View mSongHeader;
    private SearchSongAdapter mSongAdapter;

    /**
     * コンストラクタ
     */
    @Inject
    public SearchMusicFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return SearchMusicFragment
     */
    public static SearchMusicFragment newInstance(Bundle args) {
        SearchMusicFragment fragment = new SearchMusicFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_music_search, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        getPresenter().setArguments(getArguments());

        mArtistHeader = inflater.inflate(R.layout.element_list_header_music_search, null, false);
        ((TextView) mArtistHeader.findViewById(R.id.text_view)).setText(R.string.tts_002);
        mArtistAdapter = new SearchArtistAdapter(getContext(), null){
            @Override
            protected void onJacketClick(long id) {
                getPresenter().onArtistPlayAction(id);
            }
        };
        mAlbumHeader = inflater.inflate(R.layout.element_list_header_music_search, null, false);
        ((TextView) mAlbumHeader.findViewById(R.id.text_view)).setText(R.string.tts_003);
        mAlbumAdapter = new SearchAlbumAdapter(getContext(), null){
            @Override
            protected void onJacketClick(long id) {
                getPresenter().onAlbumPlayAction(id);
            }
        };
        mSongHeader = inflater.inflate(R.layout.element_list_header_music_search, null, false);
        ((TextView) mSongHeader.findViewById(R.id.text_view)).setText(R.string.tts_004);
        mSongAdapter = new SearchSongAdapter(getContext(), null);

        mMergeAdapter = new MergeAdapter();
        mMergeAdapter.addView(mArtistHeader);
        mMergeAdapter.setActive(mArtistHeader, false);
        mMergeAdapter.addAdapter(mArtistAdapter);
        mMergeAdapter.addView(mAlbumHeader);
        mMergeAdapter.setActive(mAlbumHeader, false);
        mMergeAdapter.addAdapter(mAlbumAdapter);
        mMergeAdapter.addView(mSongHeader);
        mMergeAdapter.setActive(mSongHeader, false);
        mMergeAdapter.addAdapter(mSongAdapter);
        mSearchList.setEmptyView(view.findViewById(R.id.empty_view));
        mSearchList.setAdapter(mMergeAdapter);
        //mSearchList.setDivider(null);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SearchMusicFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        SearchMusicFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
        return ScreenId.SEARCH_MUSIC_RESULTS;
    }

    @NonNull
    @Override
    protected SearchMusicPresenter getPresenter() {
        return mPresenter;
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void setLoaderManager() {
        getPresenter().setLoaderManager(LoaderManager.getInstance(this));
    }

    @Override
    public void setArtistCursor(Cursor data) {
        mArtistAdapter.swapCursor(data);
        //mMergeAdapter.setActive(mArtistHeader, (data != null && data.getCount() >= 1));
    }

    @Override
    public void setAlbumCursor(Cursor data) {
        mAlbumAdapter.swapCursor(data);
        //mMergeAdapter.setActive(mAlbumHeader, (data != null && data.getCount() >= 1));
    }

    @Override
    public void setMusicCursor(Cursor data) {
        mSongAdapter.swapCursor(data);
        //mMergeAdapter.setActive(mSongHeader, (data != null && data.getCount() >= 1));
    }

    /**
     * リストアイテム押下イベント
     * <p>
     * リスト内アイテム（楽曲・アーティスト・アルバム）のタッチを処理する
     *
     * @param parent   親View
     * @param view     View
     * @param position タッチ位置
     * @param id       アイテムID
     */
    @OnItemClick(R.id.search_list)
    public void onClickListItem(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getAdapter() instanceof MergeAdapter) {
            ListAdapter adapter = ((MergeAdapter) parent.getAdapter()).getAdapter(position);
            if (adapter instanceof SearchArtistAdapter) { // アーティスト選択
                Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
                getPresenter().onArtistAlbumListShowAction(cursor, id);
            }
            if (adapter instanceof SearchAlbumAdapter) { // アルバム選択
                Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
                getPresenter().onAlbumSongListShowAction(cursor, id);
            }
            if (adapter instanceof SearchSongAdapter) { // 楽曲選択
                getPresenter().onSongPlayAction(id);
            }
        }
    }

    /**
     * 楽曲検索結果ダイアログを閉幕
     */
    @Override
    public void closeDialog() {
        ((SearchContainerFragment) getParentFragment()).dismiss();
    }


}
