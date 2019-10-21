package jp.pioneer.carsync.infrastructure.repository;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.CursorLoader;
import android.support.v4.os.OperationCanceledException;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Provider;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.BtSettingController;
import jp.pioneer.carsync.domain.content.QuerySettingListParams;
import jp.pioneer.carsync.domain.content.SettingListContract;
import jp.pioneer.carsync.domain.event.DeviceSearchStartEvent;
import jp.pioneer.carsync.domain.model.DeviceListItem;
import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.domain.model.SearchListItem;
import jp.pioneer.carsync.domain.model.SettingListInfo;
import jp.pioneer.carsync.domain.model.SettingListItem;
import jp.pioneer.carsync.domain.model.SettingListType;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.repository.SettingListRepository;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStartedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStoppedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSettingListUpdateEvent;
import jp.pioneer.carsync.infrastructure.task.SettingInfoListRequestTask;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.domain.model.SettingListType.DEVICE_LIST;
import static jp.pioneer.carsync.domain.model.SettingListType.SEARCH_LIST;

/**
 * SettingListRepositoryの実装.
 */
public class SettingListRepositoryImpl implements SettingListRepository, SettingInfoListRequestTask.Callback {
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject @ForInfrastructure ExecutorService mTaskExecutor;
    @Inject CarDeviceConnection mCarDeviceConnection;
    @Inject OutgoingPacketBuilder mPacketBuilder;
    @Inject Provider<SettingInfoListRequestTask> mTaskProvider;
    @Inject BtSettingController mBtSettingController;
    @Inject AppSharedPreference mPreference;
    private SettingListInfo mDeviceSettingListInfo = new SettingListInfo(DEVICE_LIST);
    private SettingListInfo mSearchSettingListInfo = new SettingListInfo(SEARCH_LIST);
    private Future mDeviceListTaskFuture;
    private Future mSearchListTaskFuture;
    private int mDeviceListCursorCount = 0;
    private int mSearchListCursorCount = 0;

    /** デバイスリスト用列名群 */
    private String[] mDeviceListColumnNames = {
            SettingListContract.DeviceList._ID,
            SettingListContract.DeviceList.BD_ADDRESS,
            SettingListContract.DeviceList.DEVICE_NAME,
            SettingListContract.DeviceList.AUDIO_SUPPORTED,
            SettingListContract.DeviceList.PHONE_SUPPORTED,
            SettingListContract.DeviceList.AUDIO_CONNECTED,
            SettingListContract.DeviceList.PHONE_1_CONNECTED,
            SettingListContract.DeviceList.PHONE_2_CONNECTED,
            SettingListContract.DeviceList.LAST_AUDIO_DEVICE,
            SettingListContract.DeviceList.SESSION_CONNECTED,
            SettingListContract.DeviceList.AUDIO_CONNECT_STATUS,
            SettingListContract.DeviceList.PHONE_CONNECT_STATUS,
            SettingListContract.DeviceList.DELETE_STATUS
    };

    /** サーチリスト用列名群 */
    private String[] mSearchListColumnName = {
            SettingListContract.SearchList._ID,
            SettingListContract.SearchList.BD_ADDRESS,
            SettingListContract.SearchList.DEVICE_NAME,
            SettingListContract.SearchList.AUDIO_SUPPORTED,
            SettingListContract.SearchList.PHONE_SUPPORTED
    };

    /**
     * コンストラクタ
     */
    @Inject
    public SettingListRepositoryImpl() {

    }

    /**
     * 初期化.
     */
    public void initialize() {
        Timber.i("initialize()");

        mEventBus.register(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized CursorLoader getSettingList(@NonNull QuerySettingListParams params) {
        Timber.i("getSettingList() params = %s", params);
        checkNotNull(params);
        return createSettingListInfoCursorLoader(checkNotNull(mContext), this, checkNotNull(params));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized DeviceListItem getAudioConnectedDevice() {
        synchronized (mDeviceSettingListInfo.items) {
            for (int i = 0; i < mDeviceSettingListInfo.items.size(); i++) {
                DeviceListItem item = (DeviceListItem) mDeviceSettingListInfo.items.valueAt(i);
                if (item != null && item.audioConnected) {
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T extends SettingListItem> T findByBdAddress(@NonNull String bdAddress, @NonNull SettingListType listType) {
        checkNotNull(bdAddress);
        checkNotNull(listType);

        SettingListInfo info;
        switch (listType) {
            case DEVICE_LIST: {
                info = mDeviceSettingListInfo;
                break;
            }
            case SEARCH_LIST: {
                info = mSearchSettingListInfo;
                break;
            }
            default:
                throw new AssertionError("can't happen.");
        }

        synchronized (info.items) {
            for (int i = 0; i < info.items.size(); i++) {
                //noinspection unchecked
                T item = (T) info.items.valueAt(i);
                if (item != null && TextUtils.equals(item.bdAddress, bdAddress)) return item;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNoneItem(SettingListType listType) {
        mEventBus.post(new SettingListChangeEvent(listType));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onGetItem(SettingListType listType, SettingListItem item, int listIndex) {
        switch (listType) {
            case DEVICE_LIST: {
                mDeviceSettingListInfo.items.put(listIndex, item);
                break;
            }
            case SEARCH_LIST: {
                mSearchSettingListInfo.items.put(listIndex, item);
                break;
            }
            default:
                throw new AssertionError("can't happen.");
        }

        mEventBus.post(new SettingListChangeEvent(listType));
    }

    /**
     * セッション開始イベントハンドラ.
     *
     * @param ev セッション開始イベント
     */
    @Subscribe
    public synchronized void onCrpSessionStartedEvent(CrpSessionStartedEvent ev) {
        Timber.i("onCrpSessionStartedEvent()");

        startTask(DEVICE_LIST);
    }

    /**
     * セッション停止イベントハンドラ.
     *
     * @param ev セッション停止イベント
     */
    @Subscribe
    public synchronized void onCrpSessionStoppedEvent(CrpSessionStoppedEvent ev) {
        Timber.i("onCrpSessionStoppedEvent()");

        stopTask(DEVICE_LIST);
        stopTask(SEARCH_LIST);
        mDeviceSettingListInfo.reset();
        mSearchSettingListInfo.reset();
        mEventBus.post(new SettingListChangeEvent(DEVICE_LIST));
        mEventBus.post(new SettingListChangeEvent(SEARCH_LIST));
    }

    /**
     * 設定リスト情報更新イベントハンドラ
     *
     * @param ev 設定リスト情報更新イベント
     */
    @Subscribe
    public synchronized void onCrpSettingListUpdateEvent(CrpSettingListUpdateEvent ev) {
        Timber.i("onCrpSettingListUpdateEvent() listType:%s" , ev.type.name());

        switch (ev.type) {
            case DEVICE_LIST: {
                mDeviceSettingListInfo.reset();
                break;
            }
            case SEARCH_LIST: {
                mSearchSettingListInfo.reset();
                break;
            }
            default:
                throw new AssertionError("can't happen.");
        }
        startTask(ev.type);
    }

    /**
     * デバイスサーチ開始イベントハンドラ.
     *
     * @param ev デバイスサーチ開始イベント
     */
    @Subscribe
    public synchronized void onDeviceSearchStartEvent(DeviceSearchStartEvent ev) {
        Timber.i("onDeviceSearchStartEvent()");

        stopTask(SEARCH_LIST);
        mSearchSettingListInfo.reset();
        mEventBus.post(new SettingListChangeEvent(SettingListType.SEARCH_LIST));
    }

    private void startTask(SettingListType listType) {
        if (!mStatusHolder.isSettingListSupported()) {
            return;
        }

        stopTask(listType);
        switch (listType) {
            case DEVICE_LIST: {
                mDeviceSettingListInfo.reset();
                SettingInfoListRequestTask task = mTaskProvider.get().setParams(DEVICE_LIST, this);
                mDeviceListTaskFuture = mTaskExecutor.submit(task);
                break;
            }
            case SEARCH_LIST: {
                mSearchSettingListInfo.reset();
                SettingInfoListRequestTask task = mTaskProvider.get().setParams(SEARCH_LIST, this);
                mSearchListTaskFuture = mTaskExecutor.submit(task);
                break;
            }
            default:
                throw new AssertionError("can't happen.");
        }
    }

    private void stopTask(SettingListType listType) {
        switch (listType) {
            case DEVICE_LIST: {
                if (isRunningTask(mDeviceListTaskFuture)) {
                    mDeviceListTaskFuture.cancel(true);
                    mDeviceListTaskFuture = null;
                }
                break;
            }
            case SEARCH_LIST: {
                if (isRunningTask(mSearchListTaskFuture)) {
                    mSearchListTaskFuture.cancel(true);
                    mSearchListTaskFuture = null;
                }
                break;
            }
            default:
                throw new AssertionError("can't happen.");
        }
    }

    private boolean isRunningTask(Future<?> future) {
        return (future != null && !future.isDone());
    }

    /**
     * Cursor生成時の設定リスト表示判定
     * <p>
     * 生成されたCursorのリスト種別によってカウントをインクリメントし、
     * その結果によって設定リストを表示するよう車載器にパケットを送信する。
     */
    private void judgeShowingListForCreateCursor(SettingListType listType) {
        switch (listType) {
            case DEVICE_LIST: {
                mDeviceListCursorCount++;
                break;
            }
            case SEARCH_LIST: {
                mSearchListCursorCount++;
                break;
            }
            default:
                throw new AssertionError("can't happen.");
        }

        boolean isShowingDeviceList = mDeviceListCursorCount > 0;
        boolean isShowingSearchList = mSearchListCursorCount > 0;

        SmartPhoneStatus status = mStatusHolder.getSmartPhoneStatus();
        if (status.showingDeviceList != isShowingDeviceList || status.showingSearchList != isShowingSearchList) {
            status.showingDeviceList = isShowingDeviceList;
            status.showingSearchList = isShowingSearchList;

            ProtocolVersion version = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion();
            OutgoingPacket packet = mPacketBuilder.createSmartPhoneStatusNotification(version, status);
            mCarDeviceConnection.sendPacket(packet);
        }
    }

    /**
     * Cursor終了時の設定リスト表示判定
     * <p>
     * 生成されたCursorのリスト種別によってカウントをデクリメントし、
     * その結果によって設定リストを表示するよう車載器にパケットを送信する。
     */
    private void judgeShowingListForCloseCursor(SettingListType listType) {
        switch (listType) {
            case DEVICE_LIST: {
                mDeviceListCursorCount--;
                break;
            }
            case SEARCH_LIST: {
                mSearchListCursorCount--;
                break;
            }
            default:
                throw new AssertionError("can't happen.");
        }

        boolean isShowingDeviceList = mDeviceListCursorCount > 0;
        boolean isShowingSearchList = mSearchListCursorCount > 0;

        SmartPhoneStatus status = mStatusHolder.getSmartPhoneStatus();
        if (status.showingDeviceList != isShowingDeviceList || status.showingSearchList != isShowingSearchList) {
            status.showingDeviceList = isShowingDeviceList;
            status.showingSearchList = isShowingSearchList;

            ProtocolVersion version = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion();
            OutgoingPacket packet = mPacketBuilder.createSmartPhoneStatusNotification(version, status);
            mCarDeviceConnection.sendPacket(packet);
        }
    }

    /**
     * SettingListInfoCursorLoader生成.
     * <p>
     * UnitTest用
     */
    @VisibleForTesting
    SettingListInfoCursorLoader createSettingListInfoCursorLoader(Context context,
                                                                 SettingListRepositoryImpl repository,
                                                                 QuerySettingListParams params) {
        return new SettingListInfoCursorLoader(context,repository,params);
    }

    /**
     * 設定リストCursorLoader.
     */
    static class SettingListInfoCursorLoader extends CursorLoader {
        private final ContentObserver mObserver;
        private final SettingListRepositoryImpl mRepository;
        private final QuerySettingListParams mParams;

        /**
         * コンストラクタ.
         *
         * @param context コンテキスト
         * @param params  取得パラメータ
         */
        public SettingListInfoCursorLoader(@NonNull Context context,
                                           @NonNull SettingListRepositoryImpl repository,
                                           @NonNull QuerySettingListParams params) {
            super(checkNotNull(context));
            mObserver = createForceLoadContentObserver();
            mRepository = checkNotNull(repository);
            mParams = checkNotNull(params);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Cursor loadInBackground() {
            synchronized (this) {
                if (isLoadInBackgroundCanceled()) {
                    throw new OperationCanceledException();
                }
            }

            synchronized (mRepository) {
                switch (mParams.settingListType) {
                    case DEVICE_LIST:
                        return createDeviceListCursor();
                    case SEARCH_LIST:
                        return createSearchListCursor();
                    default:
                        throw new AssertionError("can't happen.");
                }
            }
        }

        private Cursor createDeviceListCursor() {
            SparseArrayCompat<SettingListItem> listItems = mRepository.mDeviceSettingListInfo.items;
            SettingListInfoCursor cursor = new SettingListInfoCursor(mRepository.mDeviceListColumnNames, mRepository.mEventBus, listItems.size(), DEVICE_LIST, mRepository);
            cursor.registerContentObserver(mObserver);
            // 歯抜けはありえないはずなのでvalueAtで取得する
            for (int i = 0; i < listItems.size(); i++) {
                DeviceListItem item = (DeviceListItem) listItems.valueAt(i);
                if (mParams.audioSupported && !item.audioSupported) {
                    continue;
                }
                if (mParams.phoneSupported && !item.phoneSupported) {
                    continue;
                }
                if (mParams.audioConnected && !item.audioConnected) {
                    continue;
                }

                cursor.addRow(new Object[]{
                        (long) (i + 1),
                        item.bdAddress,
                        item.deviceName,
                        item.audioSupported ? 1 : 0,
                        item.phoneSupported ? 1 : 0,
                        item.audioConnected ? 1 : 0,
                        item.phone1Connected ? 1 : 0,
                        item.phone2Connected ? 1 : 0,
                        item.lastAudioDevice ? 1 : 0,
                        item.sessionConnected ? 1 : 0,
                        mRepository.mBtSettingController.getAudioConnectStatus(item.bdAddress).code,
                        mRepository.mBtSettingController.getPhoneConnectStatus(item.bdAddress).code,
                        mRepository.mBtSettingController.getDeleteStatus(item.bdAddress).code
                });
            }

            return cursor;
        }

        private Cursor createSearchListCursor() {
            SparseArrayCompat<SettingListItem> listItems = mRepository.mSearchSettingListInfo.items;
            SettingListInfoCursor cursor = new SettingListInfoCursor(mRepository.mSearchListColumnName, mRepository.mEventBus, listItems.size(), SEARCH_LIST, mRepository);
            cursor.registerContentObserver(mObserver);
            // 歯抜けはありえないはずなのでvalueAtで取得する
            for (int i = 0; i < listItems.size(); i++) {
                SearchListItem item = (SearchListItem) listItems.valueAt(i);
                cursor.addRow(new Object[]{
                        (long) (i + 1),
                        item.bdAddress,
                        item.deviceName,
                        item.audioSupported ? 1 : 0,
                        item.phoneSupported ? 1 : 0
                });
            }

            return cursor;
        }

        /**
         * ForceLoadContentObserver生成.
         * <p>
         * UnitTest用
         */
        @VisibleForTesting
        ContentObserver createForceLoadContentObserver() {
            return new ForceLoadContentObserver();
        }
    }

    /**
     * 車載機設定リストCursor.
     */
    static class SettingListInfoCursor extends MatrixCursor {
        private EventBus mEventBus;
        private SettingListType mListType;
        private SettingListRepositoryImpl mRepository;

        /**
         * コンストラクタ
         *
         * @param columnNames     列名群
         * @param eventBus        イベントバス
         * @param initialCapacity 初期キャパシティ
         * @param listType        設定リスト種別
         */
        public SettingListInfoCursor(@NonNull String[] columnNames, @NonNull EventBus eventBus, int initialCapacity, SettingListType listType, SettingListRepositoryImpl repository) {
            super(columnNames, initialCapacity);
            mEventBus = eventBus;
            mEventBus.register(this);
            mListType = listType;
            mRepository = repository;

            mRepository.judgeShowingListForCreateCursor(mListType);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() {
            super.close();
            mEventBus.unregister(this);

            mRepository.judgeShowingListForCloseCursor(mListType);
        }

        /**
         * 車載機設定リスト更新イベントハンドラ.
         *
         * @param ev 車載機設定リスト更新イベント
         */
        @Subscribe
        public void onSettingListChangeEvent(SettingListChangeEvent ev) {
            Timber.i("onSettingListChangeEvent()");
            // 同種別のみchange
            if (ev.listType == mListType) {
                onChange(false);
            }
        }
    }

    /**
     * 車載機設定リスト更新イベント.
     */
    public static class SettingListChangeEvent {
        /** リスト種別 */
        public SettingListType listType;

        /**
         * コンストラクタ.
         *
         * @param listType リスト種別種別
         * @throws NullPointerException {@code listType}がnull
         */
        public SettingListChangeEvent(@NonNull SettingListType listType) {
            this.listType = checkNotNull(listType);
        }

    }
}
