package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.UpdateAdasEvent;
import jp.pioneer.carsync.domain.interactor.PreferAdas;
import jp.pioneer.carsync.domain.model.AdasFunctionSensitivity;
import jp.pioneer.carsync.domain.model.AdasFunctionType;
import jp.pioneer.carsync.presentation.view.AdasWarningSettingView;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

/**
 * Created by NSW00_007906 on 2018/07/04.
 */

public class AdasWarningSettingPresenter extends Presenter<AdasWarningSettingView> {
    @Inject EventBus mEventBus;
    @Inject Context mContext;
    @Inject PreferAdas mPreferCase;

    /**
     * コンストラクタ
     */
    @Inject
    public AdasWarningSettingPresenter() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onTakeView() {
        updateView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onResume() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void onPause() {
    }

    private void updateView() {

        Optional.ofNullable(getView()).ifPresent(view -> {
            view.setFcwSetting(mPreferCase.getFunctionSetting(AdasFunctionType.FCW));
            view.setLdwSetting(mPreferCase.getFunctionSetting(AdasFunctionType.LDW));
            view.setPcwSetting(mPreferCase.getFunctionSetting(AdasFunctionType.PCW));
        });
    }

    /**
     * LDW機能　有効設定
     */
    public void onSelectLdwAction(boolean setting) {
        mPreferCase.setFunctionEnabled(AdasFunctionType.LDW, setting);

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * LDW機能　感度設定
     */
    public void onSelectLdwSensitivityAction() {
        AdasFunctionSensitivity sensitivity = mPreferCase.getFunctionSetting(AdasFunctionType.LDW).functionSensitivity;
        mPreferCase.setFunctionSensitivity(AdasFunctionType.LDW, sensitivity.toggle());

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * PCW機能　有効設定
     */
    public void onSelectPcwAction(boolean setting) {
        mPreferCase.setFunctionEnabled(AdasFunctionType.PCW, setting);

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * PCW機能　感度設定
     */
    public void onSelectPcwSensitivityAction() {
        AdasFunctionSensitivity sensitivity = mPreferCase.getFunctionSetting(AdasFunctionType.PCW).functionSensitivity;
        mPreferCase.setFunctionSensitivity(AdasFunctionType.PCW, sensitivity.toggle());

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * FCW機能　有効設定
     */
    public void onSelectFcwAction(boolean setting) {
        mPreferCase.setFunctionEnabled(AdasFunctionType.FCW, setting);

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    /**
     * FCW機能　感度設定
     */
    public void onSelectFcwSensitivityAction() {
        AdasFunctionSensitivity sensitivity = mPreferCase.getFunctionSetting(AdasFunctionType.FCW).functionSensitivity;
        mPreferCase.setFunctionSensitivity(AdasFunctionType.FCW, sensitivity.toggle());

        updateView();
        mEventBus.post(new UpdateAdasEvent());
    }

    private Bundle createSettingsParams(String pass) {
        SettingsParams params = new SettingsParams();
        params.pass = pass;
        return params.toBundle();
    }

    private Bundle createSettingsParams(ScreenId screenId, String pass) {
        SettingsParams params = new SettingsParams();
        params.mScreenId = screenId;
        params.pass = pass;
        return params.toBundle();
    }
}