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
import jp.pioneer.carsync.domain.event.SoundFxSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.SuperTodorokiSettingChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.presentation.view.TodorokiSettingView;

/**
 * Super轟Sound設定のPresenter
 */
@PresenterLifeCycle
public class TodorokiSettingPresenter extends Presenter<TodorokiSettingView>{
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;
    @Inject PreferSoundFx mFxCase;
    @Inject GetStatusHolder mGetStatusHolder;
    private ArrayList<SuperTodorokiSetting> mTypeArray = new ArrayList<SuperTodorokiSetting>(){{
        add(SuperTodorokiSetting.OFF);
        add(SuperTodorokiSetting.LOW);
        add(SuperTodorokiSetting.HIGH);
        add(SuperTodorokiSetting.SUPER_HIGH);
    }};

    @Inject
    public TodorokiSettingPresenter() {

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
        updateView();
        setEnable();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }


    private SoundFxSetting getFxSetting() {
        StatusHolder holder = mGetStatusHolder.execute();
        return holder.getSoundFxSetting();
    }

    private void updateView() {
        SoundFxSetting fxSetting = getFxSetting();
        Optional.ofNullable(getView()).ifPresent(view -> {
            SuperTodorokiSetting settingType = fxSetting.superTodorokiSetting;
            switch (settingType) {
                case OFF:
                    view.setPresetView(R.raw.p1151_todoroki_off);
                    break;
                case LOW:
                    view.setPresetView(R.raw.p1152_todoroki_low);
                    break;
                case HIGH:
                    view.setPresetView(R.raw.p1153_todoroki_high);
                    break;
                case SUPER_HIGH:
                    view.setPresetView(R.raw.p1154_todoroki_superhigh);
                    break;
                default:
                    break;
            }
            view.setSelectedItem(mTypeArray.indexOf(settingType));
        });
    }

    /**
     * 轟選択アクション
     *
     * @param position 選択位置
     */
    public void onSelectTodorokiTypeAction(int position) {
        mFxCase.setSuperTodoroki(mTypeArray.get(position));
    }

    /**
     * Super轟設定変更イベント.
     *
     * @param event SuperTodorokiSettingChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSuperTodorokiSettingChangeEvent(SuperTodorokiSettingChangeEvent event) {
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
            view.setEnable(fxSpec.superTodorokiSettingSupported&&fxStatus.superTodorokiSettingEnabled&&isFxSettingEnabled);
        });
    }
}
