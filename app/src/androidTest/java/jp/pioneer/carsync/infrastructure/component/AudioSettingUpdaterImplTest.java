package jp.pioneer.carsync.infrastructure.component;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.model.BeatBlasterSetting;
import jp.pioneer.carsync.domain.model.CustomEqType;
import jp.pioneer.carsync.domain.model.AudioSettingEqualizerType;
import jp.pioneer.carsync.domain.model.ListeningPositionSetting;
import jp.pioneer.carsync.domain.model.LoadSettingsType;
import jp.pioneer.carsync.domain.model.LoudnessSetting;
import jp.pioneer.carsync.domain.model.MixedSpeakerType;
import jp.pioneer.carsync.domain.model.SpeakerType;
import jp.pioneer.carsync.domain.model.StandardCutoffSetting;
import jp.pioneer.carsync.domain.model.StandardSlopeSetting;
import jp.pioneer.carsync.domain.model.SubwooferPhaseSetting;
import jp.pioneer.carsync.domain.model.SubwooferSetting;
import jp.pioneer.carsync.domain.model.TimeAlignmentSettingMode;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/06/28.
 */
public class AudioSettingUpdaterImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks AudioSettingUpdaterImpl mAudioSettingUpdater;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    @Mock OutgoingPacket mOutgoingPacket;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void setBeatBlaster() throws Exception {
        // setup
        when(mPacketBuilder.createBeatBlasterSettingNotification(eq(BeatBlasterSetting.HIGH))).thenReturn(mOutgoingPacket);

        // exercise
        mAudioSettingUpdater.setBeatBlaster(BeatBlasterSetting.HIGH);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setBeatBlaster_ArgNull() throws Exception {
        // exercise
        mAudioSettingUpdater.setBeatBlaster(null);
    }

    @Test
    public void setLoudness() throws Exception {
        // setup
        when(mPacketBuilder.createLoudnessSettingNotification(eq(LoudnessSetting.HIGH))).thenReturn(mOutgoingPacket);

        // exercise
        mAudioSettingUpdater.setLoudness(LoudnessSetting.HIGH);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setLoudness_ArgNull() throws Exception {
        // exercise
        mAudioSettingUpdater.setLoudness(null);
    }

    @Test
    public void setSourceLevelAdjuster() throws Exception {
        // setup
        when(mPacketBuilder.createSlaSettingNotification(eq(1))).thenReturn(mOutgoingPacket);

        // exercise
        mAudioSettingUpdater.setSourceLevelAdjuster(1);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setFaderBalance() throws Exception {
        // setup
        when(mPacketBuilder.createFaderBalanceSettingNotification(eq(1),eq(10))).thenReturn(mOutgoingPacket);

        // exercise
        mAudioSettingUpdater.setFaderBalance(1,10);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void setListeningPosition() throws Exception {
        // setup
        when(mPacketBuilder.createListeningPositionSettingNotification(eq(ListeningPositionSetting.ALL))).thenReturn(mOutgoingPacket);

        // exercise
        mAudioSettingUpdater.setListeningPosition(ListeningPositionSetting.ALL);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setListeningPosition_ArgNull() throws Exception {
        // exercise
        mAudioSettingUpdater.setListeningPosition(null);
    }

    @Test
    public void setTimeAlignmentMode() throws Exception {
        // setup
        when(mPacketBuilder.createTimeAlignmentPresetSettingNotification(eq(TimeAlignmentSettingMode.CUSTOM))).thenReturn(mOutgoingPacket);

        // exercise
        mAudioSettingUpdater.setTimeAlignmentMode(TimeAlignmentSettingMode.CUSTOM);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setTimeAlignmentMode_ArgNull() throws Exception {
        // exercise
        mAudioSettingUpdater.setTimeAlignmentMode(null);
    }

    @Test
    public void setTimeAlignment() throws Exception {
        // setup
        when(mPacketBuilder.createTimeAlignmentSettingNotification(eq(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT), eq(1))).thenReturn(mOutgoingPacket);

        // exercise
        mAudioSettingUpdater.setTimeAlignment(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT,1);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setTimeAlignment_ArgNull() throws Exception {
        // exercise
        mAudioSettingUpdater.setTimeAlignment(null,1);
    }

    @Test
    public void setSpeakerLevel() throws Exception {
        // setup
        when(mPacketBuilder.createSpeakerLevelSettingNotification(eq(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT), eq(1))).thenReturn(mOutgoingPacket);

        // exercise
        mAudioSettingUpdater.setSpeakerLevel(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT,1);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setSpeakerLevel_ArgNull() throws Exception {
        // exercise
        mAudioSettingUpdater.setSpeakerLevel(null,1);
    }

    @Test
    public void setCrossoverHpfLpf() throws Exception {
        // setup
        when(mPacketBuilder.createCrossoverHpfLpfSettingNotification(eq(SpeakerType.FRONT),eq(true))).thenReturn(mOutgoingPacket);

        // exercise
        mAudioSettingUpdater.setCrossoverHpfLpf(SpeakerType.FRONT,true);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setCrossoverHpfLpf_ArgNull() throws Exception {
        // exercise
        mAudioSettingUpdater.setCrossoverHpfLpf(null,true);
    }

    @Test
    public void setCrossoverCutOff() throws Exception {
        // setup
        when(mPacketBuilder.createCrossoverCutoffSettingNotification(eq(SpeakerType.FRONT),eq(StandardCutoffSetting._50HZ))).thenReturn(mOutgoingPacket);

        // exercise
        mAudioSettingUpdater.setCrossoverCutOff(SpeakerType.FRONT,StandardCutoffSetting._50HZ);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setCrossoverCutOff_ArgTypeNull() throws Exception {
        // exercise
        mAudioSettingUpdater.setCrossoverCutOff(null,StandardCutoffSetting._50HZ);
    }

    @Test(expected = NullPointerException.class)
    public void setCrossoverCutOff_ArgSettingNull() throws Exception {
        // exercise
        mAudioSettingUpdater.setCrossoverCutOff(SpeakerType.FRONT,null);
    }

    @Test
    public void setCrossoverSlope() throws Exception {
        // setup
        when(mPacketBuilder.createCrossoverSlopeSettingNotification(eq(SpeakerType.FRONT),eq(StandardSlopeSetting._6DB))).thenReturn(mOutgoingPacket);

        // exercise
        mAudioSettingUpdater.setCrossoverSlope(SpeakerType.FRONT,StandardSlopeSetting._6DB);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setCrossoverSlope_ArgTypeNull() throws Exception {
        // exercise
        mAudioSettingUpdater.setCrossoverSlope(null,StandardSlopeSetting._6DB);
    }

    @Test(expected = NullPointerException.class)
    public void setCrossoverSlope_ArgSettingNull() throws Exception {
        // exercise
        mAudioSettingUpdater.setCrossoverSlope(SpeakerType.FRONT,null);
    }

    @Test
    public void setSubWoofer() throws Exception {
        // setup
        when(mPacketBuilder.createSubwooferSettingNotification(eq(SubwooferSetting.ON))).thenReturn(mOutgoingPacket);

        // exercise
        mAudioSettingUpdater.setSubWoofer(SubwooferSetting.ON);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setSubWoofer_ArgNull() throws Exception {
        // exercise
        mAudioSettingUpdater.setSubWoofer(null);
    }

    @Test
    public void setSubWooferPhase() throws Exception {
        // setup
        when(mPacketBuilder.createSubwooferPhaseSettingNotification(eq(SubwooferPhaseSetting.NORMAL))).thenReturn(mOutgoingPacket);

        // exercise
        mAudioSettingUpdater.setSubWooferPhase(SubwooferPhaseSetting.NORMAL);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setSubWooferPhase_ArgNull() throws Exception {
        // exercise
        mAudioSettingUpdater.setSubWooferPhase(null);
    }

    @Test
    public void setCustomBand() throws Exception {
        // setup
        int[] bands = new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13};
        when(mPacketBuilder.createEqualizerCustomAdjustNotification(eq(CustomEqType.CUSTOM1),eq(bands))).thenReturn(mOutgoingPacket);

        // exercise
        mAudioSettingUpdater.setCustomBand(CustomEqType.CUSTOM1,bands);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setCustomBand_ArgTypeNull() throws Exception {
        // exercise
        mAudioSettingUpdater.setCustomBand(null,new int[13]);
    }

    @Test(expected = NullPointerException.class)
    public void setCustomBand_ArgBandsNull() throws Exception {
        // exercise
        mAudioSettingUpdater.setCustomBand(CustomEqType.CUSTOM1,null);
    }

    @Test
    public void setEqualizer() throws Exception {
        // setup
        when(mPacketBuilder.createEqualizerSettingNotification(eq(AudioSettingEqualizerType.FLAT))).thenReturn(mOutgoingPacket);

        // exercise
        mAudioSettingUpdater.setEqualizer(AudioSettingEqualizerType.FLAT);

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test(expected = NullPointerException.class)
    public void setEqualizer_ArgNull() throws Exception {
        // exercise
        mAudioSettingUpdater.setEqualizer(null);
    }

    @Test
    public void saveAudioSetting() throws Exception {
        // setup
        when(mPacketBuilder.createSaveSettingNotification()).thenReturn(mOutgoingPacket);

        // exercise
        mAudioSettingUpdater.saveAudioSetting();

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void loadAudioSetting() throws Exception {
        // setup
        when(mPacketBuilder.createLoadSettingNotification(eq(LoadSettingsType.SOUND))).thenReturn(mOutgoingPacket);

        // exercise
        mAudioSettingUpdater.loadAudioSetting();

        // verify
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }
}