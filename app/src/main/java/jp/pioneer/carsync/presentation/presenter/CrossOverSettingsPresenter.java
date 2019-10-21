package jp.pioneer.carsync.presentation.presenter;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferAudio;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.CrossOverSettingsView;

/**
 * Created by NSW00_906320 on 2017/07/28.
 */
@PresenterLifeCycle
public class CrossOverSettingsPresenter extends Presenter<CrossOverSettingsView> {
    @Inject EventBus mEventBus;
    @Inject PreferAudio mPreferAudio;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject AppSharedPreference mAppSharedPreference;
    @Inject
    public CrossOverSettingsPresenter() {
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    /**
     * StatusHolder取得
     * @return StatusHolder
     */
    public StatusHolder getStatusHolder(){
        return mGetStatusHolder.execute();
    }

    /**
     * UiColor取得
     * @return UiColor
     */
    public int getUiColor(){
        return mAppSharedPreference.getUiColor().getResource();
    }

}
