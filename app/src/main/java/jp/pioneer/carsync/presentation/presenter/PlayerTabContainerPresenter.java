package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.RotaryKeyEvent;
import jp.pioneer.carsync.domain.event.SmartPhoneControlCommandEvent;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.MusicCategory;
import jp.pioneer.carsync.domain.model.RotaryKeyAction;
import jp.pioneer.carsync.domain.model.SmartPhoneControlCommand;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SubDisplayInfo;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.ListFocusEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.event.TopListEvent;
import jp.pioneer.carsync.presentation.model.AndroidMusicListType;
import jp.pioneer.carsync.presentation.view.PlayerTabContainerView;
import jp.pioneer.carsync.presentation.view.argument.MusicParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * ローカル再生リストタブコンテナのpresenter
 */
@PresenterLifeCycle
public class PlayerTabContainerPresenter extends ListPresenter<PlayerTabContainerView> {
    @Inject ControlMediaList mControlMediaList;
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mUseCase;
    @Inject AppSharedPreference mPreference;
    private ArrayList<String> mTitles = new ArrayList<>();
    private MusicCategory mCategory;
    private boolean mIsTopList = true;
    private Set<String> mCategoryName = new HashSet<>();
    private boolean mIsFirstList = true;

    /**
     * コンストラクタ
     */
    @Inject
    public PlayerTabContainerPresenter() {
    }

    @Override
    void onInitialize() {
        mCategoryName.add(mContext.getString(R.string.ply_036));
        mCategoryName.add(mContext.getString(R.string.ply_025));
        mCategoryName.add(mContext.getString(R.string.ply_006));
        mCategoryName.add(mContext.getString(R.string.ply_008));
        mCategoryName.add(mContext.getString(R.string.ply_016));
        StatusHolder holder = mUseCase.execute();
        mCategory = holder.getAppStatus().musicCategory;
        Optional.ofNullable(getView()).ifPresent(view -> {
            switch (mCategory) {
                case SONG:
                    view.onNavigate(ScreenId.SONG_LIST, createMusicParams(mContext.getString(R.string.ply_036)));
                    break;
                case PLAYLIST:
                    view.onNavigate(ScreenId.PLAYLIST_LIST, createMusicParams(mContext.getString(R.string.ply_025)));
                    break;
                case ALBUM:
                    view.onNavigate(ScreenId.ALBUM_LIST, createMusicParams(mContext.getString(R.string.ply_006)));
                    break;
                case ARTIST:
                    view.onNavigate(ScreenId.ARTIST_LIST, createMusicParams(mContext.getString(R.string.ply_008)));
                    break;
                case GENRE:
                    view.onNavigate(ScreenId.GENRE_LIST, createMusicParams(mContext.getString(R.string.ply_016)));
                    break;
            }
            notifyTopListInfo();
            view.setCategory(mCategory);
        });

    }

    @Override
    void onTakeView() {
        Optional.ofNullable(getView()).ifPresent(view -> view.setColor(mPreference.getUiColor().getResource()));
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }

        super.onResume();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
        saveCategory();
        super.onPause();
    }

    @Override
    void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setTitle(getTitle());
            view.setCategory(mCategory);
            setCategoryEnabled();
        });
    }

    private void saveCategory(){
        StatusHolder holder = mUseCase.execute();
        holder.getAppStatus().musicCategory = mCategory;
    }

    @Override
    public void onClose() {
        Optional.ofNullable(getView()).ifPresent(PlayerTabContainerView::closeDialog);
    }


    /**
     * 表示中カテゴリの設定
     *
     * @param args Bundle
     */
    public void setTitle(Bundle args) {
        MusicParams params = MusicParams.from(args);
        if (params.pass != null) {
            mTitles.add(params.pass);
        }
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setTitle(getTitle());
            view.setCategory(mCategory);
        });
        setCategoryEnabled();
    }

    /**
     * 表示中カテゴリの削除
     * <p>
     * 画面戻しの際にひとつ前のカテゴリに戻す
     */
    public void removeTitle() {
        int index = mTitles.size() - 1;
        mTitles.remove(index);
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setTitle(getTitle());
            view.setCategory(mCategory);
        });
        setCategoryEnabled();
    }

    private void setCategoryEnabled() {
        mIsFirstList = false;
        for (String name : mCategoryName) {
            if (getTitle().equals(name)) {
                mIsFirstList = true;
                break;
            }
        }
        if (!mIsFirstList) {
            Optional.ofNullable(getView()).ifPresent(view -> view.setCategoryEnabled(true));
        }
    }

    /**
     * アーティストタブ選択アクション
     */
    public void onArtistAction() {
        mCategory = MusicCategory.ARTIST;
        saveCategory();
        mEventBus.post(new NavigateEvent(ScreenId.ARTIST_LIST, createMusicParams(mContext.getString(R.string.ply_008))));
        Optional.ofNullable(getView()).ifPresent(view -> view.setCategory(MusicCategory.ARTIST));
        mIsTopList = true;
        notifyTopListInfo();
        mEventBus.post(new TopListEvent());
    }

    /**
     * アルバムタブ選択アクション
     */
    public void onAlbumAction() {
        mCategory = MusicCategory.ALBUM;
        saveCategory();
        mEventBus.post(new NavigateEvent(ScreenId.ALBUM_LIST, createMusicParams(mContext.getString(R.string.ply_006))));
        Optional.ofNullable(getView()).ifPresent(view -> view.setCategory(MusicCategory.ALBUM));
        mIsTopList = true;
        notifyTopListInfo();
        mEventBus.post(new TopListEvent());
    }

    /**
     * 楽曲タブ選択アクション
     */
    public void onSongAction() {
        mCategory = MusicCategory.SONG;
        saveCategory();
        mEventBus.post(new NavigateEvent(ScreenId.SONG_LIST, createMusicParams(mContext.getString(R.string.ply_036))));
        Optional.ofNullable(getView()).ifPresent(view -> view.setCategory(MusicCategory.SONG));
        mIsTopList = true;
        notifyTopListInfo();
        mEventBus.post(new TopListEvent());
    }

    /**
     * プレイリスト選択アクション
     */
    public void onPlaylistAction() {
        mCategory = MusicCategory.PLAYLIST;
        saveCategory();
        mEventBus.post(new NavigateEvent(ScreenId.PLAYLIST_LIST, createMusicParams(mContext.getString(R.string.ply_025))));
        Optional.ofNullable(getView()).ifPresent(view -> view.setCategory(MusicCategory.PLAYLIST));
        mIsTopList = true;
        notifyTopListInfo();
        mEventBus.post(new TopListEvent());
    }

    /**
     * ジャンル選択アクション
     */
    public void onGenreAction() {
        mCategory = MusicCategory.GENRE;
        saveCategory();
        mEventBus.post(new NavigateEvent(ScreenId.GENRE_LIST, createMusicParams(mContext.getString(R.string.ply_016))));
        Optional.ofNullable(getView()).ifPresent(view -> view.setCategory(MusicCategory.GENRE));
        mIsTopList = true;
        notifyTopListInfo();
        mEventBus.post(new TopListEvent());
    }

    /**
     * 戻るボタン処理
     */
    public void onBackAction() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (view.isFirstList()) {
                onCloseAction();
            } else {
                mEventBus.post(new GoBackEvent());
            }
        });
    }

    private Bundle createMusicParams(String pass) {
        MusicParams params = new MusicParams();
        params.pass = pass;
        return params.toBundle();
    }

    private String getTitle() {
        int index = mTitles.size() - 1;
        return mTitles.get(index);
    }

    /**
     * ロータリーキーイベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRotaryKeyAction(RotaryKeyEvent event) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (mIsTopList && view.isFirstList()) {
                int value = 0;
                if (event.action == RotaryKeyAction.CLOCKWISE) {
                    value = event.value;
                } else if (event.action == RotaryKeyAction.COUNTERCLOCKWISE) {
                    value = -event.value;
                }
                switch (event.action) {
                    case PUSH:
                        //階層を下げる
                        mIsTopList = false;
                        notifyChildListInfo();
                        mEventBus.post(new ListFocusEvent(event.action, event.value));
                        return;
                    case CLOCKWISE:
                    case COUNTERCLOCKWISE:
                        switch (MusicCategory.toggle(mCategory, value)) {
                            case SONG:
                                onSongAction();
                                break;
                            case PLAYLIST:
                                onPlaylistAction();
                                break;
                            case ALBUM:
                                onAlbumAction();
                                break;
                            case ARTIST:
                                onArtistAction();
                                break;
                            case GENRE:
                                onGenreAction();
                                break;
                        }
                        notifyTopListInfo();
                        break;
                }
            } else {
                mEventBus.post(new ListFocusEvent(event.action, event.value));
            }
        });
    }

    /**
     * SmartPhone操作コマンドイベント.
     * <p>
     * 車載機からSmartPhoneを操作する場合に発生する。
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSmartPhoneControlCommandAction(SmartPhoneControlCommandEvent event) {
        SmartPhoneControlCommand command = event.command;
        if (command == SmartPhoneControlCommand.BACK) {
            CarDeviceStatus status = mUseCase.execute().getCarDeviceStatus();
            ListType current = status.listType;
            Optional.ofNullable(getView()).ifPresent(view -> {
                if (mIsTopList&& view.isFirstList()) {
                    onCloseAction();
                } else if (current == ListType.LIST && view.isFirstList()) {
                    //第一階層の場合TopListに戻る
                    mIsTopList = true;
                    notifyTopListInfo();
                    mEventBus.post(new TopListEvent());
                } else if (current == ListType.ABC_SEARCH_LIST) {
                    mControlMediaList.enterList(ListType.LIST);
                } else {
                    mIsTopList = false;
                    mEventBus.post(new GoBackEvent());
                }
            });
        }
    }


    private void notifyTopListInfo() {
        if(mStatusHolder.execute().getCarDeviceStatus().sourceType == MediaSourceType.APP_MUSIC) {
            switch (mCategory) {
                case SONG:
                    notifySelectListInfo(AndroidMusicListType.TOP_SONG, mContext.getString(SubDisplayInfo.SONGS.label));
                    break;
                case PLAYLIST:
                    notifySelectListInfo(AndroidMusicListType.TOP_PLAYLIST, mContext.getString(SubDisplayInfo.PLAYLISTS.label));
                    break;
                case ALBUM:
                    notifySelectListInfo(AndroidMusicListType.TOP_ALBUM, mContext.getString(SubDisplayInfo.ALBUMS.label));
                    break;
                case ARTIST:
                    notifySelectListInfo(AndroidMusicListType.TOP_ARTIST, mContext.getString(SubDisplayInfo.ARTISTS.label));
                    break;
                case GENRE:
                    notifySelectListInfo(AndroidMusicListType.TOP_GENRE, mContext.getString(SubDisplayInfo.GENRES.label));
                    break;
            }
        }
    }

    private void notifyChildListInfo() {
        if(mStatusHolder.execute().getCarDeviceStatus().sourceType == MediaSourceType.APP_MUSIC) {
            switch (mCategory) {
                case SONG:
                    notifySelectListInfo(AndroidMusicListType.SONG, mContext.getString(SubDisplayInfo.SONGS.label));
                    break;
                case PLAYLIST:
                    notifySelectListInfo(AndroidMusicListType.PLAYLIST, mContext.getString(SubDisplayInfo.PLAYLISTS.label));
                    break;
                case ALBUM:
                    notifySelectListInfo(AndroidMusicListType.ALBUM, mContext.getString(SubDisplayInfo.ALBUMS.label));
                    break;
                case ARTIST:
                    notifySelectListInfo(AndroidMusicListType.ARTIST, mContext.getString(SubDisplayInfo.ARTISTS.label));
                    break;
                case GENRE:
                    notifySelectListInfo(AndroidMusicListType.GENRE, mContext.getString(SubDisplayInfo.GENRES.label));
                    break;
            }
        }
    }

    private void notifySelectListInfo(AndroidMusicListType listType, String text) {
        if(mStatusHolder.execute().getCarDeviceStatus().sourceType == MediaSourceType.APP_MUSIC) {
            mControlMediaList.notifySelectedListInfo(listType.hasParent, listType.hasChild, listType.position, listType.displayInfo, text);
        }
    }
}
