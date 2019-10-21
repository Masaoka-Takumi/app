package jp.pioneer.carsync.presentation.presenter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.LiveSimulationSetting;
import jp.pioneer.carsync.domain.model.SoundEffectSettingType;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.LiveSimulationSettingView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_906320 on 2017/07/25.
 */
public class LiveSimulationSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks LiveSimulationSettingPresenter mPresenter = new LiveSimulationSettingPresenter();
    @Mock LiveSimulationSettingView mView;
    @Mock AppSharedPreference mPreference;
    @Mock PreferSoundFx mFxCase;
    @Mock GetStatusHolder mGetStatusHolder;

//    private static final List<CoverFlowItem> LIVE_SIMULATION_ITEMS = new ArrayList<CoverFlowItem>() {{
//        add(new LiveSimulationItem(SoundFieldSettingType.DOME, R.drawable.p0650_ls_dome));
//        add(new LiveSimulationItem(SoundFieldSettingType.HALL, R.drawable.p0651_ls_hall));
//        add(new LiveSimulationItem(SoundFieldSettingType.LIVE, R.drawable.p0652_ls_live));
//        add(new LiveSimulationItem(SoundFieldSettingType.LIVE_RECORDING, R.drawable.p0653_ls_liverecording));
//        add(new LiveSimulationItem(SoundFieldSettingType.LIVE_STEREO_MIC, R.drawable.p0654_ls_livestereomic));
//        add(new LiveSimulationItem(SoundFieldSettingType.STADIUM, R.drawable.p0655_ls_stadium));
//        add(new LiveSimulationItem(SoundFieldSettingType.OFF, R.drawable.p0656_ls_off));
//    }};
//    private static final List<VisualEffectItem> EFFECT_SETTING_TYPES = new ArrayList<VisualEffectItem>() {{
//        add(new VisualEffectItem(ApplauseEffectSettingType.OFF, R.drawable.p0143_veiconbtn_1nrm));
//        add(new VisualEffectItem(ApplauseEffectSettingType.A, R.drawable.p0145_veiconbtn_1nrm));
//        add(new VisualEffectItem(ApplauseEffectSettingType.B, R.drawable.p0146_veiconbtn_1nrm));
//        add(new VisualEffectItem(ApplauseEffectSettingType.C, R.drawable.p0147_veiconbtn_1nrm));
//        add(new VisualEffectItem(ApplauseEffectSettingType.D, R.drawable.p0148_veiconbtn_1nrm));
//        add(new VisualEffectItem(ApplauseEffectSettingType.E, R.drawable.p0149_veiconbtn_1nrm));
//    }};

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mPreference.getUiColor()).thenReturn(AQUA);
    }

    @Test
    public void testOnTakeView() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        SoundFxSetting soundFxSetting = mock(SoundFxSetting.class);
        LiveSimulationSetting setting = new LiveSimulationSetting();
        setting.soundFieldControlSettingType = SoundFieldControlSettingType.OFF;
        setting.soundEffectSettingType = SoundEffectSettingType.OFF;
        soundFxSetting.liveSimulationSetting = setting;

        when(mGetStatusHolder.execute()).thenReturn(holder);
        when(holder.getSoundFxSetting()).thenReturn(soundFxSetting);

        mPresenter.onTakeView();

        verify(mView).setLiveSimulationAdapter(any(ArrayList.class), eq(0));
        verify(mView).setVisualEffectAdapter(any(List.class), eq(0));
    }

//    @Test
//    public void testOnSelectLiveSimulationSettingAction() throws Exception {
//        StatusHolder holder = mock(StatusHolder.class);
//        SoundFxSetting soundFxSetting = mock(SoundFxSetting.class);
//        LiveSimulationSetting setting = new LiveSimulationSetting();
//        setting.soundFieldControlSettingType = SoundFieldSettingType.DOME;
//        setting.soundEffectSettingType = ApplauseEffectSettingType.OFF;
//        soundFxSetting.liveSimulationSetting = setting;
//
//        when(mGetStatusHolder.execute()).thenReturn(holder);
//        when(holder.getSoundFxSetting()).thenReturn(soundFxSetting);
//
//        mPresenter.onSelectLiveSimulationAction(0);
//
//        verify(mFxCase).setLiveSimulation(setting);
//    }

    @Test
    public void testOnSelectVisualEffectAction() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        SoundFxSetting soundFxSetting = mock(SoundFxSetting.class);
        LiveSimulationSetting setting = new LiveSimulationSetting();
        setting.soundFieldControlSettingType = SoundFieldControlSettingType.DOME;
        setting.soundEffectSettingType = SoundEffectSettingType.OFF;
        soundFxSetting.liveSimulationSetting = setting;

        when(mGetStatusHolder.execute()).thenReturn(holder);
        when(holder.getSoundFxSetting()).thenReturn(soundFxSetting);

        mPresenter.onSelectVisualEffectAction(0);

        verify(mFxCase).setLiveSimulation(setting.soundFieldControlSettingType,setting.soundEffectSettingType.type);
    }
}