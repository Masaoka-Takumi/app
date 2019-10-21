package jp.pioneer.carsync.infrastructure.component;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.model.BtPhoneColor;
import jp.pioneer.carsync.domain.model.DimmerSetting;
import jp.pioneer.carsync.domain.model.DimmerTimeType;
import jp.pioneer.carsync.domain.model.IlluminationColor;
import jp.pioneer.carsync.domain.model.IlluminationTarget;
import jp.pioneer.carsync.domain.model.SphBtPhoneColorSetting;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/06/22.
 */
public class IlluminationSettingUpdaterImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public ExpectedException mException = ExpectedException.none();
    @InjectMocks IlluminationSettingUpdaterImpl mIlluminationSettingUpdater;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    @Mock OutgoingPacket mOutgoingPacket;

    static class Fixture {
        IlluminationTarget target;
        int red;
        int green;
        int blue;

        Fixture(IlluminationTarget target, int red, int green, int blue) {
            this.target = target;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }
    }

    static class WithInRangeFixture extends Fixture {

        WithInRangeFixture(IlluminationTarget target, int red, int green, int blue) {
            super(target, red, green, blue);
        }
    }

    static class OutOfRangeFixture extends Fixture {

        OutOfRangeFixture(IlluminationTarget target, int red, int green, int blue) {
            super(target, red, green, blue);
        }
    }

    static class NotOverTenFixture extends Fixture {

        NotOverTenFixture(IlluminationTarget target, int red, int green, int blue) {
            super(target, red, green, blue);
        }
    }

    @DataPoints
    public static final WithInRangeFixture[] WITH_IN_RANGE_FIXTURES = {
            new WithInRangeFixture(IlluminationTarget.DISP, 60, 60, 60),
            new WithInRangeFixture(IlluminationTarget.DISP, 10, 0, 0),
            new WithInRangeFixture(IlluminationTarget.DISP, 0, 10, 0),
            new WithInRangeFixture(IlluminationTarget.DISP, 0, 0, 10)
    };

    @DataPoints
    public static final OutOfRangeFixture[] OUT_OF_RANGE_FIXTURES = {
            new OutOfRangeFixture(IlluminationTarget.DISP, -1, -1, -1),
            new OutOfRangeFixture(IlluminationTarget.DISP, 61, 61, 61)
    };

    @DataPoints
    public static final NotOverTenFixture[] NOT_OVER_TEN_FIXTURES = {
            new NotOverTenFixture(IlluminationTarget.DISP, 0, 0, 0),
            new NotOverTenFixture(IlluminationTarget.DISP, 9, 0, 0),
            new NotOverTenFixture(IlluminationTarget.DISP, 0, 9, 0),
            new NotOverTenFixture(IlluminationTarget.DISP, 0, 0, 9)
    };

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void setColor() throws Exception {
        // setup
        when(mPacketBuilder.createColorSettingNotification(eq(IlluminationTarget.DISP), eq(IlluminationColor.WHITE))).thenReturn(mOutgoingPacket);

        // exercise
        mIlluminationSettingUpdater.setColor(IlluminationTarget.DISP, IlluminationColor.WHITE);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setColor_ArgTargetNull() throws Exception {
        // exercise
        mIlluminationSettingUpdater.setColor(null, IlluminationColor.WHITE);
    }

    @Test(expected = NullPointerException.class)
    public void setColor_ArgColorNull() throws Exception {
        // exercise
        mIlluminationSettingUpdater.setColor(IlluminationTarget.DISP, null);
    }

    @Theory
    public void setCustomColor(WithInRangeFixture fixture) throws Exception {
        // setup
        when(mPacketBuilder.createCustomColorSettingNotification(eq(fixture.target), eq(fixture.red), eq(fixture.green), eq(fixture.blue))).thenReturn(mOutgoingPacket);

        // exercise
        mIlluminationSettingUpdater.setCustomColor(fixture.target, fixture.red, fixture.green, fixture.blue);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setCustomColor_ArgNull() throws Exception {
        // exercise
        mIlluminationSettingUpdater.setCustomColor(null, 30, 30, 30);
    }

    @Theory
    public void setCustomColor_RGBValueOutOfRange(OutOfRangeFixture fixture) throws Exception {
        // exercise
        mException.expect(IllegalArgumentException.class);
        mIlluminationSettingUpdater.setCustomColor(fixture.target, fixture.red, fixture.green, fixture.blue);
    }

    @Theory
    public void setCustomColor_NotRGBValueTen(NotOverTenFixture fixture) throws Exception {
        // exercise
        mException.expect(IllegalArgumentException.class);
        mIlluminationSettingUpdater.setCustomColor(fixture.target, fixture.red, fixture.green, fixture.blue);
    }

    @Test
    public void setBtPhoneColor() throws Exception {
        // setup
        when(mPacketBuilder.createBtPhoneColorSettingNotification(eq(BtPhoneColor.FLASHING))).thenReturn(mOutgoingPacket);

        // exercise
        mIlluminationSettingUpdater.setBtPhoneColor(BtPhoneColor.FLASHING);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setBtPhoneColor_ArgNull() throws Exception {
        // exercise
        mIlluminationSettingUpdater.setBtPhoneColor(null);
    }

    @Test
    public void setDimmer() throws Exception {
        // setup
        when(mPacketBuilder.createDimmerSettingNotification(eq(DimmerSetting.Dimmer.ON))).thenReturn(mOutgoingPacket);

        // exercise
        mIlluminationSettingUpdater.setDimmer(DimmerSetting.Dimmer.ON);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setDimmer_ArgNull() throws Exception {
        // exercise
        mIlluminationSettingUpdater.setDimmer(null);
    }

    @Test
    public void setDimmerTime() throws Exception {
        // setup
        when(mPacketBuilder.createDimmerTimeSettingNotification(eq(DimmerTimeType.START_TIME), eq(10), eq(10))).thenReturn(mOutgoingPacket);

        // exercise
        mIlluminationSettingUpdater.setDimmerTime(DimmerTimeType.START_TIME, 10, 10);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setDimmerTime_ArgNull() throws Exception {
        // exercise
        mIlluminationSettingUpdater.setDimmerTime(null, 10, 10);
    }

    @Test
    public void setBrightness() throws Exception {
        // setup
        when(mPacketBuilder.createKeyDisplayBrightnessSettingNotification(eq(IlluminationTarget.DISP), eq(10))).thenReturn(mOutgoingPacket);

        // exercise
        mIlluminationSettingUpdater.setBrightness(IlluminationTarget.DISP, 10);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setBrightness_ArgNull() throws Exception {
        // exercise
        mIlluminationSettingUpdater.setBrightness(null, 10);
    }

    @Test
    public void setCommonBrightness() throws Exception {
        // setup
        when(mPacketBuilder.createBrightnessSettingNotification(eq(10))).thenReturn(mOutgoingPacket);

        // exercise
        mIlluminationSettingUpdater.setCommonBrightness(10);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setIlluminationEffect() throws Exception {
        // setup
        when(mPacketBuilder.createIlluminationEffectSettingNotification(eq(true))).thenReturn(mOutgoingPacket);

        // exercise
        mIlluminationSettingUpdater.setIlluminationEffect(true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setAudioLevelMeterLinked() throws Exception {
        // setup
        when(mPacketBuilder.createAudioLevelMeterLinkedSettingNotification(eq(true))).thenReturn(mOutgoingPacket);

        // exercise
        mIlluminationSettingUpdater.setAudioLevelMeterLinked(true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setSphBtPhoneColor() throws Exception {
        // setup
        when(mPacketBuilder.createSphBtPhoneColorSettingNotification(eq(SphBtPhoneColorSetting.ORANGE))).thenReturn(mOutgoingPacket);

        // exercise
        mIlluminationSettingUpdater.setSphBtPhoneColor(SphBtPhoneColorSetting.ORANGE);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setSphBtPhoneColor_ArgNull() throws Exception {
        // exercise
        mIlluminationSettingUpdater.setSphBtPhoneColor(null);
    }

    @Test
    public void setCommonColor() throws Exception {
        // setup
        when(mPacketBuilder.createCommonColorSettingNotification(eq(IlluminationColor.PURE_GREEN))).thenReturn(mOutgoingPacket);

        // exercise
        mIlluminationSettingUpdater.setCommonColor(IlluminationColor.PURE_GREEN);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setCommonColor_ArgNull() throws Exception {
        // exercise
        mIlluminationSettingUpdater.setCommonColor(null);
    }

    @Theory
    public void setCommonCustomColor(WithInRangeFixture fixture) throws Exception {
        // setup
        when(mPacketBuilder.createCommonCustomColorSettingNotification(eq(fixture.red), eq(fixture.green), eq(fixture.blue))).thenReturn(mOutgoingPacket);

        // exercise
        mIlluminationSettingUpdater.setCommonCustomColor(fixture.red, fixture.green, fixture.blue);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Theory
    public void setCommonCustomColor_RGBValueOutOfRange(OutOfRangeFixture fixture) throws Exception {
        // exercise
        mException.expect(IllegalArgumentException.class);
        mIlluminationSettingUpdater.setCommonCustomColor(fixture.red, fixture.green, fixture.blue);
    }

    @Theory
    public void setCommonCustomColor_NotRGBValueTen(NotOverTenFixture fixture) throws Exception {
        // exercise
        mException.expect(IllegalArgumentException.class);
        mIlluminationSettingUpdater.setCommonCustomColor(fixture.red, fixture.green, fixture.blue);
    }

}