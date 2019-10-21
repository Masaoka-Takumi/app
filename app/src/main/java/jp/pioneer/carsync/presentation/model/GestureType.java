package jp.pioneer.carsync.presentation.model;

import java.util.ArrayList;
import java.util.Arrays;

import jp.pioneer.carsync.R;

/**
 * ジェスチャー種別.
 * <p>
 * ジェスチャー画面に表示する内容を定義
 *
 */
public enum GestureType {
    /** トラックUP. */
    TRACK_UP(R.drawable.p1040_gesture_ff, R.drawable.p1041_gesture_ff_h),
    /** トラックDOWN. */
    TRACK_DOWN(R.drawable.p1030_gesture_rew, R.drawable.p1031_gesture_rew_h),
    /** Play. */
    PLAY(R.drawable.p1010_gesture_play, R.drawable.p1011_gesture_play_h),
    /** Pause. */
    PAUSE(R.drawable.p1020_gesture_pause, R.drawable.p1021_gesture_pause_h),
    /** ボリュームUP. */
    VOLUME_UP(R.drawable.p1062_gesture_volup, R.drawable.p1063_gesture_volup_h),
    /** ボリュームDOWN. */
    VOLUME_DOWN(R.drawable.p1064_gesture_voldown, R.drawable.p1065_gesture_voldown_h),
    /** P.CH UP. */
    PCH_UP(R.string.ply_065),
    /** P.CH DOWN. */
    PCH_DOWN(R.string.ply_066),
    /** FREQUENCY UP. */
    FREQUENCY_UP(R.string.ply_074),
    /** FREQUENCY DOWN. */
    FREQUENCY_DOWN(R.string.ply_075),
    /** チャンネルUP. */
    CHANNEL_UP(R.string.ply_070),
    /** チャンネルDOWN. */
    CHANNEL_DOWN(R.string.ply_071),
    /** SCAN UP. */
    SCAN_UP(R.string.ply_072),
    /** SCAN DOWN. */
    SCAN_DOWN(R.string.ply_073),
    /** MANUAL UP. */
    MANUAL_UP(R.string.ply_067),
    /** MANUAL DOWN. */
    MANUAL_DOWN(R.string.ply_068),
    /** SEEK. */
    SEEK(R.string.ply_069),
    /** LIST UPDATE. */
    LIST_UPDATE(R.string.ply_096)
    ;

    /**
     * リソースid群.
     * <p>
     * size = 1:テキスト表示
     * size = 2:画像表示
     * <p>
     * テキスト表示 [0]:表示するテキスト内容
     * 画像表示     [0]:Base [1]:Icon
     */
    public final ArrayList<Integer> ids;

    /**
     * コンストラクタ.
     *
     * @param ids リソースid群
     */
    GestureType(Integer... ids){
        this.ids = new ArrayList<>();
        this.ids.addAll(Arrays.asList(ids));
    }

    /**
     * テキスト表示対象か否か.
     */
    public boolean isDisplayText(){
        return this.ids.size() == 1;
    }

    /**
     * 画像表示対象か否か.
     */
    public boolean isDisplayImg(){
        return this.ids.size() == 2;
    }
}
