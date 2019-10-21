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
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.presentation.presenter.RadioFavoritePresenter;
import jp.pioneer.carsync.presentation.view.RadioFavoriteView;
import jp.pioneer.carsync.presentation.view.adapter.RadioFavoriteAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * ラジオお気に入りリスト画面
 */

public class RadioFavoriteFragment extends AbstractScreenFragment<RadioFavoritePresenter, RadioFavoriteView>
        implements RadioFavoriteView {

    @Inject RadioFavoritePresenter mPresenter;
    @BindView(R.id.list_view) ListView mListView;
    private RadioFavoriteAdapter mAdapter;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ
     */
    public RadioFavoriteFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return RadioFavoriteFragment
     */
    public static RadioFavoriteFragment newInstance(Bundle args) {
        RadioFavoriteFragment fragment = new RadioFavoriteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_music, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mAdapter = new RadioFavoriteAdapter(getContext(), null, getPresenter().getSourceType());
        mAdapter.setSeekStep(getPresenter().getTunerSeekStep());
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getPresenter().setLoaderManager(getLoaderManager());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.RADIO_FAVORITE_LIST;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected RadioFavoritePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setCursor(Cursor data, Bundle args) {
        mAdapter.swapCursor(data, args);
    }

    /**
     * リスト項目押下イベント
     *
     * @param parent   親View
     * @param view     View
     * @param position 押下位置
     * @param id       カーソルID
     */
    @OnItemClick(R.id.list_view)
    public void onClickListItem(AdapterView<?> parent, View view, int position, long id) {
        ListView listview = (ListView) parent;
        Cursor cursor = (Cursor) listview.getItemAtPosition(position);
        //登録されたお気に入りリストがAM Bandかつ、現在のAM Step(9kHz/10kHz)設定と異なる周波数が登録されている場合操作無効
        if(getPresenter().getSourceType()== MediaSourceType.RADIO) {
            if (TunerContract.FavoriteContract.Radio.getBandType(cursor).isAMVariant()) {
                if (TunerContract.FavoriteContract.Radio.getTunerSeekStep(cursor) != mAdapter.getSeekStep()) {
                    return;
                }
            }
        }
        getPresenter().onSelectFavoriteAction(cursor);
    }
}
