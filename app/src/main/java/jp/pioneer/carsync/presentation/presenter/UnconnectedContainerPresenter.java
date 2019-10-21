package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.UnconnectedContainerView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import timber.log.Timber;

/**
 * 非接続時画面のContainerPresenter.
 */
@PresenterLifeCycle
public class UnconnectedContainerPresenter extends Presenter<UnconnectedContainerView> {

    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;
    @Inject GetStatusHolder mStatusCase;
    @Inject
    public UnconnectedContainerPresenter() {
    }

    @Override
    void onInitialize() {
        Optional.ofNullable(getView()).ifPresent(view -> view.onNavigate(ScreenId.TIPS, Bundle.EMPTY));
    }

    @Override
    void onTakeView() {
        // java.lang.IllegalStateException: FragmentManager is already executing transactionsが発生するためhandlerに乗せる
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Optional.ofNullable(getView()).ifPresent(view -> {
                    Timber.d("Overlay:adasBillingCheck:" + mStatusCase.execute().getAppStatus().adasBillingCheck);
                    if (MainPresenter.sIsVersionQ) {
                        Timber.d("Overlay:isAgreedEulaPrivacyPolicy():" + mPreference.isAgreedEulaPrivacyPolicy() +",Settings.canDrawOverlays:"+Settings.canDrawOverlays(mContext));
                        if (mPreference.isAgreedEulaPrivacyPolicy() && !Settings.canDrawOverlays(mContext)) {
                            mEventBus.post(new NavigateEvent(ScreenId.PROMPT_AUTHORITY_PERMISSION, Bundle.EMPTY));
                            return;
                        }
                    }
                    if(mPreference.isAdasBillingRecord()&&!mStatusCase.execute().getAppStatus().adasBillingCheck) {
                        Timber.d("Overlay:setBillingHelper");
                        view.setBillingHelper();
                    }
                });
            }
        });
    }
}
