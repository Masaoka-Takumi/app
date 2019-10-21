package jp.pioneer.carsync.infrastructure.repository;

import android.app.Instrumentation;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.test.InstrumentationRegistry;
import android.support.v4.content.CursorLoader;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.domain.content.AppMusicContract;
import jp.pioneer.carsync.domain.model.SmartPhoneRepeatMode;
import jp.pioneer.carsync.infrastructure.database.AppMusicPlaylistCursor;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/21.
 */
public class NowPlayingListRepositoryImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks NowPlayingListRepositoryImpl mRepository = new NowPlayingListRepositoryImpl(){
        @Override
        NowPlayingListCursorLoader createNowPlayingListCursorLoader(NowPlayingListRepositoryImpl repository, AppMusicPlaylistCursor cursor) {
            return new NowPlayingListCursorLoader(repository, cursor){
                @Override
                ContentObserver createForceLoadContentObserver() {
                    return mContentObserver;
                }

                @Override
                void await() {
                }
            };
        }
    };
    @Mock EventBus mEventBus;
    @Mock Context mContext;
    @Mock Handler mHandler;

    @Mock AppMusicPlaylistCursor mAppMusicPlaylistCursor;
    @Mock AppMusicPlaylistCursor mAfterAppMusicPlaylistCursor;

    ContentObserver mContentObserver;
    CursorLoader mCursorLoader;

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);
    Instrumentation instr = InstrumentationRegistry.getInstrumentation();
    boolean isOnChangeCall;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mContentObserver  = new ContentObserver(mMainHandler) {
            @Override
            public boolean deliverSelfNotifications() {
                return super.deliverSelfNotifications();
            }

            @Override
            public void onChange(boolean selfChange) {
                if(!selfChange) {
                    isOnChangeCall = true;
                }
                super.onChange(selfChange);
                mSignal.countDown();
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
            }
        };

        when(mHandler.sendMessageAtTime(any(Message.class), anyLong())).then(invocationOnMock -> {
            Message message = (Message) invocationOnMock.getArguments()[0];
            mMainHandler.post(() -> {
                message.getCallback().run();
                mSignal.countDown();
            });
            return true;
        });

        // Before
        when(mAppMusicPlaylistCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ID.getName())).thenReturn(0);
        when(mAppMusicPlaylistCursor.getLong(0))
                .thenReturn(1L).thenReturn(2L).thenReturn(3L).thenReturn(4L);
        when(mAppMusicPlaylistCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.TITLE.getName())).thenReturn(1);
        when(mAppMusicPlaylistCursor.getString(1))
                .thenReturn("TEST_TITLE_1").thenReturn("TEST_TITLE_2").thenReturn("TEST_TITLE_3").thenReturn("TEST_TITLE_4");
        when(mAppMusicPlaylistCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ARTIST.getName())).thenReturn(2);
        when(mAppMusicPlaylistCursor.getString(2))
                .thenReturn("TEST_ARTIST_1").thenReturn("TEST_ARTIST_2").thenReturn("TEST_ARTIST_3").thenReturn("TEST_ARTIST_4");
        when(mAppMusicPlaylistCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ALBUM.getName())).thenReturn(3);
        when(mAppMusicPlaylistCursor.getString(3))
                .thenReturn("TEST_ALBUM_1").thenReturn("TEST_ALBUM_2").thenReturn("TEST_ALBUM_3").thenReturn("TEST_ALBUM_4");
        when(mAppMusicPlaylistCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ALBUM_ID.getName())).thenReturn(4);
        when(mAppMusicPlaylistCursor.getLong(4))
                .thenReturn(11L).thenReturn(12L).thenReturn(13L).thenReturn(14L);
        when(mAppMusicPlaylistCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.TRACK.getName())).thenReturn(5);
        when(mAppMusicPlaylistCursor.getInt(5))
                .thenReturn(21).thenReturn(22).thenReturn(23).thenReturn(24);
        when(mAppMusicPlaylistCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.DATA.getName())).thenReturn(6);
        when(mAppMusicPlaylistCursor.getString(6))
                .thenReturn("TEST_DATA_1").thenReturn("TEST_DATA_2").thenReturn("TEST_DATA_3").thenReturn("TEST_DATA_4");
        when(mAppMusicPlaylistCursor.moveToPosition(-1)).thenReturn(true);
        when(mAppMusicPlaylistCursor.moveToNext())
                .thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mAppMusicPlaylistCursor.getSmartPhoneRepeatMode()).thenReturn(SmartPhoneRepeatMode.ALL);
        when(mAppMusicPlaylistCursor.getPosition()).thenReturn(3);

        // After
        when(mAfterAppMusicPlaylistCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ID.getName())).thenReturn(0);
        when(mAfterAppMusicPlaylistCursor.getLong(0))
                .thenReturn(5L).thenReturn(6L);
        when(mAfterAppMusicPlaylistCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.TITLE.getName())).thenReturn(1);
        when(mAfterAppMusicPlaylistCursor.getString(1))
                .thenReturn("TEST_TITLE_5").thenReturn("TEST_TITLE_6");
        when(mAfterAppMusicPlaylistCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ARTIST.getName())).thenReturn(2);
        when(mAfterAppMusicPlaylistCursor.getString(2))
                .thenReturn("TEST_ARTIST_5").thenReturn("TEST_ARTIST_6");
        when(mAfterAppMusicPlaylistCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ALBUM.getName())).thenReturn(3);
        when(mAfterAppMusicPlaylistCursor.getString(3))
                .thenReturn("TEST_ALBUM_5").thenReturn("TEST_ALBUM_6");
        when(mAfterAppMusicPlaylistCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ALBUM_ID.getName())).thenReturn(4);
        when(mAfterAppMusicPlaylistCursor.getLong(4))
                .thenReturn(15L).thenReturn(16L);
        when(mAfterAppMusicPlaylistCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.TRACK.getName())).thenReturn(5);
        when(mAfterAppMusicPlaylistCursor.getInt(5))
                .thenReturn(25).thenReturn(26);
        when(mAfterAppMusicPlaylistCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.DATA.getName())).thenReturn(6);
        when(mAfterAppMusicPlaylistCursor.getString(6))
                .thenReturn("TEST_DATA_5").thenReturn("TEST_DATA_6");

        when(mAfterAppMusicPlaylistCursor.moveToPosition(-1)).thenReturn(true);
        when(mAfterAppMusicPlaylistCursor.moveToNext())
                .thenReturn(true).thenReturn(true).thenReturn(false);
        when(mAfterAppMusicPlaylistCursor.getSmartPhoneRepeatMode()).thenReturn(SmartPhoneRepeatMode.ONE);
        when(mAfterAppMusicPlaylistCursor.getPosition()).thenReturn(1);
    }

    @Test
    public void get() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        // exercise
        instr.runOnMainSync(() -> {
            mCursorLoader = mRepository.get(mAppMusicPlaylistCursor);
        });
        mSignal.await();
        Cursor cursor = mCursorLoader.loadInBackground();

        // verify
        assertThat(cursor.getCount(), is(4));
        while(cursor.moveToNext()){
            int position = cursor.getPosition() + 1;

            assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ID.getName())), is(((long) position)));
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.TITLE.getName())), is("TEST_TITLE_" + position));
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ARTIST.getName())), is("TEST_ARTIST_" + position));
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ALBUM.getName())), is("TEST_ALBUM_" + position));
            assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ALBUM_ID.getName())), is((long) position + 10L));
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.TRACK.getName())), is(position + 20));
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.DATA.getName())), is("TEST_DATA_" + position));
        }
        verify(mAppMusicPlaylistCursor).setRepeatMode(SmartPhoneRepeatMode.ALL);
        verify(mAppMusicPlaylistCursor).moveToPosition(3);
    }

    @Test
    public void get_ChangeList() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        // exercise
        instr.runOnMainSync(() -> {
            mCursorLoader = mRepository.get(mAppMusicPlaylistCursor);
        });
        mSignal.await();
        Cursor cursor = mCursorLoader.loadInBackground();
        mSignal = new CountDownLatch(1);
        ((NowPlayingListRepositoryImpl.NowPlayingListInfoCursor)cursor).onNowPlayingListUpdateEvent(new NowPlayingListRepositoryImpl.NowPlayingListUpdateEvent(mAfterAppMusicPlaylistCursor));
        mSignal.await();
        Cursor changeCursor = mCursorLoader.loadInBackground();

        // verify
        assertThat(changeCursor.getCount(), is(2));
        while(changeCursor.moveToNext()){
            int position = changeCursor.getPosition() + 5;

            assertThat(changeCursor.getLong(changeCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ID.getName())), is(((long) position)));
            assertThat(changeCursor.getString(changeCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.TITLE.getName())), is("TEST_TITLE_" + position));
            assertThat(changeCursor.getString(changeCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ARTIST.getName())), is("TEST_ARTIST_" + position));
            assertThat(changeCursor.getString(changeCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ALBUM.getName())), is("TEST_ALBUM_" + position));
            assertThat(changeCursor.getLong(changeCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ALBUM_ID.getName())), is((long) position + 10L));
            assertThat(changeCursor.getInt(changeCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.TRACK.getName())), is(position + 20));
            assertThat(changeCursor.getString(changeCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.DATA.getName())), is("TEST_DATA_" + position));
        }
        verify(mAfterAppMusicPlaylistCursor).setRepeatMode(SmartPhoneRepeatMode.ONE);
        verify(mAfterAppMusicPlaylistCursor).moveToPosition(1);
        assertThat(isOnChangeCall, is(true));
    }
}