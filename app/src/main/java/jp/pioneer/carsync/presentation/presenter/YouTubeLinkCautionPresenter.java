package jp.pioneer.carsync.presentation.presenter;

import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.YouTubeLinkCautionView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import timber.log.Timber;

@PresenterLifeCycle
public class YouTubeLinkCautionPresenter extends Presenter<YouTubeLinkCautionView> {

    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    @Inject GetStatusHolder mGetStatusHolder;
    private MediaSourceType mCurrentSourceType;

    @Inject
    public YouTubeLinkCautionPresenter() {
    }

    @Override
    void onTakeView() {
        super.onTakeView();
        Timber.i("Presenter onTakeView");
    }

    @Override
    void onInitialize() {
        super.onInitialize();
        // 画面表示時のソースを覚える(バックグラウンドでソースが切り替わった場合に画面を閉じるため)
        mCurrentSourceType = mGetStatusHolder.execute().getCarDeviceStatus().sourceType;
    }

    @Override
    void onResume() {
        super.onResume();
        Timber.i("Presenter onResume");
        if(!mEventBus.isRegistered(this)){
            mEventBus.register(this);
        }

        // ソースが画面表示時と異なる場合は画面を閉じる
        // (アプリがバックグラウンドだとイベントを受け取ることができないため)
        StatusHolder holder = mGetStatusHolder.execute();
        if(holder.getCarDeviceStatus().sourceType != mCurrentSourceType){
            Optional.ofNullable(getView()).ifPresent(view ->{
                view.callbackCloseResetLastSource();
            });
        }

        // NoDisplayAgainのチェック状態をセット(画面回転対策)
        Optional.ofNullable(getView()).ifPresent(view -> {
            boolean isChecked = mPreference.isYouTubeLinkCautionNoDisplayAgain();
            view.updateCheckBox(isChecked);
        });
    }

    @Override
    void onPause() {
        super.onPause();
        Timber.i("Presenter onPause");
        mEventBus.unregister(this);
    }

    /**
     * NoDisplayAgainのチェック状態をPreferenceに保存
     */
    public void saveNoDisplayAgainStatus(boolean isNoDisplayAgain){
        mPreference.setYouTubeLinkCautionNoDisplayAgain(isNoDisplayAgain);
    }

    /**
     * OKボタンタップ時の動作
     */
    public void onConfirmAction(){
        // YouTubeLinkWebView画面遷移
        Timber.i("YouTubeLinkCaution OK");
        mEventBus.post(new NavigateEvent(ScreenId.YOUTUBE_LINK_WEBVIEW, Bundle.EMPTY));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaSourceTypeChangeEvent(MediaSourceTypeChangeEvent ev){
        // ソースが切り替わったらYouTubeLinkCaution画面を閉じる
        Timber.i("MediaSourceTypeChangeEvent");
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.callbackCloseResetLastSource();
        });
    }

}
