package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.InformationView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_906320 on 2017/06/01.
 */
public class InformationPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks InformationPresenter mPresenter = new InformationPresenter();
    @Mock InformationView mView;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetCase;
    @Mock Context mContext;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
    }

    @After
    public void tearDown() throws Exception {

    }

    /**
     * onInitialize(デバイス情報未接続時)のテスト
     */
    @Test
    public void testOnInitialize() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.modelName = "";
        when(holder.getBtDeviceName()).thenReturn("");
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onInitialize();

        verify(mView).setDeviceInformation("未接続");
        verify(mView).setAppVersion(anyString());
    }

    /**
     * onRestoreInstanceState(デバイス情報接続時)のテスト
     */
    @Test
    public void testOnRestoreInstance() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        Bundle savedInstanceState = new Bundle();
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.modelName = "TEST";

        when(holder.getBtDeviceName()).thenReturn("TEST");
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onRestoreInstanceState(savedInstanceState);

        verify(mView).setDeviceInformation(holder.getBtDeviceName());
        verify(mView).setAppVersion(anyString());
    }

    /**
     * onManualActionのテスト
     */
    @Test
    public void testOnManualAction() throws Exception {
        ArgumentCaptor<Intent> argument = ArgumentCaptor.forClass(Intent.class);
        Uri uri = Uri.parse("http://google.com/");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        mPresenter.onManualAction();
        verify(mView).startBrowser(argument.capture());
        assertThat(argument.getValue().toString(), is(intent.toString()));
    }

    /**
     * onEulaActionのテスト
     */
    @Test
    public void testOnEulaAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onEulaAction();
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.EULA));
    }

    /**
     * onLicenseActionのテスト
     */
    @Test
    public void testOnLicenseAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onLicenseAction();
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.LICENSE));
    }

    /**
     * onPrivacyPolicyActionのテスト
     */
    @Test
    public void testOnPrivacyPolicyAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onPrivacyPolicyAction();
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.PRIVACY_POLICY));
    }

}