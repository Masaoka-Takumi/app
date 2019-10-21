package jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.IlluminationColorMap;
import jp.pioneer.carsync.domain.model.IlluminationSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.DataResponsePacketHandler;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.domain.model.IlluminationColor.*;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;

/**
 * DISP COLOR一括情報応答パケットハンドラ.
 */
public class DispColorBulkSettingInfoResponsePacketHandler extends DataResponsePacketHandler {
    private StatusHolder mStatusHolder;

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DispColorBulkSettingInfoResponsePacketHandler(@NonNull CarRemoteSession session) {
        mStatusHolder = checkNotNull(session).getStatusHolder();
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
            IlluminationSetting illuminationSetting = mStatusHolder.getIlluminationSetting();
            IlluminationColorMap colorMap = illuminationSetting.dispColorSpec;
            // D1-D3:WHITE
            colorMap.get(WHITE).setValue(ubyteToInt(data[1]), ubyteToInt(data[2]), ubyteToInt(data[3]));
            // D4-D6:RED
            colorMap.get(RED).setValue(ubyteToInt(data[4]), ubyteToInt(data[5]), ubyteToInt(data[6]));
            // D7-D9:AMBER
            colorMap.get(AMBER).setValue(ubyteToInt(data[7]), ubyteToInt(data[8]), ubyteToInt(data[9]));
            // D10-D12:ORANGE
            colorMap.get(ORANGE).setValue(ubyteToInt(data[10]), ubyteToInt(data[11]), ubyteToInt(data[12]));
            // D13-D15:YELLOW
            colorMap.get(YELLOW).setValue(ubyteToInt(data[13]), ubyteToInt(data[14]), ubyteToInt(data[15]));
            // D16-D18:PURE GREEN
            colorMap.get(PURE_GREEN).setValue(ubyteToInt(data[16]), ubyteToInt(data[17]), ubyteToInt(data[18]));
            // D19-D21:GREEN
            colorMap.get(GREEN).setValue(ubyteToInt(data[19]), ubyteToInt(data[20]), ubyteToInt(data[21]));
            // D22-D24:TURQUOISE
            colorMap.get(TURQUOISE).setValue(ubyteToInt(data[22]), ubyteToInt(data[23]), ubyteToInt(data[24]));
            // D25-D27:LIGHT BLUE
            colorMap.get(LIGHT_BLUE).setValue(ubyteToInt(data[25]), ubyteToInt(data[26]), ubyteToInt(data[27]));
            // D28-D30:BLUE
            colorMap.get(BLUE).setValue(ubyteToInt(data[28]), ubyteToInt(data[29]), ubyteToInt(data[30]));
            // D31-D33:PURPLE
            colorMap.get(PURPLE).setValue(ubyteToInt(data[31]), ubyteToInt(data[32]), ubyteToInt(data[33]));
            // D34-D36:PINK
            colorMap.get(PINK).setValue(ubyteToInt(data[34]), ubyteToInt(data[35]), ubyteToInt(data[36]));
            // D37-D39:CUSTOM
            colorMap.get(CUSTOM).setValue(ubyteToInt(data[37]), ubyteToInt(data[38]), ubyteToInt(data[39]));

            if (majorVer >= 3) {
                v3(data, colorMap);
            }

            illuminationSetting.updateVersion();
            Timber.d("handle() DispColorSpec = " + colorMap);
            setResult(Boolean.TRUE);
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "handle()");
            setResult(Boolean.FALSE);
        }
    }

    private void v3(byte[] data, IlluminationColorMap colorMap) {
        // D40-D42:FOR MY CAR
        colorMap.get(FOR_MY_CAR).setValue(ubyteToInt(data[40]), ubyteToInt(data[41]), ubyteToInt(data[42]));
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
        final int V2_DATA_LENGTH = 40;
        final int V3_DATA_LENGTH = 43;
        final int MAX_DATA_LENGTH = Math.max(V2_DATA_LENGTH, V3_DATA_LENGTH);

        switch(version){
            case 1:
            case 2:
                return V2_DATA_LENGTH;
            case 3:
                return V3_DATA_LENGTH;
            default:
                return MAX_DATA_LENGTH;
        }
    }
}
