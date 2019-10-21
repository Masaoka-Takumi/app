package jp.pioneer.carsync.presentation.presenter;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.HdRadioFunctionSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.HdRadioInfoChangeEvent;
import jp.pioneer.carsync.domain.event.RadioFunctionSettingChangeEvent;
import jp.pioneer.carsync.domain.event.RadioFunctionSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.RadioInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferHdRadioFunction;
import jp.pioneer.carsync.domain.interactor.PreferRadioFunction;
import jp.pioneer.carsync.domain.model.HdRadioBandType;
import jp.pioneer.carsync.domain.model.LocalSetting;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.LocalDialogView;

/**
 * Local設定のPresenter.
 */
public class LocalDialogPresenter extends Presenter<LocalDialogView> {
    @Inject GetStatusHolder mGetCase;
    @Inject EventBus mEventBus;
    @Inject PreferRadioFunction mPreferRadioCase;
    @Inject PreferHdRadioFunction mPreferHdRadioCase;
    private MediaSourceType mSourceType;
    private RadioBandType mRadioBandType;
    private HdRadioBandType mHdRadioBandType;
    @Inject
    public LocalDialogPresenter() {

    }

    @Override
    void onTakeView() {
        mSourceType = mGetCase.execute().getCarDeviceStatus().sourceType;
        if (mSourceType == MediaSourceType.RADIO) {
            mRadioBandType = mGetCase.execute().getCarDeviceMediaInfoHolder().radioInfo.band;
        } else if (mSourceType == MediaSourceType.HD_RADIO) {
            mHdRadioBandType = mGetCase.execute().getCarDeviceMediaInfoHolder().hdRadioInfo.band;
        }
    }

    @Override
    void onResume() {
        mSourceType = mGetCase.execute().getCarDeviceStatus().sourceType;
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        updateView();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    /**
     * ラジオ設定変更イベントハンドラ.
     *
     * @param event オーディオ設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRadioFunctionSettingChangeEvent(RadioFunctionSettingChangeEvent event) {
        updateView();
    }

    /**
     * ラジオ設定ステータス変更イベントハンドラ.
     *
     * @param event オーディオ設定ステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRadioFunctionSettingStatusChangeEvent(RadioFunctionSettingStatusChangeEvent event) {
        updateView();
    }

    /**
     * 車載機ステータス変更イベントハンドラ
     *
     * @param event 車載機ステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarDeviceStatusChangeEvent(CarDeviceStatusChangeEvent event) {
        updateView();
    }

    /**
     * ラジオ情報変更イベントハンドラ
     *
     * @param event ラジオ情報変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRadioInfoChangeEvent(RadioInfoChangeEvent event) {
        updateView();
    }

    /**
     * HDラジオ情報変更通知イベントハンドラ
     *
     * @param event ラジオ情報変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHdRadioInfoChangeEvent(HdRadioInfoChangeEvent event) {
        updateView();
    }

    /**
     * HDラジオ設定ステータス変更通知イベントハンドラ
     *
     * @param event ラジオ設定ステータス変更通知イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHdRadioFunctionSettingStatusChangeEvent(HdRadioFunctionSettingStatusChangeEvent event) {
        updateView();
    }

    private void updateView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            StatusHolder holder = mGetCase.execute();
            if (mSourceType == MediaSourceType.RADIO) {
                if ((holder.getCarDeviceStatus().tunerFunctionSettingEnabled && holder.getTunerFunctionSettingStatus().localSettingEnabled) &&
                        (mRadioBandType == holder.getCarDeviceMediaInfoHolder().radioInfo.band)) {
                    LocalSetting setting = mGetCase.execute().getTunerFunctionSetting().localSetting;
                    view.setLocalSetting(setting);
                } else {
                    view.callbackClose();
                }
            } else if (mSourceType == MediaSourceType.HD_RADIO) {
                if ((holder.getCarDeviceStatus().hdRadioFunctionSettingEnabled && holder.getHdRadioFunctionSettingStatus().localSettingEnabled) &&
                        (mHdRadioBandType == holder.getCarDeviceMediaInfoHolder().hdRadioInfo.band)) {
                    LocalSetting setting = mGetCase.execute().getHdRadioFunctionSetting().localSetting;
                    view.setLocalSetting(setting);
                } else {
                    view.callbackClose();
                }
            }
        });
    }

    public boolean isFm() {
        mSourceType = mGetCase.execute().getCarDeviceStatus().sourceType;
        if (mSourceType == MediaSourceType.RADIO) {
            mRadioBandType = mGetCase.execute().getCarDeviceMediaInfoHolder().radioInfo.band;
            return mRadioBandType.isFMVariant();
        } else if (mSourceType == MediaSourceType.HD_RADIO) {
            mHdRadioBandType = mGetCase.execute().getCarDeviceMediaInfoHolder().hdRadioInfo.band;
            return mHdRadioBandType.isFMVariant();
        }
        return true;
    }

    /**
     * リストアイテム選択
     *
     * @param setting アイテム
     */
    public void onSelectAction(LocalSetting setting) {
        if (mSourceType == MediaSourceType.RADIO) {
            mPreferRadioCase.setLocal(setting);
        } else if (mSourceType == MediaSourceType.HD_RADIO) {
            mPreferHdRadioCase.setLocal(setting);
        }
    }
}
