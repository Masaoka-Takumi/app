package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.AdasBillingPresenter;
import jp.pioneer.carsync.presentation.view.AdasBillingView;
import jp.pioneer.carsync.presentation.view.activity.MainActivity;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

/**
 * AdasBillingFragment
 */

public class AdasBillingFragment extends AbstractScreenFragment<AdasBillingPresenter, AdasBillingView>
        implements AdasBillingView, StatusPopupDialogFragment.Callback {
    @Inject AdasBillingPresenter mPresenter;
    @BindView(R.id.purchase_button) RelativeLayout mPurchaseBtn;
    @BindView(R.id.purchase_btn_text) TextView mPurchaseBtnText;
    @BindView(R.id.restore_button) TextView mRestoreBtn;
    @BindView(R.id.video_link_button) TextView mVideoLink;
    @BindView(R.id.trial_button) RelativeLayout mtrialBtn;
    @BindView(R.id.trial_button_text) TextView mtrialBtnText;
    private Unbinder mUnbinder;
    private MainActivity mActivity;
    public AdasBillingFragment() {
    }

    public static AdasBillingFragment newInstance(Bundle args) {
        AdasBillingFragment fragment = new AdasBillingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adas_billing, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        String str = getString(R.string.set_291);
        SpannableString spanStr = new SpannableString(str);
        spanStr.setSpan(new UnderlineSpan(), 0, str.length(), 0);
        mRestoreBtn.setText(spanStr);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mActivity!=null){
            mActivity.closeBillingHelper();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mActivity.setupBillingHelperNotQuery();
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.ADAS_BILLING;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected AdasBillingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setPurchaseBtn(boolean isPurchased) {
        if(isPurchased){
            mPurchaseBtn.setEnabled(false);
            mPurchaseBtn.setAlpha(0.5f);
        }else{
            mPurchaseBtn.setEnabled(true);
            mPurchaseBtn.setAlpha(1.0f);
        }
    }

    @Override
    public void setPriceText(String price) {
        mPurchaseBtnText.setText(price);
    }

    @Override
    public void setTrialButtonEnabled(boolean isEnabled) {
        if(isEnabled){
            mtrialBtn.setEnabled(true);
            mtrialBtn.setAlpha(1.0f);
        }else{
            mtrialBtn.setEnabled(false);
            mtrialBtn.setAlpha(0.5f);
        }
    }

    @Override
    public void setTrialButtonText(int src) {
        mtrialBtnText.setText(src);
    }

    /**
     * VideoLink押下イベント
     */
    @OnClick(R.id.video_link_button)
    public void onClickVideoLink() {
        Uri uri = Uri.parse("https://jpn.pioneer/ja/support/pcperipherals/app/pioneer_smart_sync/driving_support_eye/");
        Intent i = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(i);
        //getPresenter().onVideoLinkAction();
    }

    /**
     * Restore押下イベント
     */
    @OnClick(R.id.restore_button)
    public void onClickRestore() {
        if(mActivity.isBillingSetupFinished()) {
            getPresenter().onRestoreAction();
            mActivity.queryInventoryAsyncRestore();
        }
    }

    /**
     * Buy押下イベント
     */
    @OnClick(R.id.trial_button)
    public void onClickTrialBtn() {
        getPresenter().onTrialAction();
    }

    /**
     * Buy押下イベント
     */
    @OnClick(R.id.purchase_button)
    public void onClickPurchaseBtn() {
        if(getPresenter().getAdasPriceText().equals(getString(R.string.set_377))||getPresenter().getAdasPriceText().equals("")){
            mActivity.setupBillingHelperNotQuery();
            return;
        }
        if(mActivity.isBillingSetupFinished()) {
            mActivity.purchaseLauncher();
        }
        getPresenter().onPurchaseAction();
    }

    @Override
    public void onClose(StatusPopupDialogFragment fragment, String tag) {

    }

    @Override
    public void onPositiveClick(StatusPopupDialogFragment fragment, String tag) {
        if(tag.equals(AdasBillingPresenter.TAG_ADAS_TRIAL_CONFIRM)){
            getPresenter().onTrialStart();
        }else if(tag.equals(AdasBillingPresenter.TAG_ADAS_CONFIGURATION_RESET)){
            getPresenter().onConfigAction();
        }
    }

    @Override
    public void onNegativeClick(StatusPopupDialogFragment fragment, String tag) {
        if(tag.equals(AdasBillingPresenter.TAG_ADAS_TRIAL_CONFIRM)){

        }else if(tag.equals(AdasBillingPresenter.TAG_ADAS_CONFIGURATION_RESET)){
            getPresenter().goCarSafety();
        }
    }
}
