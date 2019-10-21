package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.AudioOutputMode;
import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.CrossoverSetting;
import jp.pioneer.carsync.domain.model.CrossoverSetting.SpeakerCrossoverSetting;
import jp.pioneer.carsync.domain.model.CutoffSetting;
import jp.pioneer.carsync.domain.model.HpfLpfSetting;
import jp.pioneer.carsync.domain.model.ListeningPositionSetting;
import jp.pioneer.carsync.domain.model.MixedSpeakerType;
import jp.pioneer.carsync.domain.model.SlopeSetting;
import jp.pioneer.carsync.domain.model.SpeakerLevelSetting;
import jp.pioneer.carsync.domain.model.SpeakerType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SubwooferPhaseSetting;
import jp.pioneer.carsync.domain.model.SubwooferSetting;
import jp.pioneer.carsync.domain.model.TimeAlignmentSetting;
import jp.pioneer.carsync.domain.model.TimeAlignmentStepUnit;
import jp.pioneer.carsync.presentation.controller.FilterGraphViewController;
import jp.pioneer.carsync.presentation.presenter.AdvancedAudioSettingPresenter;
import jp.pioneer.carsync.presentation.util.AppConfigUtil;
import jp.pioneer.carsync.presentation.util.FilterDesignDefaults;
import jp.pioneer.carsync.presentation.util.FilterGraphGeometry;
import jp.pioneer.carsync.presentation.util.FilterGraphSpec;
import jp.pioneer.carsync.presentation.util.FilterPathBuilder;
import jp.pioneer.carsync.presentation.util.NumberFormatUtil;
import jp.pioneer.carsync.presentation.view.AdvancedAudioSettingView;
import jp.pioneer.carsync.presentation.view.adapter.GridSpeakerSettingMenuAdapter;
import jp.pioneer.carsync.presentation.view.adapter.ListeningPositionAdapter;
import jp.pioneer.carsync.presentation.view.adapter.SpeakerSettingMenuAdapter;
import jp.pioneer.carsync.presentation.view.adapter.SpeakerSettingMenuAdapter.SpeakerSettingMenuItem;
import jp.pioneer.carsync.presentation.view.adapter.SpeakerSettingMenuAdapter.SpeakerSettingMenuType;
import jp.pioneer.carsync.presentation.view.fragment.OnGoBackListener;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.widget.FilterFrequencyView;
import jp.pioneer.carsync.presentation.view.widget.FilterGraphView;
import jp.pioneer.carsync.presentation.view.widget.ListeningPositionSettingMenuView;
import jp.pioneer.carsync.presentation.view.widget.SeatView;
import jp.pioneer.carsync.presentation.view.widget.SpeakerInfoView;
import jp.pioneer.carsync.presentation.view.widget.SpeakerSettingMenuItemView;
import jp.pioneer.carsync.presentation.view.widget.SpeakerSettingMenuView;
import jp.pioneer.carsync.presentation.view.widget.TimeAlignmentButton;
import timber.log.Timber;

/**
 * オーディオ詳細設定画面
 */

public class AdvancedAudioSettingFragment extends AbstractScreenFragment<AdvancedAudioSettingPresenter, AdvancedAudioSettingView> implements AdvancedAudioSettingView, OnGoBackListener {
    @Inject AdvancedAudioSettingPresenter mPresenter;
    private Unbinder mUnbinder;
    private StatusHolder mStatusHolder;
    private static final boolean CORE_AUDIO_ADV_SPK_MENU_NOTIFY = false;
    private static final String LEVEL_FORMAT = "%+ddB";
    private static final String DISTANCE_FORMAT = "%.1f%s";

    /**
     * コンストラクタ
     */
    public AdvancedAudioSettingFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return EulaFragment
     */
    public static AdvancedAudioSettingFragment newInstance(Bundle args) {
        AdvancedAudioSettingFragment fragment = new AdvancedAudioSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @BindView(R.id.flSpeakerInfo)
    SpeakerInfoView mFlSpeakerInfo;

    @BindView(R.id.frSpeakerInfo)
    SpeakerInfoView mFrSpeakerInfo;

    @BindView(R.id.rlSpeakerInfo)
    SpeakerInfoView mRlSpeakerInfo;

    @BindView(R.id.rrSpeakerInfo)
    SpeakerInfoView mRrSpeakerInfo;

    @BindView(R.id.swSpeakerInfo)
    SpeakerInfoView mSwSpeakerInfo;

    @BindView(R.id.listeningPositionText)
    TextView mListeningPositionText;

    @BindView(R.id.stdSeatIconContainer)
    SeatView mStdSeatView;

    @BindView(R.id.nwSeatIconContainer)
    SeatView mNwSeatView;

    @BindView(R.id.speakerSettingMenu)
    SpeakerSettingMenuView mSpeakerSettingMenu;

    @BindView(R.id.listeningPositionSettingMenu)
    ListeningPositionSettingMenuView mListeningPositionSettingMenu;

    @BindView(R.id.taButton)
    TimeAlignmentButton mTaButton;

    //    private AppearanceSpec mAppearance;
    private AudioOutputMode mMode;
    private ListeningPositionAdapter mListeningPositionAdapter;
    private GridSpeakerSettingMenuAdapter mFlAdapter;
    private GridSpeakerSettingMenuAdapter mFrAdapter;
    private GridSpeakerSettingMenuAdapter mRlAdapter;
    private GridSpeakerSettingMenuAdapter mRrAdapter;
    private GridSpeakerSettingMenuAdapter mSwAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        ColorStateList backgroundTint = ContextCompat.getColorStateList(context, R.color.button_tint);
        ColorStateList textColor = ContextCompat.getColorStateList(context, R.color.setting_menu_item_value_text);

        mListeningPositionAdapter = new ListeningPositionAdapter(context);
        mFlAdapter = new GridSpeakerSettingMenuAdapter(context, getString(R.string.speaker_front_left), backgroundTint, textColor);
        mFrAdapter = new GridSpeakerSettingMenuAdapter(context, getString(R.string.speaker_front_right), backgroundTint, textColor);
        mRlAdapter = new GridSpeakerSettingMenuAdapter(context, getString(R.string.speaker_rear_left), backgroundTint, textColor);
        mRrAdapter = new GridSpeakerSettingMenuAdapter(context, getString(R.string.speaker_rear_right), backgroundTint, textColor);
        mSwAdapter = new GridSpeakerSettingMenuAdapter(context, getString(R.string.speaker_sub_woofer), backgroundTint, textColor);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_advanced_setting, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        view.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK &&
                    (mSpeakerSettingMenu.getVisibility() == View.VISIBLE || mListeningPositionSettingMenu.getVisibility() == View.VISIBLE)) {
                if(event.getAction() == KeyEvent.ACTION_UP) {
                    if (mSpeakerSettingMenu.getVisibility() == View.VISIBLE) {
                        mSpeakerSettingMenu.slideDown();
                        setFilterGraphCurrentSpeaker(null);
                    } else if (mListeningPositionSettingMenu.getVisibility() == View.VISIBLE) {
                        mListeningPositionSettingMenu.slideDown();
                    }
                }
                return true;
            }
            return false;
        });
        view.setFocusableInTouchMode(true);
        view.requestFocus();

        mListeningPositionSettingMenu.setOnItemClickListener(mOnListeningPositionItemClickListener);
        mListeningPositionSettingMenu.setAdapter(mListeningPositionAdapter);
        setupFilterGraph();

        mSpeakerSettingMenu.setOnMenuDownListener(new SpeakerSettingMenuView.OnMenuDownListener(){
            @Override
            public void onMenuDown() {
                setFilterGraphCurrentSpeaker(null);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        applyStatus();
        applyAppearance();
        redrawFilterGraph(false);
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mListeningPositionSettingMenu.setOnItemClickListener(null);
        mListeningPositionSettingMenu.setAdapter(null);
        mUnbinder.unbind();
        super.onDestroyView();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected AdvancedAudioSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.ADVANCED_AUDIO_SETTING;
    }

    /**
     * ACTION_CANCEL 防止(無止 onLayoutから守る)策
     * SpeakerSettingMenu が表示されると、ずっと高頻度画面更新が行われる。
     * ACTION_CANCEL 発生の要因は　VerticalViewPager.onLayout:child.layout
     * に SpeakerSettingMenuView.onLayout が繰り返し呼ばれるにある
     */
    private boolean busy = false;

    public boolean isBusy() {            // AdvancedSettingIf
        return busy;
    }

    public void setBusy(boolean busy) {    // AdvancedSettingIf
        boolean old = this.busy;
        if (old != busy) {
            if (busy) {
                Timber.d("AutoRepeatButton change busy to true on AdvancedSettingsFragment");
            } else {
                Timber.d("AutoRepeatButton change busy to false on AdvancedSettingsFragment");
            }
        }

        this.busy = busy;

        if (mSpeakerSettingMenu != null) mSpeakerSettingMenu.setBusy(busy);

        if (mFlAdapter != null) mFlAdapter.setBusy(busy);
        if (mFrAdapter != null) mFrAdapter.setBusy(busy);
        if (mRlAdapter != null) mRlAdapter.setBusy(busy);
        if (mRrAdapter != null) mRrAdapter.setBusy(busy);
        if (mSwAdapter != null) mSwAdapter.setBusy(busy);
    }


    /**
     * ★長押しの間専用（※）★ Speaker Menu 画面の表示を更新する Created by nakano on 2016/06/29
     * 　adapter.notifyDataSetChanged() は効率的良くないし、busyの時動作しない、それを改修するため、当関数を作った
     * 　※Adapter 変わらない前提。Speaker Setting Menu消えない限り、変わるありえないが、旧方式はあまりにも影響反映が多いので、
     * 　　　長押しの間以外の変更は、予想外の影響よりデグレの恐れがあります
     *
     * @param adapter
     * @param position
     * @param mainType
     * @return
     */
    private View writeItemToConvertView(SpeakerSettingMenuAdapter adapter, int position, SpeakerSettingMenuAdapter.SpeakerSettingMenuType mainType) {
        SpeakerSettingMenuAdapter currentAdapter = (SpeakerSettingMenuAdapter) mSpeakerSettingMenu.mListView.getAdapter();
        if (currentAdapter != adapter) {
            // if (BuildConfig.ENABLE_TIMBER) Timber.d("AutoRepeatButton writeItemToConvertView do nothing because not same adapter");
            return null;
        }

        if (!adapter.isShowing()) {
            // if (BuildConfig.ENABLE_TIMBER) Timber.d("AutoRepeatButton writeItemToConvertView do nothing because not showing adapter");
            return null;
        }

        View view = adapter.findViewByType(mSpeakerSettingMenu.mListView, mainType);
        if (view == null) {    // 画面の見える項目を明示的更新する目的なので、null時は処理不要
            Timber.d("AutoRepeatButton writeItemToConvertView do nothing because view is null");
        } else {
            adapter.getView(position, view, mSpeakerSettingMenu.mListView);    // Speaker Menu 画面の表示を更新する
        }
        return view;

    }

    @Override
    public void applyStatus() {
        checkMenuIsAvailable();

        if (mFlSpeakerInfo == null) return; // Viewが設定されてないので
        mStatusHolder = mPresenter.getStatusHolder();
        CarDeviceSpec spec = mStatusHolder.getCarDeviceSpec();
        CarDeviceStatus status = mStatusHolder.getCarDeviceStatus();
        AudioOutputMode mode = spec.audioSettingSpec.audioOutputMode;
        AudioSetting audioSetting = mStatusHolder.getAudioSetting();
        if (mode != mMode) {    // そもそもAudioOutputModeが変わるのはありえないんだけどやはりapplyしたい、が、re-layout は重い、なのでチェックするよう
            mMode = mode;
            applyAudioOutputMode(mode);
        }

        applyEnabled(status, mode);
        applySubwooferSetting(audioSetting.subwooferSetting);
        applySubwooferPhaseSetting(audioSetting.subwooferPhaseSetting);
        applySpeakerLevelSetting(audioSetting.speakerLevelSetting);
        applyTimeAlignment(audioSetting.timeAlignmentSetting, audioSetting.listeningPositionSetting);
        applyPassFilterFrequency(audioSetting.crossoverSetting, mode);
        applyListeningPosition(audioSetting.listeningPositionSetting);

        updateFilterGraph(mode, null, AudioOutputMode.STANDARD, SpeakerType.FRONT, null);
    }

    private void applyEnabled(CarDeviceStatus status, AudioOutputMode mode) {
        mStatusHolder = mPresenter.getStatusHolder();
        StatusHolder statusHolder = mStatusHolder;
        AudioSettingStatus audioSettingStatus = mStatusHolder.getAudioSettingStatus();
        AudioSetting audioSetting = mStatusHolder.getAudioSetting();
        boolean audioEnabled = statusHolder.isAudioSettingEnabled();
        boolean enabled;
        SpeakerSettingMenuItem item;

        // Subwoofer設定
        enabled = audioEnabled && audioSettingStatus.subwooferSettingEnabled;
        item = mSwAdapter.findItem(SpeakerSettingMenuType.SUBWOOFER);
        item.enabled = enabled;
        item.increaseEnabled = true;
        item.decreaseEnabled = true;
        if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
            writeItemToConvertView(mSwAdapter, mSwAdapter.getPosition(item), item.type);
        }

        boolean subwooferOn = enabled
                && audioSetting.subwooferSetting == SubwooferSetting.ON;

        // Subwoofer Phase設定
        enabled = audioEnabled
                && audioSettingStatus.subwooferPhaseSettingEnabled
                && subwooferOn;

        item = mSwAdapter.findItem(SpeakerSettingMenuType.SUBWOOFER_PHASE);
        if (item != null) {
            item.enabled = enabled;
            item.increaseEnabled = true;
            item.decreaseEnabled = true;
        } else {
            item = mSwAdapter.findItem2(SpeakerSettingMenuType.SUBWOOFER_PHASE);
            item.enabled2 = enabled;
            item.increaseEnabled2 = enabled;
            item.decreaseEnabled2 = enabled;
        }
        if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
            writeItemToConvertView(mSwAdapter, mSwAdapter.getPosition(item), item.type);
        } else {
            mSwAdapter.notifyDataSetChanged();
        }

        // Speaker Level設定
        enabled = audioEnabled && audioSettingStatus.speakerLevelSettingEnabled;
        SpeakerLevelSetting speakerLevelSetting = audioSetting.speakerLevelSetting;
        for (SpeakerSettingMenuAdapter adapter : adapters()) {
            if (adapter == null) {
                continue;
            }
            item = adapter.findItem(SpeakerSettingMenuType.SPEAKER_LEVEL);
            if (adapter == mSwAdapter) {
                // Subwooferの場合だけSubwooferがOFFの場合は無効化
                enabled &= subwooferOn;
            }
            item.enabled = enabled;

            int currentSpeakerLevel;
            if (adapter == mFlAdapter) {
                currentSpeakerLevel = speakerLevelSetting.frontLeftHighLeftLevel;
            } else if (adapter == mFrAdapter) {
                currentSpeakerLevel = speakerLevelSetting.frontRightHighRightLevel;
            } else if (adapter == mRlAdapter) {
                currentSpeakerLevel = speakerLevelSetting.rearLeftMidLeftLevel;
            } else if (adapter == mRrAdapter) {
                currentSpeakerLevel = speakerLevelSetting.rearRightMidRightLevel;
            } else if (adapter == mSwAdapter) {
                currentSpeakerLevel = speakerLevelSetting.subwooferLevel;
            } else {
                continue;
            }

            // min/maxの値と比べて増減できるか判定
            item.increaseEnabled = currentSpeakerLevel < speakerLevelSetting.maximumLevel;
            item.decreaseEnabled = currentSpeakerLevel > speakerLevelSetting.minimumLevel;
            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
            } else {
                adapter.notifyDataSetChanged();
            }
        }

        // Crossover設定(HPF/LPF, Cutoff周波数, Slope)
        enabled = audioEnabled && audioSettingStatus.crossoverSettingEnabled;
        if (mode == AudioOutputMode.STANDARD) {
            applyEnabledToAdapters(enabled, audioSetting.crossoverSetting.front, mFlAdapter, mFrAdapter);
            applyEnabledToAdapters(enabled, audioSetting.crossoverSetting.rear, mRlAdapter, mRrAdapter);
            // Subwooferの場合だけSubwooferがOFFの場合は無効化
            applyEnabledToAdapters(enabled && subwooferOn, audioSetting.crossoverSetting.subwooferStandardMode, mSwAdapter);
        } else if (mode == AudioOutputMode.TWO_WAY_NETWORK) {
            applyEnabledToAdapters(enabled, audioSetting.crossoverSetting.high, mFlAdapter, mFrAdapter);
            applyEnabledToAdapters(enabled, audioSetting.crossoverSetting.midHPF, mRlAdapter, mRrAdapter);
            applyEnabledToAdapters(enabled, audioSetting.crossoverSetting.midLPF, mRlAdapter, mRrAdapter);
            // Subwooferの場合だけSubwooferがOFFの場合は無効化
            applyEnabledToAdapters(enabled && subwooferOn, audioSetting.crossoverSetting.subwoofer2WayNetworkMode, mSwAdapter);
        }

        // Time Alignment設定
        enabled = audioEnabled && audioSettingStatus.timeAlignmentSettingEnabled;
        TimeAlignmentSetting timeAlignmentSetting = audioSetting.timeAlignmentSetting;
        for (SpeakerSettingMenuAdapter adapter : adapters()) {
            if (adapter == null) {
                continue;
            }
            item = adapter.findItem(SpeakerSettingMenuType.TIME_ALIGNMENT);
            if (adapter == mSwAdapter) {
                // Subwooferの場合だけSubwooferがOFFの場合は無効化
                enabled &= subwooferOn;
            }
            item.enabled = enabled;

            int currentStep;
            if (adapter == mFlAdapter) {
                currentStep = timeAlignmentSetting.frontLeftHighLeftStep;
            } else if (adapter == mFrAdapter) {
                currentStep = timeAlignmentSetting.frontRightHighRightStep;
            } else if (adapter == mRlAdapter) {
                currentStep = timeAlignmentSetting.rearLeftMidLeftStep;
            } else if (adapter == mRrAdapter) {
                currentStep = timeAlignmentSetting.rearRightMidRightStep;
            } else if (adapter == mSwAdapter) {
                currentStep = timeAlignmentSetting.subwooferStep;
            } else {
                continue;
            }

            item.increaseEnabled = currentStep < timeAlignmentSetting.maximumStep;
            item.decreaseEnabled = currentStep > timeAlignmentSetting.minimumStep;
            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
            } else {
                adapter.notifyDataSetChanged();
            }
        }

        enabled = statusHolder.isTimeAlignmentSettingEnabled();

        mTaButton.setEnabled(enabled);

        // Listening Position設定
        enabled = audioEnabled && audioSettingStatus.listeningPositionSettingEnabled;
        mStdSeatView.setEnabled(enabled);
        mNwSeatView.setEnabled(enabled);
    }

    /**
     * Crossover設定をAdapterに反映させます
     *
     * @param enabled  crossover設定が有効
     * @param setting  Crossover設定
     * @param adapters 反映対象のadapter
     */
    private void applyEnabledToAdapters(boolean enabled, SpeakerCrossoverSetting setting, GridSpeakerSettingMenuAdapter... adapters) {
        boolean on = setting.hpfLpfSetting == HpfLpfSetting.ON
                || setting.hpfLpfSetting == HpfLpfSetting.ON_FIXED;
        SpeakerSettingMenuItem item;
        boolean hpf = setting.speakerType == SpeakerType.FRONT
                || setting.speakerType == SpeakerType.REAR
                || setting.speakerType == SpeakerType.HIGH
                || setting.speakerType == SpeakerType.MID_HPF;
        CutoffSetting cutoffSetting = setting.cutoffSetting;
        SlopeSetting slopeSetting = setting.slopeSetting;
        for (GridSpeakerSettingMenuAdapter adapter : adapters) {
            if (adapter == null) {
                continue;
            }
            // HPF or LPFのスイッチ
            item = adapter.findItem(hpf ? SpeakerSettingMenuType.HPF : SpeakerSettingMenuType.LPF);
            item.enabled = enabled;
            item.increaseEnabled = !(setting.speakerType == SpeakerType.HIGH && mMode == AudioOutputMode.TWO_WAY_NETWORK);
            item.decreaseEnabled = !(setting.speakerType == SpeakerType.HIGH && mMode == AudioOutputMode.TWO_WAY_NETWORK);
            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
            }

            // カットオフ周波数
            item = adapter.findItem(hpf ? SpeakerSettingMenuType.HPF_CUTOFF : SpeakerSettingMenuType.LPF_CUTOFF);
            item.enabled = enabled && on;
            item.increaseEnabled = cutoffSetting != null && cutoffSetting.toggle(1) != null;
            item.decreaseEnabled = cutoffSetting != null && cutoffSetting.toggle(-1) != null;
            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
            }

            // スロープ
            item = adapter.findItem(hpf ? SpeakerSettingMenuType.HPF_SLOPE : SpeakerSettingMenuType.LPF_SLOPE);
            if (item != null) {
                item.enabled = enabled && on;
                item.increaseEnabled = slopeSetting != null && slopeSetting.toggle(1) != null;
                item.decreaseEnabled = slopeSetting != null && slopeSetting.toggle(-1) != null;
            } else {
                item = adapter.findItem2(hpf ? SpeakerSettingMenuType.HPF_SLOPE : SpeakerSettingMenuType.LPF_SLOPE);
                item.enabled2 = enabled && on;
                item.increaseEnabled2 = slopeSetting != null && slopeSetting.toggle(1) != null;
                item.decreaseEnabled2 = slopeSetting != null && slopeSetting.toggle(-1) != null;
            }
            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void applyAudioOutputMode(AudioOutputMode mode) {
        // 座席のレイアウト切り替え
        mStdSeatView.setVisibility(mode == AudioOutputMode.STANDARD ? View.VISIBLE : View.GONE);
        mNwSeatView.setVisibility(mode == AudioOutputMode.TWO_WAY_NETWORK ? View.VISIBLE : View.GONE);

        if (mode == AudioOutputMode.STANDARD) {
            mFlSpeakerInfo.setSpeakerType(SpeakerInfoView.SPEAKER_TYPE_FL);
            mFrSpeakerInfo.setSpeakerType(SpeakerInfoView.SPEAKER_TYPE_FR);
            mRlSpeakerInfo.setSpeakerType(SpeakerInfoView.SPEAKER_TYPE_RL);
            mRrSpeakerInfo.setSpeakerType(SpeakerInfoView.SPEAKER_TYPE_RR);
            mSwSpeakerInfo.setSpeakerType(SpeakerInfoView.SPEAKER_TYPE_SW);
        } else if (mode == AudioOutputMode.TWO_WAY_NETWORK) {
            mFlSpeakerInfo.setSpeakerType(SpeakerInfoView.SPEAKER_TYPE_HL);
            mFrSpeakerInfo.setSpeakerType(SpeakerInfoView.SPEAKER_TYPE_HR);
            mRlSpeakerInfo.setSpeakerType(SpeakerInfoView.SPEAKER_TYPE_ML);
            mRrSpeakerInfo.setSpeakerType(SpeakerInfoView.SPEAKER_TYPE_MR);
            mSwSpeakerInfo.setSpeakerType(SpeakerInfoView.SPEAKER_TYPE_SW);
        }

        // ListeningPositionの選択肢を調整
        mListeningPositionAdapter.clear();
        for (ListeningPositionSetting tmp : ListeningPositionSetting.values()) {
            //OFFを選択肢に表示
            //if (tmp == ListeningPositionSetting.OFF) continue;
            if (!tmp.isSupported(mode)) continue;

            mListeningPositionAdapter.add(tmp);
        }

        setupFrontSpeakerSettingAdapter(mode, 0);
        setupFrontSpeakerSettingAdapter(mode, 1);
        setupRearSpeakerSettingAdapter(mode, 0);
        setupRearSpeakerSettingAdapter(mode, 1);
        setupSwSpeakerSettingAdapter(mode);
    }

    /**
     * 前席のスピーカー設定画面のAdapterを設定
     *
     * @param mode
     * @param side 0:LEFT 1:RIGHT
     */
    private void setupFrontSpeakerSettingAdapter(final AudioOutputMode mode, final int side) {
        int orientation = AppConfigUtil.getCurrentOrientation(getContext());
        boolean isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE;

        SpeakerSettingMenuAdapter adapter = (side == 0) ? mFlAdapter : mFrAdapter;
        SpeakerSettingMenuItem item;
        final MixedSpeakerType mixedSpeakerType = (side == 0) ? MixedSpeakerType.FRONT_LEFT_HIGH_LEFT : MixedSpeakerType.FRONT_RIGHT_HIGH_RIGHT;
        final SpeakerType speakerType = (mode == AudioOutputMode.STANDARD)
                ? SpeakerType.FRONT : SpeakerType.HIGH;

        // Speaker Level
        item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.SPEAKER_LEVEL);
/*        item.listener = new OnEchoSpeakerLevelChangingListenerImpl(this, adapter, item,
                (side == 0) ? BaseSpeakerLevelUpDownEvent.TYPE_FRONT_LEFT_HIGH_LEFT
                        : BaseSpeakerLevelUpDownEvent.TYPE_FRONT_RIGHT_HIGH_RIGHT
        );*/
        item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
            @Override
            public void onIncreaseClicked() {
            }

            @Override
            public void onDecreaseClicked() {
            }

            @Override
            public void onAction(int typeId) {
            }

            @Override
            public void onIncreaseLocalClicked() {
                getPresenter().setSpeakerLevel(mixedSpeakerType, true);
            }

            @Override
            public void onDecreaseLocalClicked() {
                getPresenter().setSpeakerLevel(mixedSpeakerType, false);
            }

            @Override
            public void onIncreaseSubmitClicked() {
                getPresenter().setSpeakerLevel(mixedSpeakerType, true);
            }

            @Override
            public void onDecreaseSubmitClicked() {
                getPresenter().setSpeakerLevel(mixedSpeakerType, false);
            }
        };
        adapter.add(item);

        // HPF on/off
        item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.HPF);
        item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
            @Override
            public void onIncreaseClicked() {

            }

            @Override
            public void onDecreaseClicked() {

            }

            @Override
            public void onAction(int typeId) {

            }

            @Override
            public void onIncreaseLocalClicked() {
                getPresenter().onToggleCrossoverHpfLpf(speakerType);
            }

            @Override
            public void onDecreaseLocalClicked() {
                getPresenter().onToggleCrossoverHpfLpf(speakerType);
            }

            @Override
            public void onIncreaseSubmitClicked() {
                getPresenter().onToggleCrossoverHpfLpf(speakerType);
            }

            @Override
            public void onDecreaseSubmitClicked() {
                getPresenter().onToggleCrossoverHpfLpf(speakerType);
            }
        };
        adapter.add(item);

        // HPF Cutoff
        item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.HPF_CUTOFF);
        if (isLandscape) {
            item.type2 = SpeakerSettingMenuType.HPF_SLOPE;
            item.listener2 = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
                @Override
                public void onIncreaseClicked() {

                }

                @Override
                public void onDecreaseClicked() {

                }

                @Override
                public void onAction(int typeId) {

                }

                @Override
                public void onIncreaseLocalClicked() {
                    getPresenter().setCrossoverSlope(speakerType, true);
                }

                @Override
                public void onDecreaseLocalClicked() {
                    getPresenter().setCrossoverSlope(speakerType, false);
                }

                @Override
                public void onIncreaseSubmitClicked() {
                    getPresenter().setCrossoverSlope(speakerType, true);
                }

                @Override
                public void onDecreaseSubmitClicked() {
                    getPresenter().setCrossoverSlope(speakerType, false);
                }
            };
        }
        item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
            @Override
            public void onIncreaseClicked() {

            }

            @Override
            public void onDecreaseClicked() {

            }

            @Override
            public void onAction(int typeId) {

            }

            @Override
            public void onIncreaseLocalClicked() {
                getPresenter().setCrossoverCutOff(speakerType, true);
            }

            @Override
            public void onDecreaseLocalClicked() {
                getPresenter().setCrossoverCutOff(speakerType, false);
            }

            @Override
            public void onIncreaseSubmitClicked() {
                getPresenter().setCrossoverCutOff(speakerType, true);
            }

            @Override
            public void onDecreaseSubmitClicked() {
                getPresenter().setCrossoverCutOff(speakerType, false);
            }
        };

        adapter.add(item);

        if (!isLandscape) {
            // HFP Slope
            item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.HPF_SLOPE);
            item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
                @Override
                public void onIncreaseClicked() {

                }

                @Override
                public void onDecreaseClicked() {

                }

                @Override
                public void onAction(int typeId) {

                }

                @Override
                public void onIncreaseLocalClicked() {
                    getPresenter().setCrossoverSlope(speakerType, true);
                }

                @Override
                public void onDecreaseLocalClicked() {
                    getPresenter().setCrossoverSlope(speakerType, false);
                }

                @Override
                public void onIncreaseSubmitClicked() {
                    getPresenter().setCrossoverSlope(speakerType, true);
                }

                @Override
                public void onDecreaseSubmitClicked() {
                    getPresenter().setCrossoverSlope(speakerType, false);
                }
            };
            adapter.add(item);
        }

        // Time Alignment
        item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.TIME_ALIGNMENT);
        item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
            @Override
            public void onIncreaseClicked() {

            }

            @Override
            public void onDecreaseClicked() {

            }

            @Override
            public void onAction(int typeId) {

            }

            @Override
            public void onIncreaseLocalClicked() {
                getPresenter().setTimeAlignment(mixedSpeakerType, true);
            }

            @Override
            public void onDecreaseLocalClicked() {
                getPresenter().setTimeAlignment(mixedSpeakerType, false);
            }

            @Override
            public void onIncreaseSubmitClicked() {
                getPresenter().setTimeAlignment(mixedSpeakerType, true);
            }

            @Override
            public void onDecreaseSubmitClicked() {
                getPresenter().setTimeAlignment(mixedSpeakerType, false);
            }
        };
        adapter.add(item);
    }

    /**
     * 後席のスピーカー設定画面のAdapterを設定
     *
     * @param mode
     * @param side 0:LEFT 1:RIGHT
     */
    private void setupRearSpeakerSettingAdapter(AudioOutputMode mode, int side) {
        int orientation = AppConfigUtil.getCurrentOrientation(getContext());
        boolean isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE;

        SpeakerSettingMenuAdapter adapter = (side == 0) ? mRlAdapter : mRrAdapter;
        SpeakerSettingMenuItem item;
        final MixedSpeakerType mixedSpeakerType = (side == 0) ? MixedSpeakerType.REAR_LEFT_MID_LEFT : MixedSpeakerType.REAR_RIGHT_MID_RIGHT;
        // Speaker Level
        item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.SPEAKER_LEVEL);
        item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
            @Override
            public void onIncreaseClicked() {
            }

            @Override
            public void onDecreaseClicked() {
            }

            @Override
            public void onAction(int typeId) {
            }

            @Override
            public void onIncreaseLocalClicked() {
                getPresenter().setSpeakerLevel(mixedSpeakerType, true);
            }

            @Override
            public void onDecreaseLocalClicked() {
                getPresenter().setSpeakerLevel(mixedSpeakerType, false);
            }

            @Override
            public void onIncreaseSubmitClicked() {
                getPresenter().setSpeakerLevel(mixedSpeakerType, true);
            }

            @Override
            public void onDecreaseSubmitClicked() {
                getPresenter().setSpeakerLevel(mixedSpeakerType, false);
            }
        };
        adapter.add(item);

        final SpeakerType speakerType = (mode == AudioOutputMode.STANDARD)
                ? SpeakerType.REAR : SpeakerType.MID_HPF;
        // HPF on/off
        item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.HPF);
        item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
            @Override
            public void onIncreaseClicked() {

            }

            @Override
            public void onDecreaseClicked() {

            }

            @Override
            public void onAction(int typeId) {

            }

            @Override
            public void onIncreaseLocalClicked() {
                getPresenter().onToggleCrossoverHpfLpf(speakerType);
            }

            @Override
            public void onDecreaseLocalClicked() {
                getPresenter().onToggleCrossoverHpfLpf(speakerType);
            }

            @Override
            public void onIncreaseSubmitClicked() {
                getPresenter().onToggleCrossoverHpfLpf(speakerType);
            }

            @Override
            public void onDecreaseSubmitClicked() {
                getPresenter().onToggleCrossoverHpfLpf(speakerType);
            }
        };
        adapter.add(item);

        // HPF Cutoff
        item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.HPF_CUTOFF);
        if (isLandscape) {
            item.type2 = SpeakerSettingMenuType.HPF_SLOPE;
            item.listener2 = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
                @Override
                public void onIncreaseClicked() {

                }

                @Override
                public void onDecreaseClicked() {

                }

                @Override
                public void onAction(int typeId) {

                }

                @Override
                public void onIncreaseLocalClicked() {
                    getPresenter().setCrossoverSlope(speakerType, true);
                }

                @Override
                public void onDecreaseLocalClicked() {
                    getPresenter().setCrossoverSlope(speakerType, false);
                }

                @Override
                public void onIncreaseSubmitClicked() {
                    getPresenter().setCrossoverSlope(speakerType, true);
                }

                @Override
                public void onDecreaseSubmitClicked() {
                    getPresenter().setCrossoverSlope(speakerType, false);
                }
            };
        }
        item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
            @Override
            public void onIncreaseClicked() {

            }

            @Override
            public void onDecreaseClicked() {

            }

            @Override
            public void onAction(int typeId) {

            }

            @Override
            public void onIncreaseLocalClicked() {
                getPresenter().setCrossoverCutOff(speakerType, true);
            }

            @Override
            public void onDecreaseLocalClicked() {
                getPresenter().setCrossoverCutOff(speakerType, false);
            }

            @Override
            public void onIncreaseSubmitClicked() {
                getPresenter().setCrossoverCutOff(speakerType, true);
            }

            @Override
            public void onDecreaseSubmitClicked() {
                getPresenter().setCrossoverCutOff(speakerType, false);
            }
        };
        adapter.add(item);

        if (!isLandscape) {
            // HFP Slope
            item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.HPF_SLOPE);
            item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
                @Override
                public void onIncreaseClicked() {

                }

                @Override
                public void onDecreaseClicked() {

                }

                @Override
                public void onAction(int typeId) {

                }

                @Override
                public void onIncreaseLocalClicked() {
                    getPresenter().setCrossoverSlope(speakerType, true);
                }

                @Override
                public void onDecreaseLocalClicked() {
                    getPresenter().setCrossoverSlope(speakerType, false);
                }

                @Override
                public void onIncreaseSubmitClicked() {
                    getPresenter().setCrossoverSlope(speakerType, true);
                }

                @Override
                public void onDecreaseSubmitClicked() {
                    getPresenter().setCrossoverSlope(speakerType, false);
                }
            };
            adapter.add(item);
        }

        if (mode == AudioOutputMode.TWO_WAY_NETWORK) {
            // LPF on/off
            final SpeakerType speakerTypeLpf = SpeakerType.MID_LPF;
            item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.LPF);
            item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
                @Override
                public void onIncreaseClicked() {

                }

                @Override
                public void onDecreaseClicked() {

                }

                @Override
                public void onAction(int typeId) {

                }

                @Override
                public void onIncreaseLocalClicked() {
                    getPresenter().onToggleCrossoverHpfLpf(speakerTypeLpf);
                }

                @Override
                public void onDecreaseLocalClicked() {
                    getPresenter().onToggleCrossoverHpfLpf(speakerTypeLpf);
                }

                @Override
                public void onIncreaseSubmitClicked() {
                    getPresenter().onToggleCrossoverHpfLpf(speakerTypeLpf);
                }

                @Override
                public void onDecreaseSubmitClicked() {
                    getPresenter().onToggleCrossoverHpfLpf(speakerTypeLpf);
                }
            };
            adapter.add(item);

            // LPF Cutoff
            item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.LPF_CUTOFF);
            if (isLandscape) {
                item.type2 = SpeakerSettingMenuType.LPF_SLOPE;
                item.listener2 = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
                    @Override
                    public void onIncreaseClicked() {

                    }

                    @Override
                    public void onDecreaseClicked() {

                    }

                    @Override
                    public void onAction(int typeId) {

                    }

                    @Override
                    public void onIncreaseLocalClicked() {
                        getPresenter().setCrossoverSlope(speakerTypeLpf, true);
                    }

                    @Override
                    public void onDecreaseLocalClicked() {
                        getPresenter().setCrossoverSlope(speakerTypeLpf, false);
                    }

                    @Override
                    public void onIncreaseSubmitClicked() {
                        getPresenter().setCrossoverSlope(speakerTypeLpf, true);
                    }

                    @Override
                    public void onDecreaseSubmitClicked() {
                        getPresenter().setCrossoverSlope(speakerTypeLpf, false);
                    }
                };
            }
            item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
                @Override
                public void onIncreaseClicked() {

                }

                @Override
                public void onDecreaseClicked() {

                }

                @Override
                public void onAction(int typeId) {

                }

                @Override
                public void onIncreaseLocalClicked() {
                    getPresenter().setCrossoverCutOff(speakerTypeLpf, true);
                }

                @Override
                public void onDecreaseLocalClicked() {
                    getPresenter().setCrossoverCutOff(speakerTypeLpf, false);
                }

                @Override
                public void onIncreaseSubmitClicked() {
                    getPresenter().setCrossoverCutOff(speakerTypeLpf, true);
                }

                @Override
                public void onDecreaseSubmitClicked() {
                    getPresenter().setCrossoverCutOff(speakerTypeLpf, false);
                }
            };
            adapter.add(item);

            if (!isLandscape) {
                // LFP Slope
                item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.LPF_SLOPE);
                item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
                    @Override
                    public void onIncreaseClicked() {

                    }

                    @Override
                    public void onDecreaseClicked() {

                    }

                    @Override
                    public void onAction(int typeId) {

                    }

                    @Override
                    public void onIncreaseLocalClicked() {
                        getPresenter().setCrossoverSlope(speakerTypeLpf, true);
                    }

                    @Override
                    public void onDecreaseLocalClicked() {
                        getPresenter().setCrossoverSlope(speakerTypeLpf, false);
                    }

                    @Override
                    public void onIncreaseSubmitClicked() {
                        getPresenter().setCrossoverSlope(speakerTypeLpf, true);
                    }

                    @Override
                    public void onDecreaseSubmitClicked() {
                        getPresenter().setCrossoverSlope(speakerTypeLpf, false);
                    }
                };
                adapter.add(item);
            }
        }

        // Time Alignment
        item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.TIME_ALIGNMENT);
        item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
            @Override
            public void onIncreaseClicked() {

            }

            @Override
            public void onDecreaseClicked() {

            }

            @Override
            public void onAction(int typeId) {

            }

            @Override
            public void onIncreaseLocalClicked() {
                getPresenter().setTimeAlignment(mixedSpeakerType, true);
            }

            @Override
            public void onDecreaseLocalClicked() {
                getPresenter().setTimeAlignment(mixedSpeakerType, false);
            }

            @Override
            public void onIncreaseSubmitClicked() {
                getPresenter().setTimeAlignment(mixedSpeakerType, true);
            }

            @Override
            public void onDecreaseSubmitClicked() {
                getPresenter().setTimeAlignment(mixedSpeakerType, false);
            }
        };
        adapter.add(item);
    }

    /**
     * サブウーファーの設定画面のAdapterを設定
     *
     * @param mode
     */
    private void setupSwSpeakerSettingAdapter(AudioOutputMode mode) {
        int orientation = AppConfigUtil.getCurrentOrientation(getContext());
        boolean isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE;

        SpeakerSettingMenuAdapter adapter = mSwAdapter;
        SpeakerSettingMenuItem item;

        // Subwoofer on/off
        item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.SUBWOOFER);
        if (isLandscape) {
            item.type2 = SpeakerSettingMenuType.SUBWOOFER_PHASE;
            item.listener2 = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
                @Override
                public void onIncreaseClicked() {
                }

                @Override
                public void onDecreaseClicked() {
                }

                @Override
                public void onAction(int typeId) {
                }

                @Override
                public void onIncreaseLocalClicked() {
                    getPresenter().onToggleSubWooferPhase();
                }

                @Override
                public void onDecreaseLocalClicked() {
                    getPresenter().onToggleSubWooferPhase();
                }

                @Override
                public void onIncreaseSubmitClicked() {
                    getPresenter().onToggleSubWooferPhase();
                }

                @Override
                public void onDecreaseSubmitClicked() {
                    getPresenter().onToggleSubWooferPhase();
                }
            };
        }
        item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
            @Override
            public void onIncreaseClicked() {
            }

            @Override
            public void onDecreaseClicked() {
            }

            @Override
            public void onAction(int typeId) {
            }

            @Override
            public void onIncreaseLocalClicked() {
                getPresenter().onToggleSubWoofer();
            }

            @Override
            public void onDecreaseLocalClicked() {
                getPresenter().onToggleSubWoofer();
            }

            @Override
            public void onIncreaseSubmitClicked() {
                getPresenter().onToggleSubWoofer();
            }

            @Override
            public void onDecreaseSubmitClicked() {
                getPresenter().onToggleSubWoofer();
            }
        };

        adapter.add(item);

        if (!isLandscape) {
            // Subwoofer Phase
            item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.SUBWOOFER_PHASE);
            item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
                @Override
                public void onIncreaseClicked() {
                }

                @Override
                public void onDecreaseClicked() {
                }

                @Override
                public void onAction(int typeId) {
                }

                @Override
                public void onIncreaseLocalClicked() {
                    getPresenter().onToggleSubWooferPhase();
                }

                @Override
                public void onDecreaseLocalClicked() {
                    getPresenter().onToggleSubWooferPhase();
                }

                @Override
                public void onIncreaseSubmitClicked() {
                    getPresenter().onToggleSubWooferPhase();
                }

                @Override
                public void onDecreaseSubmitClicked() {
                    getPresenter().onToggleSubWooferPhase();
                }
            };
            adapter.add(item);
        }

        // Speaker Level
        item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.SPEAKER_LEVEL);
        item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
            @Override
            public void onIncreaseClicked() {
            }

            @Override
            public void onDecreaseClicked() {
            }

            @Override
            public void onAction(int typeId) {
            }

            @Override
            public void onIncreaseLocalClicked() {
                getPresenter().setSpeakerLevel(MixedSpeakerType.SUBWOOFER, true);
            }

            @Override
            public void onDecreaseLocalClicked() {
                getPresenter().setSpeakerLevel(MixedSpeakerType.SUBWOOFER, false);
            }

            @Override
            public void onIncreaseSubmitClicked() {
                getPresenter().setSpeakerLevel(MixedSpeakerType.SUBWOOFER, true);
            }

            @Override
            public void onDecreaseSubmitClicked() {
                getPresenter().setSpeakerLevel(MixedSpeakerType.SUBWOOFER, false);
            }
        };
        adapter.add(item);

        // LPF on/off
//        int hpfLpf = 1; // LPF
        final SpeakerType speakerType = (mode == AudioOutputMode.STANDARD)
                ? SpeakerType.SUBWOOFER_STANDARD_MODE : SpeakerType.SUBWOOFER_2WAY_NETWORK_MODE;
        item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.LPF);
        item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
            @Override
            public void onIncreaseClicked() {

            }

            @Override
            public void onDecreaseClicked() {

            }

            @Override
            public void onAction(int typeId) {

            }

            @Override
            public void onIncreaseLocalClicked() {
                getPresenter().onToggleCrossoverHpfLpf(speakerType);
            }

            @Override
            public void onDecreaseLocalClicked() {
                getPresenter().onToggleCrossoverHpfLpf(speakerType);
            }

            @Override
            public void onIncreaseSubmitClicked() {
                getPresenter().onToggleCrossoverHpfLpf(speakerType);
            }

            @Override
            public void onDecreaseSubmitClicked() {
                getPresenter().onToggleCrossoverHpfLpf(speakerType);
            }
        };
        adapter.add(item);

        // LPF Cutoff
        item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.LPF_CUTOFF);
        if (isLandscape) {
            item.type2 = SpeakerSettingMenuType.LPF_SLOPE;
            item.listener2 = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
                @Override
                public void onIncreaseClicked() {

                }

                @Override
                public void onDecreaseClicked() {

                }

                @Override
                public void onAction(int typeId) {

                }

                @Override
                public void onIncreaseLocalClicked() {
                    getPresenter().setCrossoverSlope(speakerType, true);
                }

                @Override
                public void onDecreaseLocalClicked() {
                    getPresenter().setCrossoverSlope(speakerType, false);
                }

                @Override
                public void onIncreaseSubmitClicked() {
                    getPresenter().setCrossoverSlope(speakerType, true);
                }

                @Override
                public void onDecreaseSubmitClicked() {
                    getPresenter().setCrossoverSlope(speakerType, false);
                }
            };
        }
        item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
            @Override
            public void onIncreaseClicked() {

            }

            @Override
            public void onDecreaseClicked() {

            }

            @Override
            public void onAction(int typeId) {

            }

            @Override
            public void onIncreaseLocalClicked() {
                getPresenter().setCrossoverCutOff(speakerType, true);
            }

            @Override
            public void onDecreaseLocalClicked() {
                getPresenter().setCrossoverCutOff(speakerType, false);
            }

            @Override
            public void onIncreaseSubmitClicked() {
                getPresenter().setCrossoverCutOff(speakerType, true);
            }

            @Override
            public void onDecreaseSubmitClicked() {
                getPresenter().setCrossoverCutOff(speakerType, false);
            }
        };
        adapter.add(item);

        if (!isLandscape) {
            // LFP Slope
            item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.LPF_SLOPE);
            item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
                @Override
                public void onIncreaseClicked() {

                }

                @Override
                public void onDecreaseClicked() {

                }

                @Override
                public void onAction(int typeId) {

                }

                @Override
                public void onIncreaseLocalClicked() {
                    getPresenter().setCrossoverSlope(speakerType, true);
                }

                @Override
                public void onDecreaseLocalClicked() {
                    getPresenter().setCrossoverSlope(speakerType, false);
                }

                @Override
                public void onIncreaseSubmitClicked() {
                    getPresenter().setCrossoverSlope(speakerType, true);
                }

                @Override
                public void onDecreaseSubmitClicked() {
                    getPresenter().setCrossoverSlope(speakerType, false);
                }
            };
            adapter.add(item);
        }

        // Time Alignment
        item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.TIME_ALIGNMENT);
        item.listener = new SpeakerSettingMenuItemView.OnSpeakerSettingCommitListener() {
            @Override
            public void onIncreaseClicked() {

            }

            @Override
            public void onDecreaseClicked() {

            }

            @Override
            public void onAction(int typeId) {

            }

            @Override
            public void onIncreaseLocalClicked() {
                getPresenter().setTimeAlignment(MixedSpeakerType.SUBWOOFER, true);
            }

            @Override
            public void onDecreaseLocalClicked() {
                getPresenter().setTimeAlignment(MixedSpeakerType.SUBWOOFER, false);
            }

            @Override
            public void onIncreaseSubmitClicked() {
                getPresenter().setTimeAlignment(MixedSpeakerType.SUBWOOFER, true);
            }

            @Override
            public void onDecreaseSubmitClicked() {
                getPresenter().setTimeAlignment(MixedSpeakerType.SUBWOOFER, false);
            }
        };
        adapter.add(item);
    }

    private void applySubwooferSetting(SubwooferSetting setting) {
        SpeakerSettingMenuItem item;
        item = mSwAdapter.findItem(SpeakerSettingMenuType.SUBWOOFER);
        item.currentValue = (setting == SubwooferSetting.ON) ? getString(R.string.com_002) : getString(R.string.com_001) ;
        if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
            writeItemToConvertView(mSwAdapter, mSwAdapter.getPosition(item), item.type);
        } else {
            mSwAdapter.notifyDataSetChanged();
        }

        mSwSpeakerInfo.setSpeakerEnabled(setting == SubwooferSetting.ON);
    }

    private void applySubwooferPhaseSetting(SubwooferPhaseSetting setting) {
        SpeakerSettingMenuItem item, item2;
        item = mSwAdapter.findItem(SpeakerSettingMenuType.SUBWOOFER_PHASE);
        item2 = mSwAdapter.findItem2(SpeakerSettingMenuType.SUBWOOFER_PHASE);
        String text;
        switch (setting) {
            case NORMAL:
                text = getString(R.string.set_148);
                break;
            case REVERSE:
                text =  getString(R.string.set_186);
                break;
            default:
                text = "";
                break;
        }

        if (item != null) {
            item.currentValue = text;
            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(mSwAdapter, mSwAdapter.getPosition(item), item.type);
            }
        } else {
            item2.currentValue2 = text;
            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(mSwAdapter, mSwAdapter.getPosition(item2), item2.type);
            }
        }
        if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
            Timber.d("#applySubwooferPhaseSetting: isBusy or CORE_AUDIO_ADV_SPK_MENU_NOTIFY");
        } else {
            mSwAdapter.notifyDataSetChanged();
        }

        mSwSpeakerInfo.setSubwooferPhase(
                setting == SubwooferPhaseSetting.NORMAL ?
                        SpeakerInfoView.SUBWOOFER_PHASE_NORMAL :
                        SpeakerInfoView.SUBWOOFER_PHASE_REVERSE
        );
    }

    private void applySpeakerLevelSetting(SpeakerLevelSetting setting) {
        mFlSpeakerInfo.setSpeakerLevel(setting.frontLeftHighLeftLevel);
        mFrSpeakerInfo.setSpeakerLevel(setting.frontRightHighRightLevel);
        mRlSpeakerInfo.setSpeakerLevel(setting.rearLeftMidLeftLevel);
        mRrSpeakerInfo.setSpeakerLevel(setting.rearRightMidRightLevel);
        mSwSpeakerInfo.setSpeakerLevel(setting.subwooferLevel);

        SpeakerSettingMenuItem item;
        for (SpeakerSettingMenuAdapter adapter : adapters()) {
            if (adapter == null) {
                continue;
            }
            item = adapter.findItem(SpeakerSettingMenuType.SPEAKER_LEVEL);
            if (item == null) continue;
            int level;
            if (adapter == mFlAdapter) {
                level = setting.frontLeftHighLeftLevel;
            } else if (adapter == mFrAdapter) {
                level = setting.frontRightHighRightLevel;
            } else if (adapter == mRlAdapter) {
                level = setting.rearLeftMidLeftLevel;
            } else if (adapter == mRrAdapter) {
                level = setting.rearRightMidRightLevel;
            } else if (adapter == mSwAdapter) {
                level = setting.subwooferLevel;
            } else {
                continue;
            }
            item.currentValue = String.format(Locale.ENGLISH, LEVEL_FORMAT, level);
            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private SpeakerSettingMenuAdapter[] adapters() {
        return new SpeakerSettingMenuAdapter[]{mFlAdapter, mFrAdapter, mRlAdapter, mRrAdapter, mSwAdapter};
    }

    private SpeakerInfoView[] speakerViews() {
        return new SpeakerInfoView[]{mFlSpeakerInfo, mFrSpeakerInfo, mRlSpeakerInfo, mRrSpeakerInfo, mSwSpeakerInfo};
    }

    private void applyPassFilterFrequency(CrossoverSetting setting, AudioOutputMode mode) {
        applyPassFilterFrequencyEx(setting, mode, null, AudioOutputMode.STANDARD, SpeakerType.FRONT);
    }

    /**
     * @param setting    - Remote StatusHolder
     * @param mode
     * @param my_speaker - Local Echo Buffer 描画の時設定される
     * @param my_mode    - 未対応
     * @param my_type    - Local Echo Buffer 描画の時設定される
     */
    private void applyPassFilterFrequencyEx(CrossoverSetting setting,
                                            AudioOutputMode mode,
                                            SpeakerCrossoverSetting my_speaker,
                                            AudioOutputMode my_mode,
                                            SpeakerType my_type) {
        SpeakerCrossoverSetting speaker;
        CutoffSetting cutoff;
        float freq;
        if (mode == AudioOutputMode.STANDARD) {    // Standard Mode時の描画
            // FrontのHPF
            speaker = setting.front;
            cutoff = setting.front.cutoffSetting;
            if ((my_speaker != null) && (mode == my_mode)) {
                if (setting.findSpeakerCrossoverSetting(my_type) == setting.front) {
                    speaker = my_speaker;
                    cutoff = my_speaker.cutoffSetting;
                }
            }
            freq = cutoff != null ? cutoff.getFrequency() : 0;

            mFlSpeakerInfo.setHighPassFilterFrequency(freq);
            mFlSpeakerInfo.setLowPassFilterFrequency(-1);
            mFrSpeakerInfo.setHighPassFilterFrequency(freq);
            mFrSpeakerInfo.setLowPassFilterFrequency(-1);

            applyToAdapters(speaker, mFlAdapter, mFrAdapter);

            // RearのHPF
            speaker = setting.rear;
            cutoff = setting.rear.cutoffSetting;
            if ((my_speaker != null) && (mode == my_mode)) {
                if (setting.findSpeakerCrossoverSetting(my_type) == setting.rear) {
                    speaker = my_speaker;
                    cutoff = my_speaker.cutoffSetting;
                }
            }
            freq = cutoff != null ? cutoff.getFrequency() : 0;
            mRlSpeakerInfo.setHighPassFilterFrequency(freq);
            mRlSpeakerInfo.setLowPassFilterFrequency(-1);
            mRrSpeakerInfo.setHighPassFilterFrequency(freq);
            mRrSpeakerInfo.setLowPassFilterFrequency(-1);

            applyToAdapters(speaker, mRlAdapter, mRrAdapter);

            // SubwooferのLPF
            speaker = setting.subwooferStandardMode;
            cutoff = setting.subwooferStandardMode.cutoffSetting;
            if ((my_speaker != null) && (mode == my_mode)) {
                if (setting.findSpeakerCrossoverSetting(my_type) == setting.subwooferStandardMode) {
                    speaker = my_speaker;
                    cutoff = my_speaker.cutoffSetting;
                }
            }
            freq = cutoff != null ? cutoff.getFrequency() : 0;
            mSwSpeakerInfo.setHighPassFilterFrequency(-1);
            mSwSpeakerInfo.setLowPassFilterFrequency(freq);

            applyToAdapters(speaker, null, mSwAdapter);

            updateFilterGraph(mode, my_speaker, my_mode, my_type, null);
        } else {
            // 2 Way Network Mode時の描画
            cutoff = setting.high.cutoffSetting;
            freq = cutoff != null ? cutoff.getFrequency() : 0;
            mFlSpeakerInfo.setHighPassFilterFrequency(freq);
            mFlSpeakerInfo.setLowPassFilterFrequency(-1);
            mFrSpeakerInfo.setHighPassFilterFrequency(freq);
            mFrSpeakerInfo.setLowPassFilterFrequency(-1);

            // HighのHPF
            applyToAdapters(setting.high, mFlAdapter, mFrAdapter);

            cutoff = setting.midHPF.cutoffSetting;
            freq = cutoff != null ? cutoff.getFrequency() : 0;
            mRlSpeakerInfo.setHighPassFilterFrequency(freq);
            mRrSpeakerInfo.setHighPassFilterFrequency(freq);

            cutoff = setting.midLPF.cutoffSetting;
            freq = cutoff != null ? cutoff.getFrequency() : 0;
            mRlSpeakerInfo.setLowPassFilterFrequency(freq);
            mRrSpeakerInfo.setLowPassFilterFrequency(freq);

            // Mid-HPF
            applyToAdapters(setting.midHPF, mRlAdapter, mRrAdapter);

            // Mid-LPF
            applyToAdapters(setting.midLPF, mRlAdapter, mRrAdapter);

            cutoff = setting.subwoofer2WayNetworkMode.cutoffSetting;
            freq = cutoff != null ? cutoff.getFrequency() : 0;
            mSwSpeakerInfo.setHighPassFilterFrequency(-1);
            mSwSpeakerInfo.setLowPassFilterFrequency(freq);

            // SubwooferのLPF
            applyToAdapters(setting.subwoofer2WayNetworkMode, mSwAdapter);
        }
    }

    private void applyToAdapters(SpeakerCrossoverSetting setting, GridSpeakerSettingMenuAdapter... adapters) {
        HpfLpfSetting hpflpf = setting.hpfLpfSetting;
        boolean on = hpflpf == HpfLpfSetting.ON
                || hpflpf == HpfLpfSetting.ON_FIXED;

        CutoffSetting cutoff = setting.cutoffSetting;
        float freq = cutoff != null ? cutoff.getFrequency() : 0;

        SlopeSetting slope = setting.slopeSetting;
        int level = slope != null ? slope.getLevel() : 0;

        boolean hpf = setting.speakerType == SpeakerType.FRONT
                || setting.speakerType == SpeakerType.REAR
                || setting.speakerType == SpeakerType.HIGH
                || setting.speakerType == SpeakerType.MID_HPF;

        SpeakerSettingMenuItem item, item2;

        for (GridSpeakerSettingMenuAdapter adapter : adapters) {
            if (adapter == null) {
                continue;
            }
            item = adapter.findItem(hpf ? SpeakerSettingMenuType.HPF : SpeakerSettingMenuType.LPF);
            item.currentValue = on ? getString(R.string.com_002) : getString(R.string.com_001);
            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
            }

            item = adapter.findItem(hpf ? SpeakerSettingMenuType.HPF_CUTOFF : SpeakerSettingMenuType.LPF_CUTOFF);
            item.currentValue = NumberFormatUtil.formatFrequency(freq, true);
            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
            }

            item = adapter.findItem(hpf ? SpeakerSettingMenuType.HPF_SLOPE : SpeakerSettingMenuType.LPF_SLOPE);
            item2 = adapter.findItem2(hpf ? SpeakerSettingMenuType.HPF_SLOPE : SpeakerSettingMenuType.LPF_SLOPE);
            if (item != null) {
                item.currentValue = String.format(Locale.ENGLISH, LEVEL_FORMAT, level);
                if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                    writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
                }
            } else {
                item2.currentValue2 = String.format(Locale.ENGLISH, LEVEL_FORMAT, level);
                if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                    writeItemToConvertView(adapter, adapter.getPosition(item2), item2.type);
                }
            }

            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                Timber.d("#applyToAdapters: isBusy or CORE_AUDIO_ADV_SPK_MENU_NOTIFY");
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void applyTimeAlignment(TimeAlignmentSetting setting, ListeningPositionSetting listeningPosition) {
        // Front LeftかFront Rightの場合だけ有効
        boolean enabled = listeningPosition == ListeningPositionSetting.FRONT_LEFT || listeningPosition == ListeningPositionSetting.FRONT_RIGHT;
        float multiplier = setting.stepUnit != null ? setting.stepUnit.multiplier : 0;
        int unit = (setting.stepUnit == TimeAlignmentStepUnit._1INCH)
                ? SpeakerInfoView.TIME_ALIGNMENT_UNIT_INCH : SpeakerInfoView.TIME_ALIGNMENT_UNIT_CM;
        mFlSpeakerInfo.setTimeAlignment(setting.frontLeftHighLeftStep * multiplier, unit);
        mFrSpeakerInfo.setTimeAlignment(setting.frontRightHighRightStep * multiplier, unit);
        mRlSpeakerInfo.setTimeAlignment(setting.rearLeftMidLeftStep * multiplier, unit);
        mRrSpeakerInfo.setTimeAlignment(setting.rearRightMidRightStep * multiplier, unit);
        mSwSpeakerInfo.setTimeAlignment(setting.subwooferStep * multiplier, unit);

        switch (setting.mode) {
            case OFF:
                mTaButton.setTaMode(TimeAlignmentButton.TA_MODE_OFF);
                break;
            case AUTO_TA:
                mTaButton.setTaMode(TimeAlignmentButton.TA_MODE_ATA);
                break;
            case INITIAL:
                mTaButton.setTaMode(TimeAlignmentButton.TA_MODE_INITIAL);
                break;
            case CUSTOM:
                mTaButton.setTaMode(TimeAlignmentButton.TA_MODE_CUSTOM);
                break;
            default:
                break;
        }

        applyToAdapter(setting.frontLeftHighLeftStep * multiplier, setting.stepUnit, mFlAdapter);
        applyToAdapter(setting.frontRightHighRightStep * multiplier, setting.stepUnit, mFrAdapter);
        applyToAdapter(setting.rearLeftMidLeftStep * multiplier, setting.stepUnit, mRlAdapter);
        applyToAdapter(setting.rearRightMidRightStep * multiplier, setting.stepUnit, mRrAdapter);
        applyToAdapter(setting.subwooferStep * multiplier, setting.stepUnit, mSwAdapter);
    }

    private void applyToAdapter(float distance, TimeAlignmentStepUnit unit, SpeakerSettingMenuAdapter adapter) {
        SpeakerSettingMenuItem item = adapter.findItem(SpeakerSettingMenuType.TIME_ALIGNMENT);
        String unitText = unit == TimeAlignmentStepUnit._2_5CM ? getString(R.string.ta_distance_unit_cm) : getString(R.string.ta_distance_unit_in);
        String text = String.format(Locale.ENGLISH, DISTANCE_FORMAT, distance, unitText);
        text = text.replace(".0", "");
        item.currentValue = text;
        if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
            writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
        }
    }

    private void applyListeningPosition(ListeningPositionSetting setting) {
        String text;
        int position;
        switch (setting) {
            case OFF:
                text = getString(R.string.com_001);
                position = SeatView.LISTENING_POSITION_OFF;
                break;
            case FRONT_LEFT:
                text = getString(R.string.val_058);
                position = SeatView.LISTENING_POSITION_FRONT_LEFT;
                break;
            case FRONT_RIGHT:
                text = getString(R.string.val_059);
                position = SeatView.LISTENING_POSITION_FRONT_RIGHT;
                break;
            case FRONT:
                text = getString(R.string.val_057);
                position = SeatView.LISTENING_POSITION_FRONT;
                break;
            case ALL:
                text = getString(R.string.set_011);
                position = SeatView.LISTENING_POSITION_ALL;
                break;
            default:
                text = null;
                position = SeatView.LISTENING_POSITION_OFF;
                break;
        }
        mStdSeatView.setListeningPosition(position);
        mNwSeatView.setListeningPosition(position);
        mListeningPositionText.setText(text);
        mListeningPositionSettingMenu.setCurrentListeningPosition(setting);
    }

    private void applyAppearance() {
        if (mFlSpeakerInfo == null) return; // Viewが設定されてないとみなす
        mStatusHolder = mPresenter.getStatusHolder();
        Context context = getContext();
        int uiColor = mPresenter.getUiColor();
        int color = ContextCompat.getColor(context, mPresenter.getUiColor());
        int flfrColor = ContextCompat.getColor(context, R.color.audio_speaker_flfr);
        int rlrrColor = ContextCompat.getColor(context, R.color.audio_speaker_rlrr);
        int swColor = ContextCompat.getColor(context, R.color.audio_speaker_sw);

        // 周波数グラフにスピーカデザイン色の適用
        mFilterGraphController.setSpeakerColors(
                flfrColor,
                rlrrColor,
                swColor
        );

        for (SpeakerInfoView v : speakerViews()) {
            if (v == null) continue;
            //v.setSpeakerIconBackgroundTint(speakerIconBackgroundTint);
            v.setUiColor(uiColor);
        }

        // 下線部のカラーを設定
        mFlSpeakerInfo.setSpeakerTypeLineColor(flfrColor);
        mFrSpeakerInfo.setSpeakerTypeLineColor(flfrColor);
        mRlSpeakerInfo.setSpeakerTypeLineColor(rlrrColor);
        mRrSpeakerInfo.setSpeakerTypeLineColor(rlrrColor);
        mSwSpeakerInfo.setSpeakerTypeLineColor(swColor);

        // シートの色を変更
/*        ColorStateList seatBackgroundTint;
        if (mStatusHolder.isAudioSettingSupported()) {
            // 車種専用データがある場合はdisabledの状態でも通常通り表示する #750#note-6
            seatBackgroundTint = ContextCompat.getColorStateList(
                    context, mPresenter.getUiColor()
            );
        } else {
            seatBackgroundTint = ContextCompat.getColorStateList(
                    context, mPresenter.getUiColor()
            );
        }
        mStdSeatView.setSeatBackgroundTint(seatBackgroundTint);
        mNwSeatView.setSeatBackgroundTint(seatBackgroundTint);*/

        // Listening Positionテキスト色
        if (mListeningPositionText != null) mListeningPositionText.setTextColor(ContextCompat.getColor(context,R.color.drawable_white_color));

        // TA設定ボタン
        ViewCompat.setBackgroundTintList(mTaButton, ContextCompat.getColorStateList(context, R.color.button_tint));
        mTaButton.setValueColorList(ContextCompat.getColorStateList(context, R.color.speaker_setting_menu_item_title));

        // スピーカー設定メニューのテキストカラーの設定
        ColorStateList buttonBackgroundTint = ContextCompat.getColorStateList(context, R.color.button_tint_arrow);
        ColorStateList textColorList = ContextCompat.getColorStateList(context, R.color.setting_menu_item_value_text);
        for (SpeakerSettingMenuAdapter adapter : adapters()) {
            if (adapter == null) {
                continue;
            }
            adapter.setButtonBackgroundTint(buttonBackgroundTint);
            adapter.setCurrentValueTextColor(textColorList);
        }

        // Listening PositionのListViewのSelector切り替え
        //mListeningPositionSettingMenu.setListViewSelector(mPresenter.getUiColor());
    }

    @OnClick(R.id.taButton)
    void onTaButtonClicked() {
        mPresenter.onToggleTimeAlignmentMode();
    }

    @OnClick({R.id.flSpeakerInfo, R.id.frSpeakerInfo, R.id.rlSpeakerInfo, R.id.rrSpeakerInfo, R.id.swSpeakerInfo})
    void onSpeakerInfoClicked(SpeakerInfoView v) {
        mStatusHolder = mPresenter.getStatusHolder();
        CarDeviceSpec spec = mStatusHolder.getCarDeviceSpec();
        AudioOutputMode mode = spec.audioSettingSpec.audioOutputMode;
        int type, speakerTypeLineColor;
        SpeakerSettingMenuAdapter adapter;
        FilterGraphViewController.Speaker filterGraphSpeaker;
        Context context = getContext();

        int flfrColor = ContextCompat.getColor(context, R.color.audio_speaker_flfr);
        int rlrrColor = ContextCompat.getColor(context, R.color.audio_speaker_rlrr);
        int swColor = ContextCompat.getColor(context, R.color.audio_speaker_sw);
        switch (v.getId()) {
            case R.id.flSpeakerInfo:
                type = (mode == AudioOutputMode.STANDARD)
                        ? SpeakerSettingMenuView.SPEAKER_TYPE_FL : SpeakerSettingMenuView.SPEAKER_TYPE_HL;
                adapter = mFlAdapter;
                filterGraphSpeaker = FilterGraphViewController.Speaker.FrontLeft;
                speakerTypeLineColor = flfrColor;
                break;
            case R.id.frSpeakerInfo:
                type = (mode == AudioOutputMode.STANDARD)
                        ? SpeakerSettingMenuView.SPEAKER_TYPE_FR : SpeakerSettingMenuView.SPEAKER_TYPE_HR;
                adapter = mFrAdapter;
                filterGraphSpeaker = FilterGraphViewController.Speaker.FrontRight;
                speakerTypeLineColor = flfrColor;
                break;
            case R.id.rlSpeakerInfo:
                type = (mode == AudioOutputMode.STANDARD)
                        ? SpeakerSettingMenuView.SPEAKER_TYPE_RL : SpeakerSettingMenuView.SPEAKER_TYPE_ML;
                adapter = mRlAdapter;
                filterGraphSpeaker = FilterGraphViewController.Speaker.RearLeft;
                speakerTypeLineColor = rlrrColor;
                break;
            case R.id.rrSpeakerInfo:
                type = (mode == AudioOutputMode.STANDARD)
                        ? SpeakerSettingMenuView.SPEAKER_TYPE_RR : SpeakerSettingMenuView.SPEAKER_TYPE_MR;
                adapter = mRrAdapter;
                filterGraphSpeaker = FilterGraphViewController.Speaker.RearRight;
                speakerTypeLineColor = rlrrColor;
                break;
            case R.id.swSpeakerInfo:
                type = SpeakerSettingMenuView.SPEAKER_TYPE_SW;
                adapter = mSwAdapter;
                filterGraphSpeaker = FilterGraphViewController.Speaker.Subwoofer;
                speakerTypeLineColor = swColor;
                break;
            default:
                return;
        }
        adapter.notifyDataSetChanged();    // 初期処理なので、CORE_AUDIO_ADV_SPK_MENU_NOTIFY 対象外
        mSpeakerSettingMenu.setSpeakerType(type);
        mSpeakerSettingMenu.setSpeakerTypeLineColor(speakerTypeLineColor);
        mSpeakerSettingMenu.setAdapter(adapter);
        mSpeakerSettingMenu.slideUp();
        setFilterGraphCurrentSpeaker(filterGraphSpeaker);
    }

    @OnClick({R.id.stdSeatIconContainer, R.id.nwSeatIconContainer})
    void onSeatClicked() {
        // 設定のポップアップを開く
        mListeningPositionSettingMenu.slideUp();
    }

    @OnClick(R.id.graphViewContainer)
    void onGraphClicked() {
        if (mSpeakerSettingMenu.getVisibility() == View.VISIBLE) {
            mSpeakerSettingMenu.slideDown();
            setFilterGraphCurrentSpeaker(null);
        } else if (mListeningPositionSettingMenu.getVisibility() == View.VISIBLE) {
            mListeningPositionSettingMenu.slideDown();
        }
    }

    @OnClick({R.id.speakerSettingMenu, R.id.listeningPositionSettingMenu})
    void onMenuClicked() {
        // 背面のボタンにクリックイベントを渡さないためのダミー
    }

/*    public boolean onBackPressed() {
        boolean handled = false;
        if (mSpeakerSettingMenu.getVisibility() == View.VISIBLE) {
            mSpeakerSettingMenu.slideDown();
            setFilterGraphCurrentSpeaker(null);
            handled = true;
        }
        if (!handled && mListeningPositionSettingMenu.getVisibility() == View.VISIBLE) {
            mListeningPositionSettingMenu.slideDown();
            handled = true;
        }
        return handled;
    }*/

    private AdapterView.OnItemClickListener mOnListeningPositionItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListeningPositionAdapter adapter = (ListeningPositionAdapter) parent.getAdapter();
            mListeningPositionSettingMenu.slideDown();

            ListeningPositionSetting item = adapter.getItem(position);
            mPresenter.setListeningPosition(item);
        }
    };

    @BindView(R.id.filterGraphViewL)
    FilterGraphView mFilterGraphViewL;
    @BindView(R.id.filterGraphViewR)
    FilterGraphView mFilterGraphViewR;
    @Nullable
    @BindView(R.id.frequenciesL)
    FilterFrequencyView mFrequenciesL;
    @Nullable
    @BindView(R.id.frequenciesR)
    FilterFrequencyView mFrequenciesR;

    private FilterGraphViewController mFilterGraphController;

    private void setupFilterGraph() {
        FilterGraphSpec graphSpec = FilterDesignDefaults.defaultFilterGraphSpec();
        FilterGraphGeometry graphGeometry = new FilterGraphGeometry(graphSpec);
        FilterPathBuilder pathBuilder = new FilterPathBuilder(graphGeometry);
        mStatusHolder = mPresenter.getStatusHolder();

        if (mFrequenciesL != null)
            mFrequenciesL.setFrequencyStrings(graphGeometry);
        if (mFrequenciesR != null)
            mFrequenciesR.setFrequencyStrings(graphGeometry);

        mFilterGraphViewL.getBackgroundView().setGridPath(FilterDesignDefaults.defaultPathBuilderGridPathSpec(), graphGeometry);
        mFilterGraphViewR.getBackgroundView().setGridPath(FilterDesignDefaults.defaultPathBuilderGridPathSpec(), graphGeometry);

        mFilterGraphController = new FilterGraphViewController(pathBuilder, mFilterGraphViewL, mFilterGraphViewR);
        if (mStatusHolder.getCarDeviceSpec().jasperAudioSettingSupported) {
            mFilterGraphViewR.setVisibility(View.GONE);
        }
    }

    @Override
    public void redrawFilterGraph(boolean animated) {
        if (animated)
            mFilterGraphController.update();
        else
            mFilterGraphController.update(0);
    }


    private void setFilterGraphCurrentSpeaker(@Nullable FilterGraphViewController.Speaker speaker) {
        mFilterGraphController.setCurrentSpeaker(speaker);
    }

    /**
     * @param mode
     * @param my_speaker    - Local Echo Buffer 描画の時設定される
     * @param my_mode
     * @param my_type       - Local Echo Buffer 描画の時設定される
     * @param my_speakerLvl - Local Echo Buffer 描画の時設定される
     */
    private void updateFilterGraph(AudioOutputMode mode,
                                   SpeakerCrossoverSetting my_speaker,
                                   AudioOutputMode my_mode,
                                   SpeakerType my_type, SpeakerLevelSetting my_speakerLvl
    ) {
        mStatusHolder = mPresenter.getStatusHolder();
        mFilterGraphController.setSubwooferOn(mStatusHolder.getAudioSetting().subwooferSetting == SubwooferSetting.ON);

        SpeakerLevelSetting speakerLevel = (my_speakerLvl != null) ? my_speakerLvl : mStatusHolder.getAudioSetting().speakerLevelSetting;
        CrossoverSetting xover = mStatusHolder.getAudioSetting().crossoverSetting;
//        SpeakerCrossoverSetting speaker;
        FilterGraphViewController.Crossover graphCrossover;

        switch (mode) {
            case STANDARD:
                graphCrossover = null;
                if ((my_speaker != null) && (my_mode == mode) && (my_type == SpeakerType.FRONT)) {
                    graphCrossover = translateCrossover(my_speaker);
                } else if (my_speaker == null) {
                    graphCrossover = translateCrossover(xover.front);
                }
                if (graphCrossover != null) {
                    mFilterGraphController.setFilter(FilterGraphViewController.Speaker.FrontLeft,
                            speakerLevel.frontLeftHighLeftLevel,
                            graphCrossover,
                            null,
                            isBusy());
                }

                graphCrossover = null;
                if ((my_speaker != null) && (my_mode == mode) && (my_type == SpeakerType.FRONT)) {
                    graphCrossover = translateCrossover(my_speaker);
                } else if (my_speaker == null) {
                    graphCrossover = translateCrossover(xover.front);
                }
                if (graphCrossover != null) {
                    mFilterGraphController.setFilter(FilterGraphViewController.Speaker.FrontRight,
                            speakerLevel.frontRightHighRightLevel,
                            graphCrossover,
                            null,
                            isBusy());
                }

                graphCrossover = null;
                if ((my_speaker != null) && (my_mode == mode) && (my_type == SpeakerType.REAR)) {
                    graphCrossover = translateCrossover(my_speaker);
                } else if (my_speaker == null) {
                    graphCrossover = translateCrossover(xover.rear);
                }
                if (graphCrossover != null) {
                    mFilterGraphController.setFilter(FilterGraphViewController.Speaker.RearLeft,
                            speakerLevel.rearLeftMidLeftLevel,
                            graphCrossover,
                            null,
                            isBusy());
                }

                graphCrossover = null;
                if ((my_speaker != null) && (my_mode == mode) && (my_type == SpeakerType.REAR)) {
                    graphCrossover = translateCrossover(my_speaker);
                } else if (my_speaker == null) {
                    graphCrossover = translateCrossover(xover.rear);
                }
                if (graphCrossover != null) {
                    mFilterGraphController.setFilter(FilterGraphViewController.Speaker.RearRight,
                            speakerLevel.rearRightMidRightLevel,
                            graphCrossover,
                            null,
                            isBusy());
                }

                graphCrossover = null;
                if ((my_speaker != null) && (my_mode == mode) && (my_type == SpeakerType.SUBWOOFER_STANDARD_MODE)) {
                    graphCrossover = translateCrossover(my_speaker);
                } else if (my_speaker == null) {
                    graphCrossover = translateCrossover(xover.subwooferStandardMode);
                }
                if (graphCrossover != null) {
                    mFilterGraphController.setFilter(FilterGraphViewController.Speaker.Subwoofer,
                            speakerLevel.subwooferLevel,
                            null,
                            graphCrossover,
                            isBusy());
                }
                break;

            case TWO_WAY_NETWORK:
                mFilterGraphController.setFilter(FilterGraphViewController.Speaker.FrontLeft,
                        speakerLevel.frontLeftHighLeftLevel,
                        translateCrossover(xover.high),
                        null,
                        isBusy());
                mFilterGraphController.setFilter(FilterGraphViewController.Speaker.FrontRight,
                        speakerLevel.frontRightHighRightLevel,
                        translateCrossover(xover.high),
                        null,
                        isBusy());

                mFilterGraphController.setFilter(FilterGraphViewController.Speaker.RearLeft,
                        speakerLevel.rearLeftMidLeftLevel,
                        translateCrossover(xover.midHPF),
                        translateCrossover(xover.midLPF),
                        isBusy());

                mFilterGraphController.setFilter(FilterGraphViewController.Speaker.RearRight,
                        speakerLevel.rearRightMidRightLevel,
                        translateCrossover(xover.midHPF),
                        translateCrossover(xover.midLPF),
                        isBusy());


                mFilterGraphController.setFilter(FilterGraphViewController.Speaker.Subwoofer,
                        speakerLevel.subwooferLevel,
                        null,
                        translateCrossover(xover.subwoofer2WayNetworkMode),
                        isBusy());
                break;
        }
    }

    @Nullable
    private FilterGraphViewController.Crossover translateCrossover(@Nullable SpeakerCrossoverSetting src) {
        if (src == null)
            return null;
        boolean on;
        switch (src.hpfLpfSetting) {
            case ON:
            case ON_FIXED:
                on = true;
                break;
            default:
                on = false;
                break;
        }

        return new FilterGraphViewController.Crossover(on, (long) (src.cutoffSetting.getFrequency() * 1000), src.slopeSetting.getLevel());
    }

    /**
     * Menu画面が利用不可の状態の場合は表示しているダイアログを閉じる
     */
    protected void checkMenuIsAvailable() {
        mStatusHolder = mPresenter.getStatusHolder();
        if (!mStatusHolder.isAudioSettingSupported()) {
            dismissDialog();
        }
    }

    protected void dismissDialog() {
        if (mSpeakerSettingMenu.getVisibility() == View.VISIBLE) {
            mSpeakerSettingMenu.slideDown();
            setFilterGraphCurrentSpeaker(null);
        } else if (mListeningPositionSettingMenu.getVisibility() == View.VISIBLE) {
            mListeningPositionSettingMenu.slideDown();
        }
    }

    @Override
    public boolean onGoBack() {
        boolean result = false;
        if (mSpeakerSettingMenu.getVisibility() == View.VISIBLE) {
            mSpeakerSettingMenu.slideDown();
            setFilterGraphCurrentSpeaker(null);
            result = true;
        } else if (mListeningPositionSettingMenu.getVisibility() == View.VISIBLE) {
            mListeningPositionSettingMenu.slideDown();
            result = true;
        }
        return result;
    }
}
