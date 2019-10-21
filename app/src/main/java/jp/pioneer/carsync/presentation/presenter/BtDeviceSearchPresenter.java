package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.EnumSet;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.content.SettingListContract;
import jp.pioneer.carsync.domain.event.PhoneSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.SettingListCommandStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlBtSetting;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.QuerySettingList;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.ConnectServiceType;
import jp.pioneer.carsync.domain.model.DeviceSearchStatus;
import jp.pioneer.carsync.domain.model.PhoneSettingStatus;
import jp.pioneer.carsync.domain.model.SettingListInfoMap;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.BtDeviceSearchView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;

/**
 * BTデバイス検索のPresenter
 */
@PresenterLifeCycle
public class BtDeviceSearchPresenter extends Presenter<BtDeviceSearchView> implements LoaderManager.LoaderCallbacks<Cursor> {
    @Inject
    QuerySettingList mGetSettingList;
    @Inject ControlBtSetting mControlBtSetting;
    @Inject GetStatusHolder mGetCase;
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject
    public BtDeviceSearchPresenter() {
    }

    @Override
    void onInitialize() {
        mControlBtSetting.startPhoneSearch();
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

    @Override
    void onDestroy() {
        mControlBtSetting.stopPhoneSearchAndReconnectA2dp();
    }

    public void setLoaderManager(LoaderManager loaderManager) {
        loaderManager.initLoader(0, Bundle.EMPTY, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mGetSettingList.execute(SettingListContract.QuerySettingListParamsBuilder.createSearchList());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setDeviceCursor(data, Bundle.EMPTY));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // no action
    }

    public void onSelectDevice(Cursor cursor) {
        mControlBtSetting.pairingDevice(SettingListContract.SearchList.getBdAddress(cursor), EnumSet.of(ConnectServiceType.PHONE, ConnectServiceType.AUDIO));
    }

    public void onStartSearch() {
        mControlBtSetting.startPhoneSearch();
    }

    public void onStopSearch() {
        mControlBtSetting.stopPhoneSearch();
    }

    /**
     * 設定リストコマンド実行状態変更イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSettingListCommandStatusChangeEvent(SettingListCommandStatusChangeEvent event) {
        if (event.statusType == SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE) {
            updateView();
        }else if(event.statusType == SettingListCommandStatusChangeEvent.CommandStatusType.PAIRING_DEVICE){
            updatePairingView();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPhoneSettingStatusChangeEvent(PhoneSettingStatusChangeEvent event) {
        updateView();
    }

    private void updateView() {
        StatusHolder holder = mGetCase.execute();
        SettingListInfoMap infoMap = holder.getSettingListInfoMap();
        DeviceSearchStatus status = infoMap.deviceSearchStatus;
        CarDeviceSpec spec = holder.getCarDeviceSpec();
        PhoneSettingStatus phoneStatus = holder.getPhoneSettingStatus();

        Optional.ofNullable(getView()).ifPresent(view -> {
            if (status != null) {
                view.setEnable(
                        spec.phoneSettingSupported && phoneStatus.deviceListEnabled && phoneStatus.pairingAddEnabled,
                        status.searching
                );
            }
        });
    }
    private void updatePairingView(){
        StatusHolder holder = mGetCase.execute();
        SettingListInfoMap infoMap = holder.getSettingListInfoMap();
        SettingListInfoMap.CommandStatus pairingStatus = infoMap.devicePairingStatus;
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (pairingStatus != null) {
                switch (pairingStatus) {
                    case PROCESSING:
                        //ペアリング中ダイアログの表示
                        Bundle bundle = new Bundle();
                        bundle.putString(StatusPopupDialogFragment.MESSAGE, mContext.getResources().getString(R.string.set_262));
                        mEventBus.post(new NavigateEvent(ScreenId.STATUS_DIALOG, bundle));
                        break;
                    case SUCCESS:
                        view.dismissPairingDialog();
                        view.showToast(mContext.getString(R.string.set_265));
                        mEventBus.post(new GoBackEvent());
                        break;
                    case FAILED:
                        view.dismissPairingDialog();
                        view.showToast(mContext.getString(R.string.set_266));
                        break;
                    default:
                        view.dismissPairingDialog();
                        break;
                }
            }
        });
    }
}