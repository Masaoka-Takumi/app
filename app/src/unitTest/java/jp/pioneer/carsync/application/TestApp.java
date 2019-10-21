package jp.pioneer.carsync.application;

import android.content.Context;
import android.support.multidex.MultiDex;

import org.greenrobot.eventbus.EventBus;

import jp.pioneer.carsync.application.di.component.AppComponent;
import jp.pioneer.carsync.application.factory.ComponentFactory;

import static org.mockito.Mockito.mock;

/**
 * UnitTest用アプリケーションクラス.
 */
public class TestApp extends App {
    /**
     * {@inheritDoc}
     * <p>
     * メソッド数が65535を超えているためminifyを適用しようとしたが、
     * ビルドに失敗するため、諦めてMultiDexで行うことにした。
     *
     * @param base Context
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * {@inheritDoc}
     * <p>
     * とりあえず{@link ComponentFactory}と{@link AppComponent}は
     * mockオブジェクトを設定。
     */
    @Override
    void initialize() {
        setComponentFactory(mock(ComponentFactory.class));
        setAppComponent(mock(AppComponent.class));
        mEventBus = mock(EventBus.class);
    }

    @Override
    public void startFlurry() {

    }
}
