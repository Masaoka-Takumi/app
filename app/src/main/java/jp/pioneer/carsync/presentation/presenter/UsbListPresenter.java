package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.content.UsbListContract;
import jp.pioneer.carsync.domain.event.ListInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.CreateUsbList;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.UsbInfoType;
import jp.pioneer.carsync.infrastructure.crp.event.CrpListUpdateEvent;
import jp.pioneer.carsync.presentation.view.UsbListView;
import timber.log.Timber;

/**
 * USBリストのPresenter
 */
@PresenterLifeCycle
public class UsbListPresenter extends ListPresenter<UsbListView> implements LoaderManager.LoaderCallbacks<Cursor> {
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mStatusHolder;
    @Inject CreateUsbList mCreateUsbList;

    private static final int LOADER_ID_USB_LIST = -1;
    private static final String KEY_INDEX = "usb_index";
    private LoaderManager mLoaderManager;

    /**
     * コンストラクタ
     */
    @Inject
    public UsbListPresenter() {
    }

    @Override
    void onTakeView() {
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }

        updateView();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);

    }

    public boolean isSphCarDevice() {
        return mStatusHolder.execute().getProtocolSpec().isSphCarDevice();
    }

    @Override
    public void onClose() {
        Optional.ofNullable(getView()).ifPresent(UsbListView::closeDialog);
    }

    /**
     * LoaderManager登録
     *
     * @param loaderManager LoaderManager
     */
    public void setLoaderManager(LoaderManager loaderManager) {
        mLoaderManager = loaderManager;
        if(mLoaderManager.getLoader(LOADER_ID_USB_LIST) == null) {
            mLoaderManager.initLoader(LOADER_ID_USB_LIST, null, this);
        } else {
            mLoaderManager.restartLoader(LOADER_ID_USB_LIST, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cl = null;
        if (id == LOADER_ID_USB_LIST) {
            cl = mCreateUsbList.execute();
        }

        return cl;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_ID_USB_LIST) {
            Optional.ofNullable(getView()).ifPresent(view -> view.setCursor(data));
            //onLoadFinishedが何度も呼ばれてsetCursorするとFocus表示が消えるため。
            updateFocus();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Optional.ofNullable(getView()).ifPresent(view -> view.setCursor(null));
    }

    public void onSelectList(int listIndex, Cursor cursor) {
        if (UsbListContract.getDataEnabled(cursor)) {
            mMediaCase.selectListItem(listIndex);
            UsbInfoType infoType = UsbListContract.getInfoType(cursor);
            switch (infoType) {
                case FILE:
                    break;
                case FOLDER_MUSIC_EXIST:
                    break;
                case FOLDER_MUSIC_NOT_EXIST:
                    break;
                default:
                    break;
            }
        }
    }

    public void onAddListItem(int index) {
        Timber.d("onAddListItem index = %s", index);
        mCreateUsbList.addWantedListItemIndex(index);
    }

    public void onRemoveListItem(int firstIndex, int lastIndex) {
        Timber.d("scrolling remove list item...%s - %s", firstIndex, lastIndex);
        for (int i = firstIndex; i <= lastIndex; i++) {
            mCreateUsbList.removeWantedListItemIndex(i);
        }
    }

    /**
     * 戻るボタン処理
     */
    public void onBackAction() {
        mMediaCase.goBack();
    }

    private String getTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            return title;
        } else {
            return mContext.getString(R.string.ply_081);
        }
    }

    /**
     * リスト情報更新イベントハンドラ
     *
     * @param ev リスト情報更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onCrpListUpdateEvent(CrpListUpdateEvent ev) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            switch (ev.type) {
                case FORWARD:
                case BACK:
                    view.setFirst(true);
                    //階層を戻る
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * リスト情報更新イベントハンドラ
     *
     * @param ev リスト情報更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void OnListInfoChangeEvent(ListInfoChangeEvent ev) {
        updateView();
    }

    private void updateView() {
        StatusHolder holder = mStatusHolder.execute();
        ListInfo info = holder.getListInfo();
        int position = info.focusListIndex - 1;
        String hierarchyName = info.transactionInfo.hierarchyName;
        //Timber.d("position:%d",position);
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setTitle(getTitle(hierarchyName));
            //Backボタンも常時表示する
            //view.setBackButtonVisible(!TextUtils.isEmpty(hierarchyName));
            if (position >= 0) {
                view.setSelectedPosition(position);
            }
        });
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
}
