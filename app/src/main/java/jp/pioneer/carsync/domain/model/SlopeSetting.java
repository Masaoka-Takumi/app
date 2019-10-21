package jp.pioneer.carsync.domain.model;


/**
 * スロープ設定を抽象化したインターフェース.
 *
 * @see Ac2StandardSubwooferSlopeSetting
 * @see JasperSlopeSetting
 * @see StandardSlopeSetting
 * @see TwoWayNetworkMidHfpMidLfpHighHpfSlopeSetting
 * @see TwoWayNetworkSubwooferLpfSlopeSetting
 */
public interface SlopeSetting {
    /**
     * プロトコルでの定義値取得.
     *
     * @return プロトコルでの定義値
     */
    int getCode();

    /**
     * レベル取得.
     *
     * @return レベル（dB/oct）
     */
    int getLevel();

    /**
     * トグル.
     *
     * @param delta スロープの変化量
     * @return トグル後のSlopeSetting。変化の範囲を超えた場合null。
     */
    SlopeSetting toggle(int delta);
}
