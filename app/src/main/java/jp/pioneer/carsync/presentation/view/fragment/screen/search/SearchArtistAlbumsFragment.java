package jp.pioneer.carsync.presentation.view.fragment.screen.search;

import android.Manifest;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.commonsware.cwac.merge.MergeAdapter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.SearchArtistAlbumsPresenter;
import jp.pioneer.carsync.presentation.view.AlbumsView;
import jp.pioneer.carsync.presentation.view.adapter.AlbumAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static jp.pioneer.carsync.presentation.view.fragment.ScreenId.SEARCH_MUSIC_ARTIST_ALBUM_LIST;

/**
 * アーティスト指定アルバムリスト画面（検索用）
 * <p>
 * {@link jp.pioneer.carsync.presentation.view.fragment.screen.player.list.ArtistAlbumsFragment}と同様の画面
 */
@RuntimePermissions
public class SearchArtistAlbumsFragment extends AbstractScreenFragment<SearchArtistAlbumsPresenter, AlbumsView> implements AlbumsView {
    @Inject SearchArtistAlbumsPresenter mPresenter;
    @BindView(R.id.list_view) ListView mListView;
    private MergeAdapter mMergeAdapter;
    private AlbumAdapter mAlbumAdapter;
    private Unbinder mUnbinder;

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return SearchArtistAlbumsFragment
     */
    public static SearchArtistAlbumsFragment newInstance(Bundle args) {
        SearchArtistAlbumsFragment fragment = new SearchArtistAlbumsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_generic, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        getPresenter().setArguments(getArguments());
        //mMergeAdapter = new MergeAdapter();
        //ShuffleAdapter shuffle = new ShuffleAdapter(getContext());
        //mMergeAdapter.addAdapter(shuffle);
        mAlbumAdapter = new AlbumAdapter(getContext(), null, false) {
            @Override
            protected void onJacketClick(long id) {
                getPresenter().onArtistAlbumPlayAction(id);
            }
        };
        mAlbumAdapter.setSphCarDevice(getPresenter().isSphCarDevice());
        mAlbumAdapter.setSearchResult(true);
        //mMergeAdapter.addAdapter(mAlbumAdapter);
        mListView.setAdapter(mAlbumAdapter);
        mListView.setFastScrollEnabled(true);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SearchArtistAlbumsFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        SearchArtistAlbumsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
    protected SearchArtistAlbumsPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return SEARCH_MUSIC_ARTIST_ALBUM_LIST;
    }

    /**
     * LoaderManagerの設定
     */
    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void setLoaderManager() {
        getPresenter().setLoaderManager(LoaderManager.getInstance(this));
    }

    @Override
    public void setAlbumCursor(Cursor data, Bundle args) {
        mAlbumAdapter.swapCursor(data, args);
    }

    /**
     * リストアイテム押下イベント
     * <p>
     * 先頭アイテムはシャッフル機能で固定されている。
     * 以下アイテムがアルバムリストになる。
     *
     * @param parent   親View
     * @param view     View
     * @param position タッチ位置
     * @param id       アイテムID
     */
    @OnItemClick(R.id.list_view)
    public void onClickListItem(AdapterView<?> parent, View view, int position, long id) {
        if (id == 0) {
            getPresenter().onArtistAlbumShufflePlayAction();
        } else {
            ListView listview = (ListView) parent;
            Cursor cursor = (Cursor) listview.getItemAtPosition(position);
            getPresenter().onArtistAlbumSongListShowAction(cursor, id);
        }
    }

    @Override
    public int getSectionIndex(){
        return mAlbumAdapter.getSectionForPosition(mListView.getCheckedItemPosition());
    }

    @Override
    public void setSectionIndex(int sectionIndex){
        mListView.setItemChecked(mAlbumAdapter.getPositionForSection(sectionIndex) ,true);
        mListView.setSelection(mAlbumAdapter.getPositionForSection(sectionIndex));
    }

    @Override
    public int getSelectPosition(){
        return mListView.getCheckedItemPosition();
    }

    @Override
    public void setSelectPosition(int position){
        mListView.setItemChecked(position ,true);
        mListView.setSelection(position);
    }

    @Override
    public long getItemId(int position){
        return mAlbumAdapter.getItemId(position);
    }

    @Override
    public Cursor getItem(int position){
        return (Cursor) mAlbumAdapter.getItem(position);
    }

    @Override
    public int getItemsCount(){
        return mAlbumAdapter.getCount();
    }

    @Override
    public int getSectionCount(){
        return mAlbumAdapter.getSectionCount();
    }

    @Override
    public String getSectionString(int sectionIndex) {
        return (String)mAlbumAdapter.getSections()[sectionIndex];
    }
}
