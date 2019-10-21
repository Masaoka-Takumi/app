package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.interactor.ExitMenu;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferAdas;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.AdasTrialState;
import jp.pioneer.carsync.presentation.view.SettingsContainerView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * Created by NSW00_008316 on 2017/03/23.
 */
@PresenterLifeCycle
public class SettingsContainerPresenter extends Presenter<SettingsContainerView> {
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject ExitMenu mExitMenu;
    @Inject GetStatusHolder mStatusHolder;
    @Inject AppSharedPreference mPreference;
    @Inject PreferAdas mPreferAdas;
    private ArrayList<String> mPass = new ArrayList<>();
    private Bundle mArguments;
    private Bundle mContainerArguments;
    private boolean mIsShowCaution;
    private ScreenId mPrevScreen;
    // 閉じるボタンで戻る画面
    private ScreenId mReturnScreenWhenClose = ScreenId.HOME;

    @Inject
    public SettingsContainerPresenter() {
    }

    @Override
    void onTakeView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.updateCloseButton();
            view.updateBackButton();
            view.updateNextButton();
            view.updateNavigateBar();
            view.updateOtherButton();
            if (mIsShowCaution) {
                view.updateCaution();
            }
        });
    }

    @Override
    void onInitialize() {
        SettingsParams params = SettingsParams.from(mArguments);
        Optional.ofNullable(getView()).ifPresent(view -> {
            params.pass = mContext.getResources().getString(R.string.hom_015);
            mPass.clear();
            view.onNavigate(ScreenId.SETTINGS_ENTRANCE, params.toBundle());
            if (params.mScreenId != null) {
                view.onNavigate(params.mScreenId, mArguments);
            }
        });
    }

    @Override
    void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setPass(getPass()));
    }

    public boolean isAdasSettingConfigured(){
        return mPreferAdas.isAdasSettingConfigured();
    }

    public ScreenId getPrevScreen() {
        return mPrevScreen;
    }

    public void setPrevScreen(ScreenId prevScreen) {
        mPrevScreen = prevScreen;
    }

    public void setArgument(Bundle args) {
        mArguments = args;
    }

    public Bundle getContainerArguments() {
        return mContainerArguments;
    }

    public void setContainerArguments(Bundle args) {
        mContainerArguments = args;
    }

    public void onBackAction() {
        mEventBus.post(new GoBackEvent());
    }

    public SessionStatus getSessionStatus() {
        return mStatusHolder.execute().getSessionStatus();
    }

    public void onGoCarSafetyAction(){
        mEventBus.post(new NavigateEvent(ScreenId.CAR_SAFETY_SETTINGS, createSettingsParams(ScreenId.CALIBRATION_SETTING, mContext.getString(R.string.set_038))));
    }
    public void onGoAdasSettingAction(){
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_ADAS, createSettingsParams(ScreenId.ADAS_MANUAL,mContext.getString(R.string.set_003))));
    }
    public void onGoSettingTop(){
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_ENTRANCE, createSettingsParams(null,mContext.getString(R.string.hom_015))));
    }

    public void onCloseAction() {
        if (mStatusHolder.execute().getSessionStatus() == SessionStatus.STARTED) {
            if (mReturnScreenWhenClose == ScreenId.HOME||mReturnScreenWhenClose==null) {
                mEventBus.post(new NavigateEvent(ScreenId.HOME_CONTAINER));
            } else {
                mEventBus.post(new NavigateEvent(ScreenId.PLAYER_CONTAINER));
            }
        } else {
            mEventBus.post(new NavigateEvent(ScreenId.UNCONNECTED_CONTAINER));
        }
    }

    /**
     * Next押下アクション
     */
    public void onNextAction() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            ScreenId screenId = view.getCurrentScreenId();
            SettingsParams params = SettingsParams.from(mContainerArguments);
            if (screenId == ScreenId.ADAS_CAMERA_SETTING) {
                mEventBus.post(new NavigateEvent(ScreenId.ADAS_WARNING_SETTING, createSettingsParams(ScreenId.ADAS_CAMERA_SETTING, mContext.getString(R.string.set_298))));
            } else if (screenId == ScreenId.ADAS_WARNING_SETTING) {
                mPreferAdas.setAdasSettingConfigured(true);
                mPreferAdas.setAdasEnabled(true);
                if (mStatusHolder.execute().getSessionStatus() == SessionStatus.STARTED) {
                    mEventBus.post(new NavigateEvent(ScreenId.HOME_CONTAINER, Bundle.EMPTY));
                } else {
                    mEventBus.post(new NavigateEvent(ScreenId.CAR_SAFETY_SETTINGS, createSettingsParams(ScreenId.ADAS_WARNING_SETTING, mContext.getString(R.string.set_038))));
                }
           /* } else if (screenId == ScreenId.ADAS_USAGE_CAUTION) {
                mEventBus.post(new NavigateEvent(ScreenId.ADAS_MANUAL, createSettingsParams(ScreenId.ADAS_USAGE_CAUTION, mContext.getString(R.string.set_341))));
*/            }else if (screenId == ScreenId.ADAS_MANUAL) {
                mEventBus.post(new NavigateEvent(ScreenId.CALIBRATION_SETTING, createSettingsParams(params.mScreenId, mContext.getString(R.string.set_036))));
            }
        });
    }
    public void onMyPhotoAction(){

    }

    public boolean isFirstInitialSetting() {
        return mPreference.isFirstInitialSettingCompletion();
    }

    public void setFirstInitialSetting(boolean completion) {
        mPreference.setFirstInitialSettingCompletion(completion);
    }

    /**
     * 表示中パスの設定
     *
     * @param args Bundle
     */
    public void setPass(Bundle args) {
        SettingsParams params = SettingsParams.from(args);
        if (params.pass != null) {
            mPass.add(params.pass);
        }
        Optional.ofNullable(getView()).ifPresent(view -> view.setPass(getPass()));
    }

    /**
     * 表示中パスの差し替え
     *
     * @param pass String
     */
    public void setPassCurrent(String pass) {
        int index = mPass.size() - 1;
        mPass.set(index,pass);
    }

    /**
     * 表示中パスの削除
     */
    public void removePass() {
        int index = mPass.size() - 1;
        mPass.remove(index);
        Optional.ofNullable(getView()).ifPresent(view -> view.setPass(getPass()));
    }

    /**
     * 表示中パスの削除(ページ数)
     */
    public void removePass(int page) {
        for(int i = 1;i <= page;i++){
            int index = mPass.size() - 1;
            mPass.remove(index);
        }
        Optional.ofNullable(getView()).ifPresent(view -> view.setPass(getPass()));
    }
    /**
     * 表示中パスの削除（指定のページ名まで）
     */
    public void removePass(String page) {
        int allPass = mPass.size();
        for(int i = 1;i <= allPass;i++){
            int index = mPass.size() - 1;
            if(mPass.get(index).equals(page))break;
            mPass.remove(index);
        }
        Optional.ofNullable(getView()).ifPresent(view -> view.setPass(getPass()));
    }

    /**
     * メニュー解除通知.
     * <p>
     * 車載機へメニュー解除通知を送る。
     */
    public void requestExitMenu() {
        mExitMenu.execute();
    }

    private String getPass() {
        if (mPass.size() > 0) {
            int index = mPass.size() - 1;
            return mPass.get(index);
        }
        return "";
    }

    /**
     * Cautionの表示が必要か否か設定.
     *
     * @param isNeed 必要か否か
     */
    public void setNeedDisplayCaution(boolean isNeed) {
        mIsShowCaution = isNeed;
    }

    /**
     * Caution確認処理.
     */
    public void onAgreedCaution() {
        mIsShowCaution = false;
    }

    private Bundle createSettingsParams(ScreenId screenId, String pass) {
        SettingsParams params = new SettingsParams();
        params.mScreenId = screenId;
        params.pass = pass;
        return params.toBundle();
    }

    /**
     * 連携済みか否か
     */
    public boolean isSessionConnected(){
        return mStatusHolder.execute().getSessionStatus() == SessionStatus.STARTED;
    }

    /**
     * 閉じるボタンで戻る画面を設定
     * @param id
     */
    public void setReturnScreenWhenClose(ScreenId id) {
        mReturnScreenWhenClose = id;
    }

    public void suppressDeviceConnection(ScreenId screenId){
        AppStatus status = mStatusHolder.execute().getAppStatus();

        if((MainPresenter.sIsVersionQ&&!Settings.canDrawOverlays(mContext))||!mPreference.isAgreedEulaPrivacyPolicy()) {
            return;
        }
        if(mPreference.isAdasBillingRecord()){
            if(!status.adasBillingCheck||screenId==ScreenId.TIPS) {
                return;
            }
        }
        if(!screenId.isDialog()) {
            if (screenId == ScreenId.ADAS_TUTORIAL || screenId == ScreenId.ADAS_USAGE_CAUTION
                    || screenId == ScreenId.ADAS_BILLING ) {
                if(!mStatusHolder.execute().getAppStatus().adasPurchased&&mPreference.getAdasTrialState()!= AdasTrialState.TRIAL_DURING) {
                    status.deviceConnectionSuppress = true;
                }
            } else {
                if(status.deviceConnectionSuppress) {
                    status.deviceConnectionSuppress = false;
                    //mEventBus.post(new DeviceConnectionSuppressEvent());
                }
            }
        }
    }
}
