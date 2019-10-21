package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.RadioInfoChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.ControlRadioSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TunerFrequencyUnit;
import jp.pioneer.carsync.domain.model.TunerStatus;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.RadioTabContainerView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ラジオリストコンテナ画面presenterのテストコード
 */
public class RadioTabContainerPresenterTest {

    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks RadioTabContainerPresenter mPresenter = new RadioTabContainerPresenter();
    @Mock RadioTabContainerView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock ControlMediaList mMediaCase;
    @Mock AppSharedPreference mPreference;
    @Mock ControlRadioSource mControlCase;
    @Mock GetStatusHolder mStatusHolder;

    private RadioInfo mTestRadio;
    private RadioTabContainerPresenter.RadioTabType mTab;
    private MediaSourceType mSourceType;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mTestRadio = new RadioInfo();
        mTestRadio.band = RadioBandType.FM1;
        mTestRadio.psInfo = "TEST PS";
        mTestRadio.ptyInfo = "TEST PTY";
        mTestRadio.currentFrequency = 99999L;
        mTestRadio.frequencyUnit = TunerFrequencyUnit.MHZ2;
        mTestRadio.songTitle = "TEST SONG";
        mTestRadio.artistName = "TEST ARTIST";
        mTestRadio.antennaLevel = 8;
        mTestRadio.maxAntennaLevel = 10;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
    }

    @Test
    public void testOnInitialize() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = new CarDeviceStatus();
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);

        mockMediaHolder.radioInfo = mTestRadio;
        mockStatus.sourceType = MediaSourceType.RADIO;
        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);
        mPresenter.takeView(mView);
        mPresenter.initialize();

        verify(mView).setTitle("FM1");
        verify(mView).onNavigate(ScreenId.RADIO_PRESET_LIST, Bundle.EMPTY);
    }

    @Test
    public void testInstanceState() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        Bundle args = new Bundle();
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = new CarDeviceStatus();
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);

        mockMediaHolder.radioInfo = mTestRadio;
        mockStatus.sourceType = MediaSourceType.RADIO;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);
        mPresenter.takeView(mView);
        mPresenter.initialize();
        mPresenter.onFavoriteAction();
        mPresenter.saveInstanceState(args);
        //mPresenter = new RadioTabContainerPresenter();
        mPresenter.onRestoreInstanceState(args);

        verify(mView).setTitle("FM1");
    }
    @Test
    public void testOnPresetAction() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = new CarDeviceStatus();
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);

        mockMediaHolder.radioInfo = mTestRadio;
        mockStatus.sourceType = MediaSourceType.RADIO;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);
        mPresenter.takeView(mView);
        mPresenter.onPresetAction();

        verify(mView).setTitle("FM1");
        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.RADIO_PRESET_LIST));
        assertThat(event.args, is(Bundle.EMPTY));
    }

    @Test
    public void testOnFavoriteAction() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = new CarDeviceStatus();
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);

        mockMediaHolder.radioInfo = mTestRadio;
        mockStatus.sourceType = MediaSourceType.RADIO;
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);
        mPresenter.takeView(mView);
        mPresenter.onFavoriteAction();

        verify(mView).setTitle("Favorite");
        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.RADIO_FAVORITE_LIST));
        assertThat(event.args, is(Bundle.EMPTY));
    }

    @Test
    public void testOnBackAction() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus carDeviceStatus = mock(CarDeviceStatus.class);
        carDeviceStatus.listType = ListType.PCH_LIST;
        carDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        when(holder.getCarDeviceStatus()).thenReturn(carDeviceStatus);
        when(mStatusHolder.execute()).thenReturn(holder);
        mPresenter.onClose();

        verify(mEventBus).post(any(GoBackEvent.class));
    }

    @Test
    public void testOnCloseAction() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus carDeviceStatus = mock(CarDeviceStatus.class);
        carDeviceStatus.listType = ListType.PCH_LIST;
        carDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        when(holder.getCarDeviceStatus()).thenReturn(carDeviceStatus);
        when(mStatusHolder.execute()).thenReturn(holder);
        mPresenter.onClose();

        verify(mEventBus).post(any(GoBackEvent.class));
    }

    @Test
    public void testOnRadioInfoChangeEvent() throws Exception {
        RadioInfoChangeEvent event = mock(RadioInfoChangeEvent.class);

        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);
        CarDeviceStatus mockStatus = new CarDeviceStatus();
        RadioInfo info = mTestRadio;
        info.tunerStatus = TunerStatus.BSM;
        mockStatus.sourceType = MediaSourceType.RADIO;
        mockMediaHolder.radioInfo = info;
        when(mStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);
        mPresenter.onPresetAction();
        mPresenter.onRadioInfoChangeEvent(event);

        verify(mView,atLeast(1)).onNavigate(eq(ScreenId.RADIO_BSM), any(Bundle.class));
    }

    @Test
    public void testOnBsmAction() throws Exception {
        mPresenter.onBsmAction();
        verify(mControlCase).startBsm();
    }
}