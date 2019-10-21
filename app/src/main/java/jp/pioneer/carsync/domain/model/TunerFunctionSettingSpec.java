package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * Tuner(Radio)Function設定スペック.
 */
public class TunerFunctionSettingSpec {
    /** ALARM設定対応.*/
    public boolean alarmSettingSupported;
    /** NEWS設定対応. */
    public boolean newsSettingSupported;
    /** AF設定対応. */
    public boolean afSettingSupported;
    /** TA設定対応. */
    public boolean taSettingSupported;
    /** LOCAL設定対応. */
    public boolean localSettingSupported;
    /** REG広域設定対応. */
    public boolean regSettingSupported;
    /** BSM設定対応. */
    public boolean bsmSettingSupported;
    /** FM Tuner Setting設定対応. */
    public boolean fmSettingSupported;
    /** PTY SEARCH設定対応. */
    public boolean ptySearchSettingSupported;
    /** P.CH / MANUAL設定対応. */
    public boolean pchManualSupported;

    /**
     * コンストラクタ.
     */
    public TunerFunctionSettingSpec() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        alarmSettingSupported = false;
        newsSettingSupported = false;
        afSettingSupported = false;
        taSettingSupported = false;
        localSettingSupported = false;
        regSettingSupported = false;
        bsmSettingSupported = false;
        fmSettingSupported = false;
        ptySearchSettingSupported = false;
        pchManualSupported = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("alarmSettingSupported", alarmSettingSupported)
                .add("newsSettingSupported", newsSettingSupported)
                .add("afSettingSupported", afSettingSupported)
                .add("taSettingSupported", taSettingSupported)
                .add("localSettingSupported", localSettingSupported)
                .add("regSettingSupported", regSettingSupported)
                .add("bsmSettingSupported", bsmSettingSupported)
                .add("fmSettingSupported", fmSettingSupported)
                .add("ptySearchSettingSupported", ptySearchSettingSupported)
                .add("pchManualSupported", pchManualSupported)
                .toString();
    }
}
