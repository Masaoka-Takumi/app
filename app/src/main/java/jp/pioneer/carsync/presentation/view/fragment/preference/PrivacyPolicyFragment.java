package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.PrivacyPolicyPresenter;
import jp.pioneer.carsync.presentation.view.PrivacyPolicyView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * PrivacyPolicyの画面
 */

public class PrivacyPolicyFragment  extends AbstractScreenFragment<PrivacyPolicyPresenter, PrivacyPolicyView> implements PrivacyPolicyView {
    private static final String PRIVACY_POLICY_FILE_PATH_FORMAT = "file:///android_asset/eula_privacy_policy/privacy_policy_%s.html";

    @Inject PrivacyPolicyPresenter mPresenter;
    private Unbinder mUnbinder;
    @BindView(R.id.web_view) WebView mWebView;

    /**
     * コンストラクタ
     */
    public PrivacyPolicyFragment() {
    }

    /**
     * 新規インスタンス取得
     * @param args 引き継ぎ情報
     * @return PrivacyPolicyFragment
     */
    public static PrivacyPolicyFragment newInstance(Bundle args) {
        PrivacyPolicyFragment fragment = new PrivacyPolicyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_information_privacy_policy, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mWebView.loadUrl(String.format(PRIVACY_POLICY_FILE_PATH_FORMAT, getString(R.string.url_001)));
        mWebView.setBackgroundColor(Color.TRANSPARENT);
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

    @NonNull
    @Override
    protected PrivacyPolicyPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.PRIVACY_POLICY;
    }
}
