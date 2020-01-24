package jp.pioneer.carsync.presentation.presenter;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.NaviGuideVoiceSettingChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferMarinApp;
import jp.pioneer.carsync.domain.interactor.PreferNaviApp;
import jp.pioneer.carsync.domain.interactor.PreferNaviGuideVoice;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.NaviApp;
import jp.pioneer.carsync.domain.model.NaviGuideVoiceSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.NavigationView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * Navigation設定のPresenter
 */
@PresenterLifeCycle
public class NavigationPresenter extends Presenter<NavigationView> {
    @Inject GetStatusHolder mGetCase;
    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    @Inject PreferNaviApp mNaviCase;
    @Inject PreferMarinApp mMarinCase;
    @Inject PreferNaviGuideVoice mPreferCase;
    @Inject Context mContext;
    private ArrayList<String> mTypeArray = new ArrayList<>();

    @Inject
    public NavigationPresenter() {
    }

    @Override
    void onTakeView() {
        CarDeviceSpec spec = mGetCase.execute().getCarDeviceSpec();

        mTypeArray.clear();
        if(mPreference.getLastConnectedCarDeviceClassId() == CarDeviceClassId.MARIN){
            mTypeArray.add(mContext.getString(R.string.set_326));
            mTypeArray.add(mContext.getString(R.string.set_327));
            mTypeArray.add(mContext.getString(R.string.set_328));
            mTypeArray.add(mContext.getString(R.string.set_145));
        }else{
            mTypeArray.add(mContext.getString(R.string.set_145));
        }
        if(spec.naviGuideVoiceSettingSupported) {
            mTypeArray.add(mContext.getString(R.string.set_146));
            mTypeArray.add(mContext.getString(R.string.set_075));
        }
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdapter(mTypeArray,mPreference.getLastConnectedCarDeviceClassId() == CarDeviceClassId.MARIN));
        updateView();
    }

    @Override
    void onResume(){
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    /**
     * ナビガイド音声設定変更イベントハンドラ.
     *
     * @param event ナビガイド音声設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNaviGuideVoiceSettingChangeEvent(NaviGuideVoiceSettingChangeEvent event) {
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
        StatusHolder holder = mGetCase.execute();
        boolean isNaviSettingEnabled = holder.getCarDeviceStatus().naviGuideVoiceSettingEnabled &&
                holder.getCarDeviceSpec().naviGuideVoiceSettingSupported;
        NaviGuideVoiceSetting setting = holder.getNaviGuideVoiceSetting();

        Optional.ofNullable(getView()).ifPresent(view -> {
            List<ApplicationInfo> naviApps = mNaviCase.getInstalledTargetAppList();
            if(mPreference.getLastConnectedCarDeviceClassId()== CarDeviceClassId.MARIN){
                List<ApplicationInfo> allNaviApps = mMarinCase.getInstalledTargetAppList();
                List<ApplicationInfo> weatherApps = mMarinCase.getInstalledWeatherTargetAppList();
                List<ApplicationInfo> boatingApps = mMarinCase.getInstalledBoatingTargetAppList();
                List<ApplicationInfo> fishingApps = mMarinCase.getInstalledFishingTargetAppList();

                view.setApplicationList(
                        weatherApps,
                        boatingApps,
                        fishingApps,
                        naviApps,
                        getSelectedApplication(allNaviApps)
                );
            }else{
                view.setApplicationList(
                        naviApps,
                        getSelectedApplication(naviApps)
                );
            }

           view.setMixingSetting(
                   isNaviSettingEnabled,
                   setting.naviGuideVoiceSetting
           );
           view.setMixingVolumeSetting(
                   isNaviSettingEnabled && setting.naviGuideVoiceSetting,
                   setting.naviGuideVoiceVolumeSetting
           );
        });
    }

    @Nullable
    private ApplicationInfo getSelectedApplication(List<ApplicationInfo> naviApps) {
        String packageName = null;
        if(mPreference.getLastConnectedCarDeviceClassId()== CarDeviceClassId.MARIN){
            AppSharedPreference.Application application = mPreference.getNavigationMarinApp();
            if(application != null) {
                packageName = application.packageName;
            }
        }else{
            packageName= mPreference.getNavigationApp().packageName;
        }
        if(packageName!=null) {
            for (ApplicationInfo app : naviApps) {
                if (packageName.equals(app.packageName)) {
                    return app;
                }
            }
        }
        return null;
    }

    /**
     * ナビアプリケーション選択後の処理
     *
     * @param app 選択したアプリケーション
     */
    public void onNavigationAppSelectedAction(ApplicationInfo app) {
        String packageName = app.packageName;
        String label = app.loadLabel(mContext.getPackageManager()).toString();
        AppSharedPreference.Application naviApp = new AppSharedPreference.Application(packageName, label);
        if(mPreference.getLastConnectedCarDeviceClassId()== CarDeviceClassId.MARIN){
            mPreference.setNavigationMarinApp(naviApp);
        }else{
            mPreference.setNavigationApp(naviApp);
        }
        updateView();
    }

    /**
     * ナビガイド設定.
     *
     * @param setting 設定内容
     */
    public void onMixingSettingAction(boolean setting){
        mPreferCase.setNaviGuideVoice(setting);
    }

    /**
     * ナビガイド音声設定.
     */
    public void onMixingVolumeSettingAction(){
        mEventBus.post(new NavigateEvent(ScreenId.GUIDANCE_VOLUME_DIALOG, Bundle.EMPTY));
    }
}
