package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.content.UsbListContract;
import jp.pioneer.carsync.presentation.presenter.UsbListPresenter;
import jp.pioneer.carsync.presentation.view.UsbListView;
import jp.pioneer.carsync.presentation.view.adapter.UsbListAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * USBリストの画面
 */

public class UsbListFragment extends AbstractScreenFragment<UsbListPresenter, UsbListView>
        implements UsbListView {
    @Inject UsbListPresenter mPresenter;
    @BindView(R.id.directory_pass_text) TextView mTitle;
    @BindView(R.id.list_view) ListView mListView;
    @BindView(R.id.back_button) ImageView mBackBtn;
    @BindView(R.id.close_button) ImageView mCloseBtn;
    private UsbListAdapter mAdapter;
    private Unbinder mUnbinder;
    private boolean mIsFirst = true;
    private int mTotalCount = 0;

    /**
     * コンストラクタ
     */
    public UsbListFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return UsbListFragment
     */
    public static UsbListFragment newInstance(Bundle args) {
        UsbListFragment fragment = new UsbListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_usb_list, container, false);

        view.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                getPresenter().onBackAction();
                return true;
            }
            return false;
        });
        view.setFocusableInTouchMode(true);
        view.requestFocus();

        mUnbinder = ButterKnife.bind(this, view);
        mTitle.setText(R.string.ply_081);
        mCloseBtn.setVisibility(View.VISIBLE);
        mAdapter = new UsbListAdapter(getContext(), null, false){
            @Override
            public void onWantedItem(int index) {
                mPresenter.onAddListItem(index);
            }
        };
        mAdapter.setSphCarDevice(getPresenter().isSphCarDevice());
        mListView.setAdapter(mAdapter);
        mListView.setFastScrollEnabled(true);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getPresenter().setLoaderManager(LoaderManager.getInstance(this));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.USB_LIST;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected UsbListPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setCursor(Cursor cursor) {
        mAdapter.swapCursor(cursor);
        mAdapter.notifyDataSetChanged();
        mIsFirst = false;
    }

    @Override
    public void setBackButtonVisible(boolean visible) {
        if(visible) {
            mBackBtn.setVisibility(View.VISIBLE);
        }else{
            mBackBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void setFirst(boolean first) {
        mIsFirst = first;
        mTotalCount = 0;
    }

    @Override
    public void updateCursor() {
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 項目リスト選択
     *
     * @param parent   AdapterView
     * @param view     View
     * @param position int
     * @param id       選択ID
     */
    @OnItemClick(R.id.list_view)
    public void onClickListItem(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        int listIndex = UsbListContract.getListIndex(cursor);
        getPresenter().onSelectList(listIndex, cursor);
    }

    @Override
    public void closeDialog() {
        if (getParentFragment() != null) ((UsbListContainerFragment) getParentFragment()).dismiss();
    }

    /**
     * 戻るボタン押下イベント
     */
    @OnClick(R.id.back_button)
    public void onClickBackButton() {
        getPresenter().onBackAction();
    }

    /**
     * 閉じるボタン押下イベント
     */
    @OnClick(R.id.close_button)
    public void onClickCloseButton() {
        mPresenter.onCloseAction();
    }

    @Override
    public void setTitle(String title) {
        mTitle.setText(title);
    }

    @Override
    public void setSelectedPosition(int position) {
        mListView.setItemChecked(position, true);
        if (mListView.getFirstVisiblePosition() > position
                || mListView.getLastVisiblePosition() <= position) {
            mListView.smoothScrollToPosition(position);
        }
    }

    @Override
    public void setSelectedPositionNotScroll(int position) {
        mListView.setItemChecked(position, true);
    }

    @Override
    public int getSelectPosition(){
        return mListView.getCheckedItemPosition();
    }

    @Override
    public Cursor getItem(int position){
        return (Cursor) mAdapter.getItem(position);
    }

}
