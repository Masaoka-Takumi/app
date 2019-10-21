package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import java.util.Random;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.view.SpeechRecognizerDialogView;
import jp.pioneer.carsync.presentation.view.fragment.dialog.SpeechRecognizerDialogFragment;

/**
 * Created by NSW00_007906 on 2017/11/27.
 */

public class SpeechRecognizerDialogPresenter extends Presenter<SpeechRecognizerDialogView> {
    public static final String SEARCH_TYPE = "search_type";
    @Inject Context mContext;
    private boolean mIsLocal = false;
    private String mExText = "";
    private SpeechRecognizerDialogFragment.StateType mStateType = SpeechRecognizerDialogFragment.StateType.WAITING;
    /**
     * コンストラクタ
     */
    @Inject
    public SpeechRecognizerDialogPresenter() {
    }

    public void setStateType(SpeechRecognizerDialogFragment.StateType stateType) {
        mStateType = stateType;
    }

    @Override
    public void onInitialize() {
        Random rnd = new Random();
        int ran;
        if (mIsLocal) {
            ran = rnd.nextInt(7);
            String[] textArray = mContext.getResources().getStringArray(R.array.speech_recognizer_local_random);
            mExText = textArray[ran];
        } else {
            ran = rnd.nextInt(4);
            String[] textArray = mContext.getResources().getStringArray(R.array.speech_recognizer_global_random);
            mExText = textArray[ran];
        }
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setText(mExText);
            view.setState(mStateType);
        });
    }

    @Override
    void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        mExText = savedInstanceState.getString("text");
        mStateType = SpeechRecognizerDialogFragment.StateType.valueOf(savedInstanceState.getString("state"));
        //Timber.d("onRestoreInstanceState:mStateType="+mStateType.name());
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setText(mExText);
            view.setState(mStateType);
        });
    }

    @Override
    void onSaveInstanceState(@NonNull Bundle outState) {
        //Timber.d("onSaveInstanceState:mStateType="+mStateType.name());
        Optional.ofNullable(getView()).ifPresent(view -> {
            outState.putString("text",view.getText());
            outState.putString("state",mStateType.name());
        });
    }

    /**
     * 選択中Customタイプの設定
     *
     * @param args Bundle
     */
    public void setArgument(Bundle args) {
        mIsLocal = args.getBoolean(SEARCH_TYPE);
    }
}
