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
import jp.pioneer.carsync.presentation.presenter.SongsPresenter;
import jp.pioneer.carsync.presentation.view.SongsView;
import jp.pioneer.carsync.presentation.view.adapter.SongAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static jp.pioneer.carsync.presentation.view.fragment.ScreenId.SONG_LIST;

/**
 * 楽曲リストの画面
 */
@RuntimePermissions
public class SongsFragment extends AbstractScreenFragment<SongsPresenter, SongsView> implements SongsView {
    @Inject SongsPresenter mPresenter;
    @BindView(R.id.list_view) ListView mListView;
    private SongAdapter mSongAdapter;
    private Unbinder mUnbinder;

    public static SongsFragment newInstance(Bundle args) {
        SongsFragment fragment = new SongsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_music, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mSongAdapter = new SongAdapter(getContext(), null, true);
        mSongAdapter.setSphCarDevice(getPresenter().isSphCarDevice());
        mListView.setAdapter(mSongAdapter);
        mListView.setFastScrollEnabled(true);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SongsFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        SongsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
    protected SongsPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return SONG_LIST;
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void setLoaderManager() {
        getPresenter().setLoaderManager(getLoaderManager());
    }

    @Override
    public void setSongCursor(Cursor data, Bundle args) {
        mSongAdapter.swapCursor(data, args);
    }

    /**
     * 項目リスト選択
     * @param parent AdapterView
     * @param view View
     * @param position int
     * @param id 選択ID
     */
    @OnItemClick(R.id.list_view)
    public void onClickListItem(AdapterView<?> parent, View view, int position, long id) {
        getPresenter().onSongPlayAction(id);
    }

    @Override
    public int getSectionIndex(){
        return mSongAdapter.getSectionForPosition(mListView.getCheckedItemPosition());
    }

    @Override
    public String getSectionString(int sectionIndex) {
        return (String)mSongAdapter.getSections()[sectionIndex];
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
    public Cursor getItem(int position){
        return (Cursor) mSongAdapter.getItem(position);
    }
    @Override
    public int getItemsCount(){
        return mSongAdapter.getCount();
    }
    @Override
    public int getSectionCount(){
        return mSongAdapter.getSectionCount();
    }

}
