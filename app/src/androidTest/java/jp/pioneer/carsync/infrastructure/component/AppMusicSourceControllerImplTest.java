package jp.pioneer.carsync.infrastructure.component;

import android.Manifest;
import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;

import org.greenrobot.eventbus.EventBus;
import org.junit.*;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.event.AppStateChangeEvent;
import jp.pioneer.carsync.domain.component.AppMusicSourceController;
import jp.pioneer.carsync.domain.content.AppMusicContract;
import jp.pioneer.carsync.domain.content.AppMusicCursorLoader;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.model.AndroidMusicMediaInfo;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ProtocolSpec;
import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.domain.model.SmartPhoneMediaInfoType;
import jp.pioneer.carsync.domain.model.SmartPhoneRepeatMode;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.entity.SmartPhoneMediaCommand;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSmartPhoneMediaCommandEvent;
import jp.pioneer.carsync.infrastructure.database.AppMusicPlaylistCursor;
import jp.pioneer.mle.pmg.player.PMGPlayer;
import jp.pioneer.mle.pmg.player.PMGPlayerListener;
import jp.pioneer.mle.pmg.player.data.PlayRange;
import jp.pioneer.mle.pmg.player.data.PlayerStatus;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.application.content.ProviderContract.Artwork.CONTENT_URI;
import static jp.pioneer.carsync.domain.content.AppMusicContract.QueryParamsBuilder.createAllSongs;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AppMusicSourceControllerImplのテストコード
 */
@SuppressWarnings({"unchecked", "ConstantConditions"})
@RunWith(Theories.class)
public class AppMusicSourceControllerImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks AppMusicSourceControllerImpl mAppMusicSourceController = new AppMusicSourceControllerImpl() {
        @Override
        File getDataDir() {
            return mFile;
        }

        @Nullable
        @Override
        Cursor createCursor(QueryParams params) {
            return isActiveMethod ? mActiveMethodCursor : mPlayMethodCursor;
        }

        @Override
        AppMusicPlaylistCursor createAppMusicPlaylistCursor(Cursor cursor, SmartPhoneRepeatMode repeatMode, ShuffleMode shuffleMode) {
            return isActiveMethod ? mActiveMethodCursor : mPlayMethodCursor;
        }

        @Override
        TrackPositionMonitor createTrackPositionMonitor() {
            return mTrackPositionMonitor;
        }

        TrackPositionMonitor mTrackPositionMonitor = new TrackPositionMonitor() {
            @Override
            void stop() {
            }

            @Override
            void start() {
                if (isPositionMonitorStart) {
                    mMainHandler.post(() -> {
                        mTrackPositionMonitor.run();
                        mSignal.countDown();
                    });
                }
            }
        };

//        @Override
//        SeekCalculator createSeekCalculator() {
//            return mSeekCalculator;
//        }
//
//        SeekCalculator mSeekCalculator = new SeekCalculator() {
//            @Override
//            long getSystemCurrentTimeMillis() {
//                return mSystemTimeMillis;
//            }
//        };
    };

    enum COLUMN_NAME {
        ID(1), TITLE(2), ARTIST(3), ALBUM_ID(4), ALBUM(5), TRACK(6), DATA(7), NAME(8);
        int code;

        COLUMN_NAME(int code) {
            this.code = code;
        }
    }

    @Mock App mApp;
    @Mock Context mContext;
    @Mock AppSharedPreference mPreference;
    @Mock AudioManager mAudioManager;
    @Mock TelephonyManager mTelephonyManager;
    @Mock PMGPlayer mPlayer;
    @Mock EventBus mEventBus;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock Handler mHandler;
    @Mock StatusHolder mStatusHolder;

    AppMusicPlaylistCursor mActiveMethodCursor;
    AppMusicPlaylistCursor mPlayMethodCursor;
    ApplicationInfo mApplicationInfo;
    File mFile;
    PlayRange mPlayRange;
    OutgoingPacket mOutgoingPacket;
    ProtocolSpec mProtocolSpec;
    ProtocolVersion mProtocolVersion;

    ShuffleMode mShuffleMode;
    SmartPhoneRepeatMode mSmartPhoneRepeatMode;
    QueryParams mQueryParams = createAllSongs();
    AppMusicContract.PlayParams mPlayParams;
    SmartPhoneStatus mSmartPhoneStatus;
    CarDeviceMediaInfoHolder mCarDeviceMediaInfoHolder;
    PMGPlayerListener mPMGPlayerListener;
    SourceControllerImpl mSourceController;
    PlayerStatus.PlayEndStatus mPlayEndStatus;

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);
    Instrumentation instr = InstrumentationRegistry.getInstrumentation();

    AppMusicCursorLoader mAppMusicCursorLoader;

    boolean isActiveMethod;
    boolean isPositionMonitorStart;
    boolean isTestFinish;
    long mSystemTimeMillis;

    @DataPoints
    public static final AppStateChangeEvent.AppState[] NOT_CHECK_PERMISSION_APP_STATES = {
            AppStateChangeEvent.AppState.PAUSED,
            AppStateChangeEvent.AppState.STARTED,
            AppStateChangeEvent.AppState.STOPPED
    };

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mActiveMethodCursor = mock(AppMusicPlaylistCursor.class);
        mPlayMethodCursor = mock(AppMusicPlaylistCursor.class);
        mFile = mock(File.class);
        mPlayRange = mock(PlayRange.class);
        mOutgoingPacket = mock(OutgoingPacket.class);
        mProtocolSpec = mock(ProtocolSpec.class);
        mProtocolVersion = mock(ProtocolVersion.class);
        mApplicationInfo = mock(ApplicationInfo.class);
        mApplicationInfo.dataDir = "TEST";

        isActiveMethod = true;
        isPositionMonitorStart = false;
        isTestFinish = false;

        mShuffleMode = ShuffleMode.ON;
        mSmartPhoneRepeatMode = SmartPhoneRepeatMode.ONE;
        mQueryParams = createAllSongs();
        mPlayParams = AppMusicContract.PlayParams.createPlayParams(mQueryParams, 3L);
        mSmartPhoneStatus = new SmartPhoneStatus();
        mCarDeviceMediaInfoHolder = new CarDeviceMediaInfoHolder();
        mPMGPlayerListener = mAppMusicSourceController;
        mSourceController = mAppMusicSourceController;

        when(mHandler.sendMessageAtTime(any(Message.class), anyLong())).then(invocationOnMock -> {
            Message message = (Message) invocationOnMock.getArguments()[0];
            mMainHandler.post(() -> {
                message.getCallback().run();
                mSignal.countDown();
            });
            return true;
        });

        /*Happy Path Setting*/
        when(mActiveMethodCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ID.getName())).thenReturn(COLUMN_NAME.ID.code);
        when(mActiveMethodCursor.getLong(COLUMN_NAME.ID.code)).thenReturn(1L).thenReturn(2L).thenReturn(1L).thenReturn(2L);
        when(mActiveMethodCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.TITLE.getName())).thenReturn(COLUMN_NAME.TITLE.code);
        when(mActiveMethodCursor.getString(COLUMN_NAME.TITLE.code)).thenReturn("TEST_TITLE_1").thenReturn("TEST_TITLE_2").thenReturn("TEST_TITLE_1").thenReturn("TEST_TITLE_2");
        when(mActiveMethodCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ARTIST.getName())).thenReturn(COLUMN_NAME.ARTIST.code);
        when(mActiveMethodCursor.getString(COLUMN_NAME.ARTIST.code)).thenReturn("TEST_ARTIST_1").thenReturn("TEST_ARTIST_2").thenReturn("TEST_ARTIST_1").thenReturn("TEST_ARTIST_2");
        when(mActiveMethodCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ALBUM_ID.getName())).thenReturn(COLUMN_NAME.ALBUM_ID.code);
        when(mActiveMethodCursor.getLong(COLUMN_NAME.ALBUM_ID.code)).thenReturn(11L).thenReturn(12L).thenReturn(11L).thenReturn(12L);
        when(mActiveMethodCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ALBUM.getName())).thenReturn(COLUMN_NAME.ALBUM.code);
        when(mActiveMethodCursor.getString(COLUMN_NAME.ALBUM.code)).thenReturn("TEST_ALBUM_1").thenReturn("TEST_ALBUM_2").thenReturn("TEST_ALBUM_1").thenReturn("TEST_ALBUM_2");
        when(mActiveMethodCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.TRACK.getName())).thenReturn(COLUMN_NAME.TRACK.code);
        when(mActiveMethodCursor.getInt(COLUMN_NAME.TRACK.code)).thenReturn(21).thenReturn(22).thenReturn(21).thenReturn(22);
        when(mActiveMethodCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.DATA.getName())).thenReturn(COLUMN_NAME.DATA.code);
        when(mActiveMethodCursor.getString(COLUMN_NAME.DATA.code)).thenReturn("TEST_DATA_1").thenReturn("TEST_DATA_2").thenReturn("TEST_DATA_1").thenReturn("TEST_DATA_2");
        when(mActiveMethodCursor.getColumnIndexOrThrow(AppMusicContract.Genre.Column.NAME.getName())).thenReturn(COLUMN_NAME.NAME.code);
        when(mActiveMethodCursor.getString(COLUMN_NAME.NAME.code)).thenReturn("TEST_GENRE_1").thenReturn("TEST_GENRE_2").thenReturn("TEST_GENRE_1").thenReturn("TEST_GENRE_2");
        when(mActiveMethodCursor.getCount()).thenReturn(1);
        when(mActiveMethodCursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mActiveMethodCursor.getPosition()).thenReturn(1).thenReturn(2);

        when(mPlayMethodCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ID.getName())).thenReturn(COLUMN_NAME.ID.code);
        when(mPlayMethodCursor.getLong(COLUMN_NAME.ID.code)).thenReturn(31L).thenReturn(32L);
        when(mPlayMethodCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.TITLE.getName())).thenReturn(COLUMN_NAME.TITLE.code);
        when(mPlayMethodCursor.getString(COLUMN_NAME.TITLE.code)).thenReturn("TEST_TITLE_11").thenReturn("TEST_TITLE_12");
        when(mPlayMethodCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ARTIST.getName())).thenReturn(COLUMN_NAME.ARTIST.code);
        when(mPlayMethodCursor.getString(COLUMN_NAME.ARTIST.code)).thenReturn("TEST_ARTIST_11").thenReturn("TEST_ARTIST_12");
        when(mPlayMethodCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ALBUM_ID.getName())).thenReturn(COLUMN_NAME.ALBUM_ID.code);
        when(mPlayMethodCursor.getLong(COLUMN_NAME.ALBUM_ID.code)).thenReturn(41L).thenReturn(42L);
        when(mPlayMethodCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ALBUM.getName())).thenReturn(COLUMN_NAME.ALBUM.code);
        when(mPlayMethodCursor.getString(COLUMN_NAME.ALBUM.code)).thenReturn("TEST_ALBUM_11").thenReturn("TEST_ALBUM_12");
        when(mPlayMethodCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.TRACK.getName())).thenReturn(COLUMN_NAME.TRACK.code);
        when(mPlayMethodCursor.getInt(COLUMN_NAME.TRACK.code)).thenReturn(51).thenReturn(52);
        when(mPlayMethodCursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.DATA.getName())).thenReturn(COLUMN_NAME.DATA.code);
        when(mPlayMethodCursor.getString(COLUMN_NAME.DATA.code)).thenReturn("TEST_DATA_11").thenReturn("TEST_DATA_12");
        when(mPlayMethodCursor.getColumnIndexOrThrow(AppMusicContract.Genre.Column.NAME.getName())).thenReturn(COLUMN_NAME.NAME.code);
        when(mPlayMethodCursor.getString(COLUMN_NAME.NAME.code)).thenReturn("TEST_GENRE_11").thenReturn("TEST_GENRE_12");
        when(mPlayMethodCursor.getCount()).thenReturn(1);
        when(mPlayMethodCursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mPlayMethodCursor.getPosition()).thenReturn(1).thenReturn(2);

        when(mPlayer.open(anyInt(), anyInt(), anyString(), anyInt(), anyString())).thenReturn(PMGPlayer.ResultCode.SUCCESS);
        when(mPlayer.clearPlaylist()).thenReturn(PMGPlayer.ResultCode.SUCCESS);
        when(mPlayer.trackUp()).thenReturn(PMGPlayer.ResultCode.SUCCESS);
        when(mPlayer.trackDown()).thenReturn(PMGPlayer.ResultCode.SUCCESS);
        when(mPlayer.addMusicToPlaylist(any(ArrayList.class))).thenReturn(PMGPlayer.ResultCode.SUCCESS);
        when(mPlayer.seek(anyInt())).thenReturn(PMGPlayer.ResultCode.SUCCESS);
        when(mPlayer.pause()).thenReturn(PMGPlayer.ResultCode.SUCCESS);
        when(mPlayer.play()).thenReturn(PMGPlayer.ResultCode.SUCCESS);
        when(mPlayer.selectTrackNo(anyInt())).thenReturn(PMGPlayer.ResultCode.SUCCESS);
        when(mPlayer.setRepeatMode(any(PlayerStatus.RepeatMode.class))).thenReturn(PMGPlayer.ResultCode.SUCCESS);
        when(mPlayer.registerMusicData(anyString(), any(ArrayList.class))).thenReturn(1).thenReturn(2).thenReturn(3).thenReturn(4);
        when(mPlayer.getCurrentMusicPlayRange()).thenReturn(mPlayRange);
        when(mPlayer.getCurrentPosition()).thenReturn(1000);

        when(mFile.exists()).thenReturn(true);
        when(mFile.mkdir()).thenReturn(true);

        when(mPreference.getAppMusicShuffleMode()).thenReturn(mShuffleMode);
        when(mPreference.getAppMusicRepeatMode()).thenReturn(mSmartPhoneRepeatMode);
        when(mPreference.getAppMusicAudioPlayPosition()).thenReturn(1000);
        when(mPreference.getAppMusicQueryParams()).thenReturn(mQueryParams);
        when(mPreference.getAppMusicAudioId()).thenReturn(2L);

        when(mStatusHolder.getCarDeviceMediaInfoHolder()).thenReturn(mCarDeviceMediaInfoHolder);
        when(mStatusHolder.getSmartPhoneStatus()).thenReturn(mSmartPhoneStatus);
        when(mStatusHolder.getProtocolSpec()).thenReturn(mProtocolSpec);

        when(mPlayRange.getInPoint()).thenReturn(100L);
        when(mPlayRange.getOutPoint()).thenReturn(1000L);

        when(mContext.getApplicationInfo()).thenReturn(mApplicationInfo);
        when(mAudioManager.requestAudioFocus(mAppMusicSourceController, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)).thenReturn(AudioManager.AUDIOFOCUS_REQUEST_GRANTED);
        when(mProtocolSpec.getConnectingProtocolVersion()).thenReturn(mProtocolVersion);
        when(mTelephonyManager.getCallState()).thenReturn(TelephonyManager.CALL_STATE_IDLE);
    }

    @After
    public void teardown() throws Exception {
        isTestFinish = true;
        mSourceController.inactive();
    }

    @Test
    public void onActive() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        /*比較用Music情報生成*/
        AndroidMusicMediaInfo androidMusicMediaInfo = mCarDeviceMediaInfoHolder.androidMusicMediaInfo;
        androidMusicMediaInfo.songTitle = "TEST_TITLE_1";
        androidMusicMediaInfo.artistName = "TEST_ARTIST_1";
        androidMusicMediaInfo.albumTitle = "TEST_ALBUM_1";
        androidMusicMediaInfo.trackNumber = 21;
        androidMusicMediaInfo.mediaId = 1;
        androidMusicMediaInfo.genre = "TEST_GENRE_1";
        androidMusicMediaInfo.artworkImageLocation = Uri.parse(CONTENT_URI + "/songs/" + 11L);
        PlayRange playRange = mPlayer.getCurrentMusicPlayRange();
        int musicDuration = (int) (playRange.getOutPoint() - playRange.getInPoint());
        androidMusicMediaInfo.durationInSec = musicDuration / 1000;
        androidMusicMediaInfo.positionInSec = mPlayer.getCurrentPosition() / 1000;

        when(mPacketBuilder.createSmartPhoneAudioInfoNotification(any(AndroidMusicMediaInfo.class), any(SmartPhoneMediaInfoType.class), any(CarDeviceSpec.class))).then(invocationOnMock -> {
            AndroidMusicMediaInfo info = invocationOnMock.getArgument(0);
            assertThat(androidMusicMediaInfo, is(info));
            return mOutgoingPacket;
        });

        // exercise
        mSourceController.active();

        // verify
        verify(mPlayer).open(44100, 2, "TEST/databases/music.db", 1, "TEST/lib");
        verify(mPlayer).addListener(mAppMusicSourceController);
        verify(mPlayer, times(1)).registerMusicData(eq("TEST_DATA_1"), any(ArrayList.class));
        verify(mPlayer).clearPlaylist();
        verify(mPlayer).addMusicToPlaylist(new ArrayList<>(Collections.singletonList(1)));
        verify(mPlayer).seek(1000);
        verify(mPlayer, never()).startPostSpectrumAnalyze(4096, 31, false, 90);
        verify(mEventBus).register(mAppMusicSourceController);
    }

    @Test
    public void onInactive() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        /*togglePlay(ToPlay)カウントダウン*/
        when(mPlayer.play()).then(invocationOnMock -> {
            mMainHandler.post(() -> {
                mPMGPlayerListener.onStartPlayMusic(1);
                mSignal.countDown();
            });
            return PMGPlayer.ResultCode.SUCCESS;
        });

        when(mPreference.setAppMusicQueryParams(any(QueryParams.class))).then(invocationOnMock -> {
            QueryParams params = invocationOnMock.getArgument(0);
            assertThat(params, is(mQueryParams));
            return mPreference;
        });
        when(mPreference.setAppMusicAudioPlayPosition(anyInt())).then(invocationOnMock -> {
            int position = invocationOnMock.getArgument(0);
            assertThat(position, is(1000));
            return mPreference;
        });
        when(mPreference.setAppMusicAudioId(anyLong())).then(invocationOnMock -> {
            long id = invocationOnMock.getArgument(0);
            assertThat(id, is(2L));
            return mPreference;
        });

        mSourceController.active();
        mAppMusicSourceController.togglePlay();
        mSignal.await();

        // exercise
        mSourceController.inactive();

        // verify
        verify(mPlayer, never()).stopPostSpectrumAnalyze();
        verify(mEventBus).unregister(mAppMusicSourceController);
    }

    @Test
    public void play_PositionChange() throws Exception {
        // setup
        mSignal = new CountDownLatch(3);
        mSourceController.active();

        when(mPlayer.play()).then(invocationOnMock -> {
            mMainHandler.post(() -> {
                mPMGPlayerListener.onStartPlayMusic(1);
                mSignal.countDown();
            });
            return PMGPlayer.ResultCode.SUCCESS;
        });

        isActiveMethod = false;
        isPositionMonitorStart = true;
        when(mPlayer.getCurrentPosition()).thenReturn(3000);

        AndroidMusicMediaInfo androidMusicMediaInfo = mCarDeviceMediaInfoHolder.androidMusicMediaInfo;
        androidMusicMediaInfo.positionInSec = 3000 / 1000;

        when(mPacketBuilder.createSmartPhoneAudioInfoNotification(any(AndroidMusicMediaInfo.class), any(SmartPhoneMediaInfoType.class), any(CarDeviceSpec.class))).then(invocationOnMock -> {
            AndroidMusicMediaInfo info = invocationOnMock.getArgument(0);
            assertThat(androidMusicMediaInfo, is(info));
            return mOutgoingPacket;
        });

        //exercise
        mAppMusicSourceController.play(mPlayParams);
        mSignal.await();

        //verify
        verify(mPlayer, times(1)).registerMusicData(eq("TEST_DATA_11"), any(ArrayList.class));
        verify(mPlayer, times(1)).registerMusicData(eq("TEST_DATA_1"), any(ArrayList.class));
        verify(mPlayer).addMusicToPlaylist(new ArrayList<>(Collections.singletonList(1)));
        verify(mPlayer).addMusicToPlaylist(new ArrayList<>(Collections.singletonList(2)));
        verify(mPlayer).play();
    }

    @Test
    public void play_PositionNotChange() throws Exception {
        // setup
        mSignal = new CountDownLatch(2);

        mSourceController.active();

        when(mPlayer.play()).then(invocationOnMock -> {
            mMainHandler.post(() -> {
                mPMGPlayerListener.onStartPlayMusic(1);
                mSignal.countDown();
            });
            return PMGPlayer.ResultCode.SUCCESS;
        });

        isActiveMethod = false;
        isPositionMonitorStart = true;

        when(mPlayer.getCurrentPosition()).thenReturn(0);

        //exercise
        mAppMusicSourceController.play(mPlayParams);
        mSignal.await();

        //verify
        verify(mPlayer, times(1)).registerMusicData(eq("TEST_DATA_11"), any(ArrayList.class));
        verify(mPlayer, times(1)).registerMusicData(eq("TEST_DATA_1"), any(ArrayList.class));
        verify(mPlayer).addMusicToPlaylist(new ArrayList<>(Collections.singletonList(1)));
        verify(mPlayer).addMusicToPlaylist(new ArrayList<>(Collections.singletonList(2)));
        verify(mPlayer).play();
        verify(mCarDeviceConnection, never()).sendPacket(any(OutgoingPacket.class));
    }

    @Test
    public void togglePlay_toPlay() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        when(mPlayer.play()).then(invocationOnMock -> {
            mMainHandler.post(() -> {
                mPMGPlayerListener.onStartPlayMusic(1);
                mSignal.countDown();
            });
            return PMGPlayer.ResultCode.SUCCESS;
        });

        when(mPacketBuilder.createSmartPhoneStatusNotification(any(ProtocolVersion.class), any(SmartPhoneStatus.class)))
                .then(invocationOnMock -> {
                    // onActiveのrestorePlayer()の流れで呼ばれる
                    if (!isTestFinish) {
                        ProtocolVersion version = invocationOnMock.getArgument(0);
                        assertThat(version, is(mProtocolVersion));
                        SmartPhoneStatus status = invocationOnMock.getArgument(1);
                        assertThat(status.playbackMode, is(PlaybackMode.PAUSE));
                    }
                    return mOutgoingPacket;
                })
                .then(invocationOnMock -> {
                    // togglePlay()の流れで呼ばれる
                    if (!isTestFinish) {
                        ProtocolVersion version = invocationOnMock.getArgument(0);
                        assertThat(version, is(mProtocolVersion));
                        SmartPhoneStatus status = invocationOnMock.getArgument(1);
                        assertThat(status.playbackMode, is(PlaybackMode.PLAY));
                    }
                    return mOutgoingPacket;
                });

        // exercise
        mSourceController.active();
        mAppMusicSourceController.togglePlay();
        mSignal.await();

        // verify
        verify(mPlayer).play();

    }

    @Test
    public void togglePlay_toPause() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        /*togglePlay(ToPlay)カウントダウン*/
        when(mPlayer.play()).then(invocationOnMock -> {
            mMainHandler.post(() -> {
                mPMGPlayerListener.onStartPlayMusic(1);
                mSignal.countDown();
            });
            return PMGPlayer.ResultCode.SUCCESS;
        });

        mSourceController.active();
        mAppMusicSourceController.togglePlay();
        mSignal.await();

        when(mPacketBuilder.createSmartPhoneStatusNotification(any(ProtocolVersion.class), any(SmartPhoneStatus.class))).then(invocationOnMock -> {
            if (!isTestFinish) {
                ProtocolVersion version = invocationOnMock.getArgument(0);
                assertThat(version, is(mProtocolVersion));
                SmartPhoneStatus status = invocationOnMock.getArgument(1);
                assertThat(status.playbackMode, is(PlaybackMode.PAUSE));
            }
            return mOutgoingPacket;
        });

        when(mPreference.setAppMusicQueryParams(any(QueryParams.class))).then(invocationOnMock -> {
            QueryParams params = invocationOnMock.getArgument(0);
            assertThat(params, is(mQueryParams));
            return mPreference;
        });
        when(mPreference.setAppMusicAudioPlayPosition(anyInt())).then(invocationOnMock -> {
            int position = invocationOnMock.getArgument(0);
            assertThat(position, is(1000));
            return mPreference;
        });
        when(mPreference.setAppMusicAudioId(anyLong())).then(invocationOnMock -> {
            long id = invocationOnMock.getArgument(0);
            assertThat(id, is(2L));
            return mPreference;
        });

        // exercise
        mAppMusicSourceController.togglePlay();

        // verify
        verify(mPlayer).pause();
    }

    @Test
    public void toggleShuffleMode_toON() throws Exception {
        // setup
        mShuffleMode = ShuffleMode.OFF;
        when(mPreference.getAppMusicShuffleMode()).thenReturn(mShuffleMode);

        when(mPacketBuilder.createSmartPhoneStatusNotification(any(ProtocolVersion.class), any(SmartPhoneStatus.class)))
                .then(invocationOnMock -> {
                    // onActiveのrestorePlayer()の流れで呼ばれる
                    if (!isTestFinish) {
                        ProtocolVersion version = invocationOnMock.getArgument(0);
                        assertThat(version, is(mProtocolVersion));
                        SmartPhoneStatus status = invocationOnMock.getArgument(1);
                        assertThat(status.shuffleMode, is(ShuffleMode.OFF));
                    }
                    return mOutgoingPacket;
                })
                .then(invocationOnMock -> {
                    // onActiveのtoggleShuffleMode()の流れで呼ばれる
                    if (!isTestFinish) {
                        ProtocolVersion version = invocationOnMock.getArgument(0);
                        assertThat(version, is(mProtocolVersion));
                        SmartPhoneStatus status = invocationOnMock.getArgument(1);
                        assertThat(status.shuffleMode, is(ShuffleMode.ON));
                    }
                    return mOutgoingPacket;
                });


        mSourceController.active();

        // exercise
        mAppMusicSourceController.toggleShuffleMode();

        // verify
        verify(mPreference).setAppMusicShuffleMode(ShuffleMode.ON);
        verify(mActiveMethodCursor).setShuffleMode(ShuffleMode.ON);
    }

    @Test
    public void toggleShuffleMode_toOFF() throws Exception {
        // setup
        mShuffleMode = ShuffleMode.ON;
        when(mPreference.getAppMusicShuffleMode()).thenReturn(mShuffleMode);

        when(mPacketBuilder.createSmartPhoneStatusNotification(any(ProtocolVersion.class), any(SmartPhoneStatus.class))).then(invocationOnMock -> {
            if (!isTestFinish) {
                ProtocolVersion version = invocationOnMock.getArgument(0);
                assertThat(version, is(mProtocolVersion));
                SmartPhoneStatus status = invocationOnMock.getArgument(1);
                assertThat(status.shuffleMode, is(ShuffleMode.OFF));
            }
            return mOutgoingPacket;
        });

        mSourceController.active();

        // exercise
        mAppMusicSourceController.toggleShuffleMode();

        // verify
        verify(mPreference).setAppMusicShuffleMode(ShuffleMode.OFF);
        verify(mActiveMethodCursor).setShuffleMode(ShuffleMode.OFF);
    }

    @Test
    public void toggleRepeatMode_toALL() throws Exception {
        // setup
        mSmartPhoneRepeatMode = SmartPhoneRepeatMode.ONE;
        when(mPreference.getAppMusicRepeatMode()).thenReturn(mSmartPhoneRepeatMode);

        when(mPacketBuilder.createSmartPhoneStatusNotification(any(ProtocolVersion.class), any(SmartPhoneStatus.class))).then(invocationOnMock -> {
            if (!isTestFinish) {
                ProtocolVersion version = invocationOnMock.getArgument(0);
                assertThat(version, is(mProtocolVersion));
                SmartPhoneStatus status = invocationOnMock.getArgument(1);
                assertThat(status.repeatMode, is(SmartPhoneRepeatMode.ALL));
            }
            return mOutgoingPacket;
        });

        mSourceController.active();

        // exercise
        mAppMusicSourceController.toggleRepeatMode();

        // verify
        verify(mActiveMethodCursor).setRepeatMode(SmartPhoneRepeatMode.ALL);
        verify(mPreference).setAppMusicRepeatMode(SmartPhoneRepeatMode.ALL);
    }

    @Test
    public void toggleRepeatMode_toOFF() throws Exception {
        // setup
        mSmartPhoneRepeatMode = SmartPhoneRepeatMode.ALL;
        when(mPreference.getAppMusicRepeatMode()).thenReturn(mSmartPhoneRepeatMode);

        when(mPacketBuilder.createSmartPhoneStatusNotification(any(ProtocolVersion.class), any(SmartPhoneStatus.class)))
                .then(invocationOnMock -> {
                    // onActiveのrestorePlayer()の流れで呼ばれる
                    if (!isTestFinish) {
                        ProtocolVersion version = invocationOnMock.getArgument(0);
                        assertThat(version, is(mProtocolVersion));
                        SmartPhoneStatus status = invocationOnMock.getArgument(1);
                        assertThat(status.repeatMode, is(SmartPhoneRepeatMode.ALL));
                    }
                    return mOutgoingPacket;
                })
                .then(invocationOnMock -> {
                    // toggleRepeatMode()の流れで呼ばれる
                    if (!isTestFinish) {
                        ProtocolVersion version = invocationOnMock.getArgument(0);
                        assertThat(version, is(mProtocolVersion));
                        SmartPhoneStatus status = invocationOnMock.getArgument(1);
                        assertThat(status.repeatMode, is(SmartPhoneRepeatMode.OFF));
                    }
                    return mOutgoingPacket;
                });

        mSourceController.active();

        // exercise
        mAppMusicSourceController.toggleRepeatMode();

        // verify
        verify(mActiveMethodCursor).setRepeatMode(SmartPhoneRepeatMode.OFF);
        verify(mPreference).setAppMusicRepeatMode(SmartPhoneRepeatMode.OFF);
    }

    @Test
    public void toggleRepeatMode_toONE() throws Exception {
        // setup
        mSmartPhoneRepeatMode = SmartPhoneRepeatMode.OFF;
        mSmartPhoneStatus.repeatMode = SmartPhoneRepeatMode.OFF;
        when(mPreference.getAppMusicRepeatMode()).thenReturn(mSmartPhoneRepeatMode);

        when(mPacketBuilder.createSmartPhoneStatusNotification(any(ProtocolVersion.class), any(SmartPhoneStatus.class)))
                .then(invocationOnMock -> {
                    // onActiveのrestorePlayer()の流れで呼ばれる
                    if (!isTestFinish) {
                        ProtocolVersion version = invocationOnMock.getArgument(0);
                        assertThat(version, is(mProtocolVersion));
                        SmartPhoneStatus status = invocationOnMock.getArgument(1);
                        assertThat(status.repeatMode, is(SmartPhoneRepeatMode.OFF));
                    }
                    return mOutgoingPacket;
                })
                .then(invocationOnMock -> {
                    // toggleRepeatMode()の流れで呼ばれる
                    if (!isTestFinish) {
                        ProtocolVersion version = invocationOnMock.getArgument(0);
                        assertThat(version, is(mProtocolVersion));
                        SmartPhoneStatus status = invocationOnMock.getArgument(1);
                        assertThat(status.repeatMode, is(SmartPhoneRepeatMode.ONE));
                    }
                    return mOutgoingPacket;
                });

        mSourceController.active();

        // exercise
        mAppMusicSourceController.toggleRepeatMode();

        // verify
        verify(mActiveMethodCursor).setRepeatMode(SmartPhoneRepeatMode.ONE);
        verify(mPreference).setAppMusicRepeatMode(SmartPhoneRepeatMode.ONE);
    }

    @Test
    public void skipNextTrack() throws Exception {
        // exercise
        mSourceController.active();
        mAppMusicSourceController.skipNextTrack();

        // verify
        verify(mActiveMethodCursor).skipNext();
    }

    @Test
    public void skipPreviousTrack() throws Exception {
        // exercise
        mSourceController.active();
        mAppMusicSourceController.skipPreviousTrack();

        // verify
        verify(mActiveMethodCursor).skipPrevious();
    }

    @Test
    public void onAudioFocusChange_AUDIOFOCUS_LOSS() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        when(mPlayer.play()).then(invocationOnMock -> {
            mMainHandler.post(() -> {
                mPMGPlayerListener.onStartPlayMusic(1);
                mSignal.countDown();
            });
            return PMGPlayer.ResultCode.SUCCESS;
        });


        // exercise
        mSourceController.active();
        mAppMusicSourceController.togglePlay();
        mSignal.await();
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);

        // verify
        verify(mPlayer).pause();
        verify(mAudioManager, never()).abandonAudioFocus(mAppMusicSourceController);
    }

    @Test
    public void onAudioFocusChange_AUDIOFOCUS_GAIN_IsReturnToPlayModeTrue() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        when(mPlayer.play()).then(invocationOnMock -> {
            mMainHandler.post(() -> {
                mPMGPlayerListener.onStartPlayMusic(1);
                mSignal.countDown();
            });
            return PMGPlayer.ResultCode.SUCCESS;
        });

        // exercise
        mSourceController.active();
        mAppMusicSourceController.togglePlay();
        mSignal.await();
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);

        // verify
        verify(mPlayer, times(2)).play();
        verify(mPlayer).setGain(1, 1.0f);
    }

    @Test
    public void onAudioFocusChange_AUDIOFOCUS_GAIN_IsReturnToPlayModeFalse() throws Exception {
        // exercise
        mSourceController.active();
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);

        // verify
        verify(mPlayer, never()).play();
        verify(mPlayer).setGain(1, 1.0f);
    }

    @Test
    public void onAudioFocusChange_AUDIOFOCUS_LOSS_TRANSIENT() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        when(mPlayer.play()).then(invocationOnMock -> {
            mMainHandler.post(() -> {
                mPMGPlayerListener.onStartPlayMusic(1);
                mSignal.countDown();
            });
            return PMGPlayer.ResultCode.SUCCESS;
        });


        // exercise
        mSourceController.active();
        mAppMusicSourceController.togglePlay();
        mSignal.await();
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT);

        // verify
        verify(mPlayer).pause();
        verify(mAudioManager, never()).abandonAudioFocus(mAppMusicSourceController);
    }

    @Test
    public void onAudioFocusChange_AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK);

        // verify
        verify(mPlayer).setGain(1, 0.5F);
    }


    @Test
    public void onCrpSmartPhoneMediaCommandEvent_PlayPause_toPlay_AudioFocusGain() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);
        mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.PLAY_PAUSE));
        mSignal.await();
        // verify
        verify(mPlayer).play();
    }

    @Test
    public void onCrpSmartPhoneMediaCommandEvent_PlayPause_toPlay_AudioFocusLoss() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        ArgumentCaptor<KeyEvent> captor = ArgumentCaptor.forClass(KeyEvent.class);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
        mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.PLAY_PAUSE));
        mSignal.await();
        // verify
        verify(mAudioManager, times(2)).dispatchMediaKeyEvent(captor.capture());
        assertThat(captor.getValue().getKeyCode(), is(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
    }

    @Test
    public void onCrpSmartPhoneMediaCommandEvent_PlayPause_toPause_AudioFocusGain() throws Exception {
        // setup
        when(mPlayer.play()).then(invocationOnMock -> {
            mMainHandler.post(() -> {
                mPMGPlayerListener.onStartPlayMusic(1);
                mSignal.countDown();
            });
            return PMGPlayer.ResultCode.SUCCESS;
        });


        // exercise
        mSignal = new CountDownLatch(1);
        mSourceController.active();
        mAppMusicSourceController.togglePlay();
        mSignal.await();
        mSignal = new CountDownLatch(1);
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);
        mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.PLAY_PAUSE));
        mSignal.await();

        // verify
        verify(mPlayer).pause();
    }

    @Test
    public void onCrpSmartPhoneMediaCommandEvent_PlayPause_toPause_AudioFocusLoss() throws Exception {
        // setup
        ArgumentCaptor<KeyEvent> captor = ArgumentCaptor.forClass(KeyEvent.class);
        when(mPlayer.play()).then(invocationOnMock -> {
            mMainHandler.post(() -> {
                mPMGPlayerListener.onStartPlayMusic(1);
                mSignal.countDown();
            });
            return PMGPlayer.ResultCode.SUCCESS;
        });


        // exercise
        mSignal = new CountDownLatch(1);
        mSourceController.active();
        mAppMusicSourceController.togglePlay();
        mSignal.await();
        mSignal = new CountDownLatch(1);
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
        mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.PLAY_PAUSE));
        mSignal.await();

        // verify
        verify(mAudioManager, times(2)).dispatchMediaKeyEvent(captor.capture());
        assertThat(captor.getValue().getKeyCode(), is(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
    }

    @Test
    public void onCrpSmartPhoneMediaCommandEvent_PlayResume_StatusPause_AudioFocusGain() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);
        mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.PLAY_RESUME));
        mSignal.await();

        // verify
        verify(mPlayer).play();
    }

    @Test
    public void onCrpSmartPhoneMediaCommandEvent_PlayResume_StatusPause_AudioFocusLoss() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        ArgumentCaptor<KeyEvent> captor = ArgumentCaptor.forClass(KeyEvent.class);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
        mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.PLAY_RESUME));
        mSignal.await();

        // verify
        verify(mAudioManager, times(2)).dispatchMediaKeyEvent(captor.capture());
        assertThat(captor.getValue().getKeyCode(), is(KeyEvent.KEYCODE_MEDIA_PLAY));
    }

    @Test
    public void onCrpSmartPhoneMediaCommandEvent_PlayResume_StatusPlaying_AudioFocusGain() throws Exception {
        // setup
        when(mPlayer.play()).then(invocationOnMock -> {
            mMainHandler.post(() -> {
                mPMGPlayerListener.onStartPlayMusic(1);
                mSignal.countDown();
            });
            return PMGPlayer.ResultCode.SUCCESS;
        });


        // exercise
        mSignal = new CountDownLatch(1);
        mSourceController.active();
        mAppMusicSourceController.togglePlay();
        mSignal.await();
        mSignal = new CountDownLatch(1);
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);
        mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.PLAY_RESUME));
        mSignal.await();

        // verify
        verify(mPlayer, times(1)).play();
    }

    @Test
    public void onCrpSmartPhoneMediaCommandEvent_PlayResume_StatusPlaying_AudioFocusLoss() throws Exception {
        // setup
        ArgumentCaptor<KeyEvent> captor = ArgumentCaptor.forClass(KeyEvent.class);
        when(mPlayer.play()).then(invocationOnMock -> {
            mMainHandler.post(() -> {
                mPMGPlayerListener.onStartPlayMusic(1);
                mSignal.countDown();
            });
            return PMGPlayer.ResultCode.SUCCESS;
        });


        // exercise
        mSignal = new CountDownLatch(1);
        mSourceController.active();
        mAppMusicSourceController.togglePlay();
        mSignal.await();
        mSignal = new CountDownLatch(1);
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
        mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.PLAY_RESUME));
        mSignal.await();

        // verify
        verify(mAudioManager, times(2)).dispatchMediaKeyEvent(captor.capture());
        assertThat(captor.getValue().getKeyCode(), is(KeyEvent.KEYCODE_MEDIA_PLAY));
    }

    @Test
    public void onCrpSmartPhoneMediaCommandEvent_TrackUp_AudioFocusGain() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);
        mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.TRACK_UP));
        mSignal.await();

        // verify
        verify(mActiveMethodCursor).skipNext();
    }

    @Test
    public void onCrpSmartPhoneMediaCommandEvent_TrackUp_AudioFocusLoss() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        ArgumentCaptor<KeyEvent> captor = ArgumentCaptor.forClass(KeyEvent.class);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
        mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.TRACK_UP));
        mSignal.await();

        // verify
        verify(mAudioManager, times(2)).dispatchMediaKeyEvent(captor.capture());
        assertThat(captor.getValue().getKeyCode(), is(KeyEvent.KEYCODE_MEDIA_NEXT));
    }

    @Test
    public void onCrpSmartPhoneMediaCommandEvent_TrackDown_AudioFocusGain() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);
        mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.TRACK_DOWN));
        mSignal.await();

        // verify
        verify(mActiveMethodCursor).skipPrevious();
    }

    @Test
    public void onCrpSmartPhoneMediaCommandEvent_TrackDown_AudioFocusLoss() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        ArgumentCaptor<KeyEvent> captor = ArgumentCaptor.forClass(KeyEvent.class);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
        mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.TRACK_DOWN));
        mSignal.await();

        // verify
        verify(mAudioManager, times(2)).dispatchMediaKeyEvent(captor.capture());
        assertThat(captor.getValue().getKeyCode(), is(KeyEvent.KEYCODE_MEDIA_PREVIOUS));
    }

    static class SeekStepSystemTimePair {
        int seekStep;
        int systemTimeMillis;

        SeekStepSystemTimePair(int seekStep, int systemTimeMillis) {
            this.seekStep = seekStep;
            this.systemTimeMillis = systemTimeMillis;
        }
    }

    public static final SeekStepSystemTimePair[] SeekStepSystemTimePairs = new SeekStepSystemTimePair[]{
            new SeekStepSystemTimePair(1000, 100),
            new SeekStepSystemTimePair(1500, 100),
            new SeekStepSystemTimePair(2250, 100),
            new SeekStepSystemTimePair(3375, 100),
            new SeekStepSystemTimePair(5062, 100),
            new SeekStepSystemTimePair(7593, 100),
            new SeekStepSystemTimePair(10000, 100),
            new SeekStepSystemTimePair(10000, 100),
            new SeekStepSystemTimePair(1000, 10000),
            new SeekStepSystemTimePair(1500, 10000),
    };

    @Test
    public void onCrpSmartPhoneMediaCommandEvent_FastForward_AudioFocusGain() throws Exception {
        // setup
        isPositionMonitorStart = true;
        int currentPosition = 1000;
        PlayRange range = new PlayRange();
        range.setInPoint(0);
        range.setOutPoint(5000000);
        range.setMixPoint(5000000);
        when(mPlayer.getCurrentMusicPlayRange()).thenReturn(range);
        when(mPlayer.getCurrentPosition()).thenReturn(currentPosition);

        ArgumentCaptor<Integer> integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        when(mPlayer.seek(integerArgumentCaptor.capture())).thenReturn(PMGPlayer.ResultCode.SUCCESS);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);

        // verify
        for (SeekStepSystemTimePair pair : SeekStepSystemTimePairs) {
            mSignal = new CountDownLatch(2);
            mSystemTimeMillis = pair.systemTimeMillis;
            mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.FAST_FORWARD));
            mSignal.await();

            int position = currentPosition + pair.seekStep;
            position -= position % 1000;

            assertThat(integerArgumentCaptor.getValue(), is(position));
        }
    }

    @Test
    public void onCrpSmartPhoneMediaCommandEvent_FastForward_AudioFocusLoss() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        ArgumentCaptor<KeyEvent> captor = ArgumentCaptor.forClass(KeyEvent.class);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
        mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.FAST_FORWARD));
        mSignal.await();

        // verify
        verify(mAudioManager, times(2)).dispatchMediaKeyEvent(captor.capture());
        assertThat(captor.getValue().getKeyCode(), is(KeyEvent.KEYCODE_MEDIA_FAST_FORWARD));
    }

    @Test
    public void onCrpSmartPhoneMediaCommandEvent_Rewind_AudioFocusGain() throws Exception {
        // setup
        isPositionMonitorStart = true;
        int currentPosition = 2500000;
        when(mPlayer.getDuration(anyInt())).thenReturn(5000000);
        when(mPlayer.getCurrentPosition()).thenReturn(currentPosition);

        ArgumentCaptor<Integer> integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        when(mPlayer.seek(integerArgumentCaptor.capture())).thenReturn(PMGPlayer.ResultCode.SUCCESS);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);

        // verify
        for (SeekStepSystemTimePair pair : SeekStepSystemTimePairs) {
            mSignal = new CountDownLatch(2);
            mSystemTimeMillis = pair.systemTimeMillis;
            mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.REWIND));
            mSignal.await();

            int position = currentPosition - pair.seekStep;
            position -= position % 1000;

            assertThat(integerArgumentCaptor.getValue(), is(position));
        }
    }

    @Test
    public void onCrpSmartPhoneMediaCommandEvent_Rewind_AudioFocusLoss() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        ArgumentCaptor<KeyEvent> captor = ArgumentCaptor.forClass(KeyEvent.class);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.onAudioFocusChange(AudioManager.AUDIOFOCUS_LOSS);
        mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.REWIND));
        mSignal.await();

        // verify
        verify(mAudioManager, times(2)).dispatchMediaKeyEvent(captor.capture());
        assertThat(captor.getValue().getKeyCode(), is(KeyEvent.KEYCODE_MEDIA_REWIND));
    }

    @Test
    public void endPlaylist() throws Exception {
        // setup
        when(mPlayer.play()).then(invocationOnMock -> {
            mMainHandler.post(() -> {
                mPMGPlayerListener.onStartPlayMusic(1);
                mSignal.countDown();
            });
            return PMGPlayer.ResultCode.SUCCESS;
        });


        when(mActiveMethodCursor.skipNext()).then(invocationOnMock -> {
            mMainHandler.post(() -> {
                mPMGPlayerListener.onCompletePlayMusic(mPlayEndStatus, true, 0, 0, 0, -1);
                mSignal.countDown();
            });
            return true;
        }).thenReturn(false);

        ArgumentCaptor<ArrayList<Integer>> arrayListArgumentCaptor = ArgumentCaptor.forClass(ArrayList.class);
        when(mPlayer.addMusicToPlaylist(arrayListArgumentCaptor.capture())).thenReturn(PMGPlayer.ResultCode.SUCCESS);

        when(mPacketBuilder.createSmartPhoneStatusNotification(any(ProtocolVersion.class), any(SmartPhoneStatus.class)))
                .then(invocationOnMock -> {
                    // onActiveのrestorePlayer()の流れで呼ばれる
                    if (!isTestFinish) {
                        ProtocolVersion version = invocationOnMock.getArgument(0);
                        assertThat(version, is(mProtocolVersion));
                        SmartPhoneStatus status = invocationOnMock.getArgument(1);
                        assertThat(status.playbackMode, is(PlaybackMode.PAUSE));
                    }
                    return mOutgoingPacket;
                })
                .then(invocationOnMock -> {
                    // togglePlay()の流れで呼ばれる
                    if (!isTestFinish) {
                        ProtocolVersion version = invocationOnMock.getArgument(0);
                        assertThat(version, is(mProtocolVersion));
                        SmartPhoneStatus status = invocationOnMock.getArgument(1);
                        assertThat(status.playbackMode, is(PlaybackMode.PLAY));
                    }
                    return mOutgoingPacket;
                })
                .then(invocationOnMock -> {
                    // onCompletePlayMusicのsetStatus(PAUSED)の流れで呼ばれる
                    if (!isTestFinish) {
                        ProtocolVersion version = invocationOnMock.getArgument(0);
                        assertThat(version, is(mProtocolVersion));
                        SmartPhoneStatus status = invocationOnMock.getArgument(1);
                        assertThat(status.playbackMode, is(PlaybackMode.PAUSE));
                    }
                    return mOutgoingPacket;
                });

        when(mPreference.setAppMusicQueryParams(any(QueryParams.class))).then(invocationOnMock -> {
            QueryParams params = invocationOnMock.getArgument(0);
            assertThat(params, is(mQueryParams));
            return mPreference;
        });
        when(mPreference.setAppMusicAudioPlayPosition(anyInt())).then(invocationOnMock -> {
            int position = invocationOnMock.getArgument(0);
            assertThat(position, is(1000));
            return mPreference;
        });
        when(mPreference.setAppMusicAudioId(anyLong())).then(invocationOnMock -> {
            long id = invocationOnMock.getArgument(0);
            assertThat(id, is(2L));
            return mPreference;
        });


        //exercise
        mSignal = new CountDownLatch(1);
        mSourceController.active();
        mAppMusicSourceController.togglePlay();
        mSignal.await();
        mSignal = new CountDownLatch(2);
        mAppMusicSourceController.skipNextTrack();
        mSignal.await();

        //verify
        verify(mPlayer, times(2)).play();
        verify(mActiveMethodCursor).skipNext();
    }

    @Test
    public void onActive_ResultCode_FAILED() throws Exception {
        // setup
        when(mPlayer.open(anyInt(), anyInt(), anyString(), anyInt(), anyString())).thenReturn(PMGPlayer.ResultCode.FAILED);

        when(mPacketBuilder.createSmartPhoneStatusNotification(any(ProtocolVersion.class), any(SmartPhoneStatus.class))).then(invocationOnMock -> {
            if (!isTestFinish) {
                ProtocolVersion version = invocationOnMock.getArgument(0);
                assertThat(version, is(mProtocolVersion));
                SmartPhoneStatus status = invocationOnMock.getArgument(1);
                assertThat(status.playbackMode, is(PlaybackMode.ERROR));
            }
            return mOutgoingPacket;
        });

        // exercise
        mSourceController.active();

        // verify
        verify(mPlayer, never()).addListener(mAppMusicSourceController);
        verify(mPlayer, never()).registerMusicData(anyString(), any(ArrayList.class));
        verify(mPlayer, never()).registerMusicData(anyString(), any(ArrayList.class));
        verify(mPlayer, never()).clearPlaylist();
        verify(mPlayer, never()).addMusicToPlaylist(any(ArrayList.class));
        verify(mPlayer, never()).selectTrackNo(anyInt());
        verify(mPlayer, never()).seek(anyInt());
        verify(mEventBus, never()).register(any(Object.class));
    }

    @Test
    public void play_ResultCode_FAILED() throws Exception {
        // setup
        when(mPlayer.play()).thenReturn(PMGPlayer.ResultCode.FAILED);
        when(mPacketBuilder.createSmartPhoneStatusNotification(any(ProtocolVersion.class), any(SmartPhoneStatus.class)))
                .then(invocationOnMock -> {
                    // onActiveのrestorePlayer()の流れで呼ばれる
                    if (!isTestFinish) {
                        ProtocolVersion version = invocationOnMock.getArgument(0);
                        assertThat(version, is(mProtocolVersion));
                        SmartPhoneStatus status = invocationOnMock.getArgument(1);
                        assertThat(status.playbackMode, is(PlaybackMode.PAUSE));
                    }
                    return mOutgoingPacket;
                })
                .then(invocationOnMock -> {
                    // playのsetStatus(PREPARING)の流れで呼ばれる
                    if (!isTestFinish) {
                        ProtocolVersion version = invocationOnMock.getArgument(0);
                        assertThat(version, is(mProtocolVersion));
                        SmartPhoneStatus status = invocationOnMock.getArgument(1);
                        assertThat(status.playbackMode, is(PlaybackMode.STOP));
                    }
                    return mOutgoingPacket;
                })
                .then(invocationOnMock -> {
                    // playのpreparePlayer()の流れで呼ばれる
                    if (!isTestFinish) {
                        ProtocolVersion version = invocationOnMock.getArgument(0);
                        assertThat(version, is(mProtocolVersion));
                        SmartPhoneStatus status = invocationOnMock.getArgument(1);
                        assertThat(status.playbackMode, is(PlaybackMode.PAUSE));
                    }
                    return mOutgoingPacket;
                })
                .then(invocationOnMock -> {
                    // playの失敗の流れで呼ばれる
                    if (!isTestFinish) {
                        ProtocolVersion version = invocationOnMock.getArgument(0);
                        assertThat(version, is(mProtocolVersion));
                        SmartPhoneStatus status = invocationOnMock.getArgument(1);
                        assertThat(status.playbackMode, is(PlaybackMode.STOP));
                    }
                    return mOutgoingPacket;
                });

        //exercise
        mSourceController.active();
        isActiveMethod = false;
        mAppMusicSourceController.play(mPlayParams);

        //verify
        verify(mPlayer).deleteListener(mAppMusicSourceController);
        verify(mPlayer).close();
        verify(mPlayMethodCursor, times(2)).close();
    }

    @Test
    public void onCrpSmartPhoneMediaCommandEvent_PlayPause_isAndroidMusicInterrupted() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        when(mTelephonyManager.getCallState()).thenReturn(TelephonyManager.CALL_STATE_RINGING);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.PLAY_PAUSE));
        mSignal.await();
        // verify
        verify(mPlayer, never()).play();
    }

    @Test
    public void onCrpSmartPhoneMediaCommandEvent_PlayResume_isAndroidMusicInterrupted() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        when(mTelephonyManager.getCallState()).thenReturn(TelephonyManager.CALL_STATE_RINGING);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.PLAY_RESUME));
        mSignal.await();

        // verify
        verify(mPlayer, never()).play();
    }

    @Test
    public void onCrpSmartPhoneMediaCommandEvent_isInActive() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        // exercise
        mSourceController.active();
        mSourceController.inactive();
        mAppMusicSourceController.onCrpSmartPhoneMediaCommandEvent(new CrpSmartPhoneMediaCommandEvent(SmartPhoneMediaCommand.PLAY_RESUME));
        mSignal.await();

        // verify
        verify(mPlayer, never()).play();
    }

    @Test
    public void permissionDenied() throws Exception {
        // setup
        when(mContext.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, android.os.Process.myPid(), Process.myUid()))
                .thenReturn(PERMISSION_DENIED);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.togglePlay();

        // verify
        verify(mPacketBuilder, never()).createSmartPhoneStatusNotification(any(ProtocolVersion.class), any(SmartPhoneStatus.class));
    }

    @Test
    public void retryCheckPermission() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        when(mContext.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, android.os.Process.myPid(), Process.myUid()))
                .thenReturn(PERMISSION_DENIED)
                .thenReturn(PERMISSION_GRANTED);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.onAppStateChangedEvent(new AppStateChangeEvent(AppStateChangeEvent.AppState.RESUMED));
        mSignal.await();

        // verify
        verify(mPacketBuilder).createSmartPhoneStatusNotification(any(ProtocolVersion.class), any(SmartPhoneStatus.class));
    }

    @Theory
    public void notCheckPermissionAppState(AppStateChangeEvent.AppState state) throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        when(mContext.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, android.os.Process.myPid(), Process.myUid()))
                .thenReturn(PERMISSION_DENIED);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.onAppStateChangedEvent(new AppStateChangeEvent(state));

        // verify
        verify(mPacketBuilder, never()).createSmartPhoneStatusNotification(any(ProtocolVersion.class), any(SmartPhoneStatus.class));
    }

    @Test
    public void addSpeAnaDataListener_OnActive() throws Exception {
        // 本当は再生状態でないとスペアナのデータは通知されないが、テストとして必須ではないので省略する

        // setup
        mSignal = new CountDownLatch(1);
        float[] speAnaData = new float[31];
        for (int i = 0; i < speAnaData.length; i++) {
            speAnaData[i] = i * -0.5f;
        }
        when(mPlayer.startPostSpectrumAnalyze(4096, 31, false, 90))
                .then(invocationOnMock -> {
                    mMainHandler.post(() -> {
                        mPMGPlayerListener.onCompletePostSpeAna(speAnaData, speAnaData.length);
                        mSignal.countDown();
                    });
                    return PMGPlayer.ResultCode.SUCCESS;
                });
        AppMusicSourceController.OnSpeAnaDataListener listener1 = mock(AppMusicSourceController.OnSpeAnaDataListener.class);
        AppMusicSourceController.OnSpeAnaDataListener listener2 = mock(AppMusicSourceController.OnSpeAnaDataListener.class);

        // exercise
        mAppMusicSourceController.addSpeAnaDataListener(listener1);
        mAppMusicSourceController.addSpeAnaDataListener(listener2);
        mSourceController.active();
        mSignal.await();

        // verify
        verify(mPlayer).startPostSpectrumAnalyze(4096, 31, false, 90);
        verify(listener1).onSpeAnaData(aryEq(speAnaData));
        verify(listener2).onSpeAnaData(aryEq(speAnaData));
    }

    @Test
    public void addSpeAnaDataListener_Inactive() throws Exception {
        // setup
        AppMusicSourceController.OnSpeAnaDataListener listener1 = mock(AppMusicSourceController.OnSpeAnaDataListener.class);
        AppMusicSourceController.OnSpeAnaDataListener listener2 = mock(AppMusicSourceController.OnSpeAnaDataListener.class);

        // exercise
        mAppMusicSourceController.addSpeAnaDataListener(listener1);
        mAppMusicSourceController.addSpeAnaDataListener(listener2);

        // verify
        verify(mPlayer, never()).startPostSpectrumAnalyze(4096, 31, false, 90);
    }

    @Test
    public void addSpeAnaDataListener_ChangeActive() throws Exception {
        // setup
        when(mPlayer.startPostSpectrumAnalyze(4096, 31, false, 90)).thenReturn(PMGPlayer.ResultCode.SUCCESS);
        when(mPlayer.stopPostSpectrumAnalyze()).thenReturn(PMGPlayer.ResultCode.SUCCESS);
        AppMusicSourceController.OnSpeAnaDataListener listener1 = mock(AppMusicSourceController.OnSpeAnaDataListener.class);
        AppMusicSourceController.OnSpeAnaDataListener listener2 = mock(AppMusicSourceController.OnSpeAnaDataListener.class);

        // exercise
        mAppMusicSourceController.addSpeAnaDataListener(listener1);
        mAppMusicSourceController.addSpeAnaDataListener(listener2);
        mSourceController.active();
        mSourceController.inactive();
        mSourceController.active();
        mSourceController.inactive();

        // verify
        verify(mPlayer, times(2)).startPostSpectrumAnalyze(4096, 31, false, 90);
        verify(mPlayer, times(2)).stopPostSpectrumAnalyze();
    }

    @Test(expected = NullPointerException.class)
    public void addSpeAnaDataListener_ArgNull() throws Exception {
        // exercise
        mAppMusicSourceController.addSpeAnaDataListener(null);
    }

    @Test
    public void deleteSpeAnaDataListener_OnActive_AllDelete() throws Exception {
        // setup
        float[] speAnaData = new float[31];
        for (int i = 0; i < speAnaData.length; i++) {
            speAnaData[i] = i * -0.5f;
        }
        when(mPlayer.startPostSpectrumAnalyze(4096, 31, false, 90)).thenReturn(PMGPlayer.ResultCode.SUCCESS);
        when(mPlayer.stopPostSpectrumAnalyze()).thenReturn(PMGPlayer.ResultCode.SUCCESS);
        AppMusicSourceController.OnSpeAnaDataListener listener1 = mock(AppMusicSourceController.OnSpeAnaDataListener.class);
        AppMusicSourceController.OnSpeAnaDataListener listener2 = mock(AppMusicSourceController.OnSpeAnaDataListener.class);
        mSourceController.active();
        mAppMusicSourceController.addSpeAnaDataListener(listener1);
        mAppMusicSourceController.addSpeAnaDataListener(listener2);

        // exercise
        mAppMusicSourceController.deleteSpeAnaDataListener(listener1);
        mAppMusicSourceController.deleteSpeAnaDataListener(listener2);
        mPMGPlayerListener.onCompletePostSpeAna(speAnaData, speAnaData.length);

        // verify
        verify(mPlayer).startPostSpectrumAnalyze(4096, 31, false, 90);
        verify(mPlayer).stopPostSpectrumAnalyze();
        verify(listener1, never()).onSpeAnaData(any(float[].class));
        verify(listener2, never()).onSpeAnaData(any(float[].class));
    }

    @Test
    public void deleteSpeAnaDataListener_OnActive_RemainDelete() throws Exception {
        // setup
        float[] speAnaData = new float[31];
        for (int i = 0; i < speAnaData.length; i++) {
            speAnaData[i] = i * -0.5f;
        }
        when(mPlayer.startPostSpectrumAnalyze(4096, 31, false, 90)).thenReturn(PMGPlayer.ResultCode.SUCCESS);
        when(mPlayer.stopPostSpectrumAnalyze()).thenReturn(PMGPlayer.ResultCode.SUCCESS);
        AppMusicSourceController.OnSpeAnaDataListener listener1 = mock(AppMusicSourceController.OnSpeAnaDataListener.class);
        AppMusicSourceController.OnSpeAnaDataListener listener2 = mock(AppMusicSourceController.OnSpeAnaDataListener.class);
        mSourceController.active();
        mAppMusicSourceController.addSpeAnaDataListener(listener1);
        mAppMusicSourceController.addSpeAnaDataListener(listener2);

        // exercise
        mAppMusicSourceController.deleteSpeAnaDataListener(listener1);
        mPMGPlayerListener.onCompletePostSpeAna(speAnaData, speAnaData.length);

        // verify
        verify(mPlayer).startPostSpectrumAnalyze(4096, 31, false, 90);
        verify(mPlayer, never()).stopPostSpectrumAnalyze();
        verify(listener1, never()).onSpeAnaData(any(float[].class));
        verify(listener2).onSpeAnaData(aryEq(speAnaData));
    }

    @Test
    public void deleteSpeAnaDataListener_OnInactive() throws Exception {
        // setup
        when(mPlayer.startPostSpectrumAnalyze(4096, 31, false, 90)).thenReturn(PMGPlayer.ResultCode.SUCCESS);
        when(mPlayer.stopPostSpectrumAnalyze()).thenReturn(PMGPlayer.ResultCode.SUCCESS);
        AppMusicSourceController.OnSpeAnaDataListener listener1 = mock(AppMusicSourceController.OnSpeAnaDataListener.class);
        AppMusicSourceController.OnSpeAnaDataListener listener2 = mock(AppMusicSourceController.OnSpeAnaDataListener.class);

        // exercise
        mAppMusicSourceController.addSpeAnaDataListener(listener1);
        mAppMusicSourceController.addSpeAnaDataListener(listener2);
        mAppMusicSourceController.deleteSpeAnaDataListener(listener1);
        mAppMusicSourceController.deleteSpeAnaDataListener(listener2);

        // verify
        verify(mPlayer, never()).startPostSpectrumAnalyze(4096, 31, false, 90);
        verify(mPlayer, never()).stopPostSpectrumAnalyze();
    }

    @Test(expected = NullPointerException.class)
    public void deleteSpeAnaDataListener_ArgNull() {
        // exercise
        mAppMusicSourceController.deleteSpeAnaDataListener(null);
    }

    @Test
    public void createNowPlayingList() throws Exception {
        // setup
        mSourceController.active();

        int index = 0;
        String[] columns = new String[AppMusicContract.Song.Column.values().length];
        for(AppMusicContract.Song.Column column : AppMusicContract.Song.Column.values()){
            columns[index] = column.getName();
            ++index;
        }
        when(mActiveMethodCursor.moveToNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mActiveMethodCursor.getCount()).thenReturn(2);
        when(mActiveMethodCursor.getColumnNames()).thenReturn(columns);

        // exercise
//        instr.runOnMainSync(() ->
//                mAppMusicCursorLoader = mAppMusicSourceController.createNowPlayingList()
//        );
        Cursor cursor = mAppMusicCursorLoader.loadInBackground();
        Bundle bundle = mAppMusicCursorLoader.getExtras();

        // verify
        assertThat(cursor.getCount(), is(2));
        while(cursor.moveToNext()){
            int position = cursor.getPosition() + 1;

            assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ID.getName())), is(((long) position)));
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.TITLE.getName())), is("TEST_TITLE_" + position));
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ARTIST.getName())), is("TEST_ARTIST_" + position));
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ALBUM.getName())), is("TEST_ALBUM_" + position));
            assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.ALBUM_ID.getName())), is(((long) 10 + position)));
            assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.TRACK.getName())), is(((long) 20 + position)));
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow(AppMusicContract.Song.Column.DATA.getName())), is("TEST_DATA_" + position));
        }
        index = 0;
        for(int value : (int[])bundle.get(AppMusicCursorLoader.SECTION_INDEXES)){
            assertThat(value, is(index));
            ++index;
        }
        for(String value : (String[])bundle.get(AppMusicCursorLoader.SECTION_STRINGS)){
            assertThat(value, is("T"));
        }
    }

    @Test
    public void createNowPlayingList_NoData() throws Exception {
        // setup
        mSourceController.active();

        int index = 0;
        String[] columns = new String[AppMusicContract.Song.Column.values().length];
        for(AppMusicContract.Song.Column column : AppMusicContract.Song.Column.values()){
            columns[index] = column.getName();
            ++index;
        }
        when(mActiveMethodCursor.moveToNext()).thenReturn(false);
        when(mActiveMethodCursor.getCount()).thenReturn(0);
        when(mActiveMethodCursor.getColumnNames()).thenReturn(columns);

        // exercise
//        instr.runOnMainSync(() ->
//                mAppMusicCursorLoader = mAppMusicSourceController.createNowPlayingList()
//        );
        Cursor cursor = mAppMusicCursorLoader.loadInBackground();
        Bundle bundle = mAppMusicCursorLoader.getExtras();

        // verify
        assertThat(cursor.getCount(), is(0));
        assertThat(bundle.get(AppMusicCursorLoader.SECTION_INDEXES), is(nullValue()));
        assertThat(bundle.get(AppMusicCursorLoader.SECTION_STRINGS), is(nullValue()));
    }

    @Test
    public void selectTrack() throws Exception {
        // setup
        when(mActiveMethodCursor.selectTrack(0)).thenReturn(true);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.selectTrack(0);
    }

    @Test
    public void selectTrack_CursorIsNull() throws Exception {
        // setup
        when(mPlayer.addMusicToPlaylist(any(ArrayList.class))).thenReturn(PMGPlayer.ResultCode.FAILED);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.selectTrack(0);

        // verify
        verify(mActiveMethodCursor, never()).moveToPosition(anyInt());
    }

    @Test
    public void selectTrack_MoveToPositionFailed() throws Exception {
        // setup
        when(mActiveMethodCursor.selectTrack(0)).thenReturn(false);

        // exercise
        mSourceController.active();
        mAppMusicSourceController.selectTrack(0);

        // verify
        verify(mPlayer, never()).play();
    }

    @Test(expected = IllegalArgumentException.class)
    public void selectTrack_OverPlaylistCount() throws Exception {
        // exercise
        mSourceController.active();
        mAppMusicSourceController.selectTrack(100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void selectTrack_UnderZero() throws Exception {
        // exercise
        mSourceController.active();
        mAppMusicSourceController.selectTrack(-5);
    }
}
