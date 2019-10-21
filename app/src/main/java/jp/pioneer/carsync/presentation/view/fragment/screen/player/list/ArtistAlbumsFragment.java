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

import com.commonsware.cwac.merge.MergeAdapter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.ArtistAlbumsPresenter;
import jp.pioneer.carsync.presentation.view.AlbumsView;
import jp.pioneer.carsync.presentation.view.adapter.AlbumAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static jp.pioneer.carsync.presentation.view.fragment.ScreenId.ARTIST_ALBUM_LIST;

/**
 * アーティストリスト内アルバムリストの画面
 */
@RuntimePermissions
public class ArtistAlbumsFragment extends AbstractScreenFragment<ArtistAlbumsPresenter, AlbumsView> implements AlbumsView {
    @Inject ArtistAlbumsPresenter mPresenter;
    @BindView(R.id.list_view) ListView mListView;
    private MergeAdapter mMergeAdapter;
    private AlbumAdapter mAlbumAdapter;
    private Unbinder mUnbinder;

    public static ArtistAlbumsFragment newInstance(Bundle args) {
        ArtistAlbumsFragment fragment = new ArtistAlbumsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_music, container, false);
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
        //mMergeAdapter.addAdapter(mAlbumAdapter);
        //mListView.setAdapter(mMergeAdapter);
        mListView.setAdapter(mAlbumAdapter);
        mListView.setFastScrollEnabled(true);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArtistAlbumsFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ArtistAlbumsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
    protected ArtistAlbumsPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ARTIST_ALBUM_LIST;
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void setLoaderManager() {
        getPresenter().setLoaderManager(getLoaderManager());
    }

    /**
     * アルバムの設定
     * @param data アルバムのカーソル
     * @param args アルバムのバンドル
     */
    @Override
    public void setAlbumCursor(Cursor data, Bundle args) {
        mAlbumAdapter.swapCursor(data, args);
    }

    /**
     * リスト項目クリック時
     * @param parent AdapterView
     * @param view View
     * @param position 選択位置
     * @param id 選択ID
     */
    @OnItemClick(R.id.list_view)
    public void onClickListItem(AdapterView<?> parent, View view, int position, long id) {
        if (id == 0) {
            getPresenter().onArtistAlbumShufflePlayAction();
        } else {
            ListView listview = (ListView) parent;
            Cursor cursor = (Cursor) listview.getItemAtPosition(position);
            getPresenter().onArtistAlbumSongListShowAction(cursor, id,false);
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
