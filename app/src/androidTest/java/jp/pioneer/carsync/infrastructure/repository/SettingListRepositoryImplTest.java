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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Provider;

import jp.pioneer.carsync.domain.component.BtSettingController;
import jp.pioneer.carsync.domain.content.QuerySettingListParams;
import jp.pioneer.carsync.domain.content.SettingListContract;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.DeviceListItem;
import jp.pioneer.carsync.domain.model.ProtocolSpec;
import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.domain.model.SearchListItem;
import jp.pioneer.carsync.domain.model.SettingListInfo;
import jp.pioneer.carsync.domain.model.SettingListInfoMap;
import jp.pioneer.carsync.domain.model.SettingListItem;
import jp.pioneer.carsync.domain.model.SettingListType;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStartedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStoppedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSettingListUpdateEvent;
import jp.pioneer.carsync.infrastructure.task.SettingInfoListRequestTask;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.domain.model.SettingListType.DEVICE_LIST;
import static jp.pioneer.carsync.domain.model.SettingListType.SEARCH_LIST;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/06/21.
 */
public class SettingListRepositoryImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SettingListRepositoryImpl mSettingListRepository = new SettingListRepositoryImpl(){
        @Override
        SettingListInfoCursorLoader createSettingListInfoCursorLoader(Context context, SettingListRepositoryImpl repository, QuerySettingListParams params) {
            return new SettingListInfoCursorLoader(context, repository, params){
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
    @Mock OutgoingPacketBuilder mOutgoingPacketBuilder;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock Provider<SettingInfoListRequestTask> mTaskProvider;
    @Mock BtSettingController mBtSettingController;
    @Mock SettingInfoListRequestTask mSettingInfoListRequestTask;
    @Mock SettingInfoListRequestTask mDeviceListRequestTask;
    @Mock SettingInfoListRequestTask mSearchListRequestTask;
    @Mock OutgoingPacket mOutgoingPacket;
    @Mock ProtocolSpec mProtocolSpec;
    @Mock ProtocolVersion mProtocolVersion;

    Future mDeviceListTaskFuture;
    Future mSearchListTaskFuture;

    SettingListInfo mDeviceSettingListInfo;
    SettingListInfo mSearchSettingListInfo;
    SparseArrayCompat<SparseArrayCompat> mListItems;
    Cursor mCursor;
    CarDeviceStatus mCarDeviceStatus;
    CursorLoader mCursorLoader;
    ContentObserver mContentObserver;
    SettingListInfoMap mSettingListInfoMap;
    SmartPhoneStatus mSmartPhoneStatus;
    ArgumentCaptor<SmartPhoneStatus> mCaptor = ArgumentCaptor.forClass(SmartPhoneStatus.class);

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);
    Instrumentation instr = InstrumentationRegistry.getInstrumentation();
    boolean isOnChangeCall;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mDeviceListTaskFuture = mock(Future.class);
        mSearchListTaskFuture = mock(Future.class);

        isOnChangeCall = false;
        mCarDeviceStatus = new CarDeviceStatus();
        mDeviceSettingListInfo = new SettingListInfo(DEVICE_LIST);
        mSearchSettingListInfo = new SettingListInfo(SEARCH_LIST);
        mListItems = new SparseArrayCompat<>();
        mSmartPhoneStatus = new SmartPhoneStatus();
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

        when(mStatusHolder.getSettingListInfoMap()).thenReturn(mSettingListInfoMap);
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);
        when(mStatusHolder.getSmartPhoneStatus()).thenReturn(mSmartPhoneStatus);
        when(mStatusHolder.getProtocolSpec()).thenReturn(mProtocolSpec);
        when(mStatusHolder.isSettingListSupported()).thenReturn(true);
        when(mTaskProvider.get()).thenReturn(mSettingInfoListRequestTask);
        when(mSettingInfoListRequestTask.setParams(any(SettingListType.class),any(SettingInfoListRequestTask.Callback.class))).thenReturn(mSettingInfoListRequestTask);
        when(mProtocolSpec.getConnectingProtocolVersion()).thenReturn(mProtocolVersion);
        when(mOutgoingPacketBuilder.createSmartPhoneStatusNotification(eq(mProtocolVersion), mCaptor.capture())).thenReturn(mOutgoingPacket);

        when(mTaskExecutor.submit(eq(mDeviceListRequestTask))).thenReturn(mDeviceListTaskFuture);
        when(mTaskExecutor.submit(eq(mSearchListRequestTask))).thenReturn(mSearchListTaskFuture);
        when(mBtSettingController.getAudioConnectStatus(anyString())).thenReturn(SettingListContract.DeviceList.AudioConnectStatus.STATUS_DEFAULT);
        when(mBtSettingController.getPhoneConnectStatus(anyString())).thenReturn(SettingListContract.DeviceList.PhoneConnectStatus.STATUS_DEFAULT);
        when(mBtSettingController.getDeleteStatus(anyString())).thenReturn(SettingListContract.DeviceList.DeleteStatus.STATUS_DEFAULT);
        when(mSettingInfoListRequestTask.setParams(eq(DEVICE_LIST), eq(mSettingListRepository))).thenReturn(mDeviceListRequestTask);
        when(mSettingInfoListRequestTask.setParams(eq(SEARCH_LIST), eq(mSettingListRepository))).thenReturn(mSearchListRequestTask);

        for(int i = 1; i <= 8; i++){
            DeviceListItem deviceListItem = new DeviceListItem.Builder()
                    .bdAddress("TEST_BD_ADDRESS_" + i)
                    .deviceName("TEST_DEVICE_NAME_" + i)
                    .audioSupported(i ==2)
                    .phoneSupported(i == 3)
                    .audioConnected(i == 4)
                    .phone1Connected(i == 5)
                    .phone2Connected(i == 6)
                    .lastAudioDevice(i == 7)
                    .sessionConnected(i == 8)
                    .build();
            mDeviceSettingListInfo.items.put(i, deviceListItem);
            mSettingListRepository.onGetItem(DEVICE_LIST, deviceListItem, i);
        }

        for(int i = 1; i <= 3; i++){
            SearchListItem searchListItem = new SearchListItem.Builder()
                    .bdAddress("TEST_BD_ADDRESS_" + i)
                    .deviceName("TEST_DEVICE_NAME_" + i)
                    .audioSupported(i ==2)
                    .phoneSupported(i == 3)
                    .build();
            mSearchSettingListInfo.items.put(i, searchListItem);
            mSettingListRepository.onGetItem(SEARCH_LIST, searchListItem, i);
        }

        Mockito.reset(mEventBus);
    }

    @After
    public void tearDown() throws Exception {
        // stop task
        mSettingListRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());
    }

    @Test
    public void initialize() throws Exception {
        // exercise
        mSettingListRepository.initialize();

        // verify
        verify(mEventBus).register(mSettingListRepository);
    }

    @Test
    public void getSettingList_NoData() throws Exception {
        // exercise
        mSettingListRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());
        instr.runOnMainSync(() ->
            mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createDeviceList())
        );
        Cursor cursor = mCursorLoader.loadInBackground();

        // verify
        assertThat(cursor.getCount(), is(0));
    }

    @Test
    public void getSettingList_DeviceList() throws Exception {
        // exercise
        instr.runOnMainSync(() ->
            mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createDeviceList())
        );
        Cursor cursor = mCursorLoader.loadInBackground();

        // verify
        assertThat(cursor.getCount(), is(8));
        while(cursor.moveToNext()){
            int position = cursor.getPosition() + 1;

            assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList._ID)), is(((long) position)));
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.BD_ADDRESS)), is("TEST_BD_ADDRESS_" + position));
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.DEVICE_NAME)), is("TEST_DEVICE_NAME_" + position));
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.AUDIO_SUPPORTED)), is(position == 2 ? 1 : 0));
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.PHONE_SUPPORTED)), is(position == 3 ? 1 : 0));
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.AUDIO_CONNECTED)), is(position == 4 ? 1 : 0));
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.PHONE_1_CONNECTED)), is(position == 5 ? 1 : 0));
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.PHONE_2_CONNECTED)), is(position == 6 ? 1 : 0));
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.LAST_AUDIO_DEVICE)), is(position == 7 ? 1 : 0));
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.SESSION_CONNECTED)), is(position == 8 ? 1 : 0));
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.AUDIO_CONNECT_STATUS)), is(SettingListContract.DeviceList.AudioConnectStatus.STATUS_DEFAULT.code));
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.PHONE_CONNECT_STATUS)), is(SettingListContract.DeviceList.PhoneConnectStatus.STATUS_DEFAULT.code));
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.DELETE_STATUS)), is(SettingListContract.DeviceList.DeleteStatus.STATUS_DEFAULT.code));
        }
    }

    @Test
    public void getSettingList_SearchList() throws Exception {
        // exercise
        instr.runOnMainSync(() ->
            mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createSearchList())
        );
        Cursor cursor = mCursorLoader.loadInBackground();

        // verify
        assertThat(cursor.getCount(), is(3));
        while(cursor.moveToNext()){
            int position = cursor.getPosition() + 1;

            assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(SettingListContract.SearchList._ID)), is(((long) position)));
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow(SettingListContract.SearchList.BD_ADDRESS)), is("TEST_BD_ADDRESS_" + position));
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow(SettingListContract.SearchList.DEVICE_NAME)), is("TEST_DEVICE_NAME_" + position));
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.SearchList.AUDIO_SUPPORTED)), is(position == 2 ? 1 : 0));
            assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.SearchList.PHONE_SUPPORTED)), is(position == 3 ? 1 : 0));
        }
    }

    @Test
    public void getSettingList_A2dpList() throws Exception {
        // exercise
        instr.runOnMainSync(() ->
            mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createA2dpList())
        );
        Cursor cursor = mCursorLoader.loadInBackground();

        // verify
        assertThat(cursor.getCount(), is(1));
        cursor.moveToFirst();
        assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList._ID)), is(2L));
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.BD_ADDRESS)), is("TEST_BD_ADDRESS_2"));
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.DEVICE_NAME)), is("TEST_DEVICE_NAME_2"));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.AUDIO_SUPPORTED)), is(1));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.PHONE_SUPPORTED)), is(0));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.AUDIO_CONNECTED)), is(0));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.PHONE_1_CONNECTED)), is(0));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.PHONE_2_CONNECTED)), is(0));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.LAST_AUDIO_DEVICE)), is(0));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.SESSION_CONNECTED)), is(0));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.AUDIO_CONNECT_STATUS)), is(SettingListContract.DeviceList.AudioConnectStatus.STATUS_DEFAULT.code));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.PHONE_CONNECT_STATUS)), is(SettingListContract.DeviceList.PhoneConnectStatus.STATUS_DEFAULT.code));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.DELETE_STATUS)), is(SettingListContract.DeviceList.DeleteStatus.STATUS_DEFAULT.code));
    }

    @Test
    public void getSettingList_AudioConnectedDevice() throws Exception {
        // exercise
        instr.runOnMainSync(() ->
            mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createAudioConnectedDevice())
        );
        Cursor cursor = mCursorLoader.loadInBackground();

        // verify
        assertThat(cursor.getCount(), is(1));
        cursor.moveToFirst();
        assertThat(cursor.getLong(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList._ID)), is(4L));
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.BD_ADDRESS)), is("TEST_BD_ADDRESS_4"));
        assertThat(cursor.getString(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.DEVICE_NAME)), is("TEST_DEVICE_NAME_4"));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.AUDIO_SUPPORTED)), is(0));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.PHONE_SUPPORTED)), is(0));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.AUDIO_CONNECTED)), is(1));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.PHONE_1_CONNECTED)), is(0));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.PHONE_2_CONNECTED)), is(0));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.LAST_AUDIO_DEVICE)), is(0));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.SESSION_CONNECTED)), is(0));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.AUDIO_CONNECT_STATUS)), is(SettingListContract.DeviceList.AudioConnectStatus.STATUS_DEFAULT.code));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.PHONE_CONNECT_STATUS)), is(SettingListContract.DeviceList.PhoneConnectStatus.STATUS_DEFAULT.code));
        assertThat(cursor.getInt(cursor.getColumnIndexOrThrow(SettingListContract.DeviceList.DELETE_STATUS)), is(SettingListContract.DeviceList.DeleteStatus.STATUS_DEFAULT.code));
    }

    @Test
    public void getAudioConnectedDevice() throws Exception {
        // exercise
        DeviceListItem actual = mSettingListRepository.getAudioConnectedDevice();

        // verify
        assertThat(actual, is(mDeviceSettingListInfo.items.get(4)));
    }

    @Test
    public void findByBdAddress_DeviceListItem() throws Exception {
        // exercise
        DeviceListItem actual = mSettingListRepository.findByBdAddress("TEST_BD_ADDRESS_3", DEVICE_LIST);

        // verify
        assertThat(actual, is(mDeviceSettingListInfo.items.get(3)));
    }

    @Test
    public void findByBdAddress_SearchListItem() throws Exception {
        // exercise
        SearchListItem actual = mSettingListRepository.findByBdAddress("TEST_BD_ADDRESS_2", SEARCH_LIST);

        // verify
        assertThat(actual, is(mSearchSettingListInfo.items.get(2)));
    }

    @Test
    public void onCrpSessionStartedEvent_HappyPath() throws Exception {
        // exercise
        mSettingListRepository.onCrpSessionStartedEvent(new CrpSessionStartedEvent());

        // verify
        verify(mSettingInfoListRequestTask).setParams(DEVICE_LIST, mSettingListRepository);
        verify(mTaskExecutor).submit(mDeviceListRequestTask);
    }

    @Test
    public void onCrpSessionStartedEvent_SettingListUnsupported() throws Exception {
        // setup
        when(mStatusHolder.isSettingListSupported()).thenReturn(false);

        // exercise
        mSettingListRepository.onCrpSessionStartedEvent(new CrpSessionStartedEvent());

        // verify
        verify(mSettingInfoListRequestTask, never()).setParams(any(SettingListType.class), any(SettingInfoListRequestTask.Callback.class));
        verify(mTaskExecutor, never()).submit(any(SettingInfoListRequestTask.class));
    }

    @Test
    public void onCrpSessionStoppedEvent_NotRunningTask() throws Exception {
        // exercise
        mSettingListRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());

        // verify
        verify(mEventBus, times(2)).post(any(SettingListRepositoryImpl.SettingListChangeEvent.class));
        verify(mDeviceListTaskFuture, never()).cancel(true);
        verify(mSearchListTaskFuture, never()).cancel(true);
    }

    @Test
    public void onCrpSessionStoppedEvent_RunningDeviceListTask() throws Exception {
        // setup
        when(mDeviceListTaskFuture.isDone()).thenReturn(false);
        mSettingListRepository.onCrpSettingListUpdateEvent(new CrpSettingListUpdateEvent(DEVICE_LIST));
        Mockito.reset(mEventBus);

        // exercise
        mSettingListRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());

        // verify
        verify(mEventBus, times(2)).post(any(SettingListRepositoryImpl.SettingListChangeEvent.class));
        verify(mDeviceListTaskFuture).cancel(true);
        verify(mSearchListTaskFuture, never()).cancel(true);
    }

    @Test
    public void onCrpSessionStoppedEvent_RunningSearchListTask() throws Exception {
        // setup
        when(mSearchListTaskFuture.isDone()).thenReturn(false);
        mSettingListRepository.onCrpSettingListUpdateEvent(new CrpSettingListUpdateEvent(SEARCH_LIST));
        Mockito.reset(mEventBus);

        // exercise
        mSettingListRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());

        // verify
        verify(mEventBus, times(2)).post(any(SettingListRepositoryImpl.SettingListChangeEvent.class));
        verify(mSearchListTaskFuture).cancel(true);
        verify(mDeviceListTaskFuture, never()).cancel(true);
    }

    @Test
    public void onGetItem_DeviceList() throws Exception {
        // setup
        SettingInfoListRequestTask.Callback callback = mSettingListRepository;
        ArgumentCaptor<SettingListRepositoryImpl.SettingListChangeEvent> captor = ArgumentCaptor.forClass(SettingListRepositoryImpl.SettingListChangeEvent.class);
        mSettingListRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());
        Mockito.reset(mEventBus);
        DeviceListItem deviceListItem = new DeviceListItem.Builder()
                .bdAddress("TEST")
                .build();

        // exercise
        callback.onGetItem(DEVICE_LIST, deviceListItem, 1);

        // verify
        verify(mEventBus).post(captor.capture());
        assertThat(captor.getValue().listType, is(DEVICE_LIST));

        instr.runOnMainSync(() ->
                mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createSearchList())
        );
        Cursor cursor = mCursorLoader.loadInBackground();
        assertThat(cursor.getCount(), is(0));

        instr.runOnMainSync(() ->
                mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createDeviceList())
        );
        cursor = mCursorLoader.loadInBackground();
        assertThat(cursor.getCount(), is(1));

    }

    @Test
    public void onGetItem_SearchList() throws Exception {
        // setup
        SettingInfoListRequestTask.Callback callback = mSettingListRepository;
        ArgumentCaptor<SettingListRepositoryImpl.SettingListChangeEvent> captor = ArgumentCaptor.forClass(SettingListRepositoryImpl.SettingListChangeEvent.class);
        mSettingListRepository.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());
        Mockito.reset(mEventBus);

        // exercise
        callback.onGetItem(SEARCH_LIST, mock(SearchListItem.class), 1);

        // verify
        verify(mEventBus).post(captor.capture());
        assertThat(captor.getValue().listType, is(SEARCH_LIST));

        instr.runOnMainSync(() ->
                mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createSearchList())
        );
        Cursor cursor = mCursorLoader.loadInBackground();
        assertThat(cursor.getCount(), is(1));

        instr.runOnMainSync(() ->
                mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createDeviceList())
        );
        cursor = mCursorLoader.loadInBackground();
        assertThat(cursor.getCount(), is(0));

    }

    @Test
    public void onCrpSettingListUpdateEvent_DeviceList() throws Exception {
        // exercise
        mSettingListRepository.onCrpSettingListUpdateEvent(new CrpSettingListUpdateEvent(DEVICE_LIST));

        // verify
        verify(mSettingInfoListRequestTask).setParams(DEVICE_LIST, mSettingListRepository);
        verify(mTaskExecutor).submit(mDeviceListRequestTask);
    }

    @Test
    public void onCrpSettingListUpdateEvent_SearchList() throws Exception {
        // exercise
        mSettingListRepository.onCrpSettingListUpdateEvent(new CrpSettingListUpdateEvent(SEARCH_LIST));

        // verify
        verify(mSettingInfoListRequestTask).setParams(SEARCH_LIST, mSettingListRepository);
        verify(mTaskExecutor).submit(mSearchListRequestTask);
    }

    @Test
    public void SettingListInfoCursor_constructorDeviceList() throws Exception {
        // exercise
        instr.runOnMainSync(() ->
                mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createDeviceList())
        );
        Cursor cursor = mCursorLoader.loadInBackground();

        // verify
        assertThat(mCaptor.getValue().showingDeviceList, is(true));
        assertThat(mCaptor.getValue().showingSearchList, is(false));
        verify(mEventBus).register(cursor);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void SettingListInfoCursor_constructorSearchList() throws Exception {
        // exercise
        instr.runOnMainSync(() ->
                mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createSearchList())
        );
        Cursor cursor = mCursorLoader.loadInBackground();

        // verify
        assertThat(mCaptor.getValue().showingDeviceList, is(false));
        assertThat(mCaptor.getValue().showingSearchList, is(true));
        verify(mEventBus).register(cursor);
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void SettingListInfoCursor_closeDeviceList() throws Exception {
        // exercise
        instr.runOnMainSync(() ->
                mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createDeviceList())
        );
        Cursor deviceCursor = mCursorLoader.loadInBackground();
        instr.runOnMainSync(() ->
                mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createSearchList())
        );
        Cursor searchCursor = mCursorLoader.loadInBackground();
        deviceCursor.close();

        // verify
        verify(mEventBus).register(deviceCursor);
        verify(mEventBus).register(searchCursor);
        assertThat(mCaptor.getValue().showingDeviceList, is(false));
        assertThat(mCaptor.getValue().showingSearchList, is(true));
        verify(mCarDeviceConnection, times(3)).sendPacket(mOutgoingPacket);
    }

    @Test
    public void SettingListInfoCursor_closeSearchList() throws Exception {
        // exercise
        instr.runOnMainSync(() ->
                mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createDeviceList())
        );
        Cursor deviceCursor = mCursorLoader.loadInBackground();
        instr.runOnMainSync(() ->
                mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createSearchList())
        );
        Cursor searchCursor = mCursorLoader.loadInBackground();
        searchCursor.close();

        // verify
        verify(mEventBus).register(deviceCursor);
        verify(mEventBus).register(searchCursor);
        assertThat(mCaptor.getValue().showingDeviceList, is(true));
        assertThat(mCaptor.getValue().showingSearchList, is(false));
        verify(mCarDeviceConnection, times(3)).sendPacket(mOutgoingPacket);
    }

    @Test
    public void SettingListInfoCursor_onSettingListChangeEvent_DeviceList() throws Exception {
        // setup
        mSignal = new CountDownLatch(2);
        SettingInfoListRequestTask.Callback callback = mSettingListRepository;
        instr.runOnMainSync(() ->
                mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createDeviceList())
        );
        Cursor cursor = mCursorLoader.loadInBackground();

        doAnswer(invocationOnMock -> {
            mMainHandler.post(() -> {
                ((SettingListRepositoryImpl.SettingListInfoCursor) cursor).onSettingListChangeEvent(new SettingListRepositoryImpl.SettingListChangeEvent(DEVICE_LIST));
                mSignal.countDown();
            });
            return null;
        }).when(mEventBus).post(any(SettingListRepositoryImpl.SettingListChangeEvent.class));

        // exercise
        callback.onGetItem(DEVICE_LIST, mock(DeviceListItem.class), 1);
        mSignal.await();

        // verify
        assertThat(isOnChangeCall, is(true));
    }

    @Test
    public void SettingListInfoCursor_onSettingListChangeEvent_DeviceList_WrongListChange() throws Exception {
        // setup
        SettingInfoListRequestTask.Callback callback = mSettingListRepository;
        instr.runOnMainSync(() ->
                mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createDeviceList())
        );
        Cursor cursor = mCursorLoader.loadInBackground();

        doAnswer(invocationOnMock -> {
            mMainHandler.post(() -> {
                ((SettingListRepositoryImpl.SettingListInfoCursor) cursor).onSettingListChangeEvent(new SettingListRepositoryImpl.SettingListChangeEvent(SEARCH_LIST));
                mSignal.countDown();
            });
            return null;
        }).when(mEventBus).post(any(SettingListRepositoryImpl.SettingListChangeEvent.class));

        // exercise
        callback.onGetItem(DEVICE_LIST, mock(DeviceListItem.class), 1);
        mSignal.await();

        // verify
        assertThat(isOnChangeCall, is(false));
    }

    @Test
    public void SettingListInfoCursor_onSettingListChangeEvent_SearchList() throws Exception {
        // setup
        mSignal = new CountDownLatch(2);
        SettingInfoListRequestTask.Callback callback = mSettingListRepository;
        instr.runOnMainSync(() ->
                mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createSearchList())
        );
        Cursor cursor = mCursorLoader.loadInBackground();

        doAnswer(invocationOnMock -> {
            mMainHandler.post(() -> {
                ((SettingListRepositoryImpl.SettingListInfoCursor) cursor).onSettingListChangeEvent(new SettingListRepositoryImpl.SettingListChangeEvent(SEARCH_LIST));
                mSignal.countDown();
            });
            return null;
        }).when(mEventBus).post(any(SettingListRepositoryImpl.SettingListChangeEvent.class));

        // exercise
        callback.onGetItem(SEARCH_LIST, mock(SearchListItem.class), 1);
        mSignal.await();

        // verify
        assertThat(isOnChangeCall, is(true));
    }

    @Test
    public void SettingListInfoCursor_onSettingListChangeEvent_SearchList_WrongListChange() throws Exception {
        // setup
        SettingInfoListRequestTask.Callback callback = mSettingListRepository;
        instr.runOnMainSync(() ->
                mCursorLoader = mSettingListRepository.getSettingList(SettingListContract.QuerySettingListParamsBuilder.createSearchList())
        );
        Cursor cursor = mCursorLoader.loadInBackground();

        doAnswer(invocationOnMock -> {
            mMainHandler.post(() -> {
                ((SettingListRepositoryImpl.SettingListInfoCursor) cursor).onSettingListChangeEvent(new SettingListRepositoryImpl.SettingListChangeEvent(DEVICE_LIST));
                mSignal.countDown();
            });
            return null;
        }).when(mEventBus).post(any(SettingListRepositoryImpl.SettingListChangeEvent.class));

        // exercise
        callback.onGetItem(SEARCH_LIST, mock(SearchListItem.class), 1);
        mSignal.await();

        // verify
        assertThat(isOnChangeCall, is(false));
    }

    @Test(expected = NullPointerException.class)
    public void getSettingList_ArgNull() throws Exception {
        // exercise
        CursorLoader cursorLoader = mSettingListRepository.getSettingList(null);
    }

    @Test(expected = NullPointerException.class)
    public void findByBdAddress_ArgBdAddressNull() throws Exception {
        // exercise
        SettingListItem settingListItem = mSettingListRepository.findByBdAddress(null, DEVICE_LIST);
    }

    @Test(expected = NullPointerException.class)
    public void findByBdAddress_ArgSettingListTypeNull() throws Exception {
        // exercise
        SettingListItem settingListItem = mSettingListRepository.findByBdAddress("TEST", null);
    }

}