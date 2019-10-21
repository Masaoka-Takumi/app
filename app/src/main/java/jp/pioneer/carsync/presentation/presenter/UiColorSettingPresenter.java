package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;

import com.annimon.stream.Optional;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.presentation.model.UiColor;
import jp.pioneer.carsync.presentation.view.UiColorSettingView;

/**
 * UIカラー設定画面のpresenter
 */
@PresenterLifeCycle
public class UiColorSettingPresenter extends Presenter<UiColorSettingView> {
    private final UiColor[] UI_COLOR_RESOURCE = new UiColor[]{
            UiColor.BLUE,
            UiColor.AQUA,
            UiColor.GREEN,
            UiColor.YELLOW,
            UiColor.AMBER,
            UiColor.RED,
            UiColor.PINK,
    };
    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;

    /**
     * コンストラクタ
     */
    @Inject
    public UiColorSettingPresenter() {
    }

    @Override
    void onTakeView() {
        UiColor color = mPreference.getUiColor();

        List<UiColor> list = Arrays.asList(UI_COLOR_RESOURCE);
        int position = list.indexOf(color);

        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setColor(list);
            view.setPosition(position, color.getResource());
        });
    }

    /**
     * カラー選択時処理
     *
     * @param position 選択位置
     */
    public void onSelectColorItemAction(int position) {
        mPreference.setUiColor(UI_COLOR_RESOURCE[position]);
        Optional.ofNullable(getView()).ifPresent(view -> view.setPosition(position, UI_COLOR_RESOURCE[position].getResource()));
        int theme;
        switch(UI_COLOR_RESOURCE[position]){
            case BLUE:
                theme = R.style.AppTheme_Blue;
                break;
            case AQUA:
                theme = R.style.AppTheme_Aqua;
                break;
            case GREEN:
                theme = R.style.AppTheme_Green;
                break;
            case YELLOW:
                theme = R.style.AppTheme_Yellow;
                break;
            case AMBER:
                theme = R.style.AppTheme_Amber;
                break;
            case RED:
                theme = R.style.AppTheme_Red;
                break;
            case PINK:
                theme = R.style.AppTheme_Pink;
                break;
            default:
                theme = R.style.AppTheme_Blue;
                break;
        }
        Optional.ofNullable(getView()).ifPresent(view -> view.setTheme(theme));

    }

}
