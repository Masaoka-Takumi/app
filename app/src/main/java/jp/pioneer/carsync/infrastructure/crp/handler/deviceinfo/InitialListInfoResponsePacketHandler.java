package jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.ListInfo;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.DataResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;
import jp.pioneer.carsync.infrastructure.crp.util.TextBytesUtil;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;

/**
 * リスト初期情報応答パケットハンドラ.
 */
public class InitialListInfoResponsePacketHandler extends DataResponsePacketHandler {
    private CarRemoteSession mSession;
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public InitialListInfoResponsePacketHandler(@NonNull CarRemoteSession session) {
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

            int majorVer = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion().major;
            checkPacketDataLength(data, getDataLength(majorVer));
            ListInfo.TransactionInfo info = mStatusHolder.getListInfo().transactionInfo;

            // D1:ソース種別
            MediaSourceType sourceType = MediaSourceType.valueOf(data[1]);
            // D2:リスト種別
            ListType listType = ListType.valueOf(data[2], sourceType);
            // D3-D4:件数
            int total = PacketUtil.ushortToInt(data, 3);
            // D5-D6:一括要求可能件数
            int limit = PacketUtil.ushortToInt(data, 5);
            // D7-D8:フォーカス位置
            int focusListIndex = PacketUtil.ushortToInt(data, 7);

            mStatusHolder.getListInfo().focusListIndex = focusListIndex;

            String hierarchyName = "";
            if (majorVer >= 4) {
                // D9:文字コード
                // D10-D75:文字列(階層名)
                hierarchyName = TextBytesUtil.extractText(data, 9);
            }

            mStatusHolder.getListInfo().updateVersion();
            info.setInitialInfo(sourceType, listType, total, limit, focusListIndex, hierarchyName);

            mSession.publishStatusUpdateEvent(packet.getPacketIdType());
            Timber.d("handle() TransactionInfo = " + info);
            setResult(Boolean.TRUE);
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "doHandle()");
            setResult(Boolean.FALSE);
        }
    }

    /**
     * データ長取得.
     * <p>
     * メジャーバージョンからそれに対応したデータ長を取得する
     * アップデートによりデータ長が変更された場合は本メソッドに追加する
     * <p>
     * 対応したバージョンが存在しない場合は、
     * アップデートされたがデータ長は変更されていないと判断し、
     * 最大のデータ長を返す
     *
     * @param version メジャーバージョン
     * @return データ長
     */
    private int getDataLength(int version) {
        final int DATA_LENGTH = 9;
        final int V4_DATA_LENGTH = 76;
        final int MAX_DATA_LENGTH = Math.max(DATA_LENGTH, V4_DATA_LENGTH);

        switch(version){
            case 1:
            case 2:
            case 3:
                return DATA_LENGTH;
            case 4:
                return V4_DATA_LENGTH;
            default:
                return MAX_DATA_LENGTH;
        }
    }
}
