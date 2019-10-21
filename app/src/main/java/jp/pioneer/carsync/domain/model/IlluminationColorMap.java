package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;

import com.annimon.stream.Stream;
import com.google.common.base.MoreObjects;

import java.util.EnumMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * イルミCOLOR設定色マップ.
 * <p>
 * RGB要素で表現されるイルミネーションの色設定辞書。
 */
public class IlluminationColorMap {
    private Map<IlluminationColor, IlluminationColorSpec> mMap = new EnumMap<>(IlluminationColor.class);

    /**
     * コンストラクタ.
     */
    public IlluminationColorMap() {
        Stream.of(IlluminationColor.values())
                .forEach(color -> mMap.put(color, new IlluminationColorSpec()));
    }

    /**
     * イルミCOLOR設定色取得.
     *
     * @param key イルミ設定色種別
     * @return イルミCOLOR設定色
     * @throws NullPointerException {@code key}がnull
     */
    public IlluminationColorSpec get(@NonNull IlluminationColor key) {
        return mMap.get(checkNotNull(key));
    }

    /**
     * リセット.
     */
    public void reset() {
        Stream.of(mMap)
                .forEach(entry -> entry.getValue().reset());
    }

    /**
     * 指定した色の設定色が全て有効か否か取得.
     *
     * @param colors 有効かを調べる色
     * @return {@code true}:全て有効。{@code false}:それ以外。
     * @throws NullPointerException {@code colors}がnull
     */
    public boolean isAllColorValid(@NonNull IlluminationColor[] colors) {
        for (IlluminationColor color : colors) {
            IlluminationColorSpec spec = mMap.get(color);
            if (spec == null || !spec.isValid()) {
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .addValue(mMap)
                .toString();
    }
}
