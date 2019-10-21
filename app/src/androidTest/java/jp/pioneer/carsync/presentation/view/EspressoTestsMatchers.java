package jp.pioneer.carsync.presentation.view;

import android.view.View;

import org.hamcrest.Matcher;

/**
 * EspressoTestsMatchers　自作Matchersクラス
 */

public class EspressoTestsMatchers {

    /**
     * ImageViewに指定したリソースIDのimageが設定されているかチェック
     */
    public static Matcher<View> withDrawable(final int resourceId) {
        return new DrawableMatcher(resourceId);
    }

    /**
     * ImageViewにimageが設定されていないことをチェック
     */
    public static Matcher<View> noDrawable() {
        return new DrawableMatcher(-1);
    }
}
