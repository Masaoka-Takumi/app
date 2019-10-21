package jp.pioneer.carsync.application.di.component;

import dagger.Subcomponent;
import jp.pioneer.carsync.application.di.CarRemoteSessionLifeCycle;
import jp.pioneer.carsync.application.di.module.CarRemoteSessionModule;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.task.AudioSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.FunctionSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.IlluminationSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.InitialAuthTask;
import jp.pioneer.carsync.infrastructure.crp.task.InitialSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.MediaListSelectTask;
import jp.pioneer.carsync.infrastructure.crp.task.NaviGuideVoiceSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.ParkingSensorSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.PhoneSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.PostTask;
import jp.pioneer.carsync.infrastructure.crp.task.SendTask;
import jp.pioneer.carsync.infrastructure.crp.task.SessionStartTask;
import jp.pioneer.carsync.infrastructure.crp.task.SoundFxSettingsRequestTask;
import jp.pioneer.carsync.infrastructure.crp.task.SystemSettingsRequestTask;

/**
 * CarRemoteSession用のコンポーネント.
 */
@CarRemoteSessionLifeCycle
@Subcomponent(modules = CarRemoteSessionModule.class)
public interface CarRemoteSessionComponent {
    void inject(CarRemoteSession session);
    void inject(SendTask task);
    void inject(InitialAuthTask task);
    void inject(SessionStartTask task);
    void inject(PostTask task);
    void inject(SystemSettingsRequestTask task);
    void inject(AudioSettingsRequestTask task);
    void inject(IlluminationSettingsRequestTask task);
    void inject(FunctionSettingsRequestTask task);
    void inject(ParkingSensorSettingsRequestTask task);
    void inject(NaviGuideVoiceSettingsRequestTask task);
    void inject(InitialSettingsRequestTask task);
    void inject(PhoneSettingsRequestTask task);
    void inject(SoundFxSettingsRequestTask task);
    void inject(MediaListSelectTask task);
}
