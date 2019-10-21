package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.ListeningPosition;
import jp.pioneer.carsync.domain.model.SmallCarTaSetting;
import jp.pioneer.carsync.domain.model.SmallCarTaSettingType;
import jp.pioneer.carsync.presentation.presenter.SmallCarTaSettingPresenter;
import jp.pioneer.carsync.presentation.view.SmallCarTaSettingView;
import jp.pioneer.carsync.presentation.view.adapter.SmallCarTaSettingAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * SmallCarTa設定の画面
 */

public class SmallCarTaSettingFragment extends AbstractScreenFragment<SmallCarTaSettingPresenter, SmallCarTaSettingView> implements SmallCarTaSettingView{
    @Inject SmallCarTaSettingPresenter mPresenter;

    @BindView(R.id.list_view) ListView mListView;
    @BindView(R.id.preset_view) ImageView mPresetView;
    @BindView(R.id.front_l) RadioButton mLeftRadio;
    @BindView(R.id.front_r) RadioButton mRightRadio;
    @BindView(R.id.radioGroup) RadioGroup mRadioGroup;
    private Unbinder mUnbinder;
    private SmallCarTaSettingAdapter mAdapter;
    private boolean mIsEnabled = true;
    private boolean mIsSeatTypeSettingEnabled = true;
    private static final SmallCarTaSettingType[] SMALL_CAR_TA_SETTING_TYPES = new SmallCarTaSettingType[]{
            SmallCarTaSettingType.OFF,
            SmallCarTaSettingType.COMPACT,
            SmallCarTaSettingType.STANDARD,
            SmallCarTaSettingType.INTERMEDIATE,
            SmallCarTaSettingType.SUV_PREMIUM,
    };
    private static final ListeningPosition[] SMALL_CAR_TA_SEAT_POSITIONS = new ListeningPosition[]{
            ListeningPosition.LEFT,
            ListeningPosition.RIGHT,
    };
    public SmallCarTaSettingFragment() {
    }

    public static SmallCarTaSettingFragment newInstance(Bundle args) {
        SmallCarTaSettingFragment fragment = new SmallCarTaSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_smallcarta, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mLeftRadio.getBackground().setAlpha(76);
        mRightRadio.getBackground().setAlpha(76);
        mLeftRadio.setOnClickListener(v -> {
            SmallCarTaSetting setting = mPresenter.getFxSetting().smallCarTaSetting;
            mPresenter.onSelectSmallCarTaSettingAction(setting.smallCarTaSettingType, SMALL_CAR_TA_SEAT_POSITIONS[0]);
        });
        mRightRadio.setOnClickListener(v -> {
            SmallCarTaSetting setting = mPresenter.getFxSetting().smallCarTaSetting;
            mPresenter.onSelectSmallCarTaSettingAction(setting.smallCarTaSettingType, SMALL_CAR_TA_SEAT_POSITIONS[1]);
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected SmallCarTaSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId(){ return ScreenId.SMALL_CAR_TA_SETTING; }

    /**
     * アダプター設定
     * @param types タイプリスト
     */
    @Override
    public void setAdapter(ArrayList<SmallCarTaSettingType> types) {
        mAdapter = new SmallCarTaSettingAdapter(getContext(), types);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            SmallCarTaSetting setting = mPresenter.getFxSetting().smallCarTaSetting;
            mPresenter.onSelectSmallCarTaSettingAction(SMALL_CAR_TA_SETTING_TYPES[position], setting.listeningPosition);
        });
    }

    /**
     * 選択中轟設定
     * @param selected 選択中インデックス
     */
    @Override
    public void setSelectedItem(int selected) {
        mAdapter.setSelectedIndex(selected);
    }

    /**
     * Presetイメージ設定
     *
     * @param resId リソース
     */
    @Override
    public void setPresetView(int resId) {
        if(resId==0){
            mPresetView.setImageDrawable(null);
        }
        mPresetView.setImageResource(resId);
    }

    @Override
    public void setSeatTypeSettingEnabled(boolean isEnabled) {
        mIsSeatTypeSettingEnabled = isEnabled;
        if(mIsEnabled) {
            mLeftRadio.setEnabled(isEnabled);
            mRightRadio.setEnabled(isEnabled);
            if (isEnabled) {
                mRadioGroup.setAlpha(1.0f);
            } else {
                mRadioGroup.setAlpha(0.5f);
            }
        }
    }

    /**
     * シートタイプの設定
     * @param position シートのポジション
     */
    @Override
    public void setSeatType(ListeningPosition position){

        switch (position) {
            case LEFT:
                mLeftRadio.setChecked(true);
                mRightRadio.setChecked(false);
                break;
            case RIGHT:
                mLeftRadio.setChecked(false);
                mRightRadio.setChecked(true);
                break;
            default:
                throw new AssertionError("This case is impossible.");
        }
    }

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    @Override
    public void setColor(@ColorRes int color) {
        mAdapter.setColor(color);
    }

    @Override
    public void setEnable(boolean isEnabled) {
        mIsEnabled = isEnabled;
        mListView.setEnabled(isEnabled);
        mAdapter.setEnabled(isEnabled);
        if(mIsSeatTypeSettingEnabled) {
            mLeftRadio.setEnabled(isEnabled);
            mRightRadio.setEnabled(isEnabled);
            if (isEnabled) {
                mLeftRadio.setAlpha(1.0f);
                mRightRadio.setAlpha(1.0f);
            } else {
                mLeftRadio.setAlpha(0.5f);
                mRightRadio.setAlpha(0.5f);
            }
        }
    }
}
