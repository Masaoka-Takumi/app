package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.SpeechRecognizerDialogPresenter;
import jp.pioneer.carsync.presentation.view.SpeechRecognizerDialogView;

/**
 * Created by NSW00_007906 on 2017/11/27.
 */

public class SpeechRecognizerDialogFragment extends AbstractDialogFragment<SpeechRecognizerDialogPresenter,
        SpeechRecognizerDialogView, SpeechRecognizerDialogFragment.Callback>implements
        SpeechRecognizerDialogView {
    private static final float SOUND_LEVEL_MIN = 1.0f;//最小音量レベル=1dB
    private static final float SOUND_LEVEL_MAX = 8.0f;//最大音量レベル=8dB
    @Inject SpeechRecognizerDialogPresenter mPresenter;
    @BindView(R.id.title_text) TextView mTitle;
    @BindView(R.id.mic_icon) ImageView mMicIcon;
    @BindView(R.id.mic_vol1) ImageView mMicVol1;
    @BindView(R.id.mic_vol2) ImageView mMicVol2;
    @BindView(R.id.close_button) ImageView mCloseBtn;
    @BindView(R.id.text_view) TextView mTextView;
    private boolean  mIsFirst = true;
    private float mRmsDbFilter = 0;
    private Unbinder mUnbinder;
    public enum StateType {
        WAITING,
        LISTENING,
        SPEAKING,
    }
    /**
     * コンストラクタ
     */
    public SpeechRecognizerDialogFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return AccidentDetectDialogFragment
     */
    public static SpeechRecognizerDialogFragment newInstance(Fragment target, Bundle args) {
        SpeechRecognizerDialogFragment fragment = new SpeechRecognizerDialogFragment();
        fragment.setTargetFragment(target, 0);
        fragment.setArguments(args);
        fragment.setCancelable(false);
        return fragment;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.VRDialogStyle);
        View view = inflater.inflate(R.layout.fragment_dialog_voice_recognizier, null, false);
        mUnbinder = ButterKnife.bind(this, view);
        getPresenter().setArgument(getArguments());
        builder.setView(view);
        mMicVol1.setAlpha(0.0f);
        mMicVol2.setAlpha(0.0f);
        mIsFirst = true;
        Dialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                if (getCallback() != null) {
                    getCallback().onClose(this);
                }else {
                    this.dismiss();
                }
                return true;
            }
            return false;
        });
        return dialog;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected SpeechRecognizerDialogPresenter getPresenter() {
        return mPresenter;
    }


    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof Callback;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @OnClick(R.id.close_button)
    public void onClickCloseButton(View view) {
        if (getCallback() != null) {
            getCallback().onClose(this);
        } else {
            this.dismiss();
        }
    }

    public void changeSpeechVolume(float rmsdB) {
        //Timber.d("rmsdB = " + rmsdB);
        //ローパスフィルター
        final float alpha = 0.8f;
        if(rmsdB<0){
            rmsdB = 0;
        }
        if(mIsFirst){
            mRmsDbFilter = rmsdB;
            mIsFirst = false;
        }else{
            mRmsDbFilter = alpha * mRmsDbFilter + (1 - alpha) * rmsdB;
        }
        //Timber.d("mRmsDbFilter = " + mRmsDbFilter);
        float level = mRmsDbFilter > SOUND_LEVEL_MIN ? (mRmsDbFilter-SOUND_LEVEL_MIN)/(SOUND_LEVEL_MAX-SOUND_LEVEL_MIN) : 0;
        if(level>1.0f)level=1.0f;
        mMicVol2.setAlpha(level);
        mMicVol1.setAlpha(level>0.5f?(level-0.5f)*2:0);
        //Timber.d("mic Volume level : " + level);
    }
    @Override
    public void setState(StateType state){
        //Timber.d("setState:state="+state.name());
        switch (state){
            case WAITING:
                mTitle.setText(R.string.rec_029);
                mMicVol1.setVisibility(View.INVISIBLE);
                mMicVol2.setVisibility(View.INVISIBLE);
                mMicIcon.setImageResource(0);
                break;
            case LISTENING:
                mTitle.setText(R.string.rec_030);
                mMicVol1.setVisibility(View.VISIBLE);
                mMicVol2.setVisibility(View.VISIBLE);
                mMicIcon.setImageResource(R.drawable.p0821_mic);
                break;
            case SPEAKING:
                mTitle.setText(null);
                mMicVol1.setVisibility(View.INVISIBLE);
                mMicVol2.setVisibility(View.INVISIBLE);
                mMicIcon.setImageResource(R.drawable.p0822_feedback);
                break;
        }
        getPresenter().setStateType(state);
    }
    @Override
    public void setText(String text){
        mTextView.setText(text);
    }

    @Override
    public String getText() {
        return mTextView.getText().toString();
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
        void onClose(SpeechRecognizerDialogFragment fragment);
    }
}
