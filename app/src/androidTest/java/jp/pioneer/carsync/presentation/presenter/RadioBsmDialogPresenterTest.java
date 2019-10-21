package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.event.RadioInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TunerStatus;
import jp.pioneer.carsync.presentation.view.RadioBsmDialogView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Radio BSM DialogのPresenterのテスト
 */
public class RadioBsmDialogPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks RadioBsmDialogPresenter mPresenter = new RadioBsmDialogPresenter();
    @Mock RadioBsmDialogView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mStatusHolder;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    /**
     * onResumeのテスト PLAY中の場合
     */
    @Test
    public void testOnResumeBsm() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);
        Bundle bundle = new Bundle();
        bundle.putInt(RadioBsmDialogPresenter.TYPE, 0);
        mPresenter.setArgument(bundle);
        mPresenter.onResume();
        verify(mEventBus).register(mPresenter);
        verify(mView).setTitleText(R.string.radio_bsm_dialog);
    }

    /**
     * onResumeのテスト PLAY中の場合
     */
    @Test
    public void testOnResumePty() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);
        Bundle bundle = new Bundle();
        bundle.putInt(RadioBsmDialogPresenter.TYPE, 1);
        mPresenter.setArgument(bundle);
        mPresenter.onResume();
        verify(mEventBus).register(mPresenter);
        verify(mView).setTitleText(R.string.radio_pty_dialog);
    }

    /**
     * onPauseのテスト
     */
    @Test
    public void testOnPause() throws Exception {
        mPresenter.onPause();
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void testOnRadioInfoChangeEvent() throws Exception {
        RadioInfoChangeEvent event = mock(RadioInfoChangeEvent.class);

        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);
        RadioInfo info = new RadioInfo();
        info.tunerStatus = TunerStatus.NORMAL;
        mockMediaHolder.radioInfo = info;
        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);
        mPresenter.onRadioInfoChangeEvent(event);
        verify(mView).callbackClose();
    }

}