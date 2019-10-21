package jp.pioneer.carsync.presentation.presenter;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.ControlSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferMusicApp;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.MusicApp;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.model.SourceSelectItem;
import jp.pioneer.carsync.presentation.view.SourceSelectView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * ソース選択のPresenter
 */
@PresenterLifeCycle
public class SourceSelectPresenter extends Presenter<SourceSelectView> {
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mGetCase;
    @Inject ControlSource mControlSource;
    @Inject AppSharedPreference mPreference;
    @Inject PreferMusicApp mPreferMusicApp;
    @Inject ControlAppMusicSource mControlAppMusicSource;

    private Set<MediaSourceType> mAvailableTypeSet = new HashSet<>();
    private List<SourceSelectItem> mSourceSelectList = new ArrayList<>();
    private boolean isScrolled = false;
    private int mCurrentPosition = -1;
    private List<SourceSelectItem> mAllSourceList = new ArrayList<SourceSelectItem>() {{
        add(new SourceSelectItem(MediaSourceType.OFF, R.string.src_013, R.drawable.p0090_off));
        add(new SourceSelectItem(MediaSourceType.SIRIUS_XM, R.string.src_003, R.drawable.p0089_sxm));
        add(new SourceSelectItem(MediaSourceType.DAB, R.string.src_014, R.drawable.p0083_dab));
        add(new SourceSelectItem(MediaSourceType.RADIO, R.string.src_004, R.drawable.p0083_radio));
        add(new SourceSelectItem(MediaSourceType.HD_RADIO, R.string.src_015, R.drawable.p1091_hdradio));
        add(new SourceSelectItem(MediaSourceType.CD, R.string.src_005, R.drawable.p0084_cd));
        add(new SourceSelectItem(MediaSourceType.APP_MUSIC, R.string.src_006, R.drawable.p0081_music));
        add(new SourceSelectItem(MediaSourceType.USB, R.string.src_007, R.drawable.p0085_usb));
        add(new SourceSelectItem(MediaSourceType.PANDORA, R.string.src_008, R.drawable.p0087_pandora));
        add(new SourceSelectItem(MediaSourceType.SPOTIFY, R.string.src_009, R.drawable.p0088_spotify));
        add(new SourceSelectItem(MediaSourceType.AUX, R.string.src_010, R.drawable.p0086_aux));
        add(new SourceSelectItem(MediaSourceType.TI, R.string.src_011, R.drawable.p1391_ti));
        add(new SourceSelectItem(MediaSourceType.BT_AUDIO, R.string.src_012, R.drawable.p0082_bta));
    }};

    /**
     * コンストラクタ
     */
    @Inject
    public SourceSelectPresenter() {
    }

    @Override
    public void onInitialize() {
        mCurrentPosition= -1;
        setScrolled(false);
    }

    @Override
    void onTakeView() {
        Optional.ofNullable(getView()).ifPresent(view -> view.setColor(mPreference.getUiColor().getResource()));
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        updateView();
        updateCurrentSource();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    public boolean isScrolled() {
        return isScrolled;
    }

    public void setScrolled(boolean scrolled) {
        isScrolled = scrolled;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        mCurrentPosition = currentPosition;
    }

    private CarDeviceStatus getCarDeviceStatus() {
        StatusHolder holder = mGetCase.execute();
        return holder.getCarDeviceStatus();
    }

    private void updateView() {
        CarDeviceStatus deviceStatus = getCarDeviceStatus();
        Set<MediaSourceType> typeSet = deviceStatus.availableSourceTypes;
        mAvailableTypeSet = new HashSet<>(typeSet);
        mSourceSelectList.clear();
        for(SourceSelectItem item : mAllSourceList){
            if(mAvailableTypeSet.contains(item.sourceType)){
                mSourceSelectList.add(item);
            }
        }
        AppSharedPreference.Application[] musicAppList = mPreferMusicApp.getSelectedAppList();
        Stream.of(musicAppList).forEach(app -> {
            if (app.packageName.equals(MusicApp.PANDORA.getPackageName())) {
                if (mAvailableTypeSet.contains(MediaSourceType.PANDORA)) {
                    return; // 利用可能なソースにPandoraがある場合は3rdアプリからは除外
                }
            }
            if (app.packageName.equals(MusicApp.SPOTIFY.getPackageName())) {
                if (mAvailableTypeSet.contains(MediaSourceType.SPOTIFY)) {
                    return; // 利用可能なソースにSpotifyがある場合は3rdアプリからは除外
                }
            }
            mSourceSelectList.add(new SourceSelectItem(app.packageName, app.label));
        });

        Optional.ofNullable(getView()).ifPresent(view -> view.setAdapter(mSourceSelectList));
    }

    private void updateCurrentSource() {
        Optional.ofNullable(getView()).ifPresent(view -> {
            if(mCurrentPosition!=-1){
                view.setCurrentSource(mCurrentPosition);
            }else {
                CarDeviceStatus deviceStatus = getCarDeviceStatus();
                MediaSourceType currentType = deviceStatus.sourceType;
                int position = 0;
                for (int i = 0; i < mSourceSelectList.size(); i++) {
                    if (mSourceSelectList.get(i).sourceType == currentType) {
                        position = i;
                        break;
                    }
                }
                view.setCurrentSource(position);
            }
        });
    }

    /**
     * ソース種別変更アクション
     *
     * @param type ソース種別
     */
    public void onChangeSourceAction(MediaSourceType type) {
        CarDeviceStatus deviceStatus = getCarDeviceStatus();
        MediaSourceType currentType = deviceStatus.sourceType;
        if (type == currentType) {
            Optional.ofNullable(getView()).ifPresent(SourceSelectView::dismissDialog);
        }
        mControlSource.selectSource(type);

    }

    /**
     * 音楽アプリ選択アクション
     */
    public void onSelectAudioAppAction(){
        CarDeviceStatus deviceStatus = getCarDeviceStatus();
        MediaSourceType currentType = deviceStatus.sourceType;
        AppStatus appStatus = mGetCase.execute().getAppStatus();
        appStatus.isLaunchedThirdPartyAudioApp = true;
        if(currentType == MediaSourceType.APP_MUSIC) {
            mControlAppMusicSource.abandonFocus();
        }
        onChangeSourceAction(MediaSourceType.APP_MUSIC);
    }

    /**
     * ソース種別変更イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaSourceTypeChangeAction(MediaSourceTypeChangeEvent event) {
        Optional.ofNullable(getView()).ifPresent(SourceSelectView::dismissDialog);
    }

    /**
     * 車載機ステータス変更イベント通知
     *
     * @param event 更新イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCarDeviceStatusChangeEvent(CarDeviceStatusChangeEvent event) {
        CarDeviceStatus deviceStatus = getCarDeviceStatus();
        Set<MediaSourceType> typeSet = deviceStatus.availableSourceTypes;
        if (!typeSet.equals(mAvailableTypeSet)) {
            Optional.ofNullable(getView()).ifPresent(SourceSelectView::dismissDialog);
        }
    }

    /**
     * カスタマイズ押下アクション
     */
    public void onCustomizeAction() {
        setScrolled(false);
        mEventBus.post(new NavigateEvent(ScreenId.SOURCE_APP_SETTING, null));
    }

    /**
     * 戻るアクション
     */
    public void onBackAction() {
        setScrolled(false);
        Optional.ofNullable(getView()).ifPresent(SourceSelectView::dismissDialog);
    }
}
