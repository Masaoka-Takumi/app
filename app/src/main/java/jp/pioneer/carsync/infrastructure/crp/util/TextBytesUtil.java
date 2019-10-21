package jp.pioneer.carsync.infrastructure.crp.util;

import android.support.annotation.NonNull;

import java.io.UnsupportedEncodingException;

import jp.pioneer.carsync.domain.model.CharSetType;
import timber.log.Timber;

/**
 * 文字列のバイト列を扱うユーティリティ.
 */
public class TextBytesUtil {
    /**
     * 文字列抽出.
     * <p>
     * data内のテキストのバイト列をStringとして抽出する。
     *
     * @param data データ
     * @param start 文字コードのデータ位置
     * @return 文字列
     */
    public static String extractText(@NonNull byte[] data, int start) {
        byte charSetCode = data[start];
        start++;
        CharSetType charSetType = CharSetType.valueOf(charSetCode);
        if (charSetType == CharSetType.INVALID) {
            // 無効値(0xFF)になっている場合はテキストデータは無いので空文字列を返す
            Timber.d("Invalid CharSet. code=%X", charSetCode);
            return "";
        }

        return extractText(data, start, charSetType);
    }

    /**
     * 文字列抽出.
     * <p>
     * data内のテキストのバイト列をStringとして抽出する。
     *
     * @param data データ
     * @param start 開始位置
     * @param charSetType 文字コード種別
     * @return 文字列
     */
    public static String extractText(@NonNull byte[] data, int start, @NonNull CharSetType charSetType) {
        if (data.length == 0) {
            return "";
        }

        byte[] stringTerminator = charSetType == CharSetType.UTF16BE
                ? new byte[]{0x00, 0x00} : new byte[]{0x00};
        int nullPos = Kmp.indexOf(data, start, data.length, stringTerminator);
        if (nullPos == -1) {
            throw new IllegalArgumentException("Null character was not found.");
        }

        String text;
        if (charSetType.charset != null) {
            try {
                // Dstart-Dnまでの(n - start)バイトをStringに変換
                // DnのnはnullPos - 1. length = (nullPos - 1) - start + 1 = nullPos - start
                text = new String(data, start, nullPos - start, charSetType.charset);
            } catch (UnsupportedEncodingException e) {
                Timber.w("TODO Unsupported encoding:" + charSetType.charset);
                text = "?????";
            }
        } else if (charSetType == CharSetType.EBU_COMPLETE){
            text = EbuCompleteConverter.toString(data, start, nullPos - start);
        } else if (charSetType == CharSetType.TITLE_TEXT_STD_EURO) {
            text = TitleTextStdEuroConverter.toString(data, start, nullPos - start);
        } else {
            Timber.w("TODO Unsupported CharSetType:%s", charSetType);
            text = "?????";
        }
        return text;
    }
}
