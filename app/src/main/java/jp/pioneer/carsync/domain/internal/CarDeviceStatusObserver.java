package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.ListTypeChangeEvent;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.event.ParkingSensorDisplayStatusChangeEvent;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * 車載機ステータス監視.
 * <p>
 * 車載機ステータスが変わった場合に{@link CarDeviceStatusChangeEvent}を発行する。
 *
 * ソース種別が変わった場合は{@link MediaSourceTypeChangeEvent}も発行する。
 *
 * リバース線が変わった場合は{@link ParkingSensorDisplayStatusChangeEvent}も発行する。
 * また、センサー値が正しいかどうか不明になるためパーキングセンサーのセンサー値をリセットする
 *
 * リスト種別が変わった場合は{@link ListTypeChangeEvent}も発行する。
 */
public class CarDeviceStatusObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mSerialVersion;
    private MediaSourceType mSourceType;
    private MediaSourceStatus mSourceStatus;
    private boolean mIsDisplayParkingSensor;
    private ListType mListType;

    /**
     * コンストラクタ
     */
    @Inject
    public CarDeviceStatusObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mSerialVersion = statusHolder.getCarDeviceStatus().getSerialVersion();
        mSourceType = statusHolder.getCarDeviceStatus().sourceType;
        mIsDisplayParkingSensor = statusHolder.getCarDeviceStatus().isDisplayParkingSensor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long serialVersion = statusHolder.getCarDeviceStatus().getSerialVersion();
        if (serialVersion != mSerialVersion) {
            mSerialVersion = serialVersion;
            mEventBus.post(new CarDeviceStatusChangeEvent());
        }

        MediaSourceType sourceType = statusHolder.getCarDeviceStatus().sourceType;
        MediaSourceStatus sourceStatus = statusHolder.getCarDeviceStatus().sourceStatus;
        if (sourceType != mSourceType ||
                sourceStatus != mSourceStatus) {
            mSourceType = sourceType;
            mSourceStatus = sourceStatus;
            mEventBus.post(new MediaSourceTypeChangeEvent());
        }

        boolean isDisplayParkingSensor = statusHolder.getCarDeviceStatus().isDisplayParkingSensor;
        if (isDisplayParkingSensor != mIsDisplayParkingSensor) {
            mIsDisplayParkingSensor = isDisplayParkingSensor;
            mEventBus.post(new ParkingSensorDisplayStatusChangeEvent());
        }

        ListType listType = statusHolder.getCarDeviceStatus().listType;
        if (listType != mListType) {
            mListType = listType;
            mEventBus.post(new ListTypeChangeEvent());
        }
    }
}
