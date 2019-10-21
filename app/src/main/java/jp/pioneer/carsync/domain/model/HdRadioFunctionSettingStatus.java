package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * HD Radio Function設定ステータス.
 *
 * @see HdRadioFunctionSettingSpec
 */
public class HdRadioFunctionSettingStatus extends SerialVersion {
    /** ACTIVE RADIO設定有効. */
    public boolean activeRadioSettingEnabled;
    /** BLENDING設定有効. */
    public boolean blendingSettingEnabled;
    /** HD SEEK設定有効. */
    public boolean hdSeekSettingEnabled;
    /** LOCAL設定有効. */
    public boolean localSettingEnabled;
    /** BSM設定有効. */
    public boolean bsmSettingEnabled;

    /**
     * コンストラクタ.
     */
    public HdRadioFunctionSettingStatus() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        activeRadioSettingEnabled = false;
        blendingSettingEnabled = false;
        hdSeekSettingEnabled = false;
        localSettingEnabled = false;
        bsmSettingEnabled = false;
        updateVersion();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("activeRadioSettingEnabled", activeRadioSettingEnabled)
                .add("blendingSettingEnabled", blendingSettingEnabled)
                .add("hdSeekSettingEnabled", hdSeekSettingEnabled)
                .add("localSettingEnabled", localSettingEnabled)
                .add("bsmSettingEnabled", bsmSettingEnabled)
                .toString();
    }
}
