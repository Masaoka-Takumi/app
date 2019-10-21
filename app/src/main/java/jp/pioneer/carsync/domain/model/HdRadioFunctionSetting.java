package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * HD Radio Function設定.
 */
public class HdRadioFunctionSetting extends Setting {
    /** 車載機へのリクエスト状態. */
    public RequestStatus requestStatus;
    /** BSM設定ON. */
    public boolean bsmSetting;
    /** LOCAL設定ON. */
    public LocalSetting localSetting;
    /** HD SEEK設定ON. */
    public boolean hdSeekSetting;
    /** BLENDING設定ON. */
    public boolean blendingSetting;
    /** ACTIVE RADIO設定ON. */
    public boolean activeRadioSetting;

    /**
     * コンストラクタ.
     */
    public HdRadioFunctionSetting() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        requestStatus = RequestStatus.NOT_SENT;
        bsmSetting = false;
        localSetting = LocalSetting.OFF;
        hdSeekSetting = false;
        blendingSetting = false;
        activeRadioSetting = false;
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
                .add("hdSeekSetting", hdSeekSetting)
                .add("blendingSetting", blendingSetting)
                .add("activeRadioSetting", activeRadioSetting)
                .toString();
    }
}
