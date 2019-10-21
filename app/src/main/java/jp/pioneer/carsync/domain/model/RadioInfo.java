package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import jp.pioneer.carsync.domain.util.TextMatchingUtil;

/**
 * ラジオ情報.
 */
public class RadioInfo extends AbstractTunerInfo {
    /** BAND種別. */
    public RadioBandType band;
    /** Artist Name. */
    public String artistName;
    /** PS情報. */
    public String psInfo;
    /**
     * PTY情報.
     * <p>
     * LOWモデル:PTY情報 / Song Title
     * HIGHモデル:PTY情報
     */
    public String ptyInfo;
    /** PI. */
    public int pi;
    /**
     * Song Title受信中.
     * <p>
     * HIGHモデルのみ.
     */
    public String songTitle;
    /** RDS割り込み種別. */
    public RdsInterruptionType rdsInterruptionType;

    /**
     * コンストラクタ.
     */
    public RadioInfo() {
        reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        super.reset();
        band = null;
        artistName = null;
        psInfo = null;
        ptyInfo = null;
        pi = 0;
        songTitle = null;
        rdsInterruptionType = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RadioBandType getBand() {
        return band;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    MoreObjects.ToStringHelper addToString(MoreObjects.ToStringHelper helper) {
        return super.addToString(helper)
                .add("band", band)
                .add("artistName", artistName)
                .add("psInfo", psInfo)
                .add("ptyInfo", ptyInfo)
                .add("pi", pi)
                .add("songTitle", songTitle)
                .add("rdsInterruptionType", rdsInterruptionType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSearchStatus() {
        return tunerStatus == TunerStatus.SEEK
                || tunerStatus == TunerStatus.PI_SEARCH
                || tunerStatus == TunerStatus.BSM
                || tunerStatus == TunerStatus.PTY_SEARCH;
    }

    /**
     * PTY取得不可か否か取得.
     * <p>
     * 専用のステータス領域はなくptyInfoの文字列で判断するしなかい。
     *
     * @return {@code true}:取得不可。{@code false}:それ以外。
     */
    public boolean isNoPty() {
        return TextMatchingUtil.equals(ptyInfo, "NO PTY");
    }

    /**
     * タイトル無しか否か取得.
     * <p>
     * 専用のステータス領域はなくptyInfoの文字列で判断するしかない。
     *
     * @return {@code true}:タイトル無し。{@code false}:それ以外。
     */
    public boolean isNoTitle() {
        return TextMatchingUtil.equals(songTitle, "No Title");
    }
}
