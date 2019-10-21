package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.presentation.view.UsbListContainerView;

/**
 * Created by NSW00_007906 on 2017/12/22.
 */
@PresenterLifeCycle
public class UsbListContainerPresenter extends Presenter<UsbListContainerView> {
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    /**
     * コンストラクタ
     */
    @Inject
    public UsbListContainerPresenter() {
    }

}
