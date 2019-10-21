package jp.pioneer.carsync.presentation.view.service;

import android.app.Service;
import android.support.annotation.NonNull;

import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.di.component.PresenterComponent;
import jp.pioneer.carsync.application.di.component.ServiceComponent;
import jp.pioneer.carsync.application.factory.ComponentFactory;
import jp.pioneer.carsync.presentation.presenter.Presenter;

/**
 * サービスの抽象クラス
 *
 * @see jp.pioneer.carsync.presentation.view.service.ResourcefulService
 */

public abstract class AbstractService<P extends Presenter<V>, V> extends Service {
    private ServiceComponent mServiceComponent;

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate() {
        super.onCreate();
        mServiceComponent = createServiceComponent();
        doInject(mServiceComponent);
        doCreate();
        //noinspection unchecked
        getPresenter().takeView((V) this);
        getPresenter().initialize();
        getPresenter().resume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPresenter().pause();
        getPresenter().dropView();
        getPresenter().destroy();
    }

    protected abstract void doCreate();

    protected abstract void doInject(ServiceComponent serviceComponent);

    @NonNull
    protected abstract P getPresenter();

    private ComponentFactory getComponentFactory() {
        return App.getApp(this).getComponentFactory();
    }

    private ServiceComponent createServiceComponent() {
        return getComponentFactory().createServiceComponent(getPresenterComponent(), this);
    }

    private PresenterComponent getPresenterComponent() {
        return getComponentFactory().getPresenterComponent(App.getApp(this).getAppComponent(), getClass());
    }
}
