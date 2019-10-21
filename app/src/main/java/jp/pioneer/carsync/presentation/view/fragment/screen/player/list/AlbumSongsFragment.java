package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

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
import jp.pioneer.carsync.presentation.presenter.AlbumSongsPresenter;
import jp.pioneer.carsync.presentation.view.SongsView;
import jp.pioneer.carsync.presentation.view.adapter.SongAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static jp.pioneer.carsync.presentation.view.fragment.ScreenId.ALBUM_SONG_LIST;

/**
 * アルバムリスト内楽曲リストの画面
 */
@RuntimePermissions
public class AlbumSongsFragment extends AbstractScreenFragment<AlbumSongsPresenter, SongsView> implements SongsView {
    @Inject AlbumSongsPresenter mPresenter;
    @BindView(R.id.list_view) ListView mListView;
    private SongAdapter mSongAdapter;
    private Unbinder mUnbinder;

    public static AlbumSongsFragment newInstance(Bundle args) {
        AlbumSongsFragment fragment = new AlbumSongsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_music, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mSongAdapter = new SongAdapter(getContext(), null, false);
        mSongAdapter.setSphCarDevice(getPresenter().isSphCarDevice());
        getPresenter().setArguments(getArguments());
        mListView.setAdapter(mSongAdapter);
        mListView.setFastScrollEnabled(false);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AlbumSongsFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        AlbumSongsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
    protected AlbumSongsPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ALBUM_SONG_LIST;
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void setLoaderManager() {
        getPresenter().setLoaderManager(getLoaderManager());
    }

    /**
     * 曲の設定
     *
     * @param data 曲のカーソル
     * @param args 曲のバンドル
     */
    @Override
    public void setSongCursor(Cursor data, Bundle args) {
        mSongAdapter.swapCursor(data, args);
    }

    /**
     * 項目リスト選択
     *
     * @param parent   AdapterView
     * @param view     View
     * @param position int
     * @param id       選択楽曲ID
     */
    @OnItemClick(R.id.list_view)
    public void onClickListItem(AdapterView<?> parent, View view, int position, long id) {
        getPresenter().onAlbumSongPlayAction(id);
    }

    @Override
    public int getSectionIndex(){
        return mSongAdapter.getSectionForPosition(mListView.getCheckedItemPosition());
    }

    @Override
    public void setSectionIndex(int sectionIndex){
        mListView.setItemChecked(mSongAdapter.getPositionForSection(sectionIndex), true);
        mListView.setSelection(mSongAdapter.getPositionForSection(sectionIndex));
    }

    @Override
    public int getSelectPosition(){
        return mListView.getCheckedItemPosition();
    }

    @Override
    public void setSelectPosition(int position){
        mListView.setItemChecked(position, true);
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
