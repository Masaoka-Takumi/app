package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.Manifest;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.content.AppMusicContract;
import jp.pioneer.carsync.presentation.presenter.PlaylistsPresenter;
import jp.pioneer.carsync.presentation.view.PlaylistsView;
import jp.pioneer.carsync.presentation.view.adapter.PlaylistAdapter;
import jp.pioneer.carsync.presentation.view.adapter.PlaylistCardAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.widget.CardRecyclerView;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static jp.pioneer.carsync.presentation.view.fragment.ScreenId.PLAYLIST_LIST;

/**
 * プレイリストの画面
 */
@RuntimePermissions
public class PlaylistsFragment extends AbstractScreenFragment<PlaylistsPresenter, PlaylistsView> implements PlaylistsView {
    @Inject PlaylistsPresenter mPresenter;
    @BindView(R.id.root_view) RelativeLayout mRootView;
    private ViewGroup mListView;
    private PlaylistAdapter mPlaylistAdapter;
    private PlaylistCardAdapter mPlaylistCardAdapter;
    private Unbinder mUnbinder;
    private boolean mIsPlaylistCard = false;
    private LinearLayoutManager mLayoutManager;
    public static PlaylistsFragment newInstance(Bundle args) {
        PlaylistsFragment fragment = new PlaylistsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_variable, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mIsPlaylistCard = mPresenter.isPlaylistCardEnabled();
        setAdapter();
        return view;
    }

    private void setAdapter() {
        if (mListView != null) {
            mRootView.removeView(mListView);
            mListView = null;
        }
        if(mPresenter.isPlaylistCardEnabled()) {
            setCardAdapter();
        }
        else{
            setListAdapter();
        }
        mRootView.addView(mListView);
        PlaylistsFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    private void setListAdapter() {
        mPlaylistAdapter = new PlaylistAdapter(getContext(), null) {
            @Override
            protected void onJacketClick(long id) {
                getPresenter().onPlaylistPlayAction(id);
            }
        };
        mPlaylistAdapter.setSphCarDevice(getPresenter().isSphCarDevice());
        ListView listview = new ListView(getContext());
        AbsListView.LayoutParams params =
                new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        listview.setLayoutParams(params);
        ColorDrawable separate_line_color = new ColorDrawable(ContextCompat.getColor(getContext(),R.color.list_divider));
        listview.setDivider(separate_line_color);
        listview.setDividerHeight((int)getResources().getDimension(R.dimen.list_divider_height));
        listview.setAdapter(mPlaylistAdapter);
        listview.setFastScrollEnabled(true);
        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listview.setPadding((int)getResources().getDimension(R.dimen.player_list_left_padding),0,0,0);
        listview.setOnItemClickListener((parent, view, position, id) -> {
            ListView listView = (ListView) parent;
            Cursor cursor = (Cursor) listView.getItemAtPosition(position);
            getPresenter().onPlaylistSongListShowAction(cursor, id,false);
        });
        mListView = listview;
    }

    private void setCardAdapter() {
        mPlaylistCardAdapter = new PlaylistCardAdapter(getContext(), null) {
            @Override
            public void onItemClick(Cursor cursor) {
                setSelectPosition(cursor.getPosition());
                getPresenter().onPlaylistSongListShowAction(cursor, AppMusicContract.Playlist.getId(cursor),false);
            }

            @Override
            public void onPlayClick(int position) {
                getPresenter().onPlaylistPlayAction(mPlaylistCardAdapter.getItemId(position));
            }
        };
        mPlaylistCardAdapter.setSphCarDevice(getPresenter().isSphCarDevice());
        CardRecyclerView recycler = new CardRecyclerView(getContext(), null, R.attr.verticalRecyclerViewStyle);
        recycler.setAdapter(mPlaylistCardAdapter);
        recycler.setPadding(0,(int)getResources().getDimension(R.dimen.player_list_card_view_padding_top),0,0);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recycler.setLayoutManager(mLayoutManager);
        mListView = recycler;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PlaylistsFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PlaylistsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
    protected PlaylistsPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return PLAYLIST_LIST;
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void setLoaderManager() {
        getPresenter().setLoaderManager(LoaderManager.getInstance(this));
    }

    /**
     * プレイリストの設定
     * @param data プレイリストのカーソル
     * @param args プレイリストのバンドル
     */
    @Override
    public void setPlaylistCursor(Cursor data, Bundle args) {
        if (mPresenter.isPlaylistCardEnabled()){
            mPlaylistCardAdapter.swapAdapter(data, args);
        }
        else {
            mPlaylistAdapter.swapCursor(data, args);
        }
    }
    @Override
    public int getSectionIndex(){
        if (mIsPlaylistCard) {
            return mPlaylistCardAdapter.getSectionForPosition(mPlaylistCardAdapter.getCheckedPos());
        } else {
            return mPlaylistAdapter.getSectionForPosition(((ListView)mListView).getCheckedItemPosition());
        }
    }

    @Override
    public void setSectionIndex(int sectionIndex){
        if (mIsPlaylistCard) {
            mPlaylistCardAdapter.setCheckedPos(mPlaylistCardAdapter.getPositionForSection(sectionIndex));
            //((CardRecyclerView)mListView).smoothScrollToPosition(mPlaylistCardAdapter.getPositionForSection(sectionIndex));
            mLayoutManager.scrollToPosition(mPlaylistCardAdapter.getPositionForSection(sectionIndex));
        } else {
            ((ListView) mListView).setItemChecked(mPlaylistAdapter.getPositionForSection(sectionIndex), true);
            ((ListView) mListView).setSelection(mPlaylistAdapter.getPositionForSection(sectionIndex));
        }
    }

    @Override
    public int getSelectPosition(){
        if (mIsPlaylistCard) {
            return mPlaylistCardAdapter.getCheckedPos();
        } else {
            return ((ListView)mListView).getCheckedItemPosition();
        }
    }

    @Override
    public void setSelectPosition(int position){
        if (mIsPlaylistCard) {
            mPlaylistCardAdapter.setCheckedPos(position);
            //((CardRecyclerView)mListView).smoothScrollToPosition(position);
            mLayoutManager.scrollToPosition(position);
        } else {
            ((ListView) mListView).setItemChecked(position, true);
            ((ListView) mListView).setSelection(position);
        }
    }

    @Override
    public long getItemId(int position){
        if (mIsPlaylistCard) {
            return mPlaylistCardAdapter.getItemId(position);
        } else {
            return mPlaylistAdapter.getItemId(position);
        }
    }

    @Override
    public Cursor getItem(int position){
        if (mIsPlaylistCard) {
            return (Cursor)mPlaylistCardAdapter.getItem(position);
        } else {
            return (Cursor)mPlaylistAdapter.getItem(position);
        }
    }

    @Override
    public int getItemsCount(){
        if (mIsPlaylistCard) {
            return mPlaylistCardAdapter.getCount();
        } else {
            return mPlaylistAdapter.getCount();
        }
    }

    @Override
    public int getSectionCount(){
        if (mIsPlaylistCard) {
            return mPlaylistCardAdapter.getSectionCount();
        } else {
            return mPlaylistAdapter.getSectionCount();
        }
    }

    @Override
    public String getSectionString(int sectionIndex) {
        if (mIsPlaylistCard) {
            return mPlaylistCardAdapter.getSectionString(sectionIndex);
        } else {
            return (String)mPlaylistAdapter.getSections()[sectionIndex];
        }
    }
}
