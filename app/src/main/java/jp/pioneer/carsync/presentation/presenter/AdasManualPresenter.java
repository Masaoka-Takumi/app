package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.presentation.view.AdasManualView;

public class AdasManualPresenter extends Presenter<AdasManualView> {
    @Inject EventBus mEventBus;
    @Inject Context mContext;

    /**
     * コンストラクタ.
     */
    @Inject
    public AdasManualPresenter() {
    }


}