package jp.pioneer.carsync.presentation.presenter;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.presentation.view.PromptAuthorityPermissionDialogView;
@PresenterLifeCycle
public class PromptAuthorityPermissionDialogPresenter extends Presenter<PromptAuthorityPermissionDialogView> {
    /**
     * コンストラクタ
     */
    @Inject
    public PromptAuthorityPermissionDialogPresenter() {
    }
    
}
