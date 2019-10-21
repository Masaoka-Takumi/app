package jp.pioneer.mbg.alexa.util;

import java.util.Map;

public class CastUtil {

    /**
     *  戻り値の型に合わせてキャスト
     *
     */
    @SuppressWarnings("unchecked")
    public static <T> T autoCast(Object obj) {
        T castObj = (T) obj;
        return castObj;
    }
}
