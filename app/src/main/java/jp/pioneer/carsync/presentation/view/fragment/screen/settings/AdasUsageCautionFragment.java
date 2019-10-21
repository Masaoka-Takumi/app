package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.AdasUsageCautionPresenter;
import jp.pioneer.carsync.presentation.view.AdasUsageCautionView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.StatusPopupDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.widget.CustomScrollView;

public class AdasUsageCautionFragment extends AbstractScreenFragment<AdasUsageCautionPresenter, AdasUsageCautionView> implements AdasUsageCautionView, CustomScrollView.ScrollToBottomListener, StatusPopupDialogFragment.Callback  {

    @Inject AdasUsageCautionPresenter mPresenter;
    private Unbinder mUnbinder;
    @BindView(R.id.confirm) RelativeLayout mConfirm;
    @BindView(R.id.confirm_button) TextView mConfirmBtn;
    @BindView(R.id.scroll_view) CustomScrollView mScrollView;
    @BindView(R.id.caution_text_1_1) TextView mText1_1;
    @BindView(R.id.caution_text_1_2) TextView mText1_2;
    @BindView(R.id.caution_text_2) TextView mText2;
    /**
     * コンストラクタ
     */
    public AdasUsageCautionFragment() {
    }

    /**
     * 新規インスタンス取得
     * @param args 引き継ぎ情報
     * @return AdasUsageCautionFragment
     */
    public static AdasUsageCautionFragment newInstance(Bundle args) {
        AdasUsageCautionFragment fragment = new AdasUsageCautionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adas_usage_caution, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        getPresenter().setArgument(getArguments());
        mConfirmBtn.setEnabled(false);
        mScrollView.setScrollToBottomListener(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected AdasUsageCautionPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.ADAS_USAGE_CAUTION;
    }

    /**
     * 同意ボタン
     */
    @OnClick(R.id.confirm_button)
    public void onClickAcceptButton() {
        getPresenter().onAcceptAction();
    }

    @Override
    public void onScrollToBottom(CustomScrollView scrollView) {
        getPresenter().onScrollBottomAction();
    }

    @Override
    public void setVisibleAgreeBtn(boolean visible) {
        mConfirm.setVisibility(visible?View.VISIBLE:View.GONE);
    }

    @Override
    public void setEnabledAgreeBtn(boolean isEnabled) {
        mConfirmBtn.setEnabled(isEnabled);
    }

    @Override
    public void setPage(int page) {
        if(page == 0) {
            ((SettingsContainerFragment) getParentFragment()).getPresenter().setPassCurrent(getString(R.string.set_340) + getString(R.string.set_370));
            ((SettingsContainerFragment) getParentFragment()).setPass(getString(R.string.set_340) + getString(R.string.set_370));
            mText1_1.setVisibility(View.VISIBLE);
            mText1_2.setVisibility(View.VISIBLE);
            mText2.setVisibility(View.GONE);
        } else if (page == 1) {
            ((SettingsContainerFragment) getParentFragment()).getPresenter().setPassCurrent(getString(R.string.set_340) + getString(R.string.set_371));
            ((SettingsContainerFragment)getParentFragment()).setPass(getString(R.string.set_340)+getString(R.string.set_371));
            mText1_1.setVisibility(View.GONE);
            mText1_2.setVisibility(View.GONE);
            mText2.setVisibility(View.VISIBLE);
        }
        mScrollView.scrollTo(0,0);
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(mScrollView!=null) {
                    setEnabledAgreeBtn(!canScroll(mScrollView));
                }
            }
        });

    }

    private boolean canScroll(ScrollView scrollView) {
        View child = (View) scrollView.getChildAt(0);
        if (child != null) {
            int childHeight = (child).getHeight();
            return scrollView.getHeight() < childHeight + scrollView.getPaddingTop() + scrollView.getPaddingBottom();
        }
        return false;
    }

    @Override
    public void onClose(StatusPopupDialogFragment fragment, String tag) {

    }

    @Override
    public void onPositiveClick(StatusPopupDialogFragment fragment, String tag) {
        if(tag.equals(AdasUsageCautionPresenter.TAG_ADAS_SPEC_CHECK)) {
            getPresenter().goAdasBilling();
        }
    }

    @Override
    public void onNegativeClick(StatusPopupDialogFragment fragment, String tag) {

    }
}
