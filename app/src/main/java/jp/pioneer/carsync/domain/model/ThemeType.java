package jp.pioneer.carsync.domain.model;

import org.jetbrains.annotations.NotNull;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.model.IlluminationColorModel;
import jp.pioneer.carsync.presentation.model.UiColor;

/**
 * ThemeSet設定値
 */
public enum ThemeType {
    VIDEO_PATTERN1,
    VIDEO_PATTERN2,
    VIDEO_PATTERN3,
    VIDEO_PATTERN4,
    PICTURE_PATTERN1,
    PICTURE_PATTERN2,
    PICTURE_PATTERN3,
    PICTURE_PATTERN4,
    PICTURE_PATTERN5,
    PICTURE_PATTERN6,
    PICTURE_PATTERN7,
    PICTURE_PATTERN8,
    PICTURE_PATTERN9,
    PICTURE_PATTERN10,
    PICTURE_PATTERN11,
    PICTURE_PATTERN12,
    PICTURE_PATTERN13;
    /**
     * Backgroundリソースが動画かどうか
     *
     * @return True: 動画 / False: 静止画
     */
    public boolean isVideo() {
        switch (this) {
            case VIDEO_PATTERN1:
            case VIDEO_PATTERN2:
            case VIDEO_PATTERN3:
            case VIDEO_PATTERN4:
                return true;
            default:
                return false;
        }
    }

    /**
     * ResourceIdの取得
     * <p>
     * Videoパターンについては、@RawRes(R.raw.xx)
     * Pictureパターンについては、@DrawableRes(R.drawable.xx)
     *
     * @return resId
     */
    public int getResourceId() {
        switch (this) {
            case VIDEO_PATTERN1:
                return R.raw.bgv_001;
            case VIDEO_PATTERN2:
                return R.raw.bgv_002;
            case VIDEO_PATTERN3:
                return R.raw.bgv_003;
            case VIDEO_PATTERN4:
                return R.raw.bgv_004;
            case PICTURE_PATTERN1:
                return R.drawable.bgp_001;
            case PICTURE_PATTERN2:
                return R.drawable.bgp_002;
            case PICTURE_PATTERN3:
                return R.drawable.bgp_003;
            case PICTURE_PATTERN4:
                return R.drawable.bgp_004;
            case PICTURE_PATTERN5:
                return R.drawable.bgp_005;
            case PICTURE_PATTERN6:
                return R.drawable.bgp_006;
            case PICTURE_PATTERN7:
                return R.drawable.bgp_007;
            case PICTURE_PATTERN8:
                return R.drawable.bgp_008;
            case PICTURE_PATTERN9:
                return R.drawable.bgp_009;
            case PICTURE_PATTERN10:
                return R.drawable.bgp_010;
            case PICTURE_PATTERN11:
                return R.drawable.bgp_011;
            case PICTURE_PATTERN12:
                return R.drawable.bgp_012;
            case PICTURE_PATTERN13:
                return R.drawable.bgp_001;
            default:
                return 0;
        }
    }

    /**
     * Thumbnailの取得
     *
     * @return resId
     */
    public int getThumbnail() {
        switch (this) {
            case VIDEO_PATTERN1:
                return R.drawable.p1250_bgv_001;
            case VIDEO_PATTERN2:
                return R.drawable.p1250_bgv_002;
            case VIDEO_PATTERN3:
                return R.drawable.p1250_bgv_003;
            case VIDEO_PATTERN4:
                return R.drawable.p1250_bgv_004;
            case PICTURE_PATTERN1:
                return R.drawable.p1250_bgp_001;
            case PICTURE_PATTERN2:
                return R.drawable.p1250_bgp_002;
            case PICTURE_PATTERN3:
                return R.drawable.p1250_bgp_003;
            case PICTURE_PATTERN4:
                return R.drawable.p1250_bgp_004;
            case PICTURE_PATTERN5:
                return R.drawable.p1250_bgp_005;
            case PICTURE_PATTERN6:
                return R.drawable.p1250_bgp_006;
            case PICTURE_PATTERN7:
                return R.drawable.p1250_bgp_007;
            case PICTURE_PATTERN8:
                return R.drawable.p1250_bgp_008;
            case PICTURE_PATTERN9:
                return R.drawable.p1250_bgp_009;
            case PICTURE_PATTERN10:
                return R.drawable.p1250_bgp_010;
            case PICTURE_PATTERN11:
                return R.drawable.p1250_bgp_011;
            case PICTURE_PATTERN12:
                return R.drawable.p1250_bgp_012;
            case PICTURE_PATTERN13:
                return R.drawable.p1250_bgp_001;
            default:
                return 0;
        }
    }

    /**
     * イルミネーションカラー値の取得
     * <p>
     * 当メソッドは、イルミネーションカラーが
     * 個別設定できない(Disp/Keyが分かれていない)車載器の場合使用すること
     *
     * @return イルミネーションカラー設定値
     */
    @NotNull
    public IlluminationColorModel getIlluminationColor() {
        IlluminationColorModel model = new IlluminationColorModel();

        switch (this) {
            case VIDEO_PATTERN1:
                model.red.setValue(41);
                model.green.setValue(60);
                model.blue.setValue(60);
                break;
            case VIDEO_PATTERN2:
                model.red.setValue(41);
                model.green.setValue(60);
                model.blue.setValue(60);
                break;
            case VIDEO_PATTERN3:
                model.red.setValue(6);
                model.green.setValue(54);
                model.blue.setValue(36);
                break;
            case VIDEO_PATTERN4:
                model.red.setValue(0);
                model.green.setValue(28);
                model.blue.setValue(60);
                break;
            case PICTURE_PATTERN1:
                model.red.setValue(2);
                model.green.setValue(58);
                model.blue.setValue(45);
                break;
            case PICTURE_PATTERN2:
                model.red.setValue(5);
                model.green.setValue(45);
                model.blue.setValue(55);
                break;
            case PICTURE_PATTERN3:
                model.red.setValue(50);
                model.green.setValue(7);
                model.blue.setValue(56);
                break;
            case PICTURE_PATTERN4:
                model.red.setValue(5);
                model.green.setValue(45);
                model.blue.setValue(55);
                break;
            case PICTURE_PATTERN5:
                model.red.setValue(2);
                model.green.setValue(58);
                model.blue.setValue(45);
                break;
            case PICTURE_PATTERN6:
                model.red.setValue(2);
                model.green.setValue(58);
                model.blue.setValue(45);
                break;
            case PICTURE_PATTERN7:
                model.red.setValue(2);
                model.green.setValue(58);
                model.blue.setValue(45);
                break;
            case PICTURE_PATTERN8:
                model.red.setValue(25);
                model.green.setValue(49);
                model.blue.setValue(16);
                break;
            case PICTURE_PATTERN9:
                model.red.setValue(2);
                model.green.setValue(58);
                model.blue.setValue(45);
                break;
            case PICTURE_PATTERN10:
                model.red.setValue(51);
                model.green.setValue(25);
                model.blue.setValue(2);
                break;
            case PICTURE_PATTERN11:
                model.red.setValue(2);
                model.green.setValue(58);
                model.blue.setValue(45);
                break;
            case PICTURE_PATTERN12:
                model.red.setValue(2);
                model.green.setValue(58);
                model.blue.setValue(45);
                break;
            case PICTURE_PATTERN13:
                model.red.setValue(2);
                model.green.setValue(58);
                model.blue.setValue(45);
                break;
            default:
                model.red.setValue(60);
                model.green.setValue(60);
                model.blue.setValue(60);
                break;
        }
        return model;
    }

    /**
     * Display用イルミネーションカラー値の取得
     * <p>
     * 当メソッドは、イルミネーションカラーが
     * 個別設定できる(Disp/Keyが分かれている)車載器の場合使用すること
     *
     * @return イルミネーションカラー設定値
     */
    @NotNull
    public IlluminationColorModel getIlluminationDisplayColor() {
        IlluminationColorModel model = new IlluminationColorModel();

        switch (this) {
            case VIDEO_PATTERN1:
                model.red.setValue(41);
                model.green.setValue(60);
                model.blue.setValue(60);
                break;
            case VIDEO_PATTERN2:
                model.red.setValue(41);
                model.green.setValue(60);
                model.blue.setValue(60);
                break;
            case VIDEO_PATTERN3:
                model.red.setValue(6);
                model.green.setValue(54);
                model.blue.setValue(36);
                break;
            case VIDEO_PATTERN4:
                model.red.setValue(0);
                model.green.setValue(28);
                model.blue.setValue(60);
                break;
            case PICTURE_PATTERN1:
                model.red.setValue(30);
                model.green.setValue(60);
                model.blue.setValue(55);
                break;
            case PICTURE_PATTERN2:
                model.red.setValue(45);
                model.green.setValue(60);
                model.blue.setValue(40);
                break;
            case PICTURE_PATTERN3:
                model.red.setValue(45);
                model.green.setValue(4);
                model.blue.setValue(14);
                break;
            case PICTURE_PATTERN4:
                model.red.setValue(0);
                model.green.setValue(6);
                model.blue.setValue(60);
                break;
            case PICTURE_PATTERN5:
                model.red.setValue(30);
                model.green.setValue(60);
                model.blue.setValue(55);
                break;
            case PICTURE_PATTERN6:
                model.red.setValue(30);
                model.green.setValue(60);
                model.blue.setValue(55);
                break;
            case PICTURE_PATTERN7:
                model.red.setValue(30);
                model.green.setValue(60);
                model.blue.setValue(55);
                break;
            case PICTURE_PATTERN8:
                model.red.setValue(0);
                model.green.setValue(60);
                model.blue.setValue(0);
                break;
            case PICTURE_PATTERN9:
                model.red.setValue(30);
                model.green.setValue(60);
                model.blue.setValue(55);
                break;
            case PICTURE_PATTERN10:
                model.red.setValue(60);
                model.green.setValue(4);
                model.blue.setValue(0);
                break;
            case PICTURE_PATTERN11:
                model.red.setValue(30);
                model.green.setValue(60);
                model.blue.setValue(55);
                break;
            case PICTURE_PATTERN12:
                model.red.setValue(30);
                model.green.setValue(60);
                model.blue.setValue(55);
                break;
            case PICTURE_PATTERN13:
                model.red.setValue(30);
                model.green.setValue(60);
                model.blue.setValue(55);
                break;
            default:
                model.red.setValue(60);
                model.green.setValue(60);
                model.blue.setValue(60);
                break;
        }
        return model;
    }

    /**
     * Key用イルミネーションカラー値の取得
     * <p>
     * 当メソッドは、イルミネーションカラーが
     * 個別設定できる(Disp/Keyが分かれている)車載器の場合使用すること
     *
     * @return イルミネーションカラー設定値
     */
    @NotNull
    public IlluminationColorModel getIlluminationKeyColor() {
        IlluminationColorModel model = new IlluminationColorModel();

        switch (this) {
            case VIDEO_PATTERN1:
                model.red.setValue(31);
                model.green.setValue(0);
                model.blue.setValue(45);
                break;
            case VIDEO_PATTERN2:
                model.red.setValue(31);
                model.green.setValue(0);
                model.blue.setValue(45);
                break;
            case VIDEO_PATTERN3:
                model.red.setValue(6);
                model.green.setValue(54);
                model.blue.setValue(36);
                break;
            case VIDEO_PATTERN4:
                model.red.setValue(0);
                model.green.setValue(28);
                model.blue.setValue(60);
                break;
            case PICTURE_PATTERN1:
                model.red.setValue(20);
                model.green.setValue(6);
                model.blue.setValue(53);
                break;
            case PICTURE_PATTERN2:
                model.red.setValue(0);
                model.green.setValue(6);
                model.blue.setValue(60);
                break;
            case PICTURE_PATTERN3:
                model.red.setValue(20);
                model.green.setValue(60);
                model.blue.setValue(53);
                break;
            case PICTURE_PATTERN4:
                model.red.setValue(30);
                model.green.setValue(60);
                model.blue.setValue(55);
                break;
            case PICTURE_PATTERN5:
                model.red.setValue(30);
                model.green.setValue(60);
                model.blue.setValue(55);
                break;
            case PICTURE_PATTERN6:
                model.red.setValue(20);
                model.green.setValue(6);
                model.blue.setValue(53);
                break;
            case PICTURE_PATTERN7:
                model.red.setValue(60);
                model.green.setValue(9);
                model.blue.setValue(0);
                break;
            case PICTURE_PATTERN8:
                model.red.setValue(0);
                model.green.setValue(60);
                model.blue.setValue(0);
                break;
            case PICTURE_PATTERN9:
                model.red.setValue(2);
                model.green.setValue(58);
                model.blue.setValue(30);
                break;
            case PICTURE_PATTERN10:
                model.red.setValue(60);
                model.green.setValue(0);
                model.blue.setValue(0);
                break;
            case PICTURE_PATTERN11:
                model.red.setValue(2);
                model.green.setValue(58);
                model.blue.setValue(30);
                break;
            case PICTURE_PATTERN12:
                model.red.setValue(60);
                model.green.setValue(9);
                model.blue.setValue(0);
                break;
            case PICTURE_PATTERN13:
                model.red.setValue(20);
                model.green.setValue(6);
                model.blue.setValue(53);
                break;
            default:
                model.red.setValue(60);
                model.green.setValue(60);
                model.blue.setValue(60);
                break;
        }
        return model;
    }

    /**
     * UIカラーの取得
     *
     * @return UIカラー
     */
    @NotNull
    public UiColor getUIColor() {
        switch (this) {
            case VIDEO_PATTERN1:
                return UiColor.RED;
            case VIDEO_PATTERN2:
                return UiColor.AQUA;
            case VIDEO_PATTERN3:
                return UiColor.GREEN;
            case VIDEO_PATTERN4:
                return UiColor.BLUE;
            case PICTURE_PATTERN1:
                return UiColor.AQUA;
            case PICTURE_PATTERN2:
                return UiColor.BLUE;
            case PICTURE_PATTERN3:
                return UiColor.PINK;
            case PICTURE_PATTERN4:
                return UiColor.BLUE;
            case PICTURE_PATTERN5:
                return UiColor.AQUA;
            case PICTURE_PATTERN6:
                return UiColor.AQUA;
            case PICTURE_PATTERN7:
                return UiColor.AMBER;
            case PICTURE_PATTERN8:
                return UiColor.GREEN;
            case PICTURE_PATTERN9:
                return UiColor.AQUA;
            case PICTURE_PATTERN10:
                return UiColor.AMBER;
            case PICTURE_PATTERN11:
                return UiColor.AQUA;
            case PICTURE_PATTERN12:
                return UiColor.AQUA;
            case PICTURE_PATTERN13:
                return UiColor.AQUA;
            default:
                return UiColor.YELLOW;
        }
    }

    /**
     * 車載器発光パターン取得
     *
     * @return 発光パターン
     */
    @NotNull
    public FlashPattern getFlashPattern() {
        switch (this) {
            case VIDEO_PATTERN1:
                return FlashPattern.BGV1;
            case VIDEO_PATTERN2:
                return FlashPattern.BGV2;
            case VIDEO_PATTERN3:
                return FlashPattern.BGV3;
            case VIDEO_PATTERN4:
                return FlashPattern.BGV4;
            case PICTURE_PATTERN1:
                return FlashPattern.BGP1;
            case PICTURE_PATTERN2:
                return FlashPattern.BGP2;
            case PICTURE_PATTERN3:
                return FlashPattern.BGP3;
            case PICTURE_PATTERN4:
                return FlashPattern.BGP4;
            case PICTURE_PATTERN5:
                return FlashPattern.BGP5;
            case PICTURE_PATTERN6:
                return FlashPattern.BGP6;
            case PICTURE_PATTERN7:
                return FlashPattern.BGP7;
            case PICTURE_PATTERN8:
                return FlashPattern.BGP8;
            case PICTURE_PATTERN9:
                return FlashPattern.BGP9;
            case PICTURE_PATTERN10:
                return FlashPattern.BGP10;
            case PICTURE_PATTERN11:
                return FlashPattern.BGP11;
            case PICTURE_PATTERN12:
                return FlashPattern.BGP12;
            case PICTURE_PATTERN13:
                return FlashPattern.BGP1;
            default:
                return FlashPattern.BGP1;
        }
    }
}
