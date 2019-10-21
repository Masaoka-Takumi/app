package jp.pioneer.carsync.domain.model;

/**
 * SmartPhone割り込み具体種別.
 * <p>
 * 車載機へは優先度{@link SmartPhoneInterruptType}での通知となっており、
 * 同じ優先度の異なる割込を区別するために使用する。
 */
public enum SmartPhoneInterruptConcreteKind {
    // 割り込みとなるものが決まっていないため、確認用として実装
    LOW,
    MIDDLE,
    HIGH
}
