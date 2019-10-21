package jp.pioneer.carsync.presentation.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;

/**
 * TIPSのエンドポイント.
 */
public enum TipsContentsEndpoint {
    /** production. */
    PRODUCTION("https://d2svjshr486bve.cloudfront.net/list/", R.string.dbg_026) {
        @Override
        public TipsContentsEndpoint toggle() {
            return STAGING;
        }
    },
    /** staging. */
    STAGING("https://d13xrvdgmwkyo4.cloudfront.net/list/", R.string.dbg_027) {
        @Override
        public TipsContentsEndpoint toggle() {
            return PRODUCTION;
        }
    };

    /** エンドポイント. */
    public String endpoint;

    /** 表示用文字列リソースID. */
    @StringRes public final int label;

    /**
     * コンストラクタ.
     *
     * @param endpoint エンドポイント
     */
    TipsContentsEndpoint(String endpoint, @StringRes int label) {
        this.endpoint = endpoint;
        this.label = label;
    }

    /**
     * トグル.
     */
    public TipsContentsEndpoint toggle() {
        return null;
    }
}
