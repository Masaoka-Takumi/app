package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.AccidentDetectDialogPresenter;
import jp.pioneer.carsync.presentation.view.AccidentDetectDialogView;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static jp.pioneer.carsync.presentation.presenter.AccidentDetectDialogPresenter.ACTION_SENT;

/**
 * 事故通知画面
 */
@RuntimePermissions
public class AccidentDetectDialogFragment extends AbstractDialogFragment<AccidentDetectDialogPresenter,
        AccidentDetectDialogView, AccidentDetectDialogFragment.Callback> implements AccidentDetectDialogView {

    @Inject AccidentDetectDialogPresenter mPresenter;
    @BindView(R.id.before_layout) RelativeLayout mBeforeLayout;
    @BindView(R.id.after_layout) RelativeLayout mAfterLayout;
    @BindView(R.id.circle_progress) ProgressBar mProgress;
    @BindView(R.id.timer_text) TextView mTimerText;
    @BindView(R.id.result_text) TextView mResultText;
    @BindView(R.id.title) TextView mTitle;
    @BindView(R.id.icon) ImageView mIcon;
    @BindView(R.id.caution_icon) ImageView mCautionIcon;
    private boolean isComplete = false;
    /**
     * コンストラクタ
     */
    public AccidentDetectDialogFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param target コールバック通知先
     * @param args   引き継ぎ情報
     * @return AccidentDetectDialogFragment
     */
    public static AccidentDetectDialogFragment newInstance(Fragment target, Bundle args) {
        AccidentDetectDialogFragment fragment = new AccidentDetectDialogFragment();
        fragment.setTargetFragment(target, 0);
        fragment.setCancelable(false);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CautionStyle);
        View view = inflater.inflate(R.layout.fragment_dialog_accident_detect, null, false);
        ButterKnife.bind(this, view);
        builder.setView(view);
        Dialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                if(isComplete) {
                    callbackClose();
                }
                return true;
            }
            return false;
        });
        return dialog;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        AccidentDetectDialogFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected AccidentDetectDialogPresenter getPresenter() {
        return mPresenter;
    }


    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof Callback;
    }

    /**
     * 発話
     *
     * @param number 宛先番号
     */
    @NeedsPermission(Manifest.permission.CALL_PHONE)
    public void call(String number) {
        Uri uri = Uri.parse("tel:" + number);
        Intent intent = new Intent(Intent.ACTION_CALL, uri);
        if (getCallback() != null) {
            getCallback().onActionCall(this);
        }
        startActivity(intent);
    }

    @Override
    public void callPhone(String number) {
        AccidentDetectDialogFragmentPermissionsDispatcher.callWithCheck(this, number);
    }

    /**
     * SMS送信
     *
     * @param number 宛先番号
     * @param text   本文
     */
    @NeedsPermission(Manifest.permission.SEND_SMS)
    public void send(String number, String text) {
        PendingIntent sentIntent = PendingIntent.getBroadcast(getContext(), 0, new Intent(ACTION_SENT), 0);
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, text, sentIntent, null);
        //全角70文字、半角(ucs-2)160文字以内に収めるため、sendMultipartTextMessageは不使用
/*        //ロシア語などで文字数制限を超えるため、sendMultipartTextMessageを使用する
        ArrayList<String> msgArray = smsManager.divideMessage(text);
        int numParts = msgArray.size();

        ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
        for (int i = 0; i < numParts; i++) {
            sentIntents.add(PendingIntent.getBroadcast(getContext(), 0, new Intent(ACTION_SENT), 0));
        }
        smsManager.sendMultipartTextMessage(number, null, msgArray, sentIntents, null);*/
    }

    @Override
    public void sendSMS(String number, String text) {
        if(number.equals("")){
            Toast.makeText(getActivity(), getString(R.string.err_008), Toast.LENGTH_LONG).show();
            getPresenter().onCancelAction();
        }else {
            AccidentDetectDialogFragmentPermissionsDispatcher.sendWithCheck(this, number, text);
        }
    }

    @Override
    public void initProgress(int maxValue) {
        mProgress.setMax(maxValue);
        mProgress.setSecondaryProgress(maxValue);
    }

    @Override
    public void updateProgress(long millisUntilFinished) {
        long mm = millisUntilFinished / 1000 / 60;
        long ss = millisUntilFinished / 1000 % 60;
        mProgress.setProgress(AccidentDetectDialogPresenter.PROGRESS_MAX - (int) millisUntilFinished);
        mTimerText.setText(String.format(Locale.ENGLISH, "%1$02d:%2$02d", mm, ss));
    }

    @Override
    public void updateText(String state, boolean isRetry) {
        if (isRetry) {
            mTitle.setText(R.string.col_010);
            mIcon.setImageResource(R.drawable.p0060_caution);
            mAfterLayout.setEnabled(false);
        } else {
            isComplete = true;
            mTitle.setText(R.string.col_008);
            mIcon.setImageResource(R.drawable.p0061_sent);
            mAfterLayout.setEnabled(true);
        }
        mCautionIcon.setVisibility(View.GONE);
        mBeforeLayout.setVisibility(View.GONE);
        mAfterLayout.setVisibility(View.VISIBLE);
        mResultText.setText(state);
    }

    @Override
    public void callbackClose() {
        if (getCallback() != null) {
            getCallback().onClose(this);
        }
        this.dismiss();
    }

    @Override
    public void callbackTimerFinished() {
        if (getCallback() != null) {
            getCallback().onTimerFinished(this);
        }
    }
    @Override
    public boolean isShowDialog(){
        return isResumed();
    }

    /**
     * 終了ハンドラ
     */
    @OnClick({R.id.before_layout, R.id.after_layout})
    public void onClickCancel() {
        getPresenter().onCancelAction();
    }

    /**
     * ダイアログ終了通知interface
     */
    public interface Callback {
        /**
         * ダイアログ終了通知
         *
         * @param fragment 終了ダイアログ
         */
        void onClose(AccidentDetectDialogFragment fragment);

        void onTimerFinished(AccidentDetectDialogFragment fragment);

        void onActionCall(AccidentDetectDialogFragment fragment);
    }
}
