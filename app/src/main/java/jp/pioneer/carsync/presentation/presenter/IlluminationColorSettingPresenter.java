package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.IlluminationSettingChangeEvent;
import jp.pioneer.carsync.domain.event.IlluminationSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferIllumination;
import jp.pioneer.carsync.domain.model.IlluminationColor;
import jp.pioneer.carsync.domain.model.IlluminationColorMap;
import jp.pioneer.carsync.domain.model.IlluminationColorSpec;
import jp.pioneer.carsync.domain.model.IlluminationSettingStatus;
import jp.pioneer.carsync.domain.model.IlluminationTarget;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.model.IlluminationColorModel;
import jp.pioneer.carsync.presentation.view.IlluminationColorSettingView;
import jp.pioneer.carsync.presentation.view.argument.IlluminationColorParams;

/**
 * イルミネーションカラー設定画面のpresenter
 */
@PresenterLifeCycle
public class IlluminationColorSettingPresenter extends Presenter<IlluminationColorSettingView> {
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject IlluminationColorModel mModel;
    @Inject GetStatusHolder mGetCase;
    @Inject PreferIllumination mIllumiCase;
    private List<IlluminationColor> mPresetColors;
    private IlluminationColorParams.IlluminationType mIllumiType = IlluminationColorParams.IlluminationType.DISP;

    @Inject
    public IlluminationColorSettingPresenter() {
    }

    /**
     * 引き継ぎ
     *
     * @param argument Bundle(IlluminationColorParams)
     */
    public void setArgument(Bundle argument) {
        IlluminationColorParams params = IlluminationColorParams.from(argument);
        mIllumiType = params.type;
    }

    @Override
    void onTakeView() {
        StatusHolder holder = mGetCase.execute();

        IlluminationColorMap colorMap;
        switch (mIllumiType) {
            case COMMON:
                colorMap = holder.getIlluminationSetting().commonColorSpec;
                break;
            case DISP:
                colorMap = holder.getIlluminationSetting().dispColorSpec;
                break;
            case KEY:
                colorMap = holder.getIlluminationSetting().keyColorSpec;
                break;
            default:
                colorMap = holder.getIlluminationSetting().commonColorSpec;
                break;
        }
        // preset
        mPresetColors = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (IlluminationColor color : IlluminationColor.values()) {
            switch (color) {
                case SCAN:
                case CUSTOM:
                case FOR_MY_CAR:
                    break; // プリセットには含まない
                default:
                    IlluminationColorSpec spec = colorMap.get(color);
                    if (spec != null && spec.isValid()) {
                        mPresetColors.add(color);
                        colors.add(Color.rgb(spec.red * 255 / 60, spec.green * 255 / 60, spec.blue * 255 / 60));
                    }
                    break;
            }
        }
        Optional.ofNullable(getView()).ifPresent(view -> view.setColor(colors));
        updateViews();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIlluminationSettingChangeEvent(IlluminationSettingChangeEvent event) {
        updateViews();
    }

    private void updateViews() {
        StatusHolder holder = mGetCase.execute();

        IlluminationColor curr;
        IlluminationColorMap colorMap;

        switch (mIllumiType) {
            case COMMON:
                curr = holder.getIlluminationSetting().commonColor;
                colorMap = holder.getIlluminationSetting().commonColorSpec;
                break;
            case DISP:
                curr = holder.getIlluminationSetting().dispColor;
                colorMap = holder.getIlluminationSetting().dispColorSpec;
                break;
            case KEY:
                curr = holder.getIlluminationSetting().keyColor;
                colorMap = holder.getIlluminationSetting().keyColorSpec;
                break;
            default:
                curr = holder.getIlluminationSetting().commonColor;
                colorMap = holder.getIlluminationSetting().commonColorSpec;
                break;
        }
        // current custom color
        IlluminationColorSpec custom = colorMap.get(IlluminationColor.CUSTOM);
        mModel.red.setValue(custom.red);
        mModel.green.setValue(custom.green);
        mModel.blue.setValue(custom.blue);
        Optional.ofNullable(getView()).ifPresent(view -> {
            int selectPosition;
            if(curr == IlluminationColor.SCAN){
                selectPosition = mPresetColors.size();
            }else {
                selectPosition = mPresetColors.indexOf(curr);
            }
            view.setPosition(selectPosition);
            view.setCustomColor(custom.red * 255 / 60, custom.green * 255 / 60, custom.blue * 255 / 60);
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIlluminationSettingStatusChangeEvent(IlluminationSettingStatusChangeEvent event) {
        setEnable();
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        setEnable();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    private void setEnable(){
        //イルミ設定有効無効切替
        StatusHolder holder = mGetCase.execute();
        IlluminationSettingStatus illuminationSettingStatus = holder.getIlluminationSettingStatus();
        Optional.ofNullable(getView()).ifPresent(view -> {
            switch (mIllumiType) {
                case COMMON:
                    view.setEnable(illuminationSettingStatus.commonColorSettingEnabled && illuminationSettingStatus.commonColorCustomSettingEnabled);
                    break;
                case DISP:
                    view.setEnable(illuminationSettingStatus.dispColorSettingEnabled && illuminationSettingStatus.colorCustomDispSettingEnabled);
                    break;
                case KEY:
                    view.setEnable(illuminationSettingStatus.keyColorSettingEnabled && illuminationSettingStatus.colorCustomDispSettingEnabled);
                    break;
            }
        });
    }

    /**
     * プリセットカラー選択処理
     *
     * @param position 選択位置
     */
    public void onSelectColorItemAction(int position) {
        Optional.ofNullable(getView()).ifPresent(view -> view.setPosition(position));
        IlluminationColor color;
        if(position == mPresetColors.size()){
            color = IlluminationColor.SCAN;
        }else {
            color = mPresetColors.get(position);
        }
        switch (mIllumiType) {
            case COMMON:
                mIllumiCase.setCommonColor(color);
                break;
            case DISP:
                mIllumiCase.setColor(IlluminationTarget.DISP, color);
                break;
            case KEY:
                mIllumiCase.setColor(IlluminationTarget.KEY, color);
                break;
            default:
                mIllumiCase.setCommonColor(color);
                break;
        }
    }

    /**
     * カスタムカラー選択処理
     */
    public void onSelectCustomItemAction() {
        Optional.ofNullable(getView()).ifPresent(view -> view.setPosition(-1));
        setCustomColor();
    }

    /**
     * カスタムカラー変更処理
     *
     * @param red   赤要素(0-255)
     * @param green 緑要素(0-255)
     * @param blue  青要素(0-255)
     */
    public void onCustomColorAction(int red, int green, int blue) {
        mModel.red.setValue(red * 60 / 255);
        mModel.green.setValue(green * 60 / 255);
        mModel.blue.setValue(blue * 60 / 255);

        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setPosition(-1);
            view.setCustomColor(red, green, blue);
        });
        setCustomColor();
    }

    private void setCustomColor() {
        int r = mModel.red.getValue();
        int g = mModel.green.getValue();
        int b = mModel.blue.getValue();

        switch (mIllumiType) {
            case COMMON:
                mIllumiCase.setCommonCustomColor(r, g, b);
                break;
            case DISP:
                mIllumiCase.setCustomColor(IlluminationTarget.DISP, r, g, b);
                break;
            case KEY:
                mIllumiCase.setCustomColor(IlluminationTarget.KEY, r, g, b);
                break;
        }
    }
}
