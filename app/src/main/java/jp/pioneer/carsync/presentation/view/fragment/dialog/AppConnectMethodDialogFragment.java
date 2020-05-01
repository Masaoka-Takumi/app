package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

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
    @BindView(R.id.checkbox)
    ToggleButton mCheckBox;
    @BindView(R.id.checkbox_text)
    AutofitTextView mCheckBoxText;
    @BindView(R.id.reshow_check)
    RelativeLayout mCheckBoxArea;
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
        Dialog dialog = new Dialog(getActivity(), R.style.CustomKeySettingBehindScreenStyle);
        setCancelable(false);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_app_connect_method, container, false);
        mUnbinder = ButterKnife.bind(this, view);
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
        mCheckBox.setChecked(isChecked);
    }

    /**
     * NoDisplayAgainのチェック状態切り替え動作とPreference保存
     */
    @OnClick(R.id.reshow_check)
    public void onClickNoDisplayAgain() {
        mCheckBox.setChecked(!mCheckBox.isChecked());
        // 表示したいチェック状態
        mPresenter.saveNoDisplayAgainStatus(mCheckBox.isChecked());
    }

    /**
     * Closeボタンタップ時の動作セット
     */
    @OnClick(R.id.close_button)
    public void onClickCloseButton() {
        callbackClose();
    }
}
