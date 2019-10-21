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
 * Created by NSW00_008320 on 2017/06/01.
 */
public class PandoraSourceControllerImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks PandoraSourceControllerImpl mPandoraSourceController;
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
        mPandoraSourceController.togglePlay();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.PAUSE);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);

    }

    @Test
    public void skipNextTrack() throws Exception {
        // exercise
        mPandoraSourceController.skipNextTrack();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.CROSS_RIGHT);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);

    }

    @Test
    public void setThumbUp() throws Exception {
        // exercise
        mPandoraSourceController.setThumbUp();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.PRESET_KEY_1);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);

    }

    @Test
    public void setThumbDown() throws Exception {
        // exercise
        mPandoraSourceController.setThumbDown();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.PRESET_KEY_2);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);

    }

}