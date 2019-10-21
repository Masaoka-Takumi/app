package jp.pioneer.carsync;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import jp.pioneer.carsync.application.TestApp;
import jp.pioneer.carsync.presentation.view.activity.TestActivity;

/**
 * Created by BP06565 on 2017/02/02.
 */

public abstract class DialogFragmentTestRule<F extends DialogFragment> extends ActivityTestRule<TestActivity> {
    private static final long DEFAULT_WAITING = 300;
    private F mFragment;

    public DialogFragmentTestRule() {
        super(TestActivity.class, true, false);
        mFragment = createDialogFragment();
    }

    @Override
    protected void afterActivityLaunched() {
        super.afterActivityLaunched();

        getActivity().runOnUiThread(() -> {
            FragmentManager manager = getActivity().getSupportFragmentManager();
            mFragment.show(manager, "dialog");
        });

        waiting();
    }

    public void dismiss() {
        mFragment.dismiss();
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