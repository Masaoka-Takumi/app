package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.MusicCategory;
import jp.pioneer.carsync.presentation.controller.PlayerTabFragmentController;
import jp.pioneer.carsync.presentation.presenter.PlayerTabContainerPresenter;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;
import jp.pioneer.carsync.presentation.view.PlayerTabContainerView;
import jp.pioneer.carsync.presentation.view.fragment.OnGoBackListener;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AbstractDialogFragment;
import timber.log.Timber;


/**
 * ローカル再生リストタブコンテナ
 */

public class PlayerTabContainerFragment extends AbstractDialogFragment<PlayerTabContainerPresenter, PlayerTabContainerView, AbstractDialogFragment.Callback>
        implements PlayerTabContainerView, OnGoBackListener {
    @Inject PlayerTabContainerPresenter mPresenter;
    @Inject PlayerTabFragmentController mFragmentController;
    @BindView(R.id.directory_pass_text) TextView mPath1;
    @BindView(R.id.directory_pass_text2) TextView mPath2;
    @BindView(R.id.artist_button) RelativeLayout mArtist;
    @BindView(R.id.album_button) RelativeLayout mAlbum;
    @BindView(R.id.song_button) RelativeLayout mSong;
    @BindView(R.id.playlist_button) RelativeLayout mPlaylist;
    @BindView(R.id.genre_button) RelativeLayout mGenre;
    @BindView(R.id.close_button) ImageView mCloseBtn;
    private Unbinder mUnbinder;

    public PlayerTabContainerFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args Bundle
     * @return PlayerTabContainerFragment
     */
    public static PlayerTabContainerFragment newInstance(Bundle args) {
        PlayerTabContainerFragment fragment = new PlayerTabContainerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.BehindScreenStyle);
        setCancelable(false);
        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                getPresenter().onBackAction();
                return true;
            }
            return false;
        });
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_container_player_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mCloseBtn.setVisibility(View.VISIBLE);
        mFragmentController.setContainerViewId(R.id.player_list_container);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogFragmentAnimation;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getCallback() != null) {
            getCallback().onClose(this);
        }
        mUnbinder.unbind();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected PlayerTabContainerPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof Callback;
    }

    @Override
    public boolean onGoBack() {
        if (mFragmentController.goBack()) {
            getPresenter().removeTitle();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigate(ScreenId screenId, Bundle args) {
        if (mFragmentController.navigate(screenId, args)) {
            getPresenter().setTitle(args);
            return true;
        }
        return false;
    }

    @Override
    public void setTitle(String title) {
        String paragrph = getString(R.string.ply_082);
        if(title.contains(paragrph)){
            mPath1.setVisibility(View.VISIBLE);
            mPath2.setVisibility(View.VISIBLE);
            mPath1.setText(title.substring(0,title.indexOf(paragrph)));
            mPath2.setText(title.substring(title.indexOf(paragrph),title.length()));
        }else{
            mPath1.setVisibility(View.GONE);
            mPath2.setText(title);
        }
    }

    @Override
    public void setCategory(MusicCategory category) {
        mArtist.setEnabled(true);
        mAlbum.setEnabled(true);
        mSong.setEnabled(true);
        mPlaylist.setEnabled(true);
        mGenre.setEnabled(true);
        switch (category) {
            case ARTIST:
                mArtist.getChildAt(0).setVisibility(View.VISIBLE);
                mArtist.getChildAt(1).setAlpha(0.85f);
                mAlbum.getChildAt(0).setVisibility(View.INVISIBLE);
                mAlbum.getChildAt(1).setAlpha(0.4f);
                mSong.getChildAt(0).setVisibility(View.INVISIBLE);
                mSong.getChildAt(1).setAlpha(0.4f);
                mPlaylist.getChildAt(0).setVisibility(View.INVISIBLE);
                mPlaylist.getChildAt(1).setAlpha(0.4f);
                mGenre.getChildAt(0).setVisibility(View.INVISIBLE);
                mGenre.getChildAt(1).setAlpha(0.4f);
                mArtist.setEnabled(false);
                break;
            case ALBUM:
                mArtist.getChildAt(0).setVisibility(View.INVISIBLE);
                mArtist.getChildAt(1).setAlpha(0.4f);
                mAlbum.getChildAt(0).setVisibility(View.VISIBLE);
                mAlbum.getChildAt(1).setAlpha(0.85f);
                mSong.getChildAt(0).setVisibility(View.INVISIBLE);
                mSong.getChildAt(1).setAlpha(0.4f);
                mPlaylist.getChildAt(0).setVisibility(View.INVISIBLE);
                mPlaylist.getChildAt(1).setAlpha(0.4f);
                mGenre.getChildAt(0).setVisibility(View.INVISIBLE);
                mGenre.getChildAt(1).setAlpha(0.4f);
                mAlbum.setEnabled(false);
                break;
            case SONG:
                mArtist.getChildAt(0).setVisibility(View.INVISIBLE);
                mArtist.getChildAt(1).setAlpha(0.4f);
                mAlbum.getChildAt(0).setVisibility(View.INVISIBLE);
                mAlbum.getChildAt(1).setAlpha(0.4f);
                mSong.getChildAt(0).setVisibility(View.VISIBLE);
                mSong.getChildAt(1).setAlpha(0.85f);
                mPlaylist.getChildAt(0).setVisibility(View.INVISIBLE);
                mPlaylist.getChildAt(1).setAlpha(0.4f);
                mGenre.getChildAt(0).setVisibility(View.INVISIBLE);
                mGenre.getChildAt(1).setAlpha(0.4f);
                mSong.setEnabled(false);
                break;
            case PLAYLIST:
                mArtist.getChildAt(0).setVisibility(View.INVISIBLE);
                mArtist.getChildAt(1).setAlpha(0.4f);
                mAlbum.getChildAt(0).setVisibility(View.INVISIBLE);
                mAlbum.getChildAt(1).setAlpha(0.4f);
                mSong.getChildAt(0).setVisibility(View.INVISIBLE);
                mSong.getChildAt(1).setAlpha(0.4f);
                mPlaylist.getChildAt(0).setVisibility(View.VISIBLE);
                mPlaylist.getChildAt(1).setAlpha(0.85f);
                mGenre.getChildAt(0).setVisibility(View.INVISIBLE);
                mGenre.getChildAt(1).setAlpha(0.4f);
                mPlaylist.setEnabled(false);
                break;
            case GENRE:
                mArtist.getChildAt(0).setVisibility(View.INVISIBLE);
                mArtist.getChildAt(1).setAlpha(0.4f);
                mAlbum.getChildAt(0).setVisibility(View.INVISIBLE);
                mAlbum.getChildAt(1).setAlpha(0.4f);
                mSong.getChildAt(0).setVisibility(View.INVISIBLE);
                mSong.getChildAt(1).setAlpha(0.4f);
                mPlaylist.getChildAt(0).setVisibility(View.INVISIBLE);
                mPlaylist.getChildAt(1).setAlpha(0.4f);
                mGenre.getChildAt(0).setVisibility(View.VISIBLE);
                mGenre.getChildAt(1).setAlpha(0.85f);
                mGenre.setEnabled(false);
                break;
            default:
                Timber.e("This case is impossible.");
                break;
        }
    }

    @Override
    public void setCategoryEnabled(boolean isEnabled) {
        mArtist.setEnabled(isEnabled);
        mAlbum.setEnabled(isEnabled);
        mSong.setEnabled(isEnabled);
        mPlaylist.setEnabled(isEnabled);
        mGenre.setEnabled(isEnabled);
    }

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    @Override
    public void setColor(@ColorRes int color) {
        ImageView artistSelect = (ImageView)mArtist.getChildAt(0);
        artistSelect.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0012_artistbtn_select_1nrm, color));
        ImageView albumSelect = (ImageView)mAlbum.getChildAt(0);
        albumSelect.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0011_albumbtn_select_1nrm, color));
        ImageView songSelect = (ImageView)mSong.getChildAt(0);
        songSelect.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0009_songsbtn_select_1nrm, color));
        ImageView playlistSelect = (ImageView)mPlaylist.getChildAt(0);
        playlistSelect.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0010_playlistbtn_select_1nrm, color));
        ImageView genreSelect = (ImageView)mGenre.getChildAt(0);
        genreSelect.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0013_genrebtn_select_1nrm, color));
    }

    public boolean isFirstList(){
        ScreenId screenId = mFragmentController.getScreenIdInContainer();
        switch (screenId){
            case ARTIST_LIST:
            case ALBUM_LIST:
            case SONG_LIST:
            case PLAYLIST_LIST:
            case GENRE_LIST:
                return true;
            default:
                return false;
        }
    }
    /**
     * アーティストタブ押下イベント
     */
    @OnClick(R.id.artist_button)
    public void onClickArtistButton() {
        getPresenter().onArtistAction();
    }

    /**
     * アルバムタブ押下イベント
     */
    @OnClick(R.id.album_button)
    public void onClickAlbumButton() {
        getPresenter().onAlbumAction();
    }

    /**
     * 楽曲タブ押下イベント
     */
    @OnClick(R.id.song_button)
    public void onClickSongButton() {
        getPresenter().onSongAction();
    }

    /**
     * プレイリストタブ押下イベント
     */
    @OnClick(R.id.playlist_button)
    public void onClickPlaylistButton() {
        getPresenter().onPlaylistAction();
    }

    /**
     * ジャンルタブ押下イベント
     */
    @OnClick(R.id.genre_button)
    public void onClickGenreButton() {
        getPresenter().onGenreAction();
    }

    /**
     * 戻るボタン押下イベント
     */
    @OnClick(R.id.back_button)
    public void onClickBackButton() {
        getPresenter().onBackAction();
    }

    /**
     * 閉じるボタン押下イベント
     */
    @OnClick(R.id.close_button)
    public void onClickCloseButton() {
        getPresenter().onCloseAction();
    }

    public void closeDialog(){
        this.dismiss();
    }


}
