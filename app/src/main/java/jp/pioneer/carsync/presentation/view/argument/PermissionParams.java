package jp.pioneer.carsync.presentation.view.argument;

import android.os.Bundle;

import icepick.Icepick;
import icepick.State;

/**
 * Created by BP06566 on 2017/03/03.
 */

public class PermissionParams {
    @State public boolean isExecute = false;

    public Bundle toBundle() {
        Bundle args = new Bundle();
        Icepick.saveInstanceState(this, args);
        return args;
    }

    public static PermissionParams from(Bundle args) {
        PermissionParams params = new PermissionParams();
        Icepick.restoreInstanceState(params, args);
        return params;
    }
}
