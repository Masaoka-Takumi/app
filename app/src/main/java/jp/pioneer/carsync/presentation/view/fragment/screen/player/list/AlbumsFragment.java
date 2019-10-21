package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.AlbumsPresenter;
import jp.pioneer.carsync.presentation.view.AlbumsView;
import jp.pioneer.carsync.presentation.view.adapter.AbstractCursorAdapter;
import jp.pioneer.carsync.presentation.view.adapter.AlbumAdapter;
import jp.pioneer.carsync.presentation.view.adapter.AlbumArtAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static jp.pioneer.carsync.presentation.view.fragment.ScreenId.ALBUM_LIST;

/**
 * アルバムリストの画面
 */
@RuntimePermissions
public class AlbumsFragment extends AbstractScreenFragment<AlbumsPresenter, AlbumsView> implements AlbumsView {
    @Inject AlbumsPresenter mPresenter;
    @BindView(R.id.root_view) RelativeLayout mRootView;
    private ViewGroup mListView;
    private AbstractCursorAdapter mAdapter;
    private Unbinder mUnbinder;
    private boolean mIsAlbumArt = false;
    public static AlbumsFragment newInstance(Bundle args) {
        AlbumsFragment fragment = new AlbumsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_variable, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mIsAlbumArt = mPresenter.isAlbumArtEnabled();
        setAdapter();
        return view;
    }

    private void setAdapter() {
        if (mListView != null) {
            mRootView.removeView(mListView);
            mListView = null;
        }
        if (mPresenter.isAlbumArtEnabled()) {
            setGridAdapter();
        } else {
            setListAdapter();
        }
        mRootView.addView(mListView);
        AlbumsFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    private void setListAdapter() {
        mAdapter = new AlbumAdapter(getContext(), null) {
            @Override
            protected void onJacketClick(long id) {
                getPresenter().onAlbumPlayAction(id);
            }
        };
        ((AlbumAdapter)mAdapter).setSphCarDevice(getPresenter().isSphCarDevice());
        ListView listview = new ListView(getContext());
        AbsListView.LayoutParams params =
                new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        listview.setLayoutParams(params);
        ColorDrawable separate_line_color = new ColorDrawable(ContextCompat.getColor(getContext(), R.color.list_divider));
        listview.setDivider(separate_line_color);
        listview.setDividerHeight((int) getResources().getDimension(R.dimen.list_divider_height));
        listview.setAdapter(mAdapter);
        listview.setFastScrollEnabled(true);
        listview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listview.setPadding((int)getResources().getDimension(R.dimen.player_list_left_padding),0,0,0);
        listview.setOnItemClickListener((parent, view, position, id) -> {
            ListView listView = (ListView) parent;
            Cursor cursor = (Cursor) listView.getItemAtPosition(position);
            getPresenter().onAlbumSongListShowAction(cursor, id, false);
        });
        mListView = listview;
    }

    @SuppressLint("InflateParams")
    private void setGridAdapter() {
        mAdapter = new AlbumArtAdapter(getContext(), null);
        ((AlbumArtAdapter)mAdapter).setSphCarDevice(getPresenter().isSphCarDevice());
        LayoutInflater inflater = LayoutInflater.from(getContext());
        Configuration config = getContext().getResources().getConfiguration();

        GridView gridview = (GridView) inflater.inflate(R.layout.element_list_album_gridview, null, false);
        gridview.setAdapter(mAdapter);
        //アルバムアート表示ではインデックス表示しない
        gridview.setFastScrollEnabled(false);
        gridview.setNumColumns(config.orientation == Configuration.ORIENTATION_PORTRAIT ? 3 : 4);
        gridview.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        gridview.setOnItemClickListener((parent, view, position, id) -> {
            GridView gridView = (GridView) parent;
            Cursor cursor = (Cursor) gridView.getItemAtPosition(position);
            getPresenter().onAlbumSongListShowAction(cursor, id, false);
        });
        mListView = gridview;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AlbumsFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        AlbumsFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mIsAlbumArt) {
            ((GridView)mListView).setAdapter(null);
        } else {
            ((ListView)mListView).setAdapter(null);
        }
        mRootView.removeAllViews();
        mUnbinder.unbind();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected AlbumsPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ALBUM_LIST;
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void setLoaderManager() {
        getPresenter().setLoaderManager(getLoaderManager());
    }

    /**
     * アルバムの設定
     *
     * @param data アルバムのカーソル
     * @param args アルバムのバンドル
     */
    @Override
    public void setAlbumCursor(Cursor data, Bundle args) {
        mAdapter.swapCursor(data, args);
    }

    @Override
    public int getSectionIndex(){
        if (mIsAlbumArt) {
            return mAdapter.getSectionForPosition(((GridView)mListView).getCheckedItemPosition());
        } else {
            return mAdapter.getSectionForPosition(((ListView)mListView).getCheckedItemPosition());
        }
    }

    @Override
    public void setSectionIndex(int sectionIndex){
        if (mIsAlbumArt) {
            ((GridView) mListView).setItemChecked(mAdapter.getPositionForSection(sectionIndex), true);
            //((GridView)mListView).smoothScrollToPosition(mAdapter.getPositionForSection(sectionIndex));
            smoothScrollToPosition((GridView) mListView, mAdapter.getPositionForSection(sectionIndex));
            ((AlbumArtAdapter) mAdapter).setSelectedPosition(mAdapter.getPositionForSection(sectionIndex));
        } else {
            ((ListView) mListView).setItemChecked(mAdapter.getPositionForSection(sectionIndex), true);
            ((ListView) mListView).setSelection(mAdapter.getPositionForSection(sectionIndex));
        }
    }

    @Override
    public int getSelectPosition(){
        if (mIsAlbumArt) {
            return ((GridView)mListView).getCheckedItemPosition();
        } else {
            return ((ListView)mListView).getCheckedItemPosition();
        }
    }

    @Override
    public void setSelectPosition(int position){
        if (mIsAlbumArt) {
            ((GridView) mListView).setItemChecked(position, true);
            //((GridView)mListView).smoothScrollToPosition(position);
            smoothScrollToPosition((GridView) mListView, position);
            ((AlbumArtAdapter) mAdapter).setSelectedPosition(position);
        } else {
            ((ListView) mListView).setItemChecked(position, true);
            ((ListView) mListView).setSelection(position);
        }
    }

    @Override
    public long getItemId(int position){
        return mAdapter.getItemId(position);
    }

    @Override
    public Cursor getItem(int position){
        return (Cursor) mAdapter.getItem(position);
    }

    @Override
    public int getItemsCount(){
        return mAdapter.getCount();
    }

    @Override
    public int getSectionCount(){
        return mAdapter.getSectionCount();
    }

    @Override
    public String getSectionString(int sectionIndex) {
        return (String)mAdapter.getSections()[sectionIndex];
    }

    //Androidのバグ:AbsListView.smoothScrollToPositionFromTop does not scroll correctly when position is the next visible item
    public static void smoothScrollToPosition(final AbsListView view, final int position) {
        View child = getChildAtPosition(view, position);
        // There's no need to scroll if child is already at top or view is already scrolled to its end
        if ((child != null) && ((child.getTop() == 0) || ((child.getTop() > 0) && !view.canScrollVertically(1)))) {
            return;
        }

        view.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    view.setOnScrollListener(null);

                    // Fix for scrolling bug
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            view.setSelection(position);
                        }
                    });
                }
            }

            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                                 final int totalItemCount) { }
        });

        // Perform scrolling to position
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                view.smoothScrollToPositionFromTop(position, 0);
            }
        });
    }

    public static View getChildAtPosition(final AdapterView view, final int position) {
        final int index = position - view.getFirstVisiblePosition();
        if ((index >= 0) && (index < view.getChildCount())) {
            return view.getChildAt(index);
        } else {
            return null;
        }
    }
}
