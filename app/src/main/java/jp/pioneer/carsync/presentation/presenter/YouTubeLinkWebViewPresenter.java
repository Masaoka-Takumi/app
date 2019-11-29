package jp.pioneer.carsync.presentation.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.Analytics;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.AudioMode;
import jp.pioneer.carsync.domain.model.BtAudioInfo;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.ParkingStatus;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.YouTubeLinkWebViewView;
import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderPlayerInfoItem;
import timber.log.Timber;

public class YouTubeLinkWebViewPresenter extends Presenter<YouTubeLinkWebViewView> {

    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject ControlSource mControlSource;
    @Inject Analytics mAnalytics;
    private static final String BASE_YOUTUBE_LINK_URL = "https://m.youtube.com/results?search_query=";
    private static final String NO_TITLE = "No Title";
    private static final String NO_ARTIST = "No Artist";
    private static final String NO_USB_DEVICE = "No USB Device";
    private static final String UNPLAYABLE_FILE = "Unplayable File";

    // TODO アプリがバックグラウンドで車載機ソース切替によりAppMusicから1周してAppMusicソースになっても画面を閉じない
    /**
     * 初回表示時にAppMusicソースに切り替わったかを判断するフラグ
     * 画面表示時にAppMusicソースではない場合は画面を閉じる処理がある関係で
     * 初回表示時はまだAppMusicソースではない場合があり、それによる画面を閉じるのを回避するために導入
     * [フラグを立てるタイミング]AppMusicソース or AppMusicソースに切り替わる
     */
    private boolean mIsSourceChanged = false;
    private final String IS_SOURCE_CHANGED = "is_source_changed";

    @Inject
    public YouTubeLinkWebViewPresenter() {
    }

    @Override
    void onTakeView() {
        Timber.i("Presenter onTakeView");
    }

    /**
     * 初期化ハンドラ.
     * <p>
     * 必要であればオーバーライドする。
     * {@code super.onInitialize()}は不要。
     */
    @Override
    void onInitialize() {
        Timber.i("Presenter onInitialize");
        // フラグの初期化
        mIsSourceChanged = false;
        // YouTubeLinkWebView画面表示状態のセット
        mGetStatusHolder.execute().getAppStatus().isShowYouTubeLinkWebView = true;

        // 現在のソースから検索情報を取得し、URL作成
        StatusHolder holder = mGetStatusHolder.execute();
        MediaSourceType lastSource = holder.getCarDeviceStatus().sourceType;
        String url = getSearchURLForYouTubeLink(lastSource);
        Timber.i("url=" + url);

        // 現在のソースがAppMusicソース以外ならそれを覚えてAppMusicソースに変更
        holder.getAppStatus().lastSourceBeforeYouTubeLink = null;
        if(lastSource != MediaSourceType.APP_MUSIC){
            holder.getAppStatus().lastSourceBeforeYouTubeLink = lastSource;
            mAnalytics.setSourceSelectReason(Analytics.SourceChangeReason.temporarySourceChange);
            mControlSource.selectSource(MediaSourceType.APP_MUSIC);
        } else {
            mIsSourceChanged = true; // 最初からAppMusicソースの場合はフラグを立てる
        }

        // 検索URLをFragmentに渡す
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.loadUrl(url);
        });
    }

    @Override
    void onResume() {
        Timber.i("Presenter onResume");
        if(!mEventBus.isRegistered(this)){
            mEventBus.register(this);
        }
        // YouTubeLinkWebView画面表示状態のセット
        mGetStatusHolder.execute().getAppStatus().isShowYouTubeLinkWebView = true;

        // AppMusicソースではない場合は画面を閉じる
        // (アプリがバックグラウンドだとイベントを受け取ることができないため)
        // 初回起動時はまだソースが切り替わっていない可能性があるため
        // AppMusicソースに切り替わったかどうかをフラグで判断する
        if(mIsSourceChanged) {
            CarDeviceStatus carDeviceStatus = mGetStatusHolder.execute().getCarDeviceStatus();
            if (carDeviceStatus.sourceType != MediaSourceType.APP_MUSIC) {
                Optional.ofNullable(getView()).ifPresent(view -> {
                    view.callbackCloseResetLastSource();
                });
            }
        }

        // パーキングセンスに応じてYouTubeLink走行規制画面を表示
        setYouTubeLinkRegulationVisibility();
    }

    @Override
    void onPause() {
        Timber.i("Presenter onPause");
        mEventBus.unregister(this);
        // YouTubeLinkWebView画面表示状態のセット
        mGetStatusHolder.execute().getAppStatus().isShowYouTubeLinkWebView = false;
    }

    @Override
    void onSaveInstanceState(@NonNull Bundle outState) {
        Timber.i("Presenter onSaveInstanceState");
        outState.putBoolean(IS_SOURCE_CHANGED, mIsSourceChanged);
    }

    @Override
    void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Timber.i("Presenter onRestoreInstanceState");
        mIsSourceChanged = savedInstanceState.getBoolean(IS_SOURCE_CHANGED);
    }

    @Override
    void onDropView() {
        Timber.i("Presenter onDropView");
    }

    @Override
    void onDestroy() {
        Timber.i("Presenter onDestroy");
    }

    /**
     * 戻るボタンタップ時の動作(WebViewが戻れたら戻り、戻れなかったら画面を閉じる)
     */
    public void onBackButtonAction(){
        Timber.i("onBackButtonAction");
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(view.canGoBack()){
                view.goBack();
            } else {
                view.callbackCloseByChangeLastSource();
            }
        });
    }

    /**
     * 閉じるボタンタップ時の動作(YouTubeLinkWebView画面を閉じる)
     */
    public void onCloseButtonAction(){
        Timber.i("onCloseButtonAction");
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.callbackCloseByChangeLastSource();
        });

    }

    /**
     * 検索Tag情報をもとに検索URLを返す
     *
     * @param sourceType 検索情報取得対象のMediaSourceType
     * @return String 検索URL文字列
     */
    private String getSearchURLForYouTubeLink(MediaSourceType sourceType){
        // ソースから検索Tag情報を取得
        String searchTag = getMusicInfoTagForYouTubeLink(sourceType);

        // 検索Tagがない(nullまたは空文字列)場合は検索TagなしのURLを返す
        if(TextUtils.isEmpty(searchTag)){
            return BASE_YOUTUBE_LINK_URL;
        }

        // 検索TagをURLエンコードして連結
        try {
            /**
             * FIXME URLEncoderは間違い
             * RFC3986に準拠したURLエンコード を行う必要がある
             * 実際はYouTube側がいい感じに判断してくれるため問題なく動作しているようである
             */
            String encodedTag = URLEncoder.encode(searchTag, "UTF-8");
            return BASE_YOUTUBE_LINK_URL + encodedTag;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return BASE_YOUTUBE_LINK_URL;
        }
    }

    /**
     * ソースに応じたYouTubeLink検索Tag取得
     * 対応ソースでない場合はTag情報は空文字列を返す
     * Tag情報が両方とも取得できない場合は空文字列を返す
     *
     * @param sourceType 検索情報取得対象のMediaSourceType
     * @return String 検索Tag文字列
     */
    private String getMusicInfoTagForYouTubeLink(MediaSourceType sourceType){
        // 検索Tag情報を取得
        StatusHolder holder = mGetStatusHolder.execute();
        CarDeviceMediaInfoHolder carDeviceMediaInfoHolder = holder.getCarDeviceMediaInfoHolder();
        String tag1 = "";
        String tag2 = "";
        String separator = " "; // 半角スペース
        switch(sourceType){
            case RADIO:
                tag1 = carDeviceMediaInfoHolder.radioInfo.psInfo;
                break;
            case DAB:
                tag1 = carDeviceMediaInfoHolder.dabInfo.dynamicLabel;
                break;
            case USB:
                tag1 = carDeviceMediaInfoHolder.usbMediaInfo.artistName;
                tag2 = carDeviceMediaInfoHolder.usbMediaInfo.songTitle;

                // 検索Tag情報なし扱いかを判定
                if(NO_ARTIST.equals(tag1) || NO_USB_DEVICE.equals(tag1)){
                    tag1 = "";
                }
                if(NO_TITLE.equals(tag2)){
                    tag2 = "";
                }
                if(UNPLAYABLE_FILE.equals(tag1)){
                    tag1 = ""; tag2 = "";
                }
                break;
            case CD:
                tag1 = carDeviceMediaInfoHolder.cdInfo.artistName;
                tag2 = carDeviceMediaInfoHolder.cdInfo.trackNumber;
                break;
            case SPOTIFY:
                tag1 = carDeviceMediaInfoHolder.spotifyMediaInfo.artistName;
                tag2 = carDeviceMediaInfoHolder.spotifyMediaInfo.trackNameOrSpotifyError;
                break;
            case APP_MUSIC:
                AppStatus appStatus = mGetStatusHolder.execute().getAppStatus();
                if(appStatus.appMusicAudioMode == AudioMode.MEDIA) {
                    // AppMusic(local)
                    // FIXME アーティスト情報なしの場合は"<unknown>"が入る、
                    //  YouTubeの仕様上"<",">"が入ると検索Tag情報なし扱いになる
                    tag1 = carDeviceMediaInfoHolder.androidMusicMediaInfo.artistName;
                    tag2 = carDeviceMediaInfoHolder.androidMusicMediaInfo.songTitle;
                    break;
                } else if(appStatus.appMusicAudioMode == AudioMode.ALEXA) {
                    // AppMusic(Alexa)
                    RenderPlayerInfoItem renderPlayerInfoItem = appStatus.playerInfoItem;
                    AlexaIfDirectiveItem.Content content = renderPlayerInfoItem.content;
                    tag1 = content.getTitleSubtext1(); // アーティスト
                    tag2 = content.getTitle(); // 曲名
                    break;
                }
            case BT_AUDIO:
                BtAudioInfo btAudioInfo = carDeviceMediaInfoHolder.btAudioInfo;
                tag1 = btAudioInfo.artistName;
                tag2 = btAudioInfo.songTitle;
                PlaybackMode playbackMode = btAudioInfo.playbackMode;

                // 検索Tag情報なし扱いかを判定
                if(NO_ARTIST.equals(tag1) || btAudioInfo.isConnecting()
                        || btAudioInfo.isNoService() || playbackMode == PlaybackMode.STOP){
                    tag1 = "";
                }
                if(!btAudioInfo.isConnecting()
                        && (NO_TITLE.equals(tag2) || btAudioInfo.isNoService() || playbackMode == PlaybackMode.STOP)){
                    tag2 = "";
                }
                break;
        }
        Timber.i("artist=%s, songtitle=%s,", tag1, tag2);
        // TODO [簡潔簡単に] API24以降ならStringJoinerを使える
        // Tagを連結して検索Tag文字列を生成
        StringBuilder result = new StringBuilder();
        if(tag1 != null){
            result.append(tag1);
        }
        if(!TextUtils.isEmpty(tag2)){
            if(result.toString().length() != 0){
                // tag1があり、tag2があるときにseparatorを連結
                result.append(separator);
            }
            result.append(tag2);
        }

        Timber.i("getMusicInfoTagForYouTubeLink Tag=%s", result.toString());
        return result.toString();
    }

    /**
     * パーキング状態に応じてYouTubeLink走行規制画面を表示/消す
     */
    private void setYouTubeLinkRegulationVisibility(){
        StatusHolder holder = mGetStatusHolder.execute();
        ParkingStatus parkingStatus = holder.getCarDeviceStatus().parkingStatus;
        if(parkingStatus == ParkingStatus.OFF){
            // YouTubeLink走行規制画面を表示
            Optional.ofNullable(getView()).ifPresent(view -> {
                view.closeKeyBoard();
                view.setVisibleYouTubeLinkRegulation();
            });
        } else {
            // YouTubeLink走行規制画面を消す
            Optional.ofNullable(getView()).ifPresent(view -> {
                view.setGoneYouTubeLinkRegulation();
            });
        }
    }


    /**
     * YouTubeLinkWebView画面表示状態でソースが切り替わったら画面を閉じる
     * (初回起動時の関係でAppMusicソース以外に変わったら閉じる)
     * @param event MediaSourceTypeChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaSourceTypeChangeEvent(MediaSourceTypeChangeEvent event) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            StatusHolder holder = mGetStatusHolder.execute();
            MediaSourceType currentSourceType = holder.getCarDeviceStatus().sourceType;
            Timber.i("MediaSourceTypeChangeEvent currentSourceType=" + currentSourceType);
            if(currentSourceType != MediaSourceType.APP_MUSIC) {
                // AppMusicソース以外に切り替わったら画面を閉じる
                view.callbackCloseResetLastSource();
            } else {
                // AppMusicソースに切り替わったらフラグを立てる
                mIsSourceChanged = true;
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarDeviceStatusChangeEvent(CarDeviceStatusChangeEvent event){
        // パーキング状態に応じてYouTubeLink走行規制画面を表示
        Timber.i("CarDeviceStatusChangeEvent");
        setYouTubeLinkRegulationVisibility();
    }
}
