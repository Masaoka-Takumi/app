package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.BtDeviceSearchPresenter;
import jp.pioneer.carsync.presentation.view.BtDeviceSearchView;
import jp.pioneer.carsync.presentation.view.adapter.BtDeviceSearchAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * BTデバイス検索画面
 */

public class BtDeviceSearchFragment extends AbstractScreenFragment<BtDeviceSearchPresenter, BtDeviceSearchView>
        implements BtDeviceSearchView {
    private static final String TAG_DIALOG_PAIRING = "pairing";
    @Inject BtDeviceSearchPresenter mPresenter;
    @BindView(R.id.list_view) ListView mListView;
    @BindView(R.id.search_button) RelativeLayout mSearchButton;
    @BindView(R.id.stop_button) RelativeLayout mStopButton;
    @BindView(R.id.search_status_icon) ImageView mSearchIcon;
    @BindView(R.id.disable_layer) View mDisableLayer;
    private BtDeviceSearchAdapter mAdapter;
    private Unbinder mUnbinder;
    public BtDeviceSearchFragment() {
    }

    public static BtDeviceSearchFragment newInstance(Bundle args) {
        BtDeviceSearchFragment fragment = new BtDeviceSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_bt_device_search, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mAdapter = new BtDeviceSearchAdapter(getContext(), null);
        mListView.setAdapter(mAdapter);
        AnimationDrawable frameAnimation = (AnimationDrawable) mSearchIcon.getBackground();
        // アニメーションの開始
        frameAnimation.start();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getPresenter().setLoaderManager(getLoaderManager());
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.BT_DEVICE_SEARCH;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected BtDeviceSearchPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setDeviceCursor(Cursor data, Bundle args) {
        mAdapter.swapCursor(data, args);
    }

    @OnItemClick(R.id.list_view)
    public void onClickListItem(AdapterView<?> parent, View view, int position, long id) {
        ListView listview = (ListView) parent;
        Cursor cursor = (Cursor) listview.getItemAtPosition(position);
        getPresenter().onSelectDevice(cursor);
    }

    @OnClick(R.id.search_button)
    public void onClickSearchButton() {
        getPresenter().onStartSearch();
    }

    @OnClick(R.id.stop_button)
    public void onClickStopButton() {
        getPresenter().onStopSearch();
    }

    @Override
    public void setEnable(boolean isEnabled, boolean isSearching) {
        mSearchButton.setEnabled(isEnabled);
        mStopButton.setEnabled(isEnabled);

        if (isEnabled) {
            if (isSearching) {
                mSearchIcon.setVisibility(View.VISIBLE);
                mSearchButton.setEnabled(false);
                mSearchButton.setAlpha(0.5f);
            } else {
                mSearchIcon.setVisibility(View.INVISIBLE);
                mSearchButton.setEnabled(true);
                mSearchButton.setAlpha(1.0f);
            }
            mStopButton.setAlpha(1.0f);
        } else {
            mSearchButton.setAlpha(0.5f);
            mStopButton.setAlpha(0.5f);
        }
        mListView.setEnabled(isEnabled);
        mAdapter.setEnabled(isEnabled);
    }

    @Override
    public void dismissPairingDialog() {
        if (isResumed() && getParentFragment() != null) ((SettingsContainerFragment)getParentFragment()).dismissStatusPopup();
    }

    @Override
    public void showToast(String str) {
        Toast.makeText(getActivity(), str, Toast.LENGTH_LONG).show();
    }
}

