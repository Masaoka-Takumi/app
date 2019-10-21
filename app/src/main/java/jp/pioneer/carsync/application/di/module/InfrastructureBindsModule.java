package jp.pioneer.carsync.application.di.module;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import jp.pioneer.carsync.application.di.MediaSourceTypeKey;
import jp.pioneer.carsync.domain.component.BluetoothHeadsetProvider;
import jp.pioneer.carsync.domain.component.DabFunctionSettingUpdater;
import jp.pioneer.carsync.domain.component.Geocoder;
import jp.pioneer.carsync.domain.component.GooglePlayServicesAvailabilityChecker;
import jp.pioneer.carsync.domain.component.HdRadioFunctionSettingUpdater;
import jp.pioneer.carsync.domain.component.IlluminationSettingUpdater;
import jp.pioneer.carsync.domain.component.InitialSettingUpdater;
import jp.pioneer.carsync.domain.component.LocationProvider;
import jp.pioneer.carsync.domain.component.MediaListController;
import jp.pioneer.carsync.domain.component.NaviGuideVoiceSettingUpdater;
import jp.pioneer.carsync.domain.component.NotificationProvider;
import jp.pioneer.carsync.domain.component.ParkingSensorSettingUpdater;
import jp.pioneer.carsync.domain.component.PhoneSettingUpdater;
import jp.pioneer.carsync.domain.component.RadioFunctionSettingUpdater;
import jp.pioneer.carsync.domain.component.SoundFxSettingUpdater;
import jp.pioneer.carsync.domain.component.SourceController;
import jp.pioneer.carsync.domain.component.SystemSettingUpdater;
import jp.pioneer.carsync.domain.component.TextToSpeechController;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.repository.AppMusicRepository;
import jp.pioneer.carsync.domain.repository.ApplicationInfoRepository;
import jp.pioneer.carsync.domain.repository.ContactRepository;
import jp.pioneer.carsync.domain.repository.FavoriteRepository;
import jp.pioneer.carsync.domain.repository.NowPlayingListRepository;
import jp.pioneer.carsync.domain.repository.PairingDeviceListRepository;
import jp.pioneer.carsync.domain.repository.StatusHolderRepository;
import jp.pioneer.carsync.infrastructure.component.AuxSourceControllerImpl;
import jp.pioneer.carsync.infrastructure.component.BluetoothHeadsetProviderImpl;
import jp.pioneer.carsync.infrastructure.component.BtAudioSourceControllerImpl;
import jp.pioneer.carsync.infrastructure.component.CdSourceControllerImpl;
import jp.pioneer.carsync.infrastructure.component.DabFunctionSettingUpdaterImpl;
import jp.pioneer.carsync.infrastructure.component.DabSourceControllerImpl;
import jp.pioneer.carsync.infrastructure.component.GeocoderImpl;
import jp.pioneer.carsync.infrastructure.component.GoogleApiAvailabilityCheckerImpl;
import jp.pioneer.carsync.infrastructure.component.HdRadioFunctionSettingUpdaterImpl;
import jp.pioneer.carsync.infrastructure.component.HdRadioSourceControllerImpl;
import jp.pioneer.carsync.infrastructure.component.IlluminationSettingUpdaterImpl;
import jp.pioneer.carsync.infrastructure.component.InitialSettingUpdaterImpl;
import jp.pioneer.carsync.infrastructure.component.LocationProviderImpl;
import jp.pioneer.carsync.infrastructure.component.MediaListControllerImpl;
import jp.pioneer.carsync.infrastructure.component.NaviGuideVoiceSettingUpdaterImpl;
import jp.pioneer.carsync.infrastructure.component.NotificationInteractor;
import jp.pioneer.carsync.infrastructure.component.NotificationListener;
import jp.pioneer.carsync.infrastructure.component.PandoraSourceControllerImpl;
import jp.pioneer.carsync.infrastructure.component.ParkingSensorSettingUpdaterImpl;
import jp.pioneer.carsync.infrastructure.component.PhoneSettingUpdaterImpl;
import jp.pioneer.carsync.infrastructure.component.RadioSourceControllerImpl;
import jp.pioneer.carsync.infrastructure.component.RadioFunctionSettingUpdaterImpl;
import jp.pioneer.carsync.infrastructure.component.SiriusXmSourceControllerImpl;
import jp.pioneer.carsync.infrastructure.component.SoundFxSettingUpdaterImpl;
import jp.pioneer.carsync.infrastructure.component.SpotifySourceControllerImpl;
import jp.pioneer.carsync.infrastructure.component.SystemSettingUpdaterImpl;
import jp.pioneer.carsync.infrastructure.component.TextToSpeechControllerImpl;
import jp.pioneer.carsync.infrastructure.component.UsbSourceControllerImpl;
import jp.pioneer.carsync.infrastructure.repository.AppMusicRepositoryImpl;
import jp.pioneer.carsync.infrastructure.repository.ApplicationInfoRepositoryImpl;
import jp.pioneer.carsync.infrastructure.repository.ContactRepositoryImpl;
import jp.pioneer.carsync.infrastructure.repository.FavoriteRepositoryImpl;
import jp.pioneer.carsync.infrastructure.repository.NowPlayingListRepositoryImpl;
import jp.pioneer.carsync.infrastructure.repository.PairingDeviceListRepositoryImpl;
import jp.pioneer.carsync.infrastructure.repository.StatusHolderRepositoryImpl;

/**
 * AppComponent用のDaggerモジュール.
 * <p>
 * Infrastructure層向けで、interfaceの実装クラスを単純にDIする（即returnするだけ）場合はこのクラスに定義する。
 *
 * @see AppModule
 * @see DomainModule
 * @see InfrastructureModule
 */
@Module
public abstract class InfrastructureBindsModule {
    @Binds
    public abstract LocationProvider provideLocationProvider(LocationProviderImpl impl);

    @Singleton
    @Binds
    public abstract StatusHolderRepository provideStatusHolderRepository(StatusHolderRepositoryImpl impl);

    @Singleton
    @Binds
    public abstract ApplicationInfoRepository provideApplicationInfoRepository(ApplicationInfoRepositoryImpl impl);

    @Singleton
    @Binds
    public abstract AppMusicRepository provideAppMusicLoaderRepository(AppMusicRepositoryImpl impl);

    @Singleton
    @Binds
    public abstract ContactRepository provideContactRepository(ContactRepositoryImpl impl);

    @Singleton
    @Binds
    public abstract Geocoder provideGeocoder(GeocoderImpl impl);

    @Binds
    public abstract TextToSpeechController provideTextToSpeechController(TextToSpeechControllerImpl impl);

    @Singleton
    @Binds
    public abstract NotificationProvider provideNotificationProvider(NotificationInteractor interactor);

    @Singleton
    @Binds
    public abstract NotificationListener provideNotificationListener(NotificationInteractor interactor);

    @Singleton
    @Binds
    public abstract GooglePlayServicesAvailabilityChecker provideGoogleApiAvailabilityChecker(GoogleApiAvailabilityCheckerImpl impl);

    @Singleton
    @Binds
    public abstract MediaListController provideMediaListController(MediaListControllerImpl impl);

    @Singleton
    @Binds
    public abstract BluetoothHeadsetProvider provideBluetoothHeadsetProvider(BluetoothHeadsetProviderImpl impl);

    @Singleton
    @Binds
    public abstract FavoriteRepository provideFavoriteRepository(FavoriteRepositoryImpl impl);

    @Singleton
    @Binds
    public abstract RadioFunctionSettingUpdater provideRadioFunctionSettingUpdater(RadioFunctionSettingUpdaterImpl impl);

    @Singleton
    @Binds
    public abstract DabFunctionSettingUpdater provideDabFunctionSettingUpdater(DabFunctionSettingUpdaterImpl impl);

    @Singleton
    @Binds
    public abstract HdRadioFunctionSettingUpdater provideHdRadioFunctionSettingUpdater(HdRadioFunctionSettingUpdaterImpl impl);

    @Singleton
    @Binds
    @IntoMap
    @MediaSourceTypeKey(MediaSourceType.RADIO)
    public abstract SourceController provideRadioSourceController(RadioSourceControllerImpl impl);

    @Singleton
    @Binds
    @IntoMap
    @MediaSourceTypeKey(MediaSourceType.DAB)
    public abstract SourceController provideDabSourceController(DabSourceControllerImpl impl);

    @Singleton
    @Binds
    @IntoMap
    @MediaSourceTypeKey(MediaSourceType.HD_RADIO)
    public abstract SourceController provideHdRadioSourceController(HdRadioSourceControllerImpl impl);

    @Singleton
    @Binds
    @IntoMap
    @MediaSourceTypeKey(MediaSourceType.TI)
    public abstract SourceController provideTiSourceController(RadioSourceControllerImpl impl);

    @Singleton
    @Binds
    @IntoMap
    @MediaSourceTypeKey(MediaSourceType.BT_AUDIO)
    public abstract SourceController provideBtAudioSourceController(BtAudioSourceControllerImpl impl);

    @Singleton
    @Binds
    @IntoMap
    @MediaSourceTypeKey(MediaSourceType.SPOTIFY)
    public abstract SourceController provideSpotifySourceControllerImpl(SpotifySourceControllerImpl impl);

    @Singleton
    @Binds
    @IntoMap
    @MediaSourceTypeKey(MediaSourceType.PANDORA)
    public abstract SourceController providePandoraSourceControllerImpl(PandoraSourceControllerImpl impl);

    @Singleton
    @Binds
    @IntoMap
    @MediaSourceTypeKey(MediaSourceType.CD)
    public abstract SourceController provideCdSourceControllerImpl(CdSourceControllerImpl impl);

    @Singleton
    @Binds
    @IntoMap
    @MediaSourceTypeKey(MediaSourceType.USB)
    public abstract SourceController provideUsbSourceControllerImpl(UsbSourceControllerImpl impl);

    @Singleton
    @Binds
    @IntoMap
    @MediaSourceTypeKey(MediaSourceType.SIRIUS_XM)
    public abstract SourceController provideSiriusXmSourceControllerImpl(SiriusXmSourceControllerImpl impl);

    @Singleton
    @Binds
    @IntoMap
    @MediaSourceTypeKey(MediaSourceType.AUX)
    public abstract SourceController provideAuxSourceControllerImpl(AuxSourceControllerImpl impl);

    @Singleton
    @Binds
    public abstract IlluminationSettingUpdater provideIlluminationSettingUpdater(IlluminationSettingUpdaterImpl impl);

    @Singleton
    @Binds
    public abstract SoundFxSettingUpdater providerSoundFxSettingUpdater(SoundFxSettingUpdaterImpl impl);

    @Singleton
    @Binds
    public abstract ParkingSensorSettingUpdater providerParkingSensorSettingUpdater(ParkingSensorSettingUpdaterImpl impl);

    @Singleton
    @Binds
    public abstract NowPlayingListRepository providerNowPlayingListRepository(NowPlayingListRepositoryImpl impl);

    @Singleton
    @Binds
    public abstract PairingDeviceListRepository providerPairingDeviceListRepository(PairingDeviceListRepositoryImpl impl);

    @Singleton
    @Binds
    public abstract SystemSettingUpdater providerSystemSettingUpdater(SystemSettingUpdaterImpl impl);

    @Singleton
    @Binds
    public abstract InitialSettingUpdater providerInitialSettingUpdater(InitialSettingUpdaterImpl impl);

    @Singleton
    @Binds
    public abstract PhoneSettingUpdater providerPhoneSettingUpdater(PhoneSettingUpdaterImpl impl);

    @Singleton
    @Binds
    public abstract NaviGuideVoiceSettingUpdater providerNaviGuideVoiceSettingUpdater(NaviGuideVoiceSettingUpdaterImpl impl);
}
