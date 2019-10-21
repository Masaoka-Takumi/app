package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.BtPhoneColor;
import jp.pioneer.carsync.presentation.presenter.IncomingCallPatternPresenter;
import jp.pioneer.carsync.presentation.view.IncomingCallPatternView;
import jp.pioneer.carsync.presentation.view.adapter.IncomingCallSettingAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * Incoming Call Color設定画面.
 */
public class IncomingCallPatternFragment extends AbstractScreenFragment<IncomingCallPatternPresenter, IncomingCallPatternView>
        implements IncomingCallPatternView {

    @Inject IncomingCallPatternPresenter mPresenter;
    @BindView(R.id.list_view) ListView mListView;
    private IncomingCallSettingAdapter mAdapter;
    private Unbinder mUnbinder;

    private static final SparseArrayCompat<BtPhoneColor> BT_PHONE_COLORS = new SparseArrayCompat<BtPhoneColor>(){{
        put(0,BtPhoneColor.FLASHING);
        put(1,BtPhoneColor.FLASHING_PATTERN1);
        put(2,BtPhoneColor.FLASHING_PATTERN2);
        put(3,BtPhoneColor.FLASHING_PATTERN3);
        put(4,BtPhoneColor.FLASHING_PATTERN4);
        put(5,BtPhoneColor.FLASHING_PATTERN5);
        put(6,BtPhoneColor.FLASHING_PATTERN6);
        put(7,BtPhoneColor.OFF);
    }};

    /**
     * コンストラクタ.
     */
    public IncomingCallPatternFragment(){

    }

    /**
     * 新規インスタンス取得.
     *
     * @param args 引き継ぎ情報
     * @return SystemFragment
     */
    public static IncomingCallPatternFragment newInstance(Bundle args) {
        IncomingCallPatternFragment fragment = new IncomingCallPatternFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    protected IncomingCallPatternPresenter getPresenter() {
        return mPresenter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_phone_incoming_call_color, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAdapter(ArrayList<String> types) {
        mAdapter = new IncomingCallSettingAdapter(getContext(), types);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener((parent, view, position, id) ->
                mPresenter.onSelectedItemAction(BT_PHONE_COLORS.get(position)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectedItem(BtPhoneColor selected) {
        mAdapter.setSelectedIndex(BT_PHONE_COLORS.indexOfValue(selected));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScreenId getScreenId() {
        return ScreenId.INCOMING_CALL_PATTERN_SETTING;
    }
}
