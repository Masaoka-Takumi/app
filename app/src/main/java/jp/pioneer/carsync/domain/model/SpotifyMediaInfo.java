package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Spotify情報.
 */
@SuppressFBWarnings("UWF_NULL_FIELD")
public class SpotifyMediaInfo extends AbstractMediaInfo {
    /** Radio再生中. */
    public boolean radioPlaying;
    /** Track Name or Spotify ERROR. */
    public String trackNameOrSpotifyError;
    /** Artist Name. */
    public String artistName;
    /** Album Name. */
    public String albumName;
    /** 再生中の再生源 */
    public String playingTrackSource;
    /** Rate設定状態（Thumb Up/Downの状態）. */
    public ThumbStatus thumbStatus;

    /**
     * コンストラクタ.
     */
    public SpotifyMediaInfo() {
        reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        super.reset();
        radioPlaying = false;
        trackNameOrSpotifyError = null;
        artistName = null;
        albumName = null;
        playingTrackSource = null;
        thumbStatus = ThumbStatus.NONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    MoreObjects.ToStringHelper addToString(MoreObjects.ToStringHelper helper) {
        return super.addToString(helper)
                .add("radioPlaying", radioPlaying)
                .add("trackNameOrSpotifyError", trackNameOrSpotifyError)
                .add("artistName", artistName)
                .add("albumName", albumName)
                .add("playingTrackSource", playingTrackSource)
                .add("thumbStatus", thumbStatus);
    }
}
