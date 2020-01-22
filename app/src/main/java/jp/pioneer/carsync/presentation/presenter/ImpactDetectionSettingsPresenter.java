package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.interactor.IsCapableOfImpactDetector;
import jp.pioneer.carsync.domain.interactor.QueryContact;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.ImpactNotificationMethod;
import jp.pioneer.carsync.presentation.view.ImpactDetectionSettingsView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;

/**
 * 衝突検知機能設定のpresenter
 */
@PresenterLifeCycle
public class ImpactDetectionSettingsPresenter extends Presenter<ImpactDetectionSettingsView> {
    public static final String DIALOG_TAG_CONTACT_ALERT = "impact_detect_contact_alert";
    public static final String DIALOG_TAG_CONTACT_CONFIRM = "impact_detect_contact_confirm";
    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;
    @Inject EventBus mEventBus;
    @Inject QueryContact mContactCase;
    @Inject IsCapableOfImpactDetector mIsCapableOfImpactDetector;
    private Handler mHandler = new Handler();

    /**
     * コンストラクタ
     */
    @Inject
    public ImpactDetectionSettingsPresenter() {
    }

    @Override
    void onTakeView() {
        updateView();
    }


    private void updateView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (!mIsCapableOfImpactDetector.execute()) {
                view.invalidImpactDetection();
            } else {
                view.setImpactDetectionEnabled(mPreference.isImpactDetectionEnabled());
            }
            view.setImpactNotificationMethod(mPreference.getImpactNotificationMethod());
        });
    }

    /**
     * 衝突検知設定スイッチ押下時の処理
     *
     * @param newValue スイッチの状態
     */
    public void onImpactDetectionChange(boolean newValue) {
        mPreference.setImpactDetectionEnabled(newValue);
        Optional.ofNullable(getView()).ifPresent(view -> view.setImpactDetectionEnabled(newValue));
    }

    /**
     * 連絡先設定済確認
     */
    public void checkContactNumber(){
        if(TextUtils.isEmpty(mPreference.getImpactNotificationContactNumber())) {
            showAlertDialog();
        }else{
            showContactConfirmDialog();
        }
    }

    /**
     * 緊急連絡手段切換処理
     */
    public void onImpactNotificationMethodChange() {
        ImpactNotificationMethod method = mPreference.getImpactNotificationMethod().toggle();
        mPreference.setImpactNotificationMethod(method);
        Optional.ofNullable(getView()).ifPresent(view -> view.setImpactNotificationMethod(method));
    }

    /**
     * 緊急連絡先設定の処理
     */
    public void onImpactNotificationContactAction() {
        SettingsParams params = new SettingsParams();
        params.pass = mContext.getString(R.string.set_252);
        mEventBus.post(new NavigateEvent(ScreenId.IMPACT_DETECTION_CONTACT_REGISTER_SETTING, params.toBundle()));
    }

    /**
     * SET-06-02-D01:連絡先未設定ダイアログ
     */
    private void showAlertDialog(){
        mHandler.post(() -> {
            Bundle bundle = new Bundle();
            bundle.putString(StatusPopupDialogFragment.TAG, DIALOG_TAG_CONTACT_ALERT);
            bundle.putString(StatusPopupDialogFragment.MESSAGE, mContext.getString(R.string.set_280));
            bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
            bundle.putBoolean(StatusPopupDialogFragment.NEGATIVE, false);
            mEventBus.post(new NavigateEvent(ScreenId.STATUS_DIALOG, bundle));
        });
    }

    /**
     * SET-06-02-D02:通知先連絡確認ダイアログ
     */
    public void showContactConfirmDialog(){
        mHandler.post(() -> {
            Bundle bundle = new Bundle();
            bundle.putString(StatusPopupDialogFragment.TAG, DIALOG_TAG_CONTACT_CONFIRM);
            bundle.putString(StatusPopupDialogFragment.MESSAGE, mContext.getString(R.string.set_395));
            bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
            bundle.putBoolean(StatusPopupDialogFragment.NEGATIVE, false);
            mEventBus.post(new NavigateEvent(ScreenId.STATUS_DIALOG, bundle));
        });
    }
}
