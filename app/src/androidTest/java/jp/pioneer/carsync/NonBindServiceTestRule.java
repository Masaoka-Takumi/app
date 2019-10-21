package jp.pioneer.carsync;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import jp.pioneer.carsync.application.TestApp;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * バインドしないサービス用のテストルール.

 * @param <T> サービス
 */
public class NonBindServiceTestRule<T extends Service> implements TestRule {
    private static final long DEFAULT_WAITING = 250;
    private Class<T> mServiceClass;
    private boolean mStartService;
    private boolean mStarted;
    private Instrumentation mInstrumentation;

    public NonBindServiceTestRule(@NonNull Class<T> serviceClass) {
        this(serviceClass, false);
    }

    public NonBindServiceTestRule(@NonNull Class<T> serviceClass, boolean startService) {
        mServiceClass = checkNotNull(serviceClass);
        mStartService = startService;
        mInstrumentation = InstrumentationRegistry.getInstrumentation();
    }

    public TestApp getTestApp() {
        return (TestApp) InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext()
                .getApplicationContext();
    }

    public boolean startService() {
        Context context = mInstrumentation.getTargetContext();
        Intent intent = new Intent(context, mServiceClass);
        mStarted = (context.startService(intent) != null);
        if (mStarted) {
            waiting();
        }

        return mStarted;
    }

    public boolean startService(@NonNull Intent intent) {
        checkNotNull(intent);
        Context context = mInstrumentation.getTargetContext();
        mStarted = (context.startService(intent) != null);
        if (mStarted) {
            waiting();
        }

        return mStarted;
    }

    public void stopService() {
        if (mStarted) {
            Context context = mInstrumentation.getTargetContext();
            Intent intent = new Intent(context, mServiceClass);
            context.stopService(intent);
            waiting();
        }
    }

    @NonNull
    public Intent createIntent() {
        Context context = mInstrumentation.getTargetContext();
        Intent intent = new Intent(context, mServiceClass);
        return intent;
    }

    public void waiting() {
        waiting(DEFAULT_WAITING);
    }

    public void waiting(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    if (mStartService) {
                        startService();
                    }
                    base.evaluate();
                } finally {
                    stopService();
                }
            }
        };
    }
}
