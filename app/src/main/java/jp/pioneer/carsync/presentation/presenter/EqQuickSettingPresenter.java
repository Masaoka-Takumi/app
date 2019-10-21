package jp.pioneer.carsync.presentation.presenter;

import android.os.Bundle;
import android.support.annotation.Size;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.SoundFxSettingChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.AudioSettingSpec;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.CustomBandSetting;
import jp.pioneer.carsync.domain.model.CustomEqType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.EqQuickSettingView;
import jp.pioneer.carsync.presentation.view.argument.EqSettingParams;


/**
 * EQ Quick SettingのPresenter
 */
@PresenterLifeCycle
public class EqQuickSettingPresenter extends Presenter<EqQuickSettingView>{
    @Inject PreferSoundFx mFxCase;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject AppSharedPreference mPreference;
    private static final int BAND_DATA_COUNT = 31; //全Band数
    private CustomEqType mCustomType = CustomEqType.CUSTOM1;

    @Inject
    public EqQuickSettingPresenter() {
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        Optional.ofNullable(getView()).ifPresent(view -> view.setColor(mPreference.getUiColor().getResource()));
        updateView();
        setEnable();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    /**
     * 選択中Customタイプの設定
     *
     * @param args Bundle
     */
    public void setCustomType(Bundle args) {
        EqSettingParams params = EqSettingParams.from(args);
        mCustomType = params.customType;
    }

    public CustomEqType getCustomType() {
        return mCustomType;
    }

    private SoundFxSetting getFxSetting(){
        StatusHolder holder = mGetStatusHolder.execute();
        return holder.getSoundFxSetting();
    }

    private void updateView(){
        SoundFxSetting fxSetting = getFxSetting();
        CustomBandSetting bandSetting;
        switch (mCustomType) {
            case CUSTOM1:
                bandSetting = fxSetting.customBandSettingA;
                break;
            case CUSTOM2:
                bandSetting = fxSetting.customBandSettingB;
                break;
            default:
                bandSetting = fxSetting.customBandSettingA;
                break;
        }
        float[] bands = new float[BAND_DATA_COUNT];
        //bands= bandSetting.bands;
        //配列の深いコピー
        if(bandSetting.bands!=null&&bandSetting.bands.length==BAND_DATA_COUNT) {
            System.arraycopy(bandSetting.bands, 0, bands, 0, bandSetting.bands.length);
        }
        Optional.ofNullable(getView()).ifPresent(view -> view.setBandData(bands));
    }

    /**
     * Band設定値変更アクション
     * @param bands Band配列
     */
    public void onChangeBandValueAction(@Size(31)float bands[]){
//        CustomBandSetting bandSetting = new CustomBandSetting(mCustomType);
//        //bandSetting.bands = bands;
//        //配列の深いコピー
//        System.arraycopy(bands,0,bandSetting.bands,0,bands.length);
        mFxCase.setCustomBand(mCustomType, bands);
    }

    /**
     * SoundFx設定の更新通知
     * @param event SoundFxChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSoundFxChangeEvent(SoundFxSettingChangeEvent event) {
        updateView();
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
        AudioSettingSpec audioSpec = holder.getCarDeviceSpec().audioSettingSpec;
        AudioSettingStatus audioStatus = holder.getAudioSettingStatus();
        boolean isFxSettingEnabled = holder.getCarDeviceStatus().soundFxSettingEnabled &&
                holder.getCarDeviceSpec().soundFxSettingSupported;

        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setEnable(audioSpec.equalizerSettingSupported&&audioStatus.equalizerSettingEnabled&&isFxSettingEnabled);
        });
    }

}
