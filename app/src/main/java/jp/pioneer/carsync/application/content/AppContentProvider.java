package jp.pioneer.carsync.application.content;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Audio.Artists;
import android.provider.MediaStore.Audio.Genres;
import android.provider.MediaStore.Audio.Playlists;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static android.content.ContentResolver.EXTRA_SIZE;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * アプリケーション用のコンテンツプロバイダ.
 * <p>
 * DB（SQLite）で管理するアプリケーション設定のCRUDとArtworkファイルのアクセスを提供する。
 */
public class AppContentProvider extends ContentProvider {
    private static final Uri ALBUM_ART_CONTENT_URI = Uri.parse("content://media/external/audio/albumart");
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int CODE_SONGS_ARTWORK = 0;
    private static final int CODE_ALBUMS_ARTWORK = 1;
    private static final int CODE_ARTISTS_ARTWORK = 2;
    private static final int CODE_GENRES_ARTWORK = 3;
    private static final int CODE_PLAYLISTS_ARTWORK = 4;
    private static final int CODE_FAVORITE_ALL_CODE = 5;
    private static final int CODE_FAVORITE_ID_CODE = 6;
    private static final int CODE_GENRES_ALL_CODE = 7;
    private static final int CODE_ALBUMS_ALL_CODE = 8;
    private static final int CODE_ARTISTS_ALBUMS_ALL_CODE = 9;
    private static final int CODE_GENRES_ID_MEMBERS = 10;
    private static final int CODE_PLAYLISTS_ID_MEMBERS = 11;

    static {
        sUriMatcher.addURI(ProviderContract.AUTHORITY, "artwork/songs/#", CODE_SONGS_ARTWORK);
        sUriMatcher.addURI(ProviderContract.AUTHORITY, "artwork/albums/#", CODE_ALBUMS_ARTWORK);
        sUriMatcher.addURI(ProviderContract.AUTHORITY, "artwork/artists/#", CODE_ARTISTS_ARTWORK);
        sUriMatcher.addURI(ProviderContract.AUTHORITY, "artwork/genres/#", CODE_GENRES_ARTWORK);
        sUriMatcher.addURI(ProviderContract.AUTHORITY, "artwork/playlists/#", CODE_PLAYLISTS_ARTWORK);
        sUriMatcher.addURI(ProviderContract.AUTHORITY, "favorite", CODE_FAVORITE_ALL_CODE);
        sUriMatcher.addURI(ProviderContract.AUTHORITY, "favorite/#", CODE_FAVORITE_ID_CODE);
        sUriMatcher.addURI(ProviderContract.AUTHORITY, "genres", CODE_GENRES_ALL_CODE);
        sUriMatcher.addURI(ProviderContract.AUTHORITY, "albums", CODE_ALBUMS_ALL_CODE);
        sUriMatcher.addURI(ProviderContract.AUTHORITY, "artists/#/albums", CODE_ARTISTS_ALBUMS_ALL_CODE);
        sUriMatcher.addURI(ProviderContract.AUTHORITY, "genres/#/members", CODE_GENRES_ID_MEMBERS);
        sUriMatcher.addURI(ProviderContract.AUTHORITY, "playlists/#/members", CODE_PLAYLISTS_ID_MEMBERS);
    }

    private AppDatabaseHelper mHelper;

    /**
     * コンストラクタ.
     */
    public AppContentProvider() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreate() {
        Context context = getContext();
        mHelper = new AppDatabaseHelper(context);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        checkNotNull(uri);
        checkUri(uri);

        SQLiteDatabase db = mHelper.getWritableDatabase();
        String table = uri.getPathSegments().get(0);
        final long rowId = db.insertOrThrow(table, null, values);
        Uri returnUri = ContentUris.withAppendedId(uri, rowId);
        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(returnUri, null);
        return returnUri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        checkNotNull(uri);

        Context context = getContext();
        if (context == null) {
            Timber.e("query() getContext() is null.");
            return null;
        }

        Cursor cursor;
        int code = checkUri(uri);
        switch (code) {
            case CODE_GENRES_ALL_CODE:
                cursor = queryToMediaStoreGenres(uri, projection, selection, selectionArgs, sortOrder);
                break;
            case CODE_ALBUMS_ALL_CODE:
                cursor = queryToMediaStoreAlbums(uri, projection, selection, selectionArgs, sortOrder);
                break;
            case CODE_ARTISTS_ALBUMS_ALL_CODE:
                cursor = queryToMediaStoreArtistsAlbums(uri, projection, selection, selectionArgs, sortOrder);
                break;
            case CODE_GENRES_ID_MEMBERS:
                long genreId = Long.parseLong(uri.getPathSegments().get(1));
                cursor = new AliasColumnNameCursorWrapper(
                        getContext().getContentResolver().query(
                                MediaStore.Audio.Genres.Members.getContentUri("external", genreId),
                                projection,
                                selection,
                                selectionArgs,
                                sortOrder
                        ),
                        new HashMap<String, String>() {{ put("_id", Genres.Members.AUDIO_ID); }}
                );
                break;
            case CODE_PLAYLISTS_ID_MEMBERS:
                long playlistId = Long.parseLong(uri.getPathSegments().get(1));
                cursor = new AliasColumnNameCursorWrapper(
                        getContext().getContentResolver().query(
                                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                                projection,
                                selection,
                                selectionArgs,
                                sortOrder
                        ),
                        new HashMap<String, String>() {{ put("_id", Playlists.Members.AUDIO_ID); }}
                );
                break;
            default:
                SQLiteDatabase db = mHelper.getReadableDatabase();
                String table = uri.getPathSegments().get(0);
                cursor = db.query(table, projection, appendSelection(code, uri, selection)
                        , appendSelectionArgs(code, uri, selectionArgs), null, null, sortOrder);
        }
        //noinspection ConstantConditions
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        checkNotNull(uri);
        int code = checkUri(uri);

        final int count;
        String table = uri.getPathSegments().get(0);
        SQLiteDatabase db = mHelper.getWritableDatabase();
        count = db.update(table, values, appendSelection(code, uri, selection)
                , appendSelectionArgs(code, uri, selectionArgs));
        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        checkNotNull(uri);
        int code = checkUri(uri);

        SQLiteDatabase db = mHelper.getWritableDatabase();
        String table = uri.getPathSegments().get(0);
        final int count = db.delete(table, appendSelection(code, uri, selection)
                , appendSelectionArgs(code, uri, selectionArgs));
        //noinspection ConstantConditions
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType(@NonNull Uri uri) {
        checkNotNull(uri);

        final int code = sUriMatcher.match(uri);
        if (code == CODE_FAVORITE_ALL_CODE) {
            return ProviderContract.Favorite.MIME_TYPE_MANY;
        } else if (code == CODE_FAVORITE_ID_CODE) {
            return ProviderContract.Favorite.MIME_TYPE_SINGLE;
        }
        throw new IllegalArgumentException("unknown uri : " + uri);
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        checkNotNull(uri);
        checkNotNull(mode);

        Context context = getContext();
        if (context == null) {
            Timber.e("openFile() getContext() is null.");
            return null;
        }

        int code = sUriMatcher.match(uri);
        String path;
        switch (code) {
            case CODE_SONGS_ARTWORK:
            case CODE_ALBUMS_ARTWORK:
                long albumId = Long.parseLong(uri.getLastPathSegment());
                uri = ContentUris.withAppendedId(ALBUM_ART_CONTENT_URI, albumId);
                return context.getContentResolver().openFileDescriptor(uri, mode);
            case CODE_ARTISTS_ARTWORK:
                long artistId = Long.parseLong(uri.getLastPathSegment());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return openArtworkFileForArtist(artistId);
                } else {
                    path = getArtworkPathForArtist(artistId);
                }
                break;
            case CODE_GENRES_ARTWORK:
                long genreId = Long.parseLong(uri.getLastPathSegment());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return openArtworkFileForGenre(genreId);
                } else {
                    path = getArtworkPathForGenre(genreId);
                }
                break;
            case CODE_PLAYLISTS_ARTWORK:
                long playlistId = Long.parseLong(uri.getLastPathSegment());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return openArtworkFileForPlaylist(playlistId);
                } else {
                    path = getArtworkPathForPlaylist(playlistId);
                }
                break;
            default:
                return null;
        }

        if (path == null) {
            return null;
        }

        int modeBits = ParcelFileDescriptor.parseMode(mode);
        return ParcelFileDescriptor.open(new File(path), modeBits);
    }

    /**
     * アーティスト用のArtworkファイルパス取得.
     *
     * @param artistId アーティストID
     * @return Artworkファイルパス。ない場合はnull。
     */
    @Nullable
    private String getArtworkPathForArtist(long artistId) {
        Uri uri = Artists.Albums.getContentUri("external", artistId);
        String[] projection = {Artists.Albums.ALBUM_ART};
        String selection = Audio.AudioColumns.ARTIST_ID + " = ? AND " + Artists.Albums.ALBUM_ART + " IS NOT NULL";
        String[] selectionArgs = {String.valueOf(artistId)};
        String sortOrder = Artists.Albums.ALBUM + " COLLATE LOCALIZED ASC";
        //noinspection ConstantConditions
        Cursor cursor = getContext().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        if (cursor == null) {
            return null;
        }

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndexOrThrow(Artists.Albums.ALBUM_ART));
        } finally {
            cursor.close();
        }
    }

    /**
     * アーティスト用のArtworkファイルオープン.
     *
     * Android10以降用。
     *
     * @param artistId アーティストID
     * @return ParcelFileDescriptor
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Nullable
    private ParcelFileDescriptor openArtworkFileForArtist(long artistId) {
        Uri uri = Artists.Albums.getContentUri("external", artistId);
        String[] projection = {Artists.Albums.ALBUM_ID};
        String selection = Audio.AudioColumns.ARTIST_ID + " = ?";
        String[] selectionArgs = {String.valueOf(artistId)};
        String sortOrder = Artists.Albums.ALBUM + " COLLATE LOCALIZED ASC";
        //noinspection ConstantConditions
        Cursor cursor = getContext().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        if (cursor == null) {
            return null;
        }

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            ContentResolver resolver = getContext().getContentResolver();
            Uri baseUri = Albums.getContentUri("external");
            int index = cursor.getColumnIndexOrThrow(Artists.Albums.ALBUM_ID);
            // MediaProvider#openTypedAssetFileCommonのwantsThumbを満たすためにEXTRA_SIZEが必要
            Bundle opts = new Bundle();
            opts.putParcelable(EXTRA_SIZE, new Point(240, 240));
            while (cursor.moveToNext()) {
                try {
                    return resolver.openTypedAssetFile(
                            ContentUris.withAppendedId(baseUri, cursor.getLong(index)),
                            "image/*",
                            opts,
                            null).getParcelFileDescriptor();
                } catch (FileNotFoundException e){
                    // 見つかるまでループ
                }
            }

            return null;
        } finally {
            cursor.close();
        }
    }

    /**
     * ジャンル用のArtworkファイルパス取得.
     *
     * @param genreId ジャンルID
     * @return Artworkファイルパス。ない場合はnull。
     */
    @Nullable
    private String getArtworkPathForGenre(long genreId) {
        Uri uri = Genres.Members.getContentUri("external", genreId);
        String albumIdColumn = Genres.Members.ALBUM_ID;
        return getAlbumArt(createAlbumIdSet(uri, albumIdColumn));
    }

    /**
     * ジャンル用のArtworkファイルオープン.
     *
     * Android10以降用。
     *
     * @param genreId ジャンルID
     * @return ParcelFileDescriptor
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Nullable
    private ParcelFileDescriptor openArtworkFileForGenre(long genreId) {
        Uri uri = Genres.Members.getContentUri("external", genreId);
        String albumIdColumn = Genres.Members.ALBUM_ID;
        return openAlbumArtFile(createAlbumIdSet(uri, albumIdColumn));
    }

    /**
     * プレイリストのArtworkファイルパス取得.
     *
     * @param playlistId プレイリストID
     * @return Artworkファイルパス。ない場合はnull。
     */
    @Nullable
    private String getArtworkPathForPlaylist(long playlistId) {
        Uri uri = Playlists.Members.getContentUri("external", playlistId);
        String albumIdColumn = Playlists.Members.ALBUM_ID;
        return getAlbumArt(createAlbumIdSet(uri, albumIdColumn));
    }

    /**
     * プレイリストのArtworkファイルオープン.
     *
     * Android10以降用。
     *
     * @param playlistId プレイリストID
     * @return ParcelFileDescriptor
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Nullable
    private ParcelFileDescriptor openArtworkFileForPlaylist(long playlistId) {
        Uri uri = Playlists.Members.getContentUri("external", playlistId);
        String albumIdColumn = Playlists.Members.ALBUM_ID;
        return openAlbumArtFile(createAlbumIdSet(uri, albumIdColumn));
    }

    /**
     * アルバムIDセット生成.
     *
     * @param uri           コンテントURI
     * @param albumIdColumn アルバムIDのカラム名
     * @return アルバムIDセット
     */
    @NonNull
    private Set<Long> createAlbumIdSet(Uri uri, String albumIdColumn) {
        Set<Long> result = new HashSet<>();
        String[] projection = {albumIdColumn};
        //noinspection ConstantConditions
        Cursor cursor = getContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) {
            return result;
        }

        try {
            int columnIndex = cursor.getColumnIndex(albumIdColumn);
            while (cursor.moveToNext()) {
                result.add(cursor.getLong(columnIndex));
            }

            return result;
        } finally {
            cursor.close();
        }
    }

    /**
     * アルバムアート取得.
     *
     * @param albumIdSet アルバムIDセット
     * @return アルバムアート(ファイルのパス)。ない場合はnull。
     */
    @Nullable
    private String getAlbumArt(Set<Long> albumIdSet) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (Long albumId : albumIdSet) {
            if (!isFirst) {
                sb.append(",");
            }

            sb.append(albumId.toString());
            isFirst = false;
        }

        Uri uri = Albums.EXTERNAL_CONTENT_URI;
        String[] projection = {Albums.ALBUM_ART};
        String selection = Albums._ID + " IN (" + sb.toString() + ") AND " + Albums.ALBUM_ART + " IS NOT NULL";
        String sortOrder = Albums.ALBUM + " COLLATE LOCALIZED ASC";
        //noinspection ConstantConditions
        Cursor cursor = getContext().getContentResolver().query(uri, projection, selection, null, sortOrder);
        if (cursor == null) {
            return null;
        }

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex(Albums.ALBUM_ART));
        } finally {
            cursor.close();
        }
    }

    /**
     * アルバムアートファイルオープン.
     *
     * @param albumIdSet アルバムIDセット
     * @return ParcelFileDescriptor
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Nullable
    private ParcelFileDescriptor openAlbumArtFile(Set<Long> albumIdSet) {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (Long albumId : albumIdSet) {
            if (!isFirst) {
                sb.append(",");
            }

            sb.append(albumId.toString());
            isFirst = false;
        }

        Uri uri = Albums.EXTERNAL_CONTENT_URI;
        String[] projection = {Albums._ID};
        String selection = Albums._ID + " IN (" + sb.toString() + ")";
        String sortOrder = Albums.ALBUM + " COLLATE LOCALIZED ASC";
        //noinspection ConstantConditions
        Cursor cursor = getContext().getContentResolver().query(uri, projection, selection, null, sortOrder);
        if (cursor == null) {
            return null;
        }

        try {
            if (cursor.getCount() == 0) {
                return null;
            }

            ContentResolver resolver = getContext().getContentResolver();
            Uri baseUri = Albums.getContentUri("external");
            int index = cursor.getColumnIndexOrThrow(Albums._ID);
            // MediaProvider#openTypedAssetFileCommonのwantsThumbを満たすためにEXTRA_SIZEが必要
            Bundle opts = new Bundle();
            opts.putParcelable(EXTRA_SIZE, new Point(240, 240));
            while (cursor.moveToNext()) {
                try {
                    return resolver.openTypedAssetFile(
                            ContentUris.withAppendedId(baseUri, cursor.getLong(index)),
                            "image/*",
                            opts,
                            null).getParcelFileDescriptor();
                } catch (FileNotFoundException e){
                    // 見つかるまでループ
                }
            }

            return null;
        } finally {
            cursor.close();
        }
    }

    /**
     * uriに_idが設定されている場合にselectionに_idの条件文を追加する
     *
     * @param code      Matcherで取得したURIのコード
     * @param uri       URI
     * @param selection Selection
     * @return _idの条件分が追加されたselection
     */
    private String appendSelection(int code, Uri uri, String selection) {
        List<String> pathSegments = uri.getPathSegments();
        if (code == CODE_FAVORITE_ID_CODE || pathSegments.size() == 1) {
            return selection;
        }
        return BaseColumns._ID + " = ?"
                + (selection == null ? "" : " AND (" + selection + ")");
    }

    /**
     * uriに_idが設定されている場合にselectionArgsにidの値を追加する
     *
     * @param code          Matcherで取得したURIのコード
     * @param uri           URI
     * @param selectionArgs SelectionArgs
     * @return _idの条件分が追加されたselectionArgs
     */
    private String[] appendSelectionArgs(int code, Uri uri, String[] selectionArgs) {
        List<String> pathSegments = uri.getPathSegments();
        if (code == CODE_FAVORITE_ID_CODE || pathSegments.size() == 1) {
            return selectionArgs;
        }
        if (selectionArgs == null || selectionArgs.length == 0) {
            return new String[]{pathSegments.get(1)};
        }
        String[] returnArgs = new String[selectionArgs.length + 1];
        returnArgs[0] = pathSegments.get(1);
        System.arraycopy(selectionArgs, 0, returnArgs, 1, selectionArgs.length);
        return returnArgs;
    }

    /**
     * 有効URI判定
     * <p>
     * お気に入りのURIか判定。
     *
     * @param uri URI
     * @return code
     */
    private int checkUri(Uri uri) {
        final int code = sUriMatcher.match(uri);
        switch (code){
            case CODE_FAVORITE_ALL_CODE:
            case CODE_FAVORITE_ID_CODE:
            case CODE_GENRES_ALL_CODE:
            case CODE_ALBUMS_ALL_CODE:
            case CODE_ARTISTS_ALBUMS_ALL_CODE:
            case CODE_GENRES_ID_MEMBERS:
            case CODE_PLAYLISTS_ID_MEMBERS:
                return code;
            default:
                throw new IllegalArgumentException("unknown uri : " + uri);
        }
    }

    private Cursor queryToMediaStoreGenres(Uri uri, String[] projection, String selection,
                                           String[] selectionArgs, String sortOrder) {
        String table = uri.getPathSegments().get(0);
        Uri mediaStoreUri;
        if ("genres".equals(table)) {
            mediaStoreUri = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;
        } else {
            return null;
        }

        // #4789 対応。Projection は null を指定する。
        String[] customProjection = null;

        if(getContext() == null){
            return null;
        }

        Cursor c = getContext().getContentResolver()
                .query(mediaStoreUri, customProjection, selection, selectionArgs, sortOrder);
        if (c == null) {
            return null;
        }

        int idPos = c.getColumnIndexOrThrow(Genres._ID);
        List<Long> invalidIds = new ArrayList<>();
        while (c.moveToNext()) {
            long tmpId = c.getLong(idPos);
            if (isMemberEmpty(table, tmpId)) {
                invalidIds.add(tmpId);
            }
        }
        if (invalidIds.isEmpty()) {
            // 問題なし
            c.moveToPosition(-1); // 初期の位置に戻す
            return c;
        } else {
            c.close();

            try {
                String customSelection;
                // invalidなIDを検索結果から除外する
                if (TextUtils.isEmpty(selection)) {
                    customSelection = " audio_genres." + Genres._ID  + " NOT IN (" + TextUtils.join(", ", invalidIds) + ")";
                } else {
                    customSelection = selection + " AND " + " audio_genres." + Genres._ID + " NOT IN (" + TextUtils.join(", ", invalidIds) + ")";
                }

                return getContext().getContentResolver()
                        .query(mediaStoreUri, customProjection, customSelection, selectionArgs, sortOrder);
            } catch (Exception ex){
                return getContext().getContentResolver()
                        .query(mediaStoreUri, customProjection, selection, selectionArgs, sortOrder);
            }
        }
    }

    private boolean isMemberEmpty(String table, long tmpId) {
        Uri uri;
        if ("genres".equalsIgnoreCase(table)) {
            uri = MediaStore.Audio.Genres.Members.getContentUri("external", tmpId);
        } else {
            return false;
        }
        Cursor c = null;
        try {
            if(getContext() == null){
                return false;
            }

            c = getContext().getContentResolver().query(uri, null, null, null, null);
            return c != null && c.getCount() == 0;
        } finally {
            if (c != null) c.close();
        }
    }

    private Cursor queryToMediaStoreAlbums(Uri uri, String[] projection, String selection,
                                           String[] selectionArgs, String sortOrder) {
        String table = uri.getPathSegments().get(0);
        Uri mediaStoreUriForAllMedia;
        Uri mediaStoreUriForAlbum;
        if ("albums".equals(table)) {
            mediaStoreUriForAllMedia = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            mediaStoreUriForAlbum = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        } else {
            return null;
        }

        if(getContext() == null){
            return null;
        }

        Cursor c = getContext().getContentResolver()
                .query(mediaStoreUriForAlbum, projection, selection, selectionArgs, sortOrder);
        if (c == null) {
            return null;
        }

        int idPos = c.getColumnIndexOrThrow("_id");
        List<Long> validIds = new ArrayList<>();
        while (c.moveToNext()) {
            validIds.add(c.getLong(idPos));
        }
        if (validIds.isEmpty()) {
            c.moveToPosition(-1); // 初期の位置に戻す
            return c;
        } else {
            c.close();

            try {
                String[] customProjection = new String[projection.length + 1];
                for (int i = 0;i < projection.length;i++){
                    if(projection[i].startsWith("_id")){
                        customProjection[i] = "album" + projection[i];
                    } else {
                        customProjection[i] = projection[i];
                    }
                }
                customProjection[projection.length] = "album_artist";

                String customSelection;
                if (TextUtils.isEmpty(selection)) {
                    customSelection = " album_id IN (" + TextUtils.join(", ", validIds) + ") ) GROUP BY ( album_id";
                } else {
                    customSelection = selection + " AND _id IN (" + TextUtils.join(", ", validIds) + ") ) GROUP BY ( album_id";
                }

                return new AliasColumnNameCursorWrapper(
                        getContext().getContentResolver().query(mediaStoreUriForAllMedia, customProjection, customSelection, selectionArgs, sortOrder),
                        new HashMap<String, String>() {{ put("_id", Audio.AudioColumns.ALBUM_ID); }});
            } catch (Exception ex){
                return getContext().getContentResolver()
                        .query(mediaStoreUriForAlbum, projection, selection, selectionArgs, sortOrder);
            }
        }
    }

    private Cursor queryToMediaStoreArtistsAlbums(Uri uri, String[] projection, String selection,
                                                 String[] selectionArgs, String sortOrder) {
        String artistId = uri.getPathSegments().get(1);
        Uri mediaStoreUri = Artists.Albums.getContentUri("external", Long.parseLong(artistId));

        // Version10でaudio.album_id列のエイリアスが変更されているのでprojectionを差し替える。
        // Version9 までは「_id」、Version10は「album_id」。
        int apiInt = Build.VERSION.SDK_INT;
        String[] customProjection = new String[projection.length];
        if (apiInt >= 29) {
            for (int i = 0;i < projection.length;i++){
                if(projection[i].startsWith("audio.album_id")){
                    customProjection[i] = Artists.Albums.ALBUM_ID;
                } else {
                    customProjection[i] = projection[i];
                }
            }
        } else {
            customProjection = projection;
        }

        Cursor c = getContext().getContentResolver()
                .query(mediaStoreUri, customProjection, selection, selectionArgs, sortOrder);

        if (apiInt >= 29) {
            return new AliasColumnNameCursorWrapper(c, new HashMap<String, String>() {{put("_id", Artists.Albums.ALBUM_ID); }});
        } else {
            return c;
        }
    }

    /**
     * 別名列名CursorWrapper.
     *
     * Android10でASが使えなくなったので、CursorWrapperで読み替え対応する。
     */
    private static class AliasColumnNameCursorWrapper extends CursorWrapper {
        private Map<String, String> columnNames;

        public AliasColumnNameCursorWrapper(Cursor cursor, Map<String, String> columnNames) {
            super(cursor);
            this.columnNames = columnNames;
        }

        @Override
        public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
            String alias = this.columnNames.get(columnName);
            if (alias != null) {
                return super.getColumnIndexOrThrow(alias);
            } else {
                return super.getColumnIndexOrThrow(columnName);
            }
        }
    }
}
