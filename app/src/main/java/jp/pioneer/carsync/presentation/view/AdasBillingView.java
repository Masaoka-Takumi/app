package jp.pioneer.carsync.presentation.view;

import android.support.annotation.StringRes;

/**
 * AdasBillingView
 */

public interface AdasBillingView {
    void setPurchaseBtn(boolean isPurchased);
    void setPriceText(String price);
    void setTrialButtonEnabled(boolean isEnabled);
    void setTrialButtonText(@StringRes int src);
}
