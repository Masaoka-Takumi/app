package jp.pioneer.carsync.infrastructure.crp.handler.setting.audio;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.CustomEqType;
import jp.pioneer.carsync.domain.model.CustomEqualizerSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * カスタムイコライザー設定情報通知パケットハンドラ.
 */
public class CustomEqualizerSettingInfoNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 15;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public CustomEqualizerSettingInfoNotificationPacketHandler(@NonNull CarRemoteSession session) {
        super(session);
        mStatusHolder = checkNotNull(session).getStatusHolder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected OutgoingPacket doHandle(@NonNull IncomingPacket packet) throws Exception {
        try {
            byte[] data = packet.getData();
            checkPacketDataLength(data, DATA_LENGTH);

            AudioSetting audioSetting = mStatusHolder.getAudioSetting();
            CustomEqualizerSetting setting = audioSetting.customEqualizerSetting;
            // D1:カスタムEQ種別
            setting.customEqType = CustomEqType.valueOf(data[1]);
            // D2:最小ステップ値
            setting.minimumStep = data[2];
            // D3:最大ステップ値
            setting.maximumStep = data[3];
            // D4:BAND1(50Hz)
            setting.band1 = data[4];
            // D5:BAND2(80Hz)
            setting.band2 = data[5];
            // D6:BAND3(125Hz)
            setting.band3 = data[6];
            // D7:BAND4(200Hz)
            setting.band4 = data[7];
            // D8:BAND5(315Hz)
            setting.band5 = data[8];
            // D9:BAND6(500Hz)
            setting.band6 = data[9];
            // D10:BAND7(800Hz)
            setting.band7 = data[10];
            // D11:BAND8(1.25KHz)
            setting.band8 = data[11];
            // D12:BAND9(2KHz)
            setting.band9 = data[12];
            // D13:BAND10(3.15KHz)
            setting.band10 = data[13];
            // D14:BAND11(5KHz)
            setting.band11 = data[14];
            // D15:BAND12(8KHz)
            setting.band12 = data[15];
            // D16:BAND13(12.5KHz)
            setting.band13 = data[16];

            setting.updateVersion();
            audioSetting.updateVersion();
            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return getPacketBuilder().createCustomEqualizerSettingRequest(setting.customEqType);
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}