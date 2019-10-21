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

import jp.pioneer.carsync.domain.model.FMTunerSetting;
import jp.pioneer.carsync.domain.model.LocalSetting;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PCHManualSetting;
import jp.pioneer.carsync.domain.model.TASetting;
import jp.pioneer.carsync.domain.model.TunerFunctionType;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/06/01.
 */
@RunWith(Theories.class)
public class RadioFunctionSettingUpdaterImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks RadioFunctionSettingUpdaterImpl mRadioFunctionSettingUpdater;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    @Mock OutgoingPacket mOutgoingPacket;

    static class BooleanTestFixture {
        boolean setting;
        int expected;

        BooleanTestFixture(boolean setting, int expected) {
            this.setting = setting;
            this.expected = expected;
        }
    }

    @DataPoints
    public static final BooleanTestFixture[] BOOLEAN_TEST_FIXTURES = new BooleanTestFixture[] {
            new BooleanTestFixture(true, 0x01),
            new BooleanTestFixture(false, 0x00)
    };

    static class LocalTestFixture {
        LocalSetting setting;
        int expected;

        LocalTestFixture(LocalSetting setting, int expected) {
            this.setting = setting;
            this.expected = expected;
        }
    }

    @DataPoints
    public static final LocalTestFixture[] LOCAL_TEST_FIXTURES = new LocalTestFixture[] {
            new LocalTestFixture(LocalSetting.OFF, LocalSetting.OFF.code),
            new LocalTestFixture(LocalSetting.LEVEL1, LocalSetting.LEVEL1.code),
            new LocalTestFixture(LocalSetting.LEVEL2, LocalSetting.LEVEL2.code),
            new LocalTestFixture(LocalSetting.LEVEL3, LocalSetting.LEVEL3.code),
            new LocalTestFixture(LocalSetting.LEVEL4, LocalSetting.LEVEL4.code)
    };

    static class FmTunerTestFixture {
        FMTunerSetting setting;
        int expected;

        FmTunerTestFixture(FMTunerSetting setting, int expected) {
            this.setting = setting;
            this.expected = expected;
        }
    }

    @DataPoints
    public static final FmTunerTestFixture[] FM_TUNER_TEST_FIXTURES = new FmTunerTestFixture[] {
            new FmTunerTestFixture(FMTunerSetting.MUSIC, FMTunerSetting.MUSIC.code),
            new FmTunerTestFixture(FMTunerSetting.STANDARD, FMTunerSetting.STANDARD.code),
            new FmTunerTestFixture(FMTunerSetting.TALK, FMTunerSetting.TALK.code)
    };

    static class TaTestFixture {
        TASetting setting;
        int expected;

        TaTestFixture(TASetting setting, int expected) {
            this.setting = setting;
            this.expected = expected;
        }
    }

    @DataPoints
    public static final TaTestFixture[] TA_TEST_FIXTURES = new TaTestFixture[] {
            new TaTestFixture(TASetting.OFF, TASetting.OFF.code),
            new TaTestFixture(TASetting.RDS_TA_ON, TASetting.RDS_TA_ON.code),
            new TaTestFixture(TASetting.DAB_RDS_TA_ON, TASetting.DAB_RDS_TA_ON.code)
    };

    static class PchManualTestFixture {
        PCHManualSetting setting;
        int expected;

        PchManualTestFixture(PCHManualSetting setting, int expected) {
            this.setting = setting;
            this.expected = expected;
        }
    }

    @DataPoints
    public static final PchManualTestFixture[] PCH_MANUAL_TEST_FIXTURES = new PchManualTestFixture[] {
            new PchManualTestFixture(PCHManualSetting.MANUAL, PCHManualSetting.MANUAL.code),
            new PchManualTestFixture(PCHManualSetting.PCH, PCHManualSetting.PCH.code)
    };

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        when(mPacketBuilder.createFunctionSettingNotification(any(MediaSourceType.class), anyInt(), anyInt())).thenReturn(mOutgoingPacket);
    }

    @Theory
    public void setLocal(LocalTestFixture fixture) throws Exception {
        // exercise
        mRadioFunctionSettingUpdater.setLocal(fixture.setting);

        // verify
        verify(mPacketBuilder).createFunctionSettingNotification(MediaSourceType.RADIO, TunerFunctionType.LOCAL.code, fixture.expected);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Theory
    public void setFmTuner(FmTunerTestFixture fixture) throws Exception {
        // exercise
        mRadioFunctionSettingUpdater.setFmTuner(fixture.setting);

        // verify
        verify(mPacketBuilder).createFunctionSettingNotification(MediaSourceType.RADIO, TunerFunctionType.FM_TUNER_SETTING.code, fixture.expected);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Theory
    public void setReg(BooleanTestFixture fixture) throws Exception {
        // exercise
        mRadioFunctionSettingUpdater.setReg(fixture.setting);

        // verify
        verify(mPacketBuilder).createFunctionSettingNotification(MediaSourceType.RADIO, TunerFunctionType.REG.code, fixture.expected);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Theory
    public void setTa(TaTestFixture fixture) throws Exception {
        // exercise
        mRadioFunctionSettingUpdater.setTa(fixture.setting);

        // verify
        verify(mPacketBuilder).createFunctionSettingNotification(MediaSourceType.RADIO, TunerFunctionType.TA.code, fixture.expected);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Theory
    public void setAf(BooleanTestFixture fixture) throws Exception {
        // exercise
        mRadioFunctionSettingUpdater.setAf(fixture.setting);

        // verify
        verify(mPacketBuilder).createFunctionSettingNotification(MediaSourceType.RADIO, TunerFunctionType.AF.code, fixture.expected);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Theory
    public void setNews(BooleanTestFixture fixture) throws Exception {
        // exercise
        mRadioFunctionSettingUpdater.setNews(fixture.setting);

        // verify
        verify(mPacketBuilder).createFunctionSettingNotification(MediaSourceType.RADIO, TunerFunctionType.NEWS.code, fixture.expected);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Theory
    public void setAlarm(BooleanTestFixture fixture) throws Exception {
        // exercise
        mRadioFunctionSettingUpdater.setAlarm(fixture.setting);

        // verify
        verify(mPacketBuilder).createFunctionSettingNotification(MediaSourceType.RADIO, TunerFunctionType.ALARM.code, fixture.expected);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Theory
    public void setPchManual(PchManualTestFixture fixture) throws Exception {
        // exercise
        mRadioFunctionSettingUpdater.setPchManual(fixture.setting);

        // verify
        verify(mPacketBuilder).createFunctionSettingNotification(MediaSourceType.RADIO, TunerFunctionType.PCH_MANUAL.code, fixture.expected);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

}