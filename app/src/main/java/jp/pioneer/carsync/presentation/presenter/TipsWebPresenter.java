package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.webkit.WebResourceRequest;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.TipsWebView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import timber.log.Timber;

/**
 * TipsWebPresenter
 */
@PresenterLifeCycle
public class TipsWebPresenter extends Presenter<TipsWebView>{
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    private String mUrl = "";
    /**
     * コンストラクタ
     */
    @Inject
    public TipsWebPresenter() {
    }

    @Override
    void onTakeView() {
        Optional.ofNullable(getView()).ifPresent(view -> view.loadUrl(mUrl));
        Timber.i("TipsWebP onTakeView");
    }

    public void setArgument(Bundle args) {
        mUrl = args.getString("url");
    }

    public void onBackAction(){
        mEventBus.post(new GoBackEvent());
    }


    public boolean navigateScreen(WebResourceRequest request) {
        return navigateScreen(request.getUrl().getScheme(), request.getUrl().getHost());
    }

    public boolean navigateScreen(String scheme, String host) {
        // TODO #5224 URLがpss://を含まなければreturn false
        if(!"pss".equals(scheme)) {
            return false;
        }

        ScreenId target;
        switch (host) {
            case "SettingsTop":
                target = ScreenId.SETTINGS_CONTAINER;
                break;
            default:
                return true;
        }
        mEventBus.post(new NavigateEvent(target, Bundle.EMPTY));
        return true;
    }
}
