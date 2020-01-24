package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.model.YouTubeLinkSearchItem;
import jp.pioneer.carsync.presentation.presenter.YouTubeLinkSearchItemDialogPresenter;
import jp.pioneer.carsync.presentation.view.YouTubeLinkSearchItemDialogView;
import jp.pioneer.carsync.presentation.view.adapter.YouTubeLinkSearchItemAdapter;

public class YouTubeLinkSearchItemDialogFragment extends AbstractDialogFragment<YouTubeLinkSearchItemDialogPresenter, YouTubeLinkSearchItemDialogView, AbstractDialogFragment.Callback>
        implements YouTubeLinkSearchItemDialogView {

    @Inject
    YouTubeLinkSearchItemDialogPresenter mPresenter;
    @BindView(R.id.close_button)
    ImageView mCloseBtn;
    @BindView(R.id.list_view)
    ListView mListView;
    @BindView(R.id.custom_key_setting_title)
    TextView titleText;
    @BindView(R.id.setting_bar)
    LinearLayout titleBar;
    private YouTubeLinkSearchItemAdapter mAdapter;

    // デフォルトコンストラクタ
    public YouTubeLinkSearchItemDialogFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param target コールバック通知先
     * @param args   引き継ぎ情報
     * @return YouTubeLinkSearchItemDialogFragment
     */
    public static YouTubeLinkSearchItemDialogFragment newInstance(Fragment target, Bundle args) {
        YouTubeLinkSearchItemDialogFragment fragment = new YouTubeLinkSearchItemDialogFragment();
        fragment.setTargetFragment(target, 0); // 呼び出し元のFragmentに結果を通知するためのもの
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getContext(), R.style.CustomKeySettingBehindScreenStyle);
        setCancelable(true);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_youtube_link_search_item, container, false);
        ButterKnife.bind(this, view);
        titleText.setText(R.string.set_397);
        titleBar.setBackground(null);
        mAdapter = new YouTubeLinkSearchItemAdapter(getContext());
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox chk = view.findViewById(R.id.checkBox1);
                chk.setChecked(!chk.isChecked());
                mPresenter.onItemChecked();
            }
        });
        //2項目の高さで固定
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)getContext().getResources().getDimension(R.dimen.setting_list_item_height)*2);
        mListView.setLayoutParams(params);
        return view;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof Callback;
    }

    @NonNull
    @Override
    protected YouTubeLinkSearchItemDialogPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setListItem(ArrayList<YouTubeLinkSearchItem> listItem) {
        mAdapter.clear();
        mAdapter.addAll(listItem);
    }

    @Override
    public void setCheckedItemPositions(SparseBooleanArray positions) {
        for (int i = 0; i < positions.size(); i++) {
            if (positions.valueAt(i)) {
                mListView.setItemChecked(positions.keyAt(i), true);
            }
        }
        mAdapter.setCheckedPositions(positions);
    }

    @Override
    public SparseBooleanArray getCheckedItemPositions() {
        return mListView.getCheckedItemPositions();
    }

    /**
     * ダイアログ終了メソッド
     */
    @Override
    public void callbackClose() {
        if (getCallback() != null) {
            getCallback().onClose(this);
        }
        this.dismiss();
    }

    /**
     * キャンセルボタンを押したときの動作(ダイアログを閉じる)
     */
    @OnClick(R.id.close_button)
    public void onClickDismissBtn() {
        callbackClose();
    }

}
