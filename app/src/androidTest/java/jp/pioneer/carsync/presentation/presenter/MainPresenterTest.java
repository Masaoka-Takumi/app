package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.EnumSet;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.AppMusicPlayPositionChangeEvent;
import jp.pioneer.carsync.domain.interactor.CheckAvailableTextToSpeech;
import jp.pioneer.carsync.domain.interactor.ControlSource;
import jp.pioneer.carsync.domain.interactor.GetAddressFromLocationName;
import jp.pioneer.carsync.domain.interactor.GetRunningStatus;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.JudgeVoiceCommand;
import jp.pioneer.carsync.domain.interactor.PreferAdas;
import jp.pioneer.carsync.domain.interactor.PrepareSpeechRecognizer;
import jp.pioneer.carsync.domain.interactor.ReadText;
import jp.pioneer.carsync.domain.model.AdasWarningEvent;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.VoiceCommand;
import jp.pioneer.carsync.domain.model.VoiceRecognitionSearchType;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStartedEvent;
import jp.pioneer.carsync.infrastructure.crp.event.CrpSessionStoppedEvent;
import jp.pioneer.carsync.presentation.controller.MainFragmentController;
import jp.pioneer.carsync.presentation.event.BackgroundChangeEvent;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.event.ShowCautionEvent;
import jp.pioneer.carsync.presentation.view.MainView;
import jp.pioneer.carsync.presentation.view.argument.SearchContentParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.array;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MainActivityのpresenterテスト
 */
public class MainPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks MainPresenter mPresenter = new MainPresenter();
    @Mock MainView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock AppSharedPreference mPreference;
    @Mock GetStatusHolder mStatusCase;
    @Mock PrepareSpeechRecognizer mMicCase;
    @Mock JudgeVoiceCommand mJudgeVoiceCase;
    @Mock GetAddressFromLocationName mLocationCase;
    @Mock MainFragmentController mController;
    @Mock GetRunningStatus mGetRunningStatus;
    @Mock CheckAvailableTextToSpeech mCheckTtsCase;
    @Mock ReadText mReadText;
    @Mock ControlSource mControlSource;
    @Mock PreferAdas mPreferAdas;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
    }

    @Test
    public void testOnInitialize() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);

        when(mStatusCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        when(mView.isShowCaution()).thenReturn(false);

        mPresenter.takeView(mView);
        mPresenter.initialize();

        verify(mView).navigate(eq(ScreenId.OPENING), any(Bundle.class));
    }

    @Test
    public void testOnInitializeAlreadyShown() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);

        when(mStatusCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        when(mView.isShowCaution()).thenReturn(true);

        mPresenter.takeView(mView);
        mPresenter.initialize();

        verify(mView).navigate(eq(ScreenId.OPENING), any(Bundle.class));
        verify(mView, never()).showCaution(any(Bundle.class));
    }

    @Test
    public void testOnInitializeNotConnected() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);

        when(mStatusCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getSessionStatus()).thenReturn(SessionStatus.STOPPED);

        mPresenter.takeView(mView);
        mPresenter.initialize();

        verify(mView).navigate(eq(ScreenId.OPENING), any(Bundle.class));
        verify(mView, never()).showCaution(any(Bundle.class));
    }

    @Test
    public void testOnAppSharedPreferenceChanged() throws Exception {
        when(mPreference.getBackgroundType()).thenReturn(1);
        when(mPreference.getBackgroundPictureId()).thenReturn(1);
        when(mPreference.getBackgroundVideoId()).thenReturn(1);

        mPresenter.takeView(mView);
        mPresenter.setActive(true);
        mPresenter.onAppSharedPreferenceChanged(mPreference, AppSharedPreference.KEY_BACKGROUND_TYPE);
        mPresenter.onAppSharedPreferenceChanged(mPreference, AppSharedPreference.KEY_BACKGROUND_VIDEO_ID);
        when(mPreference.getBackgroundType()).thenReturn(0);
        mPresenter.onAppSharedPreferenceChanged(mPreference, AppSharedPreference.KEY_BACKGROUND_PICTURE_ID);

        verify(mView).changeBackgroundType(true);
        verify(mView).changeBackgroundImage(1);
        verify(mView).changeBackgroundVideo(any(Uri.class));
    }

    @Test
    public void testOnShowAccidentDetectAction() throws Exception {
        when(mView.isShowAccidentDetect()).thenReturn(false);

        mPresenter.takeView(mView);
        mPresenter.onShowAccidentDetectAction(Bundle.EMPTY);

        verify(mView).showAccidentDetect(any(Bundle.class));
    }

    @Test
    public void testOnShowAccidentDetectActionAlreadyShown() throws Exception {
        when(mView.isShowAccidentDetect()).thenReturn(true);

        mPresenter.takeView(mView);
        mPresenter.onShowAccidentDetectAction(Bundle.EMPTY);

        verify(mView, never()).showAccidentDetect(any(Bundle.class));
    }

    @Test
    public void testOnShowParkingSensorAction() throws Exception {
        when(mView.isShowParkingSensor()).thenReturn(false);

        mPresenter.takeView(mView);
        mPresenter.onShowParkingSensorAction(Bundle.EMPTY);

        verify(mView).showParkingSensor(any(Bundle.class));
    }

    @Test
    public void testOnShowParkingSensorActionAlreadyShown() throws Exception {
        when(mView.isShowParkingSensor()).thenReturn(true);

        mPresenter.takeView(mView);
        mPresenter.onShowParkingSensorAction(Bundle.EMPTY);

        verify(mView, never()).showParkingSensor(any(Bundle.class));
    }

    @Test
    public void testOnShowAdasWarningAction() throws Exception {
        when(mView.isShowAdasWarning()).thenReturn(false);

        mPresenter.takeView(mView);
        mPresenter.onShowAdasWarningAction(Bundle.EMPTY);

        verify(mView).showAdasWarning(any(Bundle.class));
    }

    @Test
    public void testOnShowAdasWarningActionAlreadyShown() throws Exception {
        when(mView.isShowAdasWarning()).thenReturn(true);

        mPresenter.takeView(mView);
        mPresenter.onShowAdasWarningAction(Bundle.EMPTY);

        verify(mView, never()).showAdasWarning(any(Bundle.class));
    }

    @Test
    public void testPrepareRecognizer() throws Exception {
        PrepareSpeechRecognizer.FinishBluetoothHeadset mockFinishBluetoothHeadset = mock(PrepareSpeechRecognizer.FinishBluetoothHeadset.class);

        when(mView.isShowSearchContainer()).thenReturn(false);
        when(mView.isShowSpeechRecognizerDialog()).thenReturn(false);
        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(0) instanceof PrepareSpeechRecognizer.PrepareCallback) {
                ((PrepareSpeechRecognizer.PrepareCallback) invocationOnMock.getArgument(0))
                        .onComplete(PrepareSpeechRecognizer.Device.PHONE, mockFinishBluetoothHeadset);
            }
            return null;
        }).when(mMicCase).execute(any(PrepareSpeechRecognizer.PrepareCallback.class));

        mPresenter.takeView(mView);
        mPresenter.prepareRecognizer();
        mPresenter.finishRecognizer();

        verify(mView).showSpeechRecognizerDialog(Bundle.EMPTY);
        verify(mView).startRecognizer();
        verify(mockFinishBluetoothHeadset).execute(any(PrepareSpeechRecognizer.FinishCallback.class));
    }

    @Test
    public void testPrepareRecognizerAlreadyShown() throws Exception {
        PrepareSpeechRecognizer.FinishBluetoothHeadset mockFinishBluetoothHeadset = mock(PrepareSpeechRecognizer.FinishBluetoothHeadset.class);

        when(mView.isShowSearchContainer()).thenReturn(true);
        when(mView.isShowSpeechRecognizerDialog()).thenReturn(false);
        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(0) instanceof PrepareSpeechRecognizer.PrepareCallback) {
                ((PrepareSpeechRecognizer.PrepareCallback) invocationOnMock.getArgument(0))
                        .onComplete(PrepareSpeechRecognizer.Device.PHONE, mockFinishBluetoothHeadset);
            }
            return null;
        }).when(mMicCase).execute(any(PrepareSpeechRecognizer.PrepareCallback.class));

        mPresenter.takeView(mView);
        mPresenter.prepareRecognizer();
        mPresenter.finishRecognizer();
        // 終了が2回行われないか確認
        mPresenter.finishRecognizer();

        verify(mView).dismissSearchContainer();
        verify(mView).startRecognizer();
        verify(mockFinishBluetoothHeadset, times(1)).execute(any(PrepareSpeechRecognizer.FinishCallback.class));
    }

    @Test
    public void testOnRecognizeResultsNoCategory() throws Exception {
        ArrayList<String> words = new ArrayList<>();
        words.add("TEST");
        when(mPreference.getBackgroundType()).thenReturn(0);
        when(mJudgeVoiceCase.execute(words, VoiceRecognitionSearchType.GLOBAL)).thenReturn(null);
        when(mView.getScreenId()).thenReturn(ScreenId.HOME);
        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(0) instanceof CheckAvailableTextToSpeech.Callback) {
                ((CheckAvailableTextToSpeech.Callback) invocationOnMock.getArgument(0))
                        .onResult(CheckAvailableTextToSpeech.Result.AVAILABLE);
            }
            return null;
        }).when(mCheckTtsCase).execute(any(CheckAvailableTextToSpeech.Callback.class));

        mPresenter.mCurrentScreenId = ScreenId.HOME;
        mPresenter.takeView(mView);
        mPresenter.onRecognizeResults(words);
        mCheckTtsCase.onInitializeSuccess();

        verify(mReadText).startReading(mContext.getString(R.string.vr_search_no_voice_command));
        verify(mView).finishSpeechRecognizer();
    }

    @Test
    public void testOnRecognizeResultsNaviNoSearchText() throws Exception {
        ArrayList<String> words = new ArrayList<>();

        words.add("Navi");

        AppSharedPreference.Application naviApp = new AppSharedPreference.Application("com.google.android.apps.maps", "");
        when(mPreference.getBackgroundType()).thenReturn(0);
        when(mPreference.getNavigationApp()).thenReturn(naviApp);

        JudgeVoiceCommand.JudgeResult result = new JudgeVoiceCommand.JudgeResult(VoiceCommand.NAVI, null);
        when(mJudgeVoiceCase.execute(words, VoiceRecognitionSearchType.GLOBAL)).thenReturn(result);
        when(mView.getScreenId()).thenReturn(ScreenId.HOME);
        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(0) instanceof CheckAvailableTextToSpeech.Callback) {
                ((CheckAvailableTextToSpeech.Callback) invocationOnMock.getArgument(0))
                        .onResult(CheckAvailableTextToSpeech.Result.AVAILABLE);
            }
            return null;
        }).when(mCheckTtsCase).execute(any(CheckAvailableTextToSpeech.Callback.class));

        mPresenter.mCurrentScreenId = ScreenId.HOME;
        mPresenter.takeView(mView);
        mPresenter.onRecognizeResults(words);

        verify(mReadText).startReading(mContext.getString(R.string.vr_search_no_search_word_navi));
        mPresenter.onSpeakDone();
        verify(mView).startRecognizer();
        words.set(0, "渋谷");

        Address mockAddress = mock(Address.class);
        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(1) instanceof GetAddressFromLocationName.Callback) {
                ((GetAddressFromLocationName.Callback) invocationOnMock.getArgument(1)).onSuccess(mockAddress);
            }
            return null;
        }).when(mLocationCase).execute(eq("渋谷"), any(GetAddressFromLocationName.Callback.class));
        when(mockAddress.getLatitude()).thenReturn(0.12345D);
        when(mockAddress.getLongitude()).thenReturn(0.67890D);

        mPresenter.onRecognizeResults(words);
        verify(mView).finishSpeechRecognizer();
        verify(mView).startNavigation(any(Intent.class));
    }

    @Test
    public void testOnRecognizeResultsNavi() throws Exception {
        ArrayList<String> words = new ArrayList<>();

        words.add("Navi 渋谷");

        AppSharedPreference.Application naviApp = new AppSharedPreference.Application("com.google.android.apps.maps", "");
        when(mPreference.getBackgroundType()).thenReturn(0);
        when(mPreference.getNavigationApp()).thenReturn(naviApp);

        when(mJudgeVoiceCase.execute(words, VoiceRecognitionSearchType.GLOBAL)).thenReturn(new JudgeVoiceCommand.JudgeResult(VoiceCommand.NAVI, new String[]{"渋谷"}));
        when(mView.getScreenId()).thenReturn(ScreenId.HOME);

        Address mockAddress = mock(Address.class);
        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(1) instanceof GetAddressFromLocationName.Callback) {
                ((GetAddressFromLocationName.Callback) invocationOnMock.getArgument(1)).onSuccess(mockAddress);
            }
            return null;
        }).when(mLocationCase).execute(eq("渋谷"), any(GetAddressFromLocationName.Callback.class));
        when(mockAddress.getLatitude()).thenReturn(0.12345D);
        when(mockAddress.getLongitude()).thenReturn(0.67890D);

        mPresenter.mCurrentScreenId = ScreenId.HOME;
        mPresenter.takeView(mView);
        mPresenter.onRecognizeResults(words);

        verify(mView).finishSpeechRecognizer();
        verify(mView).startNavigation(any(Intent.class));
    }

    @Test
    public void testOnRecognizeResultsNaviError() throws Exception {
        ArrayList<String> words = new ArrayList<>();
        words.add("Navi 渋谷");
        AppSharedPreference.Application naviApp = new AppSharedPreference.Application("com.google.android.apps.maps", "");
        when(mPreference.getBackgroundType()).thenReturn(0);
        when(mPreference.getNavigationApp()).thenReturn(naviApp);
        when(mView.getScreenId()).thenReturn(ScreenId.HOME);
        when(mJudgeVoiceCase.execute(words, VoiceRecognitionSearchType.GLOBAL)).thenReturn(new JudgeVoiceCommand.JudgeResult(VoiceCommand.NAVI, new String[]{"渋谷"}));
        when(mPreference.getNavigationApp()).thenReturn(naviApp);
        Address mockAddress = mock(Address.class);
        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(1) instanceof GetAddressFromLocationName.Callback) {
                ((GetAddressFromLocationName.Callback) invocationOnMock.getArgument(1)).onError(GetAddressFromLocationName.Error.NOT_FOUND);
            }
            return null;
        }).when(mLocationCase).execute(eq("渋谷"), any(GetAddressFromLocationName.Callback.class));
        when(mockAddress.getLatitude()).thenReturn(0.12345D);
        when(mockAddress.getLongitude()).thenReturn(0.67890D);

        mPresenter.mCurrentScreenId = ScreenId.HOME;
        mPresenter.takeView(mView);
        mPresenter.onRecognizeResults(words);

        verify(mView).startNavigation(any(Intent.class));
    }

    @Test
    public void testOnRecognizeResultsPhoneNoSearchText() throws Exception {
        ArrayList<String> words = new ArrayList<>();

        words.add("Phone");

        when(mPreference.getBackgroundType()).thenReturn(0);

        when(mJudgeVoiceCase.execute(words, VoiceRecognitionSearchType.GLOBAL)).thenReturn(new JudgeVoiceCommand.JudgeResult(VoiceCommand.PHONE, null));
        when(mView.getScreenId()).thenReturn(ScreenId.HOME);
        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(0) instanceof CheckAvailableTextToSpeech.Callback) {
                ((CheckAvailableTextToSpeech.Callback) invocationOnMock.getArgument(0))
                        .onResult(CheckAvailableTextToSpeech.Result.AVAILABLE);
            }
            return null;
        }).when(mCheckTtsCase).execute(any(CheckAvailableTextToSpeech.Callback.class));

        mPresenter.mCurrentScreenId = ScreenId.HOME;
        mPresenter.takeView(mView);
        mPresenter.onRecognizeResults(words);

        verify(mReadText).startReading(mContext.getString(R.string.vr_search_no_search_word_phone));

        mPresenter.onSpeakDone();
        verify(mView).startRecognizer();

        words.set(0, "佐藤");
        final ArgumentCaptor<Bundle> cap = ArgumentCaptor.forClass(Bundle.class);

        mPresenter.onRecognizeResults(words);

        verify(mView).finishSpeechRecognizer();
        verify(mView).showSearchContainer(cap.capture());
        SearchContentParams params = SearchContentParams.from(cap.getValue());
        assertThat(params.voiceCommand, is(VoiceCommand.PHONE));
        assertThat(params.searchWords, is(array(equalTo("佐藤"))));
    }

    @Test
    public void testOnRecognizeResultsPhone() throws Exception {
        ArrayList<String> words = new ArrayList<>();

        words.add("Phone 佐藤");

        when(mPreference.getBackgroundType()).thenReturn(0);

        when(mJudgeVoiceCase.execute(words, VoiceRecognitionSearchType.GLOBAL)).thenReturn(new JudgeVoiceCommand.JudgeResult(VoiceCommand.PHONE, new String[]{"佐藤"}));
        when(mView.getScreenId()).thenReturn(ScreenId.HOME);
        final ArgumentCaptor<Bundle> cap = ArgumentCaptor.forClass(Bundle.class);

        mPresenter.mCurrentScreenId = ScreenId.HOME;
        mPresenter.takeView(mView);
        mPresenter.onRecognizeResults(words);

        verify(mView).finishSpeechRecognizer();
        verify(mView).showSearchContainer(cap.capture());
        SearchContentParams params = SearchContentParams.from(cap.getValue());
        assertThat(params.voiceCommand, is(VoiceCommand.PHONE));
        assertThat(params.searchWords, is(array(equalTo("佐藤"))));
    }

    @Test
    public void testOnRecognizeResultsAudioNoSearchText() throws Exception {
        ArrayList<String> words = new ArrayList<>();

        words.add("Audio");

        when(mPreference.getBackgroundType()).thenReturn(0);

        when(mJudgeVoiceCase.execute(words, VoiceRecognitionSearchType.GLOBAL)).thenReturn(new JudgeVoiceCommand.JudgeResult(VoiceCommand.AUDIO, null));
        when(mView.getScreenId()).thenReturn(ScreenId.HOME);
        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(0) instanceof CheckAvailableTextToSpeech.Callback) {
                ((CheckAvailableTextToSpeech.Callback) invocationOnMock.getArgument(0))
                        .onResult(CheckAvailableTextToSpeech.Result.AVAILABLE);
            }
            return null;
        }).when(mCheckTtsCase).execute(any(CheckAvailableTextToSpeech.Callback.class));

        mPresenter.mCurrentScreenId = ScreenId.HOME;
        mPresenter.takeView(mView);
        mPresenter.onRecognizeResults(words);

        verify(mReadText).startReading(mContext.getString(R.string.vr_search_no_search_word_audio));

        mPresenter.onSpeakDone();
        verify(mView).startRecognizer();

        words.set(0, "Radio");
        when(mJudgeVoiceCase.judgeSearchWord(VoiceCommand.AUDIO, words.get(0))).thenReturn(JudgeVoiceCommand.JudgeResult.SOURCE_RADIO);
        mPresenter.onRecognizeResults(words);

        verify(mView).finishSpeechRecognizer();
        verify(mView).navigate(eq(ScreenId.PLAYER_CONTAINER), any(Bundle.class));
        verify(mControlSource).selectSource(MediaSourceType.RADIO);
    }

    @Test
    public void testOnRecognizeResultsAudio() throws Exception {
        ArrayList<String> words = new ArrayList<>();

        words.add("Audio Radio");

        when(mPreference.getBackgroundType()).thenReturn(0);
        when(mJudgeVoiceCase.execute(words, VoiceRecognitionSearchType.GLOBAL)).thenReturn(new JudgeVoiceCommand.JudgeResult(VoiceCommand.AUDIO, new String[]{JudgeVoiceCommand.JudgeResult.SOURCE_RADIO}));
        when(mView.getScreenId()).thenReturn(ScreenId.HOME);

        mPresenter.mCurrentScreenId = ScreenId.HOME;
        mPresenter.takeView(mView);
        mPresenter.onRecognizeResults(words);

        verify(mView).finishSpeechRecognizer();
        verify(mView).navigate(eq(ScreenId.PLAYER_CONTAINER), any(Bundle.class));
        verify(mControlSource).selectSource(MediaSourceType.RADIO);
    }

    @Test
    public void testOnRecognizeResultsSettingNoSearchText() throws Exception {
        ArrayList<String> words = new ArrayList<>();

        words.add("Setting");
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceSpec spec = new CarDeviceSpec();
        CarDeviceStatus status = new CarDeviceStatus();
        spec.systemSettingSupported = true;
        status.systemSettingEnabled = true;
        when(mockHolder.getCarDeviceStatus()).thenReturn(status);
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);
        when(mStatusCase.execute()).thenReturn(mockHolder);

        when(mPreference.getBackgroundType()).thenReturn(0);

        when(mJudgeVoiceCase.execute(words, VoiceRecognitionSearchType.GLOBAL)).thenReturn(new JudgeVoiceCommand.JudgeResult(VoiceCommand.SETTING, null));
        when(mView.getScreenId()).thenReturn(ScreenId.HOME);
        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(0) instanceof CheckAvailableTextToSpeech.Callback) {
                ((CheckAvailableTextToSpeech.Callback) invocationOnMock.getArgument(0))
                        .onResult(CheckAvailableTextToSpeech.Result.AVAILABLE);
            }
            return null;
        }).when(mCheckTtsCase).execute(any(CheckAvailableTextToSpeech.Callback.class));

        mPresenter.mCurrentScreenId = ScreenId.HOME;
        mPresenter.takeView(mView);
        mPresenter.onRecognizeResults(words);

        verify(mReadText).startReading(mContext.getString(R.string.vr_search_no_search_word_setting));

        mPresenter.onSpeakDone();
        verify(mView).startRecognizer();

        words.set(0, "System");
        when(mJudgeVoiceCase.judgeSearchWord(VoiceCommand.SETTING, words.get(0))).thenReturn(JudgeVoiceCommand.JudgeResult.SETTING_SYSTEM);

        mPresenter.onRecognizeResults(words);

        verify(mView).finishSpeechRecognizer();
        verify(mView).navigate(eq(ScreenId.SETTINGS_CONTAINER), any(Bundle.class));
    }

    @Test
    public void testOnRecognizeResultsSetting() throws Exception {
        ArrayList<String> words = new ArrayList<>();

        words.add("Setting System");
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceSpec spec = new CarDeviceSpec();
        CarDeviceStatus status = new CarDeviceStatus();
        spec.systemSettingSupported = true;
        status.systemSettingEnabled = true;
        when(mockHolder.getCarDeviceStatus()).thenReturn(status);
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);
        when(mStatusCase.execute()).thenReturn(mockHolder);
        when(mPreference.getBackgroundType()).thenReturn(0);

        when(mJudgeVoiceCase.execute(words, VoiceRecognitionSearchType.GLOBAL)).thenReturn(new JudgeVoiceCommand.JudgeResult(VoiceCommand.SETTING, new String[]{JudgeVoiceCommand.JudgeResult.SETTING_SYSTEM}));
        when(mView.getScreenId()).thenReturn(ScreenId.HOME);

        mPresenter.mCurrentScreenId = ScreenId.HOME;
        mPresenter.takeView(mView);
        mPresenter.onRecognizeResults(words);

        verify(mView).finishSpeechRecognizer();
        verify(mView).navigate(eq(ScreenId.SETTINGS_CONTAINER), any(Bundle.class));
    }

    @Test
    public void testOnRecognizeResultArtistNoSearchText() throws Exception {
        ArrayList<String> words = new ArrayList<>();

        words.add("Artist");
        when(mPreference.getBackgroundType()).thenReturn(0);

        when(mJudgeVoiceCase.execute(words, VoiceRecognitionSearchType.LOCAL)).thenReturn(new JudgeVoiceCommand.JudgeResult(VoiceCommand.ARTIST, null));
        when(mView.getScreenId()).thenReturn(ScreenId.ANDROID_MUSIC);
        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(0) instanceof CheckAvailableTextToSpeech.Callback) {
                ((CheckAvailableTextToSpeech.Callback) invocationOnMock.getArgument(0))
                        .onResult(CheckAvailableTextToSpeech.Result.AVAILABLE);
            }
            return null;
        }).when(mCheckTtsCase).execute(any(CheckAvailableTextToSpeech.Callback.class));

        mPresenter.mCurrentScreenId = ScreenId.ANDROID_MUSIC;
        mPresenter.takeView(mView);
        mPresenter.onRecognizeResults(words);

        verify(mReadText).startReading(mContext.getString(R.string.vr_search_no_search_word_artist));

        mPresenter.onSpeakDone();
        verify(mView).startRecognizer();

        words.set(0, "Test");

        mPresenter.onRecognizeResults(words);
        final ArgumentCaptor<Bundle> cap = ArgumentCaptor.forClass(Bundle.class);

        verify(mView).finishSpeechRecognizer();
        verify(mView).showSearchContainer(cap.capture());
        SearchContentParams params = SearchContentParams.from(cap.getValue());
        assertThat(params.voiceCommand, is(VoiceCommand.ARTIST));
        assertThat(params.searchWords, is(array(equalTo("Test"))));
    }

    @Test
    public void testOnRecognizeResultsArtist() throws Exception {
        ArrayList<String> words = new ArrayList<>();

        words.add("Artist Test");
        when(mPreference.getBackgroundType()).thenReturn(0);

        when(mJudgeVoiceCase.execute(words, VoiceRecognitionSearchType.LOCAL)).thenReturn(new JudgeVoiceCommand.JudgeResult(VoiceCommand.ARTIST, new String[]{"Test"}));
        when(mView.getScreenId()).thenReturn(ScreenId.ANDROID_MUSIC);

        mPresenter.mCurrentScreenId = ScreenId.ANDROID_MUSIC;
        mPresenter.takeView(mView);
        mPresenter.onRecognizeResults(words);
        final ArgumentCaptor<Bundle> cap = ArgumentCaptor.forClass(Bundle.class);

        verify(mView).finishSpeechRecognizer();
        verify(mView).showSearchContainer(cap.capture());
        SearchContentParams params = SearchContentParams.from(cap.getValue());
        assertThat(params.voiceCommand, is(VoiceCommand.ARTIST));
        assertThat(params.searchWords, is(array(equalTo("Test"))));
    }

    @Test
    public void testOnRecognizeResultAlbumNoSearchText() throws Exception {
        ArrayList<String> words = new ArrayList<>();

        words.add("Album");
        when(mPreference.getBackgroundType()).thenReturn(0);

        when(mJudgeVoiceCase.execute(words, VoiceRecognitionSearchType.LOCAL)).thenReturn(new JudgeVoiceCommand.JudgeResult(VoiceCommand.ALBUM, null));
        when(mView.getScreenId()).thenReturn(ScreenId.ANDROID_MUSIC);
        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(0) instanceof CheckAvailableTextToSpeech.Callback) {
                ((CheckAvailableTextToSpeech.Callback) invocationOnMock.getArgument(0))
                        .onResult(CheckAvailableTextToSpeech.Result.AVAILABLE);
            }
            return null;
        }).when(mCheckTtsCase).execute(any(CheckAvailableTextToSpeech.Callback.class));

        mPresenter.mCurrentScreenId = ScreenId.ANDROID_MUSIC;
        mPresenter.takeView(mView);
        mPresenter.onRecognizeResults(words);

        verify(mReadText).startReading(mContext.getString(R.string.vr_search_no_search_word_album));

        mPresenter.onSpeakDone();
        verify(mView).startRecognizer();

        words.set(0, "Test");
        mPresenter.onRecognizeResults(words);
        final ArgumentCaptor<Bundle> cap = ArgumentCaptor.forClass(Bundle.class);

        verify(mView).finishSpeechRecognizer();
        verify(mView).showSearchContainer(cap.capture());
        SearchContentParams params = SearchContentParams.from(cap.getValue());
        assertThat(params.voiceCommand, is(VoiceCommand.ALBUM));
        assertThat(params.searchWords, is(array(equalTo("Test"))));
    }

    @Test
    public void testOnRecognizeResultsAlbum() throws Exception {
        ArrayList<String> words = new ArrayList<>();

        words.add("Album Test");
        when(mPreference.getBackgroundType()).thenReturn(0);

        when(mJudgeVoiceCase.execute(words, VoiceRecognitionSearchType.LOCAL)).thenReturn(new JudgeVoiceCommand.JudgeResult(VoiceCommand.ALBUM, new String[]{"Test"}));
        when(mView.getScreenId()).thenReturn(ScreenId.ANDROID_MUSIC);

        mPresenter.mCurrentScreenId = ScreenId.ANDROID_MUSIC;
        mPresenter.takeView(mView);
        mPresenter.onRecognizeResults(words);
        final ArgumentCaptor<Bundle> cap = ArgumentCaptor.forClass(Bundle.class);

        verify(mView).finishSpeechRecognizer();
        verify(mView).showSearchContainer(cap.capture());
        SearchContentParams params = SearchContentParams.from(cap.getValue());
        assertThat(params.voiceCommand, is(VoiceCommand.ALBUM));
        assertThat(params.searchWords, is(array(equalTo("Test"))));
    }

    @Test
    public void testOnRecognizeResultSongNoSearchText() throws Exception {
        ArrayList<String> words = new ArrayList<>();

        words.add("Song");
        when(mPreference.getBackgroundType()).thenReturn(0);

        when(mJudgeVoiceCase.execute(words, VoiceRecognitionSearchType.LOCAL)).thenReturn(new JudgeVoiceCommand.JudgeResult(VoiceCommand.SONG, null));
        when(mView.getScreenId()).thenReturn(ScreenId.ANDROID_MUSIC);
        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(0) instanceof CheckAvailableTextToSpeech.Callback) {
                ((CheckAvailableTextToSpeech.Callback) invocationOnMock.getArgument(0))
                        .onResult(CheckAvailableTextToSpeech.Result.AVAILABLE);
            }
            return null;
        }).when(mCheckTtsCase).execute(any(CheckAvailableTextToSpeech.Callback.class));

        mPresenter.mCurrentScreenId = ScreenId.ANDROID_MUSIC;
        mPresenter.takeView(mView);
        mPresenter.onRecognizeResults(words);

        verify(mReadText).startReading(mContext.getString(R.string.vr_search_no_search_word_song));

        mPresenter.onSpeakDone();
        verify(mView).startRecognizer();

        words.set(0, "Test");
        mPresenter.onRecognizeResults(words);
        final ArgumentCaptor<Bundle> cap = ArgumentCaptor.forClass(Bundle.class);

        verify(mView).finishSpeechRecognizer();
        verify(mView).showSearchContainer(cap.capture());
        SearchContentParams params = SearchContentParams.from(cap.getValue());
        assertThat(params.voiceCommand, is(VoiceCommand.SONG));
        assertThat(params.searchWords, is(array(equalTo("Test"))));
    }

    @Test
    public void testOnRecognizeResultsSong() throws Exception {
        ArrayList<String> words = new ArrayList<>();

        words.add("Song Test");
        when(mPreference.getBackgroundType()).thenReturn(0);

        when(mJudgeVoiceCase.execute(words, VoiceRecognitionSearchType.LOCAL)).thenReturn(new JudgeVoiceCommand.JudgeResult(VoiceCommand.SONG, new String[]{"Test"}));
        when(mView.getScreenId()).thenReturn(ScreenId.ANDROID_MUSIC);

        mPresenter.mCurrentScreenId = ScreenId.ANDROID_MUSIC;
        mPresenter.takeView(mView);
        mPresenter.onRecognizeResults(words);
        final ArgumentCaptor<Bundle> cap = ArgumentCaptor.forClass(Bundle.class);

        verify(mView).finishSpeechRecognizer();
        verify(mView).showSearchContainer(cap.capture());
        SearchContentParams params = SearchContentParams.from(cap.getValue());
        assertThat(params.voiceCommand, is(VoiceCommand.SONG));
        assertThat(params.searchWords, is(array(equalTo("Test"))));
    }

    @Test
    public void testOnNavigateEvent() throws Exception {
        NavigateEvent event = new NavigateEvent(ScreenId.HOME_CONTAINER, Bundle.EMPTY);

        mPresenter.takeView(mView);
        mPresenter.setActive(true);
        mPresenter.onNavigateEvent(event);

        verify(mView).navigate(eq(ScreenId.HOME_CONTAINER), any(Bundle.class));
    }

    @Test
    public void testOnBackEvent() throws Exception {
        mPresenter.takeView(mView);
        mPresenter.setActive(true);
        mPresenter.onBackEvent(new GoBackEvent());

        verify(mView).goBack();
    }

    @Test
    public void testOnBackgroundChangeEventWithPicture() throws Exception {
        when(mPreference.getBackgroundType()).thenReturn(0);

        mPresenter.takeView(mView);
        mPresenter.setActive(true);
        mPresenter.onBackgroundChangeEvent(new BackgroundChangeEvent(true));

        verify(mView).changeBackgroundBlur(true);
    }

    @Test
    public void testOnBackgroundChangeEventWithVideo() throws Exception {
        when(mContext.getPackageName()).thenReturn("TEST");
        when(mPreference.getBackgroundType()).thenReturn(1);
        when(mPreference.getBackgroundVideoId()).thenReturn(1);

        mPresenter.takeView(mView);
        mPresenter.setActive(true);
        mPresenter.onBackgroundChangeEvent(new BackgroundChangeEvent(true));

        verify(mView).changeBackgroundBlur(eq(true));
    }

    @Test
    public void testOnCloseDialogActionWithPicture() throws Exception {
        when(mPreference.getBackgroundType()).thenReturn(0);

        mPresenter.takeView(mView);
        mPresenter.onCloseDialogAction();

        verify(mView).changeBackgroundBlur(false);
    }

    @Test
    public void testOnCloseDialogActionWithVideo() throws Exception {
        when(mPreference.getBackgroundType()).thenReturn(1);

        mPresenter.takeView(mView);
        mPresenter.onCloseDialogAction();

        verify(mView).changeBackgroundBlur(false);
    }

    @Test
    public void testOnPlayPositionChangeAction() throws Exception {
        mPresenter.takeView(mView);
        mPresenter.setActive(true);
        mPresenter.onPlayPositionChangeAction(new AppMusicPlayPositionChangeEvent());

        verify(mView).reloadBackground();
    }

    @Test
    public void testOnCrpSessionStartedEvent() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        when(mStatusCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        when(mPreference.isAgreedEulaPrivacyPolicy()).thenReturn(true);
        when(mView.isShowCaution()).thenReturn(false);
        mPresenter.takeView(mView);
        mPresenter.setActive(true);
        mPresenter.onCrpSessionStartedEvent(new CrpSessionStartedEvent());

        verify(mView).showCaution(Bundle.EMPTY);
    }

    @Test
    public void testOnCrpSessionStartedEvent_IsTermsOfUseUnRead() throws Exception {
        when(mPreference.isAgreedEulaPrivacyPolicy()).thenReturn(false);
        mPresenter.takeView(mView);
        mPresenter.onCrpSessionStartedEvent(new CrpSessionStartedEvent());

        verify(mView, never()).showCaution(any(Bundle.class));
    }

    @Test
    public void testOnCrpSessionStartedEvent_IsSessionStopped() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);

        when(mStatusCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getSessionStatus()).thenReturn(SessionStatus.STOPPED);
        when(mView.isShowCaution()).thenReturn(true);
        mPresenter.takeView(mView);
        mPresenter.onCrpSessionStartedEvent(new CrpSessionStartedEvent());

        verify(mView, never()).showCaution(any(Bundle.class));
    }

    @Test
    public void testOnCrpSessionStartedEvent_IsShowCaution() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);

        when(mStatusCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        when(mPreference.isAgreedEulaPrivacyPolicy()).thenReturn(true);
        when(mView.isShowCaution()).thenReturn(true);
        mPresenter.takeView(mView);
        mPresenter.onCrpSessionStartedEvent(new CrpSessionStartedEvent());

        verify(mView, never()).showCaution(any(Bundle.class));
    }

    @Test
    public void testOnCrpSessionStoppedEvent() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        StatusHolder holder = mock(StatusHolder.class);
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        status.adasWarningEvents = EnumSet.noneOf(AdasWarningEvent.class);
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(mStatusCase.execute()).thenReturn(holder);

        when(mView.isShowSessionStopped()).thenReturn(false);
        when(mView.getController()).thenReturn(mController);
        when(mController.getScreenIdInContainer()).thenReturn(ScreenId.EQ_SETTING);
        mPresenter.takeView(mView);
        mPresenter.setActive(true);
        mPresenter.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());

        verify(mView).showSessionStopped(Bundle.EMPTY);
        verify(mEventBus,atLeast(2)).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.UNCONNECTED_CONTAINER));
    }

    @Test
    public void testOnCrpSessionStoppedEvent_IsShowSessionStopped() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        StatusHolder holder = mock(StatusHolder.class);
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        status.adasWarningEvents = EnumSet.noneOf(AdasWarningEvent.class);
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(mStatusCase.execute()).thenReturn(holder);

        when(mView.isShowSessionStopped()).thenReturn(true);
        when(mView.getController()).thenReturn(mController);
        when(mController.getScreenIdInContainer()).thenReturn(ScreenId.EQ_SETTING);
        mPresenter.takeView(mView);
        mPresenter.setActive(true);
        mPresenter.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());

        verify(mView, never()).showSessionStopped(any(Bundle.class));
        verify(mEventBus,atLeast(2)).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.UNCONNECTED_CONTAINER));
    }

    @Test
    public void testOnCrpSessionStoppedEvent_IsNotSettingsScreen() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        status.adasWarningEvents = EnumSet.noneOf(AdasWarningEvent.class);
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(mStatusCase.execute()).thenReturn(holder);

        when(mView.isShowSessionStopped()).thenReturn(false);
        when(mView.getController()).thenReturn(mController);
        when(mController.getScreenIdInContainer()).thenReturn(ScreenId.ANDROID_MUSIC);
        mPresenter.takeView(mView);
        mPresenter.setActive(true);
        mPresenter.onCrpSessionStoppedEvent(new CrpSessionStoppedEvent());

        verify(mView).showSessionStopped(Bundle.EMPTY);
        verify(mView, never()).navigate(any(ScreenId.class), any(Bundle.class));
    }

    @Test
    public void testOnShowCautionEvent() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);

        when(mStatusCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        when(mView.isShowCaution()).thenReturn(false);
        mPresenter.takeView(mView);
        mPresenter.setActive(true);
        mPresenter.onShowCautionEvent(new ShowCautionEvent());

        verify(mView).showCaution(Bundle.EMPTY);
    }

    @Test
    public void testOnShowCautionEvent_IsSessionStopped() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);

        when(mStatusCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getSessionStatus()).thenReturn(SessionStatus.STOPPED);
        when(mView.isShowCaution()).thenReturn(true);
        mPresenter.takeView(mView);
        mPresenter.setActive(true);
        mPresenter.onShowCautionEvent(new ShowCautionEvent());

        verify(mView, never()).showCaution(any(Bundle.class));
    }

    @Test
    public void testOnShowCautionEvent_IsShowCaution() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);

        when(mStatusCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        when(mView.isShowCaution()).thenReturn(true);
        mPresenter.takeView(mView);
        mPresenter.setActive(true);
        mPresenter.onShowCautionEvent(new ShowCautionEvent());

        verify(mView, never()).showCaution(any(Bundle.class));
    }
}