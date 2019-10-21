package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.presentation.view.HomeContainerView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * Created by BP06566 on 2017/03/02.
 */
@PresenterLifeCycle
public class HomeContainerPresenter extends Presenter<HomeContainerView> {

    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;

    @Inject
    public HomeContainerPresenter() {
    }

    @Override
    void onInitialize() {
        Optional.ofNullable(getView()).ifPresent(view -> view.onNavigate(ScreenId.HOME, Bundle.EMPTY));
    }
}
