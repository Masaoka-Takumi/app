package jp.pioneer.carsync.presentation.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import timber.log.Timber;

/**
 * カスタムキー割当画面表示用データを格納するクラス
 */
public class CustomKeyItem {
    private CustomKey mCustomKey;//アクション種別
    private int mIcon;//アイコン 3rdApp以外でセット
    private int mNameId;//名前　3rdApp以外でセット
    private MediaSourceType mDirectSource;//ダイレクトソース
    private AppSharedPreference.Application mApplication;//3rdAppのApplicationInfo
    // ダイレクトソース切替に対応したアイコンと文字列のリソースID
    private static final Map<MediaSourceType, int[]> DIRECT_SOURCE_INFO = Collections.unmodifiableMap(new HashMap<MediaSourceType, int[]>(){
        {
            put(MediaSourceType.SIRIUS_XM, new int[]{ R.drawable.p1901_siriusxm_icon, R.string.src_003 });
            put(MediaSourceType.DAB, new int[]{ R.drawable.p1902_dab_icon, R.string.src_014 });
            put(MediaSourceType.RADIO, new int[]{ R.drawable.p1903_radio_icon, R.string.src_004 });
            put(MediaSourceType.HD_RADIO, new int[]{ R.drawable.p1904_hd_icon, R.string.src_015 });
            put(MediaSourceType.CD, new int[]{ R.drawable.p1905_cd_icon, R.string.src_005 });
            put(MediaSourceType.APP_MUSIC, new int[]{ R.drawable.p1906_app_music_icon, R.string.src_006 });
            put(MediaSourceType.USB, new int[]{ R.drawable.p1907_usb_icon, R.string.src_007 });
            put(MediaSourceType.PANDORA, new int[]{ R.drawable.p1908_pandora_icon, R.string.src_008 });
            put(MediaSourceType.SPOTIFY, new int[]{ R.drawable.p1909_spotify_icon, R.string.src_009 });
            put(MediaSourceType.AUX, new int[]{ R.drawable.p1910_aux_icon, R.string.src_010 });
            put(MediaSourceType.TI, new int[]{ R.drawable.p1911_ti_icon , R.string.src_011 });
            put(MediaSourceType.BT_AUDIO, new int[]{ R.drawable.p1912_bt_con, R.string.src_012 });
        }
    });

    public CustomKeyItem(){
        this.mCustomKey = null;
        this.mIcon = 0;
        this.mNameId = 0;
        this.mDirectSource = null;
        this.mApplication = null;
    }

    private CustomKeyItem(CustomKey customKey, int icon, int nameId, MediaSourceType directSource, AppSharedPreference.Application application) {
        this.mCustomKey = customKey;
        this.mIcon = icon;
        this.mNameId = nameId;
        this.mDirectSource = directSource;
        this.mApplication = application;
    }

    /**
     * ソース切替用インスタンス取得
     *
     * @return CustomKeyItem
     */
    public static CustomKeyItem newSourceChangeInstance(){
        return new CustomKeyItem(CustomKey.SOURCE_CHANGE, 0, R.string.cus_001, null, null);
    }

    /**
     * ソースOFF/ON用インスタンス取得
     *
     * @return CustomKeyItem
     */
    public static CustomKeyItem newSourceOnOffInstance(){
        return new CustomKeyItem(CustomKey.SOURCE_ON_OFF, 0, R.string.cus_002, null, null);
    }

    /**
     * ソース一覧表示用インスタンス取得
     *
     * @return CustomKeyItem
     */
    public static CustomKeyItem newSourceListInstance(){
        return new CustomKeyItem(CustomKey.SOURCE_LIST, 0, R.string.cus_003, null, null);
    }

    /**
     * ダイレクトソース切替用インスタンス取得
     * 対応するCustomKeyItemがない場合はnullが返る
     *
     * @param mediaSourceType
     * @return CustomKeyItem
     */
    public static CustomKeyItem newSourceDirectInstance(MediaSourceType mediaSourceType){
        if(!DIRECT_SOURCE_INFO.containsKey(mediaSourceType)){
            Timber.w("MediaSourceType[%s] is not supported at CustomKeyItem.", mediaSourceType);
            return null;
        }
        int icon = DIRECT_SOURCE_INFO.get(mediaSourceType)[0];
        int nameId = DIRECT_SOURCE_INFO.get(mediaSourceType)[1];
        return new CustomKeyItem(CustomKey.SOURCE_DIRECT, icon, nameId, mediaSourceType, null);
    }

    /**
     * 3rd App用インスタンス取得
     *
     * @param application
     * @return CustomKeyItem
     */
    public static CustomKeyItem newThirdAppInstance(AppSharedPreference.Application application){
        return new CustomKeyItem(CustomKey.THIRD_PARTY_APP, 0, 0, null, application);
    }

    // getter
    public CustomKey getCustomKey() {
        return mCustomKey;
    }
    public int getNameId(){
        return this.mNameId;
    }
    public int getIcon(){
        return this.mIcon;
    }
    public MediaSourceType getDirectSource() {
        return mDirectSource;
    }
    public AppSharedPreference.Application getApplication() {
        return mApplication;
    }
}
