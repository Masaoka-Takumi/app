package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import jp.pioneer.carsync.domain.util.TextMatchingUtil;

/**
 * HD Radio情報.
 */
public class HdRadioInfo extends AbstractTunerInfo {
    /** BAND種別. */
    public HdRadioBandType band;
    /** マルチキャストCH番号. */
    public int multicastChannelNumber;
    /** 放送局情報 / LINKING. */
    public String stationInfo;
    /** Song Title. */
    public String songTitle;
    /** Artist Name. */
    public String artistName;
    /** MULTICAST PROGRAM NUMBER. */
    public String multicastProgramNumber;
    /** HDデジタル音声受信状態. */
    public HdRadioDigitalAudioStatus hdRadioDigitalAudioStatus;
    /** HD放送局情報受信状態. */
    public HdRadioStationStatus hdRadioStationStatus;

    /**
     * コンストラクタ.
     */
    public HdRadioInfo() {
        reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        super.reset();
        band = null;
        multicastChannelNumber = 0;
        stationInfo = null;
        songTitle = null;
        artistName = null;
        multicastProgramNumber = null;
        hdRadioDigitalAudioStatus = null;
        hdRadioStationStatus = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HdRadioBandType getBand() {
        return band;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSearchStatus() {
        return tunerStatus == TunerStatus.SEEK || tunerStatus == TunerStatus.BSM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    MoreObjects.ToStringHelper addToString(MoreObjects.ToStringHelper helper) {
        return super.addToString(helper)
                .add("band", band)
                .add("multicastChannelNumber", multicastChannelNumber)
                .add("stationInfo", stationInfo)
                .add("songTitle", songTitle)
                .add("artistName", artistName)
                .add("multicastProgramNumber", multicastProgramNumber)
                .add("hdRadioDigitalAudioStatus", hdRadioDigitalAudioStatus)
                .add("hdRadioStationStatus", hdRadioStationStatus);
    }

    /**
     * LINKING状態か否か取得.
     * <p>
     * SPSの音声が取得出来ない状態となった場合、一定時間経過後MPSに遷移する。
     * その一定期間の間「LINKING状態」となる。
     * 専用のステータス領域はなくstationInfoの文字列で判断するしなかい。
     *
     * @return {@code true}:LINKING状態である。{@code false}:それ以外。
     */
    public boolean isLinking() {
        return TextMatchingUtil.equals(stationInfo, "Linking");
    }

    /**
     * エラー状態か否か取得.
     *
     * @return {@code true}:エラー状態である。{@code false}:それ以外。
     */
    public boolean isErrorStatus() {
        return tunerStatus == TunerStatus.ERROR;
    }
}
