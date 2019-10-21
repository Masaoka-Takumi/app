package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;

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
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.DebugSettingView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * デバッグ設定画面のテスト
 */
public class DebugSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks DebugSettingPresenter mPresenter = new DebugSettingPresenter();
    @Mock DebugSettingView mView;
    @Mock EventBus mEventBus;
    @Mock AppSharedPreference mPreference;
    @Mock Context mContext;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
    }

    @Test
    public void testOnTakeView() throws Exception {
        when(mPreference.isLogEnabled()).thenReturn(false);
        mPresenter.onTakeView();
        verify(mView).setLogEnabled(false);
    }

    @Test
    public void testOnLogEnabledAction() throws Exception {
        mPresenter.onLogEnabledAction(true);
        verify(mPreference).setLogEnabled(true);
        verify(mView).setLogEnabled(true);
    }

    @Test
    public void testOnClassicBTLinkKeyAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onClassicBTLinkKeyAction();
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, Matchers.is(ScreenId.PAIRING_DEVICE_LIST));
        assertThat(SettingsParams.from(capturedEvent.args).pass, Matchers.is(mContext.getString(R.string.setting_debug_classic_bt_link_key)));

    }

    @Test
    public void testOnBLELinkKeyAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onBLELinkKeyAction();
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, Matchers.is(ScreenId.PAIRING_DEVICE_LIST));
        assertThat(SettingsParams.from(capturedEvent.args).pass, Matchers.is(mContext.getString(R.string.setting_debug_ble_link_key)));
    }

}