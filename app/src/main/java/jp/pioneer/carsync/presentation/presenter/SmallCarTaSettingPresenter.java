package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.SoundFxSettingChangeEvent;
import jp.pioneer.carsync.domain.event.SoundFxSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.ListeningPosition;
import jp.pioneer.carsync.domain.model.SmallCarTaSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.SmallCarTaSettingView;

/**
 * mallCarTa設定画面のPresenter
 */
@PresenterLifeCycle
public class SmallCarTaSettingPresenter extends Presenter<SmallCarTaSettingView>{
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;
    @Inject PreferSoundFx mFxCase;
    @Inject GetStatusHolder mGetStatusHolder;
    private ArrayList<SmallCarTaSettingType> mTypeArray = new ArrayList<SmallCarTaSettingType>(){{
        add(SmallCarTaSettingType.OFF);
        add(SmallCarTaSettingType.COMPACT);
        add(SmallCarTaSettingType.STANDARD);
        add(SmallCarTaSettingType.INTERMEDIATE);
        add(SmallCarTaSettingType.SUV_PREMIUM);
    }};

    @Inject
    public SmallCarTaSettingPresenter() {

    }

    @Override
    void onTakeView() {

        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setAdapter(mTypeArray);
            view.setColor(mPreference.getUiColor().getResource());
        });

    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        setEnable();
        updateView();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    /**
     * Fx設定取得
     * @return SoundFxSetting
     */
    public SoundFxSetting getFxSetting() {
        StatusHolder holder = mGetStatusHolder.execute();
        return holder.getSoundFxSetting();
    }

    private void updateView() {
        SoundFxSetting fxSetting = getFxSetting();
        Optional.ofNullable(getView()).ifPresent(view -> {
            SmallCarTaSettingType settingType = fxSetting.smallCarTaSetting.smallCarTaSettingType;
            ListeningPosition seatPosition = fxSetting.smallCarTaSetting.listeningPosition;
            switch (settingType) {
                case OFF:
                    view.setSelectedItem(SmallCarTaSettingType.OFF.ordinal());
                    view.setSeatTypeSettingEnabled(false);
                    view.setPresetView(0);
                    break;
                case COMPACT:
                    view.setSelectedItem(SmallCarTaSettingType.COMPACT.ordinal());
                    view.setSeatTypeSettingEnabled(true);
                    if(seatPosition == ListeningPosition.LEFT){
                        view.setPresetView(R.drawable.p0690_esf_preset_a_l);
                    }else if(seatPosition == ListeningPosition.RIGHT){
                        view.setPresetView(R.drawable.p0691_esf_preset_a_r);
                    }
                    break;
                case STANDARD:
                    view.setSelectedItem(SmallCarTaSettingType.STANDARD.ordinal());
                    view.setSeatTypeSettingEnabled(true);
                    if(seatPosition == ListeningPosition.LEFT){
                        view.setPresetView(R.drawable.p0692_esf_preset_b_l);
                    }else if(seatPosition == ListeningPosition.RIGHT){
                        view.setPresetView(R.drawable.p0693_esf_preset_b_r);
                    }
                    break;
                case INTERMEDIATE:
                    view.setSelectedItem(SmallCarTaSettingType.INTERMEDIATE.ordinal());
                    view.setSeatTypeSettingEnabled(true);
                    if(seatPosition == ListeningPosition.LEFT){
                        view.setPresetView(R.drawable.p0694_esf_preset_c_l);
                    }else if(seatPosition == ListeningPosition.RIGHT){
                        view.setPresetView(R.drawable.p0695_esf_preset_c_r);
                    }
                    break;
                case SUV_PREMIUM:
                    view.setSelectedItem(SmallCarTaSettingType.SUV_PREMIUM.ordinal());
                    view.setSeatTypeSettingEnabled(true);
                    if(seatPosition == ListeningPosition.LEFT){
                        view.setPresetView(R.drawable.p0696_esf_preset_d_l);
                    }else if(seatPosition == ListeningPosition.RIGHT){
                        view.setPresetView(R.drawable.p0697_esf_preset_d_r);
                    }
                    break;
                default:
                    break;
            }
            view.setSeatType(fxSetting.smallCarTaSetting.listeningPosition);
        });
    }

    /**
     * SmallCarTaSettingアクション
     *
     * @param type 種別
     * @param position 位置
     */
    public void onSelectSmallCarTaSettingAction(SmallCarTaSettingType type, ListeningPosition position) {
        mFxCase.setSmallCarTa(type, position);
    }

    /**
     * SoundFx設定の更新通知
     *
     * @param event SoundFxChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSoundFxChangeEvent(SoundFxSettingChangeEvent event) {
        updateView();
    }

    /**
     * Sound FX設定ステータス変更通知
     *
     * @param event SoundFxSettingStatusChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSoundFxSettingStatusChangeEvent(SoundFxSettingStatusChangeEvent event) {
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
        SoundFxSettingSpec fxSpec = holder.getCarDeviceSpec().soundFxSettingSpec;
        SoundFxSettingStatus fxStatus = holder.getSoundFxSettingStatus();
        boolean isFxSettingEnabled = holder.getCarDeviceStatus().soundFxSettingEnabled &&
                holder.getCarDeviceSpec().soundFxSettingSupported;

        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setEnable(fxSpec.smallCarTaSettingSupported&&fxStatus.smallCarTaSettingEnabled&&isFxSettingEnabled);
        });
    }
}
