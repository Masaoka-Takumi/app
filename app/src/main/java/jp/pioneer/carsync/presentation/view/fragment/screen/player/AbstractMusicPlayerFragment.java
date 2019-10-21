package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.OnClick;
import butterknife.Optional;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.presentation.presenter.Presenter;
import timber.log.Timber;

/**
 * Music再生画面抽象クラス
 */

public abstract class AbstractMusicPlayerFragment<P extends Presenter<V>, V> extends AbstractPlayerFragment {

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
     * Fxボタン押下
     */
    @OnClick(R.id.fx_button)
    public abstract void onClickFxButton();

    /**
     * リピートアイコン押下
     *
     * @param view ビュー
     */
    @OnClick(R.id.repeat_button)
    public void onClickRepeat(View view) {
        Timber.d("OnClick Repeat ...");
    }

    /**
     * シャッフルアイコン押下
     *
     * @param view ビュー
     */
    @OnClick(R.id.shuffle_button)
    public void onClickShuffle(View view) {
        Timber.d("OnClick Shuffle ...");
    }

    /**
     * 設定アイコン押下
     *
     * @param view ビュー
     */
    @Optional
    public void onClickSetting(View view) {
        Timber.d("OnClick Setting ...");
    }

    // 再生時間＆残り時間のテキストラベルを更新する
    protected void updateTimeLabel(int contentMaxSeconds, int currentPositionSeconds, TextView currentTimeView, TextView remainingTimeView) {
        int remainingSecond = contentMaxSeconds - currentPositionSeconds;
        if (remainingSecond < 0) {
            // 本ケースがあるかどうか分からないが念のため
            remainingSecond = 0;
        }

        int currentMinute = currentPositionSeconds / 60;
        int currentSecond = currentPositionSeconds % 60;
        String currentFormatString = String.format("%d:%02d", currentMinute, currentSecond);

        int remainingMinute = remainingSecond / 60;
        int remainingSecond60 = remainingSecond % 60;
        String remainingFormatString = String.format("-%d:%02d", remainingMinute, remainingSecond60);

        currentTimeView.setText(currentFormatString);
        remainingTimeView.setText(remainingFormatString);
    }
}
