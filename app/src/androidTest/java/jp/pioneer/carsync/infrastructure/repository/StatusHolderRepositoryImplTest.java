package jp.pioneer.carsync.infrastructure.repository;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.repository.StatusHolderRepository;
import jp.pioneer.carsync.infrastructure.crp.event.CrpStatusUpdateEvent;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by NSW00_008320 on 2017/04/24.
 */
@RunWith(Theories.class)
public class StatusHolderRepositoryImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks StatusHolderRepositoryImpl mStatusHolderRepositoryImpl;
    @Mock StatusHolder mStatusHolder;
    @Mock EventBus mEventBus;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void get() throws Exception {
        // exercise
        StatusHolder actual = mStatusHolderRepositoryImpl.get();

        // verify
        assertThat(actual,is(mStatusHolder));

    }

    @Test
    public void setOnStatusUpdateListener_ListenerNotNull() throws Exception {
        // setup
        StatusHolderRepository.OnStatusUpdateListener listener = mock(StatusHolderRepository.OnStatusUpdateListener.class);

        // exercise
        mStatusHolderRepositoryImpl.setOnStatusUpdateListener(listener);
        mStatusHolderRepositoryImpl.setOnStatusUpdateListener(listener);

        // verify
        verify(mEventBus,times(1)).unregister(mStatusHolderRepositoryImpl);
        verify(mEventBus,times(2)).register(mStatusHolderRepositoryImpl);

    }

    @Test
    public void setOnStatusUpdateListener_ListenerNull() throws Exception {
         // exercise
        mStatusHolderRepositoryImpl.setOnStatusUpdateListener(null);

        // verify
        verify(mEventBus,never()).unregister(mStatusHolderRepositoryImpl);
        verify(mEventBus,never()).register(mStatusHolderRepositoryImpl);

    }

    @Test
    public void onStatusUpdateEvent_ListenerNotNull() throws Exception {
        // setup
        StatusHolderRepository.OnStatusUpdateListener listener = mock(StatusHolderRepository.OnStatusUpdateListener.class);
        CrpStatusUpdateEvent crpStatusUpdateEvent = mock(CrpStatusUpdateEvent.class);

        // exercise
        mStatusHolderRepositoryImpl.setOnStatusUpdateListener(listener);
        mStatusHolderRepositoryImpl.onStatusUpdateEvent(crpStatusUpdateEvent);

        // verify
        verify(listener).onStatusUpdate();
        verify(mEventBus,never()).unregister(mStatusHolderRepositoryImpl);
        verify(mEventBus,times(1)).register(mStatusHolderRepositoryImpl);

    }

    @Test
    public void onStatusUpdateEvent_ListenerNull() throws Exception {
        // setup
        CrpStatusUpdateEvent crpStatusUpdateEvent = mock(CrpStatusUpdateEvent.class);

        // exercise
        mStatusHolderRepositoryImpl.setOnStatusUpdateListener(null);
        mStatusHolderRepositoryImpl.onStatusUpdateEvent(crpStatusUpdateEvent);

        // verify
        verify(mEventBus,times(1)).unregister(mStatusHolderRepositoryImpl);
        verify(mEventBus,never()).register(mStatusHolderRepositoryImpl);

    }

}