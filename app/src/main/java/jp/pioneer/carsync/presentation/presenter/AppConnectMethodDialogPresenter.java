package jp.pioneer.carsync.presentation.presenter;

import com.annimon.stream.Optional;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.presentation.view.AppConnectMethodDialogView;

@PresenterLifeCycle
public class AppConnectMethodDialogPresenter extends Presenter<AppConnectMethodDialogView> {
    @Inject
    AppSharedPreference mPreference;
    @Inject
    public AppConnectMethodDialogPresenter() {
    }

    @Override
    void onResume() {
        // NoDisplayAgainのチェック状態をセット(画面回転対策)
        Optional.ofNullable(getView()).ifPresent(view -> {
            boolean isChecked = mPreference.isAppConnectMethodNoDisplayAgain();
            view.setCheckBox(isChecked);
        });
    }

    /**
     * NoDisplayAgainのチェック状態をPreferenceに保存
     */
    public void saveNoDisplayAgainStatus(boolean isNoDisplayAgain){
        mPreference.setAppConnectMethodNoDisplayAgain(isNoDisplayAgain);
    }
}
