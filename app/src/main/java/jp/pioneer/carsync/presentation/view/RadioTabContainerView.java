package jp.pioneer.carsync.presentation.view;

import android.support.annotation.ColorRes;

import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.presentation.presenter.RadioTabContainerPresenter;
import jp.pioneer.carsync.presentation.view.fragment.OnNavigateListener;

/**
 * ラジオリスト画面コンテナのinterface
 */

public interface RadioTabContainerView extends OnNavigateListener {
    void setTabVisible(boolean isVisible);
    void setTabLayout(MediaSourceType type,boolean isSph);
    /**
     * 表示中タブタイトルの設定
     *
     * @param title タブタイトル
     */
    void setTitle(String title);

    /**
     * BSMボタン表示の設定
     *
     * @param isVisible
     */
    void setBsmButtonVisible(boolean isVisible);

    /**
     * BSMボタンの有効設定.
     *
     * @param isEnabled 有効か否か
     */
    void setBsmButtonEnabled(boolean isEnabled);
    void setUpdateButtonVisible(boolean isVisible);
    void setUpdateButtonEnabled(boolean isEnabled);

    /**
     * タブボタン表示の設定
     *
     * @param tab 現在のタブ
     */
    void setTab(RadioTabContainerPresenter.RadioTabType tab);

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    void setColor(@ColorRes int color);

    /**
     * BSM、PTYSearch中のView表示/非表示
     *
     * @param isShow 表示/非表示
     * @param type タイプ
     */
    void showStatusView(boolean isShow, String type);

    /**
     * ×ボタン表示の設定
     *
     * @param isVisible
     */
    void setCloseButtonVisible(boolean isVisible);

    void closeDialog();

}
