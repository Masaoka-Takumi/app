package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

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
import jp.pioneer.carsync.domain.event.IlluminationSettingChangeEvent;
import jp.pioneer.carsync.domain.event.IlluminationSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferReadNotification;
import jp.pioneer.carsync.domain.model.IlluminationSetting;
import jp.pioneer.carsync.domain.model.IlluminationSettingSpec;
import jp.pioneer.carsync.domain.model.IlluminationSettingStatus;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.MessageView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * Message設定のPresenter
 */
@PresenterLifeCycle
public class MessagePresenter extends Presenter<MessageView> {
    @Inject GetStatusHolder mGetCase;
    @Inject PreferReadNotification mMessagingCase;
    @Inject Context mContext;
    @Inject EventBus mEventBus;
    private ArrayList<String> mTypeArray = new ArrayList<>();
    private ArrayList<ApplicationInfo> mSelectedApplication = new ArrayList<>();

    /**
     * コンストラクタ
     */
    @Inject
    public MessagePresenter() {
    }

    @Override
    void onTakeView() {
        IlluminationSettingSpec spec = mGetCase.execute().getCarDeviceSpec().illuminationSettingSpec;

        mTypeArray.clear();
        mTypeArray.add(mContext.getString(R.string.set_131));
        mTypeArray.add(mContext.getString(R.string.set_132));
        if(spec.incomingMessageColorSettingSupported) {
            mTypeArray.add(mContext.getString(R.string.set_077));
        }
        Optional.ofNullable(getView()).ifPresent(view -> view.setAdapter(mTypeArray));
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
     * イルミネーション設定変更イベントハンドラ.
     *
     * @param ev イルミネーション設定変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIlluminationSettingChangeEvent(IlluminationSettingChangeEvent ev){
        updateView();
    }

    /**
     * イルミネーション設定ステータス変更イベントハンドラ.
     *
     * @param ev イルミネーション設定ステータス変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onIlluminationSettingStatusChangeEvent(IlluminationSettingStatusChangeEvent ev){
        updateView();
    }

    private void updateView() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            IlluminationSetting setting = mGetCase.execute().getIlluminationSetting();
            IlluminationSettingStatus status = mGetCase.execute().getIlluminationSettingStatus();

            List<ApplicationInfo> apps = mMessagingCase.getInstalledTargetAppList();
            mSelectedApplication = getSelectedApplication(apps);
            view.setMessageReading(
                    mMessagingCase.isEnabled()
            );
            view.setApplicationList(
                    apps,
                    mSelectedApplication
            );
            view.setMessageColor(
                    status.incomingMessageColorSettingEnabled,
                    setting.incomingMessageColor
            );
        });
    }

    private ArrayList<ApplicationInfo> getSelectedApplication(List<ApplicationInfo> messageApps) {
        ArrayList<ApplicationInfo> result = new ArrayList<>();
        AppSharedPreference.Application[] selectedApps = mMessagingCase.getSelectedAppList();
        for (AppSharedPreference.Application selectedApp : selectedApps) {
            for (ApplicationInfo installApp : messageApps) {
                if (installApp.packageName.equals(selectedApp.packageName)) {
                    result.add(installApp);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * 通知読み上げ状態取得
     *
     * @return 通知読み上げが有効か否
     */
    public boolean getEnable() {
        return mMessagingCase.isEnabled();
    }

    /**
     * 通知読み上げスイッチ押下時の処理
     *
     * @param isEnableReadNotification スイッチの状態
     */
    public void onSwitchReadNotificationEnabledChange(boolean isEnableReadNotification) {
        mMessagingCase.setEnabled(isEnableReadNotification);
        updateView();
    }

    /**
     * メッセージアプリケーション選択後の処理
     */
    public void onMessageAppCheckedAction(ApplicationInfo selectedApp) {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(mSelectedApplication.contains(selectedApp)){
                mSelectedApplication.remove(selectedApp);
            } else {
                mSelectedApplication.add(selectedApp);
            }

            List<AppSharedPreference.Application> messagingApps = new ArrayList<>();
            PackageManager pm = mContext.getPackageManager();
            for (ApplicationInfo app : mSelectedApplication) {
                String packageName = app.packageName;
                String label = app.loadLabel(pm).toString();
                messagingApps.add(new AppSharedPreference.Application(packageName, label));
            }
            mMessagingCase.setSelectedAppList(messagingApps.toArray(new AppSharedPreference.Application[0]));
        });
        updateView();
    }

    public void onMessagingColorClickAction(){
        mEventBus.post(new NavigateEvent(ScreenId.INCOMING_MESSAGE_COLOR_SETTING, createSettingsParams(mContext.getString(R.string.set_077))));
    }

    private int indexOf(List<ApplicationInfo> apps, String packageName) {
        for (int i = 0; i < apps.size(); i++) {
            if (packageName.equals(apps.get(i).packageName)) {
                return i;
            }
        }
        return -1;
    }

    private Bundle createSettingsParams(String pass) {
        SettingsParams params = new SettingsParams();
        params.pass = pass;
        return params.toBundle();
    }
}
