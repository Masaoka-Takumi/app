package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.AppConnectMethodDialogPresenter;
import jp.pioneer.carsync.presentation.view.AppConnectMethodDialogView;
import me.grantland.widget.AutofitTextView;

public class AppConnectMethodDialogFragment
        extends AbstractDialogFragment<AppConnectMethodDialogPresenter, AppConnectMethodDialogView, AbstractDialogFragment.Callback>
        implements AppConnectMethodDialogView {

    @Inject
    AppConnectMethodDialogPresenter mPresenter;
    @BindView(R.id.checkbox_on)
    ImageView mCheckBoxOn;
    @BindView(R.id.checkbox_off)
    ImageView mCheckBoxOff;
    @BindView(R.id.checkbox_text)
    AutofitTextView mCheckBoxText;
    @BindView(R.id.no_display_again_touch_area)
    View mCheckBoxTouchArea;
    private Unbinder mUnbinder;

    public AppConnectMethodDialogFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return AppConnectMethodDialogFragment
     */
    public static AppConnectMethodDialogFragment newInstance(Fragment target, Bundle args) {
        AppConnectMethodDialogFragment fragment = new AppConnectMethodDialogFragment();
        fragment.setTargetFragment(target, 0);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.BehindScreenStyle);
        setCancelable(false);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_app_connect_method, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        // チェックボックスタッチエリアのリスナ設定
        // タッチ中はアルファを0.5に、タッチを解除したら元に戻す(Textはdefaultが0.75)
        mCheckBoxTouchArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
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
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof AbstractDialogFragment.Callback;
    }

    @NonNull
    @Override
    protected AppConnectMethodDialogPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void callbackClose() {
        if (getCallback() != null) {
            getCallback().onClose(this);
        }
        this.dismiss();
    }

    @Override
    public void setCheckBox(boolean isChecked) {
        if (isChecked) {
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
     * NoDisplayAgainのチェック状態切り替え動作とPreference保存
     */
    @OnClick(R.id.no_display_again_touch_area)
    public void onClickNoDisplayAgain() {
        // 表示したいチェック状態
        boolean isChecked = !(mCheckBoxOn.getVisibility() == View.VISIBLE);
        setCheckBox(isChecked);
        mPresenter.saveNoDisplayAgainStatus(isChecked);
    }

    /**
     * OKボタンタップ時の動作セット
     */
    @OnClick(R.id.confirm_button)
    public void onClickConfirmButton() {
        callbackClose();
    }
}
