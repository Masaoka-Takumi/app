package jp.pioneer.carsync.infrastructure.crp.util;

import android.util.SparseArray;

import jp.pioneer.carsync.domain.model.CharSetType;

/**
 * パイオニア独自仕様の文字コードコンバータ.
 * <p>
 * {@link CharSetType#EBU_COMPLETE}と{@link CharSetType#TITLE_TEXT_STD_EURO}の文字コードを
 * Unicodeに変換する際に使用する。
 */
public class TitleTextStdEuroConverter {
    private static final SparseArray<String> sCharacters = new SparseArray<>(224);
    static {
        sCharacters.put(0x20, String.valueOf(Character.toChars(0x0020)));
        sCharacters.put(0x21, String.valueOf(Character.toChars(0x0021)));
        sCharacters.put(0x22, String.valueOf(Character.toChars(0x0022)));
        sCharacters.put(0x23, String.valueOf(Character.toChars(0x0023)));
        sCharacters.put(0x24, String.valueOf(Character.toChars(0x00A4)));
        sCharacters.put(0x25, String.valueOf(Character.toChars(0x0025)));
        sCharacters.put(0x26, String.valueOf(Character.toChars(0x0026)));
        sCharacters.put(0x27, String.valueOf(Character.toChars(0x0027)));
        sCharacters.put(0x28, String.valueOf(Character.toChars(0x0028)));
        sCharacters.put(0x29, String.valueOf(Character.toChars(0x0029)));
        sCharacters.put(0x2a, String.valueOf(Character.toChars(0x002A)));
        sCharacters.put(0x2b, String.valueOf(Character.toChars(0x002B)));
        sCharacters.put(0x2c, String.valueOf(Character.toChars(0x002C)));
        sCharacters.put(0x2d, String.valueOf(Character.toChars(0x002D)));
        sCharacters.put(0x2e, String.valueOf(Character.toChars(0x002E)));
        sCharacters.put(0x2f, String.valueOf(Character.toChars(0x002F)));

        sCharacters.put(0x30, String.valueOf(Character.toChars(0x0030)));
        sCharacters.put(0x31, String.valueOf(Character.toChars(0x0031)));
        sCharacters.put(0x32, String.valueOf(Character.toChars(0x0032)));
        sCharacters.put(0x33, String.valueOf(Character.toChars(0x0033)));
        sCharacters.put(0x34, String.valueOf(Character.toChars(0x0034)));
        sCharacters.put(0x35, String.valueOf(Character.toChars(0x0035)));
        sCharacters.put(0x36, String.valueOf(Character.toChars(0x0036)));
        sCharacters.put(0x37, String.valueOf(Character.toChars(0x0037)));
        sCharacters.put(0x38, String.valueOf(Character.toChars(0x0038)));
        sCharacters.put(0x39, String.valueOf(Character.toChars(0x0039)));
        sCharacters.put(0x3a, String.valueOf(Character.toChars(0x003A)));
        sCharacters.put(0x3b, String.valueOf(Character.toChars(0x003B)));
        sCharacters.put(0x3c, String.valueOf(Character.toChars(0x003C)));
        sCharacters.put(0x3d, String.valueOf(Character.toChars(0x003D)));
        sCharacters.put(0x3e, String.valueOf(Character.toChars(0x003E)));
        sCharacters.put(0x3f, String.valueOf(Character.toChars(0x003F)));

        sCharacters.put(0x40, String.valueOf(Character.toChars(0x0040)));
        sCharacters.put(0x41, String.valueOf(Character.toChars(0x0041)));
        sCharacters.put(0x42, String.valueOf(Character.toChars(0x0042)));
        sCharacters.put(0x43, String.valueOf(Character.toChars(0x0043)));
        sCharacters.put(0x44, String.valueOf(Character.toChars(0x0044)));
        sCharacters.put(0x45, String.valueOf(Character.toChars(0x0045)));
        sCharacters.put(0x46, String.valueOf(Character.toChars(0x0046)));
        sCharacters.put(0x47, String.valueOf(Character.toChars(0x0047)));
        sCharacters.put(0x48, String.valueOf(Character.toChars(0x0048)));
        sCharacters.put(0x49, String.valueOf(Character.toChars(0x0049)));
        sCharacters.put(0x4a, String.valueOf(Character.toChars(0x004A)));
        sCharacters.put(0x4b, String.valueOf(Character.toChars(0x004B)));
        sCharacters.put(0x4c, String.valueOf(Character.toChars(0x004C)));
        sCharacters.put(0x4d, String.valueOf(Character.toChars(0x004D)));
        sCharacters.put(0x4e, String.valueOf(Character.toChars(0x004E)));
        sCharacters.put(0x4f, String.valueOf(Character.toChars(0x004F)));

        sCharacters.put(0x50, String.valueOf(Character.toChars(0x0050)));
        sCharacters.put(0x51, String.valueOf(Character.toChars(0x0051)));
        sCharacters.put(0x52, String.valueOf(Character.toChars(0x0052)));
        sCharacters.put(0x53, String.valueOf(Character.toChars(0x0053)));
        sCharacters.put(0x54, String.valueOf(Character.toChars(0x0054)));
        sCharacters.put(0x55, String.valueOf(Character.toChars(0x0055)));
        sCharacters.put(0x56, String.valueOf(Character.toChars(0x0056)));
        sCharacters.put(0x57, String.valueOf(Character.toChars(0x0057)));
        sCharacters.put(0x58, String.valueOf(Character.toChars(0x0058)));
        sCharacters.put(0x59, String.valueOf(Character.toChars(0x0059)));
        sCharacters.put(0x5a, String.valueOf(Character.toChars(0x005A)));
        sCharacters.put(0x5b, String.valueOf(Character.toChars(0x005B)));
        sCharacters.put(0x5c, String.valueOf(Character.toChars(0x005C)));
        sCharacters.put(0x5d, String.valueOf(Character.toChars(0x005D)));
        sCharacters.put(0x5e, String.valueOf(Character.toChars(0x2015)));
        sCharacters.put(0x5f, String.valueOf(Character.toChars(0x005F)));

        sCharacters.put(0x60, String.valueOf(Character.toChars(0x2016)));
        sCharacters.put(0x61, String.valueOf(Character.toChars(0x0061)));
        sCharacters.put(0x62, String.valueOf(Character.toChars(0x0062)));
        sCharacters.put(0x63, String.valueOf(Character.toChars(0x0063)));
        sCharacters.put(0x64, String.valueOf(Character.toChars(0x0064)));
        sCharacters.put(0x65, String.valueOf(Character.toChars(0x0065)));
        sCharacters.put(0x66, String.valueOf(Character.toChars(0x0066)));
        sCharacters.put(0x67, String.valueOf(Character.toChars(0x0067)));
        sCharacters.put(0x68, String.valueOf(Character.toChars(0x0068)));
        sCharacters.put(0x69, String.valueOf(Character.toChars(0x0069)));
        sCharacters.put(0x6a, String.valueOf(Character.toChars(0x006A)));
        sCharacters.put(0x6b, String.valueOf(Character.toChars(0x006B)));
        sCharacters.put(0x6c, String.valueOf(Character.toChars(0x006C)));
        sCharacters.put(0x6d, String.valueOf(Character.toChars(0x006D)));
        sCharacters.put(0x6e, String.valueOf(Character.toChars(0x006E)));
        sCharacters.put(0x6f, String.valueOf(Character.toChars(0x006F)));

        sCharacters.put(0x70, String.valueOf(Character.toChars(0x0070)));
        sCharacters.put(0x71, String.valueOf(Character.toChars(0x0071)));
        sCharacters.put(0x72, String.valueOf(Character.toChars(0x0072)));
        sCharacters.put(0x73, String.valueOf(Character.toChars(0x0073)));
        sCharacters.put(0x74, String.valueOf(Character.toChars(0x0074)));
        sCharacters.put(0x75, String.valueOf(Character.toChars(0x0075)));
        sCharacters.put(0x76, String.valueOf(Character.toChars(0x0076)));
        sCharacters.put(0x77, String.valueOf(Character.toChars(0x0077)));
        sCharacters.put(0x78, String.valueOf(Character.toChars(0x0078)));
        sCharacters.put(0x79, String.valueOf(Character.toChars(0x0079)));
        sCharacters.put(0x7a, String.valueOf(Character.toChars(0x007A)));
        sCharacters.put(0x7b, String.valueOf(Character.toChars(0x007B)));
        sCharacters.put(0x7c, String.valueOf(Character.toChars(0x007C)));
        sCharacters.put(0x7d, String.valueOf(Character.toChars(0x007D)));
        sCharacters.put(0x7e, String.valueOf(Character.toChars(0x203E)));
        sCharacters.put(0x7f, String.valueOf(Character.toChars(0x0020)));

        sCharacters.put(0x80, String.valueOf(Character.toChars(0x00E1)));
        sCharacters.put(0x81, String.valueOf(Character.toChars(0x00E0)));
        sCharacters.put(0x82, String.valueOf(Character.toChars(0x00E9)));
        sCharacters.put(0x83, String.valueOf(Character.toChars(0x00E8)));
        sCharacters.put(0x84, String.valueOf(Character.toChars(0x00ED)));
        sCharacters.put(0x85, String.valueOf(Character.toChars(0x00EC)));
        sCharacters.put(0x86, String.valueOf(Character.toChars(0x00F3)));
        sCharacters.put(0x87, String.valueOf(Character.toChars(0x00F2)));
        sCharacters.put(0x88, String.valueOf(Character.toChars(0x00FA)));
        sCharacters.put(0x89, String.valueOf(Character.toChars(0x00F9)));
        sCharacters.put(0x8a, String.valueOf(Character.toChars(0x00D1)));
        sCharacters.put(0x8b, String.valueOf(Character.toChars(0x00C7)));
        sCharacters.put(0x8c, String.valueOf(Character.toChars(0x015E)));
        sCharacters.put(0x8d, String.valueOf(Character.toChars(0x00DF)));
        sCharacters.put(0x8e, String.valueOf(Character.toChars(0x00A1)));
        sCharacters.put(0x8f, String.valueOf(Character.toChars(0x0132)));

        sCharacters.put(0x90, String.valueOf(Character.toChars(0x00E2)));
        sCharacters.put(0x91, String.valueOf(Character.toChars(0x00E4)));
        sCharacters.put(0x92, String.valueOf(Character.toChars(0x00EA)));
        sCharacters.put(0x93, String.valueOf(Character.toChars(0x00EB)));
        sCharacters.put(0x94, String.valueOf(Character.toChars(0x00EE)));
        sCharacters.put(0x95, String.valueOf(Character.toChars(0x00EF)));
        sCharacters.put(0x96, String.valueOf(Character.toChars(0x00F4)));
        sCharacters.put(0x97, String.valueOf(Character.toChars(0x00F6)));
        sCharacters.put(0x98, String.valueOf(Character.toChars(0x00FB)));
        sCharacters.put(0x99, String.valueOf(Character.toChars(0x00FC)));
        sCharacters.put(0x9a, String.valueOf(Character.toChars(0x00F1)));
        sCharacters.put(0x9b, String.valueOf(Character.toChars(0x00E7)));
        sCharacters.put(0x9c, String.valueOf(Character.toChars(0x015F)));
        sCharacters.put(0x9d, String.valueOf(Character.toChars(0x011F)));
        sCharacters.put(0x9e, String.valueOf(Character.toChars(0x0131)));
        sCharacters.put(0x9f, String.valueOf(Character.toChars(0x0133)));

        sCharacters.put(0xa0, String.valueOf(Character.toChars(0x00AA)));
        sCharacters.put(0xa1, String.valueOf(Character.toChars(0x03B1)));
        sCharacters.put(0xa2, String.valueOf(Character.toChars(0x00A9)));
        sCharacters.put(0xa3, String.valueOf(Character.toChars(0x2030)));
        sCharacters.put(0xa4, String.valueOf(Character.toChars(0x011E)));
        sCharacters.put(0xa5, String.valueOf(Character.toChars(0x011B)));
        sCharacters.put(0xa6, String.valueOf(Character.toChars(0x0148)));
        sCharacters.put(0xa7, String.valueOf(Character.toChars(0x0151)));
        sCharacters.put(0xa8, String.valueOf(Character.toChars(0x03C0)));
        sCharacters.put(0xa9, String.valueOf(Character.toChars(0x20AC)));
        sCharacters.put(0xaa, String.valueOf(Character.toChars(0x00A3)));
        sCharacters.put(0xab, String.valueOf(Character.toChars(0x0024)));
        sCharacters.put(0xac, String.valueOf(Character.toChars(0x2190)));
        sCharacters.put(0xad, String.valueOf(Character.toChars(0x2191)));
        sCharacters.put(0xae, String.valueOf(Character.toChars(0x2192)));
        sCharacters.put(0xaf, String.valueOf(Character.toChars(0x2193)));

        sCharacters.put(0xb0, String.valueOf(Character.toChars(0x00BA)));
        sCharacters.put(0xb1, String.valueOf(Character.toChars(0x00B9)));
        sCharacters.put(0xb2, String.valueOf(Character.toChars(0x00B2)));
        sCharacters.put(0xb3, String.valueOf(Character.toChars(0x00B3)));
        sCharacters.put(0xb4, String.valueOf(Character.toChars(0x00B1)));
        sCharacters.put(0xb5, String.valueOf(Character.toChars(0x0130)));
        sCharacters.put(0xb6, String.valueOf(Character.toChars(0x0144)));
        sCharacters.put(0xb7, String.valueOf(Character.toChars(0x0171)));
        sCharacters.put(0xb8, String.valueOf(Character.toChars(0x00B5)));
        sCharacters.put(0xb9, String.valueOf(Character.toChars(0x00BF)));
        sCharacters.put(0xba, String.valueOf(Character.toChars(0x00F7)));
        sCharacters.put(0xbb, String.valueOf(Character.toChars(0x00B0)));
        sCharacters.put(0xbc, String.valueOf(Character.toChars(0x00BC)));
        sCharacters.put(0xbd, String.valueOf(Character.toChars(0x00BD)));
        sCharacters.put(0xbe, String.valueOf(Character.toChars(0x00BE)));
        sCharacters.put(0xbf, String.valueOf(Character.toChars(0x00A7)));

        sCharacters.put(0xc0, String.valueOf(Character.toChars(0x00C1)));
        sCharacters.put(0xc1, String.valueOf(Character.toChars(0x00C0)));
        sCharacters.put(0xc2, String.valueOf(Character.toChars(0x00C9)));
        sCharacters.put(0xc3, String.valueOf(Character.toChars(0x00C8)));
        sCharacters.put(0xc4, String.valueOf(Character.toChars(0x00CD)));
        sCharacters.put(0xc5, String.valueOf(Character.toChars(0x00CC)));
        sCharacters.put(0xc6, String.valueOf(Character.toChars(0x00D3)));
        sCharacters.put(0xc7, String.valueOf(Character.toChars(0x00D2)));
        sCharacters.put(0xc8, String.valueOf(Character.toChars(0x00DA)));
        sCharacters.put(0xc9, String.valueOf(Character.toChars(0x00D9)));
        sCharacters.put(0xca, String.valueOf(Character.toChars(0x0158)));
        sCharacters.put(0xcb, String.valueOf(Character.toChars(0x010C)));
        sCharacters.put(0xcc, String.valueOf(Character.toChars(0x0160)));
        sCharacters.put(0xcd, String.valueOf(Character.toChars(0x017D)));
        sCharacters.put(0xce, String.valueOf(Character.toChars(0x00D0)));
        sCharacters.put(0xcf, String.valueOf(Character.toChars(0x013F)));

        sCharacters.put(0xd0, String.valueOf(Character.toChars(0x00C2)));
        sCharacters.put(0xd1, String.valueOf(Character.toChars(0x00C4)));
        sCharacters.put(0xd2, String.valueOf(Character.toChars(0x00CA)));
        sCharacters.put(0xd3, String.valueOf(Character.toChars(0x00CB)));
        sCharacters.put(0xd4, String.valueOf(Character.toChars(0x00CE)));
        sCharacters.put(0xd5, String.valueOf(Character.toChars(0x00CF)));
        sCharacters.put(0xd6, String.valueOf(Character.toChars(0x00D4)));
        sCharacters.put(0xd7, String.valueOf(Character.toChars(0x00D6)));
        sCharacters.put(0xd8, String.valueOf(Character.toChars(0x00DB)));
        sCharacters.put(0xd9, String.valueOf(Character.toChars(0x00DC)));
        sCharacters.put(0xda, String.valueOf(Character.toChars(0x0159)));
        sCharacters.put(0xdb, String.valueOf(Character.toChars(0x010D)));
        sCharacters.put(0xdc, String.valueOf(Character.toChars(0x0161)));
        sCharacters.put(0xdd, String.valueOf(Character.toChars(0x017E)));
        sCharacters.put(0xde, String.valueOf(Character.toChars(0x0111)));
        sCharacters.put(0xdf, String.valueOf(Character.toChars(0x0140)));

        sCharacters.put(0xe0, String.valueOf(Character.toChars(0x00C3)));
        sCharacters.put(0xe1, String.valueOf(Character.toChars(0x00C5)));
        sCharacters.put(0xe2, String.valueOf(Character.toChars(0x00C6)));
        sCharacters.put(0xe3, String.valueOf(Character.toChars(0x0152)));
        sCharacters.put(0xe4, String.valueOf(Character.toChars(0x0177)));
        sCharacters.put(0xe5, String.valueOf(Character.toChars(0x00DD)));
        sCharacters.put(0xe6, String.valueOf(Character.toChars(0x00D5)));
        sCharacters.put(0xe7, String.valueOf(Character.toChars(0x00D8)));
        sCharacters.put(0xe8, String.valueOf(Character.toChars(0x00DE)));
        sCharacters.put(0xe9, String.valueOf(Character.toChars(0x014A)));
        sCharacters.put(0xea, String.valueOf(Character.toChars(0x0154)));
        sCharacters.put(0xeb, String.valueOf(Character.toChars(0x0106)));
        sCharacters.put(0xec, String.valueOf(Character.toChars(0x015A)));
        sCharacters.put(0xed, String.valueOf(Character.toChars(0x0179)));
        sCharacters.put(0xee, String.valueOf(Character.toChars(0x0166)));
        sCharacters.put(0xef, String.valueOf(Character.toChars(0x00F0)));

        sCharacters.put(0xf0, String.valueOf(Character.toChars(0x00E3)));
        sCharacters.put(0xf1, String.valueOf(Character.toChars(0x00E5)));
        sCharacters.put(0xf2, String.valueOf(Character.toChars(0x00E6)));
        sCharacters.put(0xf3, String.valueOf(Character.toChars(0x0153)));
        sCharacters.put(0xf4, String.valueOf(Character.toChars(0x0175)));
        sCharacters.put(0xf5, String.valueOf(Character.toChars(0x00FD)));
        sCharacters.put(0xf6, String.valueOf(Character.toChars(0x00F5)));
        sCharacters.put(0xf7, String.valueOf(Character.toChars(0x00F8)));
        sCharacters.put(0xf8, String.valueOf(Character.toChars(0x00FE)));
        sCharacters.put(0xf9, String.valueOf(Character.toChars(0x014B)));
        sCharacters.put(0xfa, String.valueOf(Character.toChars(0x0155)));
        sCharacters.put(0xfb, String.valueOf(Character.toChars(0x0107)));
        sCharacters.put(0xfc, String.valueOf(Character.toChars(0x015B)));
        sCharacters.put(0xfd, String.valueOf(Character.toChars(0x017A)));
        sCharacters.put(0xfe, String.valueOf(Character.toChars(0x0167)));
        sCharacters.put(0xff, String.valueOf(Character.toChars(0x0020)));
    }

    /**
     * Java文字列化.
     *
     * @param data {@link CharSetType#EBU_COMPLETE}と{@link CharSetType#TITLE_TEXT_STD_EURO}の文字列
     * @return Javaで使用する文字列（UTF-16）
     */
    public static String toString(byte[] data) {
        return toString(data, 0, data.length);
    }

    /**
     * Java文字列化.
     *
     * @param data {@link CharSetType#EBU_COMPLETE}と{@link CharSetType#TITLE_TEXT_STD_EURO}の文字列
     * @param start 開始位置
     * @param length 長さ
     * @return Javaで使用する文字列（UTF-16）
     */
    public static String toString(byte[] data, int start, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = start; i < start + length; i++) {
            sb.append(sCharacters.get(PacketUtil.ubyteToInt(data[i]), "?"));
        }
        return sb.toString();
    }
}
