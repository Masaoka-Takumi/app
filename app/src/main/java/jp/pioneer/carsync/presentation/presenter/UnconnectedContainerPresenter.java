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
    private boolean mIsAppConnectShow = false;
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
        mIsAppConnectShow = false;
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
                    } else {
                        if(isAlexaAvailableConfirmNeeded() && view.getScreenIdInContainer() == ScreenId.TIPS) {
                            // TODO #5244 非同期だけどいいの？
                            // デバッグ設定でAlexa SIM判定 OFFにできるため、設定画面の画面回転のたびに実行されてしまう
                            view.showAlexaAvailableConfirmDialog();
                        }else if(view.getScreenIdInContainer() == ScreenId.TIPS){
                            //横向きからの画面回転で2回連続で呼ばれるため、1回目のみ実行
                            if(!mIsAppConnectShow) {
                                showAppConnectMethodDialog();
                                mIsAppConnectShow = true;
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * Alexa機能利用可能ダイアログを出すべきかどうかの判定
     * TODO #5244 MainPresenterの同名メソッドと共通化したい
     * @return
     * {@code true}:Alexa機能が利用可能かつAlexa機能利用可能ダイアログを未表示
     * {@code false}:それ以外(Alexa機能が利用不可能またはAlexa機能利用可能ダイアログを表示済み)
     */
    private boolean isAlexaAvailableConfirmNeeded() {
        return mStatusCase.execute().getAppStatus().isAlexaAvailableCountry && !mPreference.isAlexaAvailableConfirmShowed();
    }

    public void showAppConnectMethodDialog(){
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (!mPreference.isAppConnectMethodNoDisplayAgain()) {
                view.showAppConnectMethodDialog();
            }
        });
    }

}
