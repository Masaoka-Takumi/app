package jp.pioneer.carsync.presentation.presenter;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.presentation.view.PrivacyPolicyView;

/**
 * PrivacyPolicy画面のPresenter
 */
@PresenterLifeCycle
public class PrivacyPolicyPresenter extends Presenter<PrivacyPolicyView> {
    @Inject
    public PrivacyPolicyPresenter() {
    }
}
