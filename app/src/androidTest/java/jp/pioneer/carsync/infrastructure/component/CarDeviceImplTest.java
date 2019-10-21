package jp.pioneer.carsync.infrastructure.component;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Provider;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.component.SourceController;
import jp.pioneer.carsync.domain.event.ShortcutKeyEvent;
import jp.pioneer.carsync.domain.event.SmartPhoneControlCommandEvent;
import jp.pioneer.carsync.domain.model.AdasWarningStatus;
import jp.pioneer.carsync.domain.model.CarDeviceScreen;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.CustomFlashRequestType;
import jp.pioneer.carsync.domain.model.FlashPattern;
import jp.pioneer.carsync.domain.model.FlashPatternRegistrationType;
import jp.pioneer.carsync.domain.model.IlluminationSetting;
import jp.pioneer.carsync.domain.model.IlluminationSettingStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.ProtocolSpec;
import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.domain.model.ReadingRequestType;
import jp.pioneer.carsync.domain.model.RequestStatus;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.domain.model.SmartPhoneControlCommand;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TransitionDirection;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSendTaskFinishedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpStatusUpdateEvent;
import jp.pioneer.carsync.infrastructure.crp.task.SendTaskId;
import jp.pioneer.carsync.infrastructure.task.SetCustomFlashPatternTask;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.infrastructure.component.BroadcastReceiverImpl.ACTION_USB_ACCESSORY_PERMISSION;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/06/23.
 */
@RunWith(Theories.class)
public class CarDeviceImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks CarDeviceImpl mCarDevice;
    @Mock Handler mHandler;
    @Mock StatusHolder mStatusHolder;
    @Mock ExecutorService mTaskExecutor;
    @Mock Map<MediaSourceType, SourceController> mSourceControllers;
    @Mock NullSourceController mNullSourceController;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    @Mock EventBus mEventBus;
    @Mock OutgoingPacket mOutgoingPacket;
    @Mock AppSharedPreference mPreference;
    @Mock Provider<SetCustomFlashPatternTask> mSetCustomFlashPatternTaskProvider;
    @Mock SetCustomFlashPatternTask mSetCustomFlashPatternTask;
    @Mock ProtocolSpec mProtocolSpec;

    Future mTaskFuture;
    CarDeviceStatus mCarDeviceStatus;
    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);

    static class SourceFixture {
        MediaSourceType sourceType;
        MediaSourceType prevExpected;
        MediaSourceType nextExpected;

        SourceFixture(MediaSourceType sourceType, MediaSourceType prevExpected, MediaSourceType nextExpected) {
            this.sourceType = sourceType;
            this.prevExpected = prevExpected;
            this.nextExpected = nextExpected;
        }
    }

    @DataPoints
    public static final SourceFixture[] ORDER_EXISTS_SOURCE_FIXTURES = new SourceFixture[] {
            new SourceFixture(MediaSourceType.RADIO,      MediaSourceType.BT_AUDIO,   MediaSourceType.CD),
            new SourceFixture(MediaSourceType.CD,         MediaSourceType.RADIO,      MediaSourceType.USB),
            new SourceFixture(MediaSourceType.USB,        MediaSourceType.CD,         MediaSourceType.PANDORA),
            new SourceFixture(MediaSourceType.PANDORA,    MediaSourceType.USB,        MediaSourceType.SPOTIFY),
            new SourceFixture(MediaSourceType.SPOTIFY,    MediaSourceType.PANDORA,    MediaSourceType.APP_MUSIC),
            new SourceFixture(MediaSourceType.APP_MUSIC,  MediaSourceType.SPOTIFY,    MediaSourceType.AUX),
            new SourceFixture(MediaSourceType.AUX,        MediaSourceType.APP_MUSIC,  MediaSourceType.BT_AUDIO),
            new SourceFixture(MediaSourceType.BT_AUDIO,   MediaSourceType.AUX,        MediaSourceType.RADIO)
    };

    static class CommandFixture {
        SmartPhoneControlCommand smartPhoneControlCommand;
        ShortcutKey expected;

        CommandFixture(SmartPhoneControlCommand smartPhoneControlCommand, ShortcutKey expected) {
            this.smartPhoneControlCommand = smartPhoneControlCommand;
            this.expected = expected;
        }
    }

    @DataPoints
    public static final CommandFixture[] COMMAND_FIXTURES = new CommandFixture[]{
            new CommandFixture(SmartPhoneControlCommand.MUSIC, ShortcutKey.SOURCE),
            new CommandFixture(SmartPhoneControlCommand.NAVI, ShortcutKey.NAVI),
            new CommandFixture(SmartPhoneControlCommand.PHONE, ShortcutKey.PHONE),
            new CommandFixture(SmartPhoneControlCommand.MAIL, ShortcutKey.MESSAGE),
            new CommandFixture(SmartPhoneControlCommand.VR, ShortcutKey.VOICE),
            new CommandFixture(SmartPhoneControlCommand.APP, ShortcutKey.APP)
    };

    @DataPoints
    public static final MediaSourceType[] ORDER_NOT_EXISTS_FIXTURES = new MediaSourceType[] {
            MediaSourceType.DAB,
            MediaSourceType.SIRIUS_XM,
            MediaSourceType.HD_RADIO,
            MediaSourceType.BT_PHONE,
            MediaSourceType.OFF,
            MediaSourceType.IPOD,
            MediaSourceType.TI
    };

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        when(mHandler.sendMessageAtTime(any(Message.class), anyLong())).then(invocationOnMock -> {
            Message message = (Message) invocationOnMock.getArguments()[0];
            mMainHandler.post(() -> {
                message.getCallback().run();
                mSignal.countDown();
            });
            return true;
        });

        mCarDeviceStatus = new CarDeviceStatus();
        mCarDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        mCarDeviceStatus.availableSourceTypes = EnumSet.of(
                MediaSourceType.RADIO,
                MediaSourceType.CD,
                MediaSourceType.AUX,
                MediaSourceType.USB,
                MediaSourceType.BT_AUDIO,
                MediaSourceType.PANDORA,
                MediaSourceType.SPOTIFY,
                MediaSourceType.APP_MUSIC
        );
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);
        when(mPacketBuilder.createSourceSwitchCommand(any(MediaSourceType.class))).thenReturn(mOutgoingPacket);
        when(mSetCustomFlashPatternTaskProvider.get()).thenReturn(mSetCustomFlashPatternTask);
        when(mStatusHolder.getProtocolSpec()).thenReturn(mProtocolSpec);

        mTaskFuture = mock(Future.class);
    }

    @Test
    public void initialize() throws Exception {
        // setup
        SourceControllerImpl controller = mock(SourceControllerImpl.class);
        when(mSourceControllers.get(MediaSourceType.APP_MUSIC)).thenReturn(controller);

        // exercise
        mCarDevice.initialize();
        mSignal.await();

        // verify
        verify(mCarDeviceConnection).initialize(ACTION_USB_ACCESSORY_PERMISSION, BroadcastReceiverImpl.class);
        verify(controller).active();
        verify(mEventBus).register(mCarDevice);

    }

    @Test
    public void getSourceController() throws Exception {
        // setup
        SourceController expected = mock(SourceController.class);
        when(mSourceControllers.get(MediaSourceType.APP_MUSIC)).thenReturn(expected);

        // exercise
        SourceController actual = mCarDevice.getSourceController(MediaSourceType.APP_MUSIC);

        // verify
        assertThat(actual, is(expected));
    }

    @Test
    public void getSourceController_GetNullSourceController() throws Exception {
        // exercise
        SourceController actual = mCarDevice.getSourceController(MediaSourceType.OFF);

        // verify
        assertThat(actual, is(mNullSourceController));
    }

    @Test
    public void selectSource() throws Exception {
        // exercise
        mCarDevice.selectSource(MediaSourceType.RADIO);

        // verify
        verify(mPacketBuilder).createSourceSwitchCommand(MediaSourceType.RADIO);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Theory
    public void changeNextSource_HappyPath(SourceFixture sourceFixture) throws Exception {
        // setup
        mCarDeviceStatus.sourceType = sourceFixture.sourceType;

        // exercise
        mCarDevice.changeNextSource();

        // verify
        verify(mPacketBuilder).createSourceSwitchCommand(sourceFixture.nextExpected);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Theory
    public void changeNextSource_OrderNull(MediaSourceType type) throws Exception {
        // setup
        mCarDeviceStatus.sourceType = type;

        // exercise
        mCarDevice.changeNextSource();

        // verify
        verify(mPacketBuilder, never()).createSourceSwitchCommand(any(MediaSourceType.class));
        verify(mCarDeviceConnection, never()).sendPacket(any(OutgoingPacket.class));
    }

    @Test
    public void changeNextSource_SameSourceType() throws Exception {
        // setup
        mCarDeviceStatus.availableSourceTypes = EnumSet.of(MediaSourceType.APP_MUSIC);
        mCarDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;

        // exercise
        mCarDevice.changeNextSource();

        // verify
        verify(mPacketBuilder, never()).createSourceSwitchCommand(any(MediaSourceType.class));
        verify(mCarDeviceConnection, never()).sendPacket(any(OutgoingPacket.class));
    }

    @Theory
    public void changePreviousSource_HappyPath(SourceFixture sourceFixture) throws Exception {
        // setup
        mCarDeviceStatus.sourceType = sourceFixture.sourceType;

        // exercise
        mCarDevice.changePreviousSource();

        // verify
        verify(mPacketBuilder).createSourceSwitchCommand(sourceFixture.prevExpected);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Theory
    public void changePreviousSource_OrderNull(MediaSourceType type) throws Exception {
        // setup
        mCarDeviceStatus.sourceType = type;

        // exercise
        mCarDevice.changePreviousSource();

        // verify
        verify(mPacketBuilder, never()).createSourceSwitchCommand(any(MediaSourceType.class));
        verify(mCarDeviceConnection, never()).sendPacket(any(OutgoingPacket.class));
    }

    @Test
    public void changePreviousSource_SameSourceType() throws Exception {
        // setup
        mCarDeviceStatus.availableSourceTypes = EnumSet.of(MediaSourceType.APP_MUSIC);

        // exercise
        mCarDevice.changePreviousSource();

        // verify
        verify(mPacketBuilder, never()).createSourceSwitchCommand(any(MediaSourceType.class));
        verify(mCarDeviceConnection, never()).sendPacket(any(OutgoingPacket.class));
    }

    @Test
    public void onCrpStatusUpdateEvent() throws Exception {
        // setup
        SourceControllerImpl radioController = mock(SourceControllerImpl.class);
        when(mSourceControllers.get(MediaSourceType.RADIO)).thenReturn(radioController);
        SourceControllerImpl spotifyController = mock(SourceControllerImpl.class);
        when(mSourceControllers.get(MediaSourceType.SPOTIFY)).thenReturn(spotifyController);
        mCarDeviceStatus.sourceType = MediaSourceType.RADIO;
        mCarDevice.initialize();
        Mockito.reset(radioController);
        mCarDeviceStatus.sourceType = MediaSourceType.SPOTIFY;

        // exercise
        mCarDevice.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());
        mSignal.await();

        // verify
        verify(radioController).inactive();
        verify(spotifyController).active();
    }

    @Test
    public void onCrpStatusUpdateEvent_SameSourceType() throws Exception {
        // setup
        SourceControllerImpl radioController = mock(SourceControllerImpl.class);
        when(mSourceControllers.get(MediaSourceType.RADIO)).thenReturn(radioController);
        mCarDeviceStatus.sourceType = MediaSourceType.RADIO;
        mCarDevice.initialize();
        Mockito.reset(radioController);

        // exercise
        mCarDevice.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());
        mSignal.await();

        // verify
        verify(radioController, never()).inactive();
        verify(radioController, times(1)).active();
    }

    @Test(expected = NullPointerException.class)
    public void getSourceController_ArgNull() throws Exception {
        // exercise
        mCarDevice.getSourceController(null);
    }

    @Test(expected = NullPointerException.class)
    public void selectSource_ArgNull() throws Exception {
        // exercise
        mCarDevice.selectSource(null);
    }

    @Test
    public void changeScreen() throws Exception {
        // setup
        when(mPacketBuilder.createScreenChangeCommand(eq(CarDeviceScreen.ILLUMI_PREVIEW), eq(TransitionDirection.ENTER))).thenReturn(mOutgoingPacket);

        // exercise
        mCarDevice.changeScreen(CarDeviceScreen.ILLUMI_PREVIEW, TransitionDirection.ENTER);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void changeScreen_ArgScreenNull() throws Exception {
        // exercise
        mCarDevice.changeScreen(null, TransitionDirection.ENTER);
    }

    @Test(expected = NullPointerException.class)
    public void changeScreen_ArgDirectionNull() throws Exception {
        // exercise
        mCarDevice.changeScreen(CarDeviceScreen.ILLUMI_PREVIEW, null);
    }

    @Test
    public void readNotificationRequest() throws Exception {
        // setup
        when(mPacketBuilder.createReadingCommand(eq(ReadingRequestType.START))).thenReturn(mOutgoingPacket);

        // exercise
        mCarDevice.requestReadNotification(ReadingRequestType.START);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void customFlashRequest() throws Exception {
        // setup
        when(mPacketBuilder.createCustomFlashCommand(eq(CustomFlashRequestType.START))).thenReturn(mOutgoingPacket);

        // exercise
        mCarDevice.requestCustomFlash(CustomFlashRequestType.START);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void impactDetectionCountdown() throws Exception {
        // setup
        when(mPacketBuilder.createImpactDetectionCommand(eq(10))).thenReturn(mOutgoingPacket);

        // exercise
        mCarDevice.impactDetectionCountdown(10);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void phoneCall() throws Exception {
        // setup
        when(mPacketBuilder.createPhoneCallCommand(eq("03-1234-5678"))).thenReturn(mOutgoingPacket);

        // exercise
        mCarDevice.phoneCall("03-1234-5678");

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void exitMenu() throws Exception {
        // setup
        when(mPacketBuilder.createExitMenuCommand()).thenReturn(mOutgoingPacket);

        // exercise
        mCarDevice.exitMenu();

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void onAppSharedPreferenceChanged_Key_ThemeType() throws Exception {
        // setup
        when(mProtocolSpec.isSphCarDevice()).thenReturn(true);
        IlluminationSettingStatus status = new IlluminationSettingStatus();
        status.customFlashPatternSettingEnabled = true;
        when(mStatusHolder.getIlluminationSettingStatus()).thenReturn(status);
        when(mSetCustomFlashPatternTask.setParamsForCustomFlashPattern(FlashPattern.BGP1)).thenReturn(mSetCustomFlashPatternTask);

        // exercise
        mCarDevice.onAppSharedPreferenceChanged(mPreference, AppSharedPreference.KEY_THEME_TYPE);

        // verify
        verify(mTaskExecutor).submit(mSetCustomFlashPatternTask);
    }

    @Test
    public void onAppSharedPreferenceChanged_Key_ThemeType_TaskAlreadyStarted() throws Exception {
        // setup
        when(mTaskExecutor.submit(mSetCustomFlashPatternTask)).thenReturn(mTaskFuture);
        when(mTaskFuture.isDone()).thenReturn(false);
        when(mSetCustomFlashPatternTask.setParamsForCustomFlashPattern(FlashPattern.BGP1)).thenReturn(mSetCustomFlashPatternTask);
        when(mProtocolSpec.isSphCarDevice()).thenReturn(true);
        IlluminationSettingStatus status = new IlluminationSettingStatus();
        status.customFlashPatternSettingEnabled = true;
        when(mStatusHolder.getIlluminationSettingStatus()).thenReturn(status);

        // exercise
        mCarDevice.onAppSharedPreferenceChanged(mPreference, AppSharedPreference.KEY_THEME_TYPE);
        mCarDevice.onAppSharedPreferenceChanged(mPreference, AppSharedPreference.KEY_THEME_TYPE);

        // verify
        verify(mTaskFuture).cancel(true);
    }

    @Test
    public void onAppSharedPreferenceChanged_Key_LightingEffect_ChangeToTrue() throws Exception {
        // setup
        when(mPacketBuilder.createCustomFlashCommand(eq(CustomFlashRequestType.START))).thenReturn(mOutgoingPacket);
        when(mPreference.isLightingEffectEnabled()).thenReturn(true);
        when(mProtocolSpec.isSphCarDevice()).thenReturn(true);

        // exercise
        mCarDevice.onAppSharedPreferenceChanged(mPreference, AppSharedPreference.KEY_LIGHTING_EFFECT_ENABLED);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void onAppSharedPreferenceChanged_Key_LightingEffect_ChangeToFalse() throws Exception {
        // setup
        when(mPacketBuilder.createCustomFlashCommand(eq(CustomFlashRequestType.FINISH))).thenReturn(mOutgoingPacket);
        when(mPreference.isLightingEffectEnabled()).thenReturn(false);
        when(mProtocolSpec.isSphCarDevice()).thenReturn(true);

        // exercise
        mCarDevice.onAppSharedPreferenceChanged(mPreference, AppSharedPreference.KEY_LIGHTING_EFFECT_ENABLED);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void onAppSharedPreferenceChanged_Key_LightingEffect_CarDeviceIsDEH() throws Exception {
        // setup
        when(mProtocolSpec.isSphCarDevice()).thenReturn(false);

        // exercise
        mCarDevice.onAppSharedPreferenceChanged(mPreference, AppSharedPreference.KEY_LIGHTING_EFFECT_ENABLED);

        // verify
        verify(mCarDeviceConnection, never()).sendPacket(any(OutgoingPacket.class));
        verify(mPacketBuilder, never()).createCustomFlashCommand(any(CustomFlashRequestType.class));
    }


    @Test
    public synchronized void onAdasWarningUpdateEvent() throws Exception {
        // setup
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        ProtocolSpec spec = mock(ProtocolSpec.class);
        when(mStatusHolder.getSmartPhoneStatus()).thenReturn(status);
        when(mStatusHolder.getProtocolSpec()).thenReturn(spec);
        when(status.getAdasWarningStatus()).thenReturn(AdasWarningStatus.CONTINUOUS);
        when(spec.getConnectingProtocolVersion()).thenReturn(ProtocolVersion.V4);
        when(mPacketBuilder.createSmartPhoneStatusNotification(eq(ProtocolVersion.V4), eq(status), true)).thenReturn(mOutgoingPacket);

        // exercise
        mCarDevice.onAdasWarningUpdateEvent(null);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public synchronized void onImpactEvent() throws Exception {
        // setup
        when(mPacketBuilder.createImpactDetectionCommand(eq(60))).thenReturn(mOutgoingPacket);

        // exercise
        mCarDevice.onImpactEvent(null);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public synchronized void onReadNotificationPostedEvent() throws Exception {
        // setup
        when(mPacketBuilder.createNewMessageCommand()).thenReturn(mOutgoingPacket);

        // exercise
        mCarDevice.onReadNotificationPostedEvent(null);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Theory
    public synchronized void onSmartPhoneControlCommandEvent(CommandFixture fixture) throws Exception {
        // exercise
        mCarDevice.onSmartPhoneControlCommandEvent(new SmartPhoneControlCommandEvent(fixture.smartPhoneControlCommand));

        // verify
        ArgumentCaptor<ShortcutKeyEvent> captor = ArgumentCaptor.forClass(ShortcutKeyEvent.class);
        verify(mEventBus).post(captor.capture());
        assertThat(captor.getValue().shortcutKey, is(fixture.expected));
    }

    @Test
    public synchronized void onCrpSendTaskFinishedEvent() throws Exception {
        // setup
        when(mProtocolSpec.isSphCarDevice()).thenReturn(true);
        IlluminationSetting setting = new IlluminationSetting();
        setting.requestStatus = RequestStatus.SENT;
        when(mStatusHolder.getIlluminationSetting()).thenReturn(setting);
        when(mStatusHolder.shouldSendCustomFlashPatternRequests()).thenReturn(true);
        when(mSetCustomFlashPatternTask.setParamsForCustomFlashPattern(FlashPattern.BGP1)).thenReturn(mSetCustomFlashPatternTask);

        // exercise
        mCarDevice.onCrpSendTaskFinishedEvent(new CrpSendTaskFinishedEvent(SendTaskId.ILLUMINATION_SETTINGS_REQUEST));

        // verify
        verify(mTaskExecutor).submit(mSetCustomFlashPatternTask);
    }

    @Test
    public synchronized void onCrpSendTaskFinishedEvent_NotIlluminationSettingsRequest() throws Exception {
        // exercise
        mCarDevice.onCrpSendTaskFinishedEvent(new CrpSendTaskFinishedEvent(SendTaskId.AUDIO_SETTINGS_REQUEST));
        when(mSetCustomFlashPatternTask.setParamsForCustomFlashPattern(FlashPattern.BGP1)).thenReturn(mSetCustomFlashPatternTask);

        // verify
        verify(mTaskExecutor, never()).submit(any(Runnable.class));
    }

    @Test
    public synchronized void onCrpSendTaskFinishedEvent_SettingDisabled() throws Exception {
        // setup
        when(mProtocolSpec.isSphCarDevice()).thenReturn(true);
        IlluminationSetting setting = new IlluminationSetting();
        when(mStatusHolder.getIlluminationSetting()).thenReturn(setting);
        when(mStatusHolder.shouldSendCustomFlashPatternRequests()).thenReturn(false);

        // exercise
        mCarDevice.onCrpSendTaskFinishedEvent(new CrpSendTaskFinishedEvent(SendTaskId.ILLUMINATION_SETTINGS_REQUEST));
        when(mSetCustomFlashPatternTask.setParamsForCustomFlashPattern(FlashPattern.BGP1)).thenReturn(mSetCustomFlashPatternTask);

        // verify
        verify(mTaskExecutor, never()).submit(any(Runnable.class));
    }
}