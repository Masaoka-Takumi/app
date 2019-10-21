package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * HD Radio Function設定スペック.
 */
public class HdRadioFunctionSettingSpec {
    /** ACTIVE RADIO設定対応. */
    public boolean activeRadioSettingSupported;
    /** BLENDING設定対応. */
    public boolean blendingSettingSupported;
    /** HD SEEK設定対応. */
    public boolean hdSeekSettingSupported;
    /** LOCAL設定対応.*/
    public boolean localSettingSupported;
    /** BSM設定対応. */
    public boolean bsmSettingSupported;

    /**
     * コンストラクタ.
     */
    public HdRadioFunctionSettingSpec() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        activeRadioSettingSupported = false;
        blendingSettingSupported = false;
        hdSeekSettingSupported = false;
        localSettingSupported = false;
        bsmSettingSupported = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("activeRadioSettingSupported", activeRadioSettingSupported)
                .add("blendingSettingSupported", blendingSettingSupported)
                .add("hdSeekSettingSupported", hdSeekSettingSupported)
                .add("localSettingSupported", localSettingSupported)
                .add("bsmSettingSupported", bsmSettingSupported)
                .toString();
    }
}
