package jp.pioneer.carsync.presentation.presenter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferIllumination;
import jp.pioneer.carsync.domain.model.CarDeviceDestinationInfo;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.IlluminationColor;
import jp.pioneer.carsync.domain.model.IlluminationColorMap;
import jp.pioneer.carsync.domain.model.IlluminationColorSpec;
import jp.pioneer.carsync.domain.model.IlluminationSetting;
import jp.pioneer.carsync.domain.model.IlluminationSettingStatus;
import jp.pioneer.carsync.domain.model.IlluminationTarget;
import jp.pioneer.carsync.domain.model.ProtocolSpec;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.ThemeType;
import jp.pioneer.carsync.presentation.model.IlluminationColorModel;
import jp.pioneer.carsync.presentation.model.ThemeSelectItem;
import jp.pioneer.carsync.presentation.model.UiColor;
import jp.pioneer.carsync.presentation.view.ThemeSetView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * テーマセット設定画面presenterのテスト
 */
public class ThemeSetPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ThemeSetPresenter mPresenter = new ThemeSetPresenter();
    @Mock ThemeSetView mView;
    @Mock AppSharedPreference mPreference;
    @Mock GetStatusHolder mGetStatusHolder;
    @Mock PreferIllumination mPreferIllumination;
    private static final ArrayList<ThemeSelectItem> BACKGROUNDS = new ArrayList<ThemeSelectItem>() {{
        for (ThemeType type : ThemeType.values()) {
            add(new ThemeSelectItem(type, type.getResourceId(), type.getThumbnail(),type.isVideo(), type.code));
        }
    }};
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void testLifecycleCommon() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        IlluminationSettingStatus mockSettingStatus = mock(IlluminationSettingStatus.class);
        ArgumentCaptor<IlluminationColorModel> argument = ArgumentCaptor.forClass(IlluminationColorModel.class);
        ThemeType currentType = ThemeType.VIDEO_PATTERN1;
        mockSettingStatus.commonColorCustomSettingEnabled = true;
        mockSettingStatus.colorCustomDispSettingEnabled = false;
        mockSettingStatus.colorCustomKeySettingEnabled = false;

        IlluminationColorModel common = currentType.getIlluminationColor();
        IlluminationColorModel disp = currentType.getIlluminationDisplayColor();
        IlluminationColorModel key = currentType.getIlluminationKeyColor();
        when(mPreference.getUiColor()).thenReturn(UiColor.RED);
        IlluminationSetting mockSetting = mock(IlluminationSetting.class);
        mockSetting.commonColor = IlluminationColor.WHITE;
        mockSetting.commonColorSpec = mock(IlluminationColorMap.class);
        IlluminationColorSpec mockSpec = mock(IlluminationColorSpec.class);
        mockSpec.red = 60;
        mockSpec.green = 60;
        mockSpec.blue = 60;
        ProtocolSpec spec = mock(ProtocolSpec.class);

        CarDeviceSpec mockCarDeviceSpec = mock(CarDeviceSpec.class);
        mockCarDeviceSpec.carDeviceDestinationInfo = CarDeviceDestinationInfo.JP;
        when(mockHolder.getCarDeviceSpec()).thenReturn(mockCarDeviceSpec);

        when(spec.isSphCarDevice()).thenReturn(true);
        when(mPreference.getThemeType()).thenReturn(currentType);
        when(mockHolder.getIlluminationSettingStatus()).thenReturn(mockSettingStatus);
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getIlluminationSetting()).thenReturn(mockSetting);
        when(mockSetting.commonColorSpec.get(any(IlluminationColor.class))).thenReturn(mockSpec);
        when(mockSpec.isValid()).thenReturn(true);
        when(mockHolder.getProtocolSpec()).thenReturn(spec);
        mPresenter.takeView(mView);

        verify(mView).setAdapter(any(ArrayList.class));
        verify(mView).setCurrentItem(anyInt());
        verify(mView).setDispColorSettingEnabled(true);
        verify(mView).setKeyColorSettingEnabled(true);
        verify(mView).setDispColor(argument.capture());
        assertThat(argument.getValue().red.getValue(), is(60));
        assertThat(argument.getValue().green.getValue(), is(60));
        assertThat(argument.getValue().blue.getValue(), is(60));
        verify(mView).setKeyColor(argument.capture());
        assertThat(argument.getValue().red.getValue(), is(60));
        assertThat(argument.getValue().green.getValue(), is(60));
        assertThat(argument.getValue().blue.getValue(), is(60));
        verify(mView).setUIColor(UiColor.RED.getResource());
        verify(mView).setCustom(true);
    }

    @Test
    public void testLifecycleNotCommon() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        ThemeType currentType = ThemeType.VIDEO_PATTERN1;
        IlluminationSettingStatus mockSettingStatus = mock(IlluminationSettingStatus.class);
        ArgumentCaptor<IlluminationColorModel> argument = ArgumentCaptor.forClass(IlluminationColorModel.class);
        mockSettingStatus.commonColorCustomSettingEnabled = false;
        mockSettingStatus.colorCustomDispSettingEnabled = true;
        mockSettingStatus.colorCustomKeySettingEnabled = true;
        IlluminationColorModel common = currentType.getIlluminationColor();
        IlluminationColorModel disp = currentType.getIlluminationDisplayColor();
        IlluminationColorModel key = currentType.getIlluminationKeyColor();

        when(mPreference.getUiColor()).thenReturn(UiColor.RED);
        IlluminationSetting mockSetting = mock(IlluminationSetting.class);
        mockSetting.dispColor = IlluminationColor.WHITE;
        mockSetting.dispColorSpec = mock(IlluminationColorMap.class);
        mockSetting.keyColor = IlluminationColor.WHITE;
        mockSetting.keyColorSpec = mock(IlluminationColorMap.class);
        IlluminationColorSpec mockSpec = mock(IlluminationColorSpec.class);
        mockSpec.red = 60;
        mockSpec.green = 60;
        mockSpec.blue = 60;
        ProtocolSpec spec = mock(ProtocolSpec.class);
        when(spec.isSphCarDevice()).thenReturn(false);
        when(mPreference.getThemeType()).thenReturn(currentType);

        CarDeviceSpec mockCarDeviceSpec = mock(CarDeviceSpec.class);
        mockCarDeviceSpec.carDeviceDestinationInfo = CarDeviceDestinationInfo.JP;
        when(mockHolder.getCarDeviceSpec()).thenReturn(mockCarDeviceSpec);

        when(mockHolder.getIlluminationSettingStatus()).thenReturn(mockSettingStatus);
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getIlluminationSetting()).thenReturn(mockSetting);
        when(mockSetting.dispColorSpec.get(any(IlluminationColor.class))).thenReturn(mockSpec);
        when(mockSetting.keyColorSpec.get(any(IlluminationColor.class))).thenReturn(mockSpec);

        when(mockSpec.isValid()).thenReturn(true);
        when(mockHolder.getProtocolSpec()).thenReturn(spec);
        mPresenter.takeView(mView);

        verify(mView).setAdapter(any(ArrayList.class));
        verify(mView).setCurrentItem(anyInt());
        verify(mView).setDispColorSettingEnabled(true);
        verify(mView).setKeyColorSettingEnabled(true);
        verify(mView).setDispColor(argument.capture());
        assertThat(argument.getValue().red.getValue(), is(60));
        assertThat(argument.getValue().green.getValue(), is(60));
        assertThat(argument.getValue().blue.getValue(), is(60));
        verify(mView).setKeyColor(argument.capture());
        assertThat(argument.getValue().red.getValue(), is(60));
        assertThat(argument.getValue().green.getValue(), is(60));
        assertThat(argument.getValue().blue.getValue(), is(60));
        verify(mView).setUIColor(UiColor.RED.getResource());
        verify(mView).setCustom(true);
    }

    @Test
    public void testOnSelectThemeActionWithVideo() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        IlluminationSettingStatus mockSettingStatus = mock(IlluminationSettingStatus.class);

        ArgumentCaptor<IlluminationColorModel> argument = ArgumentCaptor.forClass(IlluminationColorModel.class);
        ThemeSelectItem item = (ThemeSelectItem) BACKGROUNDS.get(0);
        ThemeType currentType = item.themeType;
        mockSettingStatus.commonColorCustomSettingEnabled = false;
        mockSettingStatus.colorCustomDispSettingEnabled = true;
        mockSettingStatus.colorCustomKeySettingEnabled = true;
        IlluminationColorModel common = currentType.getIlluminationColor();
        IlluminationColorModel disp = currentType.getIlluminationDisplayColor();
        IlluminationColorModel key = currentType.getIlluminationKeyColor();

        when(mPreference.getUiColor()).thenReturn(UiColor.AQUA);
        IlluminationSetting mockSetting = mock(IlluminationSetting.class);
        mockSetting.commonColor = IlluminationColor.WHITE;
        mockSetting.commonColorSpec = mock(IlluminationColorMap.class);
        IlluminationColorSpec mockSpec = mock(IlluminationColorSpec.class);
        mockSpec.red = 60;
        mockSpec.green = 60;
        mockSpec.blue = 60;

        ProtocolSpec spec = mock(ProtocolSpec.class);

        CarDeviceSpec mockCarDeviceSpec = mock(CarDeviceSpec.class);
        mockCarDeviceSpec.carDeviceDestinationInfo = CarDeviceDestinationInfo.JP;
        when(mockHolder.getCarDeviceSpec()).thenReturn(mockCarDeviceSpec);
        when(mPreference.getThemeType()).thenReturn(currentType);

        when(spec.isSphCarDevice()).thenReturn(true);
        when(mockHolder.getProtocolSpec()).thenReturn(spec);
        when(mockHolder.getIlluminationSettingStatus()).thenReturn(mockSettingStatus);
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getIlluminationSetting()).thenReturn(mockSetting);
        when(mockSetting.commonColorSpec.get(any(IlluminationColor.class))).thenReturn(mockSpec);
        when(mockSpec.isValid()).thenReturn(true);
        mPresenter.takeView(mView);
        mPresenter.onSelectThemeAction(0);

        verify(mPreference).setThemeType(currentType);
        verify(mPreferIllumination,never()).setCustomColor(IlluminationTarget.DISP,disp.red.getValue(),disp.green.getValue(),disp.blue.getValue());
        verify(mPreferIllumination,never()).setCustomColor(IlluminationTarget.KEY,key.red.getValue(),key.green.getValue(),key.blue.getValue());
        verify(mPreferIllumination).setCommonCustomColor(common.red.getValue(),common.green.getValue(),common.blue.getValue());
        verify(mPreference).setBackgroundType(1);
        verify(mPreference).setBackgroundVideoId(anyInt());
        verify(mPreference).removeBackgroundPictureId();
        verify(mPreference).setUiColor(currentType.getUIColor());

        verify(mView,times(2)).setDispColorSettingEnabled(true);
        verify(mView,times(2)).setKeyColorSettingEnabled(true);
        verify(mView,times(2)).setDispColor(argument.capture());
        assertThat(argument.getValue().red.getValue(), is(41));
        assertThat(argument.getValue().green.getValue(), is(60));
        assertThat(argument.getValue().blue.getValue(), is(60));
        verify(mView,times(2)).setKeyColor(argument.capture());
        assertThat(argument.getValue().red.getValue(), is(41));
        assertThat(argument.getValue().green.getValue(), is(60));
        assertThat(argument.getValue().blue.getValue(), is(60));
        verify(mView,times(1)).setUIColor(currentType.getUIColor().getResource());
    }

    @Test
    public void testOnSelectThemeActionWithPicture() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        IlluminationSettingStatus mockSettingStatus = mock(IlluminationSettingStatus.class);
        ArgumentCaptor<IlluminationColorModel> argument = ArgumentCaptor.forClass(IlluminationColorModel.class);
        ThemeSelectItem item = (ThemeSelectItem) BACKGROUNDS.get(3);
        ThemeType currentType = item.themeType;
        mockSettingStatus.commonColorCustomSettingEnabled = false;
        mockSettingStatus.colorCustomDispSettingEnabled = true;
        mockSettingStatus.colorCustomKeySettingEnabled = true;
        IlluminationColorModel common = currentType.getIlluminationColor();
        IlluminationColorModel disp = currentType.getIlluminationDisplayColor();
        IlluminationColorModel key = currentType.getIlluminationKeyColor();

        when(mPreference.getUiColor()).thenReturn(UiColor.RED);
        IlluminationSetting mockSetting = mock(IlluminationSetting.class);
        mockSetting.dispColor = IlluminationColor.WHITE;
        mockSetting.dispColorSpec = mock(IlluminationColorMap.class);
        mockSetting.keyColor = IlluminationColor.WHITE;
        mockSetting.keyColorSpec = mock(IlluminationColorMap.class);
        IlluminationColorSpec mockSpec = mock(IlluminationColorSpec.class);
        mockSpec.red = 60;
        mockSpec.green = 60;
        mockSpec.blue = 60;

        CarDeviceSpec mockCarDeviceSpec = mock(CarDeviceSpec.class);
        mockCarDeviceSpec.carDeviceDestinationInfo = CarDeviceDestinationInfo.JP;
        when(mockHolder.getCarDeviceSpec()).thenReturn(mockCarDeviceSpec);
        when(mPreference.getThemeType()).thenReturn(currentType);

        ProtocolSpec spec = mock(ProtocolSpec.class);
        when(spec.isSphCarDevice()).thenReturn(false);
        when(mockHolder.getProtocolSpec()).thenReturn(spec);
        when(mockHolder.getIlluminationSettingStatus()).thenReturn(mockSettingStatus);
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getIlluminationSetting()).thenReturn(mockSetting);
        when(mockSetting.dispColorSpec.get(any(IlluminationColor.class))).thenReturn(mockSpec);
        when(mockSetting.keyColorSpec.get(any(IlluminationColor.class))).thenReturn(mockSpec);
        when(mockSpec.isValid()).thenReturn(true);
        mPresenter.takeView(mView);
        mPresenter.onSelectThemeAction(3);

        verify(mPreference).setThemeType(currentType);
        verify(mPreferIllumination).setCustomColor(IlluminationTarget.DISP,disp.red.getValue(),disp.green.getValue(),disp.blue.getValue());
        verify(mPreferIllumination).setCustomColor(IlluminationTarget.KEY,key.red.getValue(),key.green.getValue(),key.blue.getValue());
        verify(mPreferIllumination,never()).setCommonCustomColor(common.red.getValue(),common.green.getValue(),common.blue.getValue());
        verify(mPreference).setBackgroundType(0);
        verify(mPreference).setBackgroundPictureId(anyInt());
        verify(mPreference).removeBackgroundVideoId();
        verify(mPreference).setUiColor(currentType.getUIColor());

        verify(mView,times(2)).setDispColorSettingEnabled(true);
        verify(mView,times(2)).setKeyColorSettingEnabled(true);
        verify(mView,times(2)).setDispColor(argument.capture());
        assertThat(argument.getValue().red.getValue(), is(0));
        assertThat(argument.getValue().green.getValue(), is(43));
        assertThat(argument.getValue().blue.getValue(), is(51));
        verify(mView,times(2)).setKeyColor(argument.capture());
        assertThat(argument.getValue().red.getValue(), is(0));
        assertThat(argument.getValue().green.getValue(), is(43));
        assertThat(argument.getValue().blue.getValue(), is(51));
        verify(mView).setUIColor(currentType.getUIColor().getResource());

    }
}
