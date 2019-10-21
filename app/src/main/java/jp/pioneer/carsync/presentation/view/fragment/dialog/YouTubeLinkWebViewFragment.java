package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.YouTubeLinkWebViewPresenter;
import jp.pioneer.carsync.presentation.view.YouTubeLinkWebViewView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import timber.log.Timber;

public class YouTubeLinkWebViewFragment
        extends AbstractScreenFragment<YouTubeLinkWebViewPresenter, YouTubeLinkWebViewView>
        implements YouTubeLinkWebViewView {

    @Inject YouTubeLinkWebViewPresenter mPresenter;
    @BindView(R.id.web_view) WebView mWebView;
    @BindView(R.id.fullscreen_container) FrameLayout mCustomViewContainer;
    @BindView(R.id.youtube_link_regulation) View mYouTubeLinkRegulation;
    private Unbinder mUnbinder;
    private MyWebChromeClient mWebChromeClient;
    private View mCustomView; // 全画面表示の動画

    public YouTubeLinkWebViewFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return YouTubeLinkWebViewFragment
     */
    public static YouTubeLinkWebViewFragment newInstance(Bundle args){
        YouTubeLinkWebViewFragment fragment = new YouTubeLinkWebViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Timber.i("Fragment onCreateView");
        View view = inflater.inflate(R.layout.fragment_youtube_link_webview, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        if(savedInstanceState != null){
            // 画面回転などの場合はBundleから復元
            // WebViewClientやWebChromeClientは再設定する必要がある
            mWebView.restoreState(savedInstanceState);
        }

        // 端末のバックキーの設定 最初の2行は設定するための準備、3行目は動作のセット
        mWebView.setFocusableInTouchMode(true);
        mWebView.requestFocus();
        mWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
                    Timber.i("YouTubeLinkWebView BackKey");
                    mPresenter.onBackButtonAction();
                    return true;
                }
                return false;
            }
        });

        // WebViewのJavaScript有効化-戻るボタンの処理に必要
        mWebView.getSettings().setJavaScriptEnabled(true);
        // WebView内のリンクをクリックしても外部ブラウザではなくWebView内で読み込みを行うように設定
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });
        // 動画の全画面表示に必要なWebChromeClientをセット
        mWebChromeClient = new MyWebChromeClient();
        mWebView.setWebChromeClient(mWebChromeClient);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.i("Fragment onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.i("Fragment onPause");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Timber.i("Fragment onDestroyView");
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.i("Fragment onDestroy");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Timber.i("Fragment onSaveInstanceState");
        // WebViewの状態を保存
        mWebView.saveState(outState);
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected YouTubeLinkWebViewPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.YOUTUBE_LINK_WEBVIEW;
    }

    /**
     * WebViewにURLをセット
     * @param url セットするURL文字列
     */
    @Override
    public void loadUrl(String url) {
        mWebView.loadUrl(url);
    }

    /**
     * WebViewを前の画面に戻す
     */
    @Override
    public void goBack() {
        mWebView.goBack();
    }

    /**
     * WebViewが前の画面に戻れるかどうか
     */
    @Override
    public boolean canGoBack() {
        return mWebView.canGoBack();
    }

    /**
     * YouTubeLink走行規制画面の表示
     */
    @Override
    public void setVisibleYouTubeLinkRegulation() {
        Timber.i("YouTubeLinkDisplayOff visible");
        mYouTubeLinkRegulation.setVisibility(View.VISIBLE);
        mWebView.setVisibility(View.INVISIBLE);
    }

    /**
     * YouTubeLink走行規制画面を消す
     */
    @Override
    public void setGoneYouTubeLinkRegulation() {
        Timber.i("YouTubeLinkDisplayOff gone");
        mYouTubeLinkRegulation.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
    }

    /**
     * 動画のフルスクリーンを閉じる
     */
    @Override
    public void closeFullScreen() {
        mWebChromeClient.onHideCustomView();
    }

    /**
     * キーボードを閉じる(YouTubeLinkContainerのメソッド利用)
     */
    @Override
    public void closeKeyBoard() {
        if(getParentFragment() != null && getParentFragment() instanceof YouTubeLinkContainerFragment){
            ((YouTubeLinkContainerFragment) getParentFragment()).closeKeyBoard();
        }
    }

    /**
     * ラストソースに復帰する(間接的に画面を閉じる)
     * キーボードも閉じる
     */
    public void callbackCloseByChangeLastSource() {
        if(getParentFragment() != null && getParentFragment() instanceof YouTubeLinkContainerFragment) {
            ((YouTubeLinkContainerFragment) getParentFragment()).closeKeyBoard();
            ((YouTubeLinkContainerFragment) getParentFragment()).closeContainerDialogByChangeLastSource();
        }
    }

    /**
     * 画面を閉じる(キーボードも閉じる)
     */
    @Override
    public void callbackCloseResetLastSource() {
        if(getParentFragment() != null && getParentFragment() instanceof YouTubeLinkContainerFragment){
            ((YouTubeLinkContainerFragment) getParentFragment()).closeKeyBoard();
            ((YouTubeLinkContainerFragment) getParentFragment()).closeContainerDialogResetLastSource();
        }
    }

    /**
     * 戻るボタンタップ時の動作セット
     */
    @OnClick(R.id.back_button)
    public void onClickBackButton(){
        mPresenter.onBackButtonAction();
    }

    /**
     * 閉じるボタンタップ時の動作セット
     */
    @OnClick(R.id.close_button)
    public void onClickCloseButton(){
        mPresenter.onCloseButtonAction();
    }


    class MyWebChromeClient extends WebChromeClient {

        /**
         * Notify the host application that the current page has entered full
         * screen mode. The host application must show the custom View which
         * contains the web contents &mdash; video or other HTML content &mdash;
         * in full screen mode. Also see "Full screen support" documentation on
         * {@link WebView}.
         *
         * @param view     is the View object to be shown.
         * @param callback invoke this callback to request the page to exit
         */
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            Timber.i("onShowCustomView callback=" + callback);
            if(mCustomView != null){
                callback.onCustomViewHidden();
                return;
            }

            mCustomView = view;
            mCustomViewContainer.setVisibility(View.VISIBLE);
            mCustomViewContainer.bringToFront();
            mCustomViewContainer.addView(mCustomView);
            mWebView.setVisibility(View.GONE);
        }

        /**
         * Notify the host application that the current page has exited full
         * screen mode. The host application must hide the custom View, ie. the
         * View passed to {@link #onShowCustomView} when the content entered fullscreen.
         * Also see "Full screen support" documentation on {@link WebView}.
         */
        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            Timber.i("onHideCustomView mCustomView=" + mCustomView);
            if(mCustomView == null){
                return;
            }

            mCustomViewContainer.removeView(mCustomView);
            mCustomView = null;
            mCustomViewContainer.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
        }
    }
}
