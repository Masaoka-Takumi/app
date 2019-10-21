package jp.pioneer.carsync.presentation.view;


/**
 * 起動画面の抽象クラス
 */

public interface OpeningView {

    /**
     * アニメーション開始.
     * <p>
     * onCreateでアニメーションを実行すると、
     * 画面回転によって再度アニメーションを実施してしまうため、
     * Fragment表示後1回のみ実行するためにPresenterから呼び出す。
     */
    void startAnimation();

    /**
     * アニメーション停止.
     * <p>
     * 画面回転やアプリがバックグラウンドに移動した場合に呼び出し、
     * ImageViewを静止画に切り替える。
     */
    void stopAnimation();
}
