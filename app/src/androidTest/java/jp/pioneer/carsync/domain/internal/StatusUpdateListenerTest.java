package jp.pioneer.carsync.domain.internal;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.repository.StatusHolderRepository;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/04/26.
 */
public class StatusUpdateListenerTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks StatusUpdateListener mStatusUpdateListener;
    @Mock StatusHolderRepository mStatusHolderRepository;

    StatusHolder mStatusHolder;
    StatusObserver mStatusObserver;
    StatusObserver[] mStatusObservers;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mStatusHolder = mock(StatusHolder.class);
        mStatusObserver = mock(StatusObserver.class);
        mStatusObservers = new StatusObserver[]{mStatusObserver,mStatusObserver,mStatusObserver};

        when(mStatusHolderRepository.get()).thenReturn(mStatusHolder);

        mStatusUpdateListener.mStatusObservers = mStatusObservers;
    }

    @Test
    public void initialize_onStatusUpdate() throws Exception {
        // exercise
        mStatusUpdateListener.initialize();
        mStatusUpdateListener.onStatusUpdate();

        //verify
        verify(mStatusObserver,times(mStatusObservers.length)).initialize(mStatusHolder);
        verify(mStatusHolderRepository).setOnStatusUpdateListener(mStatusUpdateListener);
        verify(mStatusObserver,times(mStatusObservers.length)).onStatusUpdate(mStatusHolder);
    }

}