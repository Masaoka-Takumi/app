package jp.pioneer.carsync.application.di.component;

import java.util.Map;

import javax.inject.Singleton;

import dagger.Component;
import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.di.module.AppModule;
import jp.pioneer.carsync.application.di.module.CarRemoteSessionModule;
import jp.pioneer.carsync.application.di.module.DomainModule;
import jp.pioneer.carsync.application.di.module.InfrastructureBindsModule;
import jp.pioneer.carsync.application.di.module.InfrastructureModule;
import jp.pioneer.carsync.application.di.module.PresenterModule;
import jp.pioneer.carsync.domain.component.SourceController;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.infrastructure.component.BroadcastReceiverImpl;
import jp.pioneer.carsync.infrastructure.component.NotificationListenerServiceImpl;

/**
 * アプリケーション全体用のDaggerコンポーネント.
 */
@Singleton
@Component(modules = {
        AppModule.class,
        DomainModule.class,
        InfrastructureModule.class,
        InfrastructureBindsModule.class
})
public interface AppComponent {
    void inject(App app);
    void inject(NotificationListenerServiceImpl service);
    void inject(BroadcastReceiverImpl receiver);

    PresenterComponent presenterComponent(PresenterModule module);
    CarRemoteSessionComponent carRemoteProtocolComponent(CarRemoteSessionModule module);
    Map<MediaSourceType, SourceController> sourceControllers();
}
