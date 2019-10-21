package jp.pioneer.carsync.infrastructure.repository;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.content.AppMusicContract;
import jp.pioneer.carsync.domain.content.AppMusicCursorLoader;
import jp.pioneer.carsync.domain.model.SmartPhoneRepeatMode;
import jp.pioneer.carsync.domain.repository.NowPlayingListRepository;
import jp.pioneer.carsync.infrastructure.database.AppMusicPlaylistCursor;
import timber.log.Timber;
import jp.pioneer.carsync.domain.content.AppMusicContract.*;

/**
 * NowPlayingListRepositoryの実装.
 */
public class NowPlayingListRepositoryImpl implements NowPlayingListRepository {
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject @ForInfrastructure Handler mHandler;

    private static final String[] NOW_PLAYING_LIST_COLUMNS = {
            Song.Column.ID.getName(),
            Song.Column.TITLE.getName(),
            Song.Column.ARTIST.getName(),
            Song.Column.ALBUM.getName(),
            Song.Column.ALBUM_ID.getName(),
            Song.Column.TRACK.getName(),
            Song.Column.DATA.getName()
    };

    /**
     * コンストラクタ.
     */
    @Inject
    public NowPlayingListRepositoryImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized AppMusicCursorLoader get(@Nullable AppMusicPlaylistCursor cursor) {
        return createNowPlayingListCursorLoader(this, cursor);
    }

    /**
     * SettingListInfoCursorLoader生成.
     * <p>
     * UnitTest用
     */
    @VisibleForTesting
    NowPlayingListCursorLoader createNowPlayingListCursorLoader(NowPlayingListRepositoryImpl repository,
                                                                AppMusicPlaylistCursor cursor) {
        return new NowPlayingListCursorLoader(repository, cursor);
    }

    /**
     * 現在再生中のプレイリストを取得するAppMusicCursorLoader生成クラス.
     */
    static class NowPlayingListCursorLoader extends AppMusicCursorLoader implements NowPlayingListInfoCursor.Callback {
        private final ContentObserver mObserver;
        private NowPlayingListInfoCursor mNowPlayingListInfoCursor;
        private NowPlayingListRepositoryImpl mRepository;
        private EventBus mEventBus;
        private Handler mHandler;
        private CountDownLatch mSignal;

        /**
         * コンストラクタ.
         * <p>
         * {@code cursor}はローカルコンテンツ再生で使用されているため、
         *
         * @throws NullPointerException {@code controller}がnull
         */
        public NowPlayingListCursorLoader(NowPlayingListRepositoryImpl repository, AppMusicPlaylistCursor cursor) {
            super(repository.mContext);

            mRepository = repository;
            mObserver = createForceLoadContentObserver();
            mEventBus = mRepository.mEventBus;
            mHandler = mRepository.mHandler;

            mSignal = new CountDownLatch(1);
            mHandler.post(() -> {
                mNowPlayingListInfoCursor = createCursor(cursor);
                mNowPlayingListInfoCursor.registerContentObserver(mObserver);
                mSignal.countDown();
            });

            await();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Cursor loadInBackground() {
            return mNowPlayingListInfoCursor;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Bundle getExtras() {
            return Bundle.EMPTY;
        }

        /**
         * await.
         * <p>
         * UnitTest用
         * {@see Handler#post}が呼び出されないためCursor生成待ちを分ける
         */
        @VisibleForTesting
        void await(){
            try {
                mSignal.await();
            } catch (InterruptedException e) {
                Timber.d(e, "NowPlayingListCursorLoader() Interrupted");
            }
        }

        /**
         * ForceLoadContentObserver生成.
         * <p>
         * UnitTest用
         */
        @VisibleForTesting
        ContentObserver createForceLoadContentObserver() {
            return new ForceLoadContentObserver();
        }

        /**
         * Cursor生成.
         *
         * @param cursor プレイリストCursor
         * @return NowプレイングリストCursor
         */
        private NowPlayingListInfoCursor createCursor(@Nullable AppMusicPlaylistCursor cursor) {
            NowPlayingListInfoCursor playingListInfoCursor;
            if(cursor == null){
                playingListInfoCursor = new NowPlayingListInfoCursor(NOW_PLAYING_LIST_COLUMNS, 0, this, mEventBus);
            } else {
                playingListInfoCursor = new NowPlayingListInfoCursor(NOW_PLAYING_LIST_COLUMNS, cursor.getCount(), this, mEventBus);

                SmartPhoneRepeatMode repeatMode = cursor.getSmartPhoneRepeatMode();
                int position = cursor.getPosition();
                boolean isFirst = true;

                cursor.setRepeatMode(SmartPhoneRepeatMode.OFF);
                cursor.moveToPosition(-1);

                while (cursor.moveToNext()) {
                    playingListInfoCursor.addRow(new Object[]{
                            AppMusicContract.Song.getId(cursor),
                            AppMusicContract.Song.getTitle(cursor),
                            AppMusicContract.Song.getArtist(cursor),
                            AppMusicContract.Song.getAlbum(cursor),
                            AppMusicContract.Song.getAlbumId(cursor),
                            AppMusicContract.Song.getTrack(cursor),
                            AppMusicContract.Song.getData(cursor)
                    });
                }

                cursor.setRepeatMode(repeatMode);
                cursor.moveToPosition(position);

            }
            return playingListInfoCursor;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onUpdateCursor(@Nullable AppMusicPlaylistCursor cursor) {
            mNowPlayingListInfoCursor = createCursor(cursor);
            mObserver.onChange(false);
        }
    }

    /**
     * 現在再生中のプレイリストCursor.
     */
    static class NowPlayingListInfoCursor extends MatrixCursor {
        private EventBus mEventBus;
        private Callback mCallback;

        /**
         * コンストラクタ
         *
         * @param columnNames     列名群
         * @param initialCapacity 初期キャパシティ
         */
        public NowPlayingListInfoCursor(@NonNull String[] columnNames, int initialCapacity, Callback callback, EventBus eventBus) {
            super(columnNames, initialCapacity);
            mCallback = callback;
            mEventBus = eventBus;
            mEventBus.register(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() {
            super.close();
            mEventBus.unregister(this);
        }

        /**
         * NowPlayingList更新イベントハンドラ.
         *
         * @param event NowPlayingList更新イベント
         */
        @Subscribe
        public void onNowPlayingListUpdateEvent(NowPlayingListUpdateEvent event) {
            Timber.i("onNowPlayingListUpdateEvent()");
            mCallback.onUpdateCursor(event.mCursor);
        }

        /**
         * コールバック.
         * <p>
         * NowPlayingList更新時にCursorを更新するためのコールバック
         */
        interface Callback {

            /**
             * プレイリスト更新.
             *
             * @param cursor NowPlayingListに反映するプレイリストCursor
             */
            void onUpdateCursor(@Nullable AppMusicPlaylistCursor cursor);
        }
    }

    /**
     * NowPlayingList更新イベント.
     */
    public static class NowPlayingListUpdateEvent {
        /** プレイリストCursor. */
        public AppMusicPlaylistCursor mCursor;

        /**
         * コンストラクタ.
         *
         * @param cursor 更新されたプレイリストCursor
         */
        public NowPlayingListUpdateEvent(AppMusicPlaylistCursor cursor) {
            this.mCursor = cursor;
        }
    }
}
