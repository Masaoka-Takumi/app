package jp.pioneer.carsync.presentation.view.service;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.ServiceComponent;
import jp.pioneer.carsync.domain.model.AndroidMusicMediaInfo;
import jp.pioneer.carsync.domain.model.BaseApp;
import jp.pioneer.carsync.domain.model.MarinApp;
import jp.pioneer.carsync.domain.model.NaviApp;
import jp.pioneer.carsync.infrastructure.component.BroadcastReceiverImpl;
import jp.pioneer.carsync.presentation.controller.SongChangeToastController;
import jp.pioneer.carsync.presentation.presenter.MainPresenter;
import jp.pioneer.carsync.presentation.presenter.ResourcefulPresenter;
import jp.pioneer.carsync.presentation.view.ResourcefulView;
import jp.pioneer.carsync.presentation.view.activity.MainActivity;
import timber.log.Timber;

import static android.content.Intent.ACTION_POWER_CONNECTED;

/**
 * CarSyncのバックグラウンド処理を行うサービス
 * <p>
 * 一部機能を使用する場合にのみ、フォアグラウンドで動作する。
 */
public class ResourcefulService extends AbstractService<ResourcefulPresenter, ResourcefulView>
        implements ResourcefulView, SongChangeToastController.OnToastHiddenListener {
    private static final int NOTIFICATION_FOREGROUND = 1;
    @Inject ResourcefulPresenter mPresenter;
    @Inject SongChangeToastController mSongChangeToastController;
    private final BroadcastReceiverImpl mBroadcastReceiver1 = new BroadcastReceiverImpl();
    private AppOpsManager mAppOpsManager;
    private AppOpsManager.OnOpChangedListener mAppOpChangedListener;
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.i("onCreate()");
        mSongChangeToastController.setOnToastHiddenListener(this);
        if(Build.VERSION.SDK_INT>=26) {
            IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            IntentFilter filter2 = new IntentFilter(ACTION_POWER_CONNECTED);
            registerReceiver(mBroadcastReceiver1, filter1);
            registerReceiver(mBroadcastReceiver1, filter2);
        }
        if (MainPresenter.sIsVersionQ) {
            mAppOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            mAppOpChangedListener = new AppOpsManager.OnOpChangedListener() {
                @Override
                public void onOpChanged(String op, String packageName) {
                    PackageManager packageManager = getPackageManager();
                    String myPackageName = getPackageName();
                    if (myPackageName.equals(packageName) &&
                            AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW.equals(op)) {
                        Timber.d("Overlay:onOpChanged=" + Settings.canDrawOverlays(getApplicationContext()));
                        if (!Settings.canDrawOverlays(getApplicationContext())) {
                            //オーバーレイ権限不許可
                            getPresenter().startDeviceConnectionSuppress();
                        }
                    }
                }
            };
            mAppOpsManager.startWatchingMode(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
                    getPackageName(), mAppOpChangedListener);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.i("onDestroy()");

        mSongChangeToastController.setOnToastHiddenListener(null);
        if(Build.VERSION.SDK_INT>=26) {
            unregisterReceiver(mBroadcastReceiver1);
        }
        if(mAppOpsManager!=null) {
            mAppOpsManager.stopWatchingMode(mAppOpChangedListener);
            mAppOpsManager = null;
        }
    }

    @Override
    protected void doCreate() {
        // no action
    }

    @Override
    protected void doInject(ServiceComponent serviceComponent) {
        serviceComponent.inject(this);
    }

    @NonNull
    @Override
    protected ResourcefulPresenter getPresenter() {
        return mPresenter;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void startForeground(String message) {
        Timber.i("startForeground() message=%s",message);
        String CHANNEL_ID = "my_channel_07";
        NotificationManager notificationManager =
                (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(false);
            if(notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        Intent startIntent = new Intent(this, MainActivity.class);
        startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_service)
                .setTicker(message)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .build();
        startForeground(NOTIFICATION_FOREGROUND, notification);
    }

    @Override
    public void stopForeground() {
        stopForeground(true);
    }

/// MARK - ショートカットキー

    @Override
    public void dispatchAppKey() {
        Timber.i("dispatchAppKey()");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_APP_COMMAND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void dispatchNaviKey(String packageName) {
        Timber.i("dispatchNaviKey()");

        if(!TextUtils.isEmpty(packageName)) {
            try {
                NaviApp navi = NaviApp.fromPackageName(packageName);
                Intent intent = navi.createMainIntent(getApplicationContext());
                startActivity(intent);

            } catch (ActivityNotFoundException ex){
                // ナビアプリケーションが存在しない
                Timber.w("Invalid navi app. package name:" + packageName);
                showError(getString(R.string.err_007));
                return;
            } catch (IllegalArgumentException ex){
                // ナビアプリケーション未設定
                Timber.w("Unset navi app.", ex);
                showError(getString(R.string.err_007));
                return;
            }
        }
    }

    @Override
    public void dispatchNaviMarinKey(String packageName) {
        Timber.i("dispatchNaviKey()");

        if(!TextUtils.isEmpty(packageName)) {
            try {
                BaseApp navi = MarinApp.fromPackageNameNoThrow(packageName);
                if(navi==null){
                    navi = NaviApp.fromPackageName(packageName);
                }
                Intent intent = navi.createMainIntent(getApplicationContext());
                startActivity(intent);

            } catch (ActivityNotFoundException ex){
                // ナビアプリケーションが存在しない
                Timber.w("Invalid navi app. package name:" + packageName);
                showError(getString(R.string.err_035));
                return;
            } catch (IllegalArgumentException ex){
                // ナビアプリケーション未設定
                Timber.w("Unset navi app.", ex);
                showError(getString(R.string.err_035));
                return;
            }
        }
    }

    @Override
    public void dispatchMessageKey() {
        Timber.i("dispatchMessageKey()");
        getPresenter().onMessageKeyAction();
    }

    @Override
    public void dispatchPhoneKey() {
        Timber.i("dispatchPhoneKey()");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_PHONE_COMMAND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void dispatchAvKey() {
        Timber.i("dispatchSourceKey()");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_AV_COMMAND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void dispatchVoiceKey() {
        Timber.i("dispatchVoiceKey()");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_VOICE_COMMAND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void dispatchEnterList() {
        Timber.i("dispatchEnterList()");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_ENTER_LIST);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void dispatchPermissionRequest() {
        Timber.i("dispatchPermissionRequest()");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_PERMISSION_REQUEST);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

/// MARK - 衝突検知

    @Override
    public void showAccidentDetect() {
        Timber.i("showAccidentDetect()");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_SHOW_ACCIDENT_DETECT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

/// MARK - ADAS警告

    @Override
    public void showAdasWarning() {
        Timber.i("showAdasWarning()");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_SHOW_ADAS_WARNING);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

/// MARK - パーキングセンサー

    @Override
    public void showParkingSensor() {
        Timber.i("showParkingSensor()");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_SHOW_PARKING_SENSOR);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

/// MARK - 車載器エラー

    @Override
    public void showCarDeviceError(String errorTag, String errorTitle, String errorText) {
        Timber.i("showCarDeviceError()");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_CAR_DEVICE_ERROR);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("errorTag",errorTag);
        intent.putExtra("errorTitle",errorTitle);
        intent.putExtra("errorText",errorText);
        startActivity(intent);
    }

/// MARK - Subscription Update

    @Override
    public void showSubscriptionUpdating() {
        Timber.i("showSubscriptionUpdating()");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_SUBSCRIPTION_UPDATE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void showSongNotification(AndroidMusicMediaInfo mediaInfo) {
        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.element_toast_song_notification, null);

        ImageView jacket = (ImageView) view.findViewById(R.id.jacket_view);
        //jacket.setImageURI(mediaInfo.artworkImageLocation);
        Glide.with(this)
                .load(mediaInfo.artworkImageLocation)
                .error(R.drawable.p0070_noimage)
                .into(jacket);

        TextView title = (TextView) view.findViewById(R.id.title_text);
        title.setText(TextUtils.isEmpty((mediaInfo.songTitle)) ? getString(R.string.mes_001) : mediaInfo.songTitle);

        TextView album = (TextView) view.findViewById(R.id.album_text);
        album.setText(TextUtils.isEmpty((mediaInfo.albumTitle)) ? getString(R.string.mes_002) : mediaInfo.albumTitle);

        TextView artist = (TextView) view.findViewById(R.id.artist_text);
        artist.setText(TextUtils.isEmpty((mediaInfo.artistName)) ? getString(R.string.mes_003) : mediaInfo.artistName);

        TextView genre = (TextView) view.findViewById(R.id.genre_text);
        genre.setText(TextUtils.isEmpty((mediaInfo.genre)) ? getString(R.string.mes_004) : mediaInfo.genre);

        mSongChangeToastController.show(mediaInfo, view);
    }

    @Override
    public void readMessage() {
        Timber.i("showReadingMessage()");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_READING_MESSAGE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void setNaviDestination(Double latitude, Double longitude, String name) {
        Timber.i("setNaviDestination()");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_ALEXA_NAVI_COMMAND);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude",longitude);
        intent.putExtra("destName",name);
        startActivity(intent);
    }

    @Override
    public void onShowAndroidSettings(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void showError(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showShortToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void hideSongNotification() {
        mSongChangeToastController.hideNotification();
    }

    @Override
    public void startApplication(String packageName) {
        Intent intent = new Intent();
        intent.setPackage(packageName);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setAction(Intent.ACTION_MAIN);

        if(intent.getPackage() == null){
            return;
        }

        // packageNameで指定されたアプリのメインActivityを探す
        List<ResolveInfo> list = getApplicationContext().getPackageManager().queryIntentActivities(intent, 0);
        if (list.size() >= 1) {
            ResolveInfo info = list.get(0);
            ComponentName component = new ComponentName(intent.getPackage(), info.activityInfo.name);
            intent.setComponent(component);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onToastHidden(AndroidMusicMediaInfo mediaInfo) {
    }

}
