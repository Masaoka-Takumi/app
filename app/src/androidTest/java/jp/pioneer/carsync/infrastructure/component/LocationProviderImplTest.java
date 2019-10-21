package jp.pioneer.carsync.infrastructure.component;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.internal.zzary;
import com.google.android.gms.internal.zzasp;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsApi;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.domain.component.LocationProvider;
import jp.pioneer.carsync.domain.component.Resolver;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/04/21.
 */
@SuppressWarnings("unchecked")
@RunWith(Theories.class)
public class LocationProviderImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks LocationProviderImpl mLocationProviderImpl = new LocationProviderImpl() {
        @Override
        GoogleApiClient getGoogleApiClient(LocationProviderImpl provider) {
            mConnectionCallbacks = provider;
            mConnectionFailedListener = provider;
            if (mIsCallOnConnect) {
                mMainHandler.post(() -> mConnectionCallbacks.onConnected(null));
            }
            if (mIsCallOnConnectionFailed) {
                mMainHandler.post(() -> mConnectionFailedListener.onConnectionFailed(new ConnectionResult(ConnectionResult.CANCELED)));
            }
            return mGoogleApiClient;
        }

        @Override
        FusedLocationProviderApi getFusedLocationApi() {
            return mFusedLocationProviderApi;
        }

        @Override
        SettingsApi getSettingsApi() {
            return mSettingsApi;
        }

    };
    @Mock Context mContext;
    @Mock Handler mHandler;

    boolean mIsCallOnConnect;
    boolean mIsCallOnConnectionFailed;

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal;

    GoogleApiClient mGoogleApiClient;
    FusedLocationProviderApi mFusedLocationProviderApi;
    SettingsApi mSettingsApi;

    GoogleApiClient.ConnectionCallbacks mConnectionCallbacks;
    GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener;

    Location mLocation;
    ArgumentCaptor<LocationRequest> mLocationRequestArgumentCaptor;
    LocationProvider.Callback mCallback;

    PendingResult mPendingResultForCheckLocationSettings;
    PendingResult mPendingResultForRequestLocationUpdates;
    PendingResult mPendingResultForRemoveLocationUpdates;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mIsCallOnConnect = true;
        mIsCallOnConnectionFailed = false;

        mLocationRequestArgumentCaptor = ArgumentCaptor.forClass(LocationRequest.class);
        mGoogleApiClient = mock(GoogleApiClient.class);
        mLocation = mock(Location.class);
        mFusedLocationProviderApi = mock(zzary.class);
        mSettingsApi = mock(zzasp.class);
        mCallback = mock(LocationProvider.Callback.class);

        mPendingResultForCheckLocationSettings = mock(PendingResult.class);
        mPendingResultForRequestLocationUpdates = mock(PendingResult.class);
        mPendingResultForRemoveLocationUpdates = mock(PendingResult.class);

        // Handler#post(Runnable)がfinal methodなので、止むを得ずsendMessageAtTimeを使用
        when(mHandler.sendMessageAtTime(any(Message.class), anyLong())).then(invocationOnMock -> {
            Message message = (Message) invocationOnMock.getArguments()[0];
            mMainHandler.post(() -> {
                message.getCallback().run();
                mSignal.countDown();
            });
            return true;
        });
        mSignal = new CountDownLatch(1);

        when(mGoogleApiClient.isConnected()).thenReturn(true);
        when(mContext.checkPermission(anyString(), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_GRANTED);

        when(mSettingsApi.checkLocationSettings
                (eq(mGoogleApiClient), any(LocationSettingsRequest.class))).thenReturn(mPendingResultForCheckLocationSettings);
        when(mFusedLocationProviderApi.requestLocationUpdates
                (eq(mGoogleApiClient), mLocationRequestArgumentCaptor.capture(), eq(mLocationProviderImpl))).thenReturn(mPendingResultForRequestLocationUpdates);
        when(mFusedLocationProviderApi.removeLocationUpdates
                (eq(mGoogleApiClient), eq(mLocationProviderImpl))).thenReturn(mPendingResultForRemoveLocationUpdates);

    }

    static class LocationPriorityAndRequestFixture {
        LocationProvider.Priority priority;
        int result;

        LocationPriorityAndRequestFixture(LocationProvider.Priority priority, int result) {
            this.priority = priority;
            this.result = result;
        }
    }

    @DataPoints
    public static final LocationPriorityAndRequestFixture[] UNAVAILABLE_FIXTURES = new LocationPriorityAndRequestFixture[]{
            new LocationPriorityAndRequestFixture(LocationProvider.Priority.HIGH_ACCURACY, LocationRequest.PRIORITY_HIGH_ACCURACY),
            new LocationPriorityAndRequestFixture(LocationProvider.Priority.BALANCED_POWER_ACCURACY, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY),
            new LocationPriorityAndRequestFixture(LocationProvider.Priority.LOW_POWER, LocationRequest.PRIORITY_LOW_POWER),
            new LocationPriorityAndRequestFixture(LocationProvider.Priority.NO_POWER, LocationRequest.PRIORITY_NO_POWER)
    };

    @Theory
    public void getCurrentLocation_HappyPath(LocationPriorityAndRequestFixture fixture) throws Exception {
        // setup
        Status status = new Status(LocationSettingsStatusCodes.SUCCESS);
        LocationSettingsResult locationSettingsResult = new LocationSettingsResult(status);

        doAnswer(
                invocationOnMock -> {
                    ResultCallback resultCallback = (ResultCallback)invocationOnMock.getArguments()[0];
                    mMainHandler.post(() -> resultCallback.onResult(locationSettingsResult));
                    return null;
                }).when(mPendingResultForCheckLocationSettings).setResultCallback(any(ResultCallback.class));

        doAnswer(
                invocationOnMock -> {
                    ResultCallback resultCallback = (ResultCallback)invocationOnMock.getArguments()[0];
                    mMainHandler.post(() -> resultCallback.onResult(status));

                    LocationListener listener = mLocationProviderImpl;
                    mMainHandler.post(() -> listener.onLocationChanged(mLocation));
                    return null;
                }).when(mPendingResultForRequestLocationUpdates).setResultCallback(any(ResultCallback.class));

        doAnswer(
                invocationOnMock -> {
                    ResultCallback resultCallback = (ResultCallback)invocationOnMock.getArguments()[0];
                    mMainHandler.post(() -> resultCallback.onResult(status));
                    return null;
                }).when(mPendingResultForRemoveLocationUpdates).setResultCallback(any(ResultCallback.class));


        // exercise
        mLocationProviderImpl.startGetCurrentLocation(fixture.priority, mCallback, LocationProvider.GetType.SINGLE);
        mSignal.await();

        // verify
        verify(mCallback,times(1)).onSuccess(any(Location.class));

        LocationRequest locationRequest = mLocationRequestArgumentCaptor.getValue();
        int actual = locationRequest.getPriority();
        assertThat(actual, is(fixture.result));

    }

    @Test
    public void getCurrentLocation_CheckLocationSettings_Result_RESOLUTION_REQUIRED() throws Exception {
        // setup
        Status status = new Status(LocationSettingsStatusCodes.RESOLUTION_REQUIRED);
        LocationSettingsResult locationSettingsResult = new LocationSettingsResult(status);

        doAnswer(
                invocationOnMock -> {
                    ResultCallback resultCallback = (ResultCallback)invocationOnMock.getArguments()[0];
                    mMainHandler.post(() -> resultCallback.onResult(locationSettingsResult));
                    return null;
                }).when(mPendingResultForCheckLocationSettings).setResultCallback(any(ResultCallback.class));

        // exercise
        mLocationProviderImpl.startGetCurrentLocation(LocationProvider.Priority.HIGH_ACCURACY, mCallback, LocationProvider.GetType.SINGLE);
        mSignal.await();

        // verify
        verify(mCallback,times(1)).onError(eq(LocationProvider.Error.RESOLUTION_REQUIRED), any(Resolver.class));

    }

    @Test
    public void getCurrentLocation_CheckLocationSettings_Result_OTHER() throws Exception {
        // setup
        Status status = new Status(LocationSettingsStatusCodes.ERROR);
        LocationSettingsResult locationSettingsResult = new LocationSettingsResult(status);

        doAnswer(
                invocationOnMock -> {
                    ResultCallback resultCallback = (ResultCallback)invocationOnMock.getArguments()[0];
                    mMainHandler.post(() -> resultCallback.onResult(locationSettingsResult));
                    return null;
                }).when(mPendingResultForCheckLocationSettings).setResultCallback(any(ResultCallback.class));


        // exercise
        mLocationProviderImpl.startGetCurrentLocation(LocationProvider.Priority.HIGH_ACCURACY, mCallback, LocationProvider.GetType.SINGLE);
        mSignal.await();

        // verify
        verify(mCallback,times(1)).onError(LocationProvider.Error.NOT_AVAILABLE, null);
        verify(mGoogleApiClient).isConnected();
        verify(mGoogleApiClient).disconnect();

    }

    @Test
    public void getCurrentLocation_RequestLocationUpdates_CheckSelfPermission_False() throws Exception {
        // setup
        Status status = new Status(LocationSettingsStatusCodes.SUCCESS);
        LocationSettingsResult locationSettingsResult = new LocationSettingsResult(status);

        doAnswer(
                invocationOnMock -> {
                    ResultCallback resultCallback = (ResultCallback)invocationOnMock.getArguments()[0];
                    mMainHandler.post(() -> resultCallback.onResult(locationSettingsResult));
                    return null;
                }).when(mPendingResultForCheckLocationSettings).setResultCallback(any(ResultCallback.class));

        when(mContext.checkPermission(anyString(), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_DENIED);

        // exercise
        mLocationProviderImpl.startGetCurrentLocation(LocationProvider.Priority.HIGH_ACCURACY, mCallback, LocationProvider.GetType.SINGLE);
        mSignal.await();

        // verify
        verify(mCallback,times(1)).onError(LocationProvider.Error.NOT_AVAILABLE, null);
        verify(mGoogleApiClient).isConnected();
        verify(mGoogleApiClient).disconnect();

    }

    @Theory
    public void getCurrentLocation_RequestLocationUpdates_Result_False(LocationPriorityAndRequestFixture fixture) throws Exception {
        // setup
        Status statusForSuccess = new Status(LocationSettingsStatusCodes.SUCCESS);
        LocationSettingsResult locationSettingsResult = new LocationSettingsResult(statusForSuccess);

        doAnswer(
                invocationOnMock -> {
                    ResultCallback resultCallback = (ResultCallback)invocationOnMock.getArguments()[0];
                    mMainHandler.post(() -> resultCallback.onResult(locationSettingsResult));
                    return null;
                }).when(mPendingResultForCheckLocationSettings).setResultCallback(any(ResultCallback.class));

        Status statusForError = new Status(LocationSettingsStatusCodes.ERROR);
        doAnswer(
                invocationOnMock -> {
                    ResultCallback resultCallback = (ResultCallback)invocationOnMock.getArguments()[0];
                    mMainHandler.post(() -> resultCallback.onResult(statusForError));
                    return null;
                }).when(mPendingResultForRequestLocationUpdates).setResultCallback(any(ResultCallback.class));

        // exercise
        mLocationProviderImpl.startGetCurrentLocation(fixture.priority, mCallback, LocationProvider.GetType.SINGLE);
        mSignal.await();

        // verify
        verify(mCallback,times(1)).onError(LocationProvider.Error.NOT_AVAILABLE, null);
        verify(mGoogleApiClient).isConnected();
        verify(mGoogleApiClient).disconnect();

        LocationRequest locationRequest = mLocationRequestArgumentCaptor.getValue();
        int actual = locationRequest.getPriority();
        assertThat(actual, is(fixture.result));


    }

    @Test
    public void getCurrentLocation_ConnectionFailed() throws Exception {
        // setup
        mIsCallOnConnect = false;
        mIsCallOnConnectionFailed = true;

        // exercise
        mLocationProviderImpl.startGetCurrentLocation(LocationProvider.Priority.HIGH_ACCURACY, mCallback, LocationProvider.GetType.SINGLE);
        mSignal.await();

        // verify
        verify(mCallback,times(1)).onError(LocationProvider.Error.NOT_AVAILABLE, null);

    }

    @Test(expected = NullPointerException.class)
    public void getCurrentLocation_ArgPriorityNull() throws Exception {
        // setup
        LocationProvider.Priority priority = null;

        // exercise
        mLocationProviderImpl.startGetCurrentLocation(priority, mCallback, LocationProvider.GetType.SINGLE);

    }

    @Test(expected = NullPointerException.class)
    public void getCurrentLocation_ArgCallbackNull() throws Exception {
        // setup
        mCallback = null;
        LocationProvider.Priority priority = LocationProvider.Priority.HIGH_ACCURACY;

        // exercise
        mLocationProviderImpl.startGetCurrentLocation(priority, mCallback, LocationProvider.GetType.SINGLE);

    }
}