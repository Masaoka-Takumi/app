package jp.pioneer.carsync.infrastructure.component;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.model.AttMuteSetting;
import jp.pioneer.carsync.domain.model.SteeringRemoteControlSettingType;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/19.
 */
public class SystemSettingUpdaterImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SystemSettingUpdaterImpl mSystemSettingUpdater;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    @Mock OutgoingPacket mOutgoingPacket;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void setBeepTone() throws Exception {
        // setup
        when(mPacketBuilder.createBeepToneSettingNotification(true)).thenReturn(mOutgoingPacket);

        // exercise
        mSystemSettingUpdater.setBeepTone(true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setAttMute() throws Exception {
        // setup
        when(mPacketBuilder.createAttMuteSettingNotification(AttMuteSetting.ATT)).thenReturn(mOutgoingPacket);

        // exercise
        mSystemSettingUpdater.setAttMute(AttMuteSetting.ATT);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setAttMute_ArgNull() throws Exception {
        // exercise
        mSystemSettingUpdater.setAttMute(null);
    }

    @Test
    public void setDemo() throws Exception {
        // setup
        when(mPacketBuilder.createDemoSettingNotification(true)).thenReturn(mOutgoingPacket);

        // exercise
        mSystemSettingUpdater.setDemo(true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setPowerSave() throws Exception {
        // setup
        when(mPacketBuilder.createPowerSaveSettingNotification(true)).thenReturn(mOutgoingPacket);

        // exercise
        mSystemSettingUpdater.setPowerSave(true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setBtAudio() throws Exception {
        // setup
        when(mPacketBuilder.createBtAudioSettingNotification(true)).thenReturn(mOutgoingPacket);

        // exercise
        mSystemSettingUpdater.setBtAudio(true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setPandora() throws Exception {
        // setup
        when(mPacketBuilder.createPandoraSettingNotification(true)).thenReturn(mOutgoingPacket);

        // exercise
        mSystemSettingUpdater.setPandora(true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setSpotify() throws Exception {
        // setup
        when(mPacketBuilder.createSpotifySettingNotification(true)).thenReturn(mOutgoingPacket);

        // exercise
        mSystemSettingUpdater.setSpotify(true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setAux() throws Exception {
        // setup
        when(mPacketBuilder.createAuxSettingNotification(true)).thenReturn(mOutgoingPacket);

        // exercise
        mSystemSettingUpdater.setAux(true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setAppAutoStart() throws Exception {
        // setup
        when(mPacketBuilder.createAppAutoStartSettingNotification(true)).thenReturn(mOutgoingPacket);

        // exercise
        mSystemSettingUpdater.setAppAutoStart(true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setUsbAuto() throws Exception {
        // setup
        when(mPacketBuilder.createUsbAutoSettingNotification(true)).thenReturn(mOutgoingPacket);

        // exercise
        mSystemSettingUpdater.setUsbAuto(true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setSteeringRemoteControl() throws Exception {
        // setup
        when(mPacketBuilder.createSteeringRemoteControlSettingNotification(SteeringRemoteControlSettingType.SUBARU1)).thenReturn(mOutgoingPacket);

        // exercise
        mSystemSettingUpdater.setSteeringRemoteControl(SteeringRemoteControlSettingType.SUBARU1);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setSteeringRemoteControl_ArgNull() throws Exception {
        // exercise
        mSystemSettingUpdater.setSteeringRemoteControl(null);
    }

    @Test
    public void setAutoPi() throws Exception {
        // setup
        when(mPacketBuilder.createAutoPiSettingNotification(true)).thenReturn(mOutgoingPacket);

        // exercise
        mSystemSettingUpdater.setAutoPi(true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

}