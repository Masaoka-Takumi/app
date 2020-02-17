package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import jp.pioneer.carsync.domain.util.TextMatchingUtil;

/**
 * DAB情報.
 */
public class DabInfo extends AbstractTunerInfo {
    /** BAND種別. */
    public DabBandType band;
    /** EID. */
    public int eid;
    /** SID. */
    public long sid;
    /** SCIdS. */
    public int scids;
    /** Service Component Label / No Service / No Signal状態. */
    public String serviceComponentLabel;
    /** Dynamic Label. */
    public String dynamicLabel;
    /** PTY情報. */
    public String ptyInfo;
    /** SERVICE NUMBER. */
    public String serviceNumber;
    /** TimeShift Mode遷移可能. */
    public boolean timeShiftModeAvailable;
    /** TimeShift Mode中. */
    public boolean timeShiftMode;
    /** 再生状態 */
    public PlaybackMode playbackMode;
    /** 総バッファ時間（秒）. */
    public int totalBufferTime;
    /** 現在の再生時間 （秒）. */
    public int currentPosition;
    /** 現在のバッファ時間（秒）. */
    public int currentBufferTime;

    /**
     * コンストラクタ.
     */
    public DabInfo() {
        reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        super.reset();
        band = null;
        eid = 0;
        sid = 0;
        scids = 0;
        serviceComponentLabel = null;
        dynamicLabel = null;
        ptyInfo = null;
        serviceNumber = null;
        timeShiftModeAvailable = false;
        timeShiftMode = false;
        playbackMode = PlaybackMode.PAUSE;
        totalBufferTime = 0;
        currentPosition = 0;
        currentBufferTime = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DabBandType getBand() {
        return band;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSearchStatus() {
        return tunerStatus == TunerStatus.LIST_UPDATE
                || tunerStatus == TunerStatus.SEEK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    MoreObjects.ToStringHelper addToString(MoreObjects.ToStringHelper helper) {
        return super.addToString(helper)
                .add("band", band)
                .add("eid", eid)
                .add("sid", sid)
                .add("scids", scids)
                .add("serviceComponentLabel", serviceComponentLabel)
                .add("dynamicLabel", dynamicLabel)
                .add("ptyInfo", ptyInfo)
                .add("serviceNumber", serviceNumber)
                .add("timeShiftModeAvailable", timeShiftModeAvailable)
                .add("timeShiftMode", timeShiftMode)
                .add("totalBufferTime", totalBufferTime)
                .add("currentPosition", currentPosition)
                .add("currentBufferTime", currentBufferTime);
    }

    /**
     * 当ensemble信号がないか否か取得.
     * <p>
     * 専用のステータス領域はなくserviceComponentLabelの文字列で判断するしなかい。
     *
     * @return {@code true}:ensemble信号がない。{@code false}:それ以外。
     */
    public boolean isNoSignal() {
        return TextMatchingUtil.equals(serviceComponentLabel, "No Signal");
    }

    /**
     * 当service放送がない（当ensemble信号はある）か否か取得.
     * <p>
     * 専用のステータス領域はなくserviceComponentLabelの文字列で判断するしなかい。
     *
     * @return {@code true}:当service放送がない。{@code false}:それ以外。
     */
    public boolean isNoService() {
        return TextMatchingUtil.equals(serviceComponentLabel, "No Station");
    }

    /**
     * TimeShift Mode遷移不可状態でPlay中か否か取得.
     *
     * @return {@code true}:TimeShift Mode遷移不可状態でPlay中である。{@code false}:それ以外。
     */
    public boolean isPlayStatus() {
        return !timeShiftModeAvailable
                && playbackMode == PlaybackMode.PLAY;
    }

    /**
     * TimeShift Mode遷移不可状態でPause中か否か取得.
     *
     * @return {@code true}:TimeShift Mode遷移不可状態でPause中である。{@code false}:それ以外。
     */
    public boolean isPauseStatus() {
        return !timeShiftModeAvailable
                && playbackMode == PlaybackMode.PAUSE;
    }

    /**
     * エラー状態か否か取得.
     *
     * @return {@code true}:エラー状態である。{@code false}:それ以外。
     */
    public boolean isErrorStatus() {
        return tunerStatus == TunerStatus.ERROR || isNoService() || isNoSignal();
    }

    /**
     * お気に入りに利用可能（登録可能）か否か取得.
     *
     * @return {@code true}:お気に入りに利用可能（登録可能）である。{@code false}:それ以外
     */
    public boolean isFavoriteAvailable() {
        return !(timeShiftMode
                || tunerStatus == TunerStatus.FM_LINK
                || isErrorStatus()
                || isSearchStatus());
    }
}
