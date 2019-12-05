package jp.pioneer.carsync.presentation.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.Analytics;
import jp.pioneer.carsync.application.content.AnalyticsEventManager;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.ControlSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferMusicApp;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RdsInterruptionType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.SourceChangeReasonEvent;
import jp.pioneer.carsync.presentation.model.CustomKey;
import timber.log.Timber;

@Singleton
public class CustomKeyActionHandler {

    @Inject Context mContext;
    @Inject AppSharedPreference mPreference;
    @Inject ControlSource mControlSource;
    @Inject GetStatusHolder mStatusHolder;
    @Inject PreferMusicApp mPreferMusicApp;
    @Inject ControlAppMusicSource mControlAppMusicSource;
    @Inject AnalyticsEventManager mAnalytics;
    private EventBus mEventBus;
    private Runnable mRunnable = null;
    private final long NO_ACTION_TIME = 1000; // ソースON動作を無視する時間

    @Inject
    public CustomKeyActionHandler(EventBus eventBus) {
        mEventBus = eventBus;
        mEventBus.register(this);
    }

    public void setSourceListAction(Runnable runnable) {
        this.mRunnable = runnable;
    }

    public void execute(){
        StatusHolder holder = mStatusHolder.execute();
        MediaSourceType currentMediaSource = holder.getCarDeviceStatus().sourceType;
        CustomKey customKey = mPreference.getCustomKeyType();

        //カスタムの割り当てがソース切替の場合
        switch (customKey) {
            case SOURCE_CHANGE://カスタムの割り当てがソース切替の場合
                mEventBus.post(new SourceChangeReasonEvent(Analytics.SourceChangeReason.appCustomKey));
                mControlSource.changeNextSource();
                break;

            case SOURCE_ON_OFF://カスタムの割り当てがソースOFF/ONの場合
                if (currentMediaSource == MediaSourceType.OFF) {
                    // ソースONの動作(ラストソース復帰、ラストソースが無効ならソース切替)
                    sourceOnAction(holder);
                } else {
                    // ソースOFFの動作(ラストソースを保持してソースOFF)
                    sourceOffAction(holder, currentMediaSource);
                }
                break;

            case SOURCE_LIST://カスタムの割り当てがソース選択画面表示の場合
                // RDS割り込み中は何もしない
                if(currentMediaSource == MediaSourceType.RADIO && holder.getCarDeviceMediaInfoHolder().radioInfo.rdsInterruptionType != RdsInterruptionType.NORMAL){
                    return;
                }
                if(mRunnable != null){
                    mRunnable.run();
                }
                break;

            case SOURCE_DIRECT://カスタムの割り当てがダイレクトソース切替の場合
                MediaSourceType sourceType = mPreference.getCustomKeyDirectSource();
                CarDeviceSpec spec = holder.getCarDeviceSpec();
                Set<MediaSourceType> supportedSources = spec.supportedSources;
                // 選択するソースがサポート対象から無くなっている場合、設定値をソース切替に変更
                if(!supportedSources.contains(sourceType)){
                    mPreference.setCustomKeyType(CustomKey.SOURCE_CHANGE);
                    mControlSource.changeNextSource();
                    return;
                }
                Set<MediaSourceType> availableSources = holder.getCarDeviceStatus().availableSourceTypes;
                if (availableSources.contains(sourceType)) {
                    //割り当てたソースが有効の場合
                    mEventBus.post(new SourceChangeReasonEvent(Analytics.SourceChangeReason.appCustomKeyDirectSource));
                    mControlSource.selectSource(sourceType);
                } else {
                    //割り当てたソースが無効の場合、なにもしない
                }
                break;

            case THIRD_PARTY_APP://カスタムの割り当てが3rdApp切替の場合
                AppSharedPreference.Application customKeyMusicApp = mPreference.getCustomKeyMusicApp();
                // インストールされている音楽対象アプリ一覧
                List<ApplicationInfo> installedAppList = mPreferMusicApp.getInstalledTargetAppList();
                // 選択済み3rdAppリスト
                AppSharedPreference.Application[] selectedApps = mPreferMusicApp.getSelectedAppList();

                // 設定されている3rdAppが音楽アプリ選択画面で選択されていない場合、設定値をソース切替に変更
                if(!Arrays.asList(selectedApps).contains(customKeyMusicApp)){
                    mPreference.setCustomKeyType(CustomKey.SOURCE_CHANGE);
                    mControlSource.changeNextSource();
                    return;
                }

                // 設定されている3rdAppがアンインストールされている場合、設定値をソース切替に変更
                if(!isInstalled(installedAppList, customKeyMusicApp.packageName)){
                    Timber.i("3rd app is uninstalled.");
                    mPreference.setCustomKeyType(CustomKey.SOURCE_CHANGE);
                    mControlSource.changeNextSource();
                    return;
                }

                // 設定されている3rdAppの起動
                try {
                    PackageManager pm = mContext.getPackageManager();
                    Intent intent = pm.getLaunchIntentForPackage(customKeyMusicApp.packageName);
                    mContext.startActivity(intent);
                    mEventBus.post(new SourceChangeReasonEvent(Analytics.SourceChangeReason.thirdAppChange));

                    mControlSource.selectSource(MediaSourceType.APP_MUSIC);
                    AppStatus appStatus = holder.getAppStatus();
                    appStatus.isLaunchedThirdPartyAudioApp = true;
                    if(currentMediaSource == MediaSourceType.APP_MUSIC) {
                        mControlAppMusicSource.abandonFocus();
                    }
                    mAnalytics.sendThirdAppStartUpEvent(Analytics.AnalyticsThirdAppStartUp.appCustomKey);

                } catch (ActivityNotFoundException | NullPointerException ex){
                    Timber.w("Invalid music app. package name : %s", customKeyMusicApp.packageName);
                    Toast.makeText(mContext, mContext.getString(R.string.err_017), Toast.LENGTH_LONG).show();
                }
                break;

            default:
                break;
        }
    }


    /**
     * カスタムキー[ソースOFF/ON]のソースONにする動作の処理
     * ラストソースに復帰できればそれに復帰し、ラストソースが無効ならソース切替を車載機に送信
     * 連打によるラストソース以外に復帰する問題を防ぐため、ソースがONになるまで実行を無視する処理あり
     * @param holder StatusHolder ラストソースを読み込むため
     */
    private void sourceOnAction(StatusHolder holder){
        long currentTime = System.currentTimeMillis();
        // 最後にソースONを実行した時刻
        long startTime = holder.getAppStatus().lastSourceOnTime;

        // 初回実行時 or 最後の実行から一定時間経過した場合のみラストソース復帰処理を実行
        if(startTime == 0L || (currentTime - startTime) >= NO_ACTION_TIME) {
            // ラストソース復帰の実行時刻(現在時刻)を保持
            holder.getAppStatus().lastSourceOnTime = currentTime;
            mEventBus.post(new SourceChangeReasonEvent(Analytics.SourceChangeReason.getAppCustomKeySourceOnOff));

            // ラストソースに復帰
            changeLastSource(holder);
        }
        // 一定時間経過前にソースONを実行する場合は無視(連打によってラストソース以外になることを防ぐ)
        else { //  if ((currentTime - startTime) < NO_ACTION_TIME)
            return;
        }
    }

    /**
     * ラストソースに復帰する処理
     * ラストソースがないor車載機で無効状態の場合はソース切替を車載機に送信
     * @param holder StatusHolder ラストソースを読み込むため
     */
    private void changeLastSource(StatusHolder holder){
        // ソースOFF前のラストソースを読み込み
        MediaSourceType lastDirectSource = holder.getAppStatus().lastDirectSource;

        // 車載機で有効なダイレクトソース集を取得
        CarDeviceStatus carDeviceStatus = holder.getCarDeviceStatus();
        Set<MediaSourceType> availableSources =carDeviceStatus.availableSourceTypes;

        // ラストソースが未設定orラストソースが無効の場合はソース切替、有効な場合はそのソースを車載機に送信
        if(lastDirectSource == null || !availableSources.contains(lastDirectSource)){
            mControlSource.changeNextSource();
        }
        else {
            mControlSource.selectSource(lastDirectSource);
        }

        // ラストソースの設定値をクリア
        holder.getAppStatus().lastDirectSource = null;
    }

    /**
     * カスタムキー[ソースOFF/ON]のソースOFFにする動作の処理
     * ラストソースをStatusHolderに保持してソースOFFを車載機に送信
     * @param holder StatusHolder ラストソースを保持するため
     * @param currentMediaSource 現在のソース種類
     */
    private void sourceOffAction(StatusHolder holder, MediaSourceType currentMediaSource){
        // 直前のソースを保持してソースOFF
        holder.getAppStatus().lastDirectSource = currentMediaSource;
        mEventBus.post(new SourceChangeReasonEvent(Analytics.SourceChangeReason.getAppCustomKeySourceOnOff));
        mControlSource.selectSource(MediaSourceType.OFF);
    }

    /**
     * インストールされている対象アプリ一覧(apps)に引数packageNameと同じアプリが含まれているかを判断
     */
    private boolean isInstalled(List<ApplicationInfo> installedAppList, String packageName){
        for(int i = 0; i < installedAppList.size(); i++){
            if(packageName.equals(installedAppList.get(i).packageName)){
                return true;
            }
        }
        return false;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaSourceTypeChangeEvent(MediaSourceTypeChangeEvent ev){
        // ソースがONになったらカスタムキー[ソースON/OFF]の実行時刻をクリア
        StatusHolder holder = mStatusHolder.execute();
        if(holder.getCarDeviceStatus().sourceType != MediaSourceType.OFF) {
            holder.getAppStatus().lastSourceOnTime = 0L;
        }
    }
}
