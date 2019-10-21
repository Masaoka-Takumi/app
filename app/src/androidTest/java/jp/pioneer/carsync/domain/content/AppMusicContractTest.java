package jp.pioneer.carsync.domain.content;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.*;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ObjectArrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.domain.util.QueryUtil;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.application.content.ProviderContract.Artwork.CONTENT_URI;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

import static jp.pioneer.carsync.domain.content.AppMusicContract.*;
import static org.mockito.Mockito.mock;

/**
 * Created by NSW00_008320 on 2017/05/09.
 */
@RunWith(Enclosed.class)
public class AppMusicContractTest {
    public static class QueryParamsBuilderTest {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        }

        @Test
        public void createAlbumsByKeywords() throws Exception {
            // setup
            String[] keywords = {"TEST","TEST"};

            // exercise
            QueryParams actual = QueryParamsBuilder.createAlbumsByKeywords(keywords);

            // verify
            assertThat(actual,is(new QueryParams(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    Album.PROJECTION_ALBUM,
                    QueryUtil.makeLikeSelection(MediaStore.Audio.Albums.ALBUM, keywords.length),
                    QueryUtil.makeLikeSelectionArgs(keywords),
                    Album.SORT_ORDER,
                    Album.INDEX_COLUMN
            )));
        }

        @Test(expected = NullPointerException.class)
        public void createAlbumsByKeywordsArgNull() throws Exception {
            // exercise
            QueryParams queryParams = QueryParamsBuilder.createAlbumsByKeywords(null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void createAlbumsByKeywordsArgSizeMin() throws Exception {
            // setup
            String[] keywords = {};

            // exercise
            QueryParams queryParams = QueryParamsBuilder.createAlbumsByKeywords(keywords);
        }

        @Test
        public void createAlbumsForArtist() throws Exception {
            // exercise
            QueryParams actual = QueryParamsBuilder.createAlbumsForArtist(1);

            // verify
            assertThat(actual,is(new QueryParams(
                    MediaStore.Audio.Artists.Albums.getContentUri("external", 1),
                    Album.PROJECTION_ARTIST,
                    null,
                    null,
                    Album.SORT_ORDER,
                    Album.INDEX_COLUMN
            )));
        }

        @Test
        public void createAllAlbums() throws Exception {
            // exercise
            QueryParams actual = QueryParamsBuilder.createAllAlbums();

            // verify
            assertThat(actual,is(new QueryParams(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    Album.PROJECTION_ALBUM,
                    null,
                    null,
                    Album.SORT_ORDER,
                    Album.INDEX_COLUMN
            )));
        }

        @Test
        public void createAllArtists() throws Exception {
            // exercise
            QueryParams actual = QueryParamsBuilder.createAllArtists();

            // verify
            assertThat(actual,is(new QueryParams(
                    MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                    Artist.PROJECTION,
                    null,
                    null,
                    Artist.SORT_ORDER,
                    Artist.INDEX_COLUMN
            )));
        }

        @Test
        public void createAllGenres() throws Exception {
            // exercise
            QueryParams actual = QueryParamsBuilder.createAllGenres();

            // verify
            assertThat(actual,is(new QueryParams(
                    MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                    Genre.PROJECTION,
                    null,
                    null,
                    Genre.SORT_ORDER,
                    Genre.INDEX_COLUMN
            )));
        }

        @Test
        public void createAllPlaylists() throws Exception {
            // exercise
            QueryParams actual = QueryParamsBuilder.createAllPlaylists();

            // verify
            assertThat(actual,is(new QueryParams(
                    MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    Playlist.PROJECTION,
                    null,
                    null,
                    Playlist.SORT_ORDER,
                    Playlist.INDEX_COLUMN
            )));
        }

        @Test
        public void createAllSongs() throws Exception {
            // exercise
            QueryParams actual = QueryParamsBuilder.createAllSongs();

            // verify
            assertThat(actual,is(new QueryParams(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    Song.PROJECTION_MEDIA,
                    MediaStore.Audio.Media.IS_MUSIC + " = ?",
                    new String[] { "1" },
                    Song.SORT_ORDER,
                    Song.INDEX_COLUMN
            )));
        }

        @Test
        public void createSongsById() throws Exception {
            // setup
            String[] keywords = {"TEST","TEST"};

            // exercise
            QueryParams actual = QueryParamsBuilder.createArtistsByKeywords(keywords);

            // verify
            assertThat(actual,is(new QueryParams(
                    MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                    Artist.PROJECTION,
                    QueryUtil.makeLikeSelection(MediaStore.Audio.Artists.ARTIST, keywords.length),
                    QueryUtil.makeLikeSelectionArgs(keywords),
                    Artist.SORT_ORDER,
                    Artist.INDEX_COLUMN
            )));
        }

        @Test
        public void createArtistsByKeywords() throws Exception {
            // exercise
            QueryParams actual = QueryParamsBuilder.createSongsById(1);

            // verify
            assertThat(actual,is(new QueryParams(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    Song.PROJECTION_MEDIA,
                    MediaStore.Audio.Media.IS_MUSIC + " = ? AND " + MediaStore.Audio.Media._ID + " = ?",
                    new String[] {"1", String.valueOf(1)},
                    Song.SORT_ORDER_FOR_ALBUM,
                    Song.INDEX_COLUMN
            )));
        }

        @Test(expected = NullPointerException.class)
        public void createArtistsByKeywordsArgNull() throws Exception {
            // exercise
            QueryParams queryParams = QueryParamsBuilder.createArtistsByKeywords(null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void createArtistsByKeywordsArgSizeMin() throws Exception {
            // setup
            String[] keywords = {};

            // exercise
            QueryParams queryParams = QueryParamsBuilder.createArtistsByKeywords(keywords);
        }

        @Test
        public void createSongsByKeywords() throws Exception {
            // setup
            String[] keywords = {"TEST","TEST"};

            // exercise
            QueryParams actual = QueryParamsBuilder.createSongsByKeywords(keywords);

            // verify
            assertThat(actual,is(new QueryParams(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    Song.PROJECTION_MEDIA,
                    MediaStore.Audio.Media.IS_MUSIC + " = ? AND " + QueryUtil.makeLikeSelection(MediaStore.Audio.Media.TITLE, keywords.length),
                    ObjectArrays.concat(new String[] {"1"}, QueryUtil.makeLikeSelectionArgs(keywords), String.class),
                    Song.SORT_ORDER,
                    Song.INDEX_COLUMN
            )));
        }

        @Test(expected = NullPointerException.class)
        public void createSongsByKeywordsArgNull() throws Exception {
            // exercise
            QueryParams queryParams = QueryParamsBuilder.createSongsByKeywords(null);
        }

        @Test(expected = IllegalArgumentException.class)
        public void createSongsByKeywordsArgSizeMin() throws Exception {
            // setup
            String[] keywords = {};

            // exercise
            QueryParams queryParams = QueryParamsBuilder.createSongsByKeywords(keywords);
        }

        @Test
        public void createSongsForAlbum() throws Exception {
            // exercise
            QueryParams actual = QueryParamsBuilder.createSongsForAlbum(1);

            // verify
            assertThat(actual,is(new QueryParams(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    Song.PROJECTION_MEDIA,
                    MediaStore.Audio.Media.IS_MUSIC + " = ? AND " + MediaStore.Audio.Media.ALBUM_ID + " = ?",
                    new String[] {"1", String.valueOf(1)},
                    Song.SORT_ORDER_FOR_ALBUM,
                    Song.INDEX_COLUMN
            )));
        }

        @Test
        public void createSongsForArtist() throws Exception {
            // exercise
            QueryParams actual = QueryParamsBuilder.createSongsForArtist(1);

            // verify
            assertThat(actual,is(new QueryParams(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    Song.PROJECTION_MEDIA,
                    MediaStore.Audio.Media.IS_MUSIC + " = ? AND " + MediaStore.Audio.Media.ARTIST_ID + " = ?",
                    new String[] {"1", String.valueOf(1)},
                    Song.SORT_ORDER,
                    Song.INDEX_COLUMN
            )));
        }

        @Test
        public void createSongsForArtistAlbum() throws Exception {
            // exercise
            QueryParams actual = QueryParamsBuilder.createSongsForArtistAlbum(1,2);

            // verify
            assertThat(actual,is(new QueryParams(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    Song.PROJECTION_MEDIA,
                    MediaStore.Audio.Media.IS_MUSIC + " = ? AND " + MediaStore.Audio.Media.ARTIST_ID + " = ? AND " + MediaStore.Audio.Media.ALBUM_ID + " = ?",
                    new String[] {"1", String.valueOf(1), String.valueOf(2)},
                    Song.SORT_ORDER_FOR_ALBUM,
                    Song.INDEX_COLUMN
            )));
        }

        @Test
        public void createSongsForGenre() throws Exception {
            // exercise
            QueryParams actual = QueryParamsBuilder.createSongsForGenre(1);

            // verify
            assertThat(actual,is(new QueryParams(
                    MediaStore.Audio.Genres.Members.getContentUri("external", 1),
                    Song.PROJECTION_GENRE,
                    MediaStore.Audio.Media.IS_MUSIC + " = ?",
                    new String[] {"1"},
                    Song.SORT_ORDER,
                    Song.INDEX_COLUMN
            )));
        }

        @Test
        public void createSongsForPlaylist() throws Exception {
            // exercise
            QueryParams actual = QueryParamsBuilder.createSongsForPlaylist(1);

            // verify
            assertThat(actual,is(new QueryParams(
                    MediaStore.Audio.Playlists.Members.getContentUri("external", 1),
                    Song.PROJECTION_PLAYLIST,
                    MediaStore.Audio.Media.IS_MUSIC + " = ?",
                    new String[] {"1"},
                    Song.SORT_ORDER_FOR_PLAYLIST,
                    Song.INDEX_COLUMN
            )));
        }

        @Test
        public void createGenresForAudioId() throws Exception {
            // exercise
            QueryParams actual = QueryParamsBuilder.createGenresForAudioId(1);

            // verify
            assertThat(actual,is(new QueryParams(
                    MediaStore.Audio.Genres.getContentUriForAudioId("external", 1),
                    Genre.PROJECTION,
                    null,
                    null,
                    Genre.SORT_ORDER,
                    null
            )));
        }

    }

    public static class PlayParamsTest {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        QueryParams mQueryParams;
        ShuffleMode mShuffleMode;
        long mAudioId;


        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

            mQueryParams = mock(QueryParams.class);
            mShuffleMode = mock(ShuffleMode.class);
            mAudioId = 1;
        }

        @Test
        public void createPlayParamsArgQueryParams() throws Exception {
            // exercise
            PlayParams actual = PlayParams.createPlayParams(mQueryParams);

            // verify
            assertThat(actual.queryParams,is(mQueryParams));
            assertThat(actual.shuffleMode,is(nullValue()));
            assertThat(actual.audioId,is(-1l));
        }

        @Test(expected = NullPointerException.class)
        public void createPlayParamsArgQueryParamsNull() throws Exception {
            // exercise
            PlayParams playParams = PlayParams.createPlayParams(null);
        }

        @Test
        public void createPlayParamsArgNullQueryParams_AudioId() throws Exception {
            // exercise
            PlayParams actual = PlayParams.createPlayParams(mQueryParams,mAudioId);

            // verify
            assertThat(actual.queryParams,is(mQueryParams));
            assertThat(actual.shuffleMode,is(nullValue()));
            assertThat(actual.audioId,is(mAudioId));
        }

        @Test(expected = NullPointerException.class)
        public void createPlayParamsArgQueryParams_AudioId() throws Exception {
            // exercise
            PlayParams playParams = PlayParams.createPlayParams(null,mAudioId);
        }

        @Test
        public void createPlayParamsArgQueryParams_ShuffleMode() throws Exception {
            // exercise
            PlayParams actual = PlayParams.createPlayParams(mQueryParams,mShuffleMode);

            // verify
            assertThat(actual.queryParams,is(mQueryParams));
            assertThat(actual.shuffleMode,is(mShuffleMode));
            assertThat(actual.audioId,is(-1l));
        }

        @Test(expected = NullPointerException.class)
        public void createPlayParamsArgNullQueryParams_ShuffleMode() throws Exception {
            // exercise
            PlayParams playParams = PlayParams.createPlayParams(null,mShuffleMode);
        }

        @Test
        public void createPlayParamsArgQueryParams_ShuffleMode_AudioId() throws Exception {
            // exercise
            PlayParams actual = PlayParams.createPlayParams(mQueryParams,mAudioId,mShuffleMode);

            // verify
            assertThat(actual.queryParams,is(mQueryParams));
            assertThat(actual.shuffleMode,is(mShuffleMode));
            assertThat(actual.audioId,is(mAudioId));
        }

        @Test(expected = NullPointerException.class)
        public void createPlayParamsArgNullQueryParams_ShuffleMode_AudioId() throws Exception {
            // exercise
            PlayParams playParams = PlayParams.createPlayParams(null,mAudioId,mShuffleMode);
        }

        @Test
        public void playParamsToString() throws Exception {
            // setup
            PlayParams playParams = PlayParams.createPlayParams(mQueryParams);

            // exercise
            String actual = playParams.toString();

            // verify
            assertThat(actual,is(
                    MoreObjects.toStringHelper("")
                        .add("queryParams", mQueryParams)
                        .add("shuffleMode", null)
                        .add("audioId", -1)
                        .toString()));
        }

    }

    public static class SongTest {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        Cursor mCursor;

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

            String[] projection = new String[] {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.TRACK,
                    MediaStore.Audio.Media.DATA,
            };
            MatrixCursor cursor = new MatrixCursor(projection);
            cursor.addRow(new Object[] { 1,"titleTest","artistTest","albumTest",2,3,"dataTest"});
            mCursor = cursor;
            mCursor.moveToFirst();
        }

        @Test
        public void getId() throws Exception {
            //exercise
            long actual = Song.getId(mCursor);

            // verify
            assertThat(actual,is(1l));
        }

        @Test(expected = NullPointerException.class)
        public void getIdArgNull() throws Exception {
            //exercise
            long id = Song.getId(null);
        }

        @Test
        public void getTitle() throws Exception {
            //exercise
            String actual = Song.getTitle(mCursor);

            // verify
            assertThat(actual,is("titleTest"));
        }

        @Test(expected = NullPointerException.class)
        public void getTitleArgNull() throws Exception {
            //exercise
            String title = Song.getTitle(null);
        }

        @Test
        public void getArtist() throws Exception {
            //exercise
            String actual = Song.getArtist(mCursor);

            // verify
            assertThat(actual,is("artistTest"));
        }

        @Test(expected = NullPointerException.class)
        public void getArtistArgNull() throws Exception {
            //exercise
            String artist = Song.getArtist(null);
        }

        @Test
        public void getAlbumId() throws Exception {
            //exercise
            long actual = Song.getAlbumId(mCursor);

            // verify
            assertThat(actual,is(2l));
        }

        @Test(expected = NullPointerException.class)
        public void getAlbumIdArgNull() throws Exception {
            //exercise
            long albumId = Song.getAlbumId(null);
        }

        @Test
        public void getAlbum() throws Exception {
            //exercise
            String actual = Song.getAlbum(mCursor);

            // verify
            assertThat(actual,is("albumTest"));
        }

        @Test(expected = NullPointerException.class)
        public void getAlbumArgNull() throws Exception {
            //exercise
            String album = Song.getAlbum(null);
        }

        @Test
        public void getTrack() throws Exception {
            //exercise
            long actual = Song.getTrack(mCursor);

            // verify
            assertThat(actual,is(3l));
        }

        @Test(expected = NullPointerException.class)
        public void getTrackArgNull() throws Exception {
            //exercise
            long tracktual = Song.getTrack(null);
        }

        @Test
        public void getData() throws Exception {
            //exercise
            String actual = Song.getData(mCursor);

            // verify
            assertThat(actual,is("dataTest"));
        }

        @Test(expected = NullPointerException.class)
        public void getDataArgNull() throws Exception {
            //exercise
            String data = Song.getData(null);
        }

        @Test
        public void getArtworkUri() throws Exception {
            //exercise
            Uri actual = Song.getArtworkUri(mCursor);

            // verify
            assertThat(actual,is(Uri.parse(CONTENT_URI + "/songs/" + 2l)));
        }

        @Test(expected = NullPointerException.class)
        public void getArtworkUriArgNull() throws Exception {
            //exercise
            Uri artworkUri = Song.getArtworkUri(null);
        }

    }

    public static class AlbumTest {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        Cursor mCursor;

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

            String[] projection = new String[] {
                    MediaStore.Audio.Albums._ID,
                    MediaStore.Audio.Albums.ALBUM,
                    MediaStore.Audio.Albums.ARTIST,
            };
            MatrixCursor cursor = new MatrixCursor(projection);
            cursor.addRow(new Object[] { 1,"albumTest","artistTest"});
            mCursor = cursor;
            mCursor.moveToFirst();
        }

        @Test
        public void getId() throws Exception {
            //exercise
            long actual = Album.getId(mCursor);

            // verify
            assertThat(actual,is(1l));
        }

        @Test(expected = NullPointerException.class)
        public void getIdArgNull() throws Exception {
            //exercise
            long id = Album.getId(null);
        }

        @Test
        public void getAlbum() throws Exception {
            //exercise
            String actual = Album.getAlbum(mCursor);

            // verify
            assertThat(actual,is("albumTest"));
        }

        @Test(expected = NullPointerException.class)
        public void getAlbumArgNull() throws Exception {
            //exercise
            String album = Album.getAlbum(null);
        }

        @Test
        public void getArtist() throws Exception {
            //exercise
            String actual = Album.getArtist(mCursor);

            // verify
            assertThat(actual,is("artistTest"));
        }

        @Test(expected = NullPointerException.class)
        public void getArtistArgNull() throws Exception {
            //exercise
            String artist = Album.getArtist(null);
        }

        @Test
        public void getArtworkUri() throws Exception {
            //exercise
            Uri actual = Album.getArtworkUri(mCursor);

            // verify
            assertThat(actual,is(Uri.parse(CONTENT_URI + "/albums/" + 1l)));
        }

        @Test(expected = NullPointerException.class)
        public void getArtworkUriArgNull() throws Exception {
            //exercise
            Uri artworkUri = Album.getArtworkUri(null);
        }

    }

    public static class ArtistTest {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        Cursor mCursor;

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

            String[] projection = new String[] {
                    MediaStore.Audio.Artists._ID,
                    MediaStore.Audio.Artists.ARTIST,
                    MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            };
            MatrixCursor cursor = new MatrixCursor(projection);
            cursor.addRow(new Object[] { 1,"artistTest",2});
            mCursor = cursor;
            mCursor.moveToFirst();
        }

        @Test
        public void getId() throws Exception {
            //exercise
            long actual = Artist.getId(mCursor);

            // verify
            assertThat(actual,is(1l));
        }

        @Test(expected = NullPointerException.class)
        public void getIdArgNull() throws Exception {
            //exercise
            long id = Artist.getId(null);
        }

        @Test
        public void getArtist() throws Exception {
            //exercise
            String actual = Artist.getArtist(mCursor);

            // verify
            assertThat(actual,is("artistTest"));
        }

        @Test(expected = NullPointerException.class)
        public void getArtistArgNull() throws Exception {
            //exercise
            String artist = Artist.getArtist(null);
        }

        @Test
        public void getNumberOfAlbums() throws Exception {
            //exercise
            int actual = Artist.getNumberOfAlbums(mCursor);

            // verify
            assertThat(actual,is(2));
        }

        @Test(expected = NullPointerException.class)
        public void getNumberOfAlbumsArgNull() throws Exception {
            //exercise
            int numberOfAlbums = Artist.getNumberOfAlbums(null);
        }

        @Test
        public void getArtworkUri() throws Exception {
            //exercise
            Uri actual = Artist.getArtworkUri(mCursor);

            // verify
            assertThat(actual,is(Uri.parse(CONTENT_URI + "/artists/" + 1l)));
        }

        @Test(expected = NullPointerException.class)
        public void getArtworkUriArgNull() throws Exception {
            //exercise
            Uri artworkUri = Artist.getArtworkUri(null);
        }

    }

    public static class GenreTest {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        Cursor mCursor;

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

            String[] projection = new String[] {
                    MediaStore.Audio.Genres._ID,
                    MediaStore.Audio.Genres.NAME,
            };
            MatrixCursor cursor = new MatrixCursor(projection);
            cursor.addRow(new Object[] { 1,"genreTest"});
            mCursor = cursor;
            mCursor.moveToFirst();
        }

        @Test
        public void getId() throws Exception {
            //exercise
            long actual = Genre.getId(mCursor);

            // verify
            assertThat(actual,is(1l));
        }

        @Test(expected = NullPointerException.class)
        public void getIdArgNull() throws Exception {
            //exercise
            long id = Genre.getId(null);
        }

        @Test
        public void getName() throws Exception {
            //exercise
            String actual = Genre.getName(mCursor);

            // verify
            assertThat(actual,is("genreTest"));
        }

        @Test(expected = NullPointerException.class)
        public void getNameArgNull() throws Exception {
            //exercise
            String name = Genre.getName(null);
        }

        @Test
        public void getArtworkUri() throws Exception {
            //exercise
            Uri actual = Genre.getArtworkUri(mCursor);

            // verify
            assertThat(actual,is(Uri.parse(CONTENT_URI + "/genres/" + 1l)));
        }

        @Test(expected = NullPointerException.class)
        public void getArtworkUriArgNull() throws Exception {
            //exercise
            Uri artworkUri = Genre.getArtworkUri(null);
        }

    }

    public static class PlaylistTest {
        @Rule public MockitoRule mMockito = MockitoJUnit.rule();
        Cursor mCursor;

        @Before
        public void setUp() throws Exception {
            System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

            String[] projection = new String[] {
                    MediaStore.Audio.Playlists._ID,
                    MediaStore.Audio.Playlists.NAME,
            };
            MatrixCursor cursor = new MatrixCursor(projection);
            cursor.addRow(new Object[] { 1,"playlistTest"});
            mCursor = cursor;
            mCursor.moveToFirst();
        }

        @Test
        public void getId() throws Exception {
            //exercise
            long actual = Playlist.getId(mCursor);

            // verify
            assertThat(actual,is(1l));
        }

        @Test(expected = NullPointerException.class)
        public void getIdArgNull() throws Exception {
            //exercise
            long id = Playlist.getId(null);
        }

        @Test
        public void getName() throws Exception {
            //exercise
            String actual = Playlist.getName(mCursor);

            // verify
            assertThat(actual,is("playlistTest"));
        }

        @Test(expected = NullPointerException.class)
        public void getNameArgNull() throws Exception {
            //exercise
            String name = Playlist.getName(null);
        }

        @Test
        public void getArtworkUri() throws Exception {
            //exercise
            Uri actual = Playlist.getArtworkUri(mCursor);

            // verify
            assertThat(actual,is(Uri.parse(CONTENT_URI + "/playlists/" + 1l)));
        }

        @Test(expected = NullPointerException.class)
        public void getArtworkUriArgNull() throws Exception {
            //exercise
            Uri artworkUri = Playlist.getArtworkUri(null);
        }

    }

}