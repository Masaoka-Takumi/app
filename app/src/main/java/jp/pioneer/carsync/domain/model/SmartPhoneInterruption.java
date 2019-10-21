package jp.pioneer.carsync.domain.model;

/**
 * SmartPhone割り込み.
 */
public enum SmartPhoneInterruption {
    // 割り込みとなるものが決まっていないため、確認用に実装
    LOW(
            SmartPhoneInterruptType.LOW,
            SmartPhoneInterruptConcreteKind.LOW,
            "LOW",
            FlashPattern.BGV1
    ),
    MIDDLE(
            SmartPhoneInterruptType.MIDDLE,
            SmartPhoneInterruptConcreteKind.MIDDLE,
            "MIDDLE",
            FlashPattern.BGV2
    ),
    HIGH(
            SmartPhoneInterruptType.HIGH,
            SmartPhoneInterruptConcreteKind.HIGH,
            "HIGH INTERRUPT 012456789ABCDE",
            FlashPattern.BGV3
    );

    /** 割り込み優先度. */
    public final SmartPhoneInterruptType type;
    /** 割り込み具体種別. */
    public final SmartPhoneInterruptConcreteKind kind;
    /** メッセージ. */
    public final String message;
    /** 発光パターン. */
    public final FlashPattern pattern;

    /**
     * コンストラクタ.
     *
     * @param type 割り込み優先度
     * @param kind 割り込み具体種別
     * @param message メッセージ
     * @param pattern 発光パターン
     */
    SmartPhoneInterruption(SmartPhoneInterruptType type,
                           SmartPhoneInterruptConcreteKind kind,
                           String message,
                           FlashPattern pattern){
        this.type = type;
        this.kind = kind;
        this.message = message;
        this.pattern = pattern;
    }
}
