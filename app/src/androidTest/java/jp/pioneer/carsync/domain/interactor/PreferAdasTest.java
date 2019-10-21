package jp.pioneer.carsync.domain.interactor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theory;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.model.AdasCalibrationSetting;
import jp.pioneer.carsync.domain.model.AdasCameraSetting;
import jp.pioneer.carsync.domain.model.AdasFunctionSensitivity;
import jp.pioneer.carsync.domain.model.AdasFunctionSetting;
import jp.pioneer.carsync.domain.model.AdasFunctionType;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/19.
 */
public class PreferAdasTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks PreferAdas mPreferAdas;
    @Mock AppSharedPreference mPreference;

    AdasCalibrationSetting mAdasCalibrationSetting;
    AdasFunctionSetting mLdwFunctionSetting;
    AdasFunctionSetting mPcwFunctionSetting;
    AdasFunctionSetting mFcwFunctionSetting;
    AdasFunctionSetting mLkwFunctionSetting;

    @DataPoints
    public static final AdasFunctionType[] FUNCTION_TYPES = AdasFunctionType.values();

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mAdasCalibrationSetting = new AdasCalibrationSetting();
        mLdwFunctionSetting = new AdasFunctionSetting(AdasFunctionType.LDW);
        mPcwFunctionSetting = new AdasFunctionSetting(AdasFunctionType.PCW);
        mFcwFunctionSetting = new AdasFunctionSetting(AdasFunctionType.FCW);
        mLkwFunctionSetting = new AdasFunctionSetting(AdasFunctionType.LKW);

        when(mPreference.getAdasCalibrationSetting()).thenReturn(mAdasCalibrationSetting);
        when(mPreference.getAdasLdwSetting()).thenReturn(mLdwFunctionSetting);
        when(mPreference.getAdasPcwSetting()).thenReturn(mPcwFunctionSetting);
        when(mPreference.getAdasFcwSetting()).thenReturn(mFcwFunctionSetting);
        when(mPreference.getAdasLkwSetting()).thenReturn(mLkwFunctionSetting);
    }

    @Test
    public void toggleAdasEnabled() throws Exception {
        // setup
        when(mPreference.isAdasEnabled()).thenReturn(true);

        // exercise
        mPreferAdas.setAdasEnabled(false);

        // verify
        verify(mPreference).setAdasEnabled(false);
    }

    @Test
    public void getAdasEnabled() throws Exception {
        // setup
        when(mPreference.isAdasEnabled()).thenReturn(true);

        // exercise
        boolean actual = mPreferAdas.getAdasEnabled();

        // verify
        assertThat(actual, is(true));
    }

    @Test
    public void setCameraHeight() throws Exception {
        // setup
        ArgumentCaptor<AdasCameraSetting> captor = ArgumentCaptor.forClass(AdasCameraSetting.class);
        AdasCameraSetting setting = new AdasCameraSetting();

        // exercise
        mPreferAdas.setAdasCameraSetting(setting);

        // verify
        verify(mPreference).setAdasCameraSetting(captor.capture());
        assertThat(captor.getValue().cameraHeight, is(1000));
    }

    @Test
    public void setCalibration() throws Exception {
        // setup
        ArgumentCaptor<AdasCalibrationSetting> captor = ArgumentCaptor.forClass(AdasCalibrationSetting.class);
        AdasCalibrationSetting setting = new AdasCalibrationSetting();

        // exercise
        mPreferAdas.setAdasCalibrationSetting(setting);

        // verify
        verify(mPreference).setAdasCalibrationSetting(captor.capture());
        assertThat(captor.getValue().landscapeCalibrationPx, is(4000));
    }

    @Theory
    public void toggleFunctionEnabled(AdasFunctionType type) throws Exception {
        // setup
        ArgumentCaptor<AdasFunctionSetting> captor = ArgumentCaptor.forClass(AdasFunctionSetting.class);

        // exercise
        mPreferAdas.setFunctionEnabled(type, true);

        // verify
        switch(type){
            case FCW:
                verify(mPreference).setAdasFcwSetting(captor.capture());
                break;
            case LDW:
                verify(mPreference).setAdasLdwSetting(captor.capture());
                break;
            case LKW:
                verify(mPreference).setAdasLdwSetting(captor.capture());
                break;
            case PCW:
                verify(mPreference).setAdasPcwSetting(captor.capture());
                break;
        }

        assertThat(captor.getValue().settingEnabled, is(true));
    }

    @Theory
    public void setFunctionSensitivity(AdasFunctionType type) throws Exception {
        // setup
        ArgumentCaptor<AdasFunctionSetting> captor = ArgumentCaptor.forClass(AdasFunctionSetting.class);

        // exercise
        mPreferAdas.setFunctionSensitivity(type, AdasFunctionSensitivity.HIGH);

        // verify
        switch(type){
            case FCW:
                verify(mPreference).setAdasFcwSetting(captor.capture());
                break;
            case LDW:
                verify(mPreference).setAdasLdwSetting(captor.capture());
                break;
            case LKW:
                verify(mPreference).setAdasLdwSetting(captor.capture());
                break;
            case PCW:
                verify(mPreference).setAdasPcwSetting(captor.capture());
                break;
        }

        assertThat(captor.getValue().functionSensitivity, is(AdasFunctionSensitivity.HIGH));
    }

    @Test
    public void getCalibrationSetting() throws Exception {
        // exercise
        AdasCalibrationSetting actual = mPreferAdas.getAdasCalibrationSetting();

        // verify
        assertThat(actual, is(mAdasCalibrationSetting));
    }

    @Theory
    public void getFunctionSetting(AdasFunctionType type) throws Exception {
        // exercise
        AdasFunctionSetting actual = mPreferAdas.getFunctionSetting(type);

        // verify
        switch(type){
            case FCW:
                assertThat(actual, is(mFcwFunctionSetting));
                break;
            case LDW:
                assertThat(actual, is(mLdwFunctionSetting));
                break;
            case LKW:
                assertThat(actual, is(mLkwFunctionSetting));
                break;
            case PCW:
                assertThat(actual, is(mPcwFunctionSetting));
                break;
        }
    }

}