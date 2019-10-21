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
import jp.pioneer.carsync.presentation.presenter.ArtistsPresenter;
import jp.pioneer.carsync.presentation.view.ArtistsView;
import jp.pioneer.carsync.presentation.view.adapter.ArtistAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static jp.pioneer.carsync.presentation.view.fragment.ScreenId.ARTIST_LIST;

/**
 * アーティストリストの画面
 */
@RuntimePermissions
public class ArtistsFragment extends AbstractScreenFragment<ArtistsPresenter, ArtistsView> implements ArtistsView {
    @Inject ArtistsPresenter mPresenter;
    @BindView(R.id.list_view) ListView mListView;
    private ArtistAdapter mArtistAdapter;
    private Unbinder mUnbinder;

    public static ArtistsFragment newInstance(Bundle args) {
        ArtistsFragment fragment = new ArtistsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_music, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mArtistAdapter = new ArtistAdapter(getContext(), null) {
            @Override
            protected void onJacketClick(long id) {
                getPresenter().onArtistPlayAction(id);
            }
        };
        mArtistAdapter.setSphCarDevice(getPresenter().isSphCarDevice());
        mListView.setAdapter(mArtistAdapter);
        mListView.setFastScrollEnabled(true);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArtistsFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ArtistsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
    protected ArtistsPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ARTIST_LIST;
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void setLoaderManager() {
        getPresenter().setLoaderManager(getLoaderManager());
    }

    /**
     * アーティストの設定
     * @param data アーティストのカーソル
     * @param args アーティストのバンドル
     */
    @Override
    public void setArtistCursor(Cursor data, Bundle args) {
        mArtistAdapter.swapCursor(data, args);
    }

    /**
     * 項目リストクリック時
     * @param parent AdapterView
     * @param view View
     * @param position 選択位置
     * @param id 選択ID
     */
    @OnItemClick(R.id.list_view)
    public void onClickListItem(AdapterView<?> parent, View view, int position, long id) {
        ListView listview = (ListView) parent;
        Cursor cursor = (Cursor) listview.getItemAtPosition(position);
        getPresenter().onArtistAlbumListShowAction(cursor, id,false);
    }

    @Override
    public int getSectionIndex(){
        return mArtistAdapter.getSectionForPosition(mListView.getCheckedItemPosition());
    }

    @Override
    public void setSectionIndex(int sectionIndex){
        mListView.setItemChecked(mArtistAdapter.getPositionForSection(sectionIndex), true);
        mListView.setSelection(mArtistAdapter.getPositionForSection(sectionIndex));
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
        return mArtistAdapter.getItemId(position);
    }

    @Override
    public Cursor getItem(int position){
        return (Cursor) mArtistAdapter.getItem(position);
    }

    @Override
    public int getItemsCount(){
        return mArtistAdapter.getCount();
    }

    @Override
    public int getSectionCount(){
        return mArtistAdapter.getSectionCount();
    }

    @Override
    public String getSectionString(int sectionIndex) {
        return (String)mArtistAdapter.getSections()[sectionIndex];
    }
}
