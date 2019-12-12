package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.content.TunerContract;
import jp.pioneer.carsync.presentation.presenter.DabEnsembleListPresenter;
import jp.pioneer.carsync.presentation.view.DabEnsembleListView;
import jp.pioneer.carsync.presentation.view.adapter.ServiceListAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

public class DabEnsembleListFragment extends AbstractScreenFragment<DabEnsembleListPresenter, DabEnsembleListView>
        implements DabEnsembleListView {
    @Inject DabEnsembleListPresenter mPresenter;
    @BindView(R.id.list_view) ListView mListView;
    private ServiceListAdapter mServiceListAdapter;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ
     */
    public DabEnsembleListFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return DabEnsembleListFragment
     */
    public static DabEnsembleListFragment newInstance(Bundle args) {
        DabEnsembleListFragment fragment = new DabEnsembleListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_music, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mServiceListAdapter = new ServiceListAdapter(getContext(), null, false);
        mListView.setVisibility(View.VISIBLE);
        mListView.setAdapter(mServiceListAdapter);
        mListView.setFastScrollEnabled(true);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.DAB_ENSEMBLE_LIST;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected DabEnsembleListPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setCursor(Cursor cursor) {
        if (mServiceListAdapter != null) {
            mServiceListAdapter.swapCursor(cursor);
            mServiceListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setSelectedPositionNotScroll(int position) {
        mListView.setItemChecked(position, true);
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
        Cursor cursor = (Cursor) mServiceListAdapter.getItem(position);
        int selectIndex = TunerContract.ListItemContract.Dab.getListIndex(cursor);
        getPresenter().onSelectList(selectIndex, cursor);
    }

}
