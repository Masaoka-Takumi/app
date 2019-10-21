package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.ListInfoChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * リスト情報監視.
 * <p>
 * リスト情報が変わった場合に{@link ListInfoChangeEvent}を発行する。
 */
public class ListInfoObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mSerialVersion;

    /**
     * コンストラクタ
     */
    @Inject
    public ListInfoObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mSerialVersion = statusHolder.getListInfo().getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long serialVersion = statusHolder.getListInfo().getSerialVersion();
        if (serialVersion != mSerialVersion) {
            mSerialVersion = serialVersion;
            mEventBus.post(new ListInfoChangeEvent());
        }
    }
}
