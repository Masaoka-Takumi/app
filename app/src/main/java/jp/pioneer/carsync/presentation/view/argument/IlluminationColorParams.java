package jp.pioneer.carsync.presentation.view.argument;

import android.os.Bundle;

import icepick.Icepick;
import icepick.State;

/**
 * イルミネーション設定用パラメータ
 */

public class IlluminationColorParams extends SettingsParams {

    public enum IlluminationType {
        COMMON,
        DISP,
        KEY,
    }

    @State public IlluminationType type;

    /**
     * バンドル化
     *
     * @return Bundle
     */
    public Bundle toBundle() {
        Bundle args = new Bundle();
        Icepick.saveInstanceState(this, args);
        return args;
    }

    /**
     * 復元
     *
     * @param args Bundle
     * @return MusicParams
     */
    public static IlluminationColorParams from(Bundle args) {
        IlluminationColorParams params = new IlluminationColorParams();
        Icepick.restoreInstanceState(params, args);
        return params;
    }
}
