package jp.pioneer.carsync.presentation.presenter;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.AudioSettingChangeEvent;
import jp.pioneer.carsync.domain.event.AudioSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferAudio;
import jp.pioneer.carsync.domain.model.LoudnessSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.LoudnessDialogView;

/**
 * Loudness設定のPresenter.
 */
public class LoudnessDialogPresenter extends Presenter<LoudnessDialogView> {
    @Inject GetStatusHolder mGetCase;
    @Inject EventBus mEventBus;
    @Inject PreferAudio mPreferCase;

    @Inject
    public LoudnessDialogPresenter(){

    }

    @Override
    void onTakeView() {
        updateView();
    }

    @Override
    void onResume(){
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    /**
     * オーディオ設定変更イベントハンドラ.
     *
     * @param event オーディオ設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioSettingChangeEvent(AudioSettingChangeEvent event) {
        updateView();
    }

    /**
     * オーディオ設定ステータス変更イベントハンドラ.
     *
     * @param event オーディオ設定ステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioSettingStatusChangeEvent(AudioSettingStatusChangeEvent event) {
        updateView();
    }

    /**
     * 車載機ステータス変更イベントハンドラ
     *
     * @param event 車載機ステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarDeviceStatusChangeEvent(CarDeviceStatusChangeEvent event) {
        updateView();
    }

    private void updateView(){
        Optional.ofNullable(getView()).ifPresent(view -> {
            StatusHolder holder = mGetCase.execute();
            if(holder.isAudioSettingEnabled() && holder.getAudioSettingStatus().loudnessSettingEnabled) {
                LoudnessSetting setting = mGetCase.execute().getAudioSetting().loudnessSetting;
                view.setLoudnessSetting(setting);
            } else {
                view.callbackClose();
            }
        });
    }

    /**
     * リストアイテム選択
     *
     * @param setting アイテム
     */
    public void onSelectAction(LoudnessSetting setting){
        mPreferCase.setLoudness(setting);
    }
}