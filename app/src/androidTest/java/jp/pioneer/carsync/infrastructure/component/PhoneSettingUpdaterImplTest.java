package jp.pioneer.carsync.infrastructure.component;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

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
public class PhoneSettingUpdaterImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks PhoneSettingUpdaterImpl mPhoneSettingUpdater;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    @Mock OutgoingPacket mOutgoingPacket;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void setAutoAnswer() throws Exception {
        // setup
        when(mPacketBuilder.createAutoAnswerSettingNotification(true)).thenReturn(mOutgoingPacket);

        // exercise
        mPhoneSettingUpdater.setAutoAnswer(true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setAutoPairing() throws Exception {
        // setup
        when(mPacketBuilder.createAutoPairingSettingNotification(true)).thenReturn(mOutgoingPacket);

        // exercise
        mPhoneSettingUpdater.setAutoPairing(true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

}