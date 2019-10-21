package jp.pioneer.carsync.infrastructure.crp.handler.settingcommand;

import android.support.annotation.NonNull;

import java.util.EnumSet;

import jp.pioneer.carsync.domain.model.CharSetType;
import jp.pioneer.carsync.domain.model.ConnectServiceType;
import jp.pioneer.carsync.domain.model.PhoneConnectRequestType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.OutgoingPacket;
import jp.pioneer.carsync.infrastructure.crp.event.CrpPhoneServiceConnectCompleteEvent;
import jp.pioneer.carsync.infrastructure.crp.handler.AbstractPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponseCode;
import jp.pioneer.carsync.infrastructure.crp.util.TextBytesUtil;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * サービスコネクトコマンド完了通知パケットハンドラ.
 */
public class PhoneServiceConnectCompleteNotificationPacketHandler extends AbstractPacketHandler {
    private static final int DATA_LENGTH = 22;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public PhoneServiceConnectCompleteNotificationPacketHandler(@NonNull CarRemoteSession session) {
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

            // D1:結果
            ResponseCode result = ResponseCode.valueOf(data[1]);
            // D2-D19:BDアドレス
            String bdAddress = TextBytesUtil.extractText(data, 2, CharSetType.UTF8);
            // D20:接続種別
            PhoneConnectRequestType requestType = PhoneConnectRequestType.valueOf(data[20]);
            // D21:サービス種別
            EnumSet<ConnectServiceType> serviceTypes;
            int serviceType = ubyteToInt(data[21]);
            if (serviceType == ConnectServiceType.ALL_CODE) {
                serviceTypes = EnumSet.allOf(ConnectServiceType.class);
            } else if (serviceType == ConnectServiceType.PHONE.code) {
                serviceTypes = EnumSet.of(ConnectServiceType.PHONE);
            } else if (serviceType == ConnectServiceType.AUDIO.code) {
                serviceTypes = EnumSet.of(ConnectServiceType.AUDIO);
            } else {
                throw new IllegalArgumentException("invalid serviceType: " + serviceType);
            }

            CrpPhoneServiceConnectCompleteEvent ev = new CrpPhoneServiceConnectCompleteEvent(
                    result, bdAddress, requestType, serviceTypes);
            Timber.d("doHandle() " + ev);
            getSession().publishEvent(ev);
            return getPacketBuilder().createPhoneServiceConnectCompleteNotificationResponse();
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            return null;
        }
    }
}
