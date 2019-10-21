package jp.pioneer.carsync.presentation.presenter;

import android.os.Handler;
import android.os.Looper;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.DabFunctionSettingChangeEvent;
import jp.pioneer.carsync.domain.event.DabFunctionSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferDabFunction;
import jp.pioneer.carsync.domain.model.DabFunctionSetting;
import jp.pioneer.carsync.domain.model.DabFunctionSettingSpec;
import jp.pioneer.carsync.domain.model.DabFunctionSettingStatus;
import jp.pioneer.carsync.domain.model.DabInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TASetting;
import jp.pioneer.carsync.presentation.view.DabSettingView;

/**
 * Dab設定画面のPresenter.
 */
@PresenterLifeCycle
public class DabSettingPresenter extends Presenter<DabSettingView> {
    @Inject GetStatusHolder mGetCase;
    @Inject EventBus mEventBus;
    @Inject PreferDabFunction mPreferCase;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * コンストラクタ.
     */
    @Inject
    public DabSettingPresenter() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onTakeView() {
        updateView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    /**
     * Radio Function設定変更イベントハンドラ.
     *
     * @param event Radio Function設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDabFunctionSettingChangeEvent(DabFunctionSettingChangeEvent event) {
        mHandler.postDelayed(this::updateView, 100);
    }

    /**
     * Radio Function設定状態変更イベントハンドラ.
     *
     * @param event Radio Function設定状態変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDabFunctionSettingStatusChangeEvent(DabFunctionSettingStatusChangeEvent event) {
        mHandler.postDelayed(this::updateView, 100);
    }

    /**
     * 車載機ステータス変更イベントハンドラ
     *
     * @param event 車載機ステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarDeviceStatusChangeEvent(CarDeviceStatusChangeEvent event) {
        mHandler.postDelayed(this::updateView, 100);
    }

    private void updateView() {
        Optional.ofNullable(getView()).ifPresent(view -> {

            StatusHolder holder = mGetCase.execute();
            DabInfo info = holder.getCarDeviceMediaInfoHolder().dabInfo;
            boolean isDabSettingEnabled = holder.getCarDeviceStatus().dabFunctionSettingEnabled &&
                    holder.getCarDeviceSpec().dabFunctionSettingSupported;
            DabFunctionSettingSpec spec = holder.getCarDeviceSpec().dabFunctionSettingSpec;
            DabFunctionSettingStatus status = holder.getDabFunctionSettingStatus();
            DabFunctionSetting setting = holder.getDabFunctionSetting();
            view.setTaSetting(
                    spec.taSettingSupported,
                    status.taSettingEnabled && isDabSettingEnabled,
                    setting.taSetting
            );
            view.setServiceFollowSetting(
                    spec.serviceFollowSettingSupported,
                    status.serviceFollowSettingEnabled && isDabSettingEnabled,
                    setting.serviceFollowSetting
            );
            view.setSoftLinkSetting(
                    spec.softlinkSettingSupported,
                    status.softlinkSettingEnabled && isDabSettingEnabled,
                    setting.softlinkSetting
            );
        });
    }

    /**
     * TA設定選択処理.
     */
    public void onSelectTaSettingAction() {
        StatusHolder holder = mGetCase.execute();
        DabFunctionSetting setting = holder.getDabFunctionSetting();
        TASetting taSetting = setting.taSetting;
        mPreferCase.setTa(taSetting.toggle());
    }

    public void onSelectServiceFollowAction(boolean setting){
        mPreferCase.setServiceFollow(setting);
    }

    public void onSelectSoftLinkAction(boolean setting){
        mPreferCase.setSoftLink(setting);
    }

}