package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.presentation.model.CustomKeyItem;
import jp.pioneer.carsync.presentation.model.YouTubeLinkSearchItem;
import jp.pioneer.carsync.presentation.presenter.VoiceRecognizeTypeSelectDialogPresenter;
import jp.pioneer.carsync.presentation.view.VoiceRecognizeTypeSelectDialogView;
import jp.pioneer.carsync.presentation.view.adapter.VoiceRecognizeTypeSelectAdapter;
import jp.pioneer.carsync.presentation.view.adapter.YouTubeLinkSearchItemAdapter;

public class VoiceRecognizeTypeSelectDialogFragment extends AbstractDialogFragment<VoiceRecognizeTypeSelectDialogPresenter, VoiceRecognizeTypeSelectDialogView, AbstractDialogFragment.Callback>
        implements VoiceRecognizeTypeSelectDialogView {

    @Inject
    VoiceRecognizeTypeSelectDialogPresenter mPresenter;
    @BindView(R.id.close_button)
    ImageView mCloseBtn;
    @BindView(R.id.list_view)
    ListView mListView;
    @BindView(R.id.custom_key_setting_title)
    TextView titleText;
    @BindView(R.id.setting_bar)
    LinearLayout titleBar;
    private VoiceRecognizeTypeSelectAdapter mAdapter;

    // デフォルトコンストラクタ
    public VoiceRecognizeTypeSelectDialogFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param target コールバック通知先
     * @param  args  引き継ぎ情報
     * @return VoiceRecognizeTypeSelectDialogFragment
     */
    public static VoiceRecognizeTypeSelectDialogFragment newInstance(Fragment target, Bundle args){
        VoiceRecognizeTypeSelectDialogFragment fragment = new VoiceRecognizeTypeSelectDialogFragment();
        fragment.setTargetFragment(target, 0); // 呼び出し元のFragmentに結果を通知するためのもの
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        Dialog dialog = new Dialog(getContext(), R.style.CustomKeySettingBehindScreenStyle);
        setCancelable(true);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_dialog_youtube_link_search_item, container, false);
        ButterKnife.bind(this, view);
        titleText.setText(R.string.set_323);
        titleBar.setBackground(null);
        mAdapter = new VoiceRecognizeTypeSelectAdapter(getContext());
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getPresenter().onSelectItem(mAdapter.getItem(position));
                setSelectedItem(position);
            }
        });
        return view;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent){
        fragmentComponent.inject(this);
    }

    @Override
    protected boolean isInstanceOfCallback(Object callback){
        return callback instanceof Callback;
    }

    @NonNull
    @Override
    protected VoiceRecognizeTypeSelectDialogPresenter getPresenter(){
        return mPresenter;
    }

    /**
     * 選択中のカスタムキー設定position
     *
     * @param selectedItem リスト項目の位置
     */
    @Override
    public void setSelectedItem(int selectedItem){
        mAdapter.setSelectedIndex(selectedItem);
    }

    /**
     * AdapterをListViewにセット、ListViewのリスナセット
     *
     * @param listItem 表示するカスタムキー項目のリスト
     */
    @Override
    public void setAdapter(ArrayList<VoiceRecognizeType> listItem){
        mAdapter.clear();
        mAdapter.addAll(listItem);
    }

    /**
     * ダイアログ終了メソッド
     */
    @Override
    public void callbackClose(){
        if (getCallback() != null) {
            getCallback().onClose(this);
        }
        this.dismiss();
    }

    /**
     * キャンセルボタンを押したときの動作(ダイアログを閉じる)
     */
    @OnClick(R.id.close_button)
    public void onClickDismissBtn(){
        callbackClose();
    }

}
