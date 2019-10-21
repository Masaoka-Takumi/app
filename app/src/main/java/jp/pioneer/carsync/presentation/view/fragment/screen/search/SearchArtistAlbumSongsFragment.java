package jp.pioneer.carsync.presentation.view.fragment.screen.search;

import android.Manifest;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.SearchArtistAlbumSongsPresenter;
import jp.pioneer.carsync.presentation.view.SongsView;
import jp.pioneer.carsync.presentation.view.adapter.SongAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static jp.pioneer.carsync.presentation.view.fragment.ScreenId.SEARCH_MUSIC_ARTIST_ALBUM_SONG_LIST;

/**
 * アーティスト指定アルバム内楽曲リスト画面（検索用）
 * <p>
 * {@link jp.pioneer.carsync.presentation.view.fragment.screen.player.list.ArtistAlbumSongsFragment}と同様の画面
 */
@RuntimePermissions
public class SearchArtistAlbumSongsFragment extends AbstractScreenFragment<SearchArtistAlbumSongsPresenter, SongsView> implements SongsView {
    @Inject SearchArtistAlbumSongsPresenter mPresenter;
    @BindView(R.id.list_view) ListView mListView;
    private SongAdapter mSongAdapter;
    private Unbinder mUnbinder;

    /**
     * 新規インタスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return SearchArtistAlbumSongsFragment
     */
    public static SearchArtistAlbumSongsFragment newInstance(Bundle args) {
        SearchArtistAlbumSongsFragment fragment = new SearchArtistAlbumSongsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_generic, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mSongAdapter = new SongAdapter(getContext(), null, false);
        mSongAdapter.setSphCarDevice(getPresenter().isSphCarDevice());
        mSongAdapter.setSearchResult(true);
        getPresenter().setArguments(getArguments());
        mListView.setAdapter(mSongAdapter);
        mListView.setFastScrollEnabled(true);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SearchArtistAlbumSongsFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        SearchArtistAlbumSongsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
    protected SearchArtistAlbumSongsPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return SEARCH_MUSIC_ARTIST_ALBUM_SONG_LIST;
    }

    /**
     * LoaderManagerの設定
     */
    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void setLoaderManager() {
        getPresenter().setLoaderManager(getLoaderManager());
    }

    @Override
    public void setSongCursor(Cursor data, Bundle args) {
        mSongAdapter.swapCursor(data, args);
    }

    /**
     * リストアイテム押下イベント
     *
     * @param parent   親View
     * @param view     View
     * @param position タッチ位置
     * @param id       アイテムID
     */
    @OnItemClick(R.id.list_view)
    public void onClickListItem(AdapterView<?> parent, View view, int position, long id) {
        getPresenter().onArtistAlbumSongPlayAction(id);
    }

    @Override
    public int getSectionIndex(){
        return mSongAdapter.getSectionForPosition(mListView.getSelectedItemPosition());
    }

    @Override
    public void setSectionIndex(int sectionIndex){
        mListView.setSelection(mSongAdapter.getPositionForSection(sectionIndex));
    }

    @Override
    public int getSelectPosition(){
        return mListView.getSelectedItemPosition();
    }

    @Override
    public void setSelectPosition(int position){
        mListView.setSelection(position);
    }

    @Override
    public long getItemId(int position){
        return mSongAdapter.getItemId(position);
    }

    @Override
    public int getItemsCount(){
        return mSongAdapter.getCount();
    }
    @Override
    public int getSectionCount(){
        return mSongAdapter.getSectionCount();
    }

    @Override
    public String getSectionString(int sectionIndex) {
        return (String)mSongAdapter.getSections()[sectionIndex];
    }
    @Override
    public Cursor getItem(int position){
        return (Cursor) mSongAdapter.getItem(position);
    }
}
