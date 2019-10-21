package jp.pioneer.carsync.presentation.model;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

/**
 * TIPSタグ.
 * <p>
 * 記事の絞り込みは{@link #slug}で行う
 */
public class TipsTag {

    /** タグID. */
    @SerializedName("tag_id")
    public int id;

    /** タグ名. */
    @SerializedName("tag_name")
    public String name;

    /** タグスラッグ. */
    @SerializedName("tag_slug")
    public String slug;

    /** タグアイコン画像URL. */
    @SerializedName("icon_url")
    public String iconUrl;

    /** タグアイコン画像. */
    @Nullable
    public Bitmap iconImage;

    /**
     * コンストラクタ.
     * <p>
     * Gsonを使用してJsonファイルから本クラスに変換するため、
     * 基本的には使用しない。
     */
    public TipsTag(int id,
                   String name,
                   String slug,
                   String iconUrl) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.iconUrl = iconUrl;
    }
}
