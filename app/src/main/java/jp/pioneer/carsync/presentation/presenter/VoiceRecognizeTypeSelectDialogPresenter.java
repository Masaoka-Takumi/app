package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.speech.tts.Voice;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.event.VoiceRecognitionTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.presentation.model.SourceAppModel;
import jp.pioneer.carsync.presentation.view.VoiceRecognizeTypeSelectDialogView;
import jp.pioneer.carsync.presentation.view.YouTubeLinkSearchItemDialogView;
import timber.log.Timber;

/**
 * 音声認識切り替え画面のPresenter
 */
@PresenterLifeCycle
public class VoiceRecognizeTypeSelectDialogPresenter extends Presenter<VoiceRecognizeTypeSelectDialogView> {
    @Inject
    GetStatusHolder mGetStatusHolder;
    @Inject
    Context mContext;
    @Inject
    EventBus mEventBus;
    @Inject
    AppSharedPreference mPreference;
    private ArrayList<VoiceRecognizeType> mVoiceTypeList = new ArrayList<>();
    @Inject
    public VoiceRecognizeTypeSelectDialogPresenter(){
    }

    @Override
    void onResume() {
        if(!mEventBus.isRegistered(this)){
            mEventBus.register(this);
        }
        updateView();
    }

    @Override
    void onPause(){
        mEventBus.unregister(this);
    }

    private void updateView() {
        mVoiceTypeList.clear();
        mVoiceTypeList.add(VoiceRecognizeType.PIONEER_SMART_SYNC);
        if(mPreference.getLastConnectedCarDeviceAndroidVr()){
            mVoiceTypeList.add(VoiceRecognizeType.ANDROID_VR);
        }
        if(mGetStatusHolder.execute().getAppStatus().isAlexaAvailableCountry){
            mVoiceTypeList.add(VoiceRecognizeType.ALEXA);
        }
        VoiceRecognizeType currentType;
        currentType = mPreference.getVoiceRecognitionType();
        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setAdapter(mVoiceTypeList);
            view.setSelectedItem(mVoiceTypeList.indexOf(currentType));
        });
    }

    public void onSelectItem(VoiceRecognizeType type){
        mPreference.setVoiceRecognitionType(type);
        Optional.ofNullable(getView()).ifPresent(VoiceRecognizeTypeSelectDialogView::callbackClose);
        mEventBus.post(new VoiceRecognitionTypeChangeEvent());
    }

    /**
     * ソース変更イベントハンドラ
     *
     * @param ev ソース変更イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaSourceTypeChangeEvent(MediaSourceTypeChangeEvent ev) {
        Optional.ofNullable(getView()).ifPresent(VoiceRecognizeTypeSelectDialogView::callbackClose);
    }
}
