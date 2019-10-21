package jp.pioneer.carsync.presentation.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.OpeningEulaView;
import jp.pioneer.carsync.presentation.view.argument.PermissionParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * 起動時EULA画面のPresenter
 */
@PresenterLifeCycle
public class OpeningEulaPresenter extends Presenter<OpeningEulaView> {
    @Inject EventBus mEventBus;
    private boolean mIsScrollBottom;

    @Inject
    public OpeningEulaPresenter() {
    }

    @Override
    public void saveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("agree", mIsScrollBottom);
    }

    @Override
    public void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        setEnabledAgreeBtn(savedInstanceState.getBoolean("agree"));
    }

    public void onScrollBottomAction(){
        mIsScrollBottom = true;
        setEnabledAgreeBtn(true);
    }

    /**
     * 同意ボタン押下時の処理
     */
    public void onAcceptAction() {
        PermissionParams params = new PermissionParams();
        params.isExecute = true;
        mEventBus.post(new NavigateEvent(ScreenId.OPENING_PRIVACY_POLICY, params.toBundle()));
    }

    private void setEnabledAgreeBtn(boolean isEnabled){
        Optional.ofNullable(getView()).ifPresent(view -> view.setEnabledAgreeBtn(isEnabled));
    }

}
