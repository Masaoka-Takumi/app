package jp.pioneer.carsync.domain.model;

import android.support.annotation.IntRange;

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * ゾーンカラースペック.
 */
public class ZoneColorSpec {
    /** 赤成分. */
    public int red;
    /** 緑成分. */
    public int green;
    /** 青成分. */
    public int blue;

    /**
     * コンストラクタ.
     */
    public ZoneColorSpec() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        red = 60;
        green = 60;
        blue = 60;
    }

    /**
     * 値設定.
     * <p>
     * 色の設定値が0~60の数値であること
     * {@code red}、{@code green}、{@code blue}のどれか1つが10以上であること。
     *
     * @param red 赤
     * @param green 緑
     * @param blue 青
     * @throws IllegalArgumentException {@code red}、{@code green}、{@code blue}が不正
     */
    public void setValue(@IntRange(from = 0, to = 60) int red,
                         @IntRange(from = 0, to = 60) int green,
                         @IntRange(from = 0, to = 60) int blue) {
        checkArgument(red >= 0 && red <= 60);
        checkArgument(green >= 0 && green <= 60);
        checkArgument(blue >= 0 && blue <= 60);
        checkArgument(Math.max(red, Math.max(green, blue)) >= 10);

        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /**
     * 設定値が有効か否か取得.
     *
     * @return {@code true}:有効である。{@code false}:それ以外。
     */
    public boolean isValid() {
        return (0 <= red && red <= 60)
                && (0 <= green && green <= 60)
                && (0 <= blue && blue <= 60)
                && (Math.max(red, Math.max(green, blue)) >= 10);
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
