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
import jp.pioneer.carsync.domain.model.ConnectedDevicesCountStatus;
import jp.pioneer.carsync.domain.model.PhoneSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.BtDeviceListView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;

/**
 * BTデバイスリスト画面のPresenter.
 */
@PresenterLifeCycle
public class BtDeviceListPresenter extends Presenter<BtDeviceListView> implements LoaderManager.LoaderCallbacks<Cursor> {
    @Inject QuerySettingList mGetSettingList;
    @Inject ControlBtSetting mControlBtSetting;
    @Inject GetStatusHolder mGetCase;
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject
    public BtDeviceListPresenter() {
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

    public void setLoaderManager(LoaderManager loaderManager) {
        loaderManager.initLoader(0, Bundle.EMPTY, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mGetSettingList.execute(SettingListContract.QuerySettingListParamsBuilder.createDeviceList());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setDeviceCursor(data, Bundle.EMPTY));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // no action
    }

    public void onClickSearchButton() {
        mEventBus.post(new NavigateEvent(ScreenId.BT_DEVICE_SEARCH, createSettingsParams(mContext.getString(R.string.set_244))));
    }

    public void onSelectDevice() {
        //TODO 選択時処理
    }

    public void onDeleteDevice(Cursor cursor) {
        mControlBtSetting.deleteDevice(SettingListContract.DeviceList.getBdAddress(cursor));
    }

    public void onConnectHfpDevice(Cursor cursor) {
        if(SettingListContract.DeviceList.isPhone1Connected(cursor) || SettingListContract.DeviceList.isPhone2Connected(cursor)){
            mControlBtSetting.disconnectPhoneServiceCommand(SettingListContract.DeviceList.getBdAddress(cursor), EnumSet.of(ConnectServiceType.PHONE));
        } else {
            mControlBtSetting.connectPhoneServiceCommand(SettingListContract.DeviceList.getBdAddress(cursor), EnumSet.of(ConnectServiceType.PHONE));
        }
    }

    public void OnShowDeleteDialog(String tag){
        Bundle bundle = new Bundle();
        bundle.putString(StatusPopupDialogFragment.TAG, tag);
        bundle.putString(StatusPopupDialogFragment.MESSAGE, mContext.getResources().getString(R.string.set_261));
        bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
        bundle.putBoolean(StatusPopupDialogFragment.NEGATIVE, true);
        mEventBus.post(new NavigateEvent(ScreenId.STATUS_DIALOG, bundle));
    }

    private void updateView(){
        StatusHolder holder = mGetCase.execute();
        CarDeviceSpec spec = holder.getCarDeviceSpec();
        PhoneSettingStatus phoneStatus = holder.getPhoneSettingStatus();
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setEnable(spec.phoneSettingSupported && phoneStatus.deviceListEnabled);
            view.setAddButtonEnabled(phoneStatus.pairingAddEnabled&&phoneStatus.pairingDevicesCountStatus != ConnectedDevicesCountStatus.FULL);
            view.setDeleteButtonEnabled(phoneStatus.pairingClearEnabled);
            view.updateListView();
        });
    }

    private Bundle createSettingsParams(String pass) {
        SettingsParams params = new SettingsParams();
        params.pass = pass;
        return params.toBundle();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPhoneSettingStatusChangeEvent(PhoneSettingStatusChangeEvent event) {
        updateView();
    }

    /**
     * 設定リストコマンド実行状態変更イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSettingListCommandStatusChangeEvent(SettingListCommandStatusChangeEvent event) {
        if (event.statusType != SettingListCommandStatusChangeEvent.CommandStatusType.SEARCH_DEVICE && event.statusType != SettingListCommandStatusChangeEvent.CommandStatusType.PAIRING_DEVICE) {
            updateView();
        }
    }

}
