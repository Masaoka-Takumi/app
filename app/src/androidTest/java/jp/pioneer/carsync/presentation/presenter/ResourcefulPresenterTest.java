package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.support.v4.content.CursorLoader;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;

import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.component.TextToSpeechController;
import jp.pioneer.carsync.domain.content.ContactsContract;
import jp.pioneer.carsync.domain.event.AdasWarningUpdateEvent;
import jp.pioneer.carsync.domain.event.AppMusicTrackChangeEvent;
import jp.pioneer.carsync.domain.event.AppStartCommandEvent;
import jp.pioneer.carsync.domain.event.ImpactEvent;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.event.ReadNotificationRemovedEvent;
import jp.pioneer.carsync.domain.event.ReverseStatusChangeEvent;
import jp.pioneer.carsync.domain.event.ShortcutKeyEvent;
import jp.pioneer.carsync.domain.event.SmartPhoneControlCommandEvent;
import jp.pioneer.carsync.domain.event.TransportStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.CheckAvailableTextToSpeech;
import jp.pioneer.carsync.domain.interactor.ControlImpactDetector;
import jp.pioneer.carsync.domain.interactor.GetReadNotificationList;
import jp.pioneer.carsync.domain.interactor.IsGrantReadNotification;
import jp.pioneer.carsync.domain.interactor.PrepareReadNotification;
import jp.pioneer.carsync.domain.interactor.QueryContact;
import jp.pioneer.carsync.domain.interactor.ReadNotification;
import jp.pioneer.carsync.domain.model.AdasWarningStatus;
import jp.pioneer.carsync.domain.model.AndroidMusicMediaInfo;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.LineNotification;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.Notification;
import jp.pioneer.carsync.domain.model.ParkingSensorSetting;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ReverseStatus;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.domain.model.SmartPhoneControlCommand;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TransportStatus;
import jp.pioneer.carsync.presentation.view.ResourcefulView;
import jp.pioneer.carsync.presentation.view.service.InitializeState;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ReadNotificationPresenterのテスト
 */

public class ResourcefulPresenterTest {

    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ResourcefulPresenter mPresenter = new ResourcefulPresenter();
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock AppSharedPreference mPreference;
    @Mock StatusHolder mStatusHolder;
    @Mock ResourcefulView mView;
    @Mock ControlImpactDetector mControlImpactDetector;
    @Mock IsGrantReadNotification mIsGrantCase;
    @Mock CheckAvailableTextToSpeech mCheckCase;
    @Mock GetReadNotificationList mNotificationCase;
    @Mock ReadNotification mReadCase;
    @Mock PrepareReadNotification mPrepareReadCase;
    @Mock QueryContact mContactCase;
    private static final int LOADER_ID_NUMBER = 1;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void testLifecycle() throws Exception {
        SmartPhoneStatus status = new SmartPhoneStatus();
        status.playbackMode = PlaybackMode.ERROR;

        when(mStatusHolder.getSmartPhoneStatus()).thenReturn(status);
        when(mStatusHolder.getTransportStatus()).thenReturn(TransportStatus.UNUSED);
        when(mPreference.isImpactDetectionEnabled()).thenReturn(false);
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);

        mPresenter.takeView(mView);
        mPresenter.resume();
        mPresenter.pause();
        mPresenter.dropView();

        verify(mReadCase).initialize(mPresenter);
        verify(mEventBus).register(mPresenter);
        verify(mEventBus).unregister(mPresenter);
        verify(mReadCase).terminate();
    }

    @Test
    public void testAlreadyRegister() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(true);

        mPresenter.onResume();

        verify(mEventBus, never()).register(mPresenter);
    }

    @Test
    public void testOnAppMusicPlaybackModeChangeEvent() throws Exception {
        SmartPhoneStatus status = new SmartPhoneStatus();
        status.playbackMode = PlaybackMode.PLAY;

        when(mStatusHolder.getSmartPhoneStatus()).thenReturn(status);
        when(mStatusHolder.getTransportStatus()).thenReturn(TransportStatus.UNUSED);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.onAppSharedPreferenceChanged(mPreference, "");

        verify(mView).startForeground("Playing Music");
    }

    @Test
    public void testOnShortcutAppKeyEvent() throws Exception {
        mPresenter.onShortcutKeyEvent(new ShortcutKeyEvent(ShortcutKey.APP));
        verify(mView).dispatchAppKey();
    }

    @Test
    public void testOnShortcutNaviKeyEvent() throws Exception {
        AppSharedPreference.Application naviApp = new AppSharedPreference.Application("TEST_PACKAGE", "TEST_LABEL");
        when(mPreference.getNavigationApp()).thenReturn(naviApp);

        mPresenter.onShortcutKeyEvent(new ShortcutKeyEvent(ShortcutKey.NAVI));
        verify(mView).dispatchNaviKey("TEST_PACKAGE");
    }

    @Test
    public void testOnShortcutMessageKeyEvent() throws Exception {
        mPresenter.onShortcutKeyEvent(new ShortcutKeyEvent(ShortcutKey.MESSAGE));
        verify(mView).dispatchMessageKey();
    }

    @Test
    public void testOnShortcutPhoneKeyEvent() throws Exception {
        when(mPreference.isPhoneBookAccessible()).thenReturn(true);
        mPresenter.onShortcutKeyEvent(new ShortcutKeyEvent(ShortcutKey.PHONE));
        verify(mView).dispatchPhoneKey();
    }

    @Test
    public void testOnShortcutSourceKeyEvent() throws Exception {
        mPresenter.onShortcutKeyEvent(new ShortcutKeyEvent(ShortcutKey.SOURCE));
    }

    @Test
    public void testOnShortcutVoiceKeyEvent() throws Exception {
        when(mPreference.isVoiceRecognitionEnabled()).thenReturn(true);
        mPresenter.onShortcutKeyEvent(new ShortcutKeyEvent(ShortcutKey.VOICE));
        verify(mView).dispatchVoiceKey();
    }

    @Test
    public void testOnSmartPhoneControlCommandAVEvent() throws Exception {
        mPresenter.onSmartPhoneControlCommandEvent(new SmartPhoneControlCommandEvent(SmartPhoneControlCommand.AV));
        verify(mView).dispatchAvKey();
    }

    @Test
    public void testOnSmartPhoneControlCommandDirectCallEvent() throws Exception {
        CursorLoader cursorLoader = mock(CursorLoader.class);
        when(mPreference.getDirectCallContactNumberId()).thenReturn((long)1);
        when(mContactCase.execute(ContactsContract.QueryParamsBuilder.createPhone(1))).thenReturn(cursorLoader);
        mPresenter.onSmartPhoneControlCommandEvent(new SmartPhoneControlCommandEvent(SmartPhoneControlCommand.DIRECT_CALL));
        verify(mContactCase).execute(ContactsContract.QueryParamsBuilder.createPhone(1));
        verify(cursorLoader).registerListener(LOADER_ID_NUMBER,mPresenter);
        verify(cursorLoader).startLoading();
    }

    @Test
    public void testOnImpactEvent() throws Exception {
        when(mPreference.isImpactDetectionEnabled()).thenReturn(true);

        mPresenter.onImpactEvent(new ImpactEvent());
        verify(mView).showAccidentDetect();
    }

    @Test
    public void testOnImpactEventDisable() throws Exception {
        when(mPreference.isImpactDetectionEnabled()).thenReturn(false);

        mPresenter.onImpactEvent(new ImpactEvent());
        verify(mView, never()).showAccidentDetect();
    }

    @Test
    public void testOnReverseStatusChangeEvent() throws Exception {
        ParkingSensorSetting parkingSensorSetting = mock(ParkingSensorSetting.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        parkingSensorSetting.parkingSensorSetting = true;
        status.reverseStatus = ReverseStatus.ON;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(status);
        when(mStatusHolder.getParkingSensorSetting()).thenReturn(parkingSensorSetting);
        mPresenter.onReverseStatusChangeEvent(new ReverseStatusChangeEvent());
        verify(mView).showParkingSensor();
    }

    @Test
    public void testOnReverseStatusChangeEventDisable() throws Exception {
        ParkingSensorSetting parkingSensorSetting = mock(ParkingSensorSetting.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        parkingSensorSetting.parkingSensorSetting = false;
        status.reverseStatus = ReverseStatus.OFF;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(status);
        when(mStatusHolder.getParkingSensorSetting()).thenReturn(parkingSensorSetting);
        mPresenter.onReverseStatusChangeEvent(new ReverseStatusChangeEvent());
        verify(mView, never()).showParkingSensor();
    }

    @Test
    public void testOnAdasWarningUpdateEvent() throws Exception {
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        when(mStatusHolder.getSmartPhoneStatus()).thenReturn(status);
        when(status.getAdasWarningStatus()).thenReturn(AdasWarningStatus.CONTINUOUS);
        mPresenter.onAdasWarningUpdateEvent(new AdasWarningUpdateEvent());
        verify(mView).showAdasWarning();
    }

    @Test
    public void testOnAppSharedPreferenceChanged() throws Exception {
        SmartPhoneStatus status = new SmartPhoneStatus();
        status.playbackMode = PlaybackMode.ERROR;

        when(mStatusHolder.getSmartPhoneStatus()).thenReturn(status);
        when(mStatusHolder.getTransportStatus()).thenReturn(TransportStatus.UNUSED);
        when(mPreference.isImpactDetectionEnabled()).thenReturn(true);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.onAppSharedPreferenceChanged(mPreference, "");

        verify(mView).startForeground("Detecting Impact");
    }

    @Test
    public void testOnInitializeSuccess() throws Exception {
        mPresenter.onInitializeSuccess();
        // no verify
    }

    @Test
    public void testOnInitializeSuccessWithPending() throws Exception {
        List<Notification> notifications = new ArrayList<>();
        Notification notification = new LineNotification();
        notifications.add(notification);

        when(mIsGrantCase.execute()).thenReturn(true);
        when(mNotificationCase.execute()).thenReturn(notifications);
        doAnswer(invocationOnMock -> {
            mPresenter.onInitializeSuccess();
            return null;
        }).when(mReadCase).initialize(mPresenter);

        mPresenter.setInitializeState(InitializeState.ERROR);
        mPresenter.onExistNotificationAction();

        verify(mReadCase).startReading(notification);
    }

    @Test
    public void testOnInitializeErrorAtFirst() throws Exception {
        mPresenter.onInitializeError(TextToSpeechController.Error.LANG_NOT_SUPPORTED);
        verify(mView, never()).showError(any(String.class));
    }

    @Test
    public void testOnInitializeError() throws Exception {
        mPresenter.setInitializeState(InitializeState.INITIALIZING);
        mPresenter.onInitializeError(TextToSpeechController.Error.LANG_NOT_SUPPORTED);
        verify(mView).showError(any(String.class));
    }

    @Test
    public void testOnSpeakStart() throws Exception {
        mPresenter.onSpeakStart();
            //verify(mView).showReadingNotification(null);
    }

    @Test
    public void testOnSpeakError() throws Exception {
        mPresenter.onSpeakError(TextToSpeechController.Error.LANG_MISSING_DATA);
        mPresenter.onSpeakError(TextToSpeechController.Error.LANG_NOT_SUPPORTED);
        mPresenter.onSpeakError(TextToSpeechController.Error.FAILURE);

        verify(mView).showError("LANG_MISSING_DATA");
        verify(mView).showError("LANG_NOT_SUPPORTED");
        verify(mView).showError("FAILURE");
    }

    @Test
    public void testOnAppMusicTrackChangeEvent() throws Exception {
        App mApp = mock(App.class);
        when(mApp.getApp(mContext)).thenReturn(mApp);
        when(mApp.getApp(mContext).isForeground()).thenReturn(false);
        CarDeviceMediaInfoHolder mediaHolder = new CarDeviceMediaInfoHolder();
        SmartPhoneStatus smartPhoneStatus = new SmartPhoneStatus();
        smartPhoneStatus.playbackMode = PlaybackMode.PLAY;
        when(mStatusHolder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(mStatusHolder.getSmartPhoneStatus()).thenReturn(smartPhoneStatus);

        AppMusicTrackChangeEvent event = new AppMusicTrackChangeEvent();
        mPresenter.onAppMusicTrackChangeEvent(event);
        verify(mView).showSongNotification(mediaHolder.androidMusicMediaInfo);
    }

    @Test
    public void testOnAppMusicTrackChangeEvent_NotPlay() throws Exception {
        App mApp = mock(App.class);
        when(mApp.getApp(mContext)).thenReturn(mApp);
        when(mApp.getApp(mContext).isForeground()).thenReturn(false);
        CarDeviceMediaInfoHolder mediaHolder = new CarDeviceMediaInfoHolder();
        SmartPhoneStatus smartPhoneStatus = new SmartPhoneStatus();
        smartPhoneStatus.playbackMode = PlaybackMode.PAUSE;
        when(mStatusHolder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(mStatusHolder.getSmartPhoneStatus()).thenReturn(smartPhoneStatus);

        AppMusicTrackChangeEvent event = new AppMusicTrackChangeEvent();
        mPresenter.onAppMusicTrackChangeEvent(event);
        verify(mView, never()).showSongNotification(any(AndroidMusicMediaInfo.class));
    }

    @Test
    public void testOnAppMusicTrackChangeEvent_Foreground() throws Exception {
        App mApp = mock(App.class);
        when(mApp.getApp(mContext)).thenReturn(mApp);
        when(mApp.getApp(mContext).isForeground()).thenReturn(true);

        AppMusicTrackChangeEvent event = new AppMusicTrackChangeEvent();
        mPresenter.onAppMusicTrackChangeEvent(event);

        verify(mView, never()).showSongNotification(any(AndroidMusicMediaInfo.class));
    }

    @Test
    public void testOnReadNotificationRemovedEvent() throws Exception {
        LineNotification notification = new LineNotification();
        ReadNotificationRemovedEvent event = new ReadNotificationRemovedEvent(notification);
        mPresenter.onReadNotificationRemovedEvent(event);
        // no verify
    }

    @Test
    public void testOnTransportStatusChangeEvent_BLUETOOTH_LISTENING() throws Exception {
        when(mStatusHolder.getTransportStatus()).thenReturn(TransportStatus.BLUETOOTH_LISTENING);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.onTransportStatusChangeEvent(new TransportStatusChangeEvent());

        verify(mView).startForeground("Waiting for Bluetooth Connection");
    }

    @Test
    public void testOnTransportStatusChangeEvent_BLUETOOTH_CONNECTING() throws Exception {
        when(mStatusHolder.getTransportStatus()).thenReturn(TransportStatus.BLUETOOTH_CONNECTING);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.onTransportStatusChangeEvent(new TransportStatusChangeEvent());

        verify(mView).startForeground("Connecting to Car Device via Bluetooth");
    }

    @Test
    public void testOnTransportStatusChangeEvent_USB_CONNECTING() throws Exception {
        when(mStatusHolder.getTransportStatus()).thenReturn(TransportStatus.USB_CONNECTING);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.onTransportStatusChangeEvent(new TransportStatusChangeEvent());

        verify(mView).startForeground("Connecting to Car Device via USB");
    }

    @Test
    public void testOnMessageKeyAction() throws Exception {
        CarDeviceStatus status = mock(CarDeviceStatus.class);

        status.sourceType = MediaSourceType.APP_MUSIC;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(status);
        mPresenter.onMessageKeyAction();
        verify(mPrepareReadCase).start();
    }

    @Test
    public void testOnMediaSourceTypeChangeAction() throws Exception {
        CarDeviceStatus status = mock(CarDeviceStatus.class);

        status.sourceType = MediaSourceType.APP_MUSIC;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(status);
        mPresenter.onMessageKeyAction();
        mPresenter.onMediaSourceTypeChangeAction(new MediaSourceTypeChangeEvent());
        verify(mPrepareReadCase,times(2)).start();
        status.sourceType = MediaSourceType.TTS;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(status);
        mPresenter.onMediaSourceTypeChangeAction(new MediaSourceTypeChangeEvent());
        status.sourceType = MediaSourceType.APP_MUSIC;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(status);
        mPresenter.onMediaSourceTypeChangeAction(new MediaSourceTypeChangeEvent());
    }

    @Test
    public void testOnExistNotificationAction() throws Exception {
        List<Notification> notificationList = new ArrayList<>();
        Notification notification = new LineNotification();
        notificationList.add(notification);

        when(mIsGrantCase.execute()).thenReturn(true);
        when(mNotificationCase.execute()).thenReturn(notificationList);
        doAnswer(invocationOnMock -> {
            mPresenter.onSpeakStart();
            Thread.sleep(1000);
            mPresenter.onSpeakDone();
            return null;
        }).when(mReadCase).startReading(notification);

        mPresenter.onExistNotificationAction();

        verify(mReadCase).startReading(notification);
        verify(mPrepareReadCase).finish();
    }

    @Test
    public void testOnExistNotificationActionSpeakError() throws Exception {
        List<Notification> notificationList = new ArrayList<>();
        Notification notification = new LineNotification();
        notificationList.add(notification);

        when(mIsGrantCase.execute()).thenReturn(true);
        when(mNotificationCase.execute()).thenReturn(notificationList);
        doAnswer(invocationOnMock -> {
            mPresenter.onSpeakStart();
            Thread.sleep(1000);
            mPresenter.onSpeakError(TextToSpeechController.Error.LANG_MISSING_DATA);
            return null;
        }).when(mReadCase).startReading(notification);

        mPresenter.onExistNotificationAction();
        verify(mView).showError("LANG_MISSING_DATA");
        verify(mReadCase).startReading(notification);
        verify(mPrepareReadCase).finish();

    }

    @Test
    public void testMultiOnExistNotificationAction() throws Exception {
        List<Notification> notificationList = new ArrayList<>();
        Notification notification = new LineNotification();
        notificationList.add(notification);
        Notification notification2 = new LineNotification();
        notificationList.add(notification2);

        when(mIsGrantCase.execute()).thenReturn(true);
        when(mNotificationCase.execute()).thenReturn(notificationList);
        doAnswer(invocationOnMock -> {
            mPresenter.onSpeakStart();
            Thread.sleep(1000);
            mPresenter.onSpeakDone();
            return null;
        }).when(mReadCase).startReading(notification);
        doAnswer(invocationOnMock -> {
            mPresenter.onSpeakStart();
            Thread.sleep(1000);
            mPresenter.onSpeakDone();
            return null;
        }).when(mReadCase).startReading(notification2);

        mPresenter.onExistNotificationAction();
        mPresenter.onExistNotificationAction();
        mPresenter.onExistNotificationAction();

        verify(mReadCase).startReading(notification);
        verify(mReadCase).startReading(notification2);
        verify(mReadCase).stopReading();
    }

    @Test
    public void testOnExistNotificationActionError() throws Exception {
        when(mIsGrantCase.execute()).thenReturn(false);

        mPresenter.onExistNotificationAction();

        verify(mView).showError(any(String.class));
    }

    @Test
    public void testOnExistNotificationActionError2() throws Exception {
        when(mIsGrantCase.execute()).thenReturn(true);

        mPresenter.setInitializeState(InitializeState.INITIALIZING);
        mPresenter.onExistNotificationAction();

        verify(mReadCase, never()).startReading(any(Notification.class));
    }

    @Test
    public void testOnAppStartCommandEvent() throws Exception {
        when(mPreference.isAgreedEulaPrivacyPolicy()).thenReturn(true);
        mPresenter.onAppStartCommandEvent(new AppStartCommandEvent("test"));
        verify(mView).startApplication("test");
    }

    @Test
    public void testOnAppStartCommandEvent_IsTermsOfUseUnRead() throws Exception {
        when(mPreference.isAgreedEulaPrivacyPolicy()).thenReturn(false);
        mPresenter.onAppStartCommandEvent(new AppStartCommandEvent("test"));
        verify(mView, never()).startApplication(anyString());
    }
}
