package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.app.Dialog;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.presentation.controller.RadioTabFragmentController;
import jp.pioneer.carsync.presentation.presenter.RadioPresenter;
import jp.pioneer.carsync.presentation.presenter.RadioTabContainerPresenter;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;
import jp.pioneer.carsync.presentation.view.RadioTabContainerView;
import jp.pioneer.carsync.presentation.view.fragment.OnGoBackListener;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AbstractDialogFragment;

/**
 * ラジオリスト画面コンテナ
 */

public class RadioTabContainerFragment extends AbstractDialogFragment<RadioTabContainerPresenter, RadioTabContainerView, AbstractDialogFragment.Callback>
        implements RadioTabContainerView, OnGoBackListener {
    @Inject RadioTabContainerPresenter mPresenter;
    @Inject RadioTabFragmentController mFragmentController;

    @BindView(R.id.path_text) TextView mTitle;
    @BindView(R.id.bsm_button) RelativeLayout mBsm;
    @BindView(R.id.tab_layout) LinearLayout mTabLayout;
    @BindView(R.id.tab_layout_dab) LinearLayout mTabLayoutDab;
    @BindView(R.id.separator) View mSeparater;
    @BindView(R.id.update_button) RelativeLayout mUpdateBtn;
    @BindView(R.id.preset_button) RelativeLayout mPresetTab;
    @BindView(R.id.favorite_button) RelativeLayout mFavoriteTab;
    @BindView(R.id.tab_station) RelativeLayout mStationTab;
    @BindView(R.id.tab_pty) RelativeLayout mPtyTab;
    @BindView(R.id.tab_ensemble) RelativeLayout mEnsembleTab;
    @BindView(R.id.tab_preset) RelativeLayout mDabPresetTab;
    @BindView(R.id.status_view_back) View mStatusViewBack;
    @BindView(R.id.status_view) LinearLayout mStatusView;
    @BindView(R.id.bsm_icon) ImageView mBsmIcon;
    @BindView(R.id.title_text) TextView mTitleText;
    @BindView(R.id.back_button) ImageView mBackButton;
    @BindView(R.id.close_button) ImageView mCloseButton;
    @BindView(R.id.dialog_close_button) ImageView mDialogCloseButton;
    private Unbinder mUnbinder;

    /**
     * コンストラクタ
     */
    public RadioTabContainerFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return RadioTabContainerFragment
     */
    public static RadioTabContainerFragment newInstance(Bundle args) {
        RadioTabContainerFragment fragment = new RadioTabContainerFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_container_radio_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mFragmentController.setContainerViewId(R.id.player_list_container);
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
    protected RadioTabContainerPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setTabLayout(MediaSourceType type,boolean isSph) {
        if(type == MediaSourceType.RADIO&&isSph){
            mTabLayout.setVisibility(View.GONE);
            mTabLayoutDab.setVisibility(View.GONE);
            mSeparater.setVisibility(View.GONE);
            return;
        }else{
            mSeparater.setVisibility(View.VISIBLE);
        }
        mTabLayout.removeAllViews();
        if (type == MediaSourceType.RADIO) {
            mTabLayout.addView(mFavoriteTab);
            mTabLayout.addView(mPresetTab);
        }else{
            mTabLayout.addView(mPresetTab);
            mTabLayout.addView(mFavoriteTab);
        }
        if(type == MediaSourceType.DAB&&isSph){
            mTabLayout.setVisibility(View.GONE);
            mTabLayoutDab.setVisibility(View.VISIBLE);
        }else{
            mTabLayout.setVisibility(View.VISIBLE);
            mTabLayoutDab.setVisibility(View.GONE);
        }
    }

    @Override
    public void setTitle(String title) {
        mTitle.setText(title);
    }

    /**
     * BSMボタン表示の設定
     *
     * @param isVisible
     */
    @Override
    public void setBsmButtonVisible(boolean isVisible) {
        if(isVisible) {
            mBsm.setVisibility(View.VISIBLE);
        }else{
            mBsm.setVisibility(View.GONE);
        }
    }
    /**
     * ×ボタン表示の設定
     *
     * @param isVisible
     */
    @Override
    public void setCloseButtonVisible(boolean isVisible) {
        if(isVisible) {
            mCloseButton.setVisibility(View.VISIBLE);
        }else{
            // BSM ボタンの位置はキープしたいのでINVISIBLE
            mCloseButton.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    public void setBsmButtonEnabled(boolean isEnabled) {
        mBsm.setEnabled(isEnabled);
        if(isEnabled) {
            mBsm.setAlpha(1.0f);
        }else{
            mBsm.setAlpha(0.35f);
        }
    }
    /**
     * Updateボタン表示の設定
     *
     * @param isVisible
     */
    @Override
    public void setUpdateButtonVisible(boolean isVisible) {
        if(isVisible) {
            mUpdateBtn.setVisibility(View.VISIBLE);
        }else{
            mUpdateBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public void setUpdateButtonEnabled(boolean isEnabled) {
        mUpdateBtn.setEnabled(isEnabled);
        if(isEnabled) {
            mUpdateBtn.setAlpha(1.0f);
        }else{
            mUpdateBtn.setAlpha(0.35f);
        }
    }

    /**
     * タブボタン表示の設定
     *
     * @param tab 現在のタブ
     */
    @Override
    public void setTab(RadioTabContainerPresenter.RadioTabType tab) {
        mPresetTab.getChildAt(0).setVisibility(View.INVISIBLE);
        mFavoriteTab.getChildAt(0).setVisibility(View.INVISIBLE);
        mPresetTab.getChildAt(1).setAlpha(0.35f);
        mFavoriteTab.getChildAt(1).setAlpha(0.35f);
        mPresetTab.setEnabled(true);
        mFavoriteTab.setEnabled(true);
        mStationTab.getChildAt(0).setVisibility(View.INVISIBLE);
        mPtyTab.getChildAt(0).setVisibility(View.INVISIBLE);
        mEnsembleTab.getChildAt(0).setVisibility(View.INVISIBLE);
        mDabPresetTab.getChildAt(0).setVisibility(View.INVISIBLE);
        mStationTab.getChildAt(1).setAlpha(0.35f);
        mPtyTab.getChildAt(1).setAlpha(0.35f);
        mEnsembleTab.getChildAt(1).setAlpha(0.35f);
        mDabPresetTab.getChildAt(1).setAlpha(0.35f);
        mStationTab.setEnabled(true);
        mPtyTab.setEnabled(true);
        mEnsembleTab.setEnabled(true);
        mDabPresetTab.setEnabled(true);
        switch (tab){
            case PRESET:
                mPresetTab.getChildAt(0).setVisibility(View.VISIBLE);
                mPresetTab.getChildAt(1).setAlpha(1.0f);
                mPresetTab.setEnabled(false);
                break;
            case FAVORITE:
                mFavoriteTab.getChildAt(0).setVisibility(View.VISIBLE);
                mFavoriteTab.getChildAt(1).setAlpha(1.0f);
                mFavoriteTab.setEnabled(false);
                break;
            case DAB_STATION:
                mStationTab.getChildAt(0).setVisibility(View.VISIBLE);
                mStationTab.getChildAt(1).setAlpha(1.0f);
                mStationTab.setEnabled(false);
                break;
            case DAB_PTY:
                mPtyTab.getChildAt(0).setVisibility(View.VISIBLE);
                mPtyTab.getChildAt(1).setAlpha(1.0f);
                mPtyTab.setEnabled(false);
                break;
            case DAB_ENSEMBLE:
                mEnsembleTab.getChildAt(0).setVisibility(View.VISIBLE);
                mEnsembleTab.getChildAt(1).setAlpha(1.0f);
                mEnsembleTab.setEnabled(false);
                break;
            case DAB_PRESET:
                mDabPresetTab.getChildAt(0).setVisibility(View.VISIBLE);
                mDabPresetTab.getChildAt(1).setAlpha(1.0f);
                mDabPresetTab.setEnabled(false);
                break;
            default:
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
        ImageView presetSelect = (ImageView) mPresetTab.getChildAt(0);
        presetSelect.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0301_pchbtn_select_1nrm, color));
        ImageView favoriteSelect = (ImageView) mFavoriteTab.getChildAt(0);
        favoriteSelect.setImageDrawable(ImageViewUtil.setTintColor(getContext(), R.drawable.p0017_favoritebtn_select_1nrm, color));
    }

    @Override
    public boolean onNavigate(ScreenId screenId, Bundle args) {
        return mFragmentController.navigate(screenId, args);
    }

    @Override
    public boolean onGoBack() {
        return mFragmentController.goBack();
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
    @OnClick( R.id.close_button)
    public void onClickCloseButton() {
        getPresenter().onCloseAction();
    }

    /**
     * お気に入りタブ押下イベント
     */
    @OnClick(R.id.favorite_button)
    public void onClickFavoriteButton() {
        getPresenter().onFavoriteAction();
    }

    /**
     * プリセットタブ押下イベント
     */
    @OnClick(R.id.preset_button)
    public void onClickPresetButton() {
        getPresenter().onPresetAction();
    }

    /**
     * Stationタブ押下イベント
     */
    @OnClick(R.id.tab_station)
    public void onClickStationTab() {
        getPresenter().onDabStationAction();
    }

    /**
     * PTYタブ押下イベント
     */
    @OnClick(R.id.tab_pty)
    public void onClickPtyTab() {
        getPresenter().onDabPtyAction();
    }

    /**
     * Ensembleタブ押下イベント
     */
    @OnClick(R.id.tab_ensemble)
    public void onClickEnsembleTab() {
        getPresenter().onDabEnsembleAction();
    }

    /**
     * DAB Presetタブ押下イベント
     */
    @OnClick(R.id.tab_preset)
    public void onClickDabPresetTab() {
        getPresenter().onDabPresetAction();
    }

    /**
     * BSMハンドラ
     */
    @OnClick(R.id.bsm_button)
    public void onClickBsmButton() {
        getPresenter().onBsmAction();
    }

    /**
     * Updateハンドラ
     */
    @OnClick(R.id.update_button)
    public void onClickUpdateButton() {
        getPresenter().onUpdateAction();
    }

    public void showStatusView(boolean isShow, String type){
        if(isShow) {
            mStatusViewBack.setVisibility(View.VISIBLE);
            mStatusView.setVisibility(View.VISIBLE);
            mStatusViewBack.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            //some code....
                            break;
                        case MotionEvent.ACTION_UP:
                            v.performClick();
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
            int text;
            int animationResource;
            switch (type) {
                case RadioPresenter.TAG_BSM:
                    text = R.string.ply_058;
                    animationResource = R.drawable.animation_bsm;
                    mDialogCloseButton.setVisibility(View.VISIBLE);
                    break;
                case RadioPresenter.TAG_PTY_SEARCH:
                    text = R.string.ply_041;
                    animationResource = R.drawable.animation_bsm;
                    mDialogCloseButton.setVisibility(View.GONE);
                    break;
                default:
                    text = R.string.ply_058;
                    animationResource = R.drawable.animation_bsm;
                    mDialogCloseButton.setVisibility(View.VISIBLE);
                    break;
            }
            mTitleText.setText(text);
            mBsmIcon.setImageResource(animationResource);
            AnimationDrawable frameAnimation = (AnimationDrawable) mBsmIcon.getDrawable();
            // アニメーションの開始
            frameAnimation.start();
        }else{
            mStatusViewBack.setVisibility(View.GONE);
            mStatusView.setVisibility(View.GONE);
            mStatusViewBack.setOnTouchListener(null);
        }
    }

    /**
     * BSMの閉じるボタン押下イベント
     *
     * @param view ビュー
     */
    @OnClick(R.id.dialog_close_button)
    public void onClickBsmCloseButton(View view) {
        getPresenter().onStatusCloseAction();
    }

    @Override
    public void closeDialog(){
        this.dismiss();
    }
}
