package jp.pioneer.carsync.infrastructure.crp.util;

import java.io.ByteArrayOutputStream;

import jp.pioneer.carsync.infrastructure.crp.BadPacketException;

/**
 * パケットユーティリティ.
 */
public class PacketUtil {
    private static final int DLE = 0x9F;
    private static final int STX = 0x02;
    private static final int ETX = 0x03;
    private static final int ESCAPE = 0x9F;
    private static final String HEX_DIGITS = "0123456789ABCDEF";

    /**
     * チェックサム生成.
     * <p>
     * ID-Dn（データ部終端）までを1ByteずつXORした値。
    *
     * @param id ID（コマンドの大分類）
     * @param type Type（IDより下位のコマンド小分類）
     * @param data データ部
     * @return チェックサム
     */
    public static int createChecksum(int id, int type, byte[] data) {
        int checksum = id ^ type;
        for (byte tmp : data) {
            checksum = checksum ^ ubyteToInt(tmp);
        }
        return checksum;
    }

    /**
     * unsigned byte型をintに変換.
     * <p>
     * Javaはunsigned型が無いため、大きな型で管理する。
     *
     * <pre>{@code
     *  int result = PacketUtil.ubyteToInt((byte) -1); // resultは255になる
     * }</pre>
     *
     * @param b unsigned byte型を想定した値
     * @return int値
     */
    public static int ubyteToInt(byte b) {
        return b & 0xff;
    }

    /**
     * unsigned short型をintに変換.
     * <p>
     * data[start]、data[start + 1]をunsigned short型の値として変換する。
     * Javaはunsigned型が無いため、大きな型で管理する。
     * バイトの並びはBig Endianとする。
     *
     * @param data データ
     * @param start 変換を行う開始位置
     * @return int値
     */
    public static int ushortToInt(byte[] data, int start) {
        return ((ubyteToInt(data[start]) << 8) & 0xFF00)
                | (ubyteToInt(data[start + 1]) & 0xFF);
    }

    /**
     * unsigned int型をlongに変換.
     * <p>
     * data[start]～data[start + 3]をunsigned int型の値として変換する。
     * Javaはunsigned型が無いため、大きな型で管理する。
     * バイトの並びはBig Endianとする。
     *
     * @param data データ
     * @param start 変換を行う開始位置
     * @return int値
     */
    public static long uintToLong(byte[] data, int start) {
        return ((ubyteToInt(data[start]) << 24) & 0xFF000000L)
                | ((ubyteToInt(data[start + 1]) << 16) & 0xFF0000L)
                | ((ubyteToInt(data[start + 2]) << 8) & 0xFF00L)
                | (ubyteToInt(data[start + 3]) & 0xFFL);
    }

    /**
     * 指定ビットがONか否か取得.
     *
     * @param b バイト値
     * @param bitPosition 対象のビット（0～7）
     * @return {@code true}:ONである。{@code false}:それ以外。
     * @throws IllegalArgumentException {@code bitPosition}が0～7ではない
     */
    public static boolean isBitOn(byte b, int bitPosition) {
        if (bitPosition < 0 || 7 < bitPosition) {
            throw new IllegalArgumentException("Invalid bitPosition:" + bitPosition);
        }

        return ((ubyteToInt(b) >> bitPosition) & 0x01) == 0x01;
    }

    /**
     * 1バイト内の連続したビットの値取得.
     *
     * @param b バイト値
     * @param start 開始ビット位置（0～7）
     * @param len ビット数(1〜8-start)
     * @return 値
     * @throws IllegalArgumentException {@code start}、または、{@code len}の指定不正
     */
    public static int getBitsValue(byte b, int start, int len) {
        if (start < 0 || 7 < start) {
            throw new IllegalArgumentException("Invalid start:" + start);
        }

        if (len < 1 || 8 - start < len) {
            throw new IllegalArgumentException("Invalid length:" + len);
        }

        int mask = 0xFF >> (8 - len);
        return (ubyteToInt(b) >> start) & mask;
    }

    /**
     * 16進文字列化.
     *
     * @param bytes バイト列
     * @return 16進文字列
     */
    public static String toHex(byte... bytes) {
        StringBuilder sb = new StringBuilder();
        if (bytes != null) {
            for (byte b: bytes) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }

                sb.append(HEX_DIGITS.charAt((b >> 4 & 0xF)));
                sb.append(HEX_DIGITS.charAt(b & 0xF));
            }
        }

        return sb.toString();
    }

    /**
     * エスケープ.
     * <p>
     * バイト列内のDLEと同じ値の要素をエスケープする。
     *
     * @param bytes バイト列
     * @return エスケープ後のバイト列
     */
    public static byte[] escape(byte[] bytes) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        for (byte b : bytes) {
            if (ubyteToInt(b) == DLE) {
                stream.write(ESCAPE);
            }

            stream.write(ubyteToInt(b));
        }

        return stream.toByteArray();
    }

    /**
     * バイト値がDLEか否か取得.
     *
     * @param b バイト値
     * @return {@code true}:DLEである。{@code false}:それ以外。
     */
    public static boolean isDLE(byte b) {
        return ubyteToInt(b) == DLE;
    }

    /**
     * unsigned short型の値をバイト列化.
     * <p>
     * バイトの並びはBig Endianとする。
     *
     * @param value unsigned short型を想定した値
     * @return バイト列。
     */
    public static byte[] ushortToByteArray(int value) {
        return new byte[] {
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF)
        };
    }

    /**
     * unsigned int型の値をバイト列化.
     * <p>
     * バイトの並びはBig Endianとする。
     *
     * @param value unsigned int型を想定した値
     * @return バイト列
     */
    public static byte[] uintToByteArray(long value) {
        return new byte[]{
                (byte) ((value >> 24) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF),
        };
    }

    /**
     * パケット不良チェック.
     *
     * @param expression 式
     * @throws BadPacketException {@code expression}がfalse
     */
    public static void checkPacket(
            boolean expression, String format, Object... args) throws BadPacketException {
        if (!expression) {
            throw new BadPacketException(String.format(format, args));
        }
    }

    /**
     * パケットのデータ部長チェック.
     * <p>
     * パケットのデータ部長は、完全一致ではなくパケットを処理するための最低限必要な長さを
     * 満たしているかでチェックする。想定していない領域部分は参照しない。
     *
     * @param data パケットのデータ部
     * @param expected 期待する長さ（最低限必要な長さ）
     * @throws BadPacketException {@code data}が期待する長さではない
     */
    public static void checkPacketDataLength(byte[] data, int expected) throws BadPacketException {
        checkPacket(expected <= data.length,
                "data length invalid.  (Expected, Actual) = %d<=, %d",
                expected, data.length);
    }
}
