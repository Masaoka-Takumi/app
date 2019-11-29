package jp.pioneer.carsync.presentation.presenter;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.Analytics;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.interactor.ControlSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.CautionDialogView;
import timber.log.Timber;

/**
 * Caution画面のpresenter
 */
@PresenterLifeCycle
public class CautionDialogPresenter extends Presenter<CautionDialogView> {
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.SEND_SMS
    };

    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;
    @Inject ControlSource mControlSource;
    @Inject Analytics mAnalytics;
    /**
     * コンストラクタ
     */
    @Inject
    public CautionDialogPresenter() {
    }

    /**
     * 確認ボタン押下イベント
     */
    public void onConfirmAction() {
        //車載器連携と同時にソースOFFにすると上手くいかないため、確認ボタンを押したタイミングでソースOFF
        StatusHolder holder = mGetStatusHolder.execute();
        if (!mPreference.isFirstInitialSettingCompletion()) {
            if (holder.getProtocolSpec().isSphCarDevice()) {
                Timber.d("onConfirmAction:sourceOff");
                mControlSource.selectSource(MediaSourceType.OFF);
            }
        }
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setScreenOn();
            mGetStatusHolder.execute().getAppStatus().isAgreedCaution = true;
            CarDeviceSpec spec = mGetStatusHolder.execute().getCarDeviceSpec();
            mAnalytics.logDeviceConnectedEvent(spec);

            requestPermissions();
            view.callbackClose();
        });
    }
    private void requestPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = getDenyPermissions(PERMISSIONS);
            if (permissions.length > 0) {
                Optional.ofNullable(getView()).ifPresent(view -> view.requestPermissions(permissions));
            }
        }
    }

    private String[] getDenyPermissions(final String... permissions) {
        final List<String> denyPermissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (permission != null && ContextCompat.checkSelfPermission(mContext, permission) != PackageManager.PERMISSION_GRANTED) {
                denyPermissionList.add(permission);
            }
        }
        // 追加したPermissionがない場合でも、空のリストを返す
        return denyPermissionList.toArray(new String[denyPermissionList.size()]);
    }
}
