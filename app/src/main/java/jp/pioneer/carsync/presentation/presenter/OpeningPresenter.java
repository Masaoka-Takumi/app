package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.interactor.ControlSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.event.ShowCautionEvent;
import jp.pioneer.carsync.presentation.view.OpeningView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * 起動画面のPresenter
 */

@PresenterLifeCycle
public class OpeningPresenter extends Presenter<OpeningView> {

    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject Context mContext;
    @Inject ControlSource mControlSource;
    private boolean mIsRunTask;
    private boolean mIsStartAnimation;

    private Handler mHandler = new Handler();

    @Inject
    public OpeningPresenter() {
    }

    @Override
    void onTakeView() {
        if(!mIsStartAnimation) {
            Optional.ofNullable(getView()).ifPresent(OpeningView::startAnimation);
            mIsStartAnimation = true;
        }
    }

    @Override
    void onResume() {
        if(mIsRunTask) {
            Optional.ofNullable(getView()).ifPresent(OpeningView::stopAnimation);
            mHandler.postDelayed(mSendFragmentTask, 1000);
        }
    }

    @Override
    void onPause() {
        if(mIsRunTask) {
            mHandler.removeCallbacks(mSendFragmentTask);
        }
    }

    /**
     * 遷移処理.
     * <p>
     * GIF準備前にViewがDestroyされてしまった場合に使用する。
     */
    public void onSendAction(){
        if(!mIsRunTask) {
            mHandler.removeCallbacks(mSendFragmentTask);
            mHandler.postDelayed(mSendFragmentTask, 1000);
            mIsRunTask = true;
        }
    }

    /**
     * 遷移処理
     * <p>
     * {@code duration}にはGIFのアニメーション時間を設定する。
     *
     * @param duration GIFアニメーション時間
     */
    public void onSendAction(int duration){
        mHandler.removeCallbacks(mSendFragmentTask);
        mHandler.postDelayed(mSendFragmentTask, duration);
        mIsRunTask = true;
    }

    private Runnable mSendFragmentTask = new Runnable() {
        public void run() {
            if (!mPreference.isAgreedEulaPrivacyPolicy()) {
                mEventBus.post(new NavigateEvent(ScreenId.OPENING_EULA, Bundle.EMPTY));
            } else {
                StatusHolder holder = mGetStatusHolder.execute();
                if(holder.getSessionStatus() == SessionStatus.STARTED) {
                    if (mPreference.isFirstInitialSettingCompletion()) {
                        mEventBus.post(new NavigateEvent(ScreenId.HOME_CONTAINER, Bundle.EMPTY));
                    } else {
                        if (holder.getProtocolSpec().isSphCarDevice()) {
                            //mControlSource.selectSource(MediaSourceType.OFF);
                            transitionFirstInitialSetting();
                        } else {
                            mPreference.setFirstInitialSettingCompletion(true);
                            mEventBus.post(new NavigateEvent(ScreenId.HOME_CONTAINER, Bundle.EMPTY));
                        }
                    }
                    mEventBus.post(new ShowCautionEvent());
                } else {
                    mEventBus.post(new NavigateEvent(ScreenId.UNCONNECTED_CONTAINER, Bundle.EMPTY));
                }
            }
        }
    };

    private void transitionFirstInitialSetting() {
        SettingsParams params = new SettingsParams();
        params.pass = mContext.getString(R.string.set_104);
        params.mScreenId = ScreenId.SETTINGS_SYSTEM_INITIAL;
        mEventBus.post(new NavigateEvent(ScreenId.SETTINGS_CONTAINER, params.toBundle()));
    }
}