package jp.pioneer.carsync.presentation.presenter;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferMusicApp;
import jp.pioneer.carsync.domain.interactor.PreferNaviApp;
import jp.pioneer.carsync.domain.interactor.PreferReadNotification;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.MessagingApp;
import jp.pioneer.carsync.domain.model.MusicApp;
import jp.pioneer.carsync.domain.model.NaviApp;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.model.SourceSelectItem;
import jp.pioneer.carsync.presentation.view.SourceSelectView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ソース選択のPresenterのテスト
 */
public class SourceSelectPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SourceSelectPresenter mPresenter = new SourceSelectPresenter();
    @Mock SourceSelectView mView;
    @Mock ControlSource mControlSource;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetCase;
    @Mock AppSharedPreference mPreference;
    @Mock PreferMusicApp mPreferMusicApp;

    @Captor ArgumentCaptor<List<SourceSelectItem>> argument;

    private Set<MediaSourceType> mTypeSet = new HashSet<>();
    private AppSharedPreference.Application[] mMusicAppList;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        mTypeSet.clear();
        mTypeSet.add(MediaSourceType.APP_MUSIC);
        mTypeSet.add(MediaSourceType.RADIO);
        mTypeSet.add(MediaSourceType.CD);
        mTypeSet.add(MediaSourceType.PANDORA);
        mTypeSet.add(MediaSourceType.SPOTIFY);
        mMusicAppList = new AppSharedPreference.Application[]{
                new AppSharedPreference.Application(MusicApp.PANDORA.getPackageName(), MusicApp.SPOTIFY.getPackageName()),
        };
    }

    @Test
    public void testOnTakeView() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus deviceStatus = mock(CarDeviceStatus.class);
        deviceStatus.availableSourceTypes = mTypeSet;
        deviceStatus.sourceType = MediaSourceType.CD;
        when(holder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(holder);
        when(mPreferMusicApp.getSelectedAppList()).thenReturn(mMusicAppList);
        mPresenter.onTakeView();
        verify(mView).setColor(anyInt());
        verify(mView).setAdapter(argument.capture());
        int i = 0;
        for (MediaSourceType type : mTypeSet) {
            if (type == deviceStatus.sourceType) {
                break;
            }
            i++;
        }
        verify(mView).setCurrentSource(anyInt());
        assertThat(argument.getValue().size(), is(5));
    }

    /**
     * OnResumeのテスト EventBus未登録の場合
     */
    @Test
    public void testOnResume() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);
        mPresenter.onResume();

        verify(mEventBus).register(mPresenter);
    }

    /**
     * OnResumeのテスト EventBus登録済の場合
     */
    @Test
    public void testOnResume2() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(true);
        mPresenter.onResume();

        verify(mEventBus, times(0)).register(mPresenter);
    }

    /**
     * onPauseのテスト
     */
    @Test
    public void testOnPause() throws Exception {
        mPresenter.onPause();
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void testOnChangeSourceAction() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus deviceStatus = mock(CarDeviceStatus.class);
        deviceStatus.sourceType = MediaSourceType.RADIO;
        when(holder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onChangeSourceAction(MediaSourceType.SPOTIFY);
        verify(mView, times(0)).dismissDialog();
        verify(mControlSource).selectSource(MediaSourceType.SPOTIFY);
    }

    @Test
    public void testOnChangeSourceAction2() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus deviceStatus = mock(CarDeviceStatus.class);
        deviceStatus.sourceType = MediaSourceType.RADIO;
        when(holder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onChangeSourceAction(MediaSourceType.RADIO);
        verify(mView).dismissDialog();
        verify(mControlSource).selectSource(MediaSourceType.RADIO);
    }

    @Test
    public void testOnMediaSourceTypeChangeAction() throws Exception {
        MediaSourceTypeChangeEvent event = mock(MediaSourceTypeChangeEvent.class);
        mPresenter.onMediaSourceTypeChangeAction(event);
        verify(mView).dismissDialog();
    }

    @Test
    public void testOnCarDeviceStatusChangeEvent() throws Exception {
        MediaSourceTypeChangeEvent event = mock(MediaSourceTypeChangeEvent.class);

        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus deviceStatus = mock(CarDeviceStatus.class);
        mTypeSet.add(MediaSourceType.IPOD);
        deviceStatus.availableSourceTypes = mTypeSet;
        deviceStatus.sourceType = MediaSourceType.RADIO;
        when(holder.getCarDeviceStatus()).thenReturn(deviceStatus);
        when(mGetCase.execute()).thenReturn(holder);
        when(mPreferMusicApp.getSelectedAppList()).thenReturn(mMusicAppList);
        mPresenter.onTakeView();
        mPresenter.onMediaSourceTypeChangeAction(event);
        verify(mView).dismissDialog();
    }

    @Test
    public void testOnCustomizeAction() throws Exception {
        mPresenter.onCustomizeAction();
    }

    @Test
    public void testOnBackAction() throws Exception {
        mPresenter.onBackAction();
        verify(mView).dismissDialog();
    }
}