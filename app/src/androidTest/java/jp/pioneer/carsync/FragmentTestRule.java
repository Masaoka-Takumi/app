package jp.pioneer.carsync;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import jp.pioneer.carsync.application.TestApp;
import jp.pioneer.carsync.presentation.view.activity.TestAbstractActivity;
import jp.pioneer.carsync.presentation.view.activity.TestActivity;

/**
 * Created by BP06565 on 2017/02/02.
 */

public abstract class FragmentTestRule<F extends Fragment> extends ActivityTestRule<TestActivity> {
    private static final long DEFAULT_WAITING = 300;
    private F mFragment;

    public FragmentTestRule() {
        super(TestActivity.class, true, false);
        mFragment = createDialogFragment();
    }

    @Override
    protected void afterActivityLaunched() {
        super.afterActivityLaunched();

        getActivity().runOnUiThread(() -> {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.container, mFragment);
            transaction.commit();
        });

        waiting();
    }

    public void remove() {
        getActivity().runOnUiThread(() -> {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.remove(mFragment);
            transaction.commit();
        });

        waiting();
    }

    public F getFragment() {
        return mFragment;
    }

    public void waiting(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void waiting() {
        waiting(DEFAULT_WAITING);
    }

    public void finishActivityIfNecessary() {
        if (!getActivity().isFinishing()) {
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            Instrumentation.ActivityMonitor monitor = instr.addMonitor(TestActivity.class.getName(), null, false);
            getActivity().finish();
            monitor.waitForActivity();
            waiting(100);
        }
    }

    public static TestApp getTestApp() {
        return (TestApp) InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext()
                .getApplicationContext();
    }

    protected abstract F createDialogFragment();
}
