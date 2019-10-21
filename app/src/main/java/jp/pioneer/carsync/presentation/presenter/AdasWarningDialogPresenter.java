package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Handler;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.EnumSet;
import java.util.Set;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.AdasWarningUpdateEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.UpdateWarningEvents;
import jp.pioneer.carsync.domain.model.AdasWarningEvent;
import jp.pioneer.carsync.domain.model.AdasWarningStatus;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.presentation.view.AdasWarningDialogView;
import timber.log.Timber;

/**
 * ADAS警報表示のPresenter
 */
@PresenterLifeCycle
public class AdasWarningDialogPresenter extends Presenter<AdasWarningDialogView> {
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject UpdateWarningEvents mUpdateWarningEvents;
    private Handler mHandler = new Handler();
    /**
     * コンストラクタ
     */
    @Inject
    public AdasWarningDialogPresenter() {
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        updateView();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
        mUpdateWarningEvents.execute(EnumSet.noneOf(AdasWarningEvent.class));
    }

    private void updateView() {
        AppStatus appStatus =mGetStatusHolder.execute().getAppStatus();
        mUpdateWarningEvents.execute(appStatus.adasWarningEvents);
        AdasWarningStatus adasWarningStatus = appStatus.getAdasWarningStatus();
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (adasWarningStatus == AdasWarningStatus.NONE) {
                view.callbackClose();
            } else {
                Set<AdasWarningEvent> list = appStatus.adasWarningEvents;
                int adasImageResource;
                String adasText = "";
                //list.add(AdasWarningEvent.PEDESTRIAN_WARNING_EVENT);
                if (list.contains(AdasWarningEvent.PEDESTRIAN_WARNING_EVENT) || list.contains(AdasWarningEvent.PEDESTRIAN_CAREFUL_EVENT)) {
                    //歩行者衝突予測
                    adasImageResource = R.drawable.p1202_pcw;
                    adasText = mContext.getString(R.string.ads_004);
                } else if (list.contains(AdasWarningEvent.FORWARD_TTC_COLLISION_EVENT) || list.contains(AdasWarningEvent.FORWARD_HEADWAY_COLLISION_EVENT)) {
                    //前方衝突予測
                    adasImageResource = R.drawable.p1201_fcw;
                    adasText = mContext.getString(R.string.ads_003);
                } else if (list.contains(AdasWarningEvent.OFF_ROAD_LEFT_SOLID_EVENT) || list.contains(AdasWarningEvent.OFF_ROAD_LEFT_DASH_EVENT)) {
                    //車線逸脱（左）
                    adasImageResource = R.drawable.p1203_ldw_l;
                    adasText = mContext.getString(R.string.ads_002);
                } else if (list.contains(AdasWarningEvent.OFF_ROAD_RIGHT_SOLID_EVENT) || list.contains(AdasWarningEvent.OFF_ROAD_RIGHT_DASH_EVENT)) {
                    //車線逸脱（右）
                    adasImageResource = R.drawable.p1204_ldw_r;
                    adasText = mContext.getString(R.string.ads_002);
                } else if (list.contains(AdasWarningEvent.LANE_KEEP_WARNING_EVENT)) {
                    //LKW
                    adasImageResource = R.drawable.p1205_lkw;
                    adasText = mContext.getString(R.string.ads_005);
                } else {
                    //INVALID_PARAMETER_EVENT,
                    //ENGINE_EXCEPTION_EVENT,
                    //AUTHORIZATION_REQUIRED_EVENT,
                    //は対応未定
                    adasImageResource = 0;
                    adasText = mContext.getString(R.string.unknown);
                }
                Timber.d("AdasWarningDialogPresenter:adasText:%s",adasText);
                view.setAdasImage(adasImageResource);
                view.setAdasText(adasText);
            }

        });
    }

    /**
     * ADAS警報状態更新イベントハンドラ
     *
     * @param ev ADAS警報状態更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAdasWarningUpdateEvent(AdasWarningUpdateEvent ev) {
        updateView();
    }
}
