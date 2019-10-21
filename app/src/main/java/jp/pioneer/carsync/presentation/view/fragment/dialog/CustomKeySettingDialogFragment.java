package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.model.CustomKeyItem;
import jp.pioneer.carsync.presentation.presenter.CustomKeySettingDialogPresenter;
import jp.pioneer.carsync.presentation.view.CustomKeySettingDialogView;
import jp.pioneer.carsync.presentation.view.adapter.CustomKeyAdapter;
import timber.log.Timber;

/**
 * カスタムキー割当画面(Caution画面参考)
 */
public class CustomKeySettingDialogFragment
        extends AbstractDialogFragment<CustomKeySettingDialogPresenter, CustomKeySettingDialogView, AbstractDialogFragment.Callback>
        implements CustomKeySettingDialogView {

    @Inject CustomKeySettingDialogPresenter mPresenter;
    @BindView(R.id.close_button) ImageView mCloseBtn;
    @BindView(R.id.list_view) ListView mListView;
    private CustomKeyAdapter mAdapter;

    // デフォルトコンストラクタ
    public CustomKeySettingDialogFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param target コールバック通知先
     * @param  args  引き継ぎ情報
     * @return CustomKeySettingDialogFragment
     */
    public static CustomKeySettingDialogFragment newInstance(Fragment target, Bundle args){
        CustomKeySettingDialogFragment fragment = new CustomKeySettingDialogFragment();
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
        View view = inflater.inflate(R.layout.fragment_dialog_customkeysetting, container, false);
        ButterKnife.bind(this, view);
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
    protected CustomKeySettingDialogPresenter getPresenter(){
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
     * @param customKeyItemArrayList 表示するカスタムキー項目のリスト
     */
    @Override
    public void setAdapter(ArrayList<CustomKeyItem> customKeyItemArrayList){
        mAdapter = new CustomKeyAdapter(getContext(), customKeyItemArrayList); // ArrayListも渡す
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mListView.setAdapter(mAdapter); // ListViewにAdapterを設定
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPresenter.saveCustomKeyToPreference(mAdapter.getItem(position)); // カスタムキー設定値をPreferenceに保存
                setSelectedItem(position); // カスタムキーの設定値をAdapterにセット
            }
        });
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
