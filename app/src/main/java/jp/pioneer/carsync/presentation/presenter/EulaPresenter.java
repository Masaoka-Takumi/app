package jp.pioneer.carsync.presentation.presenter;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.presentation.view.EulaView;

/**
 * EULA画面のPresenter
 */
@PresenterLifeCycle
public class EulaPresenter extends Presenter<EulaView> {
    @Inject
    public EulaPresenter() {
    }
}
