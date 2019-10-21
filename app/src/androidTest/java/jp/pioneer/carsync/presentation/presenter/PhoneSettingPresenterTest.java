package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.content.res.Resources;

import org.greenrobot.eventbus.EventBus;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.IlluminationSettingChangeEvent;
import jp.pioneer.carsync.domain.event.IlluminationSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.PhoneSettingChangeEvent;
import jp.pioneer.carsync.domain.event.PhoneSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.ChangeScreen;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferPhone;
import jp.pioneer.carsync.domain.model.BtPhoneColor;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.IlluminationSetting;
import jp.pioneer.carsync.domain.model.IlluminationSettingSpec;
import jp.pioneer.carsync.domain.model.IlluminationSettingStatus;
import jp.pioneer.carsync.domain.model.PhoneSetting;
import jp.pioneer.carsync.domain.model.PhoneSettingStatus;
import jp.pioneer.carsync.domain.model.SphBtPhoneColorSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.PhoneSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2017/09/22.
 */
public class PhoneSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks PhoneSettingPresenter mPresenter;
    @Mock PhoneSettingView mView;
    @Mock GetStatusHolder mGetCase;
    @Mock EventBus mEventBus;
    @Mock PreferPhone mPreferCase;
    @Mock ChangeScreen mScreenCase;
    @Mock AppSharedPreference mPreference;
    @Mock Context mContext;

    Resources mResources = mock(Resources.class);
    StatusHolder mStatusHolder = mock(StatusHolder.class);
    CarDeviceSpec mCarDeviceSpec = new CarDeviceSpec();
    PhoneSetting mPhoneSetting = new PhoneSetting();
    PhoneSettingStatus mPhoneSettingStatus = new PhoneSettingStatus();
    IlluminationSettingSpec mIlluminationSettingSpec = new IlluminationSettingSpec();
    IlluminationSetting mIlluminationSetting = new IlluminationSetting();
    IlluminationSettingStatus mIlluminationSettingStatus = new IlluminationSettingStatus();

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        StatusHolder holder = mGetCase.execute();

        mCarDeviceSpec.illuminationSettingSpec = mIlluminationSettingSpec;
        mCarDeviceSpec.phoneSettingSupported = true;
        mPhoneSetting.autoAnswerSetting = true;
        mPhoneSetting.autoPairingSetting = true;
        mPhoneSettingStatus.autoAnswerSettingEnabled = true;
        mPhoneSettingStatus.autoPairingSettingEnabled = true;
        mPhoneSettingStatus.deviceListEnabled = true;
        mIlluminationSettingSpec.btPhoneColorSettingSupported = true;
        mIlluminationSettingSpec.sphBtPhoneColorSettingSupported = true;
        mIlluminationSetting.btPhoneColor = BtPhoneColor.FLASHING_PATTERN3;
        mIlluminationSetting.sphBtPhoneColorSetting = SphBtPhoneColorSetting.ORANGE;
        mIlluminationSettingStatus.btPhoneColorSettingEnabled = true;
        mIlluminationSettingStatus.sphBtPhoneColorSettingEnabled = true;

        when(mGetCase.execute()).thenReturn(mStatusHolder);
        when(mStatusHolder.getPhoneSetting()).thenReturn(mPhoneSetting);
        when(mStatusHolder.getPhoneSettingStatus()).thenReturn(mPhoneSettingStatus);
        when(mStatusHolder.getIlluminationSetting()).thenReturn(mIlluminationSetting);
        when(mStatusHolder.getIlluminationSettingStatus()).thenReturn(mIlluminationSettingStatus);
        when(mStatusHolder.getCarDeviceSpec()).thenReturn(mCarDeviceSpec);
        when(mPreference.isPhoneBookAccessible()).thenReturn(true);
    }

    @Test
    public void testLifecycle() throws Exception {
        // setup
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);

        // exercise
        mPresenter.onInitialize();
        mPresenter.takeView(mView);
        mPresenter.resume();
        mPresenter.pause();

        // verify
        verify(mView).setDeviceSettings(mCarDeviceSpec.phoneSettingSupported, mPhoneSettingStatus.deviceListEnabled);
        verify(mView).setAutoPairingSetting(mCarDeviceSpec.phoneSettingSupported, mPhoneSettingStatus.autoPairingSettingEnabled,mPhoneSetting.autoPairingSetting);
        verify(mView).setPhoneBookAccessibleSetting(true);
        verify(mView).setIncomingCallPatternSetting(mIlluminationSettingSpec.btPhoneColorSettingSupported, mIlluminationSettingStatus.btPhoneColorSettingEnabled, mIlluminationSetting.btPhoneColor);
        verify(mView).setIncomingCallColorSetting(mIlluminationSettingSpec.sphBtPhoneColorSettingSupported, mIlluminationSettingStatus.sphBtPhoneColorSettingEnabled, mIlluminationSetting.sphBtPhoneColorSetting);
        verify(mView).setAutoAnswerSetting(mCarDeviceSpec.phoneSettingSupported, mPhoneSetting.autoAnswerSetting, mPhoneSettingStatus.autoAnswerSettingEnabled);

        verify(mEventBus).register(mPresenter);
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void onIlluminationSettingChangeEvent() throws Exception {
        // exercise
        mPresenter.onIlluminationSettingChangeEvent(new IlluminationSettingChangeEvent());

        // verify
        verify(mView).setDeviceSettings(mCarDeviceSpec.phoneSettingSupported, mPhoneSettingStatus.deviceListEnabled);
        verify(mView).setAutoPairingSetting(mCarDeviceSpec.phoneSettingSupported, mPhoneSettingStatus.autoPairingSettingEnabled,mPhoneSetting.autoPairingSetting);
        verify(mView).setPhoneBookAccessibleSetting(true);
        verify(mView).setIncomingCallPatternSetting(mIlluminationSettingSpec.btPhoneColorSettingSupported, mIlluminationSettingStatus.btPhoneColorSettingEnabled, mIlluminationSetting.btPhoneColor);
        verify(mView).setIncomingCallColorSetting(mIlluminationSettingSpec.sphBtPhoneColorSettingSupported, mIlluminationSettingStatus.sphBtPhoneColorSettingEnabled, mIlluminationSetting.sphBtPhoneColorSetting);
        verify(mView).setAutoAnswerSetting(mCarDeviceSpec.phoneSettingSupported, mPhoneSetting.autoAnswerSetting, mPhoneSettingStatus.autoAnswerSettingEnabled);
    }

    @Test
    public void onIlluminationSettingStatusChangeEvent() throws Exception {
        // exercise
        mPresenter.onIlluminationSettingStatusChangeEvent(new IlluminationSettingStatusChangeEvent());

        // verify
        verify(mView).setDeviceSettings(mCarDeviceSpec.phoneSettingSupported, mPhoneSettingStatus.deviceListEnabled);
        verify(mView).setAutoPairingSetting(mCarDeviceSpec.phoneSettingSupported, mPhoneSettingStatus.autoPairingSettingEnabled,mPhoneSetting.autoPairingSetting);
        verify(mView).setPhoneBookAccessibleSetting(true);
        verify(mView).setIncomingCallPatternSetting(mIlluminationSettingSpec.btPhoneColorSettingSupported, mIlluminationSettingStatus.btPhoneColorSettingEnabled, mIlluminationSetting.btPhoneColor);
        verify(mView).setIncomingCallColorSetting(mIlluminationSettingSpec.sphBtPhoneColorSettingSupported, mIlluminationSettingStatus.sphBtPhoneColorSettingEnabled, mIlluminationSetting.sphBtPhoneColorSetting);
        verify(mView).setAutoAnswerSetting(mCarDeviceSpec.phoneSettingSupported, mPhoneSetting.autoAnswerSetting, mPhoneSettingStatus.autoAnswerSettingEnabled);
    }

    @Test
    public void onPhoneSettingChangeEvent() throws Exception {
        // exercise
        mPresenter.onPhoneSettingChangeEvent(new PhoneSettingChangeEvent());

        // verify
        verify(mView).setDeviceSettings(mCarDeviceSpec.phoneSettingSupported, mPhoneSettingStatus.deviceListEnabled);
        verify(mView).setAutoPairingSetting(mCarDeviceSpec.phoneSettingSupported, mPhoneSettingStatus.autoPairingSettingEnabled,mPhoneSetting.autoPairingSetting);
        verify(mView).setPhoneBookAccessibleSetting(true);
        verify(mView).setIncomingCallPatternSetting(mIlluminationSettingSpec.btPhoneColorSettingSupported, mIlluminationSettingStatus.btPhoneColorSettingEnabled, mIlluminationSetting.btPhoneColor);
        verify(mView).setIncomingCallColorSetting(mIlluminationSettingSpec.sphBtPhoneColorSettingSupported, mIlluminationSettingStatus.sphBtPhoneColorSettingEnabled, mIlluminationSetting.sphBtPhoneColorSetting);
        verify(mView).setAutoAnswerSetting(mCarDeviceSpec.phoneSettingSupported, mPhoneSetting.autoAnswerSetting, mPhoneSettingStatus.autoAnswerSettingEnabled);
    }

    @Test
    public void onPhoneSettingStatusChangeEvent() throws Exception {
        // exercise
        mPresenter.onPhoneSettingStatusChangeEvent(new PhoneSettingStatusChangeEvent());

        // verify
        verify(mView).setDeviceSettings(mCarDeviceSpec.phoneSettingSupported, mPhoneSettingStatus.deviceListEnabled);
        verify(mView).setAutoPairingSetting(mCarDeviceSpec.phoneSettingSupported, mPhoneSettingStatus.autoPairingSettingEnabled,mPhoneSetting.autoPairingSetting);
        verify(mView).setPhoneBookAccessibleSetting(true);
        verify(mView).setIncomingCallPatternSetting(mIlluminationSettingSpec.btPhoneColorSettingSupported, mIlluminationSettingStatus.btPhoneColorSettingEnabled, mIlluminationSetting.btPhoneColor);
        verify(mView).setIncomingCallColorSetting(mIlluminationSettingSpec.sphBtPhoneColorSettingSupported, mIlluminationSettingStatus.sphBtPhoneColorSettingEnabled, mIlluminationSetting.sphBtPhoneColorSetting);
        verify(mView).setAutoAnswerSetting(mCarDeviceSpec.phoneSettingSupported, mPhoneSetting.autoAnswerSetting, mPhoneSettingStatus.autoAnswerSettingEnabled);
    }

    @Test
    public void onSelectDeviceSettingsAction() throws Exception {
        // setup
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        Resources resources = mock(Resources.class);
        when(mContext.getResources()).thenReturn(resources);
        when(resources.getString(R.string.setting_system_initial_settings)).thenReturn("TEST");

        // exercise
        mPresenter.onSelectDeviceSettingsAction();

        // verify
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, Matchers.is(ScreenId.BT_DEVICE_LIST));
    }

    @Test
    public void onSelectAutoPairingAction() throws Exception {
        // exercise
        mPresenter.onSelectAutoPairingAction(true);

        // verify
        verify(mPreferCase).setAutoPairing(true);
    }

    @Test
    public void onSelectDirectCallAction() throws Exception {
        // setup
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        Resources resources = mock(Resources.class);
        when(mContext.getResources()).thenReturn(resources);
        when(resources.getString(R.string.setting_system_initial_settings)).thenReturn("TEST");

        // exercise
        mPresenter.onSelectDirectCallAction();

        // verify
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, Matchers.is(ScreenId.DIRECT_CALL_SETTING));
    }

    @Test
    public void onSelectPhoneBookAccessibleAction() throws Exception {
        // exercise
        mPresenter.onSelectPhoneBookAccessibleAction(false);

        // verify
        verify(mPreference).setPhoneBookAccessible(false);
    }

    @Test
    public void onSelectIncomingCallPatternItemAction() throws Exception {
        // setup
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        Resources resources = mock(Resources.class);
        when(mContext.getResources()).thenReturn(resources);
        when(resources.getString(R.string.setting_system_initial_settings)).thenReturn("TEST");

        // exercise
        mPresenter.onSelectIncomingCallPatternItemAction();

        // verify
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, Matchers.is(ScreenId.INCOMING_CALL_PATTERN_SETTING));
    }

    @Test
    public void onSelectIncomingCallColorItemAction() throws Exception {
        // setup
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        Resources resources = mock(Resources.class);
        when(mContext.getResources()).thenReturn(resources);
        when(resources.getString(R.string.setting_system_initial_settings)).thenReturn("TEST");

        // exercise
        mPresenter.onSelectIncomingCallColorItemAction();

        // verify
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, Matchers.is(ScreenId.INCOMING_CALL_COLOR_SETTING));
    }

    @Test
    public void onSelectIncomingCallAutoAnswer() throws Exception {
        // exercise
        mPresenter.onSelectIncomingCallAutoAnswer(false);

        // verify
        verify(mPreferCase).setAutoAnswer(false);
    }
}