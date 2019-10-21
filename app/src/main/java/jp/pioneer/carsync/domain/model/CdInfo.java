package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * CD情報.
 */
@SuppressFBWarnings("UWF_NULL_FIELD")
public class CdInfo extends AbstractMediaInfo {
    /** Track Number / Track Title. */
    public String trackNumber;
    /** Artist Name / Track Artist. */
    public String artistName;
    /** Disc Title / Album Title. */
    public String discTitle;
    /** 全てのファイルがDRMにより保護されている. */
    public boolean musicProtected;
    /** DRM保護されたファイルを再生した. */
    public boolean drmSkipped;

    /**
     * コンストラクタ.
     */
    public CdInfo() {
        reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        super.reset();
        trackNumber = null;
        artistName = null;
        discTitle = null;
        musicProtected = false;
        drmSkipped = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    MoreObjects.ToStringHelper addToString(MoreObjects.ToStringHelper helper) {
        return super.addToString(helper)
                .add("trackNumber", trackNumber)
                .add("artistName", artistName)
                .add("discTitle", discTitle)
                .add("musicProtected", musicProtected)
                .add("drmSkipped", drmSkipped);
    }

    /**
     * エラー状態か否か取得.
     *
     * @return {@code true}:エラー状態である。{@code false}:それ以外。
     */
    public boolean isErrorStatus() {
        return musicProtected || drmSkipped;
    }
}
