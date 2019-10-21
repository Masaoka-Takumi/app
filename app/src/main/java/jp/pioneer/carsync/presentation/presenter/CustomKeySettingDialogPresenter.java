package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferMusicApp;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.model.CustomKey;
import jp.pioneer.carsync.presentation.model.CustomKeyItem;
import jp.pioneer.carsync.presentation.model.SourceAppModel;
import jp.pioneer.carsync.presentation.view.CustomKeySettingDialogView;
import timber.log.Timber;

/**
 * カスタムキー割当画面のPresenter
 */
@PresenterLifeCycle
public class CustomKeySettingDialogPresenter extends Presenter<CustomKeySettingDialogView> {
    @Inject GetStatusHolder mGetStatusHolder;
    @Inject Context mContext;
    @Inject SourceAppModel mModel;
    @Inject EventBus mEventBus;
    @Inject PreferMusicApp mPreferMusicApp;
    @Inject AppSharedPreference mPreference;
    private MediaSourceType mCurrentSourceType;
    // 画面に表示するリスト
    private ArrayList<CustomKeyItem> mCustomKeyArrayList;
    // ダイレクトソース切替で表示する全項目のリスト
    private List<MediaSourceType> mDirectSourceList = new ArrayList<MediaSourceType>(){
        {
            add(MediaSourceType.SIRIUS_XM);
            add(MediaSourceType.DAB);
            add(MediaSourceType.RADIO);
            add(MediaSourceType.HD_RADIO);
            add(MediaSourceType.CD);
            add(MediaSourceType.APP_MUSIC);
            add(MediaSourceType.USB);
            add(MediaSourceType.PANDORA);
            add(MediaSourceType.SPOTIFY);
            add(MediaSourceType.AUX);
            add(MediaSourceType.TI);
            add(MediaSourceType.BT_AUDIO);
        }
    };

    @Inject
    public CustomKeySettingDialogPresenter(){
    }

    @Override
    void onTakeView() {
        Timber.i("onTakeView");
        mCustomKeyArrayList = new ArrayList<>(); // カスタムキー割当画面表示用ArrayList
        // 固定項目のソース切替、ソースON/OFF、ソース一覧表示を追加
        mCustomKeyArrayList.add(CustomKeyItem.newSourceChangeInstance());
        mCustomKeyArrayList.add(CustomKeyItem.newSourceOnOffInstance());
        mCustomKeyArrayList.add(CustomKeyItem.newSourceListInstance());

        // 車載機でサポートされているダイレクトソースのみリスト追加
        StatusHolder holder = mGetStatusHolder.execute();
        CarDeviceSpec spec = holder.getCarDeviceSpec();
        // サポート対象ソース集
        Set<MediaSourceType> supportedSources = spec.supportedSources;
        for(MediaSourceType mediaSourceType : mDirectSourceList){
            if(supportedSources.contains(mediaSourceType)){
                CustomKeyItem customKeyItem = CustomKeyItem.newSourceDirectInstance(mediaSourceType);
                if(customKeyItem != null){
                    mCustomKeyArrayList.add(customKeyItem);
                }
            }
        }

        // インストールされている音楽対象アプリ一覧
        List<ApplicationInfo> installedAppList = mPreferMusicApp.getInstalledTargetAppList();
        // 選択済み3rdAppリスト
        AppSharedPreference.Application[] selectedApps = mPreferMusicApp.getSelectedAppList();
        // 選択済み3rdAppがインストールされている対象アプリ一覧に含まれている場合のみリスト追加
        for (AppSharedPreference.Application app : selectedApps) {
            if(isInstalled(installedAppList, app.packageName)){
                mCustomKeyArrayList.add(CustomKeyItem.newThirdAppInstance(app));
            }
        }


        // 設定済みのカスタムキーのタイプ取得
        CustomKey setCustomKey = mPreference.getCustomKeyType();

        if(setCustomKey == CustomKey.SOURCE_DIRECT){ // ダイレクトソース切替の場合
            MediaSourceType setMediaSourceType = mPreference.getCustomKeyDirectSource();
           if(!supportedSources.contains(setMediaSourceType)){
               // Preferenceに設定済みのカスタムキーが車載機でサポートされていない場合、ソース切替に変更
               mPreference.setCustomKeyType(CustomKey.SOURCE_CHANGE);
           }
        }
        else if(setCustomKey == CustomKey.THIRD_PARTY_APP){ // 3rd Appの場合
            AppSharedPreference.Application setApplication = mPreference.getCustomKeyMusicApp();
            if(!Arrays.asList(selectedApps).contains(setApplication) || !isInstalled(installedAppList, setApplication.packageName)){
                // Preferenceに設定済みの3rdAppが無効orインストールされていない場合、ソース切替に変更
                mPreference.setCustomKeyType(CustomKey.SOURCE_CHANGE);
                Timber.i("3rdApp is not selected or installed.");
            }
        }


        // ArrayListをAdapterにセット
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setAdapter(mCustomKeyArrayList);
        });
    }

    @Override
    void onInitialize() {
        Timber.i("onInitialize");
        // 画面表示時のソースを覚える(バックグラウンドでソースが切り替わった場合に画面を閉じるため)
        mCurrentSourceType = mGetStatusHolder.execute().getCarDeviceStatus().sourceType;
    }

    @Override
    void onResume() {
        Timber.i("onResume");
        if(!mEventBus.isRegistered(this)){
            mEventBus.register(this);
        }

        // ソースが画面表示時と異なる場合は画面を閉じる
        // (アプリがバックグラウンドだとイベントを受け取ることができないため)
        StatusHolder holder = mGetStatusHolder.execute();
        if(holder.getCarDeviceStatus().sourceType != mCurrentSourceType){
            Optional.ofNullable(getView()).ifPresent(view ->{
                view.callbackClose();
            });
        }

        // Adapterにカスタムキーの初期値をセット
        setCustomKeyToAdapter();
    }

    @Override
    void onPause(){
        mEventBus.unregister(this);
    }

    /**
     * CustomKey選択時の処理(Preferenceへの保存)
     *
     * @param selectedCustomKeyItem 選択されたリスト項目のオブジェクト
     */
    public void saveCustomKeyToPreference(CustomKeyItem selectedCustomKeyItem){
        if(selectedCustomKeyItem.getCustomKey() == CustomKey.SOURCE_CHANGE
            || selectedCustomKeyItem.getCustomKey() == CustomKey.SOURCE_ON_OFF
            || selectedCustomKeyItem.getCustomKey() == CustomKey.SOURCE_LIST){
            // ソース切替、ソースON/OFF、ソース一覧表示の場合
            mPreference.setCustomKeyType(selectedCustomKeyItem.getCustomKey());
        }
        else if(selectedCustomKeyItem.getCustomKey() == CustomKey.SOURCE_DIRECT){
            // ダイレクトソース切替の場合
            mPreference.setCustomKeyType(CustomKey.SOURCE_DIRECT);
            mPreference.setCustomKeyDirectSource(selectedCustomKeyItem.getDirectSource());
        }
        else if(selectedCustomKeyItem.getCustomKey() == CustomKey.THIRD_PARTY_APP){
            // 3rd Appの場合
            mPreference.setCustomKeyType(CustomKey.THIRD_PARTY_APP);
            mPreference.setCustomKeyMusicApp(selectedCustomKeyItem.getApplication());
        }
    }

    /**
     * カスタムキー設定値の読み込みとリスト該当positionをAdapterにセット
     */
    private void setCustomKeyToAdapter(){
        Optional.ofNullable(getView()).ifPresent(view -> {
            // Preferenceからカスタムキーの種類を取得
            CustomKey setCustomKeyType = mPreference.getCustomKeyType();

            // カスタムキー割当画面に表示するリスト上の該当項目のpositionを取得
            if (setCustomKeyType == CustomKey.SOURCE_CHANGE
                || setCustomKeyType == CustomKey.SOURCE_ON_OFF
                || setCustomKeyType == CustomKey.SOURCE_LIST){
                // ソース切替、ソースON/OFF、ソース一覧表示の場合
                for (CustomKeyItem item : mCustomKeyArrayList) {
                    if (setCustomKeyType == item.getCustomKey()) {
                        view.setSelectedItem(mCustomKeyArrayList.indexOf(item));
                        break;
                    }
                }
            }
            else if (setCustomKeyType == CustomKey.SOURCE_DIRECT) {
                // ダイレクトソース切替の場合
                MediaSourceType setMediaSourceType = mPreference.getCustomKeyDirectSource();

                for (CustomKeyItem item : mCustomKeyArrayList) {
                    if (item.getCustomKey() == CustomKey.SOURCE_DIRECT && setMediaSourceType == item.getDirectSource()) {
                        view.setSelectedItem(mCustomKeyArrayList.indexOf(item));
                        break;
                    }
                }
            }
            else if (setCustomKeyType == CustomKey.THIRD_PARTY_APP) {
                // 3rd Appの場合
                AppSharedPreference.Application setMusicApplication = mPreference.getCustomKeyMusicApp();

                for (CustomKeyItem item : mCustomKeyArrayList) {
                    if (item.getCustomKey() == CustomKey.THIRD_PARTY_APP && item.getApplication().equals(setMusicApplication)) {
                        view.setSelectedItem(mCustomKeyArrayList.indexOf(item));
                        break;
                    }
                }
            }
        });
    }

    /**
     * インストールされている対象アプリ一覧(apps)に引数packageNameと同じアプリが含まれているかを判断
     */
    private boolean isInstalled(List<ApplicationInfo> apps, String packageName){
        for(int i = 0; i < apps.size(); i++){
            if(packageName.equals(apps.get(i).packageName)){
                return true;
            }
        }
        return false;
    }

    /**
     * ソース変更イベントハンドラ
     *
     * @param ev ソース変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaSourceTypeChangeEvent(MediaSourceTypeChangeEvent ev){
        // ソースが切り替わったらカスタムキー割当画面を閉じる
        Timber.i("MediaSourceTypeChangeEvent");
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.callbackClose();
        });
    }
}
