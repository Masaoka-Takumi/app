package jp.pioneer.carsync.presentation.presenter;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.IlluminationSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferIllumination;
import jp.pioneer.carsync.domain.model.IlluminationColor;
import jp.pioneer.carsync.domain.model.IlluminationColorMap;
import jp.pioneer.carsync.domain.model.IlluminationColorSpec;
import jp.pioneer.carsync.domain.model.IlluminationSettingStatus;
import jp.pioneer.carsync.domain.model.IlluminationTarget;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.ThemeType;
import jp.pioneer.carsync.presentation.model.IlluminationColorModel;
import jp.pioneer.carsync.presentation.model.ThemeSelectItem;
import jp.pioneer.carsync.presentation.model.UiColor;
import jp.pioneer.carsync.presentation.view.ThemeSetView;

/**
 * テーマセット設定画面のpresenter
 */
@PresenterLifeCycle
public class ThemeSetPresenter extends Presenter<ThemeSetView> {

    private ArrayList<ThemeSelectItem> ThemeItems = new ArrayList<>();
    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject PreferIllumination mPreferIllumination;
    private UiColor mUiColor;
    private int mMyPhotoIndex;
    /**
     * コンストラクタ
     */
    @Inject
    public ThemeSetPresenter() {
    }

    public int getMyPhotoIndex() {
        return mMyPhotoIndex;
    }

    public boolean setMyPhotoEnabled() {
        if(mPreference.getThemeMyPhotoEnabled()){
            if(ThemeItems.size()<ThemeType.PICTURE_PATTERN13.ordinal()+1) {
                ThemeItems.add(new ThemeSelectItem(ThemeType.PICTURE_PATTERN13, ThemeType.PICTURE_PATTERN13.ordinal()));
                Optional.ofNullable(getView()).ifPresent(view -> {
                    view.setAdapter(ThemeItems);
                    StatusHolder holder = mGetStatusHolder.execute();
                    boolean isIllumiSettingEnabled = holder.getCarDeviceStatus().illuminationSettingEnabled &&
                            holder.getCarDeviceSpec().illuminationSettingSupported;
                    if(!isIllumiSettingEnabled){
                        ThemeType currentType = mPreference.getThemeType();
                        for (ThemeSelectItem type : ThemeItems) {
                            if (type.themeType == currentType) {
                                view.setCurrentItem(type.number);
                                break;
                            }
                        }
                    }

                });

            }else{
                Optional.ofNullable(getView()).ifPresent(view -> {
                    view.setAdapter(ThemeItems);
                });
            }
            return true;
        }
        return false;
    }

    @Override
    void onInitialize() {
       ThemeItems.clear();
        int position = 0;
        for (ThemeType type : ThemeType.values()) {
            if(type == ThemeType.PICTURE_PATTERN13){
                mMyPhotoIndex = position;
                if(!mPreference.getThemeMyPhotoEnabled())return;
            }
            ThemeItems.add(new ThemeSelectItem(type, position));
            position++;
        }
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        StatusHolder holder = mGetStatusHolder.execute();
        ThemeType currentType = mPreference.getThemeType();

        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setAdapter(ThemeItems);
            boolean isDispEnable = false;
            boolean isKeyEnable = false;
            boolean isCustom = false;
            UiColor uiColor = currentType.getUIColor();
            mUiColor = mPreference.getUiColor();
            if (!uiColor.equals(mUiColor)) {
                isCustom = true;
            }
            // イルミカラー
            IlluminationSettingStatus illuminationSettingStatus = holder.getIlluminationSettingStatus();
            if (illuminationSettingStatus.commonColorCustomSettingEnabled) {
                isDispEnable = true;
                isKeyEnable = true;
            }
            if (illuminationSettingStatus.colorCustomDispSettingEnabled) {
                isDispEnable = true;
            }
            if (illuminationSettingStatus.colorCustomKeySettingEnabled) {
                isKeyEnable = true;
            }
            view.setDispColorSettingEnabled(isDispEnable);
            view.setKeyColorSettingEnabled(isKeyEnable);
            if (holder.getProtocolSpec().isSphCarDevice()) {
                IlluminationColorModel commonModel = currentType.getIlluminationColor();
                IlluminationColor currCommon = holder.getIlluminationSetting().commonColor;
                if (currCommon != null) {
                    IlluminationColorMap colorMapCommon = holder.getIlluminationSetting().commonColorSpec;
                    IlluminationColorSpec custom = colorMapCommon.get(currCommon);
                    if (commonModel.blue.getValue() != custom.blue || commonModel.red.getValue() != custom.red || commonModel.green.getValue() != custom.green) {
                        isCustom = true;
                    }
                    IlluminationColorModel commonModelSet = new IlluminationColorModel();
                    commonModelSet.blue.setValue(custom.blue);
                    commonModelSet.red.setValue(custom.red);
                    commonModelSet.green.setValue(custom.green);
                    view.setDispColor(commonModelSet);
                    view.setKeyColor(commonModelSet);
                } else {
                    view.setDispColor(commonModel);
                    view.setKeyColor(commonModel);
                }
            } else {
                IlluminationColorModel dispModel = currentType.getIlluminationDisplayColor();
                IlluminationColorModel keyModel = currentType.getIlluminationKeyColor();
                IlluminationColor currDisp = holder.getIlluminationSetting().dispColor;
                IlluminationColorMap colorMapDisp = holder.getIlluminationSetting().dispColorSpec;
                IlluminationColor currKey = holder.getIlluminationSetting().keyColor;
                IlluminationColorMap colorMapKey = holder.getIlluminationSetting().keyColorSpec;
                if (currDisp != null && currKey != null) {
                    IlluminationColorSpec customDisp = colorMapDisp.get(currDisp);
                    IlluminationColorSpec customKey = colorMapKey.get(currKey);
                    if (dispModel.blue.getValue() != customDisp.blue || dispModel.red.getValue() != customDisp.red || dispModel.green.getValue() != customDisp.green
                            || keyModel.blue.getValue() != customKey.blue || keyModel.red.getValue() != customKey.red || keyModel.green.getValue() != customKey.green) {
                        isCustom = true;
                    }
                    IlluminationColorModel dispModelSet = new IlluminationColorModel();
                    dispModelSet.blue.setValue(customDisp.blue);
                    dispModelSet.red.setValue(customDisp.red);
                    dispModelSet.green.setValue(customDisp.green);
                    IlluminationColorModel keyModelSet = new IlluminationColorModel();
                    keyModelSet.blue.setValue(customKey.blue);
                    keyModelSet.red.setValue(customKey.red);
                    keyModelSet.green.setValue(customKey.green);
                    view.setDispColor(dispModelSet);
                    view.setKeyColor(keyModelSet);
                } else {
                    view.setDispColor(dispModel);
                    view.setKeyColor(keyModel);
                }
            }
            view.setUIColor(mUiColor.getResource());
            view.setCustom(isCustom);
       		//onResume以降でないと画面回転後に選択枠を表示できない            
			for (ThemeSelectItem type : ThemeItems) {
                if (type.themeType == currentType) {
                    view.setCurrentItem(type.number);
                    break;
                }
            }
        });
        setEnable();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
        Optional.ofNullable(getView()).ifPresent(view -> view.setTheme(mUiColor.getColorThemeId()));
    }

    private void setEnable(){
        StatusHolder holder = mGetStatusHolder.execute();
        boolean isIllumiSettingEnabled = holder.getCarDeviceStatus().illuminationSettingEnabled &&
                holder.getCarDeviceSpec().illuminationSettingSupported;

        Optional.ofNullable(getView()).ifPresent(view -> view.setEnable(isIllumiSettingEnabled));
    }

    private void updateView() {
        ThemeType type = mPreference.getThemeType();
        StatusHolder holder = mGetStatusHolder.execute();

        Optional.ofNullable(getView()).ifPresent(view -> {
            // イルミカラー
            IlluminationSettingStatus illuminationSettingStatus = holder.getIlluminationSettingStatus();

            boolean isDispEnable = false;
            boolean isKeyEnable = false;
            if (illuminationSettingStatus.commonColorCustomSettingEnabled) {
                IlluminationColorModel common = type.getIlluminationColor();
                view.setDispColor(common);
                view.setKeyColor(common);
                isDispEnable = true;
                isKeyEnable = true;
            }
            if (illuminationSettingStatus.colorCustomDispSettingEnabled) {
                IlluminationColorModel disp = type.getIlluminationDisplayColor();
                view.setDispColor(disp);
                isDispEnable = true;
            }
            if (illuminationSettingStatus.colorCustomKeySettingEnabled) {
                IlluminationColorModel key = type.getIlluminationKeyColor();
                view.setKeyColor(key);
                isKeyEnable = true;
            }
            view.setDispColorSettingEnabled(isDispEnable);
            view.setKeyColorSettingEnabled(isKeyEnable);

            // UIカラー
            UiColor ui = type.getUIColor();
            view.setUIColor(ui.getResource());

        });
    }

    /**
     * テーマ選択時処理
     *
     * @param position 選択位置
     */
    public void onSelectThemeAction(int position) {
        StatusHolder holder = mGetStatusHolder.execute();
        ThemeSelectItem item = ThemeItems.get(position);
        ThemeType currentType = item.themeType;
        if(mPreference.getThemeType()!=currentType) {
            mPreference.setThemeType(currentType);
            if (holder.getProtocolSpec().isSphCarDevice()) {
                IlluminationColorModel color = currentType.getIlluminationColor();
                mPreferIllumination.setCommonCustomColor(color.red.getValue(), color.green.getValue(), color.blue.getValue());
            } else {
                IlluminationColorModel dispColor = currentType.getIlluminationDisplayColor();
                IlluminationColorModel keyColor = currentType.getIlluminationKeyColor();
                mPreferIllumination.setCustomColor(IlluminationTarget.DISP, dispColor.red.getValue(), dispColor.green.getValue(), dispColor.blue.getValue());
                mPreferIllumination.setCustomColor(IlluminationTarget.KEY, keyColor.red.getValue(), keyColor.green.getValue(), keyColor.blue.getValue());
            }
            mPreference.setUiColor(currentType.getUIColor());
            mUiColor = currentType.getUIColor();

            updateView();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIlluminationSettingStatusChangeEvent(IlluminationSettingStatusChangeEvent event) {
        updateView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarDeviceStatusChangeEvent(CarDeviceStatusChangeEvent event) {
        setEnable();
    }

}
