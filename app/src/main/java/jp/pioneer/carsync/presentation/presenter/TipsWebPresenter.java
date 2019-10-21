package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.view.TipsWebView;

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
    }

    public void setArgument(Bundle args) {
        mUrl = args.getString("url");
    }

    public void onBackAction(){
        mEventBus.post(new GoBackEvent());
    }


}
