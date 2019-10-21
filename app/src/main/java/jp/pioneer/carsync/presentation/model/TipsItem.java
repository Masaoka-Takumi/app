package jp.pioneer.carsync.presentation.model;

import android.graphics.Bitmap;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

/**
 * TIPS記事.
 */
public class TipsItem {

    /** 記事ID. */
    public int id;

    /** 記事URL. */
    public String link;

    /** タイトル. */
    public String title;

    /** サムネイル画像URL. */
    @SerializedName("thumbnail")
    public String thumbUrl;

    /** サムネイル画像. */
    @Nullable
    public Bitmap thumbImage;

    /** カテゴリー. */
    @SerializedName("category")
    public TipsCategory[] categories;

    /** タグ. */
    @SerializedName("tag")
    public TipsTag[] tags;

    /** 説明. */
    public String description;

    /**
     * コンストラクタ.
     * <p>
     * Gsonを使用してJsonファイルから本クラスに変換するため、
     * 基本的には使用しない。
     */
    public TipsItem(int id,
                    String link,
                    String title,
                    String thumbUrl,
                    TipsCategory[] categories,
                    TipsTag[] tags,
                    String description){
        this.id = id;
        this.link = link;
        this.title = title;
        this.thumbUrl = thumbUrl;
        this.categories = categories.clone();
        this.tags = tags.clone();
        this.description = description;
    }
}
