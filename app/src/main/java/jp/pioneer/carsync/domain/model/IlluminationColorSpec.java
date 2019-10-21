package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkState;

/**
 * イルミCOLOR設定色.
 * <p>
 * RGBの各成分は使用可能な色の場合は0～60の範囲となる。
 * 車載機で使用出来ない場合は全て255となる。
 */
public class IlluminationColorSpec {
    /** 赤成分. */
    public int red;
    /** 緑成分. */
    public int green;
    /** 青成分. */
    public int blue;

    /**
     * コンストラクタ.
     */
    public IlluminationColorSpec() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        red = 255;
        green = 255;
        blue = 255;
    }

    /**
     * 色設定.
     *
     * @param red 赤
     * @param green 緑
     * @param blue 青
     */
    public void setValue(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * 色取得.
     * <p>
     * RGBの成分を0～255の範囲に変換した値を返す。
     * α成分は255である。
     *
     * @return 0～255の範囲に変換した色（0xFF{@code RRGGBB}）
     * @throws IllegalStateException 色の設定値が無効
     */
    public int getColor() {
        checkState(isValid());

        int r = (int) ((float)(red * 255) / 60);
        int g = (int) ((float)(green * 255) / 60);
        int b = (int) ((float)(blue * 255) / 60);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /**
     * 色の設定値が有効か否か取得.
     *
     * @return {@code true}:有効である。{@code false}:それ以外。
     */
    public boolean isValid() {
        return (0 <= red && red <= 60)
                && (0 <= green && green <= 60)
                && (0 <= blue && blue <= 60);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("red", red)
                .add("green", green)
                .add("blue", blue)
                .toString();
    }
}
