package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.OnClick;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.presenter.Presenter;

/**
 * ラジオ系画面の抽象クラス
 */

public abstract class AbstractRadioFragment<P extends Presenter<V>, V> extends AbstractPlayerFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * ジャケット画像押下
     */
    @OnClick(R.id.jacket_view)
    public abstract void onClickJacketView();

    /**
     * お気に入りボタン押下イベント
     */
    @OnClick(R.id.favorite_view)
    public abstract void onClickFavoriteButton();

    /**
     * Fxボタン押下
     */
    @OnClick(R.id.fx_button)
    public abstract void onClickFxButton();
}
