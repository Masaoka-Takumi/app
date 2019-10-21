package jp.pioneer.carsync.presentation.view.widget;

import android.support.v4.view.ViewPager;
import android.view.View;

import me.crosswall.lib.coverflow.core.Utils;

/**
 * カバーフロー用表示クラス
 */

public class MyTransformer implements ViewPager.PageTransformer {
    private float scale = 0.0f;

    public MyTransformer(float scale) {
        this.scale = scale;
    }

    @Override
    public void transformPage(View page, float position) {
        if (this.scale != 0.0f) {
            float scale = Math.abs(position) * this.scale < this.scale ? Math.abs(position) * this.scale : this.scale;
            scale = Utils.getFloat(1.0f - Math.abs(scale), 0.3f, 1.0f);
            page.setScaleX(scale);
            page.setScaleY(scale);
        }
    }
}
