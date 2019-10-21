package jp.pioneer.carsync.presentation.presenter;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.AudioSettingChangeEvent;
import jp.pioneer.carsync.domain.event.AudioSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferAudio;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.FaderBalanceSettingView;

/**
 * Created by NSW00_906320 on 2017/07/20.
 */
@PresenterLifeCycle
public class FaderBalanceSettingPresenter extends Presenter<FaderBalanceSettingView> {
    @Inject EventBus mEventBus;
    @Inject PreferAudio mPreferAudio;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject AppSharedPreference mAppSharedPreference;

    @Inject
    public FaderBalanceSettingPresenter() {
    }

    @Override
    void onTakeView() {
        Optional.ofNullable(getView()).ifPresent(view -> view.setColor(mAppSharedPreference.getUiColor().getResource()));
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        Optional.ofNullable(getView()).ifPresent(FaderBalanceSettingView::onStatusUpdated);
        setEnable();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    /**
     * StatusHolder取得
     *
     * @return StatusHolder
     */
    public StatusHolder getStatusHolder() {
        return mGetStatusHolder.execute();
    }

    /**
     * UiColor取得
     *
     * @return Color
     */
    public int getUiColor() {
        return mAppSharedPreference.getUiColor().getResource();
    }

    /**
     * Fader/Balance設定
     *
     * @param fader
     * @param balance
     */
    public void setFaderBalance(int fader, int balance) {
        mPreferAudio.setFaderBalance(fader, balance);
    }

    /**
     * Audio設定変更イベント通知
     *
     * @param event Audio設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioSettingChangeAction(AudioSettingChangeEvent event) {
        Optional.ofNullable(getView()).ifPresent(FaderBalanceSettingView::onStatusUpdated);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioSettingStatusChangeEvent(AudioSettingStatusChangeEvent event) {
        setEnable();
    }

    /**
     * 車載機ステータス変更イベントハンドラ
     *
     * @param event 車載機ステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarDeviceStatusChangeEvent(CarDeviceStatusChangeEvent event) {
        setEnable();
    }

    private void setEnable(){
        StatusHolder holder = mGetStatusHolder.execute();
        AudioSettingStatus status = holder.getAudioSettingStatus();
        boolean isAudioSettingEnabled = holder.isAudioSettingEnabled() &&
                holder.isAudioSettingSupported();

        Optional.ofNullable(getView()).ifPresent(view -> view.setEnable(
                (status.faderSettingEnabled || status.balanceSettingEnabled) &&
                isAudioSettingEnabled));
    }
}
