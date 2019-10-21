package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferAdas;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.AdasTutorialView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * AdasTutorialPresenter
 */
@PresenterLifeCycle
public class AdasTutorialPresenter extends Presenter<AdasTutorialView> {
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject GetStatusHolder mGetCase;
    @Inject PreferAdas mPreferAdas;
    private Bundle mArguments;

    /**
     * コンストラクタ.
     */
    @Inject
    public AdasTutorialPresenter() {
    }

    /**
     * Backアクション
     */
    public void onBackAction(){
        mEventBus.post(new GoBackEvent());
    }

    /**
     * Skipアクション
     */
    public void onSkipAction(){
        mEventBus.post(new NavigateEvent(ScreenId.ADAS_USAGE_CAUTION, createSettingsParams(ScreenId.ADAS_TUTORIAL, mContext.getString(R.string.set_340)+mContext.getString(R.string.set_370))));

    }

    private Bundle createSettingsParams(ScreenId screenId, String pass) {
        SettingsParams params = new SettingsParams();
        params.mScreenId = screenId;
        params.pass = pass;
        return params.toBundle();
    }
}
