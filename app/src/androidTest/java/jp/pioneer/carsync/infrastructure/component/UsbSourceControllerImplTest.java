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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/06/01.
 */
public class UsbSourceControllerImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks UsbSourceControllerImpl mUsbSourceController;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    @Mock OutgoingPacket mOutgoingPacket;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        when(mPacketBuilder.createDeviceControlCommand(any(CarDeviceControlCommand.class))).thenReturn(mOutgoingPacket);
    }

    @Test
    public void togglePlay() throws Exception {
        // exercise
        mUsbSourceController.togglePlay();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.PAUSE);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void toggleRepeatMode() throws Exception {
        // exercise
        mUsbSourceController.toggleRepeatMode();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.PRESET_KEY_6);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void toggleShuffleMode() throws Exception {
        // exercise
        mUsbSourceController.toggleShuffleMode();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.PRESET_KEY_5);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void skipNextTrack() throws Exception {
        // exercise
        mUsbSourceController.skipNextTrack();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.CROSS_RIGHT);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void skipPreviousTrack() throws Exception {
        // exercise
        mUsbSourceController.skipPreviousTrack();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.CROSS_LEFT);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

}