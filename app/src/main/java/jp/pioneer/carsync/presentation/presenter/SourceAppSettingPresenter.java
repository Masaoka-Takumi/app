package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.interactor.PreferMusicApp;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.SourceAppModel;
import jp.pioneer.carsync.presentation.view.SourceAppSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * Source App SettingのPresenter
 */
@PresenterLifeCycle
public class SourceAppSettingPresenter extends Presenter<SourceAppSettingView>{
    @Inject Context mContext;
    @Inject SourceAppModel mModel;
    @Inject EventBus mEventBus;
    @Inject PreferMusicApp mPreferMusicApp;

    /**
     * コンストラクタ
     */
    @Inject
    public SourceAppSettingPresenter() {
    }

    @Override
    void onTakeView() {
        List<ApplicationInfo> apps = mPreferMusicApp.getInstalledTargetAppList();
        mModel.installedMessagingApps.setValue(apps);
        SparseBooleanArray checkedItemPositions = new SparseBooleanArray();
        AppSharedPreference.Application[] checks = mPreferMusicApp.getSelectedAppList();
        for (AppSharedPreference.Application check : checks) {
            int index = indexOf(apps, check.packageName);
            if (index != -1) {
                checkedItemPositions.put(index, true);
            }
        }
        mModel.checkedItemPositions.setValue(checkedItemPositions);
    }

    @Override
    void onResume() {
        mModel.installedMessagingApps.setValue(mPreferMusicApp.getInstalledTargetAppList());
        mModel.installedMessagingApps.setDirty();
        mModel.checkedItemPositions.setDirty();
        setViewDataIfNecessary();
        Optional.ofNullable(getView()).ifPresent(view -> view.setPass(mContext.getString(R.string.src_001)));
    }

    @Override
    void onSaveInstanceState(@NonNull Bundle outState) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            mModel.checkedItemPositions.setValue(view.getCheckedItemPositions());
            mModel.saveInstanceState(outState);
        });
    }

    @Override
    void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        mModel.restoreInstanceState(savedInstanceState);

        mModel.installedMessagingApps.setValue(mPreferMusicApp.getInstalledTargetAppList());
        mModel.installedMessagingApps.setDirty();
        mModel.checkedItemPositions.setDirty();
        setViewDataIfNecessary();

    }

    private void setViewDataIfNecessary() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (mModel.installedMessagingApps.isDirty()) {
                view.setInstalledMusicApps(mModel.installedMessagingApps.getValue());
            }
            if (mModel.checkedItemPositions.isDirty()) {
                view.setCheckedItemPositions(mModel.checkedItemPositions.getValue());
            }
        });
    }

    /**
     * Musicアプリケーション選択後の処理
     *
     */
    public void onMusicAppDecided() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            SparseBooleanArray checkedItemPositions = view.getCheckedItemPositions();
            List<ApplicationInfo> selectedApps = new ArrayList<>();
            List<ApplicationInfo> installedApps = mModel.installedMessagingApps.getValue();
            for (int i = 0; i < checkedItemPositions.size(); i++) {
                if (checkedItemPositions.valueAt(i)) {
                    selectedApps.add(installedApps.get(checkedItemPositions.keyAt(i)));
                }
            }
            List<AppSharedPreference.Application> musicApps = new ArrayList<>();
            PackageManager pm = mContext.getPackageManager();
            for (ApplicationInfo app : selectedApps) {
                String packageName = app.packageName;
                String label = app.loadLabel(pm).toString();
                musicApps.add(new AppSharedPreference.Application(packageName, label));
            }
            mPreferMusicApp.setSelectedAppList(musicApps.toArray(new AppSharedPreference.Application[0]));
            view.setCheckedItemPositions(checkedItemPositions);
        });
    }

    private int indexOf(List<ApplicationInfo> apps, String packageName) {
        for (int i = 0; i < apps.size(); i++) {
            if (packageName.equals(apps.get(i).packageName)) {
                return i;
            }
        }
        return -1;
    }

    public void onBackAction() {
        mEventBus.post(new NavigateEvent(ScreenId.SOURCE_SELECT, null));
    }
}
