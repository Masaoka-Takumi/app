package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.Manifest;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import jp.pioneer.carsync.presentation.presenter.GenresPresenter;
import jp.pioneer.carsync.presentation.view.GenresView;
import jp.pioneer.carsync.presentation.view.adapter.GenreAdapter;
import jp.pioneer.carsync.presentation.view.adapter.GenreCardAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.widget.CardRecyclerView;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static jp.pioneer.carsync.presentation.view.fragment.ScreenId.GENRE_LIST;

/**
 * ジャンルリストの画面
 */
@RuntimePermissions
public class GenresFragment extends AbstractScreenFragment<GenresPresenter, GenresView> implements GenresView {
    @Inject GenresPresenter mPresenter;
    @BindView(R.id.root_view) RelativeLayout mRootView;
    private ViewGroup mListView;
    private GenreAdapter mGenreAdapter;
    private GenreCardAdapter mGenreCardAdapter;
    private Unbinder mUnbinder;
    private boolean mIsGenreCard = false;
    private LinearLayoutManager mLayoutManager;
    public static GenresFragment newInstance(Bundle args) {
        GenresFragment fragment = new GenresFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_variable, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mIsGenreCard = mPresenter.isGenreCardEnabled();
        setAdapter();
        return view;
    }

    private void setAdapter() {
        if (mListView != null) {
            mRootView.removeView(mListView);
            mListView = null;
        }
        if (mPresenter.isGenreCardEnabled()) {
            setCardAdapter();
        }
        else {
            setListAdapter();
        }
        mRootView.addView(mListView);
        GenresFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    private void setListAdapter() {
        mGenreAdapter = new GenreAdapter(getContext(), null) {
            @Override
            protected void onJacketClick(long id) {
                getPresenter().onGenrePlayAction(id);
            }
        };
        mGenreAdapter.setSphCarDevice(getPresenter().isSphCarDevice());
        ListView listview = new ListView(getContext());
        AbsListView.LayoutParams params =
                new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        listview.setLayoutParams(params);
        ColorDrawable separate_line_color = new ColorDrawable(ContextCompat.getColor(getContext(),R.color.list_divider));
        listview.setDivider(separate_line_color);
        listview.setDividerHeight((int)getResources().getDimension(R.dimen.list_divider_height));
        listview.setAdapter(mGenreAdapter);
        listview.setFastScrollEnabled(true);
        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listview.setPadding((int)getResources().getDimension(R.dimen.player_list_left_padding),0,0,0);
        listview.setOnItemClickListener((parent, view, position, id) -> {
            ListView listView = (ListView) parent;
            Cursor cursor = (Cursor) listView.getItemAtPosition(position);
            getPresenter().onGenreSongListShowAction(cursor, id,false);
        });
        mListView = listview;
    }

    private void setCardAdapter() {
        mGenreCardAdapter = new GenreCardAdapter(getContext(), null) {
            @Override
            public void onItemClick(Cursor cursor) {
                setSelectPosition(cursor.getPosition());
                getPresenter().onGenreSongListShowAction(cursor, AppMusicContract.Playlist.getId(cursor),false);
            }

            @Override
            public void onPlayClick(int position) {
                getPresenter().onGenrePlayAction(mGenreCardAdapter.getItemId(position));
            }
        };
        mGenreCardAdapter.setSphCarDevice(getPresenter().isSphCarDevice());
        CardRecyclerView recycler = new CardRecyclerView(getContext(), null, R.attr.verticalRecyclerViewStyle);
        recycler.setAdapter(mGenreCardAdapter);
        recycler.setPadding(0,(int)getResources().getDimension(R.dimen.player_list_card_view_padding_top),0,0);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recycler.setLayoutManager(mLayoutManager);
        mListView = recycler;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GenresFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        GenresFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
    protected GenresPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return GENRE_LIST;
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void setLoaderManager() {
        getPresenter().setLoaderManager(getLoaderManager());
    }

    /**
     * ジャンルの設定
     * @param data ジャンルのカーソル
     * @param args ジャンルのバンドル
     */
    @Override
    public void setGenreCursor(Cursor data, Bundle args) {
        if (mPresenter.isGenreCardEnabled()) {
            mGenreCardAdapter.swapAdapter(data, args);
        }
        else {
            mGenreAdapter.swapCursor(data, args);
        }
    }

    @Override
    public int getSectionIndex(){
        if (mIsGenreCard) {
            return mGenreCardAdapter.getSectionForPosition(mGenreCardAdapter.getCheckedPos());
        } else {
            return mGenreAdapter.getSectionForPosition(((ListView)mListView).getCheckedItemPosition());
        }
    }

    @Override
    public void setSectionIndex(int sectionIndex){
        if (mIsGenreCard) {
            mGenreCardAdapter.setCheckedPos(mGenreCardAdapter.getPositionForSection(sectionIndex));
            mLayoutManager.scrollToPosition(mGenreCardAdapter.getPositionForSection(sectionIndex));
        } else {
            ((ListView) mListView).setItemChecked(mGenreAdapter.getPositionForSection(sectionIndex), true);
            ((ListView) mListView).setSelection(mGenreAdapter.getPositionForSection(sectionIndex));
        }
    }

    @Override
    public int getSelectPosition(){
        if (mIsGenreCard) {
            return mGenreCardAdapter.getCheckedPos();
        } else {
            return ((ListView)mListView).getCheckedItemPosition();
        }
    }

    @Override
    public void setSelectPosition(int position){
        if (mIsGenreCard) {
            mGenreCardAdapter.setCheckedPos(position);
            mLayoutManager.scrollToPosition(position);
        } else {
            ((ListView) mListView).setItemChecked(position, true);
            ((ListView) mListView).setSelection(position);
        }
    }

    @Override
    public long getItemId(int position){
        if (mIsGenreCard) {
            return mGenreCardAdapter.getItemId(position);
        } else {
            return mGenreAdapter.getItemId(position);
        }
    }

    @Override
    public Cursor getItem(int position){
        if (mIsGenreCard) {
            return (Cursor)mGenreCardAdapter.getItem(position);
        } else {
            return (Cursor)mGenreAdapter.getItem(position);
        }
    }

    @Override
    public int getItemsCount(){
        if (mIsGenreCard) {
            return mGenreCardAdapter.getCount();
        } else {
            return mGenreAdapter.getCount();
        }
    }

    @Override
    public int getSectionCount(){
        if (mIsGenreCard) {
            return mGenreCardAdapter.getSectionCount();
        } else {
            return mGenreAdapter.getSectionCount();
        }
    }

    @Override
    public String getSectionString(int sectionIndex) {
        if (mIsGenreCard) {
            return mGenreCardAdapter.getSectionString(sectionIndex);
        } else {
            return (String)mGenreAdapter.getSections()[sectionIndex];
        }
    }
}
