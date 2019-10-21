package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.HdRadioFunctionSettingChangeEvent;
import jp.pioneer.carsync.domain.event.HdRadioFunctionSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.HdRadioInfoChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * HD Radioソース監視.
 * <p>
 * HD Radioソース関連の情報が更新された場合にイベントを発行する。
 *
 * @see HdRadioInfoChangeEvent
 * @see HdRadioFunctionSettingStatusChangeEvent
 * @see HdRadioFunctionSettingChangeEvent
 */
public class HdRadioObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mInfoVersion;
    private long mFunctionSettingStatusVersion;
    private long mFunctionSettingVersion;

    /**
     * コンストラクタ
     */
    @Inject
    public HdRadioObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mInfoVersion = statusHolder.getCarDeviceMediaInfoHolder().hdRadioInfo.getSerialVersion();
        mFunctionSettingStatusVersion = statusHolder.getHdRadioFunctionSettingStatus().getSerialVersion();
        mFunctionSettingVersion = statusHolder.getHdRadioFunctionSetting().getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long infoVersion = statusHolder.getCarDeviceMediaInfoHolder().hdRadioInfo.getSerialVersion();
        if (infoVersion != mInfoVersion) {
            mInfoVersion = infoVersion;
            mEventBus.post(new HdRadioInfoChangeEvent());
        }

        long functionSettingStatusVersion = statusHolder.getHdRadioFunctionSettingStatus().getSerialVersion();
        if (functionSettingStatusVersion != mFunctionSettingStatusVersion) {
            mFunctionSettingStatusVersion = functionSettingStatusVersion;
            mEventBus.post(new HdRadioFunctionSettingStatusChangeEvent());
        }

        long functionSettingVersion = statusHolder.getHdRadioFunctionSetting().getSerialVersion();
        if (functionSettingVersion != mFunctionSettingVersion) {
            mFunctionSettingVersion = functionSettingVersion;
            mEventBus.post(new HdRadioFunctionSettingChangeEvent());
        }
    }
}
