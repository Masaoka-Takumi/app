package jp.pioneer.carsync.domain.model;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import jp.pioneer.carsync.R;

/**
 * 発光パターン.
 */
public enum FlashPattern {
    /** BGV1. */
    BGV1(R.raw.flash_pattern_bgv001),
    /** BGV2. */
    BGV2(R.raw.flash_pattern_bgv002),
    /** BGV3. */
    BGV3(R.raw.flash_pattern_bgv003),
    /** BGV4. */
    BGV4(R.raw.flash_pattern_bgv004),
    /** BGP1. */
    BGP1(R.raw.flash_pattern_bgp001),
    /** BGP2. */
    BGP2(R.raw.flash_pattern_bgp002),
    /** BGP3. */
    BGP3(R.raw.flash_pattern_bgp003),
    /** BGP4. */
    BGP4(R.raw.flash_pattern_bgp004),
    /** BGP5. */
    BGP5(R.raw.flash_pattern_bgp005),
    /** BGP6. */
    BGP6(R.raw.flash_pattern_bgp006),
    /** BGP7. */
    BGP7(R.raw.flash_pattern_bgp007),
    /** BGP8. */
    BGP8(R.raw.flash_pattern_bgp008),
    /** BGP9. */
    BGP9(R.raw.flash_pattern_bgp009),
    /** BGP10. */
    BGP10(R.raw.flash_pattern_bgp010),
    /** BGP11. */
    BGP11(R.raw.flash_pattern_bgp011),
    /** BGP12. */
    BGP12(R.raw.flash_pattern_bgp012),
    ;

    /** 発光パターンJSONファイルのリソースid. */
    private int mResourceId;

    /**
     * コンストラクタ.
     *
     * @param resId 発光パターンJSONファイルのリソースid
     */
    FlashPattern(int resId) {
        mResourceId = resId;
    }

    /**
     * フレーム情報取得.
     *
     * @param context リソースを取得するためのContext
     * @return フレーム情報
     */
    @NonNull
    public ArrayList<ZoneFrameInfo> get(Context context) {
        InputStream input;
        try {
            // file読み込み
            input = context.getResources().openRawResource(mResourceId);
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            // Json読込み
            String json = new String(buffer, "UTF-8");
            ZoneFrameInfo[] frames = new Gson().fromJson(json, ZoneFrameInfo[].class);
            if(frames.length>0) {
                return new ArrayList<>(Arrays.asList(frames));
            }else {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
