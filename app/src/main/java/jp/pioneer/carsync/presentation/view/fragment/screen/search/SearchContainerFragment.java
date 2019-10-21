package jp.pioneer.carsync.presentation.view.fragment.screen.search;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.controller.SearchFragmentController;
import jp.pioneer.carsync.presentation.presenter.SearchContainerPresenter;
import jp.pioneer.carsync.presentation.view.SearchContainerView;
import jp.pioneer.carsync.presentation.view.fragment.OnGoBackListener;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AbstractDialogFragment;

/**
 * 音声認識検索画面のコンテナ
 */

public class SearchContainerFragment extends AbstractDialogFragment<SearchContainerPresenter, SearchContainerView, AbstractDialogFragment.Callback>
        implements SearchContainerView, OnGoBackListener {
    @Inject SearchContainerPresenter mPresenter;
    @Inject SearchFragmentController mFragmentController;
    @BindView(R.id.speech_icon) ImageView mSpeechIcon;
    @BindView(R.id.directory_pass_text) TextView mPath1;
    @BindView(R.id.directory_pass_text2) TextView mPath2;
    @BindView(R.id.close_button) ImageView mCloseBtn;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ
     */
    @Inject
    public SearchContainerFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return SearchContainerFragment
     */
    public static SearchContainerFragment newInstance(Bundle args) {
        SearchContainerFragment fragment = new SearchContainerFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.BehindScreenStyle);
        setCancelable(false);
        dialog.setOnKeyListener((dialog1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                getPresenter().onBackAction();
                return true;
            }
            return false;
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_container_search, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mFragmentController.setContainerViewId(R.id.container);
        getPresenter().setArgument(getArguments());
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getCallback() != null) {
            getCallback().onClose(this);
        }
        mUnbinder.unbind();
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
    protected SearchContainerPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean onGoBack() {
        if (mFragmentController.goBack()) {
            getPresenter().removeTitle();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigate(ScreenId screenId, Bundle args) {
        if (mFragmentController.navigate(screenId, args)) {
            getPresenter().setTitle(args);
            return true;
        }
        return false;
    }

    @Override
    public void setTitle(String title, boolean isResult) {
        String paragrph = getString(R.string.ply_082);
        if(title.contains(paragrph)){
            mPath1.setVisibility(View.VISIBLE);
            mPath2.setVisibility(View.VISIBLE);
            mPath1.setText(title.substring(0,title.indexOf(paragrph)));
            mPath2.setText(title.substring(title.indexOf(paragrph),title.length()));
        }else{
            mPath1.setVisibility(View.GONE);
            mPath2.setText(title);
        }
        if(isResult){
            mSpeechIcon.setVisibility(View.VISIBLE);
        }else{
            mSpeechIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClose() {
        dismiss();
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
        dismiss();
    }
}
