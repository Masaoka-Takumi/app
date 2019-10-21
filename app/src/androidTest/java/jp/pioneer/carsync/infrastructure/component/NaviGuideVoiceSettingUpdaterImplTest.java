package jp.pioneer.carsync.infrastructure.component;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.model.NaviGuideVoiceVolumeSetting;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/25.
 */
public class NaviGuideVoiceSettingUpdaterImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks NaviGuideVoiceSettingUpdaterImpl mNaviGuideVoiceSettingUpdater;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    @Mock OutgoingPacket mOutgoingPacket;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void setNaviGuideVoice() throws Exception {
        // setup
        when(mPacketBuilder.createNaviGuideVoiceSettingNotification(true)).thenReturn(mOutgoingPacket);

        // exercise
        mNaviGuideVoiceSettingUpdater.setNaviGuideVoice(true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setNaviGuideVoiceVolume() throws Exception {
        // setup
        when(mPacketBuilder.createNaviGuideVoiceVolumeSettingNotification(NaviGuideVoiceVolumeSetting.MEDIUM)).thenReturn(mOutgoingPacket);

        // exercise
        mNaviGuideVoiceSettingUpdater.setNaviGuideVoiceVolume(NaviGuideVoiceVolumeSetting.MEDIUM);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setNaviGuideVoiceVolume_ArgNull() throws Exception {
        // exercise
        mNaviGuideVoiceSettingUpdater.setNaviGuideVoiceVolume(null);
    }

}