package jp.pioneer.carsync.presentation.model;

import com.google.gson.annotations.SerializedName;

/**
 * TIPSカテゴリー.
 */
public class TipsCategory {

    /** カテゴリーID. */
    @SerializedName("category_id")
    public int id;

    /** カテゴリー名. */
    @SerializedName("category_name")
    public String name;

    /** カテゴリースラッグ. */
    @SerializedName("category_slug")
    public String slug;

    /**
     * コンストラクタ.
     * <p>
     * Gsonを使用してJsonファイルから本クラスに変換するため、
     * 基本的には使用しない。
     */
    public TipsCategory(int id,
                        String name,
                        String slug) {
        this.id = id;
        this.name = name;
        this.slug = slug;
    }
}
