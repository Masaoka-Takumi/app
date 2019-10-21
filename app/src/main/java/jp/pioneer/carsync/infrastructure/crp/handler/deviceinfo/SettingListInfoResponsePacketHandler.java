package jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo;

import android.support.annotation.NonNull;

import java.util.Locale;

import jp.pioneer.carsync.domain.model.CharSetType;
import jp.pioneer.carsync.domain.model.DeviceListItem;
import jp.pioneer.carsync.domain.model.SearchListItem;
import jp.pioneer.carsync.domain.model.SettingListTransaction;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.DataResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;
import jp.pioneer.carsync.infrastructure.crp.util.TextBytesUtil;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacket;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.isBitOn;

/**
 * 設定リスト情報応答ハンドラ.
 */
public class SettingListInfoResponsePacketHandler extends DataResponsePacketHandler {
    private static final int MIN_DATA_LENGTH = 5;
    private static final int DEVICE_ITEM_MIN_LENGTH = 22;
    private static final int SEARCH_ITEM_MIN_LENGTH = 21;
    private CarRemoteSession mSession;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public SettingListInfoResponsePacketHandler(@NonNull CarRemoteSession session) {
        mSession = checkNotNull(session);
        mStatusHolder = session.getStatusHolder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull IncomingPacket packet) throws Exception {
        try {
            byte[] data = packet.getData();
            checkPacketDataLength(data, MIN_DATA_LENGTH);

            // D1-D2:トランザクションID
            int transactionId = PacketUtil.ushortToInt(data, 1);
            SettingListTransaction transaction = mStatusHolder.getSettingListInfoMap().getTransaction(transactionId);
            if (transaction == null) {
                throw new BadPacketException(String.format(Locale.US,
                        "Transaction not found. transactionId = %d", transactionId));
            }
            // D3-D4:リストインデックス
            int listIndex = PacketUtil.ushortToInt(data, 3);
            // D5-DN:リスト情報
            switch(transaction.listType) {
                case DEVICE_LIST:
                    processDevice(data, transaction, listIndex);
                    break;
                case SEARCH_LIST:
                    processSearch(data, transaction, listIndex);
                    break;
                default:
                    throw new AssertionError("can't happen.");
            }

            mSession.publishStatusUpdateEvent(packet.getPacketIdType());
            Timber.d("handle() SettingListTransaction = " + transaction);
            setResult(Boolean.TRUE);
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "handle()");
            setResult(Boolean.FALSE);
        }
    }

    private void processDevice(byte[] data, SettingListTransaction transaction, int listIndex) throws Exception {
        int pos = 5;
        checkPacket(DEVICE_ITEM_MIN_LENGTH <= (data.length - pos),
                "data length invalid. list info = %d", data.length - pos);

        byte b;
        // D5-D22:BDアドレス
        String bdAddress = TextBytesUtil.extractText(data, 5, CharSetType.UTF8);
        // D23:対応サービス
        b = data[23];
        boolean audioSupported = isBitOn(b, 1);
        boolean phoneSupported = isBitOn(b, 0);
        // D24:接続中サービス
        b = data[24];
        boolean audioConnected = isBitOn(b, 2);
        boolean phone2Connected = isBitOn(b, 1);
        boolean phone1Connected = isBitOn(b, 0);
        // D25:デバイス情報
        b = data[25];
        boolean audioFocus = isBitOn(b, 2);
        boolean lastAudioDevice = isBitOn(b, 1);
        boolean sessionConnected = isBitOn(b, 0);
        // D26-DN:デバイス名
        String deviceName = TextBytesUtil.extractText(data, 26, CharSetType.UTF8);

        DeviceListItem item = new DeviceListItem.Builder()
                .bdAddress(bdAddress)
                .audioSupported(audioSupported)
                .phoneSupported(phoneSupported)
                .audioConnected(audioConnected)
                .phone2Connected(phone2Connected)
                .phone1Connected(phone1Connected)
                .audioFocus(audioFocus)
                .lastAudioDevice(lastAudioDevice)
                .sessionConnected(sessionConnected)
                .deviceName(deviceName)
                .build();
        transaction.items.put(listIndex, item);
    }

    private void processSearch(byte[] data, SettingListTransaction transaction, int listIndex) throws Exception {
        int pos = 5;
        checkPacket(SEARCH_ITEM_MIN_LENGTH <= (data.length - pos),
                "data length invalid. list info = %d", data.length - pos);

        byte b;
        // D5-D22:BDアドレス
        String bdAddress = TextBytesUtil.extractText(data, 5, CharSetType.UTF8);
        // D23:対応サービス
        b = data[23];
        boolean audioSupported = isBitOn(b, 1);
        boolean phoneSupported = isBitOn(b, 0);
        // D24-DN:デバイス名
        String deviceName = TextBytesUtil.extractText(data, 24, CharSetType.UTF8);

        SearchListItem item = new SearchListItem.Builder()
                .bdAddress(bdAddress)
                .audioSupported(audioSupported)
                .phoneSupported(phoneSupported)
                .deviceName(deviceName)
                .build();
        transaction.items.put(listIndex, item);
    }

    private void checkDataLength(byte[] data) throws BadPacketException {
        checkPacket(MIN_DATA_LENGTH <= data.length,
                "data length invalid.  (Expected, Actual) = %d <=, %d",
                MIN_DATA_LENGTH, data.length);
    }
}
