package jp.pioneer.carsync.presentation.view.argument;

import android.os.Bundle;

import icepick.Icepick;
import icepick.State;
import jp.pioneer.carsync.domain.model.CustomEqType;

/**
 * Created by NSW00_007906 on 2017/06/29.
 */

public class EqSettingParams {
    @State public CustomEqType customType;

    /**
     * バンドル化
     *
     * @return Bundle
     */
    public Bundle toBundle(Bundle args) {
        Icepick.saveInstanceState(this, args);
        return args;
    }


    /**
     * 復元
     *
     * @param args Bundle
     * @return MusicParams
     */
    public static EqSettingParams from(Bundle args) {
        EqSettingParams params = new EqSettingParams();
        Icepick.restoreInstanceState(params, args);
        return params;
    }
}
