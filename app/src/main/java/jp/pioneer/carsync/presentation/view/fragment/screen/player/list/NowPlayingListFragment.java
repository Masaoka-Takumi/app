package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.Manifest;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
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
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.presentation.presenter.NowPlayingListPresenter;
import jp.pioneer.carsync.presentation.view.NowPlayingListView;
import jp.pioneer.carsync.presentation.view.adapter.NowPlayAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static jp.pioneer.carsync.presentation.view.fragment.ScreenId.NOW_PLAYING_LIST;

/**
 * NowPlayingListの画面
 */
@RuntimePermissions
public class NowPlayingListFragment extends AbstractScreenFragment<NowPlayingListPresenter, NowPlayingListView>
        implements NowPlayingListView {

    @Inject NowPlayingListPresenter mPresenter;
    @BindView(R.id.list_view) ListView mListView;
    private NowPlayAdapter mNowPlayAdapter;
    private Unbinder mUnbinder;
    public NowPlayingListFragment() {
    }

    public static NowPlayingListFragment newInstance(Bundle args) {
        NowPlayingListFragment fragment = new NowPlayingListFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_now_playing, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mNowPlayAdapter = new NowPlayAdapter(getContext(), null, false);
        mListView.setAdapter(mNowPlayAdapter);
        mListView.setFastScrollEnabled(true);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        NowPlayingListFragmentPermissionsDispatcher.setLoaderManagerWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        NowPlayingListFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
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
    protected NowPlayingListPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return NOW_PLAYING_LIST;
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void setLoaderManager() {
        getPresenter().setLoaderManager(LoaderManager.getInstance(this));
    }

    @Override
    public void setSongCursor(Cursor data, Bundle args) {
        mNowPlayAdapter.swapCursor(data, args);
    }

    @Override
    public void setSelection(int position) {
        mListView.post(new Runnable() {
            @Override
            public void run() {
                if(mListView!=null) {
                    mListView.setSelection(position);
                }
            }
        });
    }

    /**
     * 再生中曲のIDの設定
     *
     * @param trackNo 再生中曲のトラックNo
     * @param id 再生中曲のID
     * @param playbackMode 再生状態
     */
    @Override
    public void setNowPlaySong(int trackNo, long id , PlaybackMode playbackMode){
        boolean isPlaying = false;
        if(playbackMode== PlaybackMode.PLAY){
            isPlaying = true;
        }
        mNowPlayAdapter.setNowPlaySongId(id, isPlaying);
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
        getPresenter().onSongPlayAction(position);
    }

}
