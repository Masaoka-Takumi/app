package jp.pioneer.carsync.application;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.presentation.view.service.ResourcefulService;
import timber.log.Timber;

/**
 * サービス制御マネージャ.
 * <p>
 * 状態に応じてサービスの動作を制御する。<br>
 * ただし、システムから起動されるNotificationListenerServiceImplは除く。
 */
public class ServiceControlManager {
    @Inject Context mContext;
    /**
     * コンストラクタ.
     */
    @Inject
    public ServiceControlManager() {
    }

    /**
     * 初期化.
     * <p>
     * アプリ起動時に一度だけ呼ばれることを想定。複数回呼び出した場合の動作は不定。
     * アプリ終了まで動作し続けるため終了用のメソッドはない。
     */
    public void initialize() {
        Timber.d("initialize()");
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        if(manager!=null) {
            List<ActivityManager.RunningServiceInfo> runningServicesInfo = manager.getRunningServices(Integer.MAX_VALUE);
            for (ActivityManager.RunningServiceInfo serviceInfo : runningServicesInfo) {
                if (ResourcefulService.class.getName().equals(serviceInfo.service.getClassName())) {
                    // 実行中なら起動しない
                    Timber.d("ResourcefulService is Running");
                    return;
                }
            }
        }
        Intent intent = new Intent(mContext, ResourcefulService.class);
        if(Build.VERSION.SDK_INT>=26){
            mContext.startForegroundService(intent);
        }else{
            // noinspection
            mContext.startService(intent);
        }
    }

}
