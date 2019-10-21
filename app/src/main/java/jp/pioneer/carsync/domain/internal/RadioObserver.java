package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.RadioFunctionSettingChangeEvent;
import jp.pioneer.carsync.domain.event.RadioFunctionSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.RadioInfoChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * ラジオソース監視.
 * <p>
 * ラジオソース関連の情報が更新された場合にイベントを発行する。
 *
 * @see RadioInfoChangeEvent
 * @see RadioFunctionSettingStatusChangeEvent
 * @see RadioFunctionSettingChangeEvent
 */
public class RadioObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mInfoVersion;
    private long mFunctionSettingStatusVersion;
    private long mFunctionSettingVersion;

    /**
     * コンストラクタ
     */
    @Inject
    public RadioObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mInfoVersion = statusHolder.getCarDeviceMediaInfoHolder().radioInfo.getSerialVersion();
        mFunctionSettingStatusVersion = statusHolder.getTunerFunctionSettingStatus().getSerialVersion();
        mFunctionSettingVersion = statusHolder.getTunerFunctionSetting().getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long infoVersion = statusHolder.getCarDeviceMediaInfoHolder().radioInfo.getSerialVersion();
        if (infoVersion != mInfoVersion) {
            mInfoVersion = infoVersion;
            mEventBus.post(new RadioInfoChangeEvent());
        }

        long functionSettingStatusVersion = statusHolder.getTunerFunctionSettingStatus().getSerialVersion();
        if (functionSettingStatusVersion != mFunctionSettingStatusVersion) {
            mFunctionSettingStatusVersion = functionSettingStatusVersion;
            mEventBus.post(new RadioFunctionSettingStatusChangeEvent());
        }

        long functionSettingVersion = statusHolder.getTunerFunctionSetting().getSerialVersion();
        if (functionSettingVersion != mFunctionSettingVersion) {
            mFunctionSettingVersion = functionSettingVersion;
            mEventBus.post(new RadioFunctionSettingChangeEvent());
        }
    }
}
