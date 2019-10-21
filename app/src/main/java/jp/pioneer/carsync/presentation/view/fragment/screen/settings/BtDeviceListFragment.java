package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.BtDeviceListPresenter;
import jp.pioneer.carsync.presentation.view.BtDeviceListView;
import jp.pioneer.carsync.presentation.view.adapter.BtDeviceAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * BTデバイスリスト画面.
 */
public class BtDeviceListFragment extends AbstractScreenFragment<BtDeviceListPresenter, BtDeviceListView>
        implements BtDeviceListView, StatusPopupDialogFragment.Callback {
    private static final String TAG_DIALOG_DELETE = "delete";
    @Inject BtDeviceListPresenter mPresenter;
    @BindView(R.id.list_view) ListView mListView;
    @BindView(R.id.add_button) RelativeLayout mAddButton;
    @BindView(R.id.delete_button) RelativeLayout mDeleteButton;
    @BindView(R.id.disable_layer) View mDisableLayer;
    private BtDeviceAdapter mAdapter;
    private Unbinder mUnbinder;

    public BtDeviceListFragment() {
    }

    public static BtDeviceListFragment newInstance(Bundle args) {
        BtDeviceListFragment fragment = new BtDeviceListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_bt_device, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mAdapter = new BtDeviceAdapter(getContext(), null) {
            @Override
            protected void onClickPhoneButton(Cursor cursor) {
                getPresenter().onConnectHfpDevice(cursor);
            }

            @Override
            protected void onClickDeleteButton(Cursor cursor) {
                getPresenter().OnShowDeleteDialog(TAG_DIALOG_DELETE);
                mListView.setItemChecked(cursor.getPosition(), true);
            }
        };
        mListView.setAdapter(mAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
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
        return ScreenId.BT_DEVICE_LIST;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected BtDeviceListPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setDeviceCursor(Cursor data, Bundle args) {
        mAdapter.swapCursor(data, args);
    }

    @Override
    public void setAddButtonEnabled(boolean isEnabled) {
        if(!mAdapter.isDeleteMode()) {
            mAddButton.setEnabled(isEnabled);
            if (isEnabled) {
                mAddButton.setAlpha(1.0f);
            } else {
                mAddButton.setAlpha(0.5f);
            }
        }
    }

    @Override
    public void setDeleteButtonEnabled(boolean isEnabled) {
        if(!mAdapter.isDeleteMode()) {
            mDeleteButton.setEnabled(isEnabled);
            if (isEnabled) {
                mDeleteButton.setAlpha(1.0f);
            } else {
                mDeleteButton.setAlpha(0.5f);
            }
        }
    }

    @OnItemClick(R.id.list_view)
    public void onClickListItem(AdapterView<?> parent, View view, int position, long id) {
        mListView.setItemChecked(position, true);
        getPresenter().onSelectDevice();
    }

    @OnClick(R.id.add_button)
    public void onClickAddButton() {
        getPresenter().onClickSearchButton();
    }

    @OnClick(R.id.delete_button)
    public void onClickDeleteButton() {
        if (mAdapter.isDeleteMode()) {
            mAdapter.setDeleteMode(false);
            setAddButtonEnabled(true);
            TextView deleteText = (TextView) mDeleteButton.getChildAt(1);
            deleteText.setText(R.string.set_053);
        } else {
            setAddButtonEnabled(false);
            mAdapter.setDeleteMode(true);
            TextView deleteText = (TextView) mDeleteButton.getChildAt(1);
            deleteText.setText(R.string.set_251);
        }
    }

    @Override
    public void setEnable(boolean isEnabled) {
        mListView.setEnabled(isEnabled);
        mAdapter.setEnabled(isEnabled);
    }

    @Override
    public void updateListView() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClose(StatusPopupDialogFragment fragment, String tag) {

    }

    @Override
    public void onPositiveClick(StatusPopupDialogFragment fragment, String tag) {
        if(tag.equals(TAG_DIALOG_DELETE)) {
            getPresenter().onDeleteDevice((Cursor) mAdapter.getItem(mListView.getCheckedItemPosition()));
        }
    }

    @Override
    public void onNegativeClick(StatusPopupDialogFragment fragment, String tag) {
    }
}
