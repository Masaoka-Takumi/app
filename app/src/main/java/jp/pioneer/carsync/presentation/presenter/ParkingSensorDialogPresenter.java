package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.ParkingSensorStatusChangeEvent;
import jp.pioneer.carsync.domain.event.ParkingSensorDisplayStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.ParkingSensorStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.ParkingSensorDialogView;

/**
 * パーキングセンサー画面のPresenter
 */
@PresenterLifeCycle
public class ParkingSensorDialogPresenter extends Presenter<ParkingSensorDialogView> {
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject GetStatusHolder mGetStatusHolder;

    @Inject
    public ParkingSensorDialogPresenter() {
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        updateView();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    private void updateView() {

        StatusHolder holder = mGetStatusHolder.execute();
        ParkingSensorStatus status = holder.getParkingSensorStatus();
        Optional.ofNullable(getView()).ifPresent(view -> view.setSensor(status));
        boolean isDisplayParkingSensor = holder.getCarDeviceStatus().isDisplayParkingSensor;
        if (!isDisplayParkingSensor) {
            Optional.ofNullable(getView()).ifPresent(ParkingSensorDialogView::dismissDialog);
        }
    }

    /**
     * パーキングセンサーステータス変更イベントハンドラ
     *
     * @param ev パーキングセンサーステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onParkingSensorStatusChangeEvent(ParkingSensorStatusChangeEvent ev) {
        updateView();
    }

    /**
     * リバース線状態変更イベントハンドラ
     *
     * @param ev リバース線状態変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReverseStatusChangeEvent(ParkingSensorDisplayStatusChangeEvent ev) {
        StatusHolder holder = mGetStatusHolder.execute();
        boolean isDisplayParkingSensor = holder.getCarDeviceStatus().isDisplayParkingSensor;
        if (!isDisplayParkingSensor) {
            Optional.ofNullable(getView()).ifPresent(ParkingSensorDialogView::dismissDialog);
        }
    }

}
