package jp.pioneer.carsync.application.di.module;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.internal.AppMusicObserver;
import jp.pioneer.carsync.domain.internal.AudioSettingObserver;
import jp.pioneer.carsync.domain.internal.BtAudioObserver;
import jp.pioneer.carsync.domain.internal.CarDeviceStatusObserver;
import jp.pioneer.carsync.domain.internal.CdObserver;
import jp.pioneer.carsync.domain.internal.DabObserver;
import jp.pioneer.carsync.domain.internal.HdRadioObserver;
import jp.pioneer.carsync.domain.internal.IlluminationSettingObserver;
import jp.pioneer.carsync.domain.internal.InitialSettingObserver;
import jp.pioneer.carsync.domain.internal.ListInfoObserver;
import jp.pioneer.carsync.domain.internal.NaviGuideVoiceSettingObserver;
import jp.pioneer.carsync.domain.internal.PandoraObserver;
import jp.pioneer.carsync.domain.internal.ParkingSensorSettingObserver;
import jp.pioneer.carsync.domain.internal.ParkingSensorStatusObserver;
import jp.pioneer.carsync.domain.internal.PhoneSettingObserver;
import jp.pioneer.carsync.domain.internal.RadioObserver;
import jp.pioneer.carsync.domain.internal.SessionStatusObserver;
import jp.pioneer.carsync.domain.internal.SoundFxSettingObserver;
import jp.pioneer.carsync.domain.internal.SpotifyObserver;
import jp.pioneer.carsync.domain.internal.StatusObserver;
import jp.pioneer.carsync.domain.internal.SxmObserver;
import jp.pioneer.carsync.domain.internal.SystemSettingObserver;
import jp.pioneer.carsync.domain.internal.TransportStatusObserver;
import jp.pioneer.carsync.domain.internal.UsbObserver;
import jp.pioneer.carsync.domain.model.AbstractNotification;
import jp.pioneer.carsync.domain.model.FacebookMessengerNotification;
import jp.pioneer.carsync.domain.model.HangoutsNotification;
import jp.pioneer.carsync.domain.model.LineNotification;
import jp.pioneer.carsync.domain.model.MessagingApp;
import jp.pioneer.carsync.domain.model.MessengerNotification;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.VKNotification;
import jp.pioneer.carsync.domain.model.ViberNotification;
import jp.pioneer.carsync.domain.model.WeChatNotification;
import jp.pioneer.carsync.domain.model.WhatsAppMessengerNotification;

/**
 * AppComponent用のDaggerモジュール.
 * <p>
 * Domain層向けはこのクラスに定義する。
 *
 * @see AppModule
 * @see InfrastructureModule
 * @see InfrastructureBindsModule
 */
@Module
public class DomainModule {
    @Singleton
    @Provides
    public StatusObserver[] provideStatusObservers(
            Provider<TransportStatusObserver> transportStatusObserver,
            Provider<SessionStatusObserver> sessionStatusObserver,
            Provider<CarDeviceStatusObserver> carDeviceStatusObserver,
            Provider<AudioSettingObserver> audioSettingObserver,
            Provider<IlluminationSettingObserver> illuminationSettingObserver,
            Provider<ListInfoObserver> listInfoObserver,
            Provider<AppMusicObserver> appMusicObserver,
            Provider<CdObserver> cdObserver,
            Provider<PandoraObserver> pandoraObserver,
            Provider<RadioObserver> radioObserver,
            Provider<DabObserver> dabObserver,
            Provider<HdRadioObserver> hdRadioObserver,
            Provider<SpotifyObserver> spotifyObserver,
            Provider<BtAudioObserver> btAudioObserver,
            Provider<UsbObserver> usbObserver,
            Provider<SxmObserver> sxmObserver,
            Provider<SoundFxSettingObserver> soundFxSettingObserver,
            Provider<InitialSettingObserver> initialSettingObserverProvider,
            Provider<NaviGuideVoiceSettingObserver> naviGuideVoiceSettingObserverProvider,
            Provider<ParkingSensorSettingObserver> parkingSensorSettingObserverProvider,
            Provider<ParkingSensorStatusObserver> parkingSensorStatusObserverProvider,
            Provider<PhoneSettingObserver> phoneSettingObserverProvider,
            Provider<SystemSettingObserver> systemSettingObserverProvider) {
        return new StatusObserver[] {
                transportStatusObserver.get(),
                sessionStatusObserver.get(),
                carDeviceStatusObserver.get(),
                audioSettingObserver.get(),
                illuminationSettingObserver.get(),
                listInfoObserver.get(),
                appMusicObserver.get(),
                cdObserver.get(),
                pandoraObserver.get(),
                radioObserver.get(),
                dabObserver.get(),
                hdRadioObserver.get(),
                spotifyObserver.get(),
                btAudioObserver.get(),
                usbObserver.get(),
                sxmObserver.get(),
                soundFxSettingObserver.get(),
                initialSettingObserverProvider.get(),
                naviGuideVoiceSettingObserverProvider.get(),
                parkingSensorSettingObserverProvider.get(),
                parkingSensorStatusObserverProvider.get(),
                phoneSettingObserverProvider.get(),
                systemSettingObserverProvider.get()

        };
    }

    @Singleton
    @Provides
    public Map<MessagingApp, Provider<? extends AbstractNotification>> provideNotificationProviders(
            Provider<FacebookMessengerNotification> facebookMessenger,
            Provider<HangoutsNotification> hangOut,
            Provider<LineNotification> line,
            Provider<MessengerNotification> messenger,
            Provider<VKNotification> vk,
            Provider<ViberNotification> viber,
            Provider<WeChatNotification> weChat,
            Provider<WhatsAppMessengerNotification> whatsAppMessenger) {
        return ImmutableMap.<MessagingApp, Provider<? extends AbstractNotification>>builder()
                .put(MessagingApp.FACEBOOK_MESSENGER, facebookMessenger)
                .put(MessagingApp.HANGOUTS, hangOut)
                .put(MessagingApp.LINE, line)
                .put(MessagingApp.MESSENGER, messenger)
                .put(MessagingApp.VK, vk)
                .put(MessagingApp.VIBER, viber)
                .put(MessagingApp.WE_CHAT, weChat)
                .put(MessagingApp.WHATS_APP_MESSENGER, whatsAppMessenger)
                .build();
    }

    @ForDomain
    @Provides
    public StatusHolder provideStatusHolder(GetStatusHolder getStatusHolder) {
        return getStatusHolder.execute();
    }
}
