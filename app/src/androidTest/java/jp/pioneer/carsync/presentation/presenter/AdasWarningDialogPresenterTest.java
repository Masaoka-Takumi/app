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

import java.util.HashSet;
import java.util.Set;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.event.AdasWarningUpdateEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.AdasWarningEvent;
import jp.pioneer.carsync.domain.model.AdasWarningStatus;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.AdasWarningDialogView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2018/01/11.
 */
public class AdasWarningDialogPresenterTest {

    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks AdasWarningDialogPresenter mPresenter;
    @Mock AdasWarningDialogView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetStatusHolder;
    private Set<AdasWarningEvent> mEventList = new HashSet<>();
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        mEventList.add(AdasWarningEvent.OFF_ROAD_LEFT_DASH_EVENT);
        mEventList.add(AdasWarningEvent.FORWARD_HEADWAY_COLLISION_EVENT);
    }

    @Test
    public void testOnTakeView() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);
        StatusHolder holder = mock(StatusHolder.class);
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        status.adasWarningEvents = mEventList;
        when(status.getAdasWarningStatus()).thenReturn(AdasWarningStatus.SINGLE);
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(mGetStatusHolder.execute()).thenReturn(holder);

        mPresenter.onTakeView();
        verify(mView).setAdasImage(R.drawable.p1203_ldw_l);
        verify(mView).setAdasText(mContext.getString(R.string.adas_warning_lane_departure_left));
    }

    @Test
    public void testOnResume() throws Exception {

        mPresenter.onResume();
        verify(mEventBus).register(mPresenter);

    }

    @Test
    public void testOnPause() throws Exception {
        mPresenter.onPause();
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void testOnAdasWarningUpdateEvent() throws Exception {
        AdasWarningUpdateEvent event = mock(AdasWarningUpdateEvent.class);
        StatusHolder holder = mock(StatusHolder.class);
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        status.adasWarningEvents = mEventList;
        when(status.getAdasWarningStatus()).thenReturn(AdasWarningStatus.SINGLE);
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(mGetStatusHolder.execute()).thenReturn(holder);

        mPresenter.onAdasWarningUpdateEvent(event);
        verify(mView).setAdasImage(R.drawable.p1203_ldw_l);
        verify(mView).setAdasText(mContext.getString(R.string.adas_warning_lane_departure_left));
    }

}