package jp.pioneer.carsync.infrastructure.component;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsApi;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.pioneer.carsync.domain.component.LocationProvider;
import jp.pioneer.carsync.domain.component.Resolver;
import timber.log.Timber;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * LocationProviderの実装.
 */
@SuppressFBWarnings(
        value = "IS2_INCONSISTENT_SYNC",
        justification = "mContextとmHandlerはDIされるため同期化出来ない")
public class LocationProviderImpl implements LocationProvider, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    @Inject Context mContext;
    @Inject Handler mHandler;
    private GoogleApiClient mGoogleApiClient;
    private WeakReference<Callback> mCallback;
    private int mPriority;
    private GetType mType;
    private Location mLastLocation;

    /**
     * コンストラクタ
     */
    @Inject
    public LocationProviderImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void startGetCurrentLocation(@NonNull Priority priority, @NonNull Callback callback, @NonNull GetType type) {
        checkNotNull(priority);
        checkNotNull(callback);
        checkNotNull(type);
        Timber.i("startGetCurrentLocation() priority = " + priority);

        if (mCallback != null && mCallback.get() != null) {
            Timber.e("startGetCurrentLocation() multiple access.");
            callback.onError(Error.MULTIPLE_ACCESS, null);
            return;
        }

        mType = type;

        mCallback = new WeakReference<>(callback);
        switch (priority) {
            case HIGH_ACCURACY:
                mPriority = LocationRequest.PRIORITY_HIGH_ACCURACY;
                break;
            case BALANCED_POWER_ACCURACY:
                mPriority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
                break;
            case LOW_POWER:
                mPriority = LocationRequest.PRIORITY_LOW_POWER;
                break;
            case NO_POWER:
                mPriority = LocationRequest.PRIORITY_NO_POWER;
                break;
            default:
                throw new IllegalArgumentException("not implemented. priority = " + priority);
        }
        mGoogleApiClient = getGoogleApiClient(this);
        mGoogleApiClient.connect();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void finishGetCurrentLocation() {
        Timber.i("finishGetCurrentLocation()");

        if (mGoogleApiClient != null) {
            getFusedLocationApi().removeLocationUpdates(mGoogleApiClient, this)
                    .setResultCallback(result -> {
                        if (!result.isSuccess()) {
                            Timber.w("finishGetCurrentLocation() removeLocationUpdates() result = " +
                                    result.getStatusCode());
                        }

                        disconnectGoogleApi();
                    });
        }

        if (mCallback != null) {
            mCallback = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Location getLastLocation(){
        return mLastLocation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        Timber.i("onConnected()");

        checkLocationSettings()
                .setResultCallback(result -> {
                    Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            requestLocationUpdates();
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            callbackError(new ResolverImpl(status));
                            break;
                        default:
                            Timber.e("onConnected() status = " + status.getStatusCode());
                            disconnectGoogleApi();
                            callbackError(Error.NOT_AVAILABLE);
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Timber.w("onConnectionSuspended() cause = " + cause);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.w("onConnectionFailed() cause = " + connectionResult.getErrorCode());
        mGoogleApiClient = null;
        callbackError(Error.NOT_AVAILABLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onLocationChanged(Location location) {
        Timber.d("onLocationChanged()");

        if (mType == GetType.SINGLE) {
            getFusedLocationApi().removeLocationUpdates(mGoogleApiClient, this)
                    .setResultCallback(result -> {
                        if (!result.isSuccess()) {
                            Timber.w("onLocationChanged() removeLocationUpdates() result = " +
                                    result.getStatusCode());
                        }

                        disconnectGoogleApi();
                        callbackSuccess(location);
                    });
        } else {
            mLastLocation = location;
        }
    }

    private synchronized PendingResult<LocationSettingsResult> checkLocationSettings() {
        return getSettingsApi().checkLocationSettings(mGoogleApiClient,
                new LocationSettingsRequest.Builder().addLocationRequest(createLocationRequest()).setAlwaysShow(true).build());
    }

    private boolean checkSelfPermission() {
        int result = ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result != PackageManager.PERMISSION_GRANTED) {
            Timber.e("checkSelfPermission() result = " + result);
            return false;
        }

        return true;
    }

    private synchronized void requestLocationUpdates() {
        if (!checkSelfPermission()) {
            disconnectGoogleApi();
            callbackError(Error.NOT_AVAILABLE);
            return;
        }

        getFusedLocationApi().requestLocationUpdates(mGoogleApiClient, createLocationRequest(), this)
                .setResultCallback(result -> {
                    if (!result.isSuccess()) {
                        Timber.e("onLocationChanged() requestLocationUpdates() result = "
                                + result.getStatusCode());
                        disconnectGoogleApi();
                        callbackError(Error.NOT_AVAILABLE);
                    }
                });
    }

    private synchronized void disconnectGoogleApi() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        mGoogleApiClient = null;
    }

    private synchronized void callbackSuccess(Location location) {
        Callback callback = getCallback(false);
        if (callback == null) {
            Timber.w("callbackSuccess() callback has been cleared.");
            return;
        }

        mHandler.post(() -> callback.onSuccess(location));
    }

    private synchronized void callbackError(Resolver resolver) {
        Callback callback = getCallback(false);
        if (callback == null) {
            Timber.w("callbackError() callback has been cleared.");
            return;
        }

        mHandler.post(() -> callback.onError(Error.RESOLUTION_REQUIRED, resolver));
    }

    private synchronized void callbackError(Error error) {
        Callback callback = getCallback(true);
        if (callback == null) {
            Timber.w("callbackError() callback has been cleared.");
            return;
        }

        mHandler.post(() -> callback.onError(error, null));
    }

    @Nullable
    private synchronized Callback getCallback(boolean isClear) {
        if (mCallback != null) {
            Callback callback = mCallback.get();
            if (mType == GetType.SINGLE || isClear) {
                mCallback = null;
            }
            return callback;
        }
        return null;
    }

    private LocationRequest createLocationRequest() {
        return LocationRequest.create()
                .setPriority(mPriority)
                .setInterval(mType.interval)
                .setFastestInterval(mType.fastInterval);
    }

    /**
     * GoogleApiClient生成.
     * <p>
     * UnitTest用
     *
     * @return GoogleApiClient
     */
    @VisibleForTesting
    GoogleApiClient getGoogleApiClient(LocationProviderImpl provider) {
        return new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * FusedLocationProviderApi生成
     * <p>
     * UnitTest用
     *
     * @return FusedLocationProviderApi
     */
    @VisibleForTesting
    FusedLocationProviderApi getFusedLocationApi() {
        return FusedLocationApi;
    }

    /**
     * SettingsApi生成
     * <p>
     * UnitTest用
     *
     * @return SettingsApi
     */
    @VisibleForTesting
    SettingsApi getSettingsApi() {
        return LocationServices.SettingsApi;
    }
}
