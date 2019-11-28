package jp.pioneer.carsync.presentation.presenter;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import java.util.Set;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.Analytics;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.interactor.ControlSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.YouTubeLinkContainerView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import timber.log.Timber;

@PresenterLifeCycle
public class YouTubeLinkContainerPresenter extends Presenter<YouTubeLinkContainerView> {

    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject ControlSource mControlSource;
    @Inject Analytics mAnalytics;
    @Inject
    public YouTubeLinkContainerPresenter() {
    }

    @Override
    void onInitialize() {
        Optional.ofNullable(getView()).ifPresent(view ->{
            if(mPreference.isYouTubeLinkCautionNoDisplayAgain()){
                // YouTubeLinkWebView画面を表示
                view.onNavigate(ScreenId.YOUTUBE_LINK_WEBVIEW, null);
            }
            else {
                // YouTubeLinkCaution画面を表示
                view.onNavigate(ScreenId.YOUTUBE_LINK_CAUTION, null);
            }
        });
    }

    /**
     * 再開ハンドラ.
     * <p>
     * 必要であればオーバーライドする。
     * {@code super.onResume()}は不要。
     */
    @Override
    void onResume() {
        Timber.i("Presenter onResume");
    }

    /**
     * 一時停止ハンドラ.
     * <p>
     * 必要であればオーバーライドする。
     * {@code super.onPause()}は不要。
     */
    @Override
    void onPause() {
        Timber.i("Presenter onPause");
    }

    /**
     * YouTubeLinkContainerDialogを閉じる処理
     * YouTubeLinkWebView画面表示前のソースに復帰することで
     * MediaSourceTypeChangeEventを利用して閉じる
     * 復帰するソースが無効な場合はソースOFFを車載機に送信する
     * ラストソースが設定されていない場合は直接ダイアログを閉じる
     */
    public void closeContainerDialogByChangeLastSource(){
        Timber.i("closeContainerDialogByChangeLastSource");
        // ラストソース読み込み
        StatusHolder holder = mGetStatusHolder.execute();
        CarDeviceStatus carDeviceStatus = holder.getCarDeviceStatus();
        MediaSourceType lastSource = holder.getAppStatus().lastSourceBeforeYouTubeLink;

        if(lastSource != null){
            // ソースが切り替わることにより画面が閉じる
            Timber.i("changeSource->closeDialog");
            Set<MediaSourceType> availableSources = carDeviceStatus.availableSourceTypes;
            if(availableSources.contains(lastSource)){
                // 有効なソースならそれに戻る
                mControlSource.selectSource(lastSource);

            } else {
                // 無効なソースならソースOFF
                mControlSource.selectSource(MediaSourceType.OFF);
            }
            mAnalytics.setSourceSelectReason(Analytics.SourceChangeReason.temporarySourceChangeBack);
            // ラストソースをクリア
            holder.getAppStatus().lastSourceBeforeYouTubeLink = null;
        } else {
            // ソースを戻す必要がない時は直接画面を閉じる
            Optional.ofNullable(getView()).ifPresent(view ->{
                Timber.i("closeDialog");
                view.dismissDialog();
            });
        }
    }

    /**
     * 割り込みによるYouTubeLinkContainerDialogを閉じる処理
     * YouTubeLinkWebView画面表示前のソースを忘れてから
     * 直接ContainerDialogを閉じる
     */
    public void closeContainerDialogResetLastSource(){
        Timber.i("closeContainerDialogResetLastSource");
        // ラストソースをクリア
        StatusHolder holder = mGetStatusHolder.execute();
        holder.getAppStatus().lastSourceBeforeYouTubeLink = null;
        // 画面を閉じる
        Optional.ofNullable(getView()).ifPresent(view ->{
            view.dismissDialog();
        });
    }
}
