package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.AudioSettingUpdater;

/**
 * 初回連携開始時のSLA設定.
 * <p>
 * SLA設定+4を実施する。
 * Appソースであることのチェックは呼び出し元で行う。
 * <p>
 * Appソース変更直後や連携開始直後に実施される想定のため、
 * 設定可能であることや対応しているかのチェックは行わない。
 * また、連続してソースが切り替わることによるSLA設定通知の失敗や、
 * 他のソースで実行してしまうことについてのリカバリーは実施しない。
 */
public class InitializeSlaSetting {
    @Inject AudioSettingUpdater mUpdater;
    @Inject @ForInfrastructure Handler mHandler;

    /**
     * コンストラクタ.
     */
    @Inject
    public InitializeSlaSetting(){
    }

    /**
     * 実行.
     */
    public void execute(){
        mHandler.post(() -> mUpdater.setSourceLevelAdjuster(2));
    }
}
