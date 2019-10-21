package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.domain.component.IlluminationSettingUpdater;
import jp.pioneer.carsync.domain.model.BtPhoneColor;
import jp.pioneer.carsync.domain.model.DimmerSetting;
import jp.pioneer.carsync.domain.model.DimmerTimeType;
import jp.pioneer.carsync.domain.model.IlluminationColor;
import jp.pioneer.carsync.domain.model.IlluminationSettingStatus;
import jp.pioneer.carsync.domain.model.IlluminationTarget;
import jp.pioneer.carsync.domain.model.SphBtPhoneColorSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/06/22.
 */
@RunWith(Theories.class)
public class PreferIlluminationTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public ExpectedException mException = ExpectedException.none();
    @InjectMocks PreferIllumination mPreferIllumination;
    @Mock Handler mHandler;
    @Mock StatusHolder mStatusHolder;
    @Mock IlluminationSettingUpdater mUpdater;

    IlluminationSettingStatus mIlluminationSettingStatus;
    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);

    @DataPoints
    public static final IlluminationTarget[] TARGETS = IlluminationTarget.values();

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
            new WithInRangeFixture(IlluminationTarget.DISP, 0, 0, 10),
            new WithInRangeFixture(IlluminationTarget.KEY, 60, 60, 60),
            new WithInRangeFixture(IlluminationTarget.KEY, 10, 0, 0),
            new WithInRangeFixture(IlluminationTarget.KEY, 0, 10, 0),
            new WithInRangeFixture(IlluminationTarget.KEY, 0, 0, 10)
    };

    @DataPoints
    public static final OutOfRangeFixture[] OUT_OF_RANGE_FIXTURES = {
            new OutOfRangeFixture(IlluminationTarget.DISP, -1, -1, -1),
            new OutOfRangeFixture(IlluminationTarget.DISP, 61, 61, 61),
            new OutOfRangeFixture(IlluminationTarget.KEY, -1, -1, -1),
            new OutOfRangeFixture(IlluminationTarget.KEY, 61, 61, 61)
    };

    @DataPoints
    public static final NotOverTenFixture[] NOT_OVER_TEN_FIXTURES = {
            new NotOverTenFixture(IlluminationTarget.DISP, 0, 0, 0),
            new NotOverTenFixture(IlluminationTarget.DISP, 9, 0, 0),
            new NotOverTenFixture(IlluminationTarget.DISP, 0, 9, 0),
            new NotOverTenFixture(IlluminationTarget.DISP, 0, 0, 9),
            new NotOverTenFixture(IlluminationTarget.KEY, 0, 0, 0),
            new NotOverTenFixture(IlluminationTarget.KEY, 9, 0, 0),
            new NotOverTenFixture(IlluminationTarget.KEY, 0, 9, 0),
            new NotOverTenFixture(IlluminationTarget.KEY, 0, 0, 9)
    };


    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        when(mHandler.sendMessageAtTime(any(Message.class), anyLong())).then(invocationOnMock -> {
            Message message = (Message) invocationOnMock.getArguments()[0];
            mMainHandler.post(() -> {
                message.getCallback().run();
                mSignal.countDown();
            });
            return true;
        });

        mIlluminationSettingStatus = new IlluminationSettingStatus();
        when(mStatusHolder.getIlluminationSettingStatus()).thenReturn(mIlluminationSettingStatus);

        mSignal = new CountDownLatch(1);
    }

    @Theory
    public void setColor_HappyPath(IlluminationTarget target) throws Exception {
        // setup
        switch (target) {
            case DISP:
                mIlluminationSettingStatus.dispColorSettingEnabled = true;
                break;
            case KEY:
                mIlluminationSettingStatus.keyColorSettingEnabled = true;
                break;
        }

        // exercise
        mPreferIllumination.setColor(target, IlluminationColor.WHITE);
        mSignal.await();

        // verify
        verify(mUpdater).setColor(target, IlluminationColor.WHITE);
    }

    @Theory
    public void setColor_Disabled(IlluminationTarget target) throws Exception {
        // exercise
        mPreferIllumination.setColor(target, IlluminationColor.WHITE);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setColor(any(IlluminationTarget.class), any(IlluminationColor.class));
    }

    @Test(expected = NullPointerException.class)
    public void setColor_ArgTargetNull() throws Exception {
        // exercise
        mPreferIllumination.setColor(null, IlluminationColor.WHITE);
    }

    @Test(expected = NullPointerException.class)
    public void setColor_ArgColorNull() throws Exception {
        // exercise
        mPreferIllumination.setColor(IlluminationTarget.DISP, null);
    }

    @Theory
    public void setCustomColor_HappyPath(WithInRangeFixture fixture) throws Exception {
        // setup
        switch (fixture.target) {
            case DISP:
                mIlluminationSettingStatus.colorCustomDispSettingEnabled = true;
                break;
            case KEY:
                mIlluminationSettingStatus.colorCustomKeySettingEnabled = true;
                break;
        }

        // exercise
        mPreferIllumination.setCustomColor(fixture.target, fixture.red, fixture.green, fixture.blue);
        mSignal.await();

        // verify
        verify(mUpdater).setCustomColor(fixture.target, fixture.red, fixture.green, fixture.blue);
    }

    @Theory
    public void setCustomColor_Disabled(WithInRangeFixture fixture) throws Exception {
        // exercise
        mPreferIllumination.setCustomColor(fixture.target, fixture.red, fixture.green, fixture.blue);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setCustomColor(any(IlluminationTarget.class), anyInt(), anyInt(), anyInt());
    }

    @Test(expected = NullPointerException.class)
    public void setCustomColor_ArgNull() throws Exception {
        // exercise
        mPreferIllumination.setCustomColor(null, 30, 30, 30);
    }

    @Theory
    public void setCustomColor_RGBValueOutOfRange(OutOfRangeFixture fixture) throws Exception {
        // exercise
        mException.expect(IllegalArgumentException.class);
        mPreferIllumination.setCustomColor(fixture.target, fixture.red, fixture.green, fixture.blue);
    }

    @Theory
    public void setCustomColor_NotRGBValueTen(NotOverTenFixture fixture) throws Exception {
        // exercise
        mException.expect(IllegalArgumentException.class);
        mPreferIllumination.setCustomColor(fixture.target, fixture.red, fixture.green, fixture.blue);
    }

    @Test
    public void setBtPhoneColor_HappyPath() throws Exception {
        // setup
        mIlluminationSettingStatus.btPhoneColorSettingEnabled = true;

        // exercise
        mPreferIllumination.setBtPhoneColor(BtPhoneColor.FLASHING);
        mSignal.await();

        // verify
        verify(mUpdater).setBtPhoneColor(BtPhoneColor.FLASHING);
    }

    @Test
    public void setBtPhoneColor_Disabled() throws Exception {
        // exercise
        mPreferIllumination.setBtPhoneColor(BtPhoneColor.FLASHING);

        // verify
        verify(mUpdater, never()).setBtPhoneColor(any(BtPhoneColor.class));
    }

    @Test(expected = NullPointerException.class)
    public void setBtPhoneColor_ArgNull() throws Exception {
        // exercise
        mPreferIllumination.setBtPhoneColor(null);
    }

    @Test
    public void setDimmer_HappyPath() throws Exception {
        // setup
        mIlluminationSettingStatus.dimmerSettingEnabled = true;

        // exercise
        mPreferIllumination.setDimmer(DimmerSetting.Dimmer.ON);
        mSignal.await();

        // verify
        verify(mUpdater).setDimmer(DimmerSetting.Dimmer.ON);
    }

    @Test
    public void setDimmer_Disabled() throws Exception {
        // exercise
        mPreferIllumination.setDimmer(DimmerSetting.Dimmer.ON);

        // verify
        verify(mUpdater, never()).setDimmer(any(DimmerSetting.Dimmer.class));
    }

    @Test(expected = NullPointerException.class)
    public void setDimmer_ArgNull() throws Exception {
        // exercise
        mPreferIllumination.setDimmer(null);
    }

    @Test
    public void setDimmerTime_HappyPath() throws Exception {
        // setup
        mIlluminationSettingStatus.dimmerSettingEnabled = true;

        // exercise
        mPreferIllumination.setDimmerTime(DimmerTimeType.START_TIME, 10, 10);
        mSignal.await();

        // verify
        verify(mUpdater).setDimmerTime(DimmerTimeType.START_TIME, 10, 10);
    }

    @Test
    public void setDimmerTime_Disabled() throws Exception {
        // exercise
        mPreferIllumination.setDimmerTime(DimmerTimeType.START_TIME, 10, 10);

        // verify
        verify(mUpdater, never()).setDimmerTime(any(DimmerTimeType.class), anyInt(), anyInt());
    }

    @Test(expected = NullPointerException.class)
    public void setDimmerTime_ArgNull() throws Exception {
        // exercise
        mPreferIllumination.setDimmerTime(null, 10, 10);
    }

    @Theory
    public void setBrightness_HappyPath(IlluminationTarget target) throws Exception {
        // setup
        switch (target) {
            case DISP:
                mIlluminationSettingStatus.dispBrightnessSettingEnabled = true;
                break;
            case KEY:
                mIlluminationSettingStatus.keyBrightnessSettingEnabled = true;
                break;
        }

        // exercise
        mPreferIllumination.setBrightness(target, 10);
        mSignal.await();

        // verify
        verify(mUpdater).setBrightness(target, 10);
    }

    @Theory
    public void setBrightness_Disabled(IlluminationTarget target) throws Exception {
        // exercise
        mPreferIllumination.setBrightness(target, 10);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setBrightness(any(IlluminationTarget.class), anyInt());
    }

    @Test(expected = NullPointerException.class)
    public void setBrightness_ArgNull() throws Exception {
        // exercise
        mPreferIllumination.setBrightness(null, 10);
    }

    @Test
    public void setCommonBrightness_HappyPath() throws Exception {
        // setup
        mIlluminationSettingStatus.brightnessSettingEnabled = true;

        // exercise
        mPreferIllumination.setCommonBrightness(10);
        mSignal.await();

        // verify
        verify(mUpdater).setCommonBrightness(10);
    }

    @Test
    public void setCommonBrightness_Disabled() throws Exception {
        // exercise
        mPreferIllumination.setCommonBrightness(10);

        // verify
        verify(mUpdater, never()).setCommonBrightness(anyInt());
    }

    @Test
    public void setIlluminationEffect_HappyPath() throws Exception {
        // setup
        mIlluminationSettingStatus.hotaruNoHikariLikeSettingEnabled = true;

        // exercise
        mPreferIllumination.setIlluminationEffect(true);
        mSignal.await();

        // verify
        verify(mUpdater).setIlluminationEffect(true);
    }

    @Test
    public void setIlluminationEffect_Disabled() throws Exception {
        // exercise
        mPreferIllumination.setIlluminationEffect(true);

        // verify
        verify(mUpdater, never()).setIlluminationEffect(anyBoolean());
    }

    @Test
    public void setAudioLevelMeterLinked() throws Exception {
        // setup
        mIlluminationSettingStatus.audioLevelMeterLinkedSettingEnabled = true;

        // exercise
        mPreferIllumination.setAudioLevelMeterLinked(true);
        mSignal.await();

        // verify
        verify(mUpdater).setAudioLevelMeterLinked(true);
    }

    @Test
    public void setAudioLevelMeterLinked_Disabled() throws Exception {
        // exercise
        mPreferIllumination.setAudioLevelMeterLinked(true);

        // verify
        verify(mUpdater, never()).setAudioLevelMeterLinked(anyBoolean());
    }

    @Test
    public void setSphBtPhoneColor() throws Exception {
        // setup
        mIlluminationSettingStatus.sphBtPhoneColorSettingEnabled = true;

        // exercise
        mPreferIllumination.setSphBtPhoneColor(SphBtPhoneColorSetting.ORANGE);
        mSignal.await();

        // verify
        verify(mUpdater).setSphBtPhoneColor(SphBtPhoneColorSetting.ORANGE);
    }

    @Test
    public void setSphBtPhoneColor_Disabled() throws Exception {
        // exercise
        mPreferIllumination.setSphBtPhoneColor(SphBtPhoneColorSetting.ORANGE);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setSphBtPhoneColor(any(SphBtPhoneColorSetting.class));
    }

    @Test(expected = NullPointerException.class)
    public void setSphBtPhoneColor_ArgNull() throws Exception {
        // exercise
        mPreferIllumination.setSphBtPhoneColor(null);
    }

    @Test
    public void setCommonColor() throws Exception {
        // setup
        mIlluminationSettingStatus.commonColorSettingEnabled = true;

        // exercise
        mPreferIllumination.setCommonColor(IlluminationColor.ORANGE);
        mSignal.await();

        // verify
        verify(mUpdater).setCommonColor(IlluminationColor.ORANGE);
    }

    @Test
    public void setCommonColor_Disabled() throws Exception {
        // exercise
        mPreferIllumination.setCommonColor(IlluminationColor.ORANGE);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setCommonColor(any(IlluminationColor.class));
    }

    @Test(expected = NullPointerException.class)
    public void setCommonColor_ArgNull() throws Exception {
        // exercise
        mPreferIllumination.setCommonColor(null);
    }

    @Theory
    public void setCommonCustomColor_HappyPath(WithInRangeFixture fixture) throws Exception {
        // setup
        mIlluminationSettingStatus.commonColorCustomSettingEnabled = true;

        // exercise
        mPreferIllumination.setCommonCustomColor(fixture.red, fixture.green, fixture.blue);
        mSignal.await();

        // verify
        verify(mUpdater).setCommonCustomColor(fixture.red, fixture.green, fixture.blue);
    }

    @Theory
    public void setCommonCustomColor_Disabled(WithInRangeFixture fixture) throws Exception {
        // exercise
        mPreferIllumination.setCommonCustomColor(fixture.red, fixture.green, fixture.blue);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setCommonCustomColor(anyInt(), anyInt(), anyInt());
    }

    @Theory
    public void setCommonCustomColor_RGBValueOutOfRange(OutOfRangeFixture fixture) throws Exception {
        // exercise
        mException.expect(IllegalArgumentException.class);
        mPreferIllumination.setCommonCustomColor(fixture.red, fixture.green, fixture.blue);
    }

    @Theory
    public void setCommonCustomColor_NotRGBValueTen(NotOverTenFixture fixture) throws Exception {
        // exercise
        mException.expect(IllegalArgumentException.class);
        mPreferIllumination.setCommonCustomColor(fixture.red, fixture.green, fixture.blue);
    }

}