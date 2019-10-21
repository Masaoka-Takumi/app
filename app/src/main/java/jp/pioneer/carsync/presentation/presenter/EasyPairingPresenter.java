package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.EasyPairingView;
import jp.pioneer.carsync.presentation.view.argument.PermissionParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * 簡単ペアリング画面のPresenter.
 */
@PresenterLifeCycle
public class EasyPairingPresenter extends Presenter<EasyPairingView> {
    private PermissionParams mParams;
    @Inject Context mContext;
    @Inject EventBus mEventBus;

    @Inject
    public EasyPairingPresenter() {
    }

    public void setArgument(Bundle args) {
        mParams = PermissionParams.from(args);

    }

    /**
     * 戻るボタン処理
     */
    public void onBackAction() {
        mEventBus.post(new GoBackEvent());
    }

    public void showPairingSelectDialog(){
        mEventBus.post(new NavigateEvent(ScreenId.PAIRING_SELECT, Bundle.EMPTY));
    }


}
