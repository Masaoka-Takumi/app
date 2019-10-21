package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.OnClick;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.presenter.Presenter;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * 再生画面の抽象クラス
 */

public abstract class AbstractPlayerFragment<P extends Presenter<V>, V> extends AbstractScreenFragment {
    /** 有効時の透過度. */
    protected static final float ENABLE_ALPHA = 1.0f;
    /** 無効時の透過度. */
    protected static final float DISABLE_ALPHA = 0.4f;
    /** ジェスチャー表示のディレイ時間. */
    protected static final int GESTURE_DELAY_TIME = 500;
    /** メッセージ表示のディレイ時間. */
    protected static final int MESSAGE_DELAY_TIME = 1500;
    /** アンテナ強度表現の画像数. */
    protected static final int ANTENNA_LEVEL_COUNT = 9;
    /** アンテナリスト. */
    protected static final SparseIntArray ANTENNA_LIST = new SparseIntArray(){{
        put(0, R.drawable.p0490_antenna);
        put(1, R.drawable.p0491_antenna);
        put(2, R.drawable.p0492_antenna);
        put(3, R.drawable.p0493_antenna);
        put(4, R.drawable.p0494_antenna);
        put(5, R.drawable.p0495_antenna);
        put(6, R.drawable.p0496_antenna);
        put(7, R.drawable.p0497_antenna);
        put(8, R.drawable.p0498_antenna);
    }};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * HOMEボタン押下イベント
     */
    @OnClick(R.id.home_button)
    public abstract void onClickHomeButton();

    /**
     * 設定ボタン押下イベント
     */
    @OnClick(R.id.player_setting_button)
    public abstract void onClickSettingButton();

    /**
     * ソース切換ボタン押下イベント
     */
    @OnClick(R.id.source_button)
    public abstract void onClickSourceButton();

    /**
     * 視覚効果ボタン押下イベント
     */
    @OnClick(R.id.visualizer_button)
    public abstract void onClickVisualizerButton();
}
