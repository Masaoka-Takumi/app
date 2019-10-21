package jp.pioneer.carsync.presentation.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;

/**
 * 緊急連絡手段
 */
public enum ImpactNotificationMethod {
    INVALID(R.string.invalid),
    PHONE(R.string.val_063) {
        @Override
        public ImpactNotificationMethod toggle() {
            return SMS;
        }
    },
    SMS(R.string.val_062) {
        @Override
        public ImpactNotificationMethod toggle() {
            return PHONE;
        }
    },;

    /** 表示用文字列リソースID. */
    @StringRes public final int label;

    /**
     * コンストラクタ.
     *
     * @param label 表示用文字列リソースID
     */
    ImpactNotificationMethod(@StringRes int label) {
        this.label = label;
    }

    /**
     * {@inheritDoc}
     */
    @StringRes
    public int getLabel() {
        return label;
    }

    /**
     * 当クラスの値切換メソッド
     *
     * @return ImpactNotificationMethod
     */
    public ImpactNotificationMethod toggle() {
        return INVALID;
    }
}
