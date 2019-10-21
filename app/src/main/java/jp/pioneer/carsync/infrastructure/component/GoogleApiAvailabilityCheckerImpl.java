package jp.pioneer.carsync.infrastructure.component;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.GooglePlayServicesAvailabilityChecker;
import timber.log.Timber;

/**
 * GooglePlayServicesAvailabilityCheckerの実装.
 */
public class GoogleApiAvailabilityCheckerImpl implements GooglePlayServicesAvailabilityChecker {
    @Inject Context mContext;

    static class ResultImpl implements Result {
        private Context mContext;
        private int mErrorCode;

        /**
         * コンストラクタ.
         *
         * @param context コンテキスト
         * @param errorCode エラーコード。{@link GoogleApiAvailability#isGooglePlayServicesAvailable(Context)}の戻り値。
         */
        ResultImpl(Context context, int errorCode) {
            mContext = context;
            mErrorCode = errorCode;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isAvailable() {
            return (mErrorCode == ConnectionResult.SUCCESS);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean resolve() {
            GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
            if (availability.isUserResolvableError(mErrorCode)) {
                availability.showErrorNotification(mContext, mErrorCode);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * コンストラクタ.
     */
    @Inject
    public GoogleApiAvailabilityCheckerImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Result doCheck() {
        Timber.i("doCheck()");

        return new ResultImpl(mContext, GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(mContext));
    }
}
