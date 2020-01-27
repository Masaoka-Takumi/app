package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.application.util.AppUtil;
import jp.pioneer.carsync.domain.event.MediaSourceTypeChangeEvent;
import jp.pioneer.carsync.domain.event.VoiceRecognitionTypeChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.presentation.event.BackgroundChangeEvent;
import jp.pioneer.carsync.presentation.view.PlayerContainerView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.player.PlayerContainerFragment;

/**
 * 再生コンテナのpresenter
 */
@PresenterLifeCycle
public class PlayerContainerPresenter extends Presenter<PlayerContainerView> {

    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject GetStatusHolder mUseCase;
    @Inject AppSharedPreference mPreference;
    private MediaSourceType mPreviousSourceType;
    private boolean mIsCalling = false;
    private boolean mIsAvailableSource;
    private Set<MediaSourceType> mAvailableTypeSet = new HashSet<>();
    private Bundle mArguments;
    @Inject
    public PlayerContainerPresenter() {
    }

    public void setCalling(boolean calling) {
        mIsCalling = calling;
    }

    @Override
    void onInitialize() {
        mPreviousSourceType = null;
        StatusHolder holder = mUseCase.execute();
        Set<MediaSourceType> typeSet = holder.getCarDeviceStatus().availableSourceTypes;
        mAvailableTypeSet = new HashSet<>(typeSet);
        navigate();
        //HandlerにしないとBlurが有効にならないことがある
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(mArguments.getBoolean(PlayerContainerFragment.KEY_HOME_SOURCE_OFF)){
                    mEventBus.post(new BackgroundChangeEvent(true));
                    Optional.ofNullable(getView()).ifPresent(view -> {
                       view.navigate(ScreenId.SOURCE_SELECT, Bundle.EMPTY);
                    });
                    mArguments.putBoolean(PlayerContainerFragment.KEY_HOME_SOURCE_OFF,false);
                }
            }
        });
    }

    @Override
    void onResume() {
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }
        navigate();
    }

    @Override
    void onPause() {
        mEventBus.unregister(this);
    }

    @Override
    void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        if(savedInstanceState.containsKey("source_type")) {
            mPreviousSourceType = MediaSourceType.valueOf((byte) savedInstanceState.getInt("source_type"));
        }
    }

    @Override
    void onSaveInstanceState(@NonNull Bundle outState) {
        if(mPreviousSourceType != null) {
            outState.putInt("source_type", mPreviousSourceType.code);
        }
    }
    public void setArgument(Bundle args) {
        mArguments = args;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMediaSourceTypeChangeEvent(MediaSourceTypeChangeEvent ev) {
        navigate();
    }

    private void navigate() {
        StatusHolder holder = mUseCase.execute();

        Optional.ofNullable(getView()).ifPresent(view -> {
            MediaSourceType currentSourceType = holder.getCarDeviceStatus().sourceType;
            if(currentSourceType==MediaSourceType.USB||currentSourceType==MediaSourceType.CD){
                Set<MediaSourceType> typeSet = holder.getCarDeviceStatus().availableSourceTypes;
                mAvailableTypeSet = new HashSet<>(typeSet);
            }
            boolean isAvailable = mAvailableTypeSet.contains(currentSourceType);
            /*
             * Resume中かつソース変更時かつ電話発話中でない時にnavigateする。
             */
            if ((mPreviousSourceType != currentSourceType || mIsAvailableSource != isAvailable) &&
                    App.getApp(mContext).isForeground() &&
                    !mIsCalling &&
                    AppUtil.isScreenOn(mContext)) {

                int srcText;
                if(!isAvailable){
                    view.navigate(ScreenId.UNSUPPORTED, Bundle.EMPTY);
                    srcText = 0;
                }else {
                    switch (currentSourceType) {
                        case RADIO:
                            view.navigate(ScreenId.RADIO, Bundle.EMPTY);
                            srcText = R.string.src_004;
                            break;
                        case SIRIUS_XM:
                            view.navigate(ScreenId.SIRIUS_XM, Bundle.EMPTY);
                            srcText = R.string.src_003;
                            break;
                        case USB:
                            view.navigate(ScreenId.USB, Bundle.EMPTY);
                            srcText = R.string.src_007;
                            break;
                        case CD:
                            view.navigate(ScreenId.CD, Bundle.EMPTY);
                            srcText = R.string.src_005;
                            break;
                        case BT_AUDIO:
                            view.navigate(ScreenId.BT_AUDIO, Bundle.EMPTY);
                            srcText = R.string.src_012;
                            break;
                        case APP_MUSIC:
                            view.navigate(ScreenId.ANDROID_MUSIC, Bundle.EMPTY);
                            srcText = R.string.src_006;
                            break;
                        case PANDORA:
                            view.navigate(ScreenId.PANDORA, Bundle.EMPTY);
                            srcText = R.string.src_008;
                            break;
                        case SPOTIFY:
                            view.navigate(ScreenId.SPOTIFY, Bundle.EMPTY);
                            srcText = R.string.src_009;
                            break;
                        case AUX:
                            view.navigate(ScreenId.AUX, Bundle.EMPTY);
                            srcText = R.string.src_010;
                            break;
                        case OFF:
                            view.navigate(ScreenId.SOURCE_OFF, Bundle.EMPTY);
                            srcText = R.string.src_013;
                            break;
                        case TI:
                            view.navigate(ScreenId.TI, Bundle.EMPTY);
                            srcText = R.string.src_011;
                            break;
                        case DAB:
                            view.navigate(ScreenId.DAB, Bundle.EMPTY);
                            srcText = R.string.src_014;
                            break;
                        case HD_RADIO:
                            view.navigate(ScreenId.HD_RADIO, Bundle.EMPTY);
                            srcText = R.string.src_015;
                            break;
                        case BT_PHONE:
                        case IPOD:
                        case DAB_INTERRUPT:
                        case HD_RADIO_INTERRUPT:
                        default:
                            view.navigate(ScreenId.UNSUPPORTED, Bundle.EMPTY);
                            srcText = 0;
                            break;
                    }
                }
                if (mPreviousSourceType != null && srcText != 0) {
                    view.displaySrcMessage(mContext.getString(srcText));
                }
                mPreviousSourceType = currentSourceType;
                mIsAvailableSource = isAvailable;
            }
        });
    }

    /**
     * VoiceRecognitionTypeChangeEvent
     * @param event VoiceRecognitionTypeChangeEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onVoiceRecognitionTypeChangeEvent(VoiceRecognitionTypeChangeEvent event) {
        VoiceRecognizeType type = mPreference.getVoiceRecognitionType();
        Optional.ofNullable(getView()).ifPresent(view -> {
            if (!mUseCase.execute().getCarDeviceStatus().androidVrEnabled) {
                if (type == VoiceRecognizeType.ALEXA) {
                    view.displayVoiceMessage(mContext.getString(type.label));
                }
            } else {
                view.displayVoiceMessage(mContext.getString(type.label));
            }
        });
    }
}
