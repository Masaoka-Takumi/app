package jp.pioneer.carsync.infrastructure.component;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.model.ListeningPosition;
import jp.pioneer.carsync.domain.model.SmallCarTaSettingType;
import jp.pioneer.carsync.domain.model.SoundEffectSettingType;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/19.
 */
public class SoundFxSettingUpdaterImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SoundFxSettingUpdaterImpl mSoundFxSettingUpdater;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    @Mock OutgoingPacket mOutgoingPacket;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void setLiveSimulation() throws Exception {
        // setup
        when(mPacketBuilder.createLiveSimulationSettingNotification(SoundFieldControlSettingType.DOME, SoundEffectSettingType.CLUB_F)).thenReturn(mOutgoingPacket);

        // exercise
        mSoundFxSettingUpdater.setLiveSimulation(SoundFieldControlSettingType.DOME, SoundEffectSettingType.CLUB_F);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setLiveSimulation_ArgFieldNull() throws Exception {
        // exercise
        mSoundFxSettingUpdater.setLiveSimulation(null, SoundEffectSettingType.CLUB_F);
    }

    @Test(expected = NullPointerException.class)
    public void setLiveSimulation_ArgEffectNull() throws Exception {
        // exercise
        mSoundFxSettingUpdater.setLiveSimulation(SoundFieldControlSettingType.DOME, null);
    }

    @Test
    public void setSuperTodoroki() throws Exception {
        // setup
        when(mPacketBuilder.createSuperTodorokiSettingNotification(SuperTodorokiSetting.SUPER_HIGH)).thenReturn(mOutgoingPacket);

        // exercise
        mSoundFxSettingUpdater.setSuperTodoroki(SuperTodorokiSetting.SUPER_HIGH);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setSuperTodoroki_ArgNull() throws Exception {
        // exercise
        mSoundFxSettingUpdater.setSuperTodoroki(null);
    }

    @Test
    public void setSmallCarTa() throws Exception {
        // setup
        when(mPacketBuilder.createSmallCarTaSettingNotification(SmallCarTaSettingType.C, ListeningPosition.RIGHT)).thenReturn(mOutgoingPacket);

        // exercise
        mSoundFxSettingUpdater.setSmallCarTa(SmallCarTaSettingType.C, ListeningPosition.RIGHT);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setSmallCarTa_ArgTypeNull() throws Exception {
        // exercise
        mSoundFxSettingUpdater.setSmallCarTa(null, ListeningPosition.RIGHT);
    }

    @Test(expected = NullPointerException.class)
    public void setSmallCarTa_ArgPositionNull() throws Exception {
        // exercise
        mSoundFxSettingUpdater.setSmallCarTa(SmallCarTaSettingType.C, null);
    }

    @Test
    public void setKaraokeSetting() throws Exception {
        // setup
        when(mPacketBuilder.createKaraokeSettingNotification(true)).thenReturn(mOutgoingPacket);

        // exercise
        mSoundFxSettingUpdater.setKaraokeSetting(true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setMicVolume() throws Exception {
        // setup
        when(mPacketBuilder.createMicVolumeSettingNotification(2)).thenReturn(mOutgoingPacket);

        // exercise
        mSoundFxSettingUpdater.setMicVolume(2);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setVocalCancel() throws Exception {
        // setup
        when(mPacketBuilder.createVocalCancelSettingNotification(true)).thenReturn(mOutgoingPacket);

        // exercise
        mSoundFxSettingUpdater.setVocalCancel(true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

}