package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.DabPtyListPresenter;
import jp.pioneer.carsync.presentation.view.DabPtyListView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

public class DabPtyListFragment extends AbstractScreenFragment<DabPtyListPresenter, DabPtyListView>
        implements DabPtyListView {
    @Inject DabPtyListPresenter mPresenter;
    @BindView(R.id.list_view) ListView mListView;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ
     */
    public DabPtyListFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return DabPtyListFragment
     */
    public static DabPtyListFragment newInstance(Bundle args) {
        DabPtyListFragment fragment = new DabPtyListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_music, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        Context context = getContext();
        if (context != null) {
            String[] items = {context.getString(R.string.ply_103), context.getString(R.string.ply_104), context.getString(R.string.ply_105), context.getString(R.string.ply_106)};
            ArrayAdapter<String> itemsAdapter =
                    new ArrayAdapter<>(context, R.layout.element_list_item_usb, R.id.title_text, items);
            mListView.setAdapter(itemsAdapter);
        }
        mListView.setFastScrollEnabled(true);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.DAB_PTY_LIST;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected DabPtyListPresenter getPresenter() {
        return mPresenter;
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
        getPresenter().onSelectList(position);
    }

}
