package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.PairingDeviceInfo;
import jp.pioneer.carsync.presentation.presenter.ClassicBtDeviceListPresenter;
import jp.pioneer.carsync.presentation.view.ClassicBtDeviceListView;
import jp.pioneer.carsync.presentation.view.adapter.PairingDeviceListAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * ペアリングリストの画面
 */

public class ClassicBtDeviceListFragment extends AbstractScreenFragment<ClassicBtDeviceListPresenter, ClassicBtDeviceListView>
        implements ClassicBtDeviceListView {
    @Inject ClassicBtDeviceListPresenter mPresenter;
    @BindView(R.id.get_button) Button mGetButton;
    @BindView(R.id.list_view) ListView mListView;
    private PairingDeviceListAdapter mAdapter;
    private Unbinder mUnbinder;

    public ClassicBtDeviceListFragment() {
    }

    public static ClassicBtDeviceListFragment newInstance(Bundle args) {
        ClassicBtDeviceListFragment fragment = new ClassicBtDeviceListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pairking_device_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        getPresenter().setArgument(getArguments());
        mAdapter = new PairingDeviceListAdapter(getContext());
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.PAIRING_DEVICE_LIST;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected ClassicBtDeviceListPresenter getPresenter() {
        return mPresenter;
    }

    /**
     * アダプター設定
     *
     * @param types タイプリスト
     */
    @Override
    public void setAdapter(ArrayList<PairingDeviceInfo> types) {
        getActivity().runOnUiThread(() -> {
            mAdapter.setTypeArray(types);
            mAdapter.notifyDataSetChanged();
        });

    }

    @OnClick(R.id.get_button)
    public void onClickGetButton() {
        getPresenter().onGetListAction();
    }
}
