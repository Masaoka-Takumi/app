package jp.pioneer.carsync.presentation.view.fragment.screen.home;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.OpeningEulaPresenter;
import jp.pioneer.carsync.presentation.view.OpeningEulaView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.widget.CustomScrollView;

/**
 * 起動時EULAの画面
 */

public class OpeningEulaFragment extends AbstractScreenFragment<OpeningEulaPresenter, OpeningEulaView> implements OpeningEulaView, CustomScrollView.ScrollToBottomListener {
    private static final String EULA_FILE_PATH_FORMAT = "file:///android_asset/eula_privacy_policy/eula_%s.html";

    @Inject OpeningEulaPresenter mPresenter;
    private Unbinder mUnbinder;
    @BindView(R.id.agree) TextView mAgree;
    @BindView(R.id.scroll_view) CustomScrollView mScrollView;
    @BindView(R.id.web_view) WebView mWebView;

    /**
     * コンストラクタ
     */
    public OpeningEulaFragment() {
    }

    /**
     * 新規インスタンス取得
     * @param args 引き継ぎ情報
     * @return OpeningEulaFragment
     */
    public static OpeningEulaFragment newInstance(Bundle args) {
        OpeningEulaFragment fragment = new OpeningEulaFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_opening_eula, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mAgree.setEnabled(false);
        mScrollView.setScrollToBottomListener(this);
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
    protected OpeningEulaPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.OPENING_EULA;
    }

    /**
     * 同意ボタン
     */
    @OnClick(R.id.agree)
    public void onClickAcceptButton() {
        getPresenter().onAcceptAction();
    }

    @Override
    public void onScrollToBottom(CustomScrollView scrollView) {
        getPresenter().onScrollBottomAction();
    }

    @Override
    public void setEnabledAgreeBtn(boolean isEnabled) {
        mAgree.setEnabled(isEnabled);
    }
}
