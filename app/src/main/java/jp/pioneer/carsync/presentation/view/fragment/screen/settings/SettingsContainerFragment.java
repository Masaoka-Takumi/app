package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.presentation.controller.SettingsFragmentController;
import jp.pioneer.carsync.presentation.presenter.SettingsContainerPresenter;
import jp.pioneer.carsync.presentation.view.SettingsContainerView;
import jp.pioneer.carsync.presentation.view.activity.MainActivity;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.OnExitSettingListener;
import jp.pioneer.carsync.presentation.view.fragment.OnGoBackListener;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.VideoPlayerDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

import static jp.pioneer.carsync.presentation.view.fragment.ScreenId.ALEXA_SETTING;
import static jp.pioneer.carsync.presentation.view.fragment.ScreenId.ALEXA_SPLASH;
import static jp.pioneer.carsync.presentation.view.fragment.ScreenId.EQ_PRO_SETTING;
import static jp.pioneer.carsync.presentation.view.fragment.ScreenId.EQ_QUICK_SETTING;
import static jp.pioneer.carsync.presentation.view.fragment.ScreenId.MENU_DISPLAY_LANGUAGE_DIALOG;
import static jp.pioneer.carsync.presentation.view.fragment.ScreenId.SETTINGS_SYSTEM_INITIAL;

/**
 * EQ Quick Settingの画面
 */

public class SettingsContainerFragment extends AbstractScreenFragment<SettingsContainerPresenter, SettingsContainerView>
        implements SettingsContainerView, OnGoBackListener, OnExitSettingListener {
    public static final String KEY_RETURN_SCREEN_WHEN_CLOSE = "KEY_RETURN_SCREEN_WHEN_CLOSE";
    @Inject SettingsContainerPresenter mPresenter;
    @Inject SettingsFragmentController mFragmentController;
    @BindView(R.id.directory_pass_text) TextView mPass;
    @BindView(R.id.directory_pass_text_autofit) TextView mPassAutofit;
    @BindView(R.id.close_button) ImageView mCloseBtn;
    @BindView(R.id.my_photo_button) ImageView mMyPhotoBtn;
    @BindView(R.id.back_button) ImageView mBackBtn;
    @BindView(R.id.next_button) ImageView mNextBtn;
    @BindView(R.id.caution_text) TextView mCautionText;
    @BindView(R.id.caution_view) RelativeLayout mCautionView;
    @BindView(R.id.caution_background) View mCautionBackground;
    @BindView(R.id.navigate_bar) LinearLayout mNavigateBar;
    private Unbinder mUnbinder;

    public SettingsContainerFragment() {
    }

    public static SettingsContainerFragment newInstance(Bundle args) {
        SettingsContainerFragment fragment = new SettingsContainerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_container_settings, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        getPresenter().setArgument(getArguments());
        mFragmentController.setContainerViewId(R.id.container);
        mCloseBtn.setVisibility(View.VISIBLE);
        // 閉じるボタン押下時に遷移する画面
        Bundle bundle = getArguments();
        getPresenter().setReturnScreenWhenClose((ScreenId) bundle.get(KEY_RETURN_SCREEN_WHEN_CLOSE));
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public ScreenId getScreenId() {
        return mFragmentController.getScreenIdInContainer();
    }

    @Override
    public ScreenId getCurrentScreenId() {
        return mFragmentController.getScreenIdInContainer();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected SettingsContainerPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public boolean onNavigate(ScreenId screenId, Bundle args) {
        if (mFragmentController.navigate(screenId, args)) {
            ((MainActivity)getActivity()).setOrientation(screenId);
            getPresenter().suppressDeviceConnection(screenId);
            getPresenter().setContainerArguments(args);
            getPresenter().setPass(args);
            updateCloseButton(screenId);
            updateBackButton(screenId);
            updateNextButton(screenId);
            updateCaution(screenId);
            updateNavigateBar(screenId);
            updateMyPhotoButton(screenId);
            if (screenId.isSettings()) {
                getPresenter().requestExitMenu();
            }
            return true;
        }else{
            if(screenId == ScreenId.CAR_SAFETY_SETTINGS){
                getPresenter().removePass(getString(R.string.set_038));
                updateCloseButton(screenId);
                updateBackButton(screenId);
                updateNextButton(screenId);
                updateNavigateBar(screenId);
                updateMyPhotoButton(screenId);
            }else if(screenId == ScreenId.SETTINGS_ADAS){
                getPresenter().removePass(getString(R.string.set_003));
                updateCloseButton(screenId);
                updateBackButton(screenId);
                updateNextButton(screenId);
                updateNavigateBar(screenId);
                updateMyPhotoButton(screenId);
            }
        }
        return false;
    }

    @Override
    public boolean onGoBack() {
        getPresenter().setPrevScreen(mFragmentController.getScreenIdInContainer());
        if (mCautionView.getVisibility() == View.VISIBLE) {
            return true;
        }

        if (mFragmentController.getScreenIdInContainer() == ScreenId.SETTINGS_ENTRANCE) {
            getPresenter().onCloseAction();
            return true;
        }

        if (mFragmentController.getScreenIdInContainer() == ScreenId.ALEXA_SETTING) {
            onNavigate(ScreenId.SETTINGS_ENTRANCE, createSettingsParams(getString(R.string.hom_015)));
            return true;
        }

        if ((mFragmentController.getScreenIdInContainer() == SETTINGS_SYSTEM_INITIAL ||
                mFragmentController.getScreenIdInContainer() == MENU_DISPLAY_LANGUAGE_DIALOG) &&
                !getPresenter().isFirstInitialSetting()) {
            return true;
        }

        if(mFragmentController.getScreenIdInContainer() == ScreenId.VIDEO_PLAYER){
            VideoPlayerDialogFragment fragment = (VideoPlayerDialogFragment) mFragmentController.getContainerFragment();
            if(fragment.onBack())return true;
        }

        SettingsParams params = SettingsParams.from(getPresenter().getContainerArguments());
        if ((mFragmentController.getScreenIdInContainer() == ScreenId.ADAS_BILLING&&params.mScreenId==ScreenId.ADAS_BILLING)
        ||(mFragmentController.getScreenIdInContainer() == ScreenId.ADAS_MANUAL&&params.mScreenId==ScreenId.ADAS_BILLING)) {
            getPresenter().onGoCarSafetyAction();
            return true;
        }
        if (mFragmentController.getScreenIdInContainer() == ScreenId.ADAS_MANUAL&&params.mScreenId==ScreenId.ADAS_USAGE_CAUTION) {
            getPresenter().onGoAdasSettingAction();
            return true;
        }
        if (mFragmentController.getScreenIdInContainer() == ScreenId.CAR_SAFETY_SETTINGS) {
            getPresenter().onGoSettingTop();
            return true;
        }

        if (mFragmentController.getScreenIdInContainer() == ScreenId.ALEXA_EXAMPLE_USAGE) {
            AlexaExampleUsageFragment fragment = (AlexaExampleUsageFragment) mFragmentController.getContainerFragment();
            if (fragment.onBackViewPagerAction()) {
                return true;
            } else if(fragment.getPresenter().getBeforeScreenId(fragment.getArguments()) != ALEXA_SETTING) {
                // Alexa設定画面以外(Alexa Splash画面)から遷移してきた場合はなにもしない
                return true;
            }
        }

        if (mFragmentController.goBack()) {
            ScreenId currentScreenId = getScreenId();
            if (currentScreenId != ScreenId.ADVANCED_AUDIO_SETTING) {
                getPresenter().removePass();
                updateCloseButton(currentScreenId);
                updateMyPhotoButton(currentScreenId);
                updateBackButton(currentScreenId);
                updateNextButton(currentScreenId);
                updateNavigateBar(currentScreenId);
                if (currentScreenId.isSettings()) {
                    getPresenter().requestExitMenu();
                }
            }

            return true;
        }

        return false;
    }

    public void removePass(int page) {
        getPresenter().removePass(page);
    }

    @Override
    public void setPass(String pass) {
        if(getScreenId()==ScreenId.ADAS_USAGE_CAUTION){
            mPass.setVisibility(View.GONE);
            mPassAutofit.setVisibility(View.VISIBLE);
            mPassAutofit.setText(pass);
        }else {
            if (mPass.getVisibility() != View.VISIBLE || mPassAutofit.getVisibility() == View.VISIBLE) {
                mPass.setVisibility(View.VISIBLE);
                mPassAutofit.setVisibility(View.GONE);
                mPassAutofit.setText(null);
            }
            mPass.setText(pass);
        }
    }

    @Override
    public void updateCloseButton() {
        ScreenId currentScreenId = getScreenId();
        if (currentScreenId != null) {
            updateCloseButton(currentScreenId);
        }
    }

    @Override
    public void updateBackButton() {
        ScreenId currentScreenId = getScreenId();
        if (currentScreenId != null) {
            updateBackButton(currentScreenId);
        }
    }

    @Override
    public void updateNextButton() {
        ScreenId currentScreenId = getScreenId();
        if (currentScreenId != null) {
            updateNextButton(currentScreenId);
        }
    }

    @Override
    public void updateCaution() {
        ScreenId currentScreenId = getScreenId();
        if (currentScreenId != null) {
            updateCaution(currentScreenId);
        }
    }

    @Override
    public void updateNavigateBar() {
        ScreenId currentScreenId = getScreenId();
        if (currentScreenId != null) {
            updateNavigateBar(currentScreenId);
        }
    }

    @Override
    public void updateOtherButton() {
        ScreenId currentScreenId = getScreenId();
        if (currentScreenId != null) {
            updateMyPhotoButton(currentScreenId);
        }
    }

    @OnClick(R.id.back_button)
    public void onClickBackButton(View view) {
        getPresenter().onBackAction();
    }

    @OnClick(R.id.close_button)
    public void onClickCloseButton(View view) {
        if (mFragmentController.getScreenIdInContainer() == ScreenId.SETTINGS_SYSTEM_INITIAL) {
            getPresenter().setFirstInitialSetting(true);
        }
        getPresenter().onCloseAction();
    }

    @OnClick(R.id.next_button)
    public void onClickNextButton(View view) {
        getPresenter().onNextAction();
    }

    @OnClick(R.id.my_photo_button)
    public void onClickMyPhotoButton(View view) {
        //getPresenter().onMyPhotoAction();
        ((MainActivity)getActivity()).getGalleryImage();
    }

    private void updateCloseButton(ScreenId id) {
        if(id.isDialog())return;
        if (id.isSettings()
                && id != ScreenId.ADAS_CAMERA_SETTING && id != ScreenId.ADAS_WARNING_SETTING
                && id != ScreenId.ADAS_BILLING && id != ScreenId.ADAS_USAGE_CAUTION && id != ScreenId.ADAS_MANUAL) {
            mCloseBtn.setVisibility(View.VISIBLE);
        } else {
            mCloseBtn.setVisibility(View.GONE);
        }
    }
    private void updateMyPhotoButton(ScreenId id) {
        if(id.isDialog())return;
        if (id.isSettings()
                && id == ScreenId.THEME_SET_SETTING) {
            mMyPhotoBtn.setVisibility(View.VISIBLE);
        } else {
            mMyPhotoBtn.setVisibility(View.GONE);
        }
    }
    private void updateBackButton(ScreenId id) {
        if ((id == SETTINGS_SYSTEM_INITIAL ||
                id == MENU_DISPLAY_LANGUAGE_DIALOG) && !getPresenter().isFirstInitialSetting()) {
            mBackBtn.setVisibility(View.GONE);
        } else {
            mBackBtn.setVisibility(View.VISIBLE);
        }
    }

    private void updateNextButton(ScreenId id) {
        SettingsParams params = SettingsParams.from(getPresenter().getContainerArguments());
        if (id == ScreenId.ADAS_CAMERA_SETTING ||
                id == ScreenId.ADAS_WARNING_SETTING ||
                /*(id == ScreenId.ADAS_USAGE_CAUTION&&(params.mScreenId==ScreenId.SETTINGS_ADAS||getPresenter().getPrevScreen()==ScreenId.ADAS_MANUAL)) ||*/
                id == ScreenId.ADAS_MANUAL&&params.mScreenId!=ScreenId.ADAS_USAGE_CAUTION) {
            mNextBtn.setVisibility(View.VISIBLE);
        } else {
            mNextBtn.setVisibility(View.GONE);
        }
    }

    private void updateNavigateBar(ScreenId id) {
        if(id.isDialog())return;
        if (id == ScreenId.ADAS_TUTORIAL||id == ScreenId.CALIBRATION_SETTING||id == ScreenId.CALIBRATION_SETTING_FITTING||id == ScreenId.VIDEO_PLAYER
                ||id == ScreenId.ALEXA_SETTING||id == ScreenId.ALEXA_SPLASH||id == ScreenId.ALEXA_EXAMPLE_USAGE) {
            mNavigateBar.setVisibility(View.GONE);
        } else {
            mNavigateBar.setVisibility(View.VISIBLE);
        }
    }

    private void updateCaution(ScreenId id) {
        if (id == EQ_PRO_SETTING || id == EQ_QUICK_SETTING || (id == ScreenId.CALIBRATION_SETTING && getPresenter().getSessionStatus() == SessionStatus.STARTED)) {
            mCautionView.setVisibility(View.VISIBLE);
            mCautionBackground.setVisibility(View.VISIBLE);
            mCautionBackground.setOnTouchListener((v, event) -> {
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
            });
            getPresenter().setNeedDisplayCaution(true);
        } else {
            mCautionView.setVisibility(View.GONE);
            mCautionBackground.setVisibility(View.GONE);
            mCautionBackground.setOnTouchListener(null);
            getPresenter().setNeedDisplayCaution(false);
        }
    }

    public void dismissStatusPopup() {
        mFragmentController.dismissStatusPopup();
    }

    /**
     * 確認ボタン押下イベント
     */
    @OnClick(R.id.confirm_button)
    public void onClickConfirm() {
        mCautionView.setVisibility(View.GONE);
        mCautionBackground.setVisibility(View.GONE);
        mCautionBackground.setOnTouchListener(null);
        getPresenter().onAgreedCaution();
    }

    private Bundle createSettingsParams(String pass) {
        SettingsParams params = new SettingsParams();
        params.pass = pass;
        return params.toBundle();
    }

    @Override
    public boolean onExitSetting() {
        getPresenter().onCloseAction();
        return true;
    }

}
