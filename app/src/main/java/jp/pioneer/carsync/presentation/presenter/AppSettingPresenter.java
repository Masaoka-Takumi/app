package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.util.YouTubeLinkStatus;
import jp.pioneer.carsync.presentation.view.AppSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;

/**
 * App設定画面のPresenter
 */
@PresenterLifeCycle
public class AppSettingPresenter extends Presenter<AppSettingView> {
    private static final String TAG_DIALOG_SERVICE_RESIDENT_OFF = "tag_dialog_service_resident_off";
    @Inject AppSharedPreference mPreference;
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetStatusHolder;

    @Inject YouTubeLinkStatus mYouTubeLinkStatus;

    /**
     * コンストラクタ
     */
    @Inject
    public AppSettingPresenter() {
    }

    @Override
    void onTakeView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setShortCutSettingEnabled(isShortCutSettingEnabled());
            view.setShortCutEnabled(mPreference.isShortCutButtonEnabled());
            view.setAlbumArtEnabled(mPreference.isAlbumArtEnabled());
            view.setGenreCardEnabled(mPreference.isGenreCardEnabled());
            view.setPlaylistCardEnabled(mPreference.isPlaylistCardEnabled());
            view.setAppServiceResidentEnabled(mPreference.isAppServiceResident());
        });
    }

    /**
     * ショートカットボタン設定項目のアクティブ/非アクティブの設定
     * Alexa利用可能なら、音声認識がAlexa or YouTubeLink設定ONのときは非アクティブ化
     * Alexa利用不可能なら、YouTubeLink設定ONのときは非アクティブ化
     * それ以外はアクティブ化
     * @return {@code true}:アクティブ {@code false}:非アクティブ
     */
    private boolean isShortCutSettingEnabled() {
        if(mGetStatusHolder.execute().getAppStatus().isAlexaAvailableCountry) {
            return !(mPreference.getVoiceRecognitionType() == VoiceRecognizeType.ALEXA || mYouTubeLinkStatus.isYouTubeLinkEnabled());
        } else {
            return !mYouTubeLinkStatus.isYouTubeLinkEnabled();
        }
    }

    /**
     * ShortCutButton押下時の処理
     *
     * @param isValue 有効/無効
     */
    public void onShortCutButtonChange(boolean isValue) {
        mPreference.setShortCutButtonEnabled(isValue);
        if(!mPreference.isConfiguredShortCutButtonEnabled()){
            mPreference.setConfiguredShortCutButtonEnabled(true);
        }
    }

    /**
     * AlbumArtList押下時の処理
     */
    public void onAlbumArtChange() {
        boolean isAlbumArtEnabled = mPreference.isAlbumArtEnabled();
        mPreference.setAlbumArtEnabled(!isAlbumArtEnabled);
        Optional.ofNullable(getView()).ifPresent(view -> view.setAlbumArtEnabled(mPreference.isAlbumArtEnabled()));
    }

    /**
     * GenreListView押下時の処理
     */
    public void onGenreCardChange(){
        boolean isGenreCardEnabled = mPreference.isGenreCardEnabled();
        mPreference.setGenreCardEnabled(!isGenreCardEnabled);
        Optional.ofNullable(getView()).ifPresent(view -> view.setGenreCardEnabled(mPreference.isGenreCardEnabled()));
    }

    /**
     * PlaylistView押下時の処理
     */
    public void onPlaylistChange(){
        boolean isPlaylistCardEnabled = mPreference.isPlaylistCardEnabled();
        mPreference.setPlayListViewId(!isPlaylistCardEnabled);
        Optional.ofNullable(getView()).ifPresent(view -> view.setPlaylistCardEnabled(mPreference.isPlaylistCardEnabled()));

    }

    /**
     * 常時待ち受け設定押下時の処理
     *
     * @param isEnabled 有効/無効
     */
    public void onAppServiceResidentChange(boolean isEnabled) {
        mPreference.setAppServiceResident(isEnabled);
        if(!isEnabled){
            Bundle bundle = new Bundle();
            bundle.putString(StatusPopupDialogFragment.TAG, TAG_DIALOG_SERVICE_RESIDENT_OFF);
            bundle.putString(StatusPopupDialogFragment.MESSAGE, mContext.getString(R.string.set_394));
            bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
            mEventBus.post(new NavigateEvent(ScreenId.STATUS_DIALOG, bundle));
        }
    }

}
