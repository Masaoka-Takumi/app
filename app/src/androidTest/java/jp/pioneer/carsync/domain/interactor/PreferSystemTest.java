package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.domain.component.SystemSettingUpdater;
import jp.pioneer.carsync.domain.model.AttMuteSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SteeringRemoteControlSettingType;
import jp.pioneer.carsync.domain.model.SystemSetting;
import jp.pioneer.carsync.domain.model.SystemSettingStatus;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/19.
 */
public class PreferSystemTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks PreferSystem mPreferSystem;
    @Mock Handler mHandler;
    @Mock StatusHolder mStatusHolder;
    @Mock SystemSettingUpdater mUpdater;

    SystemSetting mSystemSetting = new SystemSetting();
    SystemSettingStatus mSystemSettingStatus = new SystemSettingStatus();
    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);

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

        when(mStatusHolder.getSystemSetting()).thenReturn(mSystemSetting);
        when(mStatusHolder.getSystemSettingStatus()).thenReturn(mSystemSettingStatus);
    }

    @Test
    public void setBeepTone() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.beepToneSettingEnabled = true;

        // exercise
        mPreferSystem.setBeepTone(true);
        mSignal.await();

        // verify
        verify(mUpdater).setBeepTone(true);
    }

    @Test
    public void setBeepTone_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.beepToneSettingEnabled = false;

        // exercise
        mPreferSystem.setBeepTone(true);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setBeepTone(anyBoolean());
    }

    @Test
    public void toggleAttMute() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.attMuteSettingEnabled = true;
        mSystemSetting.attMuteSetting = AttMuteSetting.ATT;

        // exercise
        mPreferSystem.toggleAttMute();
        mSignal.await();

        // verify
        verify(mUpdater).setAttMute(AttMuteSetting.MUTE);
    }

    @Test
    public void toggleAttMute_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.attMuteSettingEnabled = false;

        // exercise
        mPreferSystem.toggleAttMute();
        mSignal.await();

        // verify
        verify(mUpdater, never()).setAttMute(any(AttMuteSetting.class));
    }

    @Test
    public void setDemo() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.demoSettingEnabled = true;

        // exercise
        mPreferSystem.setDemo(true);
        mSignal.await();

        // verify
        verify(mUpdater).setDemo(true);
    }

    @Test
    public void setDemo_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.demoSettingEnabled = false;

        // exercise
        mPreferSystem.setDemo(true);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setDemo(anyBoolean());
    }

    @Test
    public void setPowerSave() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.powerSaveSettingEnabled = true;

        // exercise
        mPreferSystem.setPowerSave(true);
        mSignal.await();

        // verify
        verify(mUpdater).setPowerSave(true);
    }

    @Test
    public void setPowerSave_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.powerSaveSettingEnabled = false;

        // exercise
        mPreferSystem.setPowerSave(true);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setPowerSave(anyBoolean());
    }

    @Test
    public void setBtAudio() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.btAudioSettingEnabled = true;

        // exercise
        mPreferSystem.setBtAudio(true);
        mSignal.await();

        // verify
        verify(mUpdater).setBtAudio(true);
    }

    @Test
    public void setBtAudio_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.btAudioSettingEnabled = false;

        // exercise
        mPreferSystem.setBtAudio(true);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setBtAudio(anyBoolean());
    }

    @Test
    public void setPandora() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.pandoraSettingEnabled = true;

        // exercise
        mPreferSystem.setPandora(true);
        mSignal.await();

        // verify
        verify(mUpdater).setPandora(true);
    }

    @Test
    public void setPandora_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.pandoraSettingEnabled = false;

        // exercise
        mPreferSystem.setPandora(true);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setPandora(anyBoolean());
    }

    @Test
    public void setSpotify() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.spotifySettingEnabled = true;

        // exercise
        mPreferSystem.setSpotify(true);
        mSignal.await();

        // verify
        verify(mUpdater).setSpotify(true);
    }

    @Test
    public void setSpotify_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.spotifySettingEnabled = false;

        // exercise
        mPreferSystem.setSpotify(true);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setSpotify(anyBoolean());
    }

    @Test
    public void setAux() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.auxSettingEnabled = true;

        // exercise
        mPreferSystem.setAux(true);
        mSignal.await();

        // verify
        verify(mUpdater).setAux(true);
    }

    @Test
    public void setAux_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.auxSettingEnabled = false;

        // exercise
        mPreferSystem.setAux(true);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setAux(anyBoolean());
    }

    @Test
    public void setAppAutoStart() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.appAutoStartSettingEnabled = true;

        // exercise
        mPreferSystem.setAppAutoStart(true);
        mSignal.await();

        // verify
        verify(mUpdater).setAppAutoStart(true);
    }

    @Test
    public void setAppAutoStart_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.appAutoStartSettingEnabled = false;

        // exercise
        mPreferSystem.setAppAutoStart(true);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setAppAutoStart(anyBoolean());
    }

    @Test
    public void setUsbAuto() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.usbAutoSettingEnabled = true;

        // exercise
        mPreferSystem.setUsbAuto(true);
        mSignal.await();

        // verify
        verify(mUpdater).setUsbAuto(true);
    }

    @Test
    public void setUsbAuto_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.usbAutoSettingEnabled = false;

        // exercise
        mPreferSystem.setUsbAuto(true);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setUsbAuto(anyBoolean());
    }

    @Test
    public void setSteeringRemoteControl() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.steeringRemoteControlSettingEnabled = true;

        // exercise
        mPreferSystem.setSteeringRemoteControl(SteeringRemoteControlSettingType.MAZDA);
        mSignal.await();

        // verify
        verify(mUpdater).setSteeringRemoteControl(SteeringRemoteControlSettingType.MAZDA);
    }

    @Test
    public void setSteeringRemoteControl_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.steeringRemoteControlSettingEnabled = false;

        // exercise
        mPreferSystem.setSteeringRemoteControl(SteeringRemoteControlSettingType.MAZDA);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setSteeringRemoteControl(any(SteeringRemoteControlSettingType.class));
    }

    @Test(expected = NullPointerException.class)
    public void setSteeringRemoteControl_ArgNull() throws Exception {
        // exercise
        mPreferSystem.setSteeringRemoteControl(null);
    }

    @Test
    public void setAutoPi() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.autoPiSettingEnabled = true;

        // exercise
        mPreferSystem.setAutoPi(true);
        mSignal.await();

        // verify
        verify(mUpdater).setAutoPi(true);
    }

    @Test
    public void setAutoPi_SettingDisabled() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        mSystemSettingStatus.autoPiSettingEnabled = false;

        // exercise
        mPreferSystem.setAutoPi(true);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setAutoPi(anyBoolean());
    }

}