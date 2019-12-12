package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.ListInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.DabBandType;
import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.repository.CarDeviceMediaRepository;
import jp.pioneer.carsync.infrastructure.crp.event.CrpListUpdateEvent;
import jp.pioneer.carsync.presentation.view.DabServiceListView;

public class DabServiceListPresenter extends Presenter<DabServiceListView> implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String KEY_BAND_TYPE = "band_type";
    private static final int LOADER_ID_SERVICE_LIST = 0;
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mStatusHolder;
    @Inject ControlMediaList mMediaCase;
    @Inject CarDeviceMediaRepository mCarDeviceMediaRepository;
    private MediaSourceType mSourceType;
    private LoaderManager mLoaderManager;
    private DabBandType mDabBand;
    /**
     * コンストラクタ
     */
    @Inject
    public DabServiceListPresenter() {

    }
    @Override
    void onInitialize() {
        mSourceType = mStatusHolder.execute().getCarDeviceStatus().sourceType;
    }

    @Override
    void onTakeView() {

    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        updatePresetList();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    /**
     * LoaderManager登録
     *
     * @param loaderManager LoaderManager
     */
    public void setLoaderManager(LoaderManager loaderManager) {
        mLoaderManager = loaderManager;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle bundle) {
        if(id == LOADER_ID_SERVICE_LIST) {
            if (mSourceType == MediaSourceType.DAB) {
                return mCarDeviceMediaRepository.getDabList();
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        int id = loader.getId();
        if(id == LOADER_ID_SERVICE_LIST) {
            if (mSourceType == MediaSourceType.DAB) {
                Optional.ofNullable(getView()).ifPresent(view -> view.setCursor(cursor));
                //onLoadFinishedが何度も呼ばれてsetCursorするとFocus表示が消えるため。
                updateFocus();
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // no action
    }

    /**
     * リスト情報更新イベントハンドラ
     *
     * @param ev リスト情報更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onCrpListUpdateEvent(CrpListUpdateEvent ev) {
        updatePresetList();
    }

    /**
     * リスト情報更新イベントハンドラ
     *
     * @param ev リスト情報更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void OnListInfoChangeEvent(ListInfoChangeEvent ev) {
        if(mSourceType == MediaSourceType.DAB) {
            updateFocus();
        }
    }

    private void updatePresetList() {
        StatusHolder holder = mStatusHolder.execute();
        mSourceType = mStatusHolder.execute().getCarDeviceStatus().sourceType;
        if (mSourceType == MediaSourceType.DAB) {
            mDabBand = holder.getCarDeviceMediaInfoHolder().dabInfo.band;
        }

        Bundle args = new Bundle();
        if (mSourceType == MediaSourceType.DAB) {
            if (mDabBand != null) {
                args.putByte(KEY_BAND_TYPE, (byte) (mDabBand.getCode() & 0xFF));
            }
        }
        mLoaderManager.restartLoader(LOADER_ID_SERVICE_LIST, args, this);
    }

    private void updateFocus(){
        StatusHolder holder = mStatusHolder.execute();
        ListInfo info = holder.getListInfo();
        int position = info.focusListIndex - 1;
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (position >= 0) {
                view.setSelectedPositionNotScroll(position);
            }
        });
    }

    public void onSelectList(int listIndex, Cursor cursor) {
        mMediaCase.selectListItem(listIndex);
    }
}
