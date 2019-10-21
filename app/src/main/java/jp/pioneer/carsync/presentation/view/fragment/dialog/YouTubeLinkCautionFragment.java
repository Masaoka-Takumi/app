package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.YouTubeLinkCautionPresenter;
import jp.pioneer.carsync.presentation.view.YouTubeLinkCautionView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import me.grantland.widget.AutofitTextView;
import timber.log.Timber;

public class YouTubeLinkCautionFragment
        extends AbstractScreenFragment<YouTubeLinkCautionPresenter, YouTubeLinkCautionView>
        implements YouTubeLinkCautionView {

    @Inject YouTubeLinkCautionPresenter mPresenter;
    @BindView(R.id.checkbox_on) ImageView mCheckBoxOn;
    @BindView(R.id.checkbox_off) ImageView mCheckBoxOff;
    @BindView(R.id.checkbox_text) AutofitTextView mCheckBoxText;
    @BindView(R.id.no_display_again_touch_area) View mCheckBoxTouchArea;
    private Unbinder mUnbinder;

    public YouTubeLinkCautionFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return YouTubeLinkCautionFragment
     */
    public static YouTubeLinkCautionFragment newInstance(Bundle args){
        YouTubeLinkCautionFragment fragment = new YouTubeLinkCautionFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Timber.i("Fragment onCreateView");
        View view = inflater.inflate(R.layout.fragment_youtube_link_caution, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        // 端末のバックキーの設定
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
                    // BackKey離し時点の動作(なにもしない)
                    Timber.i("YouTubeLinkCaution BackKey");
                    return true;
                }
                return false;
            }
        });

        // チェックボックスタッチエリアのリスナ設定
        // タッチ中はアルファを0.5に、タッチを解除したら元に戻す(Textはdefaultが0.75)
        mCheckBoxTouchArea.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        mCheckBoxOn.setAlpha(0.5f);
                        mCheckBoxOff.setAlpha(0.5f);
                        mCheckBoxText.setAlpha(0.5f);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        mCheckBoxOn.setAlpha(1f);
                        mCheckBoxOff.setAlpha(1f);
                        mCheckBoxText.setAlpha(0.75f);
                        break;
                }
                return false;
            }
        });
        
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Timber.i("Fragment onActivityCreated");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        Timber.i("Fragment onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        Timber.i("Fragment onPause");
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected YouTubeLinkCautionPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.YOUTUBE_LINK_CAUTION;
    }

    /**
     * NoDisplayAgainのチェック状態切り替え動作とPreference保存
     */
    @OnClick(R.id.no_display_again_touch_area)
    public void onClickNoDisplayAgain(){
        // 表示したいチェック状態
        boolean isChecked = !(mCheckBoxOn.getVisibility() == View.VISIBLE);
        updateCheckBox(isChecked);
        mPresenter.saveNoDisplayAgainStatus(isChecked);
    }

    /**
     * OKボタンタップ時の動作セット
     */
    @OnClick(R.id.youtube_link_confirm_button)
    public void onClickConfirmButton(){
        mPresenter.onConfirmAction();
    }

    /**
     * チェックボックス画像のCheck/UnCheck切り替え
     * @param isChecked セットするチェック状態 {@code true}:Check　{@code false}:UnCheck
     */
    public void updateCheckBox(boolean isChecked){
        if(isChecked){
            // チェックONにする
            mCheckBoxOn.setVisibility(View.VISIBLE);
            mCheckBoxOff.setVisibility(View.GONE);
        } else {
            // チェックOFFにする
            mCheckBoxOn.setVisibility(View.GONE);
            mCheckBoxOff.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 画面を閉じる
     */
    @Override
    public void callbackCloseResetLastSource() {
        if(getParentFragment() != null && getParentFragment() instanceof YouTubeLinkContainerFragment){
            ((YouTubeLinkContainerFragment) getParentFragment()).closeContainerDialogResetLastSource();
        }
    }
}
