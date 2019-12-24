package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.AlexaLanguageType;
import jp.pioneer.carsync.presentation.presenter.AlexaSettingPresenter;
import jp.pioneer.carsync.presentation.view.AlexaSettingView;
import jp.pioneer.carsync.presentation.view.adapter.AlexaSettingAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.SingleChoiceDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderPlayerInfoItem;
import jp.pioneer.mbg.alexa.AmazonAlexaManager;
import jp.pioneer.mbg.alexa.manager.AlexaQueueManager;
import timber.log.Timber;


/**
 * AlexaSettingFragment
 * TODO 他設定画面と同様にAbstractPreferenceFragmentで画面を作る
 *       (過去、そうできない理由があったが今は問題ないため)
 */

public class AlexaSettingFragment extends AbstractScreenFragment<AlexaSettingPresenter, AlexaSettingView>
        implements AlexaSettingView, StatusPopupDialogFragment.Callback, SingleChoiceDialogFragment.Callback{
    @Inject AlexaSettingPresenter mPresenter;
    @BindView(R.id.list_view) ListView mListView;
    private Unbinder mUnbinder;
    /** Alexaマネージャ. */
    AmazonAlexaManager mAmazonAlexaManager;
    private AlexaCallback mAlexaCallback = new AlexaCallback();
    private AlexaSettingAdapter mAdapter;
    /**
     * コンストラクタ
     */
    public AlexaSettingFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return ImpactDetectionSettingsFragment
     */
    public static AlexaSettingFragment newInstance(Bundle args) {
        AlexaSettingFragment fragment = new AlexaSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_alexa, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(getString(R.string.set_304));
        if(!mPresenter.isSessionStarted()) {
            arrayList.add(getString(R.string.set_412));
        }
        arrayList.add(getString(R.string.set_303));
        mAdapter = new AlexaSettingAdapter(getContext(), arrayList);
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mAmazonAlexaManager != null) {
            mAmazonAlexaManager.removeAlexaCallback(mAlexaCallback);
        }
        mUnbinder.unbind();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected AlexaSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.ALEXA_SETTING;
    }

    @Override
    public void onClose(StatusPopupDialogFragment fragment, String tag) {

    }

    @Override
    public void onPositiveClick(StatusPopupDialogFragment fragment, String tag) {
        if (tag.equals(AlexaSettingPresenter.TAG_DIALOG_ALEXA_SIGN_OUT)) {
            //車載器接続切断後の終了処理で破棄している場合がある
            mAmazonAlexaManager = AmazonAlexaManager.getInstance();
            if (mAmazonAlexaManager != null) {
                mAmazonAlexaManager.addAlexaCallback(mAlexaCallback);
                mAmazonAlexaManager.logoutAlexa();
            }
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
        setAlexaLanguage(AlexaLanguageType.valueOf((byte)position).label);
    }

    @OnItemClick(R.id.list_view)
    public void onClickListItem(AdapterView<?> parent, View view, int position, long id) {
        if(position == 0) {
            getPresenter().showLanguageSelectDialog();
            return;
        }
        if(position == 1) {
            if(!mPresenter.isSessionStarted()) {
                getPresenter().onNavigateAlexaUsage();
            } else {
                getPresenter().showSignOutDialog();
            }
            return;
        }
        if(position == 2) {
            getPresenter().showSignOutDialog();
            return;
        }
    }

    @Override
    public void setAlexaLanguage(@StringRes int src) {
        mAdapter.setAlexaLanguageSetting(getString(src));
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
