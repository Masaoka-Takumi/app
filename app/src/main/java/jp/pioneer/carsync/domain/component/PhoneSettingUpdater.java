package jp.pioneer.carsync.domain.component;

/**
 * Phone設定更新.
 */
public interface PhoneSettingUpdater {

    /**
     * AUTO ANSWER設定.
     *
     * @param enabled 有効か否か {@code true}:AUTO ANSWER設定有効 {@code false}:AUTO ANSWER設定無効
     */
    void setAutoAnswer(boolean enabled);

    /**
     * AUTO PAIRING設定.
     *
     * @param enabled 有効か否か {@code true}:AUTO PAIRING設定有効 {@code false}:AUTO PAIRING設定無効
     */
    void setAutoPairing(boolean enabled);
}
