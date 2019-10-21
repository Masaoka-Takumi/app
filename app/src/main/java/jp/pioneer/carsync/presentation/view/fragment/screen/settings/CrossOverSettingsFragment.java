package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.CrossoverSetting;
import jp.pioneer.carsync.domain.model.CutoffSetting;
import jp.pioneer.carsync.domain.model.HpfLpfFilterType;
import jp.pioneer.carsync.domain.model.HpfLpfSetting;
import jp.pioneer.carsync.domain.model.JasperSlopeSetting;
import jp.pioneer.carsync.domain.model.SlopeSetting;
import jp.pioneer.carsync.domain.model.SpeakerLevelSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SubwooferPhaseSetting;
import jp.pioneer.carsync.domain.model.SubwooferSetting;
import jp.pioneer.carsync.presentation.presenter.CrossOverSettingsPresenter;
import jp.pioneer.carsync.presentation.util.AppConfigUtil;
import jp.pioneer.carsync.presentation.util.FilterDesignDefaults;
import jp.pioneer.carsync.presentation.util.FilterGraphGeometry;
import jp.pioneer.carsync.presentation.util.FilterGraphSpec;
import jp.pioneer.carsync.presentation.util.FilterPathBuilder;
import jp.pioneer.carsync.presentation.util.NumberFormatUtil;
import jp.pioneer.carsync.presentation.view.CrossOverSettingsView;
import jp.pioneer.carsync.presentation.view.adapter.JasperSpeakerSettingMenuAdapter;
import jp.pioneer.carsync.presentation.view.adapter.SpeakerSettingMenuAdapter;
import jp.pioneer.carsync.presentation.view.adapter.SpeakerSettingMenuAdapter.SpeakerSettingMenuItem;
import jp.pioneer.carsync.presentation.view.adapter.SpeakerSettingMenuAdapter.SpeakerSettingMenuType;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.widget.FilterFrequencyView;
import jp.pioneer.carsync.presentation.view.widget.FilterGraphView;
import jp.pioneer.carsync.presentation.view.widget.JasperFilterGraphViewController;
import jp.pioneer.carsync.presentation.view.widget.JasperSpeakerSettingMenuView;
import jp.pioneer.carsync.presentation.view.widget.SpeakerInfoView;
import timber.log.Timber;

import static android.R.color.white;


/**
 * Created by NSW00_906320 on 2017/07/28.
 */

public class CrossOverSettingsFragment extends AbstractScreenFragment<CrossOverSettingsPresenter, CrossOverSettingsView> implements CrossOverSettingsView {
    @Inject CrossOverSettingsPresenter mPresenter;
    private Unbinder mUnbinder;
    private static final String LEVEL_FORMAT = "%+ddB";
    private static final boolean CORE_AUDIO_ADV_SPK_MENU_NOTIFY = false;
    private static final boolean CORE_17LOW_CROSSOVER_NO_LPF_SW = true;
    @BindView(R.id.titleText)
    TextView mTitleText;

    @BindView(R.id.swSpeakerInfo)
    SpeakerInfoView mSwSpeakerInfo;

    @BindView(R.id.speakerSettingMenu)
    JasperSpeakerSettingMenuView mSpeakerSettingMenu;

    /**
     * 16Highでは Speaker ごとに 当 Adapter を用意しますが、
     * 17LOWでは、この１個しかない（Speaker を選択することが出来ない）
     * Speaker の設定というより、Jasper の設定と考えると良い。
     */
    private JasperSpeakerSettingMenuAdapter _mCrossoverMenuAdapter;

    /**
     * コンストラクタ
     */
    public CrossOverSettingsFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return EulaFragment
     */
    public static CrossOverSettingsFragment newInstance(Bundle args) {
        CrossOverSettingsFragment fragment = new CrossOverSettingsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public JasperSpeakerSettingMenuAdapter getCrossoverMenuAdapter() {
        return _mCrossoverMenuAdapter;
    }

    public void setCrossoverMenuAdapter(JasperSpeakerSettingMenuAdapter adapter) {
        this._mCrossoverMenuAdapter = adapter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        ColorStateList backgroundTint = ContextCompat.getColorStateList(context, white);
        ColorStateList textColor = ContextCompat.getColorStateList(context, white);

        JasperSpeakerSettingMenuAdapter adapter = new JasperSpeakerSettingMenuAdapter(context, "Jasper", backgroundTint, textColor);
        setCrossoverMenuAdapter(adapter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jasper_crossover_settings, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        setupFilterGraph();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        openCrossoverSetting();        // Crossover Settings 画面作成（mSwAdapter にMenu Items を作る）
        applyStatus();                // Menu Items の Enable | Disableを設定し、データ eg「50Hz」を設定する
        applyAppearance();

        redrawFilterGraph(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        showSettingMenu();
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        setCrossoverMenuAdapter(null);

        super.onDestroy();
    }


    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected CrossOverSettingsPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.CROSS_OVER_SETTINGS;
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
                Timber.d("AutoRepeatButton change busy to true on JasperCrossoverSettingsFragment");
            } else {
                Timber.d("AutoRepeatButton change busy to false on JasperCrossoverSettingsFragment");
            }
        }

        this.busy = busy;

        if (mSpeakerSettingMenu != null) mSpeakerSettingMenu.setBusy(busy);

        if (_mCrossoverMenuAdapter != null) _mCrossoverMenuAdapter.setBusy(busy);
    }

    public void requestDisallowInterceptTouchEv(boolean disallowIntercept) {    // AdvancedSettingIf
        if (mSpeakerSettingMenu != null) {
            mSpeakerSettingMenu.requestDisallowInterceptTouchEv(disallowIntercept);
        }
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
    private View writeItemToConvertView(SpeakerSettingMenuAdapter adapter, int position, SpeakerSettingMenuType mainType) {
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

    /**
     * Menu Items の Enable | Disableを設定し、データ eg「50Hz」を設定する
     */
    private void applyStatus() {
        if (mSwSpeakerInfo == null) {
            return; // Viewが設定されてないので
        }

        StatusHolder holder = mPresenter.getStatusHolder();
        AudioSetting audioSetting = holder.getAudioSetting();

        applyEnabled(holder);

        applySubwooferSetting(audioSetting.subwooferSetting);
        applySubwooferPhaseSetting(audioSetting.subwooferPhaseSetting);
        applyPassFilterFrequency(audioSetting.crossoverSetting);

        updateFilterGraph(audioSetting, null, HpfLpfFilterType.HPF, null);
    }

    private void applyEnabled(StatusHolder holder) {
        AudioSettingStatus audioSettingStatus = holder.getAudioSettingStatus();
        AudioSetting audioSetting = holder.getAudioSetting();

        boolean audioEnabled = audioSettingStatus.crossoverSettingEnabled;

        boolean enabled;
        SpeakerSettingMenuAdapter.SpeakerSettingMenuItem item;

        // Subwoofer設定
        enabled = audioEnabled && audioSettingStatus.subwooferSettingEnabled;
        item = getCrossoverMenuAdapter().findItem(SpeakerSettingMenuType.SUBWOOFER);
        if (item != null) {
            item.enabled = enabled;
            item.increaseEnabled = enabled;
            item.decreaseEnabled = enabled;
            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(_mCrossoverMenuAdapter, _mCrossoverMenuAdapter.getPosition(item), item.type);
            }
        }

        boolean subwooferOn = enabled
                && audioSetting.subwooferSetting == SubwooferSetting.ON;

        SubwooferPhaseSetting subwooferPhaseSetting = audioSetting.subwooferPhaseSetting;

        // Subwoofer Phase設定
        enabled = audioEnabled
                && audioSettingStatus.subwooferPhaseSettingEnabled
                && subwooferOn;
        item = getCrossoverMenuAdapter().findItem(SpeakerSettingMenuType.SUBWOOFER_PHASE);
        if (item != null) {
            item.enabled = enabled;
            item.increaseEnabled = enabled;
            item.decreaseEnabled = enabled;
        } else {
            item = getCrossoverMenuAdapter().findItem2(SpeakerSettingMenuType.SUBWOOFER_PHASE);
            item.enabled2 = enabled;
            item.increaseEnabled2 = enabled;
            item.decreaseEnabled2 = enabled;
        }
        if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
            writeItemToConvertView(_mCrossoverMenuAdapter, _mCrossoverMenuAdapter.getPosition(item), item.type);
        } else {
            _mCrossoverMenuAdapter.notifyDataSetChanged();
        }

        /*
         * Crossover設定(HPF/LPF, Cutoff周波数, Slope)
         * TODO 17LOW は crossoverSettingEnabled を見るべきと思われる
         * （画面仕様書は「条件=常時」ですが）
         */
        enabled = audioEnabled && audioSettingStatus.crossoverSettingEnabled;
        applyEnabledToAdapters(enabled, subwooferOn, subwooferPhaseSetting, audioSetting.crossoverSetting, getCrossoverMenuAdapter());
    }


    /**
     * TODO CrossoverSetting.JasperCrossoverSetting
     *
     * @param enabled
     * @param subwooferOn
     * @param subwooferPhaseSetting
     * @param crossoverSetting
     * @param adapters
     */
    private void applyEnabledToAdapters(boolean enabled, boolean subwooferOn, SubwooferPhaseSetting subwooferPhaseSetting, CrossoverSetting crossoverSetting, JasperSpeakerSettingMenuAdapter... adapters) {
        boolean hpfOn = ((crossoverSetting.jasperHpf.hpfLpfSetting == HpfLpfSetting.ON) ||
                (crossoverSetting.jasperHpf.hpfLpfSetting == HpfLpfSetting.ON_FIXED));
        boolean lpfOn = ((crossoverSetting.jasperLpf.hpfLpfSetting == HpfLpfSetting.ON) ||
                (crossoverSetting.jasperLpf.hpfLpfSetting == HpfLpfSetting.ON_FIXED));

        CutoffSetting hpfCutoffSetting = crossoverSetting.jasperHpf.cutoffSetting;
        CutoffSetting lpfCutoffSetting = crossoverSetting.jasperLpf.cutoffSetting;
        SlopeSetting hpfSlopeSetting = crossoverSetting.jasperHpf.slopeSetting;
        SlopeSetting lpfSlopeSetting = crossoverSetting.jasperLpf.slopeSetting;

        SpeakerSettingMenuAdapter.SpeakerSettingMenuItem item;
        for (JasperSpeakerSettingMenuAdapter adapter : adapters) {
            if (adapter == null) {
                continue;
            }
            // HPF
            item = adapter.findItem(SpeakerSettingMenuType.HPF);
            item.enabled = enabled;
            item.increaseEnabled = enabled;
            item.decreaseEnabled = enabled;
            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
            }

            item = adapter.findItem(SpeakerSettingMenuType.HPF_CUTOFF);
            item.enabled = enabled && hpfOn;
            item.increaseEnabled = enabled && hpfOn && (hpfCutoffSetting != null && hpfCutoffSetting.toggle(1) != null);
            item.decreaseEnabled = enabled && hpfOn && (hpfCutoffSetting != null && hpfCutoffSetting.toggle(-1) != null);

            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
            }

            item = adapter.findItem(SpeakerSettingMenuType.HPF_SLOPE);
            if (item != null) {
                item.enabled = enabled && hpfOn;
                item.increaseEnabled = enabled && hpfOn && (hpfSlopeSetting != null && hpfSlopeSetting.toggle(1) != null);
                item.decreaseEnabled = enabled && hpfOn && (hpfSlopeSetting != null && hpfSlopeSetting.toggle(-1) != null);
            } else {
                item = adapter.findItem2(SpeakerSettingMenuType.HPF_SLOPE);
                item.enabled2 = enabled && hpfOn;
                item.increaseEnabled2 = enabled && hpfOn && (hpfSlopeSetting != null && hpfSlopeSetting.toggle(1) != null);
                item.decreaseEnabled2 = enabled && hpfOn && (hpfSlopeSetting != null && hpfSlopeSetting.toggle(-1) != null);
            }
            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
            } else {
                adapter.notifyDataSetChanged();
            }

            // LPF
            boolean lpfEnabled = enabled && subwooferOn;
            if (!CORE_17LOW_CROSSOVER_NO_LPF_SW) {
                item = adapter.findItem(SpeakerSettingMenuType.LPF);
                if (item != null) {
                    item.enabled = lpfEnabled;
                    item.increaseEnabled = lpfEnabled;
                    item.decreaseEnabled = lpfEnabled;
                    if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                        writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
                    }
                }
            }

            boolean lpfDetailEnabled = lpfEnabled;
            if ((subwooferPhaseSetting != SubwooferPhaseSetting.NORMAL) && (subwooferPhaseSetting != SubwooferPhaseSetting.REVERSE)) {
                lpfDetailEnabled = false;
            }
            item = adapter.findItem(SpeakerSettingMenuType.LPF_CUTOFF);
            item.enabled = lpfDetailEnabled && lpfOn;
            item.increaseEnabled = lpfDetailEnabled && lpfOn && (lpfCutoffSetting != null && lpfCutoffSetting.toggle(1) != null);
            item.decreaseEnabled = lpfDetailEnabled && lpfOn && (lpfCutoffSetting != null && lpfCutoffSetting.toggle(-1) != null);
            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
            }

            item = adapter.findItem(SpeakerSettingMenuType.LPF_SLOPE);
            if (item != null) {
                item.enabled = lpfDetailEnabled && lpfOn;
                item.increaseEnabled = lpfDetailEnabled && lpfOn && (lpfSlopeSetting != null && lpfSlopeSetting.toggle(1) != null);
                item.decreaseEnabled = lpfDetailEnabled && lpfOn && (lpfSlopeSetting != null && lpfSlopeSetting.toggle(1) != null);
            } else {
                item = adapter.findItem2(SpeakerSettingMenuType.LPF_SLOPE);
                item.enabled2 = lpfDetailEnabled && lpfOn;
                item.increaseEnabled2 = lpfDetailEnabled && lpfOn && (lpfSlopeSetting != null && lpfSlopeSetting.toggle(1) != null);
                item.decreaseEnabled2 = lpfDetailEnabled && lpfOn && (lpfSlopeSetting != null && lpfSlopeSetting.toggle(-1) != null);
            }
            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
            } else {
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Crossover Settings 画面作成（mSwAdapter にMenu Items を作る）
     */
    private void openCrossoverSetting() {
        mSwSpeakerInfo.setSpeakerType(SpeakerInfoView.SPEAKER_TYPE_SW);

        setupSwSpeakerSettingAdapter();
    }


    /**
     * サブウーファーの設定画面のAdapterを設定
     */
    private void setupSwSpeakerSettingAdapter() {
        int orientation = AppConfigUtil.getCurrentOrientation(getContext());
        boolean isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE;

        JasperSpeakerSettingMenuAdapter adapter = getCrossoverMenuAdapter();
        SpeakerSettingMenuItem item;
//        String myPassedColor = "#646464";
//        int disabledColor = Color.parseColor(myPassedColor);

        if (!adapter.isEmpty()) {    // 重複追加を防ぐ
            adapter.clear();
        }

        // HPF on/off
        item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.HPF);
        adapter.add(item);

        // HPF Cutoff + Slope
        item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.HPF_CUTOFF);
        if (isLandscape) {
            item.type2 = SpeakerSettingMenuType.HPF_SLOPE;
        }
        adapter.add(item);

        if (!isLandscape) {
            // HFP Slope
            item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.HPF_SLOPE);
            adapter.add(item);
        }

        // Subwoofer on/off + Phase
        item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.SUBWOOFER);
        if (isLandscape) {
            item.type2 = SpeakerSettingMenuType.SUBWOOFER_PHASE;
        }
        adapter.add(item);

        if (!isLandscape) {
            // Subwoofer Phase
            item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.SUBWOOFER_PHASE);
            adapter.add(item);
        }

        // LPF Cutoff + Slope
        item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.LPF_CUTOFF);
        if (isLandscape) {
            item.type2 = SpeakerSettingMenuType.LPF_SLOPE;
        }
        adapter.add(item);

        if (!isLandscape) {
            // LFP Slope
            item = new SpeakerSettingMenuItem(SpeakerSettingMenuType.LPF_SLOPE);
            adapter.add(item);
        }

    }

    private void applySubwooferSetting(SubwooferSetting setting) {
        SpeakerSettingMenuItem item;
        item = getCrossoverMenuAdapter().findItem(SpeakerSettingMenuType.SUBWOOFER);
        item.currentValue = (setting == SubwooferSetting.ON) ? "ON" : "OFF";
        if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
            writeItemToConvertView(_mCrossoverMenuAdapter, _mCrossoverMenuAdapter.getPosition(item), item.type);
        } else {
            _mCrossoverMenuAdapter.notifyDataSetChanged();
        }
    }

    private void applySubwooferPhaseSetting(SubwooferPhaseSetting setting) {
        SpeakerSettingMenuItem item, item2;
        item = getCrossoverMenuAdapter().findItem(SpeakerSettingMenuType.SUBWOOFER_PHASE);
        item2 = getCrossoverMenuAdapter().findItem2(SpeakerSettingMenuType.SUBWOOFER_PHASE);

        String text;
        switch (setting) {
            case NORMAL:
                text = getString(R.string.set_148);
                break;
            case REVERSE:
                text = getString(R.string.set_186);
                break;
            default:
                text = "";
                break;
        }

        if (item != null) {
            item.currentValue = text;
            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(_mCrossoverMenuAdapter, _mCrossoverMenuAdapter.getPosition(item), item.type);
            }
        } else if (item2 != null) {
            item2.currentValue2 = text;
            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(_mCrossoverMenuAdapter, _mCrossoverMenuAdapter.getPosition(item2), item2.type);
            }
        }
        if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
            Timber.d("#applySubwooferPhaseSetting: isBusy or CORE_AUDIO_ADV_SPK_MENU_NOTIFY");
        } else {
            _mCrossoverMenuAdapter.notifyDataSetChanged();
        }
    }

    private JasperSpeakerSettingMenuAdapter[] adapters() {
        return new JasperSpeakerSettingMenuAdapter[]{getCrossoverMenuAdapter()};
    }

    private SpeakerInfoView[] speakerViews() {
        return new SpeakerInfoView[]{mSwSpeakerInfo};
    }

    private void applyPassFilterFrequency(CrossoverSetting setting) {
        applyPassFilterFrequencyEx(setting, null, HpfLpfFilterType.HPF);
    }

    /**
     * @param setting         - Remote StatusHolder
     * @param my_hpfLpfFilter - Local Echo Buffer 描画の時設定される
     * @param my_type         - Local Echo Buffer 描画の時設定される
     */
    private void applyPassFilterFrequencyEx(CrossoverSetting setting,
                                            CrossoverSetting.JasperCrossoverSetting my_hpfLpfFilter,
                                            HpfLpfFilterType my_type) {
        CrossoverSetting.JasperCrossoverSetting hpfLpfFilter;
        CutoffSetting cutoff;
        float freq;

        // HPF
        if (my_hpfLpfFilter == null) {
            hpfLpfFilter = setting.findJasperCrossoverSetting(HpfLpfFilterType.HPF);
        } else if (my_type == HpfLpfFilterType.HPF) {
            hpfLpfFilter = my_hpfLpfFilter;
        } else {
            hpfLpfFilter = null;
        }

        if (hpfLpfFilter != null) {
            cutoff = hpfLpfFilter.cutoffSetting;
            freq = (cutoff != null) ? cutoff.getFrequency() : 0;
            mSwSpeakerInfo.setHighPassFilterFrequency(freq);
            applyToAdapters(hpfLpfFilter, getCrossoverMenuAdapter());
        }

        // LPF
        if (my_hpfLpfFilter == null) {
            hpfLpfFilter = setting.findJasperCrossoverSetting(HpfLpfFilterType.LPF);
        } else if (my_type == HpfLpfFilterType.LPF) {
            hpfLpfFilter = my_hpfLpfFilter;
        } else {
            hpfLpfFilter = null;
        }

        if (hpfLpfFilter != null) {
            cutoff = hpfLpfFilter.cutoffSetting;
            freq = (cutoff != null) ? cutoff.getFrequency() : 0;
            mSwSpeakerInfo.setLowPassFilterFrequency(freq);
            applyToAdapters(setting.jasperLpf, getCrossoverMenuAdapter());
        }

        StatusHolder holder = mPresenter.getStatusHolder();
        AudioSetting audioSetting = holder.getAudioSetting();
        updateFilterGraph(audioSetting, my_hpfLpfFilter, my_type, null);
    }

    /**
     * HPF | LPH の値を画面に設定する
     *
     * @param setting
     * @param adapters
     */
    private void applyToAdapters(CrossoverSetting.JasperCrossoverSetting setting, JasperSpeakerSettingMenuAdapter... adapters) {
        boolean hpf = (setting.hpfLpfFilterType == HpfLpfFilterType.HPF);
        boolean on = (setting.hpfLpfSetting == HpfLpfSetting.ON || setting.hpfLpfSetting == HpfLpfSetting.ON_FIXED);
        SpeakerSettingMenuItem item, item2;
        float freq = setting.cutoffSetting != null ? setting.cutoffSetting.getFrequency() : 0;
        int level = setting.slopeSetting != null ? setting.slopeSetting.getLevel() : 0;

        for (JasperSpeakerSettingMenuAdapter adapter : adapters) {
            item = adapter.findItem(hpf ? SpeakerSettingMenuType.HPF : SpeakerSettingMenuType.LPF);
            if (item != null) {
                item.currentValue = on ? "ON" : "OFF";
                if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                    writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
                }
            }

            item = adapter.findItem(hpf ? SpeakerSettingMenuType.HPF_CUTOFF : SpeakerSettingMenuType.LPF_CUTOFF);
            item.currentValue = NumberFormatUtil.formatFrequency(freq, true);
            if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
            }

            item = adapter.findItem(hpf ? SpeakerSettingMenuType.HPF_SLOPE : SpeakerSettingMenuType.LPF_SLOPE);
            if (item != null) {
                item.currentValue = String.format(Locale.ENGLISH, LEVEL_FORMAT, level);
                if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) {
                    writeItemToConvertView(adapter, adapter.getPosition(item), item.type);
                }
            } else {
                item2 = adapter.findItem2(hpf ? SpeakerSettingMenuType.HPF_SLOPE : SpeakerSettingMenuType.LPF_SLOPE);
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

    private void applyAppearance() {
        if (mSwSpeakerInfo == null) return; // Viewが設定されてないとみなす

        for (JasperSpeakerSettingMenuAdapter adapter : adapters()) {
            int count = adapter.getCount();
            for (int pos = 0; pos < count; pos++) {
                SpeakerSettingMenuItem item = adapter.getItem(pos);
                if (isBusy() || (!CORE_AUDIO_ADV_SPK_MENU_NOTIFY)) { // 不要？
                    writeItemToConvertView(_mCrossoverMenuAdapter, adapter.getPosition(item), item.type);
                } else {
                    _mCrossoverMenuAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @OnClick({R.id.swSpeakerInfo})
    void onSpeakerInfoClicked(SpeakerInfoView v) {
        showSettingMenu();
    }

    private void showSettingMenu() {
        int type = JasperSpeakerSettingMenuView.SPEAKER_TYPE_SW;
        getCrossoverMenuAdapter().notifyDataSetChanged();
        mSpeakerSettingMenu.setSpeakerType(type);
        mSpeakerSettingMenu.setAdapter(getCrossoverMenuAdapter());
    }

    @OnClick(R.id.graphViewContainer)
    void onGraphClicked() {
    }


    public boolean onBackPressed() {
//        boolean handled = false;
        return false;
    }

   
	/*
     * 周波数グラフの仮実装
	 * 仕様通りにするにはいろいろと調整が必要
	 */


    @BindView(R.id.filterGraphView)
    FilterGraphView mFilterGraphView;

    @Nullable
    @BindView(R.id.frequencies)
    FilterFrequencyView mFrequencies;

    private JasperFilterGraphViewController mFilterGraphController;

    private void setupFilterGraph() {
        FilterGraphSpec graphSpec = FilterDesignDefaults.defaultFilterGraphSpec();
        FilterGraphGeometry graphGeometry = new FilterGraphGeometry(graphSpec);
        FilterPathBuilder pathBuilder = new FilterPathBuilder(graphGeometry);

        if (mFrequencies != null)
            mFrequencies.setFrequencyStrings(graphGeometry);

        mFilterGraphView.getBackgroundView().setGridPath(FilterDesignDefaults.defaultPathBuilderGridPathSpec(), graphGeometry);
        mFilterGraphController = new JasperFilterGraphViewController(getContext(), pathBuilder, graphGeometry, mFilterGraphView);
    }

    private void redrawFilterGraph(boolean animated) {
        if (animated)
            mFilterGraphController.update();
        else
            mFilterGraphController.update(0);
    }

    /**
     * 17LOW: FL == FR { == RL == RR 省略 }
     *
     * @param setting         - Remote StatusHolder
     * @param my_hpfLpfFilter - Local Echo Buffer 描画の時設定される
     * @param my_type         - Local Echo Buffer 描画の時設定される
     */
    private void updateFilterGraph(AudioSetting setting,
                                   CrossoverSetting.JasperCrossoverSetting my_hpfLpfFilter,
                                   HpfLpfFilterType my_type,
                                   SubwooferSetting my_subwoofer) {
        SubwooferSetting subwooferSetting = (my_subwoofer != null) ? my_subwoofer : setting.subwooferSetting;
        mFilterGraphController.setSubwooferOn(subwooferSetting == SubwooferSetting.ON);

        CrossoverSetting xover = setting.crossoverSetting;

        JasperFilterGraphViewController.Crossover graphCrossover = null;
        if (my_hpfLpfFilter == null) {
            graphCrossover = translateCrossover(xover.jasperHpf);
        } else if (my_type == HpfLpfFilterType.HPF) {
            graphCrossover = translateCrossover(my_hpfLpfFilter);
        }
        if (graphCrossover != null) {
            mFilterGraphController.setFilter(
                    JasperFilterGraphViewController.Speaker.FrontRear,
                    getFrSpeakerLevel(),
                    graphCrossover,
                    null,
                    isBusy());    // 長押し中のローカル値を用いた表示は強制更新しないと表示されない
        }

        graphCrossover = null;
        if (my_hpfLpfFilter == null) {
            graphCrossover = translateCrossover(xover.jasperLpf);
        } else if (my_type == HpfLpfFilterType.LPF) {
            graphCrossover = translateCrossover(my_hpfLpfFilter);
        }
        if (graphCrossover != null) {
            mFilterGraphController.setFilter(
                    JasperFilterGraphViewController.Speaker.Subwoofer,
                    getSwSpeakerLevel(),
                    null,
                    graphCrossover,
                    isBusy());    // 長押し中のローカル値を用いた表示は強制更新しないと表示されない
        }
    }

    @Nullable
    private JasperFilterGraphViewController.Crossover translateCrossover(@Nullable CrossoverSetting.JasperCrossoverSetting src) {
        if (src == null) {
            return null;
        }

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

        long frequency = (long) (src.cutoffSetting.getFrequency() * 1000);
        JasperSlopeSetting slopeSetting = src.slopeSetting;
        int slopeLevel = 0;
        if (slopeSetting != JasperSlopeSetting._12DB) {
            slopeLevel = src.slopeSetting.getLevel();
        }

//        JasperFilterGraphViewController.Crossover crossover = new JasperFilterGraphViewController.Crossover(on, frequency, slopeLevel);
        return new JasperFilterGraphViewController.Crossover(on, frequency, slopeLevel);
    }

    private int getSwSpeakerLevel() {
        AudioSetting setting = mPresenter.getStatusHolder().getAudioSetting();
        SpeakerLevelSetting speakerLevelSetting = setting.speakerLevelSetting;
        return speakerLevelSetting.subwooferLevel;
    }

    private int getFrSpeakerLevel() {
        AudioSetting setting = mPresenter.getStatusHolder().getAudioSetting();
        SpeakerLevelSetting speakerLevelSetting = setting.speakerLevelSetting;

        /*
         *  TODO IOS は minimumLevel 使っているそうです, そっちにあわせる
         *  (16High は frontLeftHighLeftLevel)
         */
        return speakerLevelSetting.maximumLevel;
    }
}
