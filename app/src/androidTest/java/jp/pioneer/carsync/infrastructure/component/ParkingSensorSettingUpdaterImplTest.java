package jp.pioneer.carsync.infrastructure.component;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.model.AlarmOutputDestinationSetting;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/11/13.
 */
public class ParkingSensorSettingUpdaterImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public ExpectedException mException = ExpectedException.none();
    @InjectMocks ParkingSensorSettingUpdaterImpl mParkingSensorSettingUpdater;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    @Mock OutgoingPacket mOutgoingPacket;


    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void setParkingSensorSetting() throws Exception {
        // setup
        when(mPacketBuilder.createParkingSensorSettingNotification(true)).thenReturn(mOutgoingPacket);

        // exercise
        mParkingSensorSettingUpdater.setParkingSensorSetting(true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setAlarmOutputDestination() throws Exception {
        // setup
        when(mPacketBuilder.createAlarmOutputDestinationSettingNotification(AlarmOutputDestinationSetting.FRONT)).thenReturn(mOutgoingPacket);

        // exercise
        mParkingSensorSettingUpdater.setAlarmOutputDestination(AlarmOutputDestinationSetting.FRONT);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setAlarmOutputDestination_ArgNull() throws Exception {
        // exercise
        mParkingSensorSettingUpdater.setAlarmOutputDestination(null);
    }

    @Test
    public void setAlarmVolume() throws Exception {
        // setup
        when(mPacketBuilder.createAlarmVolumeSettingNotification(5)).thenReturn(mOutgoingPacket);

        // exercise
        mParkingSensorSettingUpdater.setAlarmVolume(5);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

}