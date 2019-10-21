package jp.pioneer.carsync.presentation.presenter;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.presentation.view.NowPlayingListContainerView;

/**
 * NowPlayingListContainerのPresenter
 */
@PresenterLifeCycle
public class NowPlayingListContainerPresenter extends Presenter<NowPlayingListContainerView> {
    /**
     * コンストラクタ
     */
    @Inject
    public NowPlayingListContainerPresenter() {
    }
}
