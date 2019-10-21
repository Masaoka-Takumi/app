package jp.pioneer.carsync.presentation.model;

import android.support.annotation.StringRes;

import jp.pioneer.carsync.R;

/**
 * Created by NSW00_008316 on 2017/06/22.
 */

public enum UiColor {
    INVALID(android.R.color.transparent, R.string.invalid),
    BLUE(R.color.ui_color_blue, R.string.val_052),
    AQUA(R.color.ui_color_aqua, R.string.val_248),
    GREEN(R.color.ui_color_green, R.string.val_049),
    YELLOW(R.color.ui_color_yellow, R.string.val_047),
    AMBER(R.color.ui_color_amber, R.string.val_045),
    RED(R.color.ui_color_red, R.string.val_044),
    PINK(R.color.ui_color_pink, R.string.val_054);

    private int mColorRes;
    /** 表示用文字列リソースID. */
    @StringRes public final int label;

    UiColor(int colorRes,@StringRes int label) {
        mColorRes = colorRes;
        this.label = label;
    }

    public int getResource() {
        return mColorRes;
    }

    public int getColorThemeId(){
        int theme = R.style.AppTheme_Blue;
        switch (this) {
            case BLUE:
                theme = R.style.AppTheme_Blue;
                break;
            case AQUA:
                theme = R.style.AppTheme_Aqua;
                break;
            case GREEN:
                theme = R.style.AppTheme_Green;
                break;
            case YELLOW:
                theme = R.style.AppTheme_Yellow;
                break;
            case AMBER:
                theme = R.style.AppTheme_Amber;
                break;
            case RED:
                theme = R.style.AppTheme_Red;
                break;
            case PINK:
                theme = R.style.AppTheme_Pink;
                break;
        }
        return theme;
    }
}
