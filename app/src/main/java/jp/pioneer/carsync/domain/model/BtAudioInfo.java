package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.pioneer.carsync.domain.util.TextMatchingUtil;

/**
 * BT Audio情報.
 */
@SuppressFBWarnings("UWF_NULL_FIELD")
public class BtAudioInfo extends AbstractMediaInfo {
    /** Song Title. */
    public String songTitle;
    /** Artist Name. */
    public String artistName;
    /** Album Title. */
    public String albumName;
    /** Device Name. */
    public String deviceName;

    /**
     * コンストラクタ.
     */
    public BtAudioInfo() {
        reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        super.reset();
        songTitle = null;
        artistName = null;
        albumName = null;
        deviceName = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    MoreObjects.ToStringHelper addToString(MoreObjects.ToStringHelper helper) {
        return super.addToString(helper)
                .add("songTitle", songTitle)
                .add("artistName", artistName)
                .add("albumName", albumName)
                .add("deviceName", deviceName);
    }

    /**
     * 接続中（A2DP接続試行中）か否か取得.
     * <p>
     * 専用のステータス領域はなくsongTitleの文字列で判断するしなかい。
     *
     * @return {@code true}:接続中（A2DP接続試行中）である。{@code false}:それ以外。
     */
    public boolean isConnecting() {
        return TextMatchingUtil.equals(songTitle, "Connecting");
    }

    /**
     * Audioサービス（A2DP）未開始か否か取得.
     *
     * @return {@code true}:Audioサービス（A2DP）未開始である。{@code false}:それ以外。
     */
    public boolean isNoService() {
        return TextMatchingUtil.equals(songTitle, "No Service");
    }
}
