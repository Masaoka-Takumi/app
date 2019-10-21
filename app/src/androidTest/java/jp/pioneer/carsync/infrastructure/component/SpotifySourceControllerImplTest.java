package jp.pioneer.carsync.infrastructure.component;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.model.CarDeviceControlCommand;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/05/29.
 */
public class SpotifySourceControllerImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SpotifySourceControllerImpl mSpotifySourceController;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    OutgoingPacket mOutgoingPacket;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mOutgoingPacket = mock(OutgoingPacket.class);
        when(mPacketBuilder.createDeviceControlCommand(any(CarDeviceControlCommand.class))).thenReturn(mOutgoingPacket);
    }

    @Test
    public void togglePlay() throws Exception {
        // exercise
        mSpotifySourceController.togglePlay();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.PAUSE);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);

    }

    @Test
    public void skipNextTrack() throws Exception {
        // exercise
        mSpotifySourceController.skipNextTrack();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.CROSS_RIGHT);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);

    }

    @Test
    public void skipPreviousTrack() throws Exception {
        // exercise
        mSpotifySourceController.skipPreviousTrack();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.CROSS_LEFT);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);

    }

    @Test
    public void toggleRepeatMode() throws Exception {
        // exercise
        mSpotifySourceController.toggleRepeatMode();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.PRESET_KEY_6);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);

    }

    @Test
    public void toggleShuffleMode() throws Exception {
        // exercise
        mSpotifySourceController.toggleShuffleMode();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.PRESET_KEY_5);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);

    }

    @Test
    public void setThumbUp() throws Exception {
        // exercise
        mSpotifySourceController.setThumbUp();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.PRESET_KEY_1);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);

    }

    @Test
    public void setThumbDown() throws Exception {
        // exercise
        mSpotifySourceController.setThumbDown();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.PRESET_KEY_2);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);

    }

}