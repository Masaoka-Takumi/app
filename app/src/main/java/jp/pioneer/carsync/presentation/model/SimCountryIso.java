package jp.pioneer.carsync.presentation.model;

public enum SimCountryIso {
    US("us"), // アメリカ
    CA("ca"), // カナダ
    JP("jp"), // 日本
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
        for (SimCountryIso value : values()) {
            if (value.getCountryIso().equals(countryIso)) {
                return value;
            }
        }
        return NO_AVAILABLE;
    }
}
