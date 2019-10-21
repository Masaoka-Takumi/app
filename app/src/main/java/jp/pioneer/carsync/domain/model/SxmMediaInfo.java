package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import jp.pioneer.carsync.domain.util.TextMatchingUtil;

/**
 * SiriusXM情報.
 */
public class SxmMediaInfo extends AbstractTunerInfo {
    /** BAND種別. */
    public SxmBandType band;
    /** Replay Mode遷移可能. */
    public boolean replayModeAvailable;
    /** Replay Mode中. */
    public boolean inReplayMode;
    /** Tune Mix遷移可能. */
    public boolean tuneMixAvailable;
    /** Tune Mix中. */
    public boolean inTuneMix;
    /** Subscription update表示中. */
    public boolean subscriptionUpdatingShowing;
    /** 再生状態. */
    public PlaybackMode playbackMode;
    /** チャンネル/チャンネルネーム or Advisory Message. */
    public String channelAndChannelNameOrAdvisoryMessage;
    /** Artist Name or Content Info. */
    public String artistNameOrContentInfo;
    /** Song Title. */
    public String songTitle;
    /** Category Name. */
    public String categoryName;
    /** 有効最小CHANNEL NUMBER. */
    public int minimumChannelNumber;
    /** 有効最大CHANNEL NUMBER. */
    public int maximumChannelNumber;
    /** 現在のCHANNEL NUMBER. */
    public int currentChannelNumber;
    /** sid. */
    public int sid;
    /** 総バッファ時間（秒）. */
    public int totalBufferTime;
    /** 現在の再生時間（秒）. */
    public int currentPosition;
    /** 現在のバッファ時間（秒）. */
    public int currentBufferTime;

    /**
     * コンストラクタ.
     */
    public SxmMediaInfo() {
        reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        super.reset();
        band = null;
        replayModeAvailable = false;
        inReplayMode = false;
        tuneMixAvailable = false;
        inTuneMix = false;
        subscriptionUpdatingShowing = false;
        playbackMode = PlaybackMode.PAUSE;
        channelAndChannelNameOrAdvisoryMessage = null;
        artistNameOrContentInfo = null;
        songTitle = null;
        categoryName = null;
        minimumChannelNumber = 0;
        maximumChannelNumber = 0;
        currentChannelNumber = 0;
        sid = 0;
        totalBufferTime = 0;
        currentPosition = 0;
        currentBufferTime = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SxmBandType getBand() {
        return band;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSearchStatus() {
        return tunerStatus == TunerStatus.SCAN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    MoreObjects.ToStringHelper addToString(MoreObjects.ToStringHelper helper) {
        return super.addToString(helper)
                .add("band", band)
                .add("replayModeAvailable", replayModeAvailable)
                .add("inReplayMode", inReplayMode)
                .add("tuneMixAvailable", tuneMixAvailable)
                .add("inTuneMix", inTuneMix)
                .add("subscriptionUpdatingShowing", subscriptionUpdatingShowing)
                .add("playbackMode", playbackMode)
                .add("channelAndChannelNameOrAdvisoryMessage", channelAndChannelNameOrAdvisoryMessage)
                .add("artistNameOrContentInfo", artistNameOrContentInfo)
                .add("songTitle", songTitle)
                .add("categoryName", categoryName)
                .add("minimumChannelNumber", minimumChannelNumber)
                .add("maximumChannelNumber", maximumChannelNumber)
                .add("currentChannelNumber", currentChannelNumber)
                .add("sid", sid)
                .add("totalBufferTime", totalBufferTime)
                .add("currentPosition", currentPosition)
                .add("currentBufferTime", currentBufferTime);
    }

    /**
     * アンテナ接続異常か否か取得.
     * <p>
     * 専用のステータス領域はなくchannelAndChannelNameOrAdvisoryMessageの文字列で判断するしなかい。
     *
     * @return {@code true}:アンテナ接続異常である。{@code false}:それ以外。
     */
    public boolean isCheckAntenna() {
        return TextMatchingUtil.equals(channelAndChannelNameOrAdvisoryMessage, "Check Antenna");
    }

    /**
     * Tuner接続異常か否か取得.
     * <p>
     * 専用のステータス領域はなくchannelAndChannelNameOrAdvisoryMessageの文字列で判断するしなかい。
     *
     * @return {@code true}:Tuner接続異常である。{@code false}:それ以外。
     */
    public boolean isCheckTuner() {
        return TextMatchingUtil.equals(channelAndChannelNameOrAdvisoryMessage, "Check Tuner");
    }

    /**
     * 信号なしか否か取得.
     * <p>
     * 専用のステータス領域はなくchannelAndChannelNameOrAdvisoryMessageの文字列で判断するしなかい。
     *
     * @return {@code true}:信号なしである。{@code false}:それ以外。
     */
    public boolean isNoSignal() {
        return TextMatchingUtil.equals(channelAndChannelNameOrAdvisoryMessage, "No Signal");
    }

    /**
     * CH無効か否か取得.
     * <p>
     * 専用のステータス領域はなくchannelAndChannelNameOrAdvisoryMessageの文字列で判断するしなかい。
     *
     * @return {@code true}:CH無効である。{@code false}:それ以外。
     */
    public boolean isChannelNotAvailable() {
        return TextMatchingUtil.equals(channelAndChannelNameOrAdvisoryMessage, "Channel Not Available");
    }

    /**
     * CH購読が無いか否か取得.
     * <p>
     * 専用のステータス領域はなくchannelAndChannelNameOrAdvisoryMessageの文字列で判断するしなかい。
     *
     * @return {@code true}:CH購読が無い。{@code false}:それ以外。
     */
    public boolean isChannelNotSubscribed() {
        return TextMatchingUtil.equals(channelAndChannelNameOrAdvisoryMessage, "Channel Not Subscribed");
    }

    /**
     * ロックされたCHか否か取得.
     * <p>
     * ペアレンタルコントロールによるロック。
     * 専用のステータス領域はなくchannelAndChannelNameOrAdvisoryMessageの文字列で判断するしなかい。
     *
     * @return {@code true}:ロックされたCHである。{@code false}:それ以外。
     */
    public boolean isLocked() {
        return TextMatchingUtil.equals(channelAndChannelNameOrAdvisoryMessage, "Locked");
    }

    /**
     * TuneMixChでないか否か取得.
     * <p>
     * カレントBandにMusicChが2つ以上ないとTuneMixが動作しない。
     *
     * @return {@code true}:TuneMixChでない。{@code false}:それ以外。
     */
    public boolean isNoTuneMixCh() {
        return TextMatchingUtil.equals(channelAndChannelNameOrAdvisoryMessage, "No TuneMix CH");
    }

    /**
     * エラー状態か否か取得.
     *
     * @return {@code true}:エラー状態である。{@code false}:それ以外。
     */
    public boolean isErrorStatus() {
        return isNoSignal()
                || isLocked()
                || isCheckAntenna()
                || isCheckTuner()
                || isNoTuneMixCh()
                || isChannelNotSubscribed()
                || isChannelNotAvailable();
    }

    /**
     * CH000か否か取得.
     * <p>
     * CH000はSiriusXM TunerのRadio ID numberを表示する特殊なCHである。
     *
     * @return {@code true}:CH000である。{@code false}:それ以外。
     */
    public boolean isCh000() {
        return currentChannelNumber == 0;
    }

    /**
     * お気に入りに利用可能（登録可能）か否か取得.
     *
     * @return {@code true}:お気に入りに利用可能（登録可能）である。{@code false}:それ以外
     */
    public boolean isFavoriteAvailable() {
        return !(tunerStatus == TunerStatus.SCAN
                || isCheckTuner()
                || isCheckAntenna()
                || isNoSignal()
                || isChannelNotAvailable()
                || isChannelNotSubscribed()
                || isLocked()
                || isCh000()
                || inReplayMode);
    }
}
