package jp.pioneer.carsync.presentation.presenter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.telephony.SmsManager;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.component.LocationProvider;
import jp.pioneer.carsync.domain.component.Resolver;
import jp.pioneer.carsync.domain.event.SmartPhoneControlCommandEvent;
import jp.pioneer.carsync.domain.interactor.ImpactDetectionCountDown;
import jp.pioneer.carsync.domain.interactor.GetCurrentLocation;
import jp.pioneer.carsync.domain.model.SmartPhoneControlCommand;
import jp.pioneer.carsync.presentation.model.ImpactNotificationMethod;
import jp.pioneer.carsync.presentation.view.AccidentDetectDialogView;
import timber.log.Timber;

/**
 * 事故通知ダイアログのpresenter
 */
@PresenterLifeCycle
public class AccidentDetectDialogPresenter extends Presenter<AccidentDetectDialogView>
        implements LocationProvider.Callback {
    public static final String ACTION_SENT = "jp.pioneer.carsync.presentation.presenter.ACTION_SENT";
    public static final int PROGRESS_MAX = 30 * 1000; // msec
    private static final int RETRY_REMIT = 10;

    /**
     * 送信状態
     */
    enum SendState {
        COUNTING,
        SENDING,
        LOCATION_ERROR,
        SEND_ERROR,
        COMPLETE,
    }

    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;
    @Inject GetCurrentLocation mLocationCase;
    @Inject ImpactDetectionCountDown mCancelCountdownCase;
    private CountDown mCountDownTimer;
    private SendState mState = SendState.COUNTING;
    private SMSSentBroadcastReceiver mSentBroadcastReceiver;
    private int mRetryCount = 0;
    private Handler mHandler = new Handler();
    private boolean isTimerFinished = false;

    /**
     * コンストラクタ
     */
    @Inject
    public AccidentDetectDialogPresenter() {
    }

    @VisibleForTesting
    void setSendState(SendState state) {
        mState = state;
    }

    @Override
    void onInitialize() {
        Timber.d("onInitialize");
        registerReceiver();
        isTimerFinished = false;
        if(mCountDownTimer==null) {
            Timber.d("onInitialize mCountDownTimer.start()");
            mCountDownTimer = new CountDown(PROGRESS_MAX, 100);
            Optional.ofNullable(getView()).ifPresent(view -> {
                view.initProgress(PROGRESS_MAX);
                view.updateProgress(0);
            });
            mCountDownTimer.start();
            mCancelCountdownCase.start();
        }
    }

    @Override
    void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        registerReceiver();
        updateView();
    }

    @Override
    void onResume() {
        if (isTimerFinished) {
            Optional.ofNullable(getView()).ifPresent(view -> {
                if (view.isShowDialog()) {
                    view.callbackClose();
                    isTimerFinished = false;
                }
            });

        }

        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    private void registerReceiver() {
        if (mSentBroadcastReceiver == null) {
            Timber.d("onInitialize/onRestoreInstanceState : register");
            mSentBroadcastReceiver = new SMSSentBroadcastReceiver();
            mContext.registerReceiver(mSentBroadcastReceiver, new IntentFilter(ACTION_SENT));
        }
    }

    @Override
    void onDestroy() {
        if (mSentBroadcastReceiver != null) {
            mContext.unregisterReceiver(mSentBroadcastReceiver);
            mSentBroadcastReceiver = null;
        }
        if(mCountDownTimer!=null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        mCancelCountdownCase.finish();
    }

    @Override
    public void onSuccess(@NonNull Location location) {
        String lat = String.format(Locale.ENGLISH,"%.5f", location.getLatitude());
        String lon = String.format(Locale.ENGLISH,"%.5f", location.getLongitude());
        String text = mContext.getResources().getString(R.string.col_012) + "\n"
                + String.format(mContext.getResources().getString(R.string.format_accident_position), lat, lon);
        Optional.ofNullable(getView()).ifPresent(view -> view.sendSMS(mPreference.getImpactNotificationContactNumber(), text));
    }

    @Override
    public void onError(@NonNull LocationProvider.Error error, @Nullable Resolver resolver) {
        mState = SendState.LOCATION_ERROR;
        //現在位置情報なしで送信
        String text = mContext.getResources().getString(R.string.col_012);
        Optional.ofNullable(getView()).ifPresent(view -> view.sendSMS(mPreference.getImpactNotificationContactNumber(), text));
    }

    /**
     * タイマー終了処理
     * <p>
     * 電話設定：指定連絡先へ発話
     * SMS設定：指定連絡先へ送信
     */
    void execute() {
        mCancelCountdownCase.finish();
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.callbackTimerFinished();
            if (mPreference.getImpactNotificationMethod() == ImpactNotificationMethod.PHONE) {
                Timber.d("ACCIDENT : Call " + mPreference.getImpactNotificationContactNumber());

                view.callPhone(mPreference.getImpactNotificationContactNumber());
                //バックグラウンド中はダイアログを閉じない
                if (view.isShowDialog()) {
                    view.callbackClose();
                } else {
                    isTimerFinished = true;
                }
            } else {
                Timber.d("ACCIDENT : Send SMS " + mPreference.getImpactNotificationContactNumber());
                sendSMS();
            }
        });
    }

    private void sendSMS() {
        mState = SendState.SENDING;
        updateView();
        mLocationCase.execute(this);
    }

    /**
     * SMS送信結果受信後処理
     *
     * @param result 送信結果コード
     */
    public void onReceiveResult(int result) {
        Timber.d("onReceiveResult : result = " + result);
        switch (result) {
            case Activity.RESULT_OK:
                mState = SendState.COMPLETE;
                updateView();
                break;
            case Activity.RESULT_CANCELED:
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
            case SmsManager.RESULT_ERROR_NO_SERVICE:
            case SmsManager.RESULT_ERROR_NULL_PDU:
            case SmsManager.RESULT_ERROR_RADIO_OFF:
            default:
                mState = SendState.SEND_ERROR;
                updateView();
                mRetryCount++;
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mHandler.post(() -> sendSMS());
                    }
                }, 2000);
                break;
        }
    }

    private void updateView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            switch (mState) {
                case COUNTING:
                    view.initProgress(PROGRESS_MAX);
                    break;
                case SENDING:
                    //送信中の表示削除
                    //view.updateText(mContext.getResources().getString(R.string.sms_sending), false);
                    break;
                case LOCATION_ERROR:
                    //view.updateText(mContext.getResources().getString(R.string.sms_failure_non_location), true);
                    break;
                case SEND_ERROR:
                    view.updateText(mContext.getResources().getString(R.string.col_011), true);
                    break;
                case COMPLETE:
                    view.updateText(mContext.getResources().getString(R.string.col_005), false);
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * ダイアログ終了
     */
    public void onCancelAction() {
        if(mCountDownTimer!=null) {
            mCountDownTimer.cancel();
            mCountDownTimer = null;
        }
        mCancelCountdownCase.finish();
        Optional.ofNullable(getView()).ifPresent(AccidentDetectDialogView::callbackClose);
    }

    /**
     * SmartPhone操作コマンドイベントハンドラ.
     *
     * @param event SmartPhone操作コマンドイベント
     */
    @Subscribe
    public void onSmartPhoneControlCommandEvent(SmartPhoneControlCommandEvent event) {
        // 車載機からキャンセル操作があった場合
        if (event.command == SmartPhoneControlCommand.BACK) {
            onCancelAction();
        }
    }

    /**
     * SMS送信結果受領receiver
     */
    class SMSSentBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onReceiveResult(getResultCode());
        }
    }

    /**
     * カウントダウンタイマー
     */
    class CountDown extends CountDownTimer {
        /**
         * コンストラクタ
         *
         * @param millisInFuture    カウント時間(msec)
         * @param countDownInterval カウント間隔(msec)
         */
        public CountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            Optional.ofNullable(getView()).ifPresent(view -> view.updateProgress(millisUntilFinished));
        }

        @Override
        public void onFinish() {
            execute();
        }
    }
}
