package jp.pioneer.carsync.infrastructure.repository;

import android.app.Instrumentation;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.v4.content.CursorLoader;
import android.support.v4.util.SparseArrayCompat;

import org.greenrobot.eventbus.EventBus;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Provider;

import jp.pioneer.carsync.domain.content.TunerContract;
import jp.pioneer.carsync.domain.model.BandType;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SxmBandType;
import jp.pioneer.carsync.domain.model.TunerFrequencyUnit;
import jp.pioneer.carsync.infrastructure.crp.entity.ListUpdateType;
import jp.pioneer.carsync.infrastructure.crp.event.CrpListUpdateEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStartedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStoppedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpStatusUpdateEvent;
import jp.pioneer.carsync.infrastructure.task.CarDeviceMediaListRequestTask;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

/**
 * Created by NSW00_008320 on 2017/05/30.
 */
@RunWith(Theories.class)
public class CarDeviceMediaRepositoryImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks CarDeviceMediaRepositoryImpl mCarDeviceMediaRepository = new CarDeviceMediaRepositoryImpl(){
        @Override
        CarDeviceMediaCursorLoader createCarDeviceMediaCursorLoader(Context context, CarDeviceMediaRepositoryImpl repository, MediaSourceType sourceType, BandType bandType) {
            return new CarDeviceMediaCursorLoader(context, repository, sourceType, bandType){
                @Override
                ContentObserver createForceLoadContentObserver() {
                    return mContentObserver;
                }
            };
        }
    };
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock StatusHolder mStatusHolder;
    @Mock ExecutorService mTaskExecutor;
    @Mock Provider<CarDeviceMediaListRequestTask> mTaskProvider;
    SparseArrayCompat<ListInfo.ListItem> mRadioListItems;
    SparseArrayCompat<ListInfo.ListItem> mSxmListItems;
    SparseArrayCompat<SparseArrayCompat> mListItems;
    Cursor mCursor;
    CarDeviceStatus mCarDeviceStatus;
    CarDeviceMediaListRequestTask mCarDeviceMediaListRequestTask;
    Future mTaskFuture;
    ListInfo mListInfo;
    CursorLoader mCursorLoader;
    ContentObserver mContentObserver;

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch  mSignal = new CountDownLatch(1);
    Instrumentation instr = InstrumentationRegistry.getInstrumentation();

    boolean isOnChangeCall;

    static class Fixture {
        MediaSourceType mediaSourceType;
        BandType bandType;

        Fixture(MediaSourceType mediaSourceType,BandType bandType) {
            this.mediaSourceType = mediaSourceType;
            this.bandType = bandType;
        }
    }

    @DataPoints
    public static final Fixture[] FIXTURES = new Fixture[] {
            new Fixture(MediaSourceType.RADIO,RadioBandType.AM2),
            new Fixture(MediaSourceType.SIRIUS_XM,SxmBandType.SXM2)
    };

    enum EXCEPTION_VALUE {
        MIN(0), MAX(7);
        int value;

        EXCEPTION_VALUE(int value) {
            this.value = value;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        isOnChangeCall = false;

        mCarDeviceMediaListRequestTask = mock(CarDeviceMediaListRequestTask.class);
        mTaskFuture = mock(Future.class);
        mCarDeviceStatus = new CarDeviceStatus();
        mListInfo = new ListInfo();
        mRadioListItems = new SparseArrayCompat<>();
        mSxmListItems = new SparseArrayCompat<>();
        mListItems = new SparseArrayCompat<>();
        mContentObserver  = new ContentObserver(mMainHandler) {
            @Override
            public boolean deliverSelfNotifications() {
                return super.deliverSelfNotifications();
            }

            @Override
            public void onChange(boolean selfChange) {
                if(!selfChange) {
                    isOnChangeCall = true;
                }
                super.onChange(selfChange);
                mSignal.countDown();
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
            }
        };

        when(mStatusHolder.getListInfo()).thenReturn(mListInfo);
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);
        when(mTaskProvider.get()).thenReturn(mCarDeviceMediaListRequestTask);
        when(mCarDeviceMediaListRequestTask.setParams(any(MediaSourceType.class),any(ListType.class),any(CarDeviceMediaListRequestTask.Callback.class))).thenReturn(mCarDeviceMediaListRequestTask);
        when(mTaskExecutor.submit(any(Runnable.class))).thenReturn(mTaskFuture);
        when(mTaskFuture.isDone()).thenReturn(true);

        // create radio list
        int count = 0;
        for(RadioBandType bandType : RadioBandType.values()) {
            count++;
            ListInfo.RadioListItem item = new ListInfo.RadioListItem();
            item.band = bandType;
            item.frequency = count * 1000;
            item.frequencyUnit = TunerFrequencyUnit.KHZ;
            item.pchNumber = count;
            item.listIndex = count;
            item.text = "TEST_TEXT_" + count;
            mRadioListItems.put(count, item);
        }
        mListItems.put(MediaSourceType.RADIO.code,mRadioListItems);
        // create sirius list
        count = 0;
        for(SxmBandType bandType : SxmBandType.values()) {
            count++;
            ListInfo.SxmListItem item = new ListInfo.SxmListItem();
            item.band = bandType;
            item.channelNumber = count;
            item.pchNumber = count;
            item.listIndex = count;
            item.text = "TEST_TEXT_" + count;
            mSxmListItems.put(count, item);
        }
        mListItems.put(MediaSourceType.SIRIUS_XM.code,mSxmListItems);
    }

    @After
    public void tearDown() throws Exception {
        // stop task
        mCarDeviceMediaRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());
    }

    @Test
    public void initialize() throws Exception {
        // exercise
        mCarDeviceMediaRepository.initialize();

        // verify
        verify(mEventBus).register(mCarDeviceMediaRepository);
    }

    @Theory
    public void getPresetChannelList_NODATA(Fixture fixture) throws Exception {
        // exercise
        instr.runOnMainSync(() -> {
            mCursorLoader = mCarDeviceMediaRepository.getPresetChannelList(fixture.mediaSourceType, null);}
        );
        Cursor cursor = mCursorLoader.loadInBackground();

        // verify
        assertThat(cursor.getCount(),is(0));
    }

    @Theory
    public void getPresetChannelList_AllData(Fixture fixture) throws Exception {
        // setup
        mListInfo.transactionInfo.sourceType = fixture.mediaSourceType;
        mListInfo.transactionInfo.items = mListItems.get(fixture.mediaSourceType.code);
//        mCarDeviceMediaRepository.onFinish();

        // exercise
        instr.runOnMainSync(() -> {
            mCursorLoader = mCarDeviceMediaRepository.getPresetChannelList(fixture.mediaSourceType, null);}
        );
        Cursor cursor = mCursorLoader.loadInBackground();

        // verify
        while(cursor.moveToNext()){
            int position = cursor.getPosition() + 1;
            switch(fixture.mediaSourceType) {
                case RADIO: {
                    assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.Radio._ID)), is(((long) position)));
                    assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.Radio.LIST_INDEX)), is(position));
                    assertThat(cursor.getString(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.Radio.TEXT)), is("TEST_TEXT_" + position));
                    assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.Radio.PCH_NUMBER)), is(position));
                    assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.Radio.FREQUENCY)), is(((long) position) * 1000L));
                    assertThat(cursor.getString(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.Radio.FREQUENCY_UNIT)), is(TunerFrequencyUnit.KHZ.name()));
                    assertThat(cursor.getString(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.Radio.BAND_TYPE)), is(RadioBandType.values()[position - 1].name()));
                    break;
                }
                case SIRIUS_XM:{
                    assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.SiriusXm._ID)), is(((long) position)));
                    assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.SiriusXm.LIST_INDEX)), is(position));
                    assertThat(cursor.getString(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.SiriusXm.TEXT)), is("TEST_TEXT_" + position));
                    assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.SiriusXm.PCH_NUMBER)), is(position));
                    assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.SiriusXm.CH_NUMBER)), is(position));
                    assertThat(cursor.getString(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.SiriusXm.BAND_TYPE)), is(SxmBandType.values()[position - 1].name()));
                    break;
                }
                default:{
                    break;
                }
            }
        }
    }

    @Theory
    public void getPresetChannelList_SelectBand(Fixture fixture) throws Exception {
        // setup
        mListInfo.transactionInfo.sourceType = fixture.mediaSourceType;
        mListInfo.transactionInfo.items = mListItems.get(fixture.mediaSourceType.code);
//        mCarDeviceMediaRepository.onFinish();

        // exercise
        instr.runOnMainSync(() -> {
            mCursorLoader = mCarDeviceMediaRepository.getPresetChannelList(fixture.mediaSourceType, fixture.bandType);}
        );
        Cursor cursor = mCursorLoader.loadInBackground();
        cursor.moveToFirst();

        // verify
        assertThat(cursor.getCount(),is(1));
        int position = 0;
        switch(fixture.mediaSourceType) {
            case RADIO: {
                for(RadioBandType bandType : RadioBandType.values()) {
                    position++;
                    if(bandType == fixture.bandType){
                        break;
                    }
                }
                assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.Radio._ID)), is(((long) position)));
                assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.Radio.LIST_INDEX)), is(position));
                assertThat(cursor.getString(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.Radio.TEXT)), is("TEST_TEXT_" + position));
                assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.Radio.PCH_NUMBER)), is(position));
                assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.Radio.FREQUENCY)), is(((long) position) * 1000L));
                assertThat(cursor.getString(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.Radio.FREQUENCY_UNIT)), is(TunerFrequencyUnit.KHZ.name()));
                assertThat(cursor.getString(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.Radio.BAND_TYPE)), is(((RadioBandType) fixture.bandType).name()));
                break;
            }
            case SIRIUS_XM:{
                for(SxmBandType bandType : SxmBandType.values()) {
                    position++;
                    if(bandType == fixture.bandType){
                        break;
                    }
                }
                assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.SiriusXm._ID)), is(((long) position)));
                assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.SiriusXm.LIST_INDEX)), is(position));
                assertThat(cursor.getString(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.SiriusXm.TEXT)), is("TEST_TEXT_" + position));
                assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.SiriusXm.PCH_NUMBER)), is(position));
                assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.SiriusXm.CH_NUMBER)), is(position));
                assertThat(cursor.getString(cursor.getColumnIndexOrThrow(TunerContract.PresetChannelContract.SiriusXm.BAND_TYPE)), is(SxmBandType.values()[position - 1].name()));
                break;
            }
            default:{
                break;
            }
        }
    }

    @Theory
    public void getListItem(Fixture fixture) throws Exception {
        // setup
        mListInfo.transactionInfo.sourceType = fixture.mediaSourceType;
        mListInfo.transactionInfo.items = mListItems.get(fixture.mediaSourceType.code);
//        mCarDeviceMediaRepository.onFinish();
        int listIndex = 1;

        // exercise
        ListInfo.ListItem listItem = mCarDeviceMediaRepository.getListItem(fixture.mediaSourceType,listIndex);

        // verify
        switch(fixture.mediaSourceType) {
            case RADIO: {
                ListInfo.RadioListItem actual = ((ListInfo.RadioListItem) listItem);
                assertThat(actual.getBand(),is(RadioBandType.values()[listIndex - 1]));
                assertThat(actual.getChannelNumber(),is(-1));
                assertThat(actual.getFrequency(),is(((long) listIndex) * 1000L));
                assertThat(actual.getFrequencyUnit(),is(TunerFrequencyUnit.KHZ));
                assertThat(actual.getPchNumber(),is(listIndex));
                break;
            }
            case SIRIUS_XM:{
                ListInfo.SxmListItem actual = ((ListInfo.SxmListItem) listItem);
                assertThat(actual.getBand(),is(SxmBandType.values()[listIndex - 1]));
                assertThat(actual.getChannelNumber(),is(listIndex));
                assertThat(actual.getFrequency(),is(-1L));
                assertThat(actual.getFrequencyUnit(),is(nullValue()));
                assertThat(actual.getPchNumber(),is(listIndex));
                break;
            }
            default:{
                break;
            }
        }
    }

    // expected = start task
    @Theory
    public void onCrpSessionStartedEvent_isPchListSupported_isListSupported(Fixture fixture) throws Exception {
        // setup
        mCarDeviceStatus.listType = ListType.PCH_LIST;
        mCarDeviceStatus.sourceType = fixture.mediaSourceType;
        when(mStatusHolder.isListSupported()).thenReturn(true);

        // exercise
        mCarDeviceMediaRepository.onCrpSessionStartedEvent(new CrpSessionStartedEvent());

        // verify
//        verify(mCarDeviceMediaListRequestTask).setParams(fixture.mediaSourceType,ListType.PCH_LIST,mCarDeviceMediaRepository);
        verify(mTaskExecutor).submit(mCarDeviceMediaListRequestTask);

    }

    // expected = start task not call
    @Theory
    public void onCrpSessionStartedEvent_isPchListSupported_isNotListSupported(Fixture fixture) throws Exception {
        // setup
        mCarDeviceStatus.listType = ListType.PCH_LIST;
        mCarDeviceStatus.sourceType = fixture.mediaSourceType;
        when(mStatusHolder.isListSupported()).thenReturn(false);


        // exercise
        mCarDeviceMediaRepository.onCrpSessionStartedEvent(new CrpSessionStartedEvent());

        // verify
        verify(mCarDeviceMediaListRequestTask,never()).setParams(any(MediaSourceType.class),any(ListType.class),any(CarDeviceMediaListRequestTask.Callback.class));
        verify(mTaskExecutor,never()).submit(any(Runnable.class));

    }

    // expected = start task not call
    @Test
    public void onCrpSessionStartedEvent_isNotPchListSupported() throws Exception {
        // setup
        mCarDeviceStatus.listType = ListType.PCH_LIST;
        mCarDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        when(mStatusHolder.isListSupported()).thenReturn(false);


        // exercise
        mCarDeviceMediaRepository.onCrpSessionStartedEvent(new CrpSessionStartedEvent());

        // verify
        verify(mCarDeviceMediaListRequestTask,never()).setParams(any(MediaSourceType.class),any(ListType.class),any(CarDeviceMediaListRequestTask.Callback.class));
        verify(mTaskExecutor,never()).submit(any(Runnable.class));

    }

    // expected = stop task
    @Theory
    public void onCrpSessionStoppedEvent_isRunningTask(Fixture fixture) throws Exception {
        // setup
        mCarDeviceStatus.listType = ListType.PCH_LIST;
        mCarDeviceStatus.sourceType = fixture.mediaSourceType;
        when(mStatusHolder.isListSupported()).thenReturn(true);
        when(mTaskFuture.isDone()).thenReturn(false);

        // exercise
        mCarDeviceMediaRepository.onCrpSessionStartedEvent(new CrpSessionStartedEvent());
        mCarDeviceMediaRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());

        // verify
        verify(mTaskFuture).cancel(true);
    }

    // expected = stop task not call
    @Test
    public void onCrpSessionStoppedEvent_isNotRunningTask() throws Exception {
        // exercise
        mCarDeviceMediaRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());

        // verify
        verify(mTaskFuture,never()).cancel(anyBoolean());
    }

    // expected = start task
    @Theory
    public void onCrpListUpdateEvent_isListSupported(Fixture fixture) throws Exception {
        // setup
        when(mStatusHolder.isListSupported()).thenReturn(true);
        mCarDeviceStatus.listType = ListType.PCH_LIST;
        mCarDeviceStatus.sourceType = fixture.mediaSourceType;

        // exercise
        mCarDeviceMediaRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());
        mCarDeviceMediaRepository.onCrpListUpdateEvent(new CrpListUpdateEvent(ListUpdateType.UPDATE));

        // verify
//        verify(mCarDeviceMediaListRequestTask).setParams(fixture.mediaSourceType,ListType.PCH_LIST,mCarDeviceMediaRepository);
        verify(mTaskExecutor).submit(mCarDeviceMediaListRequestTask);
    }

    // expected = start task not call
    @Test
    public void onCrpListUpdateEvent_isListNotSupported() throws Exception {
        // setup
        when(mStatusHolder.isListSupported()).thenReturn(false);

        // exercise
        mCarDeviceMediaRepository.onCrpListUpdateEvent(new CrpListUpdateEvent(ListUpdateType.UPDATE));

        // verify
        verify(mCarDeviceMediaListRequestTask,never()).setParams(any(MediaSourceType.class),any(ListType.class),any(CarDeviceMediaListRequestTask.Callback.class));
        verify(mTaskExecutor,never()).submit(any(Runnable.class));
    }

    // expected = start task
    @Theory
    public void onCrpStatusUpdateEvent_onSourceChanged_true_isListSupported(Fixture fixture) throws Exception {
        // setup
        mCarDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        mCarDeviceMediaRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());
        mCarDeviceStatus.sourceType = fixture.mediaSourceType;
        mCarDeviceStatus.sourceStatus = MediaSourceStatus.CHANGE_COMPLETED;
        when(mStatusHolder.isListSupported()).thenReturn(true);

        // exercise
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());

        // verify
//        verify(mCarDeviceMediaListRequestTask).setParams(fixture.mediaSourceType,ListType.PCH_LIST,mCarDeviceMediaRepository);
        verify(mTaskExecutor).submit(mCarDeviceMediaListRequestTask);
    }

    // expected = start task not call
    @Theory
    public void onCrpStatusUpdateEvent_onSourceChanged_true_isListNotSupported(Fixture fixture) throws Exception {
        // setup
        mCarDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        mCarDeviceMediaRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());
        mCarDeviceStatus.sourceType = fixture.mediaSourceType;
        mCarDeviceStatus.sourceStatus = MediaSourceStatus.CHANGE_COMPLETED;
        when(mStatusHolder.isListSupported()).thenReturn(false);

        // exercise
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());

        // verify
        verify(mCarDeviceMediaListRequestTask,never()).setParams(any(MediaSourceType.class),any(ListType.class),any(CarDeviceMediaListRequestTask.Callback.class));
        verify(mTaskExecutor,never()).submit(any(Runnable.class));
    }

    // expected = start task not call
    @Theory
    public void onCrpStatusUpdateEvent_onSourceChanged_false(Fixture fixture) throws Exception {
        // setup
        mCarDeviceStatus.sourceType = fixture.mediaSourceType;
        mCarDeviceMediaRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());
        mCarDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        mCarDeviceStatus.sourceStatus = MediaSourceStatus.CHANGE_COMPLETED;

        // exercise
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());

        // verify
        verify(mCarDeviceMediaListRequestTask,never()).setParams(any(MediaSourceType.class),any(ListType.class),any(CarDeviceMediaListRequestTask.Callback.class));
        verify(mTaskExecutor,never()).submit(any(Runnable.class));
    }

    // expected = start task call stop task call
    @Theory
    public void onCrpStatusUpdateEvent_onListTypeChanged_isPchListSupported_IsDirtyCarDeviceMediaList_isNotRunningTask_isListSupported(Fixture fixture) throws Exception {
        // setup
        mCarDeviceStatus.sourceType = fixture.mediaSourceType;
        mCarDeviceStatus.sourceStatus = MediaSourceStatus.CHANGE_COMPLETED;
        mCarDeviceStatus.listType = ListType.NOT_LIST;
        mCarDeviceMediaRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());
        mCarDeviceStatus.listType = ListType.LIST;
        when(mStatusHolder.isListSupported()).thenReturn(true);

        // exercise
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());

        // verify
//        verify(mCarDeviceMediaListRequestTask).setParams(fixture.mediaSourceType,ListType.PCH_LIST,mCarDeviceMediaRepository);
        verify(mTaskExecutor).submit(mCarDeviceMediaListRequestTask);
    }

    // expected = start task not call
    @Theory
    public void onCrpStatusUpdateEvent_onListTypeChanged_isPchListSupported_IsDirtyCarDeviceMediaList_isNotRunningTask_isNotListSupported(Fixture fixture) throws Exception {
        // setup
        mCarDeviceStatus.sourceType = fixture.mediaSourceType;
        mCarDeviceStatus.sourceStatus = MediaSourceStatus.CHANGE_COMPLETED;
        mCarDeviceStatus.listType = ListType.NOT_LIST;
        mCarDeviceMediaRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());
        mCarDeviceStatus.listType = ListType.LIST;
        when(mStatusHolder.isListSupported()).thenReturn(false);

        // exercise
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());

        // verify
        verify(mCarDeviceMediaListRequestTask,never()).setParams(any(MediaSourceType.class),any(ListType.class),any(CarDeviceMediaListRequestTask.Callback.class));
        verify(mTaskExecutor,never()).submit(any(Runnable.class));
    }

    // expected = start task not call
    @Theory
    public void onCrpStatusUpdateEvent_onListTypeChanged_isPchListSupported_IsNotDirtyCarDeviceMediaList(Fixture fixture) throws Exception {
        // setup
        mCarDeviceStatus.sourceType = fixture.mediaSourceType;
        mCarDeviceStatus.sourceStatus = MediaSourceStatus.CHANGE_COMPLETED;
        mCarDeviceStatus.listType = ListType.NOT_LIST;
        mCarDeviceMediaRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());
        mCarDeviceStatus.listType = ListType.LIST;
        mListInfo.transactionInfo.sourceType = fixture.mediaSourceType;
        mListInfo.transactionInfo.items = mRadioListItems;
//        mCarDeviceMediaRepository.onFinish();

        // exercise
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());

        // verify
        verify(mCarDeviceMediaListRequestTask,never()).setParams(any(MediaSourceType.class),any(ListType.class),any(CarDeviceMediaListRequestTask.Callback.class));
        verify(mTaskExecutor,never()).submit(any(Runnable.class));
    }

    // expected = onCrpStatusUpdateEvent method start task not call
    @Theory
    public void onCrpStatusUpdateEvent_onListTypeChanged_isPchListSupported_isRunningTask(Fixture fixture) throws Exception {
        // setup
        when(mStatusHolder.isListSupported()).thenReturn(true);
        mCarDeviceStatus.sourceType = fixture.mediaSourceType;
        mCarDeviceStatus.sourceStatus = MediaSourceStatus.CHANGE_COMPLETED;
        mCarDeviceStatus.listType = ListType.NOT_LIST;
        mCarDeviceMediaRepository.onCrpSessionStartedEvent(new CrpSessionStartedEvent());
        mCarDeviceStatus.listType = ListType.LIST;
        when(mStatusHolder.isListSupported()).thenReturn(true);
        when(mTaskFuture.isDone()).thenReturn(false);

        // exercise
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());

        // verify
//        verify(mCarDeviceMediaListRequestTask,times(1)).setParams(fixture.mediaSourceType,ListType.PCH_LIST,mCarDeviceMediaRepository);
        verify(mTaskExecutor,times(1)).submit(mCarDeviceMediaListRequestTask);
    }

    // expected = stop task
    @Theory
    public void onCrpStatusUpdateEvent_onListTypeChanged_isNotPchListSupported_mCurrentListType_NOTLIST_isRunningTask(Fixture fixture) throws Exception {
        // setup
        when(mStatusHolder.isListSupported()).thenReturn(true);
        mCarDeviceStatus.sourceType = fixture.mediaSourceType;
        mCarDeviceStatus.sourceStatus = MediaSourceStatus.CHANGE_COMPLETED;
        mCarDeviceStatus.listType = ListType.LIST;
        mCarDeviceMediaRepository.onCrpSessionStartedEvent(new CrpSessionStartedEvent());
        mCarDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());
        mCarDeviceStatus.listType = ListType.NOT_LIST;
        when(mTaskFuture.isDone()).thenReturn(false);

        // exercise
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());

        // verify
        verify(mTaskFuture).cancel(true);
    }

    // expected = stop  task not call
    @Test
    public void onCrpStatusUpdateEvent_onListTypeChanged_isNotPchListSupported_mCurrentListType_NOTLIST_isNotRunningTask() throws Exception {
        // setup
        mCarDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        mCarDeviceStatus.sourceStatus = MediaSourceStatus.CHANGE_COMPLETED;
        mCarDeviceStatus.listType = ListType.LIST;
        mCarDeviceMediaRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());
        mCarDeviceStatus.listType = ListType.NOT_LIST;

        // exercise
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());

        // verify
        verify(mTaskFuture,never()).cancel(anyBoolean());
    }

    // expected = stop task
    @Theory
    public void onCrpStatusUpdateEvent_onListTypeChanged_isNotPchListSupported_mCurrentListType_LISTUNAVAILABLE_isRunningTask(Fixture fixture) throws Exception {
        // setup
        when(mStatusHolder.isListSupported()).thenReturn(true);
        mCarDeviceStatus.sourceType = fixture.mediaSourceType;
        mCarDeviceStatus.sourceStatus = MediaSourceStatus.CHANGE_COMPLETED;
        mCarDeviceStatus.listType = ListType.LIST;
        mCarDeviceMediaRepository.onCrpSessionStartedEvent(new CrpSessionStartedEvent());
        mCarDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());
        mCarDeviceStatus.listType = ListType.LIST_UNAVAILABLE;
        when(mTaskFuture.isDone()).thenReturn(false);

        // exercise
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());

        // verify
        verify(mTaskFuture).cancel(true);
    }

    // expected = stop  task not call
    @Test
    public void onCrpStatusUpdateEvent_onListTypeChanged_isNotPchListSupported_mCurrentListType_LISTUNAVAILABLE_isNotRunningTask() throws Exception {
        // setup
        mCarDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        mCarDeviceStatus.sourceStatus = MediaSourceStatus.CHANGE_COMPLETED;
        mCarDeviceStatus.listType = ListType.LIST;
        mCarDeviceMediaRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());
        mCarDeviceStatus.listType = ListType.LIST_UNAVAILABLE;

        // exercise
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());

        // verify
        verify(mTaskFuture,never()).cancel(anyBoolean());
    }

    // expected = start task and stop task not call
    @Test
    public void onCrpStatusUpdateEvent_onListTypeChanged_isNotPchListSupported_mCurrentListType_EXIT() throws Exception {
        // setup
        mCarDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        mCarDeviceStatus.sourceStatus = MediaSourceStatus.CHANGE_COMPLETED;
        mCarDeviceStatus.listType = ListType.LIST;
        mCarDeviceMediaRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());
        mCarDeviceStatus.listType = ListType.EXIT;

        // exercise
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());

        // verify
        verify(mTaskFuture,never()).cancel(anyBoolean());
    }

    // expected = onCrpStatusUpdateEvent method start task not call
    @Theory
    public void onCrpStatusUpdateEvent_onListTypeChanged_isNotPchListSupported_IsDirtyCarDeviceMediaList_mCurrentListType_SERVICELIST(Fixture fixture) throws Exception {
        // setup
        when(mStatusHolder.isListSupported()).thenReturn(true);
        mCarDeviceStatus.sourceType = fixture.mediaSourceType;
        mCarDeviceStatus.sourceStatus = MediaSourceStatus.CHANGE_COMPLETED;
        mCarDeviceStatus.listType = ListType.NOT_LIST;
        mCarDeviceMediaRepository.onCrpSessionStartedEvent(new CrpSessionStartedEvent());
        mCarDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());
        mCarDeviceStatus.listType = ListType.SERVICE_LIST;

        // exercise
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());

        // verify
//        verify(mCarDeviceMediaListRequestTask,times(1)).setParams(fixture.mediaSourceType,ListType.PCH_LIST,mCarDeviceMediaRepository);
        verify(mTaskExecutor,times(1)).submit(mCarDeviceMediaListRequestTask);
    }

    // expected = onCrpStatusUpdateEvent method start task not call
    @Theory
    public void onCrpStatusUpdateEvent_onListTypeChanged_isNotPchListSupported_IsNotDirtyCarDeviceMediaList_mCurrentListType_SERVICELIST(Fixture fixture) throws Exception {
        // setup
        when(mStatusHolder.isListSupported()).thenReturn(true);
        mCarDeviceStatus.sourceType = fixture.mediaSourceType;
        mCarDeviceStatus.sourceStatus = MediaSourceStatus.CHANGE_COMPLETED;
        mCarDeviceStatus.listType = ListType.NOT_LIST;
        mCarDeviceMediaRepository.onCrpSessionStartedEvent(new CrpSessionStartedEvent());
        mCarDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());
        mCarDeviceStatus.listType = ListType.SERVICE_LIST;
        mListInfo.transactionInfo.sourceType = fixture.mediaSourceType;
        mListInfo.transactionInfo.items = mRadioListItems;
//        mCarDeviceMediaRepository.onFinish();

        // exercise
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());

        // verify
//        verify(mCarDeviceMediaListRequestTask,times(1)).setParams(fixture.mediaSourceType,ListType.PCH_LIST,mCarDeviceMediaRepository);
        verify(mTaskExecutor,times(1)).submit(mCarDeviceMediaListRequestTask);
    }

    // expected = onCrpStatusUpdateEvent method start task not call
    @Theory
    public void onCrpStatusUpdateEvent_onListTypeChanged_isNotPchListSupported_IsDirtyCarDeviceMediaList_mCurrentListType_EXIT(Fixture fixture) throws Exception {
        // setup
        when(mStatusHolder.isListSupported()).thenReturn(true);
        mCarDeviceStatus.sourceType = fixture.mediaSourceType;
        mCarDeviceStatus.sourceStatus = MediaSourceStatus.CHANGE_COMPLETED;
        mCarDeviceStatus.listType = ListType.NOT_LIST;
        mCarDeviceMediaRepository.onCrpSessionStartedEvent(new CrpSessionStartedEvent());
        mCarDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());
        mCarDeviceStatus.listType = ListType.EXIT;

        // exercise
        mCarDeviceMediaRepository.onCrpStatusUpdateEvent(new CrpStatusUpdateEvent());

        // verify
//        verify(mCarDeviceMediaListRequestTask,times(1)).setParams(fixture.mediaSourceType,ListType.PCH_LIST,mCarDeviceMediaRepository);
        verify(mTaskExecutor,times(1)).submit(mCarDeviceMediaListRequestTask);
    }

    @Theory
    public void CarDeviceMediaCursor_constructor(Fixture fixture) throws  Exception {
        // setup
        mListInfo.transactionInfo.sourceType = fixture.mediaSourceType;
        mListInfo.transactionInfo.items = mListItems.get(fixture.mediaSourceType.code);
//        mCarDeviceMediaRepository.onFinish();
        instr.runOnMainSync(() -> {
            mCursorLoader = mCarDeviceMediaRepository.getPresetChannelList(fixture.mediaSourceType, null);}
        );

        // exercise
        Cursor cursor = mCursorLoader.loadInBackground();

        // verify
        verify(mEventBus).register(cursor);
    }

    @Theory
    public void CarDeviceMediaCursor_close(Fixture fixture) throws  Exception {
        // setup
        mListInfo.transactionInfo.sourceType = fixture.mediaSourceType;
        mListInfo.transactionInfo.items = mListItems.get(fixture.mediaSourceType.code);
//        mCarDeviceMediaRepository.onFinish();
        instr.runOnMainSync(() -> {
            mCursorLoader = mCarDeviceMediaRepository.getPresetChannelList(fixture.mediaSourceType, null);}
        );

        // exercise
        Cursor cursor = mCursorLoader.loadInBackground();
        cursor.close();

        // verify
        verify(mEventBus).unregister(cursor);
    }

    @Theory
    public void CarDeviceMediaCursor_onCarDeviceMediaListChangedEvent(Fixture fixture) throws  Exception {
        // setup
        mSignal = new CountDownLatch(2);
        mCarDeviceStatus.listType = ListType.PCH_LIST;
        mCarDeviceStatus.sourceType = fixture.mediaSourceType;

        mListInfo.transactionInfo.sourceType = fixture.mediaSourceType;
        mListInfo.transactionInfo.items = mListItems.get(fixture.mediaSourceType.code);

//        mCarDeviceMediaRepository.onFinish();
        instr.runOnMainSync(() -> {
            mCursorLoader = mCarDeviceMediaRepository.getPresetChannelList(fixture.mediaSourceType, null);}
        );

        Cursor cursor = mCursorLoader.loadInBackground();

        doAnswer(invocationOnMock -> {
            mMainHandler.post(() -> {
                ((CarDeviceMediaRepositoryImpl.CarDeviceMediaCursor) cursor).onCarDeviceMediaListChangedEvent(new CarDeviceMediaRepositoryImpl.CarDeviceMediaListChangedEvent());
                mSignal.countDown();
            });
            return null;
        }).when(mEventBus).post(any(CarDeviceMediaRepositoryImpl.CarDeviceMediaListChangedEvent.class));

        // exercise
        mCarDeviceMediaRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());
        mSignal.await();

        // verify
        assertThat(isOnChangeCall, is(true));
    }

    @Test(expected = NullPointerException.class)
    public void getPresetChannelList_ArgNull() throws Exception {
        // exercise
        mCarDeviceMediaRepository.getPresetChannelList(null,RadioBandType.FM1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPresetChannelList_SourceTypeIsPchListUnsupported() throws Exception {
        // exercise
        mCarDeviceMediaRepository.getPresetChannelList(MediaSourceType.APP_MUSIC,RadioBandType.FM1);
    }

    @Test(expected = NullPointerException.class)
    public void getListItem_ArgSourceTypeNull() throws Exception {
        // exercise
        mCarDeviceMediaRepository.getListItem(null,1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getListItem_ArgListIndexValueMin() throws Exception {
        // exercise
        mCarDeviceMediaRepository.getListItem(MediaSourceType.RADIO,EXCEPTION_VALUE.MIN.value);
    }
}