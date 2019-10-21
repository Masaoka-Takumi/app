package jp.pioneer.carsync.application.di.component;

import dagger.Subcomponent;
import jp.pioneer.carsync.application.di.ServiceLifeCycle;
import jp.pioneer.carsync.application.di.module.ServiceModule;
import jp.pioneer.carsync.presentation.view.service.ResourcefulService;

/**
 * Service用のDaggerコンポーネント.
 */
@ServiceLifeCycle
@Subcomponent(modules = ServiceModule.class)
public interface ServiceComponent {
    void inject(ResourcefulService service);
}
