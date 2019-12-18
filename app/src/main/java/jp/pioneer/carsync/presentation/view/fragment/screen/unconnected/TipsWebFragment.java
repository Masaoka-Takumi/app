package jp.pioneer.carsync.presentation.view.fragment.screen.unconnected;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.TipsWebPresenter;
import jp.pioneer.carsync.presentation.view.TipsWebView;
import jp.pioneer.carsync.presentation.view.activity.MainActivity;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.settings.SettingsContainerFragment;
import timber.log.Timber;

/**
 * Tips Web View 画面
 */

public class TipsWebFragment extends AbstractScreenFragment<TipsWebPresenter, TipsWebView> implements TipsWebView{
    @Inject TipsWebPresenter mPresenter;
    private Unbinder mUnbinder;
    @BindView(R.id.web_view) WebView mWebView;
    public TipsWebFragment() {
    }

    public static TipsWebFragment newInstance(Bundle args) {
        TipsWebFragment fragment = new TipsWebFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(getActivity() instanceof MainActivity){
            ((MainActivity)getActivity()).showStatusBar();
        }
        View view = inflater.inflate(R.layout.fragment_tips_web, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        getPresenter().setArgument(getArguments());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri requestURL = request.getUrl();
                Timber.i("TipsWebF shouldOver Uri=%s scheme=%s, host=%s", requestURL, requestURL.getScheme(), requestURL.getHost());
                return getPresenter().navigateScreen(request);
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                int position = url.indexOf("://");
                String scheme = url.substring(0, position);
                String host = url.substring(position + "://".length());
                Timber.i("TipsWebF shouldOverrideUrlLoading deprecated url=%s scheme=%s, host=%s", url, scheme, host);
                return getPresenter().navigateScreen(scheme, host);
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        if(getActivity() instanceof MainActivity){
            ((MainActivity)getActivity()).hideStatusBar();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.i("TipsWebF onDestroy");
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected TipsWebPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.TIPS_WEB;
    }

    @Override
    public void loadUrl(String url){
        mWebView.loadUrl(url);
    }

    @OnClick(R.id.back_btn)
    public void onBackClickButton(){
        if (mWebView.canGoBack()){
            mWebView.goBack();
        }else {
            getPresenter().onBackAction();
        }
    }

}
