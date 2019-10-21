package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.event.ParkingSensorStatusChangeEvent;
import jp.pioneer.carsync.domain.event.ReverseStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ParkingSensorStatus;
import jp.pioneer.carsync.domain.model.ReverseStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.ParkingSensorDialogView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * パーキングセンサー画面のTest
 */
public class ParkingSensorDialogPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ParkingSensorDialogPresenter mPresenter = new ParkingSensorDialogPresenter();
    @Mock ParkingSensorDialogView mView;
    @Mock EventBus mEventBus;
    @Mock Context mContext;
    @Mock GetStatusHolder mGetStatusHolder;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void onResume() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);
        StatusHolder holder = mock(StatusHolder.class);
        ParkingSensorStatus status = mock(ParkingSensorStatus.class);
        CarDeviceStatus carStatus = mock(CarDeviceStatus.class);
        carStatus.reverseStatus = ReverseStatus.OFF;
        when(holder.getCarDeviceStatus()).thenReturn(carStatus);
        when(holder.getParkingSensorStatus()).thenReturn(status);
        when(mGetStatusHolder.execute()).thenReturn(holder);

        mPresenter.onResume();
        verify(mEventBus).register(mPresenter);
        verify(mView).setSensor(status);
    }

    @Test
    public void onPause() throws Exception {
        mPresenter.onPause();
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void onParkingSensorStatusChangeEvent() throws Exception {
        ParkingSensorStatusChangeEvent event = mock(ParkingSensorStatusChangeEvent.class);
        StatusHolder holder = mock(StatusHolder.class);
        ParkingSensorStatus status = mock(ParkingSensorStatus.class);
        CarDeviceStatus carStatus = mock(CarDeviceStatus.class);
        carStatus.reverseStatus = ReverseStatus.OFF;
        when(holder.getCarDeviceStatus()).thenReturn(carStatus);
        when(holder.getParkingSensorStatus()).thenReturn(status);
        when(mGetStatusHolder.execute()).thenReturn(holder);

        mPresenter.onParkingSensorStatusChangeEvent(event);
        verify(mView).setSensor(status);
    }

    @Test
    public void onReverseStatusChangeEvent() throws Exception {
        ReverseStatusChangeEvent event = mock(ReverseStatusChangeEvent.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        status.reverseStatus = ReverseStatus.OFF;
        when(holder.getCarDeviceStatus()).thenReturn(status);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        mPresenter.onReverseStatusChangeEvent(event);
        verify(mView).dismissDialog();
    }

}