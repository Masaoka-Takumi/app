package jp.pioneer.carsync.presentation.presenter;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.view.VideoPlayerDialogView;

/**
 * Created by NSW00_007906 on 2019/01/11.
 */

public class VideoPlayerDialogPresenter extends Presenter<VideoPlayerDialogView>{
    @Inject EventBus mEventBus;

    /**
     * コンストラクタ.
     */
    @Inject
    public VideoPlayerDialogPresenter() {
    }

    public void onBackAction() {
        mEventBus.post(new GoBackEvent());
    }
}
