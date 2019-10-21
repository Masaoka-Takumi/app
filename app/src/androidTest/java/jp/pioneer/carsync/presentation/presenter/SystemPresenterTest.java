package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.content.res.Resources;

import org.greenrobot.eventbus.EventBus;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.SystemSettingChangeEvent;
import jp.pioneer.carsync.domain.event.SystemSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSystem;
import jp.pioneer.carsync.domain.model.AttMuteSetting;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SteeringRemoteControlSettingType;
import jp.pioneer.carsync.domain.model.SystemSetting;
import jp.pioneer.carsync.domain.model.SystemSettingSpec;
import jp.pioneer.carsync.domain.model.SystemSettingStatus;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.SystemView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2017/09/25.
 */
public class SystemPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SystemPresenter mPresenter;
    @Mock SystemView mView;
    @Mock GetStatusHolder mGetCase;
    @Mock EventBus mEventBus;
    @Mock Context mContext;
    @Mock AppSharedPreference mPreference;
    @Mock PreferSystem mPreferCase;

    StatusHolder mStatusHolder = mock(StatusHolder.class);
    CarDeviceSpec mCarDeviceSpec = new CarDeviceSpec();
    SystemSettingSpec mSystemSettingSpec = new SystemSettingSpec();
    SystemSetting mSystemSetting = new SystemSetting();
    SystemSettingStatus mSystemSettingStatus = new SystemSettingStatus();
    CarDeviceStatus mCarDeviceStatus = new CarDeviceStatus();

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mCarDeviceSpec.systemSettingSpec = mSystemSettingSpec;
        mCarDeviceSpec.initialSettingSupported = true;
        mSystemSettingSpec.auxSettingSupported = true;
        mSystemSettingSpec.spotifySettingSupported = true;
        mSystemSettingSpec.pandoraSettingSupported = true;
        mSystemSettingSpec.btAudioSettingSupported = true;
        mSystemSettingSpec.powerSaveSettingSupported = true;
        mSystemSettingSpec.attMuteSettingSupported = true;
        mSystemSettingSpec.beepToneSettingSupported = true;
        mSystemSettingSpec.autoPiSettingSupported = true;
        mSystemSettingSpec.steeringRemoteControlSettingSupported = true;
        mSystemSettingSpec.usbAutoSettingSupported = true;
        mSystemSettingSpec.demoSettingSupported = true;
        mSystemSettingSpec.appAutoStartSettingSupported = true;
        mSystemSettingStatus.auxSettingEnabled = false;
        mSystemSettingStatus.spotifySettingEnabled = true;
        mSystemSettingStatus.pandoraSettingEnabled = false;
        mSystemSettingStatus.btAudioSettingEnabled = true;
        mSystemSettingStatus.powerSaveSettingEnabled = false;
        mSystemSettingStatus.attMuteSettingEnabled = true;
        mSystemSettingStatus.beepToneSettingEnabled = false;
        mSystemSettingStatus.autoPiSettingEnabled = true;
        mSystemSettingStatus.steeringRemoteControlSettingEnabled = false;
        mSystemSettingStatus.usbAutoSettingEnabled = true;
        mSystemSettingStatus.appAutoStartSettingEnabled = false;
        mSystemSetting.beepToneSetting = true;
        mSystemSetting.attMuteSetting = AttMuteSetting.ATT;
        mSystemSetting.demoSetting = true;
        mSystemSetting.powerSaveSetting = false;
        mSystemSetting.btAudioSetting = true;
        mSystemSetting.pandoraSetting = false;
        mSystemSetting.spotifySetting = true;
        mSystemSetting.auxSetting = false;
        mSystemSetting.appAutoStartSetting = true;
        mSystemSetting.usbAutoSetting = false;
        mSystemSetting.steeringRemoteControlSetting = SteeringRemoteControlSettingType.MAZDA;
        mSystemSetting.autoPiSetting = true;
        mCarDeviceStatus.initialSettingEnabled = true;

        when(mGetCase.execute()).thenReturn(mStatusHolder);
        when(mStatusHolder.getSystemSetting()).thenReturn(mSystemSetting);
        when(mStatusHolder.getSystemSettingStatus()).thenReturn(mSystemSettingStatus);
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);
        when(mStatusHolder.getCarDeviceSpec()).thenReturn(mCarDeviceSpec);
    }

    @Test(expected = NullPointerException.class)
    public void testLifecycle() throws Exception {
        // setup
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);

        // exercise
        mPresenter.onInitialize();
        mPresenter.takeView(mView);
        mPresenter.resume();
        mPresenter.pause();
        mPresenter.destroy();

        // verify
        verify(mView).setBeepToneSetting(mSystemSettingSpec.beepToneSettingSupported,mSystemSettingStatus.beepToneSettingEnabled, mSystemSetting.beepToneSetting);
        verify(mView).setAutoPiSetting(mSystemSettingSpec.autoPiSettingSupported,mSystemSettingStatus.autoPiSettingEnabled,mSystemSetting.autoPiSetting);
        verify(mView).setDemoModeSetting(mSystemSettingSpec.demoSettingSupported,mSystemSettingStatus.demoSettingEnabled, mSystemSetting.demoSetting);
        verify(mView).setAuxSetting(mSystemSettingSpec.auxSettingSupported,mSystemSettingStatus.auxSettingEnabled, mSystemSetting.auxSetting);
        verify(mView).setBtAudioSetting(mSystemSettingSpec.btAudioSettingSupported,mSystemSettingStatus.btAudioSettingEnabled, mSystemSetting.btAudioSetting);
        verify(mView).setPandoraSetting(mSystemSettingSpec.pandoraSettingSupported,mSystemSettingStatus.pandoraSettingEnabled, mSystemSetting.pandoraSetting);
        verify(mView).setSpotifySetting(mSystemSettingSpec.spotifySettingSupported,mSystemSettingStatus.spotifySettingEnabled, mSystemSetting.spotifySetting);
        verify(mView).setPowerSaveModeSetting(mSystemSettingSpec.powerSaveSettingSupported,mSystemSettingStatus.powerSaveSettingEnabled, mSystemSetting.powerSaveSetting);
        verify(mView).setAppAutoLaunchSetting(mSystemSettingSpec.appAutoStartSettingSupported,mSystemSettingStatus.appAutoStartSettingEnabled, mSystemSetting.appAutoStartSetting);
        verify(mView).setUsbAutoSetting(mSystemSettingSpec.usbAutoSettingSupported,mSystemSettingStatus.usbAutoSettingEnabled, mSystemSetting.usbAutoSetting);
        verify(mView).setDisplayOffSetting(false, false,true);
        verify(mView).setAttMuteSetting(mSystemSettingSpec.attMuteSettingSupported,mSystemSettingStatus.attMuteSettingEnabled, mSystemSetting.attMuteSetting);
        verify(mView).setInitialSettings(mCarDeviceSpec.initialSettingSupported,mCarDeviceStatus.initialSettingEnabled);

        verify(mEventBus).register(mPresenter);
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void onSystemSettingChangeEvent() throws Exception {
        // exercise
        mPresenter.onSystemSettingChangeEvent(new SystemSettingChangeEvent());

        // verify
        verify(mView).setBeepToneSetting(mSystemSettingSpec.beepToneSettingSupported,mSystemSettingStatus.beepToneSettingEnabled, mSystemSetting.beepToneSetting);
        verify(mView).setAutoPiSetting(mSystemSettingSpec.autoPiSettingSupported,mSystemSettingStatus.autoPiSettingEnabled,mSystemSetting.autoPiSetting);
        verify(mView).setDemoModeSetting(mSystemSettingSpec.demoSettingSupported,mSystemSettingStatus.demoSettingEnabled, mSystemSetting.demoSetting);
        verify(mView).setAuxSetting(mSystemSettingSpec.auxSettingSupported,mSystemSettingStatus.auxSettingEnabled, mSystemSetting.auxSetting);
        verify(mView).setBtAudioSetting(mSystemSettingSpec.btAudioSettingSupported,mSystemSettingStatus.btAudioSettingEnabled, mSystemSetting.btAudioSetting);
        verify(mView).setPandoraSetting(mSystemSettingSpec.pandoraSettingSupported,mSystemSettingStatus.pandoraSettingEnabled, mSystemSetting.pandoraSetting);
        verify(mView).setSpotifySetting(mSystemSettingSpec.spotifySettingSupported,mSystemSettingStatus.spotifySettingEnabled, mSystemSetting.spotifySetting);
        verify(mView).setPowerSaveModeSetting(mSystemSettingSpec.powerSaveSettingSupported,mSystemSettingStatus.powerSaveSettingEnabled, mSystemSetting.powerSaveSetting);
        verify(mView).setAppAutoLaunchSetting(mSystemSettingSpec.appAutoStartSettingSupported,mSystemSettingStatus.appAutoStartSettingEnabled, mSystemSetting.appAutoStartSetting);
        verify(mView).setUsbAutoSetting(mSystemSettingSpec.usbAutoSettingSupported,mSystemSettingStatus.usbAutoSettingEnabled, mSystemSetting.usbAutoSetting);
        verify(mView).setDisplayOffSetting(false, false,true);
        verify(mView).setAttMuteSetting(mSystemSettingSpec.attMuteSettingSupported,mSystemSettingStatus.attMuteSettingEnabled, mSystemSetting.attMuteSetting);
        verify(mView).setInitialSettings(mCarDeviceSpec.initialSettingSupported,mCarDeviceStatus.initialSettingEnabled);
    }

    @Test
    public void onSystemSettingStatusChangeEvent() throws Exception {
        // exercise
        mPresenter.onSystemSettingStatusChangeEvent(new SystemSettingStatusChangeEvent());

        // verify
        verify(mView).setBeepToneSetting(mSystemSettingSpec.beepToneSettingSupported,mSystemSettingStatus.beepToneSettingEnabled, mSystemSetting.beepToneSetting);
        verify(mView).setAutoPiSetting(mSystemSettingSpec.autoPiSettingSupported,mSystemSettingStatus.autoPiSettingEnabled,mSystemSetting.autoPiSetting);
        verify(mView).setDemoModeSetting(mSystemSettingSpec.demoSettingSupported,mSystemSettingStatus.demoSettingEnabled, mSystemSetting.demoSetting);
        verify(mView).setAuxSetting(mSystemSettingSpec.auxSettingSupported,mSystemSettingStatus.auxSettingEnabled, mSystemSetting.auxSetting);
        verify(mView).setBtAudioSetting(mSystemSettingSpec.btAudioSettingSupported,mSystemSettingStatus.btAudioSettingEnabled, mSystemSetting.btAudioSetting);
        verify(mView).setPandoraSetting(mSystemSettingSpec.pandoraSettingSupported,mSystemSettingStatus.pandoraSettingEnabled, mSystemSetting.pandoraSetting);
        verify(mView).setSpotifySetting(mSystemSettingSpec.spotifySettingSupported,mSystemSettingStatus.spotifySettingEnabled, mSystemSetting.spotifySetting);
        verify(mView).setPowerSaveModeSetting(mSystemSettingSpec.powerSaveSettingSupported,mSystemSettingStatus.powerSaveSettingEnabled, mSystemSetting.powerSaveSetting);
        verify(mView).setAppAutoLaunchSetting(mSystemSettingSpec.appAutoStartSettingSupported,mSystemSettingStatus.appAutoStartSettingEnabled, mSystemSetting.appAutoStartSetting);
        verify(mView).setUsbAutoSetting(mSystemSettingSpec.usbAutoSettingSupported,mSystemSettingStatus.usbAutoSettingEnabled, mSystemSetting.usbAutoSetting);
        verify(mView).setDisplayOffSetting(false, false,true);
        verify(mView).setAttMuteSetting(mSystemSettingSpec.attMuteSettingSupported,mSystemSettingStatus.attMuteSettingEnabled, mSystemSetting.attMuteSetting);
        verify(mView).setInitialSettings(mCarDeviceSpec.initialSettingSupported,mCarDeviceStatus.initialSettingEnabled);
    }

    @Test
    public void testOnSelectBeepToneSettingAction() throws Exception {
        // exercise
        mPresenter.onSelectBeepToneSettingAction(true);

        // verify
        verify(mPreferCase).setBeepTone(true);
    }

    @Test
    public void testOnSelectAutoPiSettingAction() throws Exception {
        // exercise
        mPresenter.onSelectAutoPiSettingAction(true);

        // verify
        verify(mPreferCase).setAutoPi(true);

    }

    @Test
    public void testOnSelectDemoModeSettingAction() throws Exception {
        // exercise
        mPresenter.onSelectDemoModeSettingAction(true);

        // verify
        verify(mPreferCase).setDemo(true);
    }

    @Test
    public void testOnSelectAuxSettingAction() throws Exception {
        // exercise
        mPresenter.onSelectAuxSettingAction(true);

        // verify
        verify(mPreferCase).setAux(true);
    }

    @Test
    public void testOnSelectBtAudioSettingAction() throws Exception {
        // exercise
        mPresenter.onSelectBtAudioSettingAction(true);

        // verify
        verify(mPreferCase).setBtAudio(true);
    }

    @Test
    public void testOnSelectPandoraSettingAction() throws Exception {
        // exercise
        mPresenter.onSelectPandoraSettingAction(true);

        // verify
        verify(mPreferCase).setPandora(true);
    }

    @Test
    public void testOnSelectSpotifySettingAction() throws Exception {
        // exercise
        mPresenter.onSelectSpotifySettingAction(true);

        // verify
        verify(mPreferCase).setSpotify(true);
    }

    @Test
    public void testOnSelectPowerSaveModeSettingAction() throws Exception {
        // exercise
        mPresenter.onSelectPowerSaveModeSettingAction(true);

        // verify
        verify(mPreferCase).setPowerSave(true);
    }

    @Test
    public void testOnSelectAppAutoLaunchSettingAction() throws Exception {
        // exercise
        mPresenter.onSelectAppAutoLaunchSettingAction(true);

        // verify
        verify(mPreferCase).setAppAutoStart(true);
    }

    @Test
    public void testOnSelectUsbAutoSettingAction() throws Exception {
        // exercise
        mPresenter.onSelectUsbAutoSettingAction(true);

        // verify
        verify(mPreferCase).setUsbAuto(true);
    }

    @Test
    public void testOnSelectDisplayOffSettingAction() throws Exception {
        // exercise
        mPresenter.onSelectDisplayOffSettingAction(true);

        // verify
        verify(mPreferCase).setDisplayOff(true);
    }

    @Test
    public void testOnSelectAttMuteSettingAction() {
        // exercise
        mPresenter.onSelectAttMuteSettingAction();

        // verify
        verify(mPreferCase).toggleAttMute();
    }
    
    @Test
    public void testOnInitialSettingAction() throws Exception {
        // setup
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        Resources resources = mock(Resources.class);
        when(mContext.getResources()).thenReturn(resources);
        when(resources.getString(R.string.setting_system_initial_settings)).thenReturn("TEST");

        // exercise
        mPresenter.onInitialSettingAction();

        // verify
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, Matchers.is(ScreenId.SETTINGS_SYSTEM_INITIAL));

    }

}