package jp.pioneer.carsync.domain.content;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Audio.Artists;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.Audio.Genres;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ObjectArrays;

import jp.pioneer.carsync.application.content.ProviderContract;
import jp.pioneer.carsync.domain.content.SortOrder.Collate;
import jp.pioneer.carsync.domain.content.SortOrder.Order;
import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.domain.util.QueryUtil;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.application.content.ProviderContract.Artwork.getAlbumArtworkUri;
import static jp.pioneer.carsync.application.content.ProviderContract.Artwork.getArtistArtworkUri;
import static jp.pioneer.carsync.application.content.ProviderContract.Artwork.getGenreArtworkUri;
import static jp.pioneer.carsync.application.content.ProviderContract.Artwork.getPlaylistArtworkUri;
import static jp.pioneer.carsync.application.content.ProviderContract.Artwork.getSongArtworkUri;

/**
 * AppMusicのコントラクト.
 */
public class AppMusicContract {

    /**
     * {@link QueryParams} のビルダー.
     */
    public static class QueryParamsBuilder {
        private final static String MIME_TYPE_MPEG = "audio/mpeg";
        private final static String MIME_TYPE_WMA = "audio/x-ms-wma";
        private final static String MIME_TYPE_WAV = "audio/x-wav";
        private final static String MIME_TYPE_AAC_1 = "audio/aac";
        private final static String MIME_TYPE_AAC_2 = "audio/aac-adts";
        private final static String MIME_TYPE_M4A = "audio/mp4";
        private final static String[] AVAILABLE_MIME_TYPE = {
                MIME_TYPE_MPEG, MIME_TYPE_WMA, MIME_TYPE_WAV, MIME_TYPE_AAC_1, MIME_TYPE_AAC_2, MIME_TYPE_M4A};

        /**
         * 部分一致検索によってアルバム情報を取得する {@link QueryParams} 生成.
         *
         * @param keywords 部分一致検索キーワード群
         * @return 部分一致検索によってアルバム情報を取得するクエリパラメータ
         * @throws NullPointerException {@code keywords}がnull
         * @throws IllegalArgumentException {@code keywords}の要素数が0
         * @see Album
         */
        @NonNull
        public static QueryParams createAlbumsByKeywords(@NonNull @Size(min = 1) String[] keywords) {
            checkNotNull(keywords);
            checkArgument(keywords.length >= 1);

            return new QueryParams(
                    Albums.EXTERNAL_CONTENT_URI,
                    Album.PROJECTION_ALBUM,
                    QueryUtil.makeLikeSelection(Albums.ALBUM, keywords.length),
                    QueryUtil.makeLikeSelectionArgs(keywords),
                    Album.SORT_ORDER,
                    Album.INDEX_COLUMN
            );
        }

        /**
         * ArtistIdからアルバム情報を取得する {@link QueryParams} 生成.
         *
         * @param artistId ArtistId
         * @return ArtistIdからアルバム情報を取得するクエリパラメータ
         * @see Album
         */
        @NonNull
        public static QueryParams createAlbumsForArtist(long artistId) {
            return new QueryParams(
                    ProviderContract.ArtistsAlbums.getArtistsAlbumsUri(artistId),
                    Album.PROJECTION_ARTIST,
                    null,
                    null,
                    Album.SORT_ORDER,
                    Album.INDEX_COLUMN
            );
        }

        /**
         * アルバム情報を取得する {@link QueryParams} 生成.
         *
         * @return アルバム情報を取得するクエリパラメータ
         * @see Album
         */
        @NonNull
        public static QueryParams createAllAlbums() {
            return new QueryParams(
                    ProviderContract.Albums.CONTENT_URI,
                    Album.PROJECTION_ALBUM,
                    null,
                    null,
                    Album.SORT_ORDER,
                    Album.INDEX_COLUMN
            );
        }

        /**
         * アーティスト情報を取得する {@link QueryParams} 生成.
         *
         * @return アーティスト情報を取得するクエリパラメータ
         * @see Album
         */
        @NonNull
        public static QueryParams createAllArtists() {
            return new QueryParams(
                    Artists.EXTERNAL_CONTENT_URI,
                    Artist.PROJECTION,
                    null,
                    null,
                    Artist.SORT_ORDER,
                    Artist.INDEX_COLUMN
            );
        }

        /**
         * ジャンル情報を取得する {@link QueryParams} 生成.
         *
         * @return ジャンル情報を取得するクエリパラメータ
         * @see Genre
         */
        @NonNull
        public static QueryParams createAllGenres() {
            return new QueryParams(
                    ProviderContract.Genres.CONTENT_URI,
                    Genre.PROJECTION,
                    null,
                    null,
                    Genre.SORT_ORDER,
                    Genre.INDEX_COLUMN
            );
        }

        /**
         * プレイリスト情報を取得する {@link QueryParams} 生成.
         *
         * @return プレイリスト情報を取得するクエリパラメータ
         * @see Playlist
         */
        @NonNull
        public static QueryParams createAllPlaylists() {
            return new QueryParams(
                    Playlists.EXTERNAL_CONTENT_URI,
                    Playlist.PROJECTION,
                    null,
                    null,
                    Playlist.SORT_ORDER,
                    Playlist.INDEX_COLUMN
            );
        }

        /**
         * 楽曲情報を取得する {@link QueryParams} 生成.
         *
         * @return 八曲情報を取得するクエリパラメータ
         * @see Song
         */
        @NonNull
        public static QueryParams createAllSongs() {
            return new QueryParams(
                    Media.EXTERNAL_CONTENT_URI,
                    Song.PROJECTION_MEDIA,
                    Media.IS_MUSIC + " = ? AND " + QueryUtil.makeInSelection(Media.MIME_TYPE, AVAILABLE_MIME_TYPE),
                    ObjectArrays.concat(new String[] {"1"}, AVAILABLE_MIME_TYPE , String.class),
                    Song.SORT_ORDER,
                    Song.INDEX_COLUMN
            );
        }

        /**
         * 部分一致検索によってアーティスト情報を取得する {@link QueryParams} 生成.
         *
         * @param keywords 部分一致検索キーワード群
         * @return 部分一致検索によってアーティスト情報を取得するクエリパラメータ
         * @throws NullPointerException {@code keywords}がnull
         * @throws IllegalArgumentException {@code keywords}の要素数が0
         * @see Artist
         */
        @NonNull
        public static QueryParams createArtistsByKeywords(@NonNull @Size(min = 1) String[] keywords) {
            checkNotNull(keywords);
            checkArgument(keywords.length >= 1);

            return new QueryParams(
                    Artists.EXTERNAL_CONTENT_URI,
                    Artist.PROJECTION,
                    QueryUtil.makeLikeSelection(Artists.ARTIST, keywords.length),
                    QueryUtil.makeLikeSelectionArgs(keywords),
                    Artist.SORT_ORDER,
                    Artist.INDEX_COLUMN
            );
        }

        /**
         * IDから楽曲情報を取得する {@link QueryParams} 生成.
         *
         * @param id id
         * @return idから楽曲情報を取得するクエリパラメータ
         * @see Song
         */
        @NonNull
        public static QueryParams createSongsById(long id) {
            return new QueryParams(
                    Media.EXTERNAL_CONTENT_URI,
                    Song.PROJECTION_MEDIA,
                    Media.IS_MUSIC + " = ? AND " + Media._ID + " = ? AND " + QueryUtil.makeInSelection(Media.MIME_TYPE, AVAILABLE_MIME_TYPE),
                    ObjectArrays.concat(new String[] {"1", String.valueOf(id)}, AVAILABLE_MIME_TYPE , String.class),
                    Song.SORT_ORDER_FOR_ALBUM,
                    Song.INDEX_COLUMN
            );
        }

        /**
         * 部分一致検索によって楽曲情報を取得する {@link QueryParams} 生成.
         *
         * @param keywords 部分一致検索キーワード群
         * @return 部分一致検索によって楽曲情報を取得するクエリパラメータ
         * @throws NullPointerException {@code keywords}がnull
         * @throws IllegalArgumentException {@code keywords}の要素数が0
         * @see Song
         */
        @NonNull
        public static QueryParams createSongsByKeywords(@NonNull @Size(min = 1) String[] keywords) {
            checkNotNull(keywords);
            checkArgument(keywords.length >= 1);

            return new QueryParams(
                    Media.EXTERNAL_CONTENT_URI,
                    Song.PROJECTION_MEDIA,
                    Media.IS_MUSIC + " = ? AND " + QueryUtil.makeInSelection(Media.MIME_TYPE, AVAILABLE_MIME_TYPE) + " AND " + QueryUtil.makeLikeSelection(Media.TITLE, keywords.length),
                    ObjectArrays.concat(new String[] {"1"}, ObjectArrays.concat(AVAILABLE_MIME_TYPE, QueryUtil.makeLikeSelectionArgs(keywords), String.class), String.class),
                    Song.SORT_ORDER,
                    Song.INDEX_COLUMN
            );
        }

        /**
         * AlbumIdから楽曲情報を取得する {@link QueryParams} 生成.
         *
         * @param albumId AlbumId
         * @return AlbumIdから楽曲情報を取得するクエリパラメータ
         * @see Song
         */
        @NonNull
        public static QueryParams createSongsForAlbum(long albumId) {
            return new QueryParams(
                    Media.EXTERNAL_CONTENT_URI,
                    Song.PROJECTION_MEDIA,
                    Media.IS_MUSIC + " = ? AND " + Media.ALBUM_ID + " = ? AND " + QueryUtil.makeInSelection(Media.MIME_TYPE, AVAILABLE_MIME_TYPE),
                    ObjectArrays.concat(new String[] {"1", String.valueOf(albumId)}, AVAILABLE_MIME_TYPE , String.class),
                    Song.SORT_ORDER_FOR_ALBUM,
                    Song.INDEX_COLUMN
            );
        }

        /**
         * ArtistIdから楽曲情報を取得する {@link QueryParams} 生成.
         *
         * @param artistId ArtistId
         * @return ArtistIdから楽曲情報を取得するクエリパラメータ
         * @see Song
         */
        @NonNull
        public static QueryParams createSongsForArtist(long artistId) {
            return new QueryParams(
                    Media.EXTERNAL_CONTENT_URI,
                    Song.PROJECTION_MEDIA,
                    Media.IS_MUSIC + " = ? AND " + Media.ARTIST_ID + " = ? AND " + QueryUtil.makeInSelection(Media.MIME_TYPE, AVAILABLE_MIME_TYPE),
                    ObjectArrays.concat(new String[] {"1", String.valueOf(artistId)}, AVAILABLE_MIME_TYPE , String.class),
                    Song.SORT_ORDER,
                    Song.INDEX_COLUMN
            );
        }

        /**
         * ArtistIdとAlbumIdから楽曲情報を取得する {@link QueryParams} 生成.
         *
         * @param artistId ArtistId
         * @param albumId AlbumId
         * @return ArtistIdとAlbumIdから楽曲情報を取得するクエリパラメータ
         * @see Song
         */
        @NonNull
        public static QueryParams createSongsForArtistAlbum(long artistId, long albumId) {
            return new QueryParams(
                    Media.EXTERNAL_CONTENT_URI,
                    Song.PROJECTION_MEDIA,
                    Media.IS_MUSIC + " = ? AND " + Media.ARTIST_ID + " = ? AND " + Media.ALBUM_ID + " = ? AND " + QueryUtil.makeInSelection(Media.MIME_TYPE, AVAILABLE_MIME_TYPE),
                    ObjectArrays.concat(new String[] {"1", String.valueOf(artistId), String.valueOf(albumId)}, AVAILABLE_MIME_TYPE , String.class),
                    Song.SORT_ORDER_FOR_ALBUM,
                    Song.INDEX_COLUMN
            );
        }

        /**
         * GenreIdから楽曲情報を取得する {@link QueryParams} 生成.
         *
         * @param genreId GenreId
         * @return GenreIdから楽曲情報を取得するクエリパラメータ
         * @see Song
         */
        @NonNull
        public static QueryParams createSongsForGenre(long genreId) {
            return new QueryParams(
                    ProviderContract.Genres.Members.getContentUri(genreId),
                    Song.PROJECTION_GENRE,
                    Media.IS_MUSIC + " = ? AND " + QueryUtil.makeInSelection(Media.MIME_TYPE, AVAILABLE_MIME_TYPE),
                    ObjectArrays.concat(new String[] {"1"}, AVAILABLE_MIME_TYPE , String.class),
                    Song.SORT_ORDER,
                    Song.INDEX_COLUMN
            );
        }

        /**
         * PlaylistIdから楽曲情報を取得する {@link QueryParams} 生成.
         *
         * @param playlistId PlaylistId
         * @return PlaylistIdから楽曲情報を取得するクエリパラメータ
         * @see Song
         */
        @NonNull
        public static QueryParams createSongsForPlaylist(long playlistId) {
            return new QueryParams(
                    ProviderContract.Playlists.Members.getContentUri(playlistId),
                    Song.PROJECTION_PLAYLIST,
                    Media.IS_MUSIC + " = ? AND " + QueryUtil.makeInSelection(Media.MIME_TYPE, AVAILABLE_MIME_TYPE),
                    ObjectArrays.concat(new String[] {"1"}, AVAILABLE_MIME_TYPE , String.class),
                    Song.SORT_ORDER_FOR_PLAYLIST,
                    Song.INDEX_COLUMN
            );
        }

        /**
         * AudioIdからジャンル情報を取得する {@link QueryParams} 生成.
         *
         * @param audioId AudioId
         * @return AudioIdからジャンル情報を取得するクエリパラメータ
         * @see Genre
         */
        @NonNull
        public static QueryParams createGenresForAudioId(long audioId) {
            return new QueryParams(
                    Genres.getContentUriForAudioId("external", (int) audioId),
                    Genre.PROJECTION,
                    null,
                    null,
                    Genre.SORT_ORDER,
                    null
            );
        }
    }

    /**
     * Playパラメータ
     * <p>
     * AppMusicに使用するパラメータをまとめたもの
     */
    public static class PlayParams {
        public final QueryParams queryParams;
        public final ShuffleMode shuffleMode;
        public final long audioId;

        /**
         * {@link PlayParams} 生成.
         *
         * @param queryParams 情報を取得するクエリパラメータ
         * @return Playパラメータ
         * @throws NullPointerException {@code queryParams}がnull
         */
        @NonNull
        public static PlayParams createPlayParams(@NonNull QueryParams queryParams) {
            checkNotNull(queryParams);

            return new PlayParams(queryParams, -1, null);
        }

        /**
         * {@link PlayParams} 生成.
         *
         * @param queryParams 情報を取得するクエリパラメータ
         * @param audioId AudioId
         * @return Playパラメータ
         * @throws NullPointerException {@code queryParams}がnull
         */
        @NonNull
        public static PlayParams createPlayParams(@NonNull QueryParams queryParams, long audioId) {
            checkNotNull(queryParams);

            return new PlayParams(queryParams, audioId, null);
        }

        /**
         * {@link PlayParams} 生成.
         *
         * @param queryParams 情報を取得するクエリパラメータ
         * @param shuffleMode プレイリストのシャッフルモード
         * @return Playパラメータ
         * @throws NullPointerException {@code queryParams}がnull
         */
        @NonNull
        public static PlayParams createPlayParams(@NonNull QueryParams queryParams, @Nullable ShuffleMode shuffleMode) {
            checkNotNull(queryParams);

            return new PlayParams(queryParams, -1, shuffleMode);
        }

        /**
         * {@link PlayParams} 生成.
         *
         * @param queryParams 情報を取得するクエリパラメータ
         * @param audioId AudioId
         * @param shuffleMode プレイリストのシャッフルモード
         * @return Playパラメータ
         * @throws NullPointerException {@code queryParams}がnull
         */
        @NonNull
        public static PlayParams createPlayParams(@NonNull QueryParams queryParams, long audioId, @Nullable ShuffleMode shuffleMode) {
            checkNotNull(queryParams);

            return new PlayParams(queryParams, audioId, shuffleMode);
        }

        private PlayParams(@NonNull QueryParams queryParams, long audioId, @Nullable ShuffleMode shuffleMode) {
            this.queryParams = queryParams;
            this.shuffleMode = shuffleMode;
            this.audioId = audioId;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return MoreObjects.toStringHelper("")
                    .add("queryParams", queryParams)
                    .add("shuffleMode", shuffleMode)
                    .add("audioId", audioId)
                    .toString();
        }
    }

    /**
     * 楽曲情報.
     */
    public static class Song {
        public enum Column {
            ID(Media._ID),
            TITLE(Media.TITLE),
            ARTIST(Media.ARTIST),
            ALBUM(Media.ALBUM),
            ALBUM_ID(Media.ALBUM_ID),
            TRACK(Media.TRACK),
            DATA(Media.DATA)
            ;

            private final String mName;

            Column(String name) {
                mName = name;
            }

            public String getName() {
                return mName;
            }

            @Override
            public String toString() {
                return mName;
            }
        }


        static final String SORT_ORDER = new SortOrder(Media.TITLE, Collate.LOCALIZED, Order.ASC).toQuery();
        static final String SORT_ORDER_FOR_ALBUM = new SortOrder(Media.TRACK, Order.ASC).toQuery();
        static final String SORT_ORDER_FOR_PLAYLIST = new SortOrder(Playlists.Members.PLAY_ORDER, Order.ASC).toQuery();

        static final String INDEX_COLUMN = Column.TITLE.getName();

        static final String[] PROJECTION_MEDIA = {
                Media._ID,
                Media.ARTIST,
                Media.TITLE,
                Media.ALBUM,
                Media.ALBUM_ID,
                Media.TRACK,
                Media.DATA
        };

        static final String[] PROJECTION_PLAYLIST = {
                Playlists.Members.AUDIO_ID,
                Playlists.Members.TITLE,
                Playlists.Members.ARTIST,
                Playlists.Members.ALBUM,
                Playlists.Members.ALBUM_ID,
                Playlists.Members.TRACK,
                Playlists.Members.DATA
        };

        static final String[] PROJECTION_GENRE = {
                Genres.Members.AUDIO_ID,
                Genres.Members.TITLE,
                Genres.Members.ARTIST,
                Genres.Members.ALBUM,
                Genres.Members.ALBUM_ID,
                Genres.Members.TRACK,
                Genres.Members.DATA
        };

        /**
         * 楽曲情報 {@code cursor} からIDを取得する.
         *
         * @param cursor 楽曲情報
         * @return 楽曲ID
         * @throws NullPointerException {@code cursor}がnull
         */
        public static long getId(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getLong(cursor.getColumnIndexOrThrow(Column.ID.getName()));
        }

        /**
         * 楽曲情報 {@code cursor} からタイトルを取得する.
         *
         * @param cursor 楽曲情報
         * @return タイトル
         * @throws NullPointerException {@code cursor}がnull
         */
        public static String getTitle(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getString(cursor.getColumnIndexOrThrow(Column.TITLE.getName()));
        }

        /**
         * 楽曲情報 {@code cursor} からアーティスト名を取得する.
         *
         * @param cursor 楽曲情報
         * @return アーティスト名
         * @throws NullPointerException {@code cursor}がnull
         */
        public static String getArtist(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getString(cursor.getColumnIndexOrThrow(Column.ARTIST.getName()));
        }

        /**
         * 楽曲情報 {@code cursor} からアルバムIDを取得する.
         *
         * @param cursor 楽曲情報
         * @return アルバムID
         * @throws NullPointerException {@code cursor}がnull
         */
        public static long getAlbumId(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getLong(cursor.getColumnIndexOrThrow(Column.ALBUM_ID.getName()));
        }

        /**
         * 楽曲情報 {@code cursor} からアルバム名を取得する.
         *
         * @param cursor 楽曲情報
         * @return アルバム名
         * @throws NullPointerException {@code cursor}がnull
         */
        public static String getAlbum(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getString(cursor.getColumnIndexOrThrow(Column.ALBUM.getName()));
        }

        /**
         * 楽曲情報 {@code cursor} からトラック番号を取得する.
         *
         * @param cursor 楽曲情報
         * @return トラック番号
         * @throws NullPointerException {@code cursor}がnull
         */
        public static int getTrack(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getInt(cursor.getColumnIndexOrThrow(Column.TRACK.getName())) % 1000;
        }

        /**
         * 楽曲情報 {@code cursor} からファイルパスを取得する.
         *
         * @param cursor 楽曲情報
         * @return ファイルパス
         * @throws NullPointerException {@code cursor}がnull
         */
        public static String getData(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getString(cursor.getColumnIndexOrThrow(Column.DATA.getName()));
        }

        /**
         * 楽曲情報 {@code cursor} から楽曲アートワークのURIを取得する.
         *
         * @param cursor 楽曲情報
         * @return 楽曲アートワークURI
         * @throws NullPointerException {@code cursor}がnull
         */
        public static Uri getArtworkUri(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return getSongArtworkUri(getAlbumId(cursor));
        }
    }

    /**
     * アルバム情報
     */
    public static class Album {
        public enum Column {
            ID(Albums._ID),
            ALBUM(Albums.ALBUM),
            ARTIST(Albums.ARTIST),
            ;

            private final String mName;

            Column(String name) {
                mName = name;
            }

            public String getName() {
                return mName;
            }

            @Override
            public String toString() {
                return mName;
            }
        }

        static final String SORT_ORDER = new SortOrder(Column.ALBUM.getName(), Collate.LOCALIZED, Order.ASC).toQuery();

        static final String INDEX_COLUMN = Column.ALBUM.getName();

        static final String[] PROJECTION_ALBUM = {
                Albums._ID,
                Albums.ALBUM,
                Albums.ARTIST
        };

        static final String[] PROJECTION_ARTIST = {
                // TODO: テスト時要確認
                // AppContentProvider#queryToMediaStoreArtistsAlbumsにて「AS」を削除しているのでここでの削除は不要。
                "audio." + Artists.Albums.ALBUM_ID + " AS " + Column.ID,
                Artists.Albums.ALBUM,
                Artists.Albums.ARTIST
        };

        /**
         * アルバム情報 {@code cursor} からIDを取得する.
         *
         * @param cursor アルバム情報
         * @return アルバムID
         * @throws NullPointerException {@code cursor}がnull
         */
        public static long getId(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getLong(cursor.getColumnIndexOrThrow(Column.ID.getName()));
        }

        /**
         * アルバム情報 {@code cursor} からアルバム名を取得する.
         *
         * @param cursor アルバム情報
         * @return アルバム名
         * @throws NullPointerException {@code cursor}がnull
         */
        public static String getAlbum(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getString(cursor.getColumnIndexOrThrow(Column.ALBUM.getName()));
        }

        /**
         * アルバム情報 {@code cursor} からアーティスト名を取得する.
         *
         * @param cursor アルバム情報
         * @return アーティスト名
         * @throws NullPointerException {@code cursor}がnull
         */
        public static String getArtist(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            int index = cursor.getColumnIndex("album_artist");
            if (index != -1 && !cursor.isNull(index)) {
                return cursor.getString(index);
            }
            return cursor.getString(cursor.getColumnIndexOrThrow(Column.ARTIST.getName()));
        }

        /**
         * アルバム情報 {@code cursor} からアルバムアートワークのURIを取得する.
         *
         * @param cursor アルバム情報
         * @return アルバムアートワークURI
         * @throws NullPointerException {@code cursor}がnull
         */
        public static Uri getArtworkUri(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return getAlbumArtworkUri(getId(cursor));
        }
    }

    /**
     * アーティスト情報
     */
    public static class Artist {
        public enum Column {
            ID(Artists._ID),
            ARTIST(Artists.ARTIST),
            NUMBER_OF_ALBUMS(Artists.NUMBER_OF_ALBUMS),
            ;

            private final String mName;

            Column(String name) {
                mName = name;
            }

            public String getName() {
                return mName;
            }

            @Override
            public String toString() {
                return mName;
            }
        }

        static final String SORT_ORDER = new SortOrder(Column.ARTIST.getName(), Collate.LOCALIZED, Order.ASC).toQuery();

        static final String INDEX_COLUMN = Column.ARTIST.getName();

        static final String[] PROJECTION = {
                Artists._ID,
                Artists.ARTIST,
                Artists.NUMBER_OF_ALBUMS,
        };

        /**
         * アーティスト情報 {@code cursor} からIDを取得する.
         *
         * @param cursor アーティスト情報
         * @return アーティストID
         * @throws NullPointerException {@code cursor}がnull
         */
        public static long getId(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getLong(cursor.getColumnIndexOrThrow(Column.ID.getName()));
        }

        /**
         * アーティスト情報 {@code cursor} からアーティスト名を取得する.
         *
         * @param cursor アーティスト情報
         * @return アーティスト名
         * @throws NullPointerException {@code cursor}がnull
         */
        public static String getArtist(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getString(cursor.getColumnIndexOrThrow(Column.ARTIST.getName()));
        }

        /**
         * アーティスト情報 {@code cursor} からアルバム番号を取得する.
         *
         * @param cursor アーティスト情報
         * @return アルバム番号
         * @throws NullPointerException {@code cursor}がnull
         */
        public static int getNumberOfAlbums(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getInt(cursor.getColumnIndexOrThrow(Column.NUMBER_OF_ALBUMS.getName()));
        }

        /**
         * アーティスト情報 {@code cursor} からアートワークのURIを取得する.
         *
         * @param cursor アーティスト情報
         * @return アーティストアートワークURI
         * @throws NullPointerException {@code cursor}がnull
         */
        public static Uri getArtworkUri(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return getArtistArtworkUri(getId(cursor));
        }
    }

    /**
     * ジャンル情報
     */
    public static class Genre {
        public enum Column {
            ID(Genres._ID),
            NAME(Genres.NAME),
            ;

            private final String mName;

            Column(String name) {
                mName = name;
            }

            public String getName() {
                return mName;
            }

            @Override
            public String toString() {
                return mName;
            }
        }

        static final String SORT_ORDER = new SortOrder(Column.NAME.getName(), Collate.LOCALIZED, Order.ASC).toQuery();

        static final String INDEX_COLUMN = Column.NAME.getName();

        static final String[] PROJECTION = {
                Genres._ID,
                Genres.NAME,
        };

        /**
         * ジャンル情報 {@code cursor} からIDを取得する.
         *
         * @param cursor ジャンル情報
         * @return ジャンルID
         * @throws NullPointerException {@code cursor}がnull
         */
        public static long getId(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getLong(cursor.getColumnIndexOrThrow(Column.ID.getName()));
        }

        /**
         * ジャンル情報 {@code cursor} からジャンル名を取得する.
         *
         * @param cursor ジャンル情報
         * @return ジャンル名
         * @throws NullPointerException {@code cursor}がnull
         */
        public static String getName(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getString(cursor.getColumnIndexOrThrow(Column.NAME.getName()));
        }

        /**
         * ジャンル情報 {@code cursor} からジャンルアートワークのURIを取得する.
         *
         * @param cursor ジャンル情報
         * @return ジャンルアートワークURI
         * @throws NullPointerException {@code cursor}がnull
         */
        public static Uri getArtworkUri(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return getGenreArtworkUri(getId(cursor));
        }
    }

    /**
     * プレイリスト情報
     */
    public static class Playlist {
        public enum Column {
            ID(Playlists._ID),
            NAME(Playlists.NAME),
            ;

            private final String mName;

            Column(String name) {
                mName = name;
            }

            public String getName() {
                return mName;
            }

            @Override
            public String toString() {
                return mName;
            }
        }

        static final String SORT_ORDER = new SortOrder(Column.NAME.getName(), Collate.LOCALIZED, Order.ASC).toQuery();

        static final String INDEX_COLUMN = Column.NAME.getName();

        static final String[] PROJECTION = {
                Playlists._ID,
                Playlists.NAME,
        };

        /**
         * プレイリスト情報 {@code cursor} からIDを取得する.
         *
         * @param cursor プレイリスト情報
         * @return プレイリストID
         * @throws NullPointerException {@code cursor}がnull
         */
        public static long getId(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getLong(cursor.getColumnIndexOrThrow(Column.ID.getName()));
        }

        /**
         * プレイリスト情報 {@code cursor} からプレイリスト名を取得する.
         *
         * @param cursor プレイリスト情報
         * @return プレイリスト名
         * @throws NullPointerException {@code cursor}がnull
         */
        public static String getName(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return cursor.getString(cursor.getColumnIndexOrThrow(Column.NAME.getName()));
        }

        /**
         * プレイリスト情報 {@code cursor} からプレイリストアートワークのURIを取得する.
         *
         * @param cursor プレイリスト情報
         * @return プレイリストアートワークURI
         * @throws NullPointerException {@code cursor}がnull
         */
        public static Uri getArtworkUri(@NonNull Cursor cursor) {
            checkNotNull(cursor);

            return getPlaylistArtworkUri(getId(cursor));
        }
    }
}
