package jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.IlluminationColor;
import jp.pioneer.carsync.domain.model.IlluminationColorMap;
import jp.pioneer.carsync.domain.model.IlluminationSetting;
import jp.pioneer.carsync.domain.model.IlluminationTarget;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * COLOR CUSTOM設定情報通知パケットハンドラ.
 */
public class CustomColorSettingInfoNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 5;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public CustomColorSettingInfoNotificationPacketHandler(@NonNull CarRemoteSession session) {
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

            IlluminationSetting illuminationSetting = mStatusHolder.getIlluminationSetting();
            IlluminationColorMap map;
            // D1:場所
            int target = ubyteToInt(data[1]);
            if (target == IlluminationTarget.KEY.code) {
                map = illuminationSetting.keyColorSpec;
                illuminationSetting.keyColor = IlluminationColor.CUSTOM;
            } else if (target == IlluminationTarget.DISP.code) {
                map = illuminationSetting.dispColorSpec;
                illuminationSetting.dispColor = IlluminationColor.CUSTOM;
            } else {
                throw new AssertionError("can't happen.");
            }
            // D2:RED:
            int red = ubyteToInt(data[2]);
            // D3:GREEN
            int green = ubyteToInt(data[3]);
            // D4:BLUE
            int blue = ubyteToInt(data[4]);

            map.get(IlluminationColor.CUSTOM).setValue(red, green, blue);
            illuminationSetting.updateVersion();
            getSession().publishStatusUpdateEvent(packet.getPacketIdType());
            return null;
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
