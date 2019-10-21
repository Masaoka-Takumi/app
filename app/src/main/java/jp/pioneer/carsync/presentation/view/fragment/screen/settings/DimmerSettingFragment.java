package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.DimmerSetting;
import jp.pioneer.carsync.domain.model.DimmerTimeType;
import jp.pioneer.carsync.presentation.model.DimmerListType;
import jp.pioneer.carsync.presentation.presenter.DimmerSettingPresenter;
import jp.pioneer.carsync.presentation.view.DimmerSettingView;
import jp.pioneer.carsync.presentation.view.adapter.DimmerSettingAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.widget.DimmerTimePicker;

/**
 * ディマー設定画面
 */

public class DimmerSettingFragment extends AbstractScreenFragment<DimmerSettingPresenter, DimmerSettingView>
        implements DimmerSettingView {
    @Inject DimmerSettingPresenter mPresenter;
    @BindView(R.id.list_view) ExpandableListView mListView;
    @BindView(R.id.disable_layer) View mDisableLayer;
    @BindView(R.id.time_picker_group) ScrollView mTimePickerGroup;
    @BindView(R.id.time_picker_start) DimmerTimePicker mTimePickerStart;
    @BindView(R.id.time_picker_stop) DimmerTimePicker mTimePickerStop;
    @BindView(R.id.cancel_button) TextView mCancel;
    @BindView(R.id.ok_button) TextView mOk;
    private Unbinder mUnbinder;
    private DimmerSettingAdapter mAdapter;

    /**
     * コンストラクタ
     */
    public DimmerSettingFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return DimmerSettingFragment
     */
    public static DimmerSettingFragment newInstance(Bundle args) {
        DimmerSettingFragment fragment = new DimmerSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_dimmer, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        getPresenter().setArgument(getArguments());
        mListView.setDivider(null);
        mListView.setGroupIndicator(null);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.ILLUMINATION_DIMMER_SETTING;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected DimmerSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setPage(int page) {
        if(page == 0){
            mListView.setVisibility(View.VISIBLE);
            mTimePickerGroup.setVisibility(View.GONE);
        }else if(page == 1){
            mListView.setVisibility(View.GONE);
            mTimePickerGroup.setVisibility(View.VISIBLE);
        }
    }

    /**
     * アダプター設定
     *
     * @param types タイプリスト
     */
    @Override
    public void setAdapter(ArrayList<DimmerListType> types) {

        mAdapter = new DimmerSettingAdapter(getContext(), types);
        mAdapter.setTimeFormatSetting(getPresenter().getTimeFormatSetting());
        mListView.setAdapter(mAdapter);
        mListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            DimmerListType type = (DimmerListType) mAdapter.getGroup(groupPosition);
            if(type == DimmerListType.SYNC_CLOCK_START||type == DimmerListType.SYNC_CLOCK_STOP){
                if(mAdapter.getSelectedDimmer()!=DimmerSetting.Dimmer.SYNC_CLOCK){
                    return false;
                }
            }
            getPresenter().onSelectDimmerAction(type);
            if(type == DimmerListType.SYNC_CLOCK){
                getPresenter().onSelectDimmerTimeAction(DimmerTimeType.START_TIME, mTimePickerStart.getHour(), mTimePickerStart.getMinute());
                getPresenter().onSelectDimmerTimeAction(DimmerTimeType.END_TIME, mTimePickerStop.getHour(), mTimePickerStop.getMinute());
            }
            return false;
        });
    }

    @Override
    public void setSelectedItem(DimmerSetting.Dimmer selected) {
        mAdapter.setSelectedIndex(selected);
    }

    @Override
    public void setDimmerSchedule(int startHour, int startMinute, int endHour, int endMinute) {
        mAdapter.setDimmerTime(startHour, startMinute, endHour, endMinute);
        mTimePickerStart.set(startHour, startMinute);
        mTimePickerStop.set(endHour, endMinute);
    }


    @Override
    public void setEnable(boolean isEnabled) {
        mListView.setEnabled(isEnabled);
        mAdapter.setEnabled(isEnabled);
        if(mTimePickerGroup.getVisibility()==View.VISIBLE) {
            if (isEnabled) {
                mDisableLayer.setVisibility(View.GONE);
                mDisableLayer.setOnTouchListener(null);
            } else {
                mDisableLayer.setVisibility(View.VISIBLE);
                mDisableLayer.setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            //some code....
                            break;
                        case MotionEvent.ACTION_UP:
                            v.performClick();
                            break;
                        default:
                            break;
                    }
                    return true;
                });
            }
        }
    }

    @OnClick(R.id.cancel_button)
    public void onClickCancelButton() {
        getPresenter().onBackAction();
    }

    @OnClick(R.id.ok_button)
    public void onClickOkButton() {
        getPresenter().onSelectDimmerTimeAction(DimmerTimeType.START_TIME, mTimePickerStart.getHour(), mTimePickerStart.getMinute());
        getPresenter().onSelectDimmerTimeAction(DimmerTimeType.END_TIME, mTimePickerStop.getHour(), mTimePickerStop.getMinute());
        getPresenter().onBackAction();
    }
}
