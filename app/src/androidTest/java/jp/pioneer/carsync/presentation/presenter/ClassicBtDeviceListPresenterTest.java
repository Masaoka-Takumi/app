package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.interactor.GetPairingDeviceList;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.DebugInfo;
import jp.pioneer.carsync.domain.model.PairingDeviceList;
import jp.pioneer.carsync.domain.model.PairingSpecType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.repository.PairingDeviceListRepository;
import jp.pioneer.carsync.presentation.view.ClassicBtDeviceListView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ペアリングデバイスリスト画面のPresenterのテスト
 */
public class ClassicBtDeviceListPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ClassicBtDeviceListPresenter mPresenter = new ClassicBtDeviceListPresenter();
    @Mock ClassicBtDeviceListView mView;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetStatusHolder;
    @Mock Context mContext;
    @Mock GetPairingDeviceList mGetPairingDeviceList;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
    }

    @Test
    public void onTakeView() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        DebugInfo info = mock(DebugInfo.class);
        PairingDeviceList list1 = new PairingDeviceList(PairingSpecType.CLASSIC_BT);
        PairingDeviceList list2 = new PairingDeviceList(PairingSpecType.BLE);
        when(info.getDeviceList(PairingSpecType.CLASSIC_BT)).thenReturn(list1);
        when(info.getDeviceList(PairingSpecType.BLE)).thenReturn(list2);
        when(mockHolder.getDebugInfo()).thenReturn(info);
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);

        mPresenter.setArgument(createSettingsParams(mContext.getString(R.string.setting_debug_classic_bt_link_key)));
        mPresenter.onTakeView();

        verify(mView).setAdapter(list1.pairingDeviceList);
    }

    @Test
    public void onGetListAction() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        DebugInfo info = mock(DebugInfo.class);
        PairingDeviceList list1 = new PairingDeviceList(PairingSpecType.CLASSIC_BT);
        PairingDeviceList list2 = new PairingDeviceList(PairingSpecType.BLE);
        when(info.getDeviceList(PairingSpecType.CLASSIC_BT)).thenReturn(list1);
        when(info.getDeviceList(PairingSpecType.BLE)).thenReturn(list2);
        when(mockHolder.getDebugInfo()).thenReturn(info);
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);

        mPresenter.setArgument(createSettingsParams(mContext.getString(R.string.setting_debug_ble_link_key)));
        mPresenter.onGetListAction();

        verify(mGetPairingDeviceList).execute(eq(PairingSpecType.BLE),any(PairingDeviceListRepository.Callback.class));
        //verify(mView).setAdapter(list2.pairingDeviceList);
    }

    private Bundle createSettingsParams(String pass) {
        SettingsParams params = new SettingsParams();
        params.pass = pass;
        return params.toBundle();
    }
}