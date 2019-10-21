package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Pandora情報.
 */
@SuppressFBWarnings("UWF_NULL_FIELD")
public class PandoraMediaInfo extends AbstractMediaInfo {
    /** Station Name. */
    public String stationName;
    /** Song Title / PANDORA ERROR. */
    public String songTitle;
    /** Artist Name. */
    public String artistName;
    /** Album Title. */
    public String albumName;
    /** Rate設定状態（Thumb Up/Downの状態）. */
    public ThumbStatus thumbStatus;

    /**
     * コンストラクタ.
     */
    public PandoraMediaInfo() {
        reset();
    }

    /**
     * リセット.
     */
    @Override
    public void reset() {
        super.reset();
        stationName = null;
        songTitle = null;
        artistName = null;
        albumName = null;
        thumbStatus = ThumbStatus.NONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    MoreObjects.ToStringHelper addToString(MoreObjects.ToStringHelper helper) {
        return super.addToString(helper)
                .add("stationName", stationName)
                .add("songTitle", songTitle)
                .add("artistName", artistName)
                .add("albumName", albumName)
                .add("thumbStatus", thumbStatus);
    }
}
