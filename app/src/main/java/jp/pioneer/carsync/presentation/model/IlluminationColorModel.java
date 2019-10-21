package jp.pioneer.carsync.presentation.model;

import android.os.Bundle;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import icepick.Icepick;
import icepick.State;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;

/**
 * イルミネーションカラー設定値
 */
@PresenterLifeCycle
public class IlluminationColorModel {
    @State(IntegerPropertyBundler.class) public Property<Integer> red = new Property<>();
    @State(IntegerPropertyBundler.class) public Property<Integer> green = new Property<>();
    @State(IntegerPropertyBundler.class) public Property<Integer> blue = new Property<>();

    /**
     * コンストラクタ
     */
    @Inject
    public IlluminationColorModel() {
    }

    /**
     * 保存
     *
     * @param outState 保存先
     */
    public void saveInstanceState(@NonNull Bundle outState) {
        Icepick.saveInstanceState(this, outState);
    }

    /**
     * 復元
     *
     * @param savedInstanceState 読み込み先
     */
    public void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        Icepick.restoreInstanceState(this, savedInstanceState);
    }
}
