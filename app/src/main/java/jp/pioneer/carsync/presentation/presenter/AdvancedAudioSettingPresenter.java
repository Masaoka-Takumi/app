package jp.pioneer.carsync.presentation.presenter;

import android.os.Handler;
import android.os.Looper;

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
import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.CrossoverSetting;
import jp.pioneer.carsync.domain.model.CutoffSetting;
import jp.pioneer.carsync.domain.model.ListeningPositionSetting;
import jp.pioneer.carsync.domain.model.MixedSpeakerType;
import jp.pioneer.carsync.domain.model.SlopeSetting;
import jp.pioneer.carsync.domain.model.SpeakerLevelSetting;
import jp.pioneer.carsync.domain.model.SpeakerType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TimeAlignmentSetting;
import jp.pioneer.carsync.presentation.view.AdvancedAudioSettingView;

/**
 * オーディオ詳細設定画面のpresenter
 */
@PresenterLifeCycle
public class AdvancedAudioSettingPresenter extends Presenter<AdvancedAudioSettingView> {
    @Inject EventBus mEventBus;
    @Inject PreferAudio mPreferAudio;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject AppSharedPreference mAppSharedPreference;
    private Handler mHandler = new Handler(Looper.getMainLooper());


    @Inject
    public AdvancedAudioSettingPresenter() {
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
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
     * @return UiColor
     */
    public int getUiColor() {
        return mAppSharedPreference.getUiColor().getResource();
    }

    /**
     * SubWooferトグル切り替え
     */
    public void onToggleSubWoofer() {
        mPreferAudio.toggleSubWoofer();
    }

    /**
     * SubWooferPhaseトグル切り替え
     */
    public void onToggleSubWooferPhase() {
        mPreferAudio.toggleSubWooferPhase();
    }

    /**
     * CrossoverHpfLpfトグル切り替え
     *
     * @param type
     */
    public void onToggleCrossoverHpfLpf(SpeakerType type) {
        mPreferAudio.toggleCrossoverHpfLpf(type);
    }

    /**
     * CrossoverCutOff設定
     *
     * @param type
     * @param inc
     */
    public void setCrossoverCutOff(SpeakerType type, boolean inc) {
        StatusHolder statusHolder = getStatusHolder();
        AudioSetting audioSetting = statusHolder.getAudioSetting();
        CrossoverSetting setting = audioSetting.crossoverSetting;
        CutoffSetting cutoff;
        switch (type) {
            case FRONT:
                cutoff = setting.front.cutoffSetting;
                break;
            case REAR:
                cutoff = setting.rear.cutoffSetting;
                break;
            case SUBWOOFER_STANDARD_MODE:
                cutoff = setting.subwooferStandardMode.cutoffSetting;
                break;
            case HIGH:
                cutoff = setting.high.cutoffSetting;
                break;
            case MID_HPF:
                cutoff = setting.midHPF.cutoffSetting;
                break;
            case MID_LPF:
                cutoff = setting.midLPF.cutoffSetting;
                break;
            case SUBWOOFER_2WAY_NETWORK_MODE:
                cutoff = setting.subwoofer2WayNetworkMode.cutoffSetting;
                break;
            default:
                throw new AssertionError("can't happen.");
        }
        if (inc) {
            cutoff = cutoff.toggle(1);
        } else {
            cutoff = cutoff.toggle(-1);
        }
        if (cutoff != null) {
            mPreferAudio.setCrossoverCutOff(type, cutoff);
        }
    }

    /**
     * CrossoverSlope設定
     *
     * @param type
     * @param inc
     */
    public void setCrossoverSlope(SpeakerType type, boolean inc) {
        StatusHolder statusHolder = getStatusHolder();
        AudioSetting audioSetting = statusHolder.getAudioSetting();
        CrossoverSetting setting = audioSetting.crossoverSetting;
        SlopeSetting slope;
        switch (type) {
            case FRONT:
                slope = setting.front.slopeSetting;
                break;
            case REAR:
                slope = setting.rear.slopeSetting;
                break;
            case SUBWOOFER_STANDARD_MODE:
                slope = setting.subwooferStandardMode.slopeSetting;
                break;
            case HIGH:
                slope = setting.high.slopeSetting;
                break;
            case MID_HPF:
                slope = setting.midHPF.slopeSetting;
                break;
            case MID_LPF:
                slope = setting.midLPF.slopeSetting;
                break;
            case SUBWOOFER_2WAY_NETWORK_MODE:
                slope = setting.subwoofer2WayNetworkMode.slopeSetting;
                break;
            default:
                throw new AssertionError("can't happen.");
        }
        if (inc) {
            slope = slope.toggle(1);
        } else {
            slope = slope.toggle(-1);
        }
        if (slope != null) {
            mPreferAudio.setCrossoverSlope(type, slope);
        }
    }

    /**
     * SpeakerLevel設定
     *
     * @param type
     * @param inc
     */
    public void setSpeakerLevel(MixedSpeakerType type, boolean inc) {
        StatusHolder statusHolder = getStatusHolder();
        AudioSetting audioSetting = statusHolder.getAudioSetting();
        SpeakerLevelSetting speakerLevelSetting = audioSetting.speakerLevelSetting;
        int currentSpeakerLevel = 0;
        switch (type) {
            case FRONT_LEFT_HIGH_LEFT:
                currentSpeakerLevel = speakerLevelSetting.frontLeftHighLeftLevel;
                break;
            case FRONT_RIGHT_HIGH_RIGHT:
                currentSpeakerLevel = speakerLevelSetting.frontRightHighRightLevel;
                break;
            case REAR_LEFT_MID_LEFT:
                currentSpeakerLevel = speakerLevelSetting.rearLeftMidLeftLevel;
                break;
            case REAR_RIGHT_MID_RIGHT:
                currentSpeakerLevel = speakerLevelSetting.rearRightMidRightLevel;
                break;
            case SUBWOOFER:
                currentSpeakerLevel = speakerLevelSetting.subwooferLevel;
                break;
            default:
                break;
        }
        int level;
        if (inc) {
            level = currentSpeakerLevel + 1;
        } else {
            level = currentSpeakerLevel - 1;
        }
        if (level >= speakerLevelSetting.minimumLevel && level <= speakerLevelSetting.maximumLevel) {
            mPreferAudio.setSpeakerLevel(type, level);
        }
    }

    /**
     * TimeAlignment設定
     *
     * @param type
     * @param inc
     */
    public void setTimeAlignment(MixedSpeakerType type, boolean inc) {
        StatusHolder statusHolder = getStatusHolder();
        AudioSetting audioSetting = statusHolder.getAudioSetting();
        TimeAlignmentSetting setting = audioSetting.timeAlignmentSetting;
        int step = 0;
        switch (type) {
            case FRONT_LEFT_HIGH_LEFT:
                step = setting.frontLeftHighLeftStep;
                break;
            case FRONT_RIGHT_HIGH_RIGHT:
                step = setting.frontRightHighRightStep;
                break;
            case REAR_LEFT_MID_LEFT:
                step = setting.rearLeftMidLeftStep;
                break;
            case REAR_RIGHT_MID_RIGHT:
                step = setting.rearRightMidRightStep;
                break;
            case SUBWOOFER:
                step = setting.subwooferStep;
                break;
            default:
                break;
        }
        if (inc) {
            step = step + 1;
        } else {
            step = step - 1;
        }
        if (step >= setting.minimumStep && step <= setting.maximumStep) {
            mPreferAudio.setTimeAlignment(type, step);
        }
    }

    /**
     * TimeAlignmentModeトグル切り替え
     */
    public void onToggleTimeAlignmentMode() {
        mPreferAudio.toggleTimeAlignmentMode();
    }

    /**
     * ListeningPosition設定
     *
     * @param item
     */
    public void setListeningPosition(ListeningPositionSetting item) {
        mPreferAudio.setListeningPosition(item);
    }

    /**
     * Audio設定変更イベント通知
     *
     * @param event Audio設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioSettingChangeAction(AudioSettingChangeEvent event) {
        Optional.ofNullable(getView()).ifPresent(view -> {
                view.applyStatus();
                view.redrawFilterGraph(true);
        });
    }

    /**
     * 車載機ステータス変更イベントハンドラ
     *
     * @param event 車載機ステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarDeviceStatusChangeEvent(CarDeviceStatusChangeEvent event) {
        Optional.ofNullable(getView()).ifPresent(AdvancedAudioSettingView::applyStatus);
    }

    /**
     * Audio設定ステータス変更通知
     *
     * @param event AudioSettingStatusChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudioSettingStatus(AudioSettingStatusChangeEvent event) {
        Optional.ofNullable(getView()).ifPresent(AdvancedAudioSettingView::applyStatus);
    }
}
