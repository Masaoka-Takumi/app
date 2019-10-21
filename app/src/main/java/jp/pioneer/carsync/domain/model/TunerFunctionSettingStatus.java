package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * Tuner(Radio) Function設定ステータス.
 *
 * @see TunerFunctionSettingSpec
 */
public class TunerFunctionSettingStatus extends SerialVersion {
    /** ALARM設定有効. */
    public boolean alarmSettingEnabled;
    /** NEWS設定有効. */
    public boolean newsSettingEnabled;
    /** AF設定有効. */
    public boolean afSettingEnabled;
    /** TA設定有効. */
    public boolean taSettingEnabled;
    /** REG広域設定有効. */
    public boolean regSettingEnabled;
    /** FM Tuner Setting設定有効. */
    public boolean fmSettingEnabled;
    /** LOCAL設定有効. */
    public boolean localSettingEnabled;
    /** BSM設定有効. */
    public boolean bsmSettingEnabled;
    /** PTY SEARCH設定有効. */
    public boolean ptySearchSettingEnabled;
    /** P.CH / MANUAL設定有効. */
    public boolean pchManualEnabled;

    /**
     * コンストラクタ.
     */
    public TunerFunctionSettingStatus() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        alarmSettingEnabled = false;
        newsSettingEnabled = false;
        afSettingEnabled = false;
        taSettingEnabled = false;
        regSettingEnabled = false;
        fmSettingEnabled = false;
        localSettingEnabled = false;
        bsmSettingEnabled = false;
        ptySearchSettingEnabled = false;
        pchManualEnabled = false;
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("alarmSettingEnabled", alarmSettingEnabled)
                .add("newsSettingEnabled", newsSettingEnabled)
                .add("afSettingEnabled", afSettingEnabled)
                .add("taSettingEnabled", taSettingEnabled)
                .add("regSettingEnabled", regSettingEnabled)
                .add("fmSettingEnabled", fmSettingEnabled)
                .add("localSettingEnabled", localSettingEnabled)
                .add("bsmSettingEnabled", bsmSettingEnabled)
                .add("ptySearchSettingEnabled", ptySearchSettingEnabled)
                .add("pchManualEnabled", pchManualEnabled)
                .toString();
    }
}
