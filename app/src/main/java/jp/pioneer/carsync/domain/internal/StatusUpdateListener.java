package jp.pioneer.carsync.domain.internal;

import com.annimon.stream.Stream;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.repository.StatusHolderRepository;

/**
 * ステータス更新リスナー.
 */
public class StatusUpdateListener implements StatusHolderRepository.OnStatusUpdateListener {
    @Inject StatusHolderRepository mStatusHolderRepository;
    @Inject StatusObserver[] mStatusObservers;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ
     */
    @Inject
    public StatusUpdateListener() {
    }

    /**
     * 初期化
     * <p>
     * {@link StatusObserver}を実装している各クラスのinitializeを実施する.
     */
    public void initialize() {
        mStatusHolder = mStatusHolderRepository.get();
        Stream.of(mStatusObservers)
                .forEach(observer -> observer.initialize(mStatusHolder));

        mStatusHolderRepository.setOnStatusUpdateListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate() {
        Stream.of(mStatusObservers)
                .forEach(observer -> observer.onStatusUpdate(mStatusHolder));
    }
}
