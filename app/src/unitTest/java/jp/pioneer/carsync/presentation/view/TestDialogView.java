package jp.pioneer.carsync.presentation.view;

/**
 * UnitTest用.
 */
public interface TestDialogView {
    void doCallback();

    interface Callback {
        void onCallback();
    }
}
