package jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo;

import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

import java.util.Locale;

import jp.pioneer.carsync.domain.model.HdRadioBandType;
import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SxmBandType;
import jp.pioneer.carsync.domain.model.TunerFrequencyUnit;
import jp.pioneer.carsync.domain.model.UsbInfoType;
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

/**
 * リスト情報応答パケットハンドラ.
 */
public class ListInfoResponsePacketHandler extends DataResponsePacketHandler {
    private static final int MIN_DATA_LENGTH = 5;
    private static final int RADIO_ITEM_LENGTH = 21;
    private static final int SIRIUS_XM_ITEM_LENGTH = 14;
    private static final int DAB_ITEM_LENGTH = 19;
    private static final int USB_ITEM_LENGTH = 68;
    private CarRemoteSession mSession;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public ListInfoResponsePacketHandler(@NonNull CarRemoteSession session) {
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

            int majorVer = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion().major;
            ListInfo.TransactionInfo info = mStatusHolder.getListInfo().transactionInfo;

            // D1-D2:トランザクションID
            int transactionId = PacketUtil.ushortToInt(data, 1);
            if (info.id != transactionId) {
                throw new BadPacketException(String.format(Locale.US,
                        "Unmatched transactionId. (Expected, Actual) = %d, %d", info.id, transactionId));
            }
            // D3-D4:リストインデックス
            int listIndex = PacketUtil.ushortToInt(data, 3);

            //リストインデックスが不正なら捨てる
            if (listIndex < 1 || listIndex > info.total) {
                Timber.w("listIndex invalid. listIndex = %d,",listIndex);
                return;
            }

            // D5-DN:リスト情報
            switch (info.sourceType) {
                case RADIO:
                case HD_RADIO:
                    processRadio(data, info, listIndex);
                    break;
                case SIRIUS_XM:
                    processSiriusXm(data, info, listIndex);
                    break;
                case DAB:
                    processDab(data, info, listIndex);
                    break;
                case USB:
                    if (majorVer >= 4) {
                        processUsb(data, info, listIndex);
                    }
                    break;
                default:
                    throw new AssertionError("can't happen.");
            }

            mSession.publishStatusUpdateEvent(packet.getPacketIdType());
            Timber.d("handle() TransactionInfo = " + info);
            setResult(Boolean.TRUE);
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "handle()");
            setResult(Boolean.FALSE);
        }
    }

    private void processRadio(byte[] data, ListInfo.TransactionInfo info, int listIndex) throws Exception {
        int pos = 5;
        checkPacket(((data.length - pos) % RADIO_ITEM_LENGTH) == 0,
                "data length invalid. list info = %d", data.length - pos);

        SparseArrayCompat<ListInfo.ListItem> items = info.items;
        int count = (data.length - pos) / RADIO_ITEM_LENGTH;
        for (int i = 0; i < count; i++, pos+=RADIO_ITEM_LENGTH) {
            int index = listIndex + i;
            ListInfo.RadioListItem item = (ListInfo.RadioListItem) items.get(index);
            if (item == null) {
                item = new ListInfo.RadioListItem();
                item.listIndex = index;
                items.put(index, item);
            }

            // offset[0]:P.CH Number
            item.pchNumber = PacketUtil.ubyteToInt(data[pos]);
            // offset[1]-offset[4]:周波数
            item.frequency = PacketUtil.uintToLong(data, pos + 1);
            // offset[5]:周波数単位
            item.frequencyUnit = TunerFrequencyUnit.valueOf(data[pos + 5], info.sourceType);
            // offset[6]:Band
            if (info.sourceType == MediaSourceType.RADIO) {
                item.band = RadioBandType.valueOf(data[pos + 6]);
            } else {
                item.band = HdRadioBandType.valueOf(data[pos + 6]);
            }
            // offset[7]:文字コード
            // offset[8]-offset[20]:文字列(PS/Call sign)
            item.text = TextBytesUtil.extractText(data, pos + 7);
        }
    }

    private void processSiriusXm(byte[] data, ListInfo.TransactionInfo info, int listIndex) throws Exception {
        int pos = 5;
        checkPacket(((data.length - pos) % SIRIUS_XM_ITEM_LENGTH) == 0,
                "data length invalid. list info = %d", data.length - pos);

        SparseArrayCompat<ListInfo.ListItem> items = info.items;
        int count = (data.length - pos) / SIRIUS_XM_ITEM_LENGTH;
        for (int i = 0; i < count; i++, pos+=SIRIUS_XM_ITEM_LENGTH) {
            int index = listIndex + i;
            ListInfo.SxmListItem item = (ListInfo.SxmListItem) items.get(index);
            if (item == null) {
                item = new ListInfo.SxmListItem();
                item.listIndex = index;
                items.put(index, item);
            }

            // offset[0]:P.CH Number
            item.pchNumber = PacketUtil.ubyteToInt(data[pos]);
            // offset[1]-offset[2]:CHANNEL NUMBER
            item.channelNumber = PacketUtil.ushortToInt(data, pos + 1);
            // offset[3]:Band
            item.band = SxmBandType.valueOf(data[pos+3]);
            // offset[4]:文字コード
            // offset[5]-offset[13]:文字コード
            item.text = TextBytesUtil.extractText(data, pos + 4);
        }
    }

    private void processDab(byte[] data, ListInfo.TransactionInfo info, int listIndex) throws Exception {
        int pos = 5;
        checkPacket(((data.length - pos) % DAB_ITEM_LENGTH) == 0,
                "data length invalid. list info = %d", data.length - pos);

        SparseArrayCompat<ListInfo.ListItem> items = info.items;
        int count = (data.length - pos) / DAB_ITEM_LENGTH;
        for (int i = 0; i < count; i++, pos+=DAB_ITEM_LENGTH) {
            int index = listIndex + i;
            ListInfo.DabListItem item = (ListInfo.DabListItem) items.get(index);
            if (item == null) {
                item = new ListInfo.DabListItem();
                item.listIndex = index;
                items.put(index, item);
            }

            // offset[0]:INDEX
            item.index = PacketUtil.ubyteToInt(data[pos]);
            // offset[1]-offset[2]:EID
            item.eid = PacketUtil.ushortToInt(data, pos + 1);
            // offset[3]-offset[6]:SID
            item.sid = PacketUtil.uintToLong(data, pos + 3);
            // offset[7]-offset[8]:SCIdS
            item.scids = PacketUtil.ushortToInt(data, pos + 7);
            // offset[9]:文字コード
            // offset[10]-offset[18]:文字列(service name)
            item.text = TextBytesUtil.extractText(data, pos + 9);
        }
    }

    private void processUsb(byte[] data, ListInfo.TransactionInfo info, int listIndex) throws Exception {
        int pos = 5;
        checkPacket(((data.length - pos) % USB_ITEM_LENGTH) == 0,
                "data length invalid. list info = %d", data.length - pos);

        SparseArrayCompat<ListInfo.ListItem> items = info.items;
        int count = (data.length - pos) / USB_ITEM_LENGTH;
        for (int i = 0; i < count; i++, pos+=USB_ITEM_LENGTH) {
            int index = listIndex + i;
            ListInfo.UsbListItem item = (ListInfo.UsbListItem) items.get(index);
            if (item == null) {
                item = new ListInfo.UsbListItem();
                item.listIndex = index;
                items.put(index, item);
            }

            // offset[0]:情報種別
            item.type = UsbInfoType.valueOf(data[pos]);
            // offset[6]:文字コード
            // offset[7]-offset[72]:文字列(service name)
            item.text = TextBytesUtil.extractText(data, pos + 1);
        }
    }
}
