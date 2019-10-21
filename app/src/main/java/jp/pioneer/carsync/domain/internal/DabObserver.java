package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.DabFunctionSettingChangeEvent;
import jp.pioneer.carsync.domain.event.DabFunctionSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.DabInfoChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * DABソース監視.
 * <p>
 * DABソース関連の情報が更新された場合にイベントを発行する。
 *
 * @see DabInfoChangeEvent
 * @see DabFunctionSettingStatusChangeEvent
 * @see DabFunctionSettingChangeEvent
 */
public class DabObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mInfoVersion;
    private long mFunctionSettingStatusVersion;
    private long mFunctionSettingVersion;

    /**
     * コンストラクタ
     */
    @Inject
    public DabObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mInfoVersion = statusHolder.getCarDeviceMediaInfoHolder().dabInfo.getSerialVersion();
        mFunctionSettingStatusVersion = statusHolder.getDabFunctionSettingStatus().getSerialVersion();
        mFunctionSettingVersion = statusHolder.getDabFunctionSetting().getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long infoVersion = statusHolder.getCarDeviceMediaInfoHolder().dabInfo.getSerialVersion();
        if (infoVersion != mInfoVersion) {
            mInfoVersion = infoVersion;
            mEventBus.post(new DabInfoChangeEvent());
        }

        long functionSettingStatusVersion = statusHolder.getDabFunctionSettingStatus().getSerialVersion();
        if (functionSettingStatusVersion != mFunctionSettingStatusVersion) {
            mFunctionSettingStatusVersion = functionSettingStatusVersion;
            mEventBus.post(new DabFunctionSettingStatusChangeEvent());
        }

        long functionSettingVersion = statusHolder.getDabFunctionSetting().getSerialVersion();
        if (functionSettingVersion != mFunctionSettingVersion) {
            mFunctionSettingVersion = functionSettingVersion;
            mEventBus.post(new DabFunctionSettingChangeEvent());
        }
    }
}
