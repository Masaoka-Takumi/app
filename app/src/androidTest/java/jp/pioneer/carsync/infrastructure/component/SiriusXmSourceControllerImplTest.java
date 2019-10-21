package jp.pioneer.carsync.infrastructure.component;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.model.CarDeviceControlCommand;
import jp.pioneer.carsync.domain.model.SxmBandType;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/06/14.
 */
@RunWith(Theories.class)
public class SiriusXmSourceControllerImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SiriusXmSourceControllerImpl mSiriusXmSourceController;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    @Mock OutgoingPacket mOutgoingPacket;

    enum EXCEPTION_VALUE {
        MIN(0), MAX(7);
        int value;

        EXCEPTION_VALUE(int value) {
            this.value = value;
        }
    }

    static class CallPresetFixture {
        int presetNo;
        CarDeviceControlCommand expected;

        CallPresetFixture(int presetNo, CarDeviceControlCommand expected) {
            this.presetNo = presetNo;
            this.expected = expected;
        }
    }

    @DataPoints
    public static final CallPresetFixture[] PRESET_FIXTURES = new CallPresetFixture[]{
            new CallPresetFixture(1, CarDeviceControlCommand.PRESET_KEY_1),
            new CallPresetFixture(2, CarDeviceControlCommand.PRESET_KEY_2),
            new CallPresetFixture(3, CarDeviceControlCommand.PRESET_KEY_3),
            new CallPresetFixture(4, CarDeviceControlCommand.PRESET_KEY_4),
            new CallPresetFixture(5, CarDeviceControlCommand.PRESET_KEY_5),
            new CallPresetFixture(6, CarDeviceControlCommand.PRESET_KEY_6)
    };

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        when(mPacketBuilder.createDeviceControlCommand(any(CarDeviceControlCommand.class))).thenReturn(mOutgoingPacket);
    }

    @Test
    public void toggleBand() throws Exception {
        // exercise
        mSiriusXmSourceController.toggleBand();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.BAND_ESC);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Theory
    public void callPreset(CallPresetFixture fixture) throws Exception {
        // exercise
        mSiriusXmSourceController.callPreset(fixture.presetNo);

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(fixture.expected);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = IllegalArgumentException.class)
    public void callPreset_ArgValueMin() throws Exception {
        // exercise
        mSiriusXmSourceController.callPreset(EXCEPTION_VALUE.MIN.value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void callPreset_ArgValueMax() throws Exception {
        // exercise
        mSiriusXmSourceController.callPreset(EXCEPTION_VALUE.MAX.value);
    }

    @Test
    public void selectFavorite() throws Exception {
        // setup
        when(mPacketBuilder.createFavoriteSiriusXmSetCommand(eq(1), eq(SxmBandType.SXM1.getCode()), eq(3))).thenReturn(mOutgoingPacket);

        // exercise
        mSiriusXmSourceController.selectFavorite(1, SxmBandType.SXM1, 3);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void selectFavorite_ArgNull() throws Exception {
        // exercise
        mSiriusXmSourceController.selectFavorite(1, null, 3);
    }

    @Test
    public void presetUp() throws Exception {
        // exercise
        mSiriusXmSourceController.presetUp();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.CROSS_UP);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void presetDown() throws Exception {
        // exercise
        mSiriusXmSourceController.presetDown();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.CROSS_DOWN);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void toggleLiveMode() throws Exception {
        // exercise
        mSiriusXmSourceController.toggleLiveMode();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.LIVE);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void toggleChannelModeOrReplayMode() throws Exception {
        // exercise
        mSiriusXmSourceController.toggleChannelModeOrReplayMode();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.REPLAY_MODE);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void toggleTuneMix() throws Exception {
        // exercise
        mSiriusXmSourceController.toggleTuneMix();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.TUNE_MIX);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void releaseSubscriptionUpdating() throws Exception {
        // exercise
        mSiriusXmSourceController.releaseSubscriptionUpdating();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.OFF_HOOK);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

}