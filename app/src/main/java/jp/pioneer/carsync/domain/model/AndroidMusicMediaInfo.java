package jp.pioneer.carsync.domain.model;

import android.net.Uri;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * AppMusic情報.
 */
public class AndroidMusicMediaInfo {
    /** 曲名. */
    public String songTitle;
    /** アーティスト名. */
    public String artistName;
    /** アルバム名. */
    public String albumTitle;
    /** ジャンル. */
    public String genre;
    /** 再生位置（秒）. */
    public int positionInSec;
    /** 曲の長さ（秒）. */
    public int durationInSec;
    /** アートワークのURI. */
    public Uri artworkImageLocation;
    /** トラック番号. */
    public int trackNumber;
    /** メディアID（Media StoreにおけるID）. */
    public long mediaId;

    /**
     * コンストラクタ.
     */
    public AndroidMusicMediaInfo() {
    }

    /**
     * コピーコンストラクタ.
     *
     * @param info コピー元オブジェクト
     */
    public AndroidMusicMediaInfo(AndroidMusicMediaInfo info) {
        songTitle = info.songTitle;
        artistName = info.artistName;
        albumTitle = info.albumTitle;
        genre = info.genre;
        positionInSec = info.positionInSec;
        durationInSec = info.durationInSec;
        artworkImageLocation = info.artworkImageLocation;
        trackNumber = info.trackNumber;
        mediaId = info.mediaId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof AndroidMusicMediaInfo)) {
            return false;
        }

        AndroidMusicMediaInfo other = (AndroidMusicMediaInfo) obj;
        return Objects.equal(songTitle, other.songTitle)
                && Objects.equal(artistName, other.artistName)
                && Objects.equal(albumTitle, other.albumTitle)
                && Objects.equal(genre, other.genre)
                && Objects.equal(positionInSec, other.positionInSec)
                && Objects.equal(durationInSec, other.durationInSec)
                && Objects.equal(artworkImageLocation, other.artworkImageLocation)
                && Objects.equal(trackNumber, other.trackNumber)
                && Objects.equal(mediaId, other.mediaId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(songTitle,
                artistName,
                albumTitle,
                genre,
                positionInSec,
                durationInSec,
                artworkImageLocation,
                artworkImageLocation,
                mediaId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("songTitle", songTitle)
                .add("artistName", artistName)
                .add("albumTitle", albumTitle)
                .add("genre", genre)
                .add("positionInSec", positionInSec)
                .add("durationInSec", durationInSec)
                .add("artworkImageLocation", artworkImageLocation)
                .add("trackNumber", trackNumber)
                .add("mediaId", mediaId)
                .toString();
    }
}
