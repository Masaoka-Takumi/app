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
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PtySearchSetting;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.TunerFunctionType;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/05/29.
 */
@RunWith(Theories.class)
public class RadioSourceControllerImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks RadioSourceControllerImpl mRadioSourceController;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    OutgoingPacket mOutgoingPacket;

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

        mOutgoingPacket = mock(OutgoingPacket.class);
        when(mPacketBuilder.createDeviceControlCommand(any(CarDeviceControlCommand.class))).thenReturn(mOutgoingPacket);
        when(mPacketBuilder.createFunctionSettingNotification(any(MediaSourceType.class), anyInt(), anyInt())).thenReturn(mOutgoingPacket);
    }

    @Test
    public void toggleBand() throws Exception {
        // exercise
        mRadioSourceController.toggleBand();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.BAND_ESC);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Theory
    public void callPreset(CallPresetFixture fixture) throws Exception {
        // exercise
        mRadioSourceController.callPreset(fixture.presetNo);

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(fixture.expected);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = IllegalArgumentException.class)
    public void callPreset_ArgValueMin() throws Exception {
        // exercise
        mRadioSourceController.callPreset(EXCEPTION_VALUE.MIN.value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void callPreset_ArgValueMax() throws Exception {
        // exercise
        mRadioSourceController.callPreset(EXCEPTION_VALUE.MAX.value);
    }

    @Test
    public void selectFavorite() throws Exception {
        // setup
        when(mPacketBuilder.createFavoriteRadioSetCommand(eq(1), eq(RadioBandType.AM.getCode()), eq(3))).thenReturn(mOutgoingPacket);

        // exercise
        mRadioSourceController.selectFavorite(1, RadioBandType.AM, 3);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void selectFavorite_ArgNull() throws Exception {
        // exercise
        mRadioSourceController.selectFavorite(1, null, 3);
    }

    @Test
    public void presetUp() throws Exception {
        // exercise
        mRadioSourceController.manualUp();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.CROSS_UP);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void presetDown() throws Exception {
        // exercise
        mRadioSourceController.manualDown();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.CROSS_DOWN);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void manualUp() throws Exception {
        // exercise
        mRadioSourceController.manualUp();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.CROSS_RIGHT);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void manualDown() throws Exception {
        // exercise
        mRadioSourceController.manualDown();

        // verify
        verify(mPacketBuilder).createDeviceControlCommand(CarDeviceControlCommand.CROSS_LEFT);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }


    @Test
    public void startBsm() throws Exception {
        // exercise
        mRadioSourceController.startBsm();

        // verify
        verify(mPacketBuilder).createFunctionSettingNotification(MediaSourceType.RADIO, TunerFunctionType.BSM.code, 0x01);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void startPtySearch() throws Exception {
        // exercise
        mRadioSourceController.startPtySearch(PtySearchSetting.OTHERS);

        // verify
        verify(mPacketBuilder).createFunctionSettingNotification(MediaSourceType.RADIO, TunerFunctionType.PTY_SEARCH.code, PtySearchSetting.OTHERS.code);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void startPtySearch_ArgNull() throws Exception {
        // exercise
        mRadioSourceController.startPtySearch(null);
    }
}