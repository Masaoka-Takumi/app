package jp.pioneer.carsync.infrastructure.component;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * SourceControllerのNullオブジェクト.
 * <p>
 * 車載機のソースではサポートしているがアプリとしてサポートしないソースに使用する。
 */
public class NullSourceController extends SourceControllerImpl {
    /**
     * コンストラクタ.
     */
    @Inject
    public NullSourceController() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onActive() {
        Timber.i("onActive()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onInactive() {
        Timber.i("onInactive()");
    }
}
