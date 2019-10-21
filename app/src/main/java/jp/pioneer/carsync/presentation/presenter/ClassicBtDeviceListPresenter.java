package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.interactor.GetPairingDeviceList;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.DebugInfo;
import jp.pioneer.carsync.domain.model.PairingDeviceList;
import jp.pioneer.carsync.domain.model.PairingSpecType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.repository.PairingDeviceListRepository;
import jp.pioneer.carsync.presentation.view.ClassicBtDeviceListView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;

/**
 * ペアリングリストのPresenter
 */
@PresenterLifeCycle
public class ClassicBtDeviceListPresenter extends Presenter<ClassicBtDeviceListView> {
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject Context mContext;
    @Inject GetPairingDeviceList mGetPairingDeviceList;
    private PairingSpecType mType;

    /**
     * コンストラクタ
     */
    @Inject
    public ClassicBtDeviceListPresenter() {
    }

    @Override
    void onTakeView() {
        updateView();
    }

    public void setArgument(Bundle args) {
        SettingsParams params = SettingsParams.from(args);
        if (params.pass.equals(mContext.getString(R.string.dbg_003))) {
            mType = PairingSpecType.CLASSIC_BT;
        } else {
            mType = PairingSpecType.BLE;
        }
    }

    public void onGetListAction() {
        mGetPairingDeviceList.execute(mType, new PairingDeviceListRepository.Callback(){
            @Override
            public void onComplete(PairingSpecType type) {
                updateView();
            }
        });
    }

    private void updateView() {
        StatusHolder holder = mGetStatusHolder.execute();
        DebugInfo info = holder.getDebugInfo();
        PairingDeviceList list;
        list = info.getDeviceList(mType);
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdapter(list.pairingDeviceList));
    }


}
