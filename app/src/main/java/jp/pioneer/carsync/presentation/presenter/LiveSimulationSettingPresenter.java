package jp.pioneer.carsync.presentation.presenter;

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
import jp.pioneer.carsync.domain.event.SoundFxSettingChangeEvent;
import jp.pioneer.carsync.domain.event.SoundFxSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.LiveSimulationSetting;
import jp.pioneer.carsync.domain.model.SoundEffectType;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.model.LiveSimulationItem;
import jp.pioneer.carsync.presentation.model.VisualEffectItem;
import jp.pioneer.carsync.presentation.view.LiveSimulationSettingView;

/**
 * LiveSimulation画面のPresenter
 */
@PresenterLifeCycle
public class LiveSimulationSettingPresenter extends Presenter<LiveSimulationSettingView> {

    static final ArrayList<LiveSimulationItem> LIVE_SIMULATION_ITEMS = new ArrayList<LiveSimulationItem>() {{
        add(new LiveSimulationItem(SoundFieldControlSettingType.OFF, R.drawable.livesimulation_off,0));
        add(new LiveSimulationItem(SoundFieldControlSettingType.CAFE, R.drawable.livesimulation_cafe,1));
        add(new LiveSimulationItem(SoundFieldControlSettingType.CLUB, R.drawable.livesimulation_club,2));
        add(new LiveSimulationItem(SoundFieldControlSettingType.CONCERT_HALL, R.drawable.livesimulation_concerthall,3));
        add(new LiveSimulationItem(SoundFieldControlSettingType.OPEN_AIR, R.drawable.livesimulation_openair,4));
    }};
    static final List<VisualEffectItem> EFFECT_SETTING_TYPES = new ArrayList<VisualEffectItem>() {{
        add(new VisualEffectItem(SoundEffectType.OFF, R.drawable.p0143_veiconbtn_1nrm));
        add(new VisualEffectItem(SoundEffectType.MALE, R.drawable.p0145_veiconbtn_1nrm));
        add(new VisualEffectItem(SoundEffectType.FEMALE, R.drawable.p0146_veiconbtn_1nrm));
    }};
    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject PreferSoundFx mFxCase;

    /**
     * コンストラクタ
     */
    @Inject
    public LiveSimulationSettingPresenter() {
    }

    @Override
    void onTakeView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            LiveSimulationSetting setting = getFxSetting().liveSimulationSetting;
            // Live Simulation
            view.setLiveSimulationAdapter(LIVE_SIMULATION_ITEMS);
            view.setVisualEffectAdapter(EFFECT_SETTING_TYPES);
            // ui color
            view.setColor(mPreference.getUiColor().getResource());
        });
    }

    @Override
    void onInitialize() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            LiveSimulationSetting setting = getFxSetting().liveSimulationSetting;
            // 画面遷移時、選択中のLiveSimulationをViewPagerの中心とする
            int i;
            for (i = 0; i < LIVE_SIMULATION_ITEMS.size(); i++) {
                LiveSimulationItem item = LIVE_SIMULATION_ITEMS.get(i);
                if (item.type == setting.soundFieldControlSettingType) {
                    break;
                }
            }
            view.setNextItem(i);
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
     *
     * @return SoundFxSetting
     */
    private SoundFxSetting getFxSetting() {
        StatusHolder holder = mGetStatusHolder.execute();
        return holder.getSoundFxSetting();
    }

    /**
     * SoundFieldSettingType設定ハンドラ
     *
     * @param position リスト選択位置
     */
    public void onSelectLiveSimulationAction(int position) {
        LiveSimulationSetting setting = getFxSetting().liveSimulationSetting;
        SoundFieldControlSettingType fieldType = LIVE_SIMULATION_ITEMS.get(position).type;
        SoundEffectType effectType = setting.soundEffectSettingType.type;
        mFxCase.setLiveSimulation(fieldType, effectType);
    }

    /**
     * ApplauseEffectSettingType設定ハンドラ
     *
     * @param position リスト選択位置
     */
    public void onSelectVisualEffectAction(int position) {
        LiveSimulationSetting setting = getFxSetting().liveSimulationSetting;
        SoundEffectType effectType = EFFECT_SETTING_TYPES.get(position).type;
        SoundFieldControlSettingType fieldType = setting.soundFieldControlSettingType;
        mFxCase.setLiveSimulation(fieldType, effectType);
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

    private void setEnable(){
        StatusHolder holder = mGetStatusHolder.execute();
        SoundFxSettingSpec fxSpec = holder.getCarDeviceSpec().soundFxSettingSpec;
        SoundFxSettingStatus fxStatus = holder.getSoundFxSettingStatus();

        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setEnable(fxSpec.liveSimulationSettingSupported&&fxStatus.liveSimulationSettingEnabled);
        });
    }

    private void updateView(){
        Optional.ofNullable(getView()).ifPresent(view -> {
            LiveSimulationSetting setting = getFxSetting().liveSimulationSetting;
            // Live Simulation
            int i;
            for (i = 0; i < LIVE_SIMULATION_ITEMS.size(); i++) {
                LiveSimulationItem item = LIVE_SIMULATION_ITEMS.get(i);
                if (item.type == setting.soundFieldControlSettingType) {
                    break;
                }
            }
            view.setCurrentPosition(i);
            // Visual Effect
            int j;
            for (j = 0; j < EFFECT_SETTING_TYPES.size(); j++) {
                if (EFFECT_SETTING_TYPES.get(j).type == setting.soundEffectSettingType.type) {
                    break;
                }
            }
            view.setVisualEffectSelectedIndex(j);

            if(setting.soundFieldControlSettingType==SoundFieldControlSettingType.OFF){
                view.setVisualEffectEnabled(false);
            }else{
                view.setVisualEffectEnabled(true);
            }
        });
    }
}
