package jp.pioneer.carsync.infrastructure.component;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.model.AmStep;
import jp.pioneer.carsync.domain.model.FmStep;
import jp.pioneer.carsync.domain.model.MenuDisplayLanguageType;
import jp.pioneer.carsync.domain.model.RearOutputPreoutOutputSetting;
import jp.pioneer.carsync.domain.model.RearOutputSetting;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/25.
 */
public class InitialSettingUpdaterImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks InitialSettingUpdaterImpl mInitialSettingUpdater;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    @Mock OutgoingPacket mOutgoingPacket;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void setFmStep() throws Exception {
        // setup
        when(mPacketBuilder.createFmStepSettingNotification(FmStep._50KHZ)).thenReturn(mOutgoingPacket);

        // exercise
        mInitialSettingUpdater.setFmStep(FmStep._50KHZ);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setAmStep() throws Exception {
        // setup
        when(mPacketBuilder.createAmStepSettingNotification(AmStep._10KHZ)).thenReturn(mOutgoingPacket);

        // exercise
        mInitialSettingUpdater.setAmStep(AmStep._10KHZ);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setRearOutputPreoutOutput() throws Exception {
        // setup
        when(mPacketBuilder.createRearOutputPreoutOutputSettingNotification(RearOutputPreoutOutputSetting.REAR_SUBWOOFER)).thenReturn(mOutgoingPacket);

        // exercise
        mInitialSettingUpdater.setRearOutputPreoutOutput(RearOutputPreoutOutputSetting.REAR_SUBWOOFER);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setRearOutput() throws Exception {
        // setup
        when(mPacketBuilder.createRearOutputSettingNotification(RearOutputSetting.REAR)).thenReturn(mOutgoingPacket);

        // exercise
        mInitialSettingUpdater.setRearOutput(RearOutputSetting.REAR);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setMenuDisplayLanguage() throws Exception {
        // setup
        when(mPacketBuilder.createMenuDisplayLanguageSettingNotification(MenuDisplayLanguageType.CANADIAN_FRENCH)).thenReturn(mOutgoingPacket);

        // exercise
        mInitialSettingUpdater.setMenuDisplayLanguage(MenuDisplayLanguageType.CANADIAN_FRENCH);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

}