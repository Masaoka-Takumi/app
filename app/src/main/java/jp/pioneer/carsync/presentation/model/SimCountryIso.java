package jp.pioneer.carsync.presentation.model;

import java.util.Locale;

public enum SimCountryIso {
    US("us"), // アメリカ
    CA("ca"), // カナダ
    JP("jp"), // 日本
    IN("in"), // インド
    GB("gb"), // イギリス
    NO_DEFINE("no_define"), // 未定義の国
    NO_AVAILABLE("none"), // 取得不可
    ;

    private final String mCountryIso;

    SimCountryIso(String mCountryIso) {
        this.mCountryIso = mCountryIso;
    }

    public String getCountryIso() {
        return mCountryIso;
    }

    public static SimCountryIso getEnum(String countryIso) {
        if(countryIso == null) {
            return NO_AVAILABLE;
        }
        for (SimCountryIso value : values()) {
            if (value.getCountryIso().equals(countryIso.toLowerCase(Locale.US))) {
                return value;
            }
        }
        return NO_DEFINE;
    }
}
