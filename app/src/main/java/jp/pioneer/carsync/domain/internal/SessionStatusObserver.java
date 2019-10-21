package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.SessionStatusChangeEvent;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * セッション状態監視.
 * <p>
 * セッション状態が変わった場合に{@link SessionStatusChangeEvent}を発行する。
 */
public class SessionStatusObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private SessionStatus mSessionStatus;

    /**
     * コンストラクタ
     */
    @Inject
    public SessionStatusObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mSessionStatus = statusHolder.getSessionStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        SessionStatus sessionStatus = statusHolder.getSessionStatus();
        if (sessionStatus != mSessionStatus) {
            mSessionStatus = sessionStatus;
            mEventBus.post(new SessionStatusChangeEvent());
        }
    }
}
