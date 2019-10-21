package jp.pioneer.carsync.infrastructure.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import jp.pioneer.carsync.infrastructure.crp.event.CrpStatusUpdateEvent;
import jp.pioneer.carsync.domain.repository.StatusHolderRepository;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * StatusHolderRepositoryの実装.
 */
public class StatusHolderRepositoryImpl implements StatusHolderRepository {
    @Inject StatusHolder mStatusHolder;
    @Inject EventBus mEventBus;
    private WeakReference<OnStatusUpdateListener> mListener;

    /**
     * コンストラクタ.
     */
    @Inject
    public StatusHolderRepositoryImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public StatusHolder get() {
        return mStatusHolder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnStatusUpdateListener(@Nullable OnStatusUpdateListener listener) {
        if (mListener != null) {
            mListener = null;
            mEventBus.unregister(this);
        }

        if (listener != null) {
            mListener = new WeakReference<>(listener);
            mEventBus.register(this);
        }
    }

    /**
     * CrpStatusUpdateEventハンドラ.
     */
    @Subscribe
    public void onStatusUpdateEvent(CrpStatusUpdateEvent ev) {
        OnStatusUpdateListener listener = (mListener != null) ? mListener.get() : null;
        if (listener == null) {
            // mListenerが解除されずにGCされた
            mListener = null;
            mEventBus.unregister(this);
        } else {
            listener.onStatusUpdate();
        }
    }
}
