package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * USB情報.
 */
public class UsbMediaInfo extends AbstractMediaInfo {
    /** Song Title / File. */
    public String songTitle;
    /** Artist Name / Folder. */
    public String artistName;
    /** Album Title. */
    public String albumName;
    /** Genre. */
    public String genre;
    /** Track Number. */
    public String trackNumber;
    /** 全てのファイルがDRMにより保護されている. */
    public boolean musicProtected;
    /** DRM保護されたファイルを再生した. */
    public boolean drmSkipped;
    /** 再生不可ファイル. */
    public boolean unplayableFile;
    /** USBメモリが接続されていない. */
    public boolean noUsbDevice;

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        super.reset();
        songTitle = null;
        artistName = null;
        albumName = null;
        genre = null;
        trackNumber = null;
        musicProtected = false;
        drmSkipped = false;
        unplayableFile = false;
        noUsbDevice = false;
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
                .add("genre", genre)
                .add("trackNumber", trackNumber)
                .add("musicProtected", musicProtected)
                .add("drmSkipped", drmSkipped)
                .add("unplayableFile", unplayableFile)
                .add("noUsbDevice", noUsbDevice);
    }

    /**
     * エラー状態か否か取得.
     *
     * @return {@code true}:エラー状態である。{@code false}:それ以外。
     */
    public boolean isErrorStatus() {
        return musicProtected || drmSkipped || unplayableFile || noUsbDevice;
    }
}
