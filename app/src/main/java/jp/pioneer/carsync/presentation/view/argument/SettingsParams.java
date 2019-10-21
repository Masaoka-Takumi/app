package jp.pioneer.carsync.presentation.view.argument;

import android.os.Bundle;

import icepick.Icepick;
import icepick.State;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * 設定選択パラメータ
 */

public class SettingsParams {
    @State public String pass;
    @State public ScreenId mScreenId;
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
     * ディレクトリの変更
     *
     * @param directory 変更後のディレクトリ
     */
    public void changeDirectory(String directory) {
        pass = directory;
    }

    /**
     * 復元
     *
     * @param args Bundle
     * @return MusicParams
     */
    public static SettingsParams from(Bundle args) {
        SettingsParams params = new SettingsParams();
        Icepick.restoreInstanceState(params, args);
        return params;
    }
}
