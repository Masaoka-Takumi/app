package jp.pioneer.carsync.presentation.presenter;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Set;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.InitialSettingChangeEvent;
import jp.pioneer.carsync.domain.event.InitialSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferInitial;
import jp.pioneer.carsync.domain.model.InitialSettingSpec;
import jp.pioneer.carsync.domain.model.MenuDisplayLanguageType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.MenuDisplayLanguageDialogView;

/**
 * Menu表示言語設定のPresenter.
 */
public class MenuDisplayLanguageDialogPresenter extends Presenter<MenuDisplayLanguageDialogView> {
    @Inject GetStatusHolder mGetCase;
    @Inject EventBus mEventBus;
    @Inject PreferInitial mPreferCase;

    @Inject
    public MenuDisplayLanguageDialogPresenter() {

    }

    @Override
    void onTakeView() {
        updateView();
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    /**
     * 初期設定変更イベントハンドラ.
     *
     * @param event 初期設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInitialSettingChangeEvent(InitialSettingChangeEvent event) {
        updateView();
    }

    /**
     * 初期設定ステータス変更イベントハンドラ.
     *
     * @param event 初期設定ステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInitialSettingStatusChangeEvent(InitialSettingStatusChangeEvent event) {
        updateView();
    }

    /**
     * 車載機ステータス変更イベントハンドラ
     *
     * @param event 車載機ステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarDeviceStatusChangeEvent(CarDeviceStatusChangeEvent event) {
        updateView();
    }

    private void updateView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            StatusHolder holder = mGetCase.execute();
            if (holder.getCarDeviceStatus().initialSettingEnabled && holder.getInitialSettingStatus().menuDisplayLanguageSettingEnabled) {
                MenuDisplayLanguageType setting = mGetCase.execute().getInitialSetting().menuDisplayLanguageType;
                view.setMenuDisplayLanguageSetting(setting);
            } else{
                view.callbackClose();
            }
        });
    }

    public Set<MenuDisplayLanguageType> getSupportedLanguage() {
        InitialSettingSpec spec = mGetCase.execute().getCarDeviceSpec().initialSettingSpec;
        return spec.supportedMenuDisplayLanguages;
    }

    /**
     * リストアイテム選択
     *
     * @param setting アイテム
     */
    public void onSelectAction(MenuDisplayLanguageType setting) {
        mPreferCase.setMenuDisplayLanguage(setting);
    }
}
