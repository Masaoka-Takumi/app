package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * Tuner(Radio) Function設定.
 */
public class TunerFunctionSetting extends Setting {
    /** 車載機へのリクエスト状態. */
    public RequestStatus requestStatus;
    /** BSM設定ON */
    public boolean bsmSetting;
    /** LOCAL設定. */
    public LocalSetting localSetting;
    /** FM Tuner設定. */
    public FMTunerSetting fmTunerSetting;
    /** REG広域設定ON. */
    public boolean regSetting;
    /** TA設定 */
    public boolean taSetting;
    /** TA設定（DABモデル） */
    public TASetting taDabSetting;
    /** AF設定ON. */
    public boolean afSetting;
    /** NEWS設定ON. */
    public boolean newsSetting;
    /** ALARM設定ON. */
    public boolean alarmSetting;
    /** PCH/MANUAL設定. */
    public PCHManualSetting pchManualSetting;

    /**
     * コンストラクタ.
     */
    public TunerFunctionSetting() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        requestStatus = RequestStatus.NOT_SENT;
        bsmSetting = false;
        localSetting = LocalSetting.OFF;
        fmTunerSetting = FMTunerSetting.STANDARD;
        regSetting = false;
        taSetting = false;
        taDabSetting = TASetting.OFF;
        afSetting = false;
        newsSetting = false;
        alarmSetting = false;
        pchManualSetting = PCHManualSetting.MANUAL;
        clear();
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("requestStatus", requestStatus)
                .add("bsmSetting", bsmSetting)
                .add("localSetting", localSetting)
                .add("fmTunerSetting", fmTunerSetting)
                .add("regSetting", regSetting)
                .add("taSetting", taSetting)
                .add("taDabSetting", taDabSetting)
                .add("afSetting", afSetting)
                .add("newsSetting", newsSetting)
                .add("alarmSetting", alarmSetting)
                .add("pchManualSetting", pchManualSetting)
                .toString();
    }
}
