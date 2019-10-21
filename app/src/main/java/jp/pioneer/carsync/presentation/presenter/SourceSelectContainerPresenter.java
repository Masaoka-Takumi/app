package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.view.SourceSelectContainerView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * Source Select ContainerのPresenter
 */
@PresenterLifeCycle
public class SourceSelectContainerPresenter extends Presenter<SourceSelectContainerView>{
    @Inject Context mContext;
    @Inject EventBus mEventBus;

    /**
     * コンストラクタ
     */
    @Inject
    public SourceSelectContainerPresenter() {
    }

    @Override
    void onInitialize() {
       Optional.ofNullable(getView()).ifPresent(view -> view.onNavigate(ScreenId.SOURCE_SELECT, null));
    }

    /**
     * 戻るボタン処理
     */
    public void onBackAction() {
        mEventBus.post(new GoBackEvent());
    }

}
