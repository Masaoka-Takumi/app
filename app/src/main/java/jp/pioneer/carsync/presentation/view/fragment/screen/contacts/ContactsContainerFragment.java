package jp.pioneer.carsync.presentation.view.fragment.screen.contacts;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.controller.ContactsFragmentController;
import jp.pioneer.carsync.presentation.presenter.ContactsContainerPresenter;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;
import jp.pioneer.carsync.presentation.view.ContactsContainerView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AbstractDialogFragment;
import timber.log.Timber;

/**
 * 電話帳 コンテナの画面
 */
public class ContactsContainerFragment extends AbstractDialogFragment<ContactsContainerPresenter, ContactsContainerView, AbstractDialogFragment.Callback>
        implements ContactsContainerView {

    @Inject ContactsContainerPresenter mPresenter;
    @Inject ContactsFragmentController mFragmentController;
    @BindView(R.id.directory_pass_text) TextView mTextView;
    @BindView(R.id.tab_history) RelativeLayout mTabHistory;
    @BindView(R.id.tab_contacts) RelativeLayout mTabContacts;
    @BindView(R.id.tab_favorites) RelativeLayout mTabFavorites;
    @BindView(R.id.close_button) ImageView mCloseButton;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ
     */
    @Inject
    public ContactsContainerFragment() {

    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return ContactsContainerFragment
     */
    public static ContactsContainerFragment newInstance(Bundle args) {
        ContactsContainerFragment fragment = new ContactsContainerFragment();
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
        View view = inflater.inflate(R.layout.fragment_container_contacts, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mFragmentController.setContainerViewId(R.id.container);
        mCloseButton.setVisibility(View.VISIBLE);
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
    protected ContactsContainerPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean onNavigate(ScreenId screenId, Bundle args) {
        return mFragmentController.navigate(screenId, args);
    }

    /**
     * 現在の選択タブ設定
     *
     * @param tab 現在の選択タブ
     */
    @Override
    public void setCurrentTab(ContactsContainerPresenter.ContactsTab tab) {

        switch (tab) {
            case HISTORY:
                mTextView.setText(getString(R.string.pho_002));
                mTabFavorites.getChildAt(0).setVisibility(View.INVISIBLE);
                mTabFavorites.getChildAt(1).setAlpha(0.4f);
                mTabContacts.getChildAt(0).setVisibility(View.INVISIBLE);
                mTabContacts.getChildAt(1).setAlpha(0.4f);
                mTabHistory.getChildAt(0).setVisibility(View.VISIBLE);
                mTabHistory.getChildAt(1).setAlpha(1.0f);
                break;
            case FAVORITE:
                mTextView.setText(getString(R.string.pho_003));
                mTabFavorites.getChildAt(0).setVisibility(View.VISIBLE);
                mTabFavorites.getChildAt(1).setAlpha(1.0f);
                mTabContacts.getChildAt(0).setVisibility(View.INVISIBLE);
                mTabContacts.getChildAt(1).setAlpha(0.4f);
                mTabHistory.getChildAt(0).setVisibility(View.INVISIBLE);
                mTabHistory.getChildAt(1).setAlpha(0.4f);
                break;
            case CONTACTS:
                mTextView.setText(getString(R.string.pho_004));
                mTabFavorites.getChildAt(0).setVisibility(View.INVISIBLE);
                mTabFavorites.getChildAt(1).setAlpha(0.4f);
                mTabContacts.getChildAt(0).setVisibility(View.VISIBLE);
                mTabContacts.getChildAt(1).setAlpha(1.0f);
                mTabHistory.getChildAt(0).setVisibility(View.INVISIBLE);
                mTabHistory.getChildAt(1).setAlpha(0.4f);
                break;
            default:
                Timber.w("This case is impossible.");
                break;
        }
    }

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    @Override
    public void setColor(@ColorRes int color) {
        ImageView favoritesSelect = (ImageView)mTabFavorites.getChildAt(0);
        favoritesSelect.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0017_favoritebtn_select_1nrm, color));
        ImageView contactsSelect = (ImageView)mTabContacts.getChildAt(0);
        contactsSelect.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0018_phonebookbtn_select_1nrm, color));
        ImageView historySelect = (ImageView)mTabHistory.getChildAt(0);
        historySelect.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0019_historybtn_select_1nrm, color));

    }

    @Override
    public void callbackClose() {
        if (getCallback() != null) {
            getCallback().onClose(this);
        }
        this.dismiss();
    }

    /**
     * Historyタブ押下
     */
    @OnClick(R.id.tab_history)
    public void onClickHistoryTab(View view) {
        getPresenter().onTabAction(ContactsContainerPresenter.ContactsTab.HISTORY);
    }

    /**
     * Contactsタブ押下
     */
    @OnClick(R.id.tab_contacts)
    public void onClickContactsTab(View view) {
        getPresenter().onTabAction(ContactsContainerPresenter.ContactsTab.CONTACTS);
    }

    /**
     * Favoritesタブ押下
     */
    @OnClick(R.id.tab_favorites)
    public void onClickFavoritesTab(View view) {
        getPresenter().onTabAction(ContactsContainerPresenter.ContactsTab.FAVORITE);
    }

    /**
     * 戻るボタン押下
     */
    @OnClick({R.id.back_button, R.id.close_button})
    public void onClickBackButton(View view) {
        getPresenter().onBackAction();
    }

}
