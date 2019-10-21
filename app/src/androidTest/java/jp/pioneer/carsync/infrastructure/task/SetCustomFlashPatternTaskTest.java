package jp.pioneer.carsync.infrastructure.task;

import android.content.Context;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.concurrent.Future;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CustomFlashPatternTransaction;
import jp.pioneer.carsync.domain.model.CustomFlashRequestType;
import jp.pioneer.carsync.domain.model.FlashPattern;
import jp.pioneer.carsync.domain.model.FlashPatternRegistrationType;
import jp.pioneer.carsync.domain.model.IlluminationSetting;
import jp.pioneer.carsync.domain.model.IlluminationSettingStatus;
import jp.pioneer.carsync.domain.model.ProtocolSpec;
import jp.pioneer.carsync.domain.model.RequestStatus;
import jp.pioneer.carsync.domain.model.SmartPhoneInterruption;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.ZoneFrameInfo;
import jp.pioneer.carsync.infrastructure.crp.CarDeviceConnection;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacketBuilder;
import jp.pioneer.carsync.infrastructure.crp.task.RequestTask;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/26.
 */
public class SetCustomFlashPatternTaskTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SetCustomFlashPatternTask mSetCustomFlashPatternTask = new SetCustomFlashPatternTask() {
        @Override
        CustomFlashPatternTransaction createTransaction() {
            return mTransaction;
        }

        @Override
        void checkInterrupted() throws InterruptedException {
            if (isInterrupted) {
                throw new InterruptedException();
            }
        }
    };
    @Mock StatusHolder mStatusHolder;
    @Mock CarDeviceConnection mCarDeviceConnection;
    @Mock OutgoingPacketBuilder mPacketBuilder;
    @Mock AppSharedPreference mPreference;
    @Mock Context mContext;
    @Mock OutgoingPacket mOutgoingPacket;

    private CustomFlashPatternTransaction mTransaction;
    private boolean isInterrupted = false;
    private ProtocolSpec mProtocolSpec = new ProtocolSpec();
    private IlluminationSetting mIlluminationSetting = new IlluminationSetting();
    private IlluminationSettingStatus mIlluminationSettingStatus = new IlluminationSettingStatus();

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        ArrayList<ZoneFrameInfo> frameInfo = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            ZoneFrameInfo info = new ZoneFrameInfo();
            info.zone2.setValue(20, 20, 20);
            info.zone3.setValue(20, 20, 20);
            info.duration = 100;
            frameInfo.add(info);
        }
        mTransaction = new CustomFlashPatternTransaction(frameInfo){
            int transactionId = 0;
            @Override
            public boolean hasNext() {
                boolean result = super.hasNext();
                id = ++transactionId;
                return result;
            }

            @Override
            public int getIndex() {
                return super.getIndex();
            }
        };
        mTransaction.id = 0;

        when(mPacketBuilder.createCustomFlashPatternSettingNotification(
                any(FlashPatternRegistrationType.class),
                anyInt(),
                anyInt(),
                anyInt(),
                any(ZoneFrameInfo[].class)
        )).thenReturn(mOutgoingPacket);

        when(mPacketBuilder.createCustomFlashCommand(CustomFlashRequestType.START))
                .thenReturn(mOutgoingPacket);

        when(mPreference.isLightingEffectEnabled()).thenReturn(true);
        when(mStatusHolder.getIlluminationSetting()).thenReturn(mIlluminationSetting);
        when(mStatusHolder.getIlluminationSettingStatus()).thenReturn(mIlluminationSettingStatus);
        when(mStatusHolder.getProtocolSpec()).thenReturn(mProtocolSpec);
    }

    @Test
    public void run_NormalPattern() throws Exception {
        // setup
        when(mCarDeviceConnection.sendRequestPacket(mOutgoingPacket, mSetCustomFlashPatternTask))
                .then(invocationOnMock -> {
                    RequestTask.Callback<Integer> resultCallback = (RequestTask.Callback<Integer>) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(1);
                    return mock(Future.class);
                })
                .then(invocationOnMock -> {
                    RequestTask.Callback<Integer> resultCallback = (RequestTask.Callback<Integer>) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(2);
                    return mock(Future.class);
                });
        mProtocolSpec.setCarDeviceClassId(CarDeviceClassId.SPH);
        mIlluminationSettingStatus.customFlashPatternSettingEnabled = true;

        // exercise
        mSetCustomFlashPatternTask = mSetCustomFlashPatternTask.setParamsForCustomFlashPattern(FlashPattern.BGP1);
        mSetCustomFlashPatternTask.run();

        // verify
        ArgumentCaptor<ZoneFrameInfo[]> captor = ArgumentCaptor.forClass(ZoneFrameInfo[].class);
        verify(mPacketBuilder).createCustomFlashPatternSettingNotification(
                eq(FlashPatternRegistrationType.NORMAL), eq(1), eq(200), eq(1), captor.capture());
        assertThat(captor.getValue().length, is(110));
        verify(mPacketBuilder).createCustomFlashPatternSettingNotification(
                eq(FlashPatternRegistrationType.NORMAL), eq(2), eq(200), eq(111), captor.capture());
        assertThat(captor.getValue().length, is(90));
        assertThat(mIlluminationSetting.customFlashPatternRequestStatus, is(RequestStatus.SENT));
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void run_NormalPattern_LightingEffectDisabled() throws Exception {
        // setup
        when(mCarDeviceConnection.sendRequestPacket(mOutgoingPacket, mSetCustomFlashPatternTask))
                .then(invocationOnMock -> {
                    RequestTask.Callback<Integer> resultCallback = (RequestTask.Callback<Integer>) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(1);
                    return mock(Future.class);
                })
                .then(invocationOnMock -> {
                    RequestTask.Callback<Integer> resultCallback = (RequestTask.Callback<Integer>) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(2);
                    return mock(Future.class);
                });
        mProtocolSpec.setCarDeviceClassId(CarDeviceClassId.SPH);
        mIlluminationSettingStatus.customFlashPatternSettingEnabled = true;
        when(mPreference.isLightingEffectEnabled()).thenReturn(false);

        // exercise
        mSetCustomFlashPatternTask = mSetCustomFlashPatternTask.setParamsForCustomFlashPattern(FlashPattern.BGP1);
        mSetCustomFlashPatternTask.run();

        // verify
        ArgumentCaptor<ZoneFrameInfo[]> captor = ArgumentCaptor.forClass(ZoneFrameInfo[].class);
        verify(mPacketBuilder).createCustomFlashPatternSettingNotification(
                eq(FlashPatternRegistrationType.NORMAL), eq(1), eq(200), eq(1), captor.capture());
        assertThat(captor.getValue().length, is(110));
        verify(mPacketBuilder).createCustomFlashPatternSettingNotification(
                eq(FlashPatternRegistrationType.NORMAL), eq(2), eq(200), eq(111), captor.capture());
        assertThat(captor.getValue().length, is(90));
        assertThat(mIlluminationSetting.customFlashPatternRequestStatus, is(RequestStatus.SENT));
        verify(mCarDeviceConnection, never()).sendPacket(any(OutgoingPacket.class));
    }

    @Test
    public void run_InterruptedPattern() throws Exception {
        // setup
        when(mCarDeviceConnection.sendRequestPacket(mOutgoingPacket, mSetCustomFlashPatternTask))
                .then(invocationOnMock -> {
                    RequestTask.Callback<Integer> resultCallback = (RequestTask.Callback<Integer>) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(1);
                    return mock(Future.class);
                })
                .then(invocationOnMock -> {
                    RequestTask.Callback<Integer> resultCallback = (RequestTask.Callback<Integer>) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(2);
                    return mock(Future.class);
                });
        mProtocolSpec.setCarDeviceClassId(CarDeviceClassId.SPH);
        mIlluminationSettingStatus.customFlashPatternSettingEnabled = true;

        // exercise
        mSetCustomFlashPatternTask = mSetCustomFlashPatternTask.setParamsForInterruption(SmartPhoneInterruption.LOW);
        mSetCustomFlashPatternTask.run();

        // verify
        ArgumentCaptor<ZoneFrameInfo[]> captor = ArgumentCaptor.forClass(ZoneFrameInfo[].class);
        verify(mPacketBuilder).createCustomFlashPatternSettingNotification(
                eq(FlashPatternRegistrationType.INTERRUPT), eq(1), eq(200), eq(1), captor.capture());
        assertThat(captor.getValue().length, is(110));
        verify(mPacketBuilder).createCustomFlashPatternSettingNotification(
                eq(FlashPatternRegistrationType.INTERRUPT), eq(2), eq(200), eq(111), captor.capture());
        assertThat(captor.getValue().length, is(90));
        assertThat(mIlluminationSetting.customFlashPatternRequestStatus, is(RequestStatus.NOT_SENT));
    }

    @Test
    public void run_Interrupted() throws Exception {
        // setup
        isInterrupted = true;

        // exercise
        mSetCustomFlashPatternTask = mSetCustomFlashPatternTask.setParamsForCustomFlashPattern(FlashPattern.BGP1);
        mSetCustomFlashPatternTask.run();

        // verify
        verify(mPacketBuilder, never()).createCustomFlashPatternSettingNotification(
                any(FlashPatternRegistrationType.class),
                anyInt(),
                anyInt(),
                anyInt(),
                any(ZoneFrameInfo[].class));
        assertThat(mIlluminationSetting.customFlashPatternRequestStatus, is(RequestStatus.NOT_SENT));
    }

    @Test
    public void run_NotSphCarDevice() throws Exception {
        // setup
        when(mCarDeviceConnection.sendRequestPacket(mOutgoingPacket, mSetCustomFlashPatternTask))
                .then(invocationOnMock -> {
                    RequestTask.Callback<Integer> resultCallback = (RequestTask.Callback<Integer>) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(1);
                    return mock(Future.class);
                })
                .then(invocationOnMock -> {
                    RequestTask.Callback<Integer> resultCallback = (RequestTask.Callback<Integer>) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(2);
                    return mock(Future.class);
                });
        mProtocolSpec.setCarDeviceClassId(CarDeviceClassId.DEH);
        mIlluminationSettingStatus.customFlashPatternSettingEnabled = true;

        // exercise
        mSetCustomFlashPatternTask = mSetCustomFlashPatternTask.setParamsForCustomFlashPattern(FlashPattern.BGP1);
        mSetCustomFlashPatternTask.run();

        // verify
        verify(mPacketBuilder, never()).createCustomFlashPatternSettingNotification(
                any(FlashPatternRegistrationType.class),
                anyInt(),
                anyInt(),
                anyInt(),
                any(ZoneFrameInfo[].class));
        assertThat(mIlluminationSetting.customFlashPatternRequestStatus, is(RequestStatus.NOT_SENT));
    }

    @Test
    public void run_SettingDisabled() throws Exception {
        // setup
        when(mCarDeviceConnection.sendRequestPacket(mOutgoingPacket, mSetCustomFlashPatternTask))
                .then(invocationOnMock -> {
                    RequestTask.Callback<Integer> resultCallback = (RequestTask.Callback<Integer>) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(1);
                    return mock(Future.class);
                })
                .then(invocationOnMock -> {
                    RequestTask.Callback<Integer> resultCallback = (RequestTask.Callback<Integer>) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(2);
                    return mock(Future.class);
                });
        mProtocolSpec.setCarDeviceClassId(CarDeviceClassId.SPH);

        // exercise
        mSetCustomFlashPatternTask = mSetCustomFlashPatternTask.setParamsForCustomFlashPattern(FlashPattern.BGP1);
        mSetCustomFlashPatternTask.run();

        // verify
        verify(mPacketBuilder, never()).createCustomFlashPatternSettingNotification(
                any(FlashPatternRegistrationType.class),
                anyInt(),
                anyInt(),
                anyInt(),
                any(ZoneFrameInfo[].class));
        assertThat(mIlluminationSetting.customFlashPatternRequestStatus, is(RequestStatus.NOT_SENT));
    }

    @Test
    public void run_SetFlashPatternResultNG() throws Exception {
        // setup
        when(mCarDeviceConnection.sendRequestPacket(mOutgoingPacket, mSetCustomFlashPatternTask))
                .then(invocationOnMock -> {
                    RequestTask.Callback<Integer> resultCallback = (RequestTask.Callback<Integer>) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(1);
                    return mock(Future.class);
                })
                .then(invocationOnMock -> {
                    RequestTask.Callback<Integer> resultCallback = (RequestTask.Callback<Integer>) invocationOnMock.getArguments()[1];
                    resultCallback.onResult(-1);
                    return mock(Future.class);
                });
        mProtocolSpec.setCarDeviceClassId(CarDeviceClassId.SPH);
        mIlluminationSettingStatus.customFlashPatternSettingEnabled = true;

        // exercise
        mSetCustomFlashPatternTask = mSetCustomFlashPatternTask.setParamsForCustomFlashPattern(FlashPattern.BGP1);
        mSetCustomFlashPatternTask.run();

        // verify
        ArgumentCaptor<ZoneFrameInfo[]> captor = ArgumentCaptor.forClass(ZoneFrameInfo[].class);
        verify(mPacketBuilder).createCustomFlashPatternSettingNotification(
                eq(FlashPatternRegistrationType.NORMAL), eq(1), eq(200), eq(1), captor.capture());
        assertThat(captor.getValue().length, is(110));
        verify(mPacketBuilder).createCustomFlashPatternSettingNotification(
                eq(FlashPatternRegistrationType.NORMAL), eq(2), eq(200), eq(111), captor.capture());
        assertThat(captor.getValue().length, is(90));
        assertThat(mIlluminationSetting.customFlashPatternRequestStatus, is(RequestStatus.SENT));
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }

    @Test
    public void run_SetFlashPatternError() throws Exception {
        // setup
        when(mCarDeviceConnection.sendRequestPacket(mOutgoingPacket, mSetCustomFlashPatternTask))
                .then(invocationOnMock -> {
                    RequestTask.Callback<Integer> resultCallback = (RequestTask.Callback<Integer>) invocationOnMock.getArguments()[1];
                    resultCallback.onError();
                    return mock(Future.class);
                });
        mProtocolSpec.setCarDeviceClassId(CarDeviceClassId.SPH);
        mIlluminationSettingStatus.customFlashPatternSettingEnabled = true;

        // exercise
        mSetCustomFlashPatternTask = mSetCustomFlashPatternTask.setParamsForCustomFlashPattern(FlashPattern.BGP1);
        mSetCustomFlashPatternTask.run();

        // verify
        ArgumentCaptor<ZoneFrameInfo[]> captor = ArgumentCaptor.forClass(ZoneFrameInfo[].class);
        verify(mPacketBuilder).createCustomFlashPatternSettingNotification(
                eq(FlashPatternRegistrationType.NORMAL), eq(1), eq(200), eq(1), captor.capture());
        assertThat(captor.getValue().length, is(110));
        verify(mPacketBuilder, never()).createCustomFlashPatternSettingNotification(
                eq(FlashPatternRegistrationType.NORMAL), eq(2), eq(200), eq(111), any(ZoneFrameInfo[].class));
        assertThat(mIlluminationSetting.customFlashPatternRequestStatus, is(RequestStatus.SENT));
        verify(mCarDeviceConnection).sendPacket(mOutgoingPacket);
    }
}