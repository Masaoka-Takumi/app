package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.AudioSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.EqualizerSettingChangeEvent;
import jp.pioneer.carsync.domain.event.LiveSimulationSettingChangeEvent;
import jp.pioneer.carsync.domain.event.SuperTodorokiSettingChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.AudioMode;
import jp.pioneer.carsync.domain.model.AudioSettingSpec;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CustomBandSetting;
import jp.pioneer.carsync.domain.model.CustomEqType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingEqualizerType;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.EqSettingView;
import jp.pioneer.carsync.presentation.view.argument.EqSettingParams;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * PresetEQ設定のPresenter
 */
@PresenterLifeCycle
public class EqSettingPresenter extends Presenter<EqSettingView> {
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;
    @Inject PreferSoundFx mFxCase;
    @Inject GetStatusHolder mGetStatusHolder;
    private static final int BAND_DATA_COUNT = 31; //全Band数
    private CustomEqType mCustomType = CustomEqType.CUSTOM1;
    private List<SoundFxSettingEqualizerType> mTypeArray;

    @Inject
    public EqSettingPresenter() {

    }

    @Override
    void onTakeView() {
        StatusHolder holder = mGetStatusHolder.execute();
        CarDeviceSpec spec = holder.getCarDeviceSpec();
        mTypeArray = new ArrayList<>();

        for(SoundFxSettingEqualizerType type : spec.soundFxSettingSpec.supportedEqualizers) {
            if(!mPreference.isDebugSpecialEqEnabled() && type.code >= (1 << 8)){
                continue;
            }
            mTypeArray.add(type);
        }

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

    private void updateView() {
         Optional.ofNullable(getView()).ifPresent(view -> {
             StatusHolder holder = mGetStatusHolder.execute();
             SoundFxSetting fxSetting = holder.getSoundFxSetting();
             SoundFxSettingStatus fxStatus = holder.getSoundFxSettingStatus();
             SoundFxSettingEqualizerType settingType = null;

             if(fxStatus.liveSimulationSettingEnabled && fxStatus.superTodorokiSettingEnabled){
                if(fxSetting.superTodorokiSetting != SuperTodorokiSetting.OFF ||
                        fxSetting.liveSimulationSetting.soundFieldControlSettingType != SoundFieldControlSettingType.OFF){
                    settingType = SoundFxSettingEqualizerType.FLAT;
                    view.setPresetView(R.drawable.preseteq_flat);
                }
            }

            if(!fxStatus.liveSimulationSettingEnabled && fxStatus.superTodorokiSettingEnabled){
                if(fxSetting.superTodorokiSetting != SuperTodorokiSetting.OFF){
                    settingType = SoundFxSettingEqualizerType.FLAT;
                    view.setPresetView(R.drawable.preseteq_flat);
                }
            }

            if(settingType == null) {
                settingType = fxSetting.soundFxSettingEqualizerType;
            }

             float[] bands = new float[BAND_DATA_COUNT];
             CustomBandSetting bandSetting;
             switch (settingType) {
                 case SUPER_BASS:
                     view.setPresetView(R.drawable.preseteq_super_bass);
                     break;
                 case POWERFUL:
                     view.setPresetView(R.drawable.preseteq_powerful);
                     break;
                 case NATURAL:
                     view.setPresetView(R.drawable.preseteq_natural);
                     break;
                 case VOCAL:
                     view.setPresetView(R.drawable.preseteq_vocal);
                     break;
                 case TODOROKI:
                     view.setPresetView(R.drawable.preseteq_todoroki);
                     break;
                 case POP_ROCK:
                     view.setPresetView(R.drawable.preseteq_poprock);
                     break;
                 case ELECTRONICA:
                     view.setPresetView(R.drawable.preseteq_eletronica);
                     break;
                 case EQ_SAMBA:
                     view.setPresetView(R.drawable.preseteq_samba);
                     break;
                 case SERTANEJO:
                     view.setPresetView(R.drawable.preseteq_sertanejo);
                     break;
                 case PRO:
                     view.setPresetView(R.drawable.preseteq_pro);
                     break;
                 case FLAT:
                     view.setPresetView(R.drawable.preseteq_flat);
                     break;
                 case COMMON_CUSTOM:
                     bandSetting = fxSetting.customBandSettingA;
                     if(bandSetting.bands!=null&&bandSetting.bands.length==BAND_DATA_COUNT) {
                         System.arraycopy(bandSetting.bands, 0, bands, 0, bandSetting.bands.length);
                     }
                     view.setCustomView(bands);
                     mCustomType = CustomEqType.CUSTOM1;
                     break;
                 case COMMON_CUSTOM_2ND:
                     bandSetting = fxSetting.customBandSettingB;
                     if(bandSetting.bands!=null&&bandSetting.bands.length==BAND_DATA_COUNT) {
                         System.arraycopy(bandSetting.bands, 0, bands, 0, bandSetting.bands.length);
                     }
                     view.setCustomView(bands);
                     mCustomType = CustomEqType.CUSTOM2;
                     break;
                 case CLEAR:
                     view.setPresetView(R.drawable.p0600_peq_clear);
                     break;
                 case VIVID:
                     view.setPresetView(R.drawable.preseteq_vivit_jazz);
                     break;
                 case DYNAMIC:
                     view.setPresetView(R.drawable.preseteq_dynamic);
                     break;
                 case JAZZ:
                     view.setPresetView(R.drawable.preseteq_vivit_jazz);
                     break;
                 case FORRO:
                     view.setPresetView(R.drawable.preseteq_dynamic);
                     break;
                 case SPECIAL_DEBUG_1:
                     view.setPresetView(R.drawable.p0070_noimage);
                     break;
                 case SPECIAL_DEBUG_2:
                     view.setPresetView(R.drawable.p0070_noimage);
                     break;
                 default:
                     view.setPresetView(-1);
                     return;
             }

            view.setSelectedItem(mTypeArray.indexOf(settingType));
        });
    }

    /**
     * EQ選択アクション
     *
     * @param position 選択位置
     */
    public void onSelectEqTypeAction(int position) {
        mFxCase.setEqualizer(mTypeArray.get(position));
    }

    /**
     * Equalizer設定変更イベント.
     *
     * @param event EqualizerSettingChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEqualizerSettingChangeEvent(EqualizerSettingChangeEvent event) {
        updateView();
    }

    /**
     * LiveSimulation設定変更イベント.
     *
     * @param event LiveSimulationSettingChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLiveSimulationSettingChangeEvent(LiveSimulationSettingChangeEvent event) {
        updateView();
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
     * Audio設定ステータス変更通知
     *
     * @param event AudioSettingStatusChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioSettingStatus(AudioSettingStatusChangeEvent event) {
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
        AudioSettingSpec audioSpec = holder.getCarDeviceSpec().audioSettingSpec;
        AudioSettingStatus audioStatus = holder.getAudioSettingStatus();
        boolean isFxSettingEnabled = holder.getCarDeviceStatus().soundFxSettingEnabled &&
                holder.getCarDeviceSpec().soundFxSettingSupported;
        ProtocolVersion version = holder.getProtocolSpec().getConnectingProtocolVersion();
        //通信プロトコル4.1以上で通信している場合、AppMusicソースでAlexa再生モード時も31バンドEQ設定を有効にする
        boolean isEnableEQ = version.isGreaterThanOrEqual(ProtocolVersion.V4_1)||!(holder.getCarDeviceStatus().sourceType== MediaSourceType.APP_MUSIC&&holder.getAppStatus().appMusicAudioMode == AudioMode.ALEXA);

        Optional.ofNullable(getView()).ifPresent(view ->
                view.setEnable(audioSpec.equalizerSettingSupported&&audioStatus.equalizerSettingEnabled&&isFxSettingEnabled&&isEnableEQ));
    }

    /**
     * QuickSet押下アクション
     */
    public void onQuickSetAction() {
        Bundle args;
        String customStr = mContext.getString(R.string.set_048);
        switch (mCustomType) {
            case CUSTOM1:
                customStr = mContext.getString(R.string.set_048);
                break;
            case CUSTOM2:
                customStr = mContext.getString(R.string.set_051);
                break;
            default:
                break;
        }
        args = createSettingsParams(customStr);
        args = createEqSettingsParams(args, mCustomType);
        mEventBus.post(new NavigateEvent(ScreenId.EQ_QUICK_SETTING, args));
    }

    /**
     * ProSet押下アクション
     */
    public void onProSetAction() {
        Bundle args;
        String customStr = mContext.getString(R.string.set_047);
        switch (mCustomType) {
            case CUSTOM1:
                customStr = mContext.getString(R.string.set_047);
                break;
            case CUSTOM2:
                customStr = mContext.getString(R.string.set_050);
                break;
            default:
                break;
        }
        args = createSettingsParams(customStr);
        args = createEqSettingsParams(args, mCustomType);
        mEventBus.post(new NavigateEvent(ScreenId.EQ_PRO_SETTING, args));
    }

    private Bundle createSettingsParams(String pass) {
        SettingsParams params = new SettingsParams();
        params.pass = pass;
        return params.toBundle();
    }

    private Bundle createEqSettingsParams(Bundle args, CustomEqType type) {
        EqSettingParams params = new EqSettingParams();
        params.customType = type;
        return params.toBundle(args);
    }
}
