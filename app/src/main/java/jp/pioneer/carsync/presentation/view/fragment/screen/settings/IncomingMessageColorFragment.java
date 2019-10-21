package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import jp.pioneer.carsync.presentation.presenter.IncomingMessageColorPresenter;
import jp.pioneer.carsync.presentation.view.IncomingMessageColorView;
import jp.pioneer.carsync.presentation.view.adapter.IncomingCallSettingAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * Created by NSW00_008320 on 2018/03/14.
 */

public class IncomingMessageColorFragment extends AbstractScreenFragment<IncomingMessageColorPresenter, IncomingMessageColorView>
        implements IncomingMessageColorView {

    @Inject IncomingMessageColorPresenter mPresenter;
    @BindView(R.id.list_view) ListView mListView;
    private IncomingCallSettingAdapter mAdapter;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ.
     */
    public IncomingMessageColorFragment(){
    }

    /**
     * 新規インスタンス取得.
     *
     * @param args 引き継ぎ情報
     * @return SystemFragment
     */
    public static IncomingMessageColorFragment newInstance(Bundle args) {
        IncomingMessageColorFragment fragment = new IncomingMessageColorFragment();
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
    protected IncomingMessageColorPresenter getPresenter() {
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
                mPresenter.onSelectedItemAction(position));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectedItem(int position) {
        mAdapter.setSelectedIndex(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScreenId getScreenId() {
        return ScreenId.INCOMING_CALL_PATTERN_SETTING;
    }
}
