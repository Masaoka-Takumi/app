package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.PromptAuthorityPermissionDialogPresenter;
import jp.pioneer.carsync.presentation.view.PromptAuthorityPermissionDialogView;
import jp.pioneer.carsync.presentation.view.activity.MainActivity;

public class PromptAuthorityPermissionDialogFragment extends AbstractDialogFragment<PromptAuthorityPermissionDialogPresenter,
        PromptAuthorityPermissionDialogView, PromptAuthorityPermissionDialogFragment.Callback> implements PromptAuthorityPermissionDialogView {
    @Inject PromptAuthorityPermissionDialogPresenter mPresenter;
    @BindView(R.id.authority_button) RelativeLayout mAuthorityButton;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ
     */
    public PromptAuthorityPermissionDialogFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param target コールバック通知先
     * @param args   引き継ぎ情報
     * @return PromptAuthorityPermissionDialogFragment
     */
    public static PromptAuthorityPermissionDialogFragment newInstance(Fragment target, Bundle args) {
        PromptAuthorityPermissionDialogFragment fragment = new PromptAuthorityPermissionDialogFragment();
        fragment.setTargetFragment(target, 0);
        fragment.setCancelable(false);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("InflateParams")
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
        View view = inflater.inflate(R.layout.fragment_dialog_prompt_authority, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected PromptAuthorityPermissionDialogPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof PromptAuthorityPermissionDialogFragment.Callback;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getCallback() != null) {
            getCallback().onClose(this);
        }
        mUnbinder.unbind();
    }

    @Override
    public void callbackClose() {
        if (getCallback() != null) {
            getCallback().onClose(this);
        }
        this.dismiss();
    }
    /**
     * 権限許可ボタン
     */
    @OnClick(R.id.authority_button)
    public void onClickAuthorityButton() {
        ((MainActivity)getActivity()).manageDrawOverlayPermission();
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
        void onClose(PromptAuthorityPermissionDialogFragment fragment);
    }
}
