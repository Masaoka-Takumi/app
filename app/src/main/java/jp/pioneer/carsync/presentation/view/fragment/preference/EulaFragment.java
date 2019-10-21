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
import jp.pioneer.carsync.presentation.presenter.EulaPresenter;
import jp.pioneer.carsync.presentation.view.EulaView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * EULAの画面
 */

public class EulaFragment extends AbstractScreenFragment<EulaPresenter, EulaView> implements EulaView {
    private static final String EULA_FILE_PATH_FORMAT = "file:///android_asset/eula_privacy_policy/eula_%s.html";

    @Inject EulaPresenter mPresenter;
    private Unbinder mUnbinder;
    @BindView(R.id.web_view) WebView mWebView;

    /**
     * コンストラクタ
     */
    public EulaFragment() {
    }

    /**
     * 新規インスタンス取得
     * @param args 引き継ぎ情報
     * @return EulaFragment
     */
    public static EulaFragment newInstance(Bundle args) {
        EulaFragment fragment = new EulaFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_information_eula, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mWebView.loadUrl(String.format(EULA_FILE_PATH_FORMAT, getString(R.string.url_001)));
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
    protected EulaPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.EULA;
    }
}
