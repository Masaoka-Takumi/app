package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.VideoPlayerDialogPresenter;
import jp.pioneer.carsync.presentation.view.VideoPlayerDialogView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * Created by NSW00_007906 on 2019/01/11.
 */

public class VideoPlayerDialogFragment extends AbstractScreenFragment<VideoPlayerDialogPresenter, VideoPlayerDialogView> implements VideoPlayerDialogView {
    @Inject VideoPlayerDialogPresenter mPresenter;
    @BindView(R.id.myWebView) WebView mWebView;
    @BindView(R.id.customView_frame) FrameLayout customViewContainer;
    private View mCustomView;
    private myWebChromeClient mWebChromeClient;
    //private myWebViewClient mWebViewClient;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private Unbinder mUnbinder;
    public VideoPlayerDialogFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return VideoPlayerDialogFragment
     */
    public static VideoPlayerDialogFragment newInstance(Bundle args) {
        VideoPlayerDialogFragment fragment = new VideoPlayerDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_player, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        view.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if(event.getAction() == KeyEvent.ACTION_UP) {
                    if (inCustomView()) {
                        hideCustomView();
                        return true;
                    }else if ((mCustomView == null) && mWebView.canGoBack()) {
                        mWebView.goBack();
                        return true;
                    }else{
                        getPresenter().onBackAction();
                    }
                }
                return true;
            }
            return false;
        });
        String html = "";
        html += "<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\"/></head><body bgcolor=\"#000000\"><div>";
        // html += "<iframe style=\"position: absolute;top:0;right:0;width:100% !important;height:100% !important;\" src=\"https://www.youtube.com/embed/ebpZNYyPPQU?autoplay=1&playsinline=1&rel=0\" frameborder=\"0\"></iframe>";
        html += "<iframe width=\"100%\" height=\"45%\" src=\"https://www.youtube.com/embed/ebpZNYyPPQU?autoplay=1&playsinline=1&rel=0\" frameborder=\"0\"></iframe>";
        html += "</div></body></html>";
        //mWebViewClient = new myWebViewClient();
        //mWebView.setWebViewClient(mWebViewClient);

        mWebChromeClient = new myWebChromeClient();
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAppCacheEnabled(true);
        html = "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/ebpZNYyPPQU?controls=0&amp;autoplay=1&amp;showinfo=0\" frameborder=\"0\" allowfullscreen></iframe>";
        mWebView.setWebChromeClient(mWebChromeClient);
        //mWebView.loadData(html, "text/html", "utf-8");
        mWebView.loadUrl("https://jpn.pioneer/ja/support/pcperipherals/app/pioneer_smart_sync/driving_support_eye/");
        mWebView.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onResume() {
        mWebView.onResume();
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
    @Override
    public void onStop() {
        super.onStop();    //To change body of overridden methods use File | Settings | File Templates.
        if (inCustomView()) {
            hideCustomView();
        }
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected VideoPlayerDialogPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.VIDEO_PLAYER;
    }
    public boolean onBack() {
        if (inCustomView()) {
            hideCustomView();
            return true;
        }else if ((mCustomView == null) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }
    public boolean inCustomView() {
        return (mCustomView != null);
    }

    public void hideCustomView() {
        mWebChromeClient.onHideCustomView();
    }

    class myWebChromeClient extends WebChromeClient {

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {

            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }

/*            final FrameLayout frame = ((FrameLayout) view);

            final View v1 = frame.getChildAt(0);
*//*            view.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
            v1.setOnKeyListener(new View.OnKeyListener() {

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                        onHideCustomView();
                        return true;
                    }
                    return false;
                }
            });*/

            mCustomView = view;
            customViewContainer.setVisibility(View.VISIBLE);
            customViewContainer.setBackgroundColor(Color.BLACK);
            customViewContainer.bringToFront();
            mWebView.setVisibility(View.GONE);

            customViewContainer.addView(mCustomView);
            customViewCallback = callback;
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            if (mCustomView == null)
                return;
            customViewContainer.removeView(mCustomView);
            mCustomView = null;
            customViewContainer.setVisibility(View.INVISIBLE);
            mWebView.setVisibility(View.VISIBLE);
            customViewCallback.onCustomViewHidden();
        }
    }

/*    class myWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }*/
}
