package jp.pioneer.carsync.application.di.module;

import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.telephony.TelephonyManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.application.di.MediaSourceTypeKey;
import jp.pioneer.carsync.domain.component.AudioSettingUpdater;
import jp.pioneer.carsync.domain.component.BearingProvider;
import jp.pioneer.carsync.domain.component.BtSettingController;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.component.ImpactDetector;
import jp.pioneer.carsync.domain.component.SmartPhoneInterruptionController;
import jp.pioneer.carsync.domain.component.SourceController;
import jp.pioneer.carsync.domain.component.VoiceRecognitionRequester;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.repository.CarDeviceMediaRepository;
import jp.pioneer.carsync.domain.repository.SettingListRepository;
import jp.pioneer.carsync.domain.repository.StatusHolderRepository;
import jp.pioneer.carsync.infrastructure.component.AppMusicSourceControllerImpl;
import jp.pioneer.carsync.infrastructure.component.AudioSettingUpdaterImpl;
import jp.pioneer.carsync.infrastructure.component.BearingProviderImpl;
import jp.pioneer.carsync.infrastructure.component.BtSettingControllerImpl;
import jp.pioneer.carsync.infrastructure.component.CarDeviceImpl;
import jp.pioneer.carsync.infrastructure.component.ImpactDetectorImpl;
import jp.pioneer.carsync.infrastructure.component.SmartPhoneInterruptionControllerImpl;
import jp.pioneer.carsync.infrastructure.component.VoiceRecognitionRequesterImpl;
import jp.pioneer.carsync.infrastructure.repository.CarDeviceMediaRepositoryImpl;
import jp.pioneer.carsync.infrastructure.repository.SettingListRepositoryImpl;
import jp.pioneer.mle.pmg.player.PMGPlayer;

import static android.content.Context.USB_SERVICE;

/**
 * AppComponent用のDaggerモジュール.
 * <p>
 * Infrastructure層向けはこのクラスに定義する。
 *
 * @see AppModule
 * @see DomainModule
 * @see InfrastructureBindsModule
 */
@Module
public class InfrastructureModule {
    @Singleton
    @Provides
    public HandlerThread provideHandlerThread() {
        HandlerThread thread = new HandlerThread("InfrastructureThread");
        thread.start();
        return thread;
    }

    @ForInfrastructure
    @Singleton
    @Provides
    public Handler provideInfrastructureHandler(HandlerThread thread) {
        return new Handler(thread.getLooper());
    }

    @Singleton
    @Provides
    public PMGPlayer providePMGPlayer() {
        return new PMGPlayer();
    }

    @Provides
    public AudioManager provideAudioManager(Context context) {
        return (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Provides
    public SensorManager provideSensorManager(Context context) {
        return (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    @Provides
    public UsbManager provideUsbManager(Context context) {
        return (UsbManager) context.getSystemService(USB_SERVICE);
    }

    @ForInfrastructure
    @Provides
    public StatusHolder provideStatusHolder(StatusHolderRepository repository) {
        return repository.get();
    }

    @Singleton
    @Provides
    public CarDevice provideCarDevice(CarDeviceImpl impl) {
        impl.initialize();
        return impl;
    }

    @Singleton
    @Provides
    public ImpactDetector provideImpactDetector(ImpactDetectorImpl impl) {
        impl.initialize();
        return impl;
    }

    @Singleton
    @Provides
    public CarDeviceMediaRepository provideCarDeviceMediaRepository(CarDeviceMediaRepositoryImpl impl) {
        impl.initialize();
        return impl;
    }

    @Provides
    public TelephonyManager provideTelephonyManager(Context context) {
        return (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @ForInfrastructure
    @Provides
    public ExecutorService provideExecutorService() {
        return Executors.newSingleThreadExecutor();
    }

    @Singleton
    @Provides
    public BtSettingController provideBtSettingController(BtSettingControllerImpl impl) {
        impl.initialize();
        return impl;
    }

    @Singleton
    @Provides
    public SettingListRepository provideSettingListRepository(SettingListRepositoryImpl impl) {
        impl.initialize();
        return impl;
    }

    @ForInfrastructure
    @Provides
    public ReentrantLock provideReentrantLock() {
        return new ReentrantLock();
    }

    @Singleton
    @Provides
    @IntoMap
    @MediaSourceTypeKey(MediaSourceType.APP_MUSIC)
    public SourceController provideAppMusicSourceController(AppMusicSourceControllerImpl impl){
        impl.initialize();
        return impl;
    }

    @Singleton
    @Provides
    public BearingProvider providerBearingProvider(BearingProviderImpl impl) {
        impl.initialize();
        return impl;
    }

    @Singleton
    @Provides
    public SmartPhoneInterruptionController providerSmartPhoneInterruptionController(SmartPhoneInterruptionControllerImpl impl) {
        impl.initialize();
        return impl;
    }

    @Singleton
    @Provides
    public VoiceRecognitionRequester providerVoiceRecognitionRequester(VoiceRecognitionRequesterImpl impl){
        impl.initialize();
        return impl;
    }


    @Singleton
    @Provides
    public AudioSettingUpdater providerAudioSettingUpdater(AudioSettingUpdaterImpl impl){
        impl.initialize();
        return impl;
    }
}
