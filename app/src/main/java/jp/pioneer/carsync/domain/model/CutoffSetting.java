package jp.pioneer.carsync.domain.model;

/**
 * Cutoff周波数設定を抽象化したインターフェース.
 *
 * @see Ac2StandardCutoffSetting
 * @see JasperCutoffSetting
 * @see StandardCutoffSetting
 * @see TwoWayNetworkSubwooferLpfMidHpfCutoffSetting
 * @see TwoWayNetworkMidLpfHighHpfCutoffSetting
 */
public interface CutoffSetting {
    /**
     * プロトコルでの定義値取得.
     *
     * @return プロトコルでの定義値
     */
    int getCode();

    /**
     * 周波数取得.
     *
     * @return 周波数
     */
    float getFrequency();

    /**
     * トグル.
     *
     * @param delta Cutoff周波数の変化量
     * @return トグル後のCutoffSetting。変化の範囲を超えた場合null。
     */
    CutoffSetting toggle(int delta);
}
