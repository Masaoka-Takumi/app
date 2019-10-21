package jp.pioneer.carsync.domain.repository;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.content.AppMusicCursorLoader;
import jp.pioneer.carsync.infrastructure.database.AppMusicPlaylistCursor;

/**
 * NowPlayingListリポジトリ.
 */
public interface NowPlayingListRepository {

    /**
     * NowPlayingList.
     * <p>
     * プレイリストCursorから{@link AppMusicCursorLoader} を作成する
     *
     * @param cursor 現在再生中のプレイリストCursor
     * @return {@link AppMusicCursorLoader}
     * @throws NullPointerException {@code cursor}がnull
     */
    AppMusicCursorLoader get(@NonNull AppMusicPlaylistCursor cursor);
}
