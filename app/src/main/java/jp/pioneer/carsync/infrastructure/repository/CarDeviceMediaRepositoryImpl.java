package jp.pioneer.carsync.infrastructure.repository;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.CursorLoader;
import android.support.v4.os.OperationCanceledException;
import android.support.v4.util.SparseArrayCompat;

import com.annimon.stream.Stream;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Provider;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.content.TunerContract.ListItemContract.Dab;
import jp.pioneer.carsync.domain.content.TunerContract.ListItemContract.HdRadio;
import jp.pioneer.carsync.domain.content.TunerContract.ListItemContract.Radio;
import jp.pioneer.carsync.domain.content.TunerContract.ListItemContract.SiriusXm;
import jp.pioneer.carsync.domain.content.UsbListContract;
import jp.pioneer.carsync.domain.event.DabInfoChangeEvent;
import jp.pioneer.carsync.domain.event.HdRadioInfoChangeEvent;
import jp.pioneer.carsync.domain.event.RadioInfoChangeEvent;
import jp.pioneer.carsync.domain.model.BandType;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.DabBandType;
import jp.pioneer.carsync.domain.model.DabInfo;
import jp.pioneer.carsync.domain.model.HdRadioBandType;
import jp.pioneer.carsync.domain.model.HdRadioInfo;
import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.RdsInterruptionType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SxmBandType;
import jp.pioneer.carsync.domain.model.TunerStatus;
import jp.pioneer.carsync.domain.repository.CarDeviceMediaRepository;
import jp.pioneer.carsync.infrastructure.crp.event.CrpListUpdateEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStartedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStoppedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpStatusUpdateEvent;
import jp.pioneer.carsync.infrastructure.task.CarDeviceMediaListRequestTask;
import jp.pioneer.carsync.infrastructure.task.UsbListRequestTask;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.domain.model.ListType.LIST;
import static jp.pioneer.carsync.domain.model.ListType.LIST_UNAVAILABLE;
import static jp.pioneer.carsync.domain.model.ListType.NOT_LIST;
import static jp.pioneer.carsync.domain.model.ListType.PCH_LIST;
import static jp.pioneer.carsync.domain.model.ListType.SERVICE_LIST;

/**
 * CarDeviceMediaRepositoryの実装.
 * <p>
 * HD_RADIOとDABは、アプリでサポートすることになったら実装する。
 * USBは通信プロトコルが対応したら実装する。
 * <p>
 * P.CHリストの取得はリスト画面以外でも表示するため、P.CHリストをサポートしている
 * ソースになったらP.CHリストの取得を行う。
 * 取得出来なかった場合を考慮し、{@link #mIsDirtyCarDeviceMediaList}で取得出来たかを
 * 管理している。{@link #mIsDirtyCarDeviceMediaList}が{@code true}の場合、リストの取得が
 * 必要だが取得が終了していないことを示す。<br>
 * P.CHリスト以外のリストはリスト状態に応じて取得を行う。
 */
@SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
public class CarDeviceMediaRepositoryImpl implements CarDeviceMediaRepository {
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    @Inject @ForInfrastructure ExecutorService mTaskExecutor;
    @Inject Provider<CarDeviceMediaListRequestTask> mMediaListTaskProvider;
    @Inject Provider<UsbListRequestTask> mUsbListTaskProvider;
    private SparseArrayCompat<ListInfo.ListItem> mRadioListItems = new SparseArrayCompat<>();
    private SparseArrayCompat<ListInfo.ListItem> mDabListItems = new SparseArrayCompat<>();     // [Ver2.5]
    private SparseArrayCompat<ListInfo.ListItem> mHdRadioListItems = new SparseArrayCompat<>(); // [Ver2.5]
    private SparseArrayCompat<ListInfo.ListItem> mSxmListItems = new SparseArrayCompat<>();
    private SparseArrayCompat<ListInfo.ListItem> mUsbListItems = new SparseArrayCompat<>();
    private Future<?> mTaskFuture;
    private ListType mCurrentListType;
    private MediaSourceType mCurrentSourceType;
    private RdsInterruptionType mInterruptionType;
    private TunerStatus mTunerStatus;
    private boolean mIsDirtyCarDeviceMediaList = true;
    private boolean mIsUsbListInitialization = false;
    private ArrayList<Integer> mRequestQueue = new ArrayList<>();

    private CarDeviceMediaListRequestTask.Callback mMediaListCallback = new CarDeviceMediaListRequestTask.Callback() {
        @Override
        public synchronized void onFinish() {
            onTaskFinish();
        }
    };

    private UsbListRequestTask.Callback mUsbListCallback = new UsbListRequestTask.Callback() {
        @Override
        public synchronized void onFinish(UsbListRequestTask.RequestType requestType) {
            if(requestType == UsbListRequestTask.RequestType.LIST_INFO){
                mIsUsbListInitialization = true;
            }
            onTaskFinish();
            stopTask();
            startTaskIfExistQueue();
        }
    };

    /**
     * コンストラクタ.
     */
    @Inject
    public CarDeviceMediaRepositoryImpl() {
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
    public CursorLoader getPresetChannelList(@NonNull MediaSourceType sourceType, @Nullable BandType bandType) {
        Timber.i("getPresetChannelList() sourceType = %s, bandType = %s", sourceType, bandType);
        checkArgument(checkNotNull(sourceType).isPchListSupported());

        return createCarDeviceMediaCursorLoader(checkNotNull(mContext), this, sourceType, bandType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CursorLoader getUsbList() {
        Timber.i("getUsbList()");

        return createCarDeviceMediaCursorLoader(checkNotNull(mContext), this, MediaSourceType.USB, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CursorLoader getDabList() {
        Timber.i("getDabList()");

        return createCarDeviceMediaCursorLoader(checkNotNull(mContext), this, MediaSourceType.DAB, null);
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public synchronized ListInfo.ListItem getListItem(@NonNull MediaSourceType sourceType, @IntRange(from = 1) int listIndex) {
        Timber.i("getListItem() sourceType = %s, listIndex = %d", sourceType, listIndex);
        checkNotNull(sourceType);
        checkArgument(1 <= listIndex);

        switch (sourceType) {
            case RADIO:
                return mRadioListItems.get(listIndex);
            case DAB:
                return mDabListItems.get(listIndex);
            case HD_RADIO:
                return mHdRadioListItems.get(listIndex);
            case SIRIUS_XM:
                return mSxmListItems.get(listIndex);
            case USB:
                return mUsbListItems.get(listIndex);
            default:
                return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWantedUsbListItemIndex(int index) {
        if(mStatusHolder.getListInfo().transactionInfo.total < index){
            return;
        }

        if (mUsbListItems.get(index) != null) {
            return;
        }

        if(mRequestQueue.size() > 0){
            if(mRequestQueue.get(0) == index){
                return;
            }
        }

        synchronized (this) {
            Integer listIndex = index;
            if(mRequestQueue.contains(listIndex)){
                mRequestQueue.remove(listIndex);
            }
            Stream.of(mRequestQueue)
                .distinct()
                .filter(value -> value <= mStatusHolder.getListInfo().transactionInfo.total)
                .collect(com.annimon.stream.Collectors.toList());
            mRequestQueue.add(0, listIndex);
            Timber.i("addWantedUsbListItemIndex() index = %d, size = %d, mRequestQueue = %s", index, mRequestQueue.size(), mRequestQueue);

            startTaskIfExistQueue();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeWantedUsbListItemIndex(int index) {
        Timber.i("removeWantedUsbListItemIndex() index = %d", index);

        synchronized (this) {
            Integer listIndex = index;
            if (mRequestQueue.contains(listIndex)) {
                mRequestQueue.remove(listIndex);
            }
        }
    }

    private synchronized void onTaskFinish() {
        Timber.i("onFinish()");

        ListInfo.TransactionInfo transactionInfo = mStatusHolder.getListInfo().transactionInfo;
        switch (transactionInfo.sourceType) {
            case RADIO:
                mRadioListItems = transactionInfo.items;
                break;
            case DAB:
                mDabListItems = transactionInfo.items;
                break;
            case HD_RADIO:
                mHdRadioListItems = transactionInfo.items;
                break;
            case SIRIUS_XM:
                mSxmListItems = transactionInfo.items;
                break;
            case USB:
                mUsbListItems = transactionInfo.items;
                break;
            default:
                throw new AssertionError("can't happen.");
        }

        mIsDirtyCarDeviceMediaList = false;
        mEventBus.post(new CarDeviceMediaListChangedEvent());
    }

    /**
     * セッション開始イベントハンドラ.
     *
     * @param ev セッション開始ンイベント
     */
    @Subscribe
    public synchronized void onCrpSessionStartedEvent(CrpSessionStartedEvent ev) {
        Timber.i("onCrpSessionStartedEvent()");

        CarDeviceStatus status = mStatusHolder.getCarDeviceStatus();
        mCurrentListType = status.listType;
        mCurrentSourceType = status.sourceType;
        if (mCurrentSourceType.isPchListSupported()) {
            if(!isRdsInterrupted()){
                startTask();
            }
        } else if (mCurrentSourceType == MediaSourceType.USB
            && mCurrentListType == LIST){
            resetUsbList();
            startTaskIfExistQueue();
        }
    }

    /**
     * セッション停止イベントハンドラ.
     *
     * @param ev セッション停止イベント
     */
    @Subscribe
    public synchronized void onCrpSessionStoppedEvent(CrpSessionStoppedEvent ev) {
        Timber.i("onCrpSessionStoppedEvent()");

        stopTask();
        CarDeviceStatus status = mStatusHolder.getCarDeviceStatus();
        mCurrentListType = status.listType;
        mCurrentSourceType = status.sourceType;
        mIsDirtyCarDeviceMediaList = true;
        mRadioListItems = new SparseArrayCompat<>();
        mDabListItems = new SparseArrayCompat<>();
        mHdRadioListItems = new SparseArrayCompat<>();
        mSxmListItems = new SparseArrayCompat<>();
        mUsbListItems = new SparseArrayCompat<>();
        resetUsbList();
        mEventBus.post(new CarDeviceMediaListChangedEvent());
    }

    /**
     * リスト情報更新イベントハンドラ
     *
     * @param ev リスト情報更新イベント
     */
    @Subscribe
    public synchronized void onCrpListUpdateEvent(CrpListUpdateEvent ev) {
        Timber.i("onCrpListUpdateEvent()");

        mIsDirtyCarDeviceMediaList = true;
        if(mCurrentSourceType == MediaSourceType.USB){
            stopTask();
            resetUsbList();
            startTaskIfExistQueue();
        } else {
            startTask();
        }
    }

    /**
     * StatusHolder更新イベントハンドラ.
     *
     * @param ev StatusHolder更新イベント
     */
    @Subscribe
    public synchronized void onCrpStatusUpdateEvent(CrpStatusUpdateEvent ev) {
        CarDeviceStatus status = mStatusHolder.getCarDeviceStatus();
        if (status.sourceType != mCurrentSourceType && status.sourceStatus == MediaSourceStatus.CHANGE_COMPLETED) {
            mCurrentSourceType = status.sourceType;
            onSourceChanged();
        }

        if (status.listType != mCurrentListType) {
            mCurrentListType = status.listType;
            onListTypeChanged();
        }
    }

    /**
     * ラジオ情報更新イベントハンドラ
     *
     * @param ev ラジオ情報更新イベント
     */
    @Subscribe
    public synchronized void onRadioInfoChangeEvent(RadioInfoChangeEvent ev) {
        RadioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().radioInfo;
        try{
            if(mCurrentSourceType == MediaSourceType.RADIO) {
                if (info.rdsInterruptionType != null &&
                    mInterruptionType != null &&
                    mInterruptionType != RdsInterruptionType.NORMAL &&
                    info.rdsInterruptionType == RdsInterruptionType.NORMAL) {
                    if(mIsDirtyCarDeviceMediaList) {
                        startTask();
                        return;
                    }
                }

                switch (info.tunerStatus){
                    case SEEK:
                    case PTY_SEARCH:
                    case PI_SEARCH:
                    case BSM:
                        if(mTunerStatus != info.tunerStatus){
                            stopTask();
                        }
                        return;
                    case NORMAL:
                        if(mTunerStatus == TunerStatus.SEEK ||
                            mTunerStatus == TunerStatus.PTY_SEARCH ||
                            mTunerStatus == TunerStatus.PI_SEARCH){
                            if(mIsDirtyCarDeviceMediaList) {
                                startTask();
                            }
                        }
                        return;
                }
            }
        } finally {
            mInterruptionType = info.rdsInterruptionType;
            mTunerStatus = info.tunerStatus;
        }
    }

    /**
     * HD Radio情報更新イベントハンドラ
     *
     * @param ev HD Radio情報更新イベント
     */
    @Subscribe
    public synchronized void onHdRadioInfoChangeEvent(HdRadioInfoChangeEvent ev) {
        final HdRadioInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().hdRadioInfo;
        try {
            if (mCurrentSourceType == MediaSourceType.HD_RADIO) {
                switch (info.tunerStatus) {
                    case SEEK:
                    case BSM:
                        if (mTunerStatus != info.tunerStatus) {
                            stopTask();
                        }
                        break;

                    case NORMAL:
                        if (mTunerStatus == TunerStatus.SEEK) {
                            if (mIsDirtyCarDeviceMediaList) {
                                startTask();
                            }
                        }
                        break;
                }
            }
        } finally {
            mTunerStatus = info.tunerStatus;
        }
    }

    /**
     * DAB情報更新イベントハンドラ
     *
     * @param ev DAB情報更新イベント
     */
    @Subscribe
    public synchronized void onDabInfoChangeEvent(DabInfoChangeEvent ev) {
        final DabInfo info = mStatusHolder.getCarDeviceMediaInfoHolder().dabInfo;
        if (mCurrentSourceType == MediaSourceType.DAB) {
            switch (info.tunerStatus) {
                case SEEK:
                case LIST_UPDATE:
                    stopTask();
                    break;
            }
        }
    }

    private void onSourceChanged() {
        Timber.d("onSourceChanged()");
        mIsDirtyCarDeviceMediaList = true;
        if (mCurrentSourceType.isPchListSupported()) {
            if(isRdsInterrupted()){
                stopTask();
            } else {
                startTask();
            }
        } else if (!mCurrentSourceType.isListSupported()) {
            stopTask();
        }
    }

    private void onListTypeChanged() {
        Timber.d("onListTypeChanged()");
        if (mCurrentSourceType.isPchListSupported()) {
            if (mIsDirtyCarDeviceMediaList && !isRunningTask()) {
                if(!isRdsInterrupted()){
                    startTask();
                }
            }
        } else {
            if (mCurrentListType == LIST && mCurrentSourceType == MediaSourceType.USB){
                startTaskIfExistQueue();
            } else if (mCurrentListType == NOT_LIST || mCurrentListType == LIST_UNAVAILABLE) {
                mIsDirtyCarDeviceMediaList = true;
                stopTask();
                resetUsbList();
            } else if (mCurrentSourceType == MediaSourceType.DAB &&
                    (mCurrentListType == ListType.SERVICE_LIST || mCurrentListType == ListType.ABC_SEARCH_LIST
                            || mCurrentListType == ListType.PTY_NEWS_INFO_LIST|| mCurrentListType == ListType.PTY_POPULER_LIST
                            || mCurrentListType == ListType.PTY_CLASSICS_LIST|| mCurrentListType == ListType.PTY_OYHERS_LIST
                            || mCurrentListType == ListType.ENSEMBLE_CATEGORY|| mCurrentListType == ListType.ENSEMBLE_LIST)) {
                startTask();
            }
        }
    }

    private void startTaskIfExistQueue() {
        if (isRunningTask()) {
            return;
        }

        if(!mIsUsbListInitialization){
            startUsbListInitialization();
            return;
        }

        if (mRequestQueue.size() > 0) {
            Integer index = mRequestQueue.get(0);
            mRequestQueue.remove(0);
            ListInfo.TransactionInfo transactionInfo = mStatusHolder.getListInfo().transactionInfo;
            transactionInfo.listIndex = index;
            transactionInfo.id++;

            startTask();
        }
    }

    private void startUsbListInitialization(){
        mRequestQueue.clear();
        mUsbListItems = new SparseArrayCompat<>();
        stopTask();
        Runnable task = mUsbListTaskProvider.get().setParams(mUsbListCallback, UsbListRequestTask.RequestType.LIST_INFO);
        mTaskFuture = mTaskExecutor.submit(task);
    }

    private void startTask() {
        if (!mStatusHolder.isListSupported()) {
            return;
        }

        stopTask();
        Runnable task;
        switch (mCurrentSourceType) {
            case RADIO:
            case HD_RADIO:
            case SIRIUS_XM:
                task = mMediaListTaskProvider.get().setParams(mCurrentSourceType, PCH_LIST, mMediaListCallback);
                break;
            case DAB:
                if(mCurrentListType== SERVICE_LIST) {
                    task = mMediaListTaskProvider.get().setParams(mCurrentSourceType, SERVICE_LIST, mMediaListCallback);
                }else{
                    task = mMediaListTaskProvider.get().setParams(mCurrentSourceType, mCurrentListType, mMediaListCallback);
                }
                break;
            case USB:
                task = mUsbListTaskProvider.get().setParams(mUsbListCallback, UsbListRequestTask.RequestType.ITEM_INFO);
                break;
            default:
                return;
        }

        mTaskFuture = mTaskExecutor.submit(task);
    }

    private void stopTask() {
        if (isRunningTask()) {
            mTaskFuture.cancel(true);
            mTaskFuture = null;
        }
    }

    private void resetUsbList(){
        mIsUsbListInitialization = false;
        mUsbListItems = new SparseArrayCompat<>();
        mRequestQueue.clear();
    }

    private boolean isRunningTask() {
        return (mTaskFuture != null && !mTaskFuture.isDone());
    }

    private boolean isRdsInterrupted(){
        if(mCurrentSourceType == MediaSourceType.RADIO){
            RdsInterruptionType type = mStatusHolder.getCarDeviceMediaInfoHolder().radioInfo.rdsInterruptionType;
            return type != null && type != RdsInterruptionType.NORMAL;
        }
        return false;
    }

    /**
     * CarDeviceMediaCursorLoader生成.
     * <p>
     * UnitTest用
     */
    @VisibleForTesting
    CarDeviceMediaCursorLoader createCarDeviceMediaCursorLoader(Context context,
                                                                CarDeviceMediaRepositoryImpl repository,
                                                                MediaSourceType sourceType,
                                                                BandType bandType) {
        return new CarDeviceMediaCursorLoader(context, repository, sourceType, bandType);
    }

    /**
     * 車載機メディアCursorLoader.
     */
    static class CarDeviceMediaCursorLoader extends CursorLoader {
        private final ContentObserver mObserver;
        private final CarDeviceMediaRepositoryImpl mRepository;
        private final MediaSourceType mSourceType;
        private final BandType mBandType;

        /**
         * コンストラクタ.
         *
         * @param context    コンテキスト
         * @param sourceType ソース種別
         * @param bandType   ハンド種別
         */
        public CarDeviceMediaCursorLoader(@NonNull Context context,
                                          @NonNull CarDeviceMediaRepositoryImpl repository,
                                          @NonNull MediaSourceType sourceType,
                                          @Nullable BandType bandType) {
            super(checkNotNull(context));
            mObserver = createForceLoadContentObserver();
            mRepository = checkNotNull(repository);
            mSourceType = checkNotNull(sourceType);
            mBandType = bandType;
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
                switch (mSourceType) {
                    case RADIO:
                        return createRadioCursor();
                    case HD_RADIO:
                        return createHdRadioCursor();
                    case DAB:
                        return createDabCursor();
                    case SIRIUS_XM:
                        return createSxmCursor();
                    case USB:
                        return createUsbCursor();
                    default:
                        throw new AssertionError("can't happen.");
                }
            }
        }

        @SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
        private Cursor createRadioCursor() {
            String[] columnNames = {
                Radio._ID,
                Radio.LIST_INDEX,
                Radio.TEXT,
                Radio.PCH_NUMBER,
                Radio.FREQUENCY,
                Radio.FREQUENCY_UNIT,
                Radio.BAND_TYPE
            };

            SparseArrayCompat<ListInfo.ListItem> listItems = mRepository.mRadioListItems;
            CarDeviceMediaCursor cursor = new CarDeviceMediaCursor(columnNames, mRepository.mEventBus, listItems.size());
            cursor.registerContentObserver(mObserver);
            RadioBandType filter = (mBandType == null) ? null : (RadioBandType) mBandType;
            // 歯抜けはありえないはずなのでvalueAtで取得する
            for (int i = 0; i < listItems.size(); i++) {
                ListInfo.RadioListItem item = (ListInfo.RadioListItem) listItems.valueAt(i);
                RadioBandType bandType = ((RadioBandType) item.getBand());
                if (filter == null || filter == bandType) {
                    cursor.addRow(new Object[]{
                        (long) (i + 1),
                        item.listIndex,
                        item.text,
                        item.getPchNumber(),
                        item.getFrequency(),
                        item.getFrequencyUnit().name(),
                        bandType.name()
                    });
                }
            }

            return cursor;
        }

        @SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
        private Cursor createHdRadioCursor() {
            String[] columnNames = {
                HdRadio._ID,
                HdRadio.LIST_INDEX,
                HdRadio.TEXT,
                HdRadio.PCH_NUMBER,
                HdRadio.FREQUENCY,
                HdRadio.FREQUENCY_UNIT,
                HdRadio.BAND_TYPE
            };

            SparseArrayCompat<ListInfo.ListItem> listItems = mRepository.mHdRadioListItems;
            CarDeviceMediaCursor cursor = new CarDeviceMediaCursor(columnNames, mRepository.mEventBus, listItems.size());
            cursor.registerContentObserver(mObserver);
            HdRadioBandType filter = (mBandType == null) ? null : (HdRadioBandType) mBandType;
            // 歯抜けはありえないはずなのでvalueAtで取得する
            for (int i = 0; i < listItems.size(); i++) {
                ListInfo.RadioListItem item = (ListInfo.RadioListItem) listItems.valueAt(i);
                HdRadioBandType bandType = ((HdRadioBandType) item.getBand());
                if (filter == null || filter == bandType) {
                    cursor.addRow(new Object[]{
                        (long) (i + 1),
                        item.listIndex,
                        item.text,
                        item.getPchNumber(),
                        item.getFrequency(),
                        item.getFrequencyUnit().name(),
                        bandType.name()
                    });
                }
            }

            return cursor;
        }

        @SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
        private Cursor createDabCursor() {
            String[] columnNames = {
                Dab._ID,
                Dab.LIST_INDEX,
                Dab.TEXT,
                Dab.INDEX,
                Dab.EID,
                Dab.SID,
                Dab.SCIDS,
            };

            SparseArrayCompat<ListInfo.ListItem> listItems = mRepository.mDabListItems;
            CarDeviceMediaCursor cursor = new CarDeviceMediaCursor(columnNames, mRepository.mEventBus, listItems.size());
            cursor.registerContentObserver(mObserver);
            DabBandType filter = (mBandType == null) ? null : (DabBandType) mBandType;
            // 歯抜けはありえないはずなのでvalueAtで取得する
            for (int i = 0; i < listItems.size(); i++) {
                ListInfo.DabListItem item = (ListInfo.DabListItem) listItems.valueAt(i);
                DabBandType bandType = ((DabBandType) item.getBand());
                if (filter == null || filter == bandType) {
                    cursor.addRow(new Object[]{
                        (long) (i + 1),
                        item.listIndex,
                        item.text,
                        item.index,
                        item.eid,
                        item.sid,
                        item.scids
                    });
                }
            }

            return cursor;
        }

        @SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
        private Cursor createSxmCursor() {
            String[] columnNames = {
                SiriusXm._ID,
                SiriusXm.LIST_INDEX,
                SiriusXm.TEXT,
                SiriusXm.PCH_NUMBER,
                SiriusXm.CH_NUMBER,
                SiriusXm.BAND_TYPE
            };

            SparseArrayCompat<ListInfo.ListItem> listItems = mRepository.mSxmListItems;
            CarDeviceMediaCursor cursor = new CarDeviceMediaCursor(columnNames, mRepository.mEventBus, listItems.size());
            cursor.registerContentObserver(mObserver);
            SxmBandType filter = (mBandType == null) ? null : (SxmBandType) mBandType;
            // 歯抜けはありえないはずなのでvalueAtで取得する
            for (int i = 0; i < listItems.size(); i++) {
                ListInfo.SxmListItem item = (ListInfo.SxmListItem) listItems.valueAt(i);
                SxmBandType bandType = ((SxmBandType) item.getBand());
                if (filter == null || filter == bandType) {
                    cursor.addRow(new Object[]{
                        (long) (i + 1),
                        item.listIndex,
                        item.text,
                        item.getPchNumber(),
                        item.getChannelNumber(),
                        bandType.name()
                    });
                }
            }

            return cursor;
        }

        @SuppressFBWarnings("IS2_INCONSISTENT_SYNC")
        private Cursor createUsbCursor() {
            String[] columnNames = {
                UsbListContract._ID,
                UsbListContract.LIST_INDEX,
                UsbListContract.TEXT,
                UsbListContract.TYPE,
                UsbListContract.DATA_ENABLED
            };


            int total = mRepository.mStatusHolder.getListInfo().transactionInfo.total;
            SparseArrayCompat<ListInfo.ListItem> listItems = mRepository.mUsbListItems;
            CarDeviceMediaCursor cursor = new CarDeviceMediaCursor(columnNames, mRepository.mEventBus, total);
            cursor.registerContentObserver(mObserver);
            for (int i = 0; i < total; i++) {
                if (listItems.get(i + 1) != null) {
                    ListInfo.UsbListItem item = (ListInfo.UsbListItem) listItems.get(i + 1);
                    cursor.addRow(new Object[]{
                        (long) (i + 1),
                        item.listIndex,
                        item.text,
                        item.getUsbInfoType().code,
                        1 // true
                    });
                } else {
                    cursor.addRow(new Object[]{
                        (long) (i + 1),
                        i + 1,
                        "",
                        null,
                        0 // false
                    });
                }
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
     * 車載機メディアCursor.
     */
    static class CarDeviceMediaCursor extends MatrixCursor {
        private EventBus mEventBus;

        /**
         * コンストラクタ.
         *
         * @param columnNames     列名群
         * @param eventBus        イベントバス
         * @param initialCapacity 初期キャパシティ
         */
        public CarDeviceMediaCursor(@NonNull String[] columnNames, @NonNull EventBus eventBus, int initialCapacity) {
            super(columnNames, initialCapacity);
            mEventBus = eventBus;
            mEventBus.register(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() {
            super.close();
            mEventBus.unregister(this);
        }

        /**
         * 車載機メディアリスト更新イベントハンドラ.
         *
         * @param ev 車載機メディアリスト更新イベント
         */
        @Subscribe
        public void onCarDeviceMediaListChangedEvent(CarDeviceMediaListChangedEvent ev) {
            Timber.i("onCarDeviceMediaListChangedEvent()");

            onChange(false);
        }
    }

    /**
     * 車載機メディアリスト更新イベント.
     */
    public static class CarDeviceMediaListChangedEvent {
    }
}
