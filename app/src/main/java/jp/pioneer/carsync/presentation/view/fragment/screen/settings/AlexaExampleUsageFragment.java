package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.AlexaExampleUsagePresenter;
import jp.pioneer.carsync.presentation.view.AlexaExampleUsageView;
import jp.pioneer.carsync.presentation.view.adapter.AlexaTutorialPagerAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.SingleChoiceDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderPlayerInfoItem;
import jp.pioneer.mbg.alexa.AmazonAlexaManager;
import jp.pioneer.mbg.alexa.manager.AlexaQueueManager;
import timber.log.Timber;

/**
 * AlexaExampleUsageFragment
 */

public class AlexaExampleUsageFragment extends AbstractScreenFragment<AlexaExampleUsagePresenter, AlexaExampleUsageView>
        implements AlexaExampleUsageView, StatusPopupDialogFragment.Callback, SingleChoiceDialogFragment.Callback {
    @Inject AlexaExampleUsagePresenter mPresenter;
    @BindView(R.id.back_button) ImageView mBackBtn;
    @BindView(R.id.next_button) ImageView mNextBtn;
    @BindView(R.id.directory_pass_text) TextView mDirectoryPass;
    @BindView(R.id.viewPager) ViewPager mViewPager;
    @BindView(R.id.indicator) CirclePageIndicator mIndicator;
//    @BindView(R.id.text_view_link) TextView mTextViewLink;
    private Unbinder mUnbinder;
    private AlexaTutorialPagerAdapter mPagerAdapter;
    /** Alexaマネージャ. */
    AmazonAlexaManager mAmazonAlexaManager;
    private AlexaCallback mAlexaCallback = new AlexaCallback();
    /**
     * コンストラクタ
     */
    public AlexaExampleUsageFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return AlexaExampleUsageFragment
     */
    public static AlexaExampleUsageFragment newInstance(Bundle args) {
        AlexaExampleUsageFragment fragment = new AlexaExampleUsageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_alexa_example_usage, container, false);
        mUnbinder = ButterKnife.bind(this, view);
//        String str1 = getString(R.string.set_320);
//        String linkStr = getString(R.string.set_322);
//        int result = str1.indexOf(linkStr);
//        if (result != -1) {
//            String htmlBlogStr = str1.substring(0, result)
//                    + "<font color =\"#00a5cf\" ><a href=\"https://play.google.com/store/apps/details?id=com.amazon.dee.app\">" + linkStr + "</a></font>"
//                    + str1.substring(result+linkStr.length());
//            CharSequence blogChar = fromHtml(htmlBlogStr);
//            mTextViewLink.setText(blogChar);
//            MovementMethod mMethod = LinkMovementMethod.getInstance();
//            mTextViewLink.setMovementMethod(mMethod);
//        } else {
//            mTextViewLink.setText(str1);
//        }
//        mBackBtn.setVisibility(View.VISIBLE);
//        mNextBtn.setVisibility(View.VISIBLE);
        view.requestFocus();
        view.setFocusableInTouchMode(true);
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    Timber.i("AlexaExampleUsage BackKey");
                    onBackAction();
                    return true;
                }
                return false;
            }
        });
        mPagerAdapter = new AlexaTutorialPagerAdapter(getActivity());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int i) {
                switch (i) {
                    case 0:
//                        mDirectoryPass.setText(R.string.set_403);
                        mDirectoryPass.setText("はじめよう");
                        break;
                    case 1:
//                        mDirectoryPass.setText(R.string.set_406);
                        mDirectoryPass.setText("使い方を知ろう");
                        break;
                    case 2:
                        mDirectoryPass.setText(R.string.set_318);
                        break;
                    default:
                        mBackBtn.setVisibility(View.INVISIBLE);
                        mNextBtn.setVisibility(View.INVISIBLE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        mIndicator.setViewPager(mViewPager);

        return view;
    }

    @SuppressWarnings("deprecation")
    public static CharSequence fromHtml(String html){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(html);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAmazonAlexaManager = AmazonAlexaManager.getInstance();
        if (mAmazonAlexaManager != null) {
            mAmazonAlexaManager.addAlexaCallback(mAlexaCallback);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAmazonAlexaManager != null) {
            mAmazonAlexaManager.removeAlexaCallback(mAlexaCallback);
        }
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
    protected AlexaExampleUsagePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.ALEXA_EXAMPLE_USAGE;
    }

    @OnClick(R.id.back_button)
    public void onClickBackButton() {
        onBackAction();
    }

    @OnClick(R.id.next_button)
    public void onClickNextButton() {
        getPresenter().onNextAction();
    }

    public boolean onBackAction() {
        if(mViewPager.getCurrentItem() == 0) {
            getPresenter().onBackAction();
            return true;
        } else {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
            return true;
        }
    }

    @Override
    public void onClose(StatusPopupDialogFragment fragment, String tag) {

    }

    @Override
    public void onPositiveClick(StatusPopupDialogFragment fragment, String tag) {
        if (tag.equals(AlexaExampleUsagePresenter.TAG_DIALOG_ALEXA_SIGN_OUT)) {
            if (mAmazonAlexaManager != null) {
                mAmazonAlexaManager.logoutAlexa();
            }
        }else if(tag.equals(AlexaExampleUsagePresenter.TAG_DIALOG_ALEXA_MIC_PROMPT)){
            getPresenter().showLanguageSelectDialog();
        }
    }

    @Override
    public void onNegativeClick(StatusPopupDialogFragment fragment, String tag) {
    }

    @Override
    public void onClose(SingleChoiceDialogFragment fragment) {

    }

    @Override
    public void selectItem(int position) {
        getPresenter().setAlexaLanguage(position);
    }
    /**
     * アレクサのイベントのコールバックを受けるメソッド.
     */
    private class AlexaCallback implements AmazonAlexaManager.IAlexaCallback {
        @Override
        public void onLoginSuccess() {

        }

        @Override
        public void onLogout() {
            Timber.d("onLogout");
            getPresenter().onLogout();
        }

        @Override
        public void onConnect() {

        }

        @Override
        public void onDisConnect() {

        }

        @Override
        public void onNetworkConnect() {

        }

        @Override
        public void onNetworkDisconnect() {

        }

        @Override
        public void onRecordingStart() {

        }

        @Override
        public void onRecordingMonitor(double db, int hertz) {

        }

        @Override
        public void onRecordingStop(boolean isCancel) {

        }

        @Override
        public void onSpeakingPrepare() {

        }

        @Override
        public void onSpeakingPrepared() {

        }

        @Override
        public void onSpeakingStart() {

        }

        @Override
        public void onSpeakingResume() {

        }

        @Override
        public void onSpeakingPause() {

        }

        @Override
        public void onSpeakingStop() {

        }

        @Override
        public void onSpeakingComplete() {

        }

        @Override
        public void onReceiveRenderPlayerInfo(RenderPlayerInfoItem playerInfoItem) {

        }

        @Override
        public void onAudioPrepare() {

        }

        @Override
        public void onAudioPrepared() {

        }

        @Override
        public void onAudioStart() {

        }

        @Override
        public void onAudioResume() {

        }

        @Override
        public void onAudioPause() {

        }

        @Override
        public void onAudioStop() {

        }

        @Override
        public void onAudioError() {

        }

        @Override
        public void onAudioComplete() {

        }

        @Override
        public void onAudioUpdateProgress(int current, int duration) {

        }

        @Override
        public void onSystemError() {

        }

        @Override
        public void onAlertStarted() {

        }

        @Override
        public void onShortAlertStarted() {

        }

        @Override
        public void onAlertStopped() {

        }

        @Override
        public void onSetAlert() {

        }

        @Override
        public void onStopAlertAll() {

        }

        @Override
        public void onPersistVisualIndicator() {

        }

        @Override
        public void onClearVisualIndicator() {

        }

        @Override
        public void onAudioIndicatorStarted() {

        }

        @Override
        public void onAudioIndicatorStopped() {

        }

        @Override
        public void onSetVolume(float volume) {

        }

        @Override
        public void onAdjustVolume(float volume) {

        }

        @Override
        public void onSetMute(boolean isMute) {

        }

        @Override
        public void onNoResponse() {

        }

        @Override
        public void onChannelActiveChange(AlexaQueueManager.AlexaChannel channel, boolean isActive) {

        }

        @Override
        public void onMicrophonePermission(int state) {

        }

        @Override
        public void onNoDirectiveAtSendEventResponse() {

        }

        @Override
        public void onLoginFailed() {

        }

        @Override
        public void onCapabilitiesSendSuccess() {
        }

        @Override
        public void onSetNaviDestination(Double latitude, Double longitude, String name) {

        }

        @Override
        public void onRecordingNotAvailable() {

        }
        @Override
        public void onWLAudioFocusLoss() {

        }

        @Override
        public void onDecodeStart() {

        }

        @Override
        public void onDecodeFinish() {

        }
    }
}
