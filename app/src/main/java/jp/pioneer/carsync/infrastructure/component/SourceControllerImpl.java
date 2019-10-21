package jp.pioneer.carsync.infrastructure.component;

import jp.pioneer.carsync.domain.component.SourceController;

/**
 * SourceControllerの実装.
 */
public class SourceControllerImpl implements SourceController {
    private boolean mIsActive;

    /**
     * アクティブ化.
     */
    public void active() {
        mIsActive = true;
        onActive();
    }

    /**
     * 非アクティブ化.
     */
    public void inactive() {
        mIsActive = false;
        onInactive();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return mIsActive;
    }

    /**
     * アクティブ処理.
     */
    void onActive() {
    }

    /**
     * 非アクティブ処理.
     */
    void onInactive() {
    }
}
