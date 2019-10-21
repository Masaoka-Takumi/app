package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.TimeZone;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.AdasPriceChangeEvent;
import jp.pioneer.carsync.domain.event.AdasPurchaseStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferAdas;
import jp.pioneer.carsync.domain.model.AdasCameraSetting;
import jp.pioneer.carsync.domain.model.AdasFunctionSensitivity;
import jp.pioneer.carsync.domain.model.AdasFunctionType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.AdasTrialState;
import jp.pioneer.carsync.presentation.view.AdasBillingView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;
import timber.log.Timber;

/**
 * AdasBillingPresenter
 */
@PresenterLifeCycle
public class AdasBillingPresenter extends Presenter<AdasBillingView> {
    public static final String TAG_ADAS_TRIAL_CONFIRM = "tag_adas_trial_confirm";
    public static final String TAG_ADAS_CONFIGURATION_RESET = "tag_adas_configuration_reset";
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject GetStatusHolder mGetCase;
    @Inject PreferAdas mPreferAdas;
    @Inject AppSharedPreference mPreference;
    private Bundle mArguments;
    private boolean mPurchased = false;
    /**
     * コンストラクタ.
     */
    @Inject
    public AdasBillingPresenter() {
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        Timber.d("onResume");
        StatusHolder holder = mGetCase.execute();
        if(mPurchased!=holder.getAppStatus().adasPurchased){
            mPurchased = holder.getAppStatus().adasPurchased;
            if(mPurchased){
                adasPurchased();
            }
        }
        AdasTrialState trialState = mPreference.getAdasTrialState();
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setPurchaseBtn(mPurchased);
            view.setPriceText(holder.getAppStatus().adasPrice);
            view.setTrialButtonText(trialState==AdasTrialState.TRIAL_BEFORE?R.string.set_331:R.string.set_332);
            view.setTrialButtonEnabled(trialState==AdasTrialState.TRIAL_BEFORE);
        });
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    /**
     * VideoLinkアクション
     */
    public void onVideoLinkAction(){
        mEventBus.post(new NavigateEvent(ScreenId.VIDEO_PLAYER, createSettingsParams(ScreenId.VIDEO_PLAYER, mContext.getString(R.string.set_036))));

    }

    /**
     * Restoreアクション
     */
    public void onRestoreAction(){
    }

    /**
     * Buyアクション
     */
    public void onPurchaseAction(){
        //adasPurchased();
        //mPreference.setAdasBillingRecord(true);
    }

    /**
     * Trialアクション
     */
    public void onTrialAction(){
        Bundle bundle = new Bundle();
        bundle.putString(StatusPopupDialogFragment.TAG, TAG_ADAS_TRIAL_CONFIRM);
        bundle.putString(StatusPopupDialogFragment.MESSAGE, mContext.getString(R.string.set_334));
        bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
        bundle.putInt(StatusPopupDialogFragment.POSITIVE_TEXT, R.string.set_335);
        bundle.putBoolean(StatusPopupDialogFragment.NEGATIVE, true);
        mEventBus.post(new NavigateEvent(ScreenId.STATUS_DIALOG, bundle));
    }

    /**
     * Trial開始
     */
    public void onTrialStart(){
        StatusHolder holder = mGetCase.execute();
        holder.getAppStatus().adasTrial = true;
        Calendar cal  = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.add(Calendar.DATE,7);
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(Calendar.HOUR_OF_DAY,0);
        mPreference.setAdasTrialPeriodEndDate(cal.getTimeInMillis());
        mPreference.setAdasTrialState(AdasTrialState.TRIAL_DURING);
        mPreferAdas.setAdasSettingConfigured(false);
        //ADAS設定初期化
        initAdasSetting();
        mEventBus.post(new NavigateEvent(ScreenId.ADAS_MANUAL, createSettingsParams(ScreenId.ADAS_BILLING, mContext.getString(R.string.set_341))));

    }

    /**
     * 初期設定開始
     */
    public void onConfigAction(){
        mPreferAdas.setAdasSettingConfigured(false);
        initAdasSetting();
        mEventBus.post(new NavigateEvent(ScreenId.ADAS_MANUAL, createSettingsParams(ScreenId.ADAS_BILLING, mContext.getString(R.string.set_341))));
    }

    /**
     * CarSafety画面遷移
     */
    public void goCarSafety(){
        Timber.d("goCarSafety");
        SettingsParams params = SettingsParams.from(mArguments);
        if(params.mScreenId==ScreenId.ADAS_BILLING){
            mEventBus.post(new NavigateEvent(ScreenId.CAR_SAFETY_SETTINGS, createSettingsParams(null,mContext.getString(R.string.set_038))));
        }else{
            mEventBus.post(new NavigateEvent(ScreenId.CAR_SAFETY_SETTINGS, createSettingsParams(null,mContext.getString(R.string.set_038))));
        }
    }

    /**
     * ADAS購入状態更新イベント
     *
     * @param event ADAS購入状態更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAdasPurchaseStatusChangeEvent(AdasPurchaseStatusChangeEvent event) {
        Timber.d("onAdasPurchaseStatusChangeEvent");
        StatusHolder holder = mGetCase.execute();
        if(mPurchased!=holder.getAppStatus().adasPurchased){
            mPurchased = holder.getAppStatus().adasPurchased;
            if(mPurchased){
                adasPurchased();
            }
        }
    }

    /**
     * ADAS価格更新イベント
     *
     * @param event ADAS価格更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAdasPriceChangeEvent(AdasPriceChangeEvent event) {
        StatusHolder holder = mGetCase.execute();
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setPriceText(holder.getAppStatus().adasPrice);
        });
    }

    public String getAdasPriceText(){
        StatusHolder holder = mGetCase.execute();
        return holder.getAppStatus().adasPrice;
    }

    private void adasPurchased(){
        //他の画面遷移と同時に動作する場合がある
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(mPreferAdas.isAdasSettingConfigured()){
                    if(mPreference.getAdasTrialState()==AdasTrialState.TRIAL_END) {
                        Bundle bundle = new Bundle();
                        bundle.putString(StatusPopupDialogFragment.TAG, TAG_ADAS_CONFIGURATION_RESET);
                        bundle.putString(StatusPopupDialogFragment.MESSAGE, mContext.getString(R.string.set_336));
                        bundle.putBoolean(StatusPopupDialogFragment.POSITIVE, true);
                        bundle.putInt(StatusPopupDialogFragment.POSITIVE_TEXT, R.string.set_338);
                        bundle.putBoolean(StatusPopupDialogFragment.NEGATIVE, true);
                        bundle.putInt(StatusPopupDialogFragment.NEGATIVE_TEXT, R.string.set_337);
                        mEventBus.post(new NavigateEvent(ScreenId.STATUS_DIALOG, bundle));
                    }else{
                        mEventBus.post(new NavigateEvent(ScreenId.CAR_SAFETY_SETTINGS, createSettingsParams(null,mContext.getString(R.string.set_038))));
                    }
                }else {
                    mEventBus.post(new NavigateEvent(ScreenId.ADAS_MANUAL, createSettingsParams(ScreenId.ADAS_BILLING, mContext.getString(R.string.set_341))));
                }
            }
        });
    }

    private void initAdasSetting(){
        //ADAS設定初期化
        mPreferAdas.setAdasEnabled(false);
        mPreferAdas.setAdasAlarmEnabled(true);
        mPreferAdas.setAdasCameraSetting(new AdasCameraSetting());
        mPreferAdas.setFunctionEnabled(AdasFunctionType.LDW,true);
        mPreferAdas.setFunctionSensitivity(AdasFunctionType.LDW, AdasFunctionSensitivity.MIDDLE);
        //TODO:PCWを塞ぐ
        mPreferAdas.setFunctionEnabled(AdasFunctionType.PCW,false);
        mPreferAdas.setFunctionSensitivity(AdasFunctionType.PCW, AdasFunctionSensitivity.MIDDLE);
        mPreferAdas.setFunctionEnabled(AdasFunctionType.FCW,true);
        mPreferAdas.setFunctionSensitivity(AdasFunctionType.FCW, AdasFunctionSensitivity.MIDDLE);
        mPreferAdas.setFunctionEnabled(AdasFunctionType.LKW,false);
        mPreferAdas.setFunctionSensitivity(AdasFunctionType.LKW, AdasFunctionSensitivity.MIDDLE);
    }

    private Bundle createSettingsParams(ScreenId screenId, String pass) {
        SettingsParams params = new SettingsParams();
        params.mScreenId = screenId;
        params.pass = pass;
        return params.toBundle();
    }

}