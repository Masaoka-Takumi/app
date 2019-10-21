package jp.pioneer.carsync.presentation.presenter;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.NaviGuideVoiceSettingChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferNaviGuideVoice;
import jp.pioneer.carsync.domain.model.NaviGuideVoiceVolumeSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.GuidanceVolumeDialogView;

/**
 * ナビガイド音声音量設定のPresenter.
 */
public class GuidanceVolumeDialogPresenter extends Presenter<GuidanceVolumeDialogView> {
    @Inject GetStatusHolder mGetCase;
    @Inject EventBus mEventBus;
    @Inject PreferNaviGuideVoice mPreferCase;

    @Inject
    public GuidanceVolumeDialogPresenter(){

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
     * ナビガイド音声設定変更イベントハンドラ.
     *
     * @param event ナビガイド音声設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNaviGuideVoiceSettingChangeEvent(NaviGuideVoiceSettingChangeEvent event) {
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
            if(holder.getCarDeviceStatus().naviGuideVoiceSettingEnabled) {
                NaviGuideVoiceVolumeSetting setting = mGetCase.execute().getNaviGuideVoiceSetting().naviGuideVoiceVolumeSetting;
                view.setVolumeSetting(setting);
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
    public void onSelectAction(NaviGuideVoiceVolumeSetting setting){
        mPreferCase.setNaviGuideVoiceVolume(setting);
    }
}
