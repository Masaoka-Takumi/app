package jp.pioneer.carsync.presentation.view.widget;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ADAS View.
 * <p>
 * ADAS解析用のSurfaceView
 */
@SuppressWarnings("deprecation")
public class AdasView extends SurfaceView implements SurfaceHolder.Callback {

    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private static final int CAMERA_FPS_RANGE_MAX = 30;//fps
    private SurfaceHolder mPreviewSurfaceHolder;
    private static int mCapImageHeight = 720;
    private static int mCapImageWidth = 1280; // 1920*1080だと処理しきれず例外が発生してしまう
    private static AdasListener mListener = null;
    private static boolean isInitialized = false;
    private CameraDevice backCameraDevice;
    private CameraCaptureSession backCameraSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private OpenCameraCallback mOpenCameraCallback;
    private static ImageReader mImageReader;
    private Context mContext;
    private static Range<Integer> mFpsRange;
    private static int mDebugFps = 0;
    private static boolean mCameraView = false;
    /**
     * Orientation of the camera sensor
     */
    private static int mSensorOrientation;
    private static int mRotation;

    /**
     * The {@link android.util.Size} of camera preview.
     */
    private static Size mPreviewSize;


    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * This is the output file for our picture.
     */
    private File mPhotoDir;

    /**
     * コンストラクタ.
     *
     * @param context Context
     */
    public AdasView(Context context) {
        super(context);
        mContext = context;
        initSurfaceHolder();
    }

    /**
     * コンストラクタ
     *
     * @param context Context
     * @param attrs   AttributeSet
     */
    public AdasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initSurfaceHolder();
    }

    public void setDebugFps(int debugFps) {
        mDebugFps = debugFps;
    }

    public void setCameraView(boolean cameraView) {
        mCameraView = cameraView;
    }

    /**
     * 起動.
     * <p>
     * 本Viewを表示状態にすることでSurfaceViewが生成される。
     */
    public void startAdas() {
        if (mListener.isConfiguredCalibration()) {
            setVisibility(VISIBLE);
/*            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            mPhotoDir = new File(picturesDir, "SmartCarSync");
            if (!mPhotoDir.exists()) {
                mPhotoDir.mkdir();
            }*/
        }
    }

    /**
     * 停止.
     */
    public void stopAdas() {
        setVisibility(INVISIBLE);
    }

    /**
     * 更新.
     */
    public void updateAdas() {
        Timber.d("updateAdas");
        if (isInitialized) {
            isInitialized = false;
            mListener.onRelease();
            initAdasEngine();
        }
    }

    /**
     * リスナー設定.
     *
     * @param listener リスナー
     * @throws NullPointerException {@code listener}がnull
     */
    public void setListener(@NonNull AdasListener listener) {
        mListener = checkNotNull(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!isInitialized) {
            initCamera(holder);
            initAdasEngine();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (!isInitialized) {
            initCamera(holder);
            initAdasEngine();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        finishAdas();
    }

    private void finishAdas(){
        //カメラ起動の中止
        mOpenCameraCallback = null;
        // カメラセッションの終了
        if (backCameraSession != null) {
            try {
                backCameraSession.stopRepeating();
                backCameraSession.close();
            } catch (CameraAccessException e) {
                Timber.e(e.toString());
            } catch (IllegalStateException e) {
                Timber.e(e.toString());
            }
            backCameraSession = null;
        }

        // カメラデバイスとの切断
        if (backCameraDevice != null) {
            backCameraDevice.close();
            backCameraDevice = null;
        }

        if (null != mImageReader) {
            mImageReader.close();
            mImageReader = null;
        }
        // カメラデバイスとの切断後にスレッドを止める
        stopBackgroundThread();

        if (isInitialized) {
            mListener.onRelease();
            isInitialized = false;
        }
    }
    private void initAdasEngine() {
        isInitialized = mListener.onInitialize(mCapImageHeight, mCapImageWidth);
    }

    private void initSurfaceHolder() {

        mPreviewSurfaceHolder = getHolder();
        // 半透明を設定
        mPreviewSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        // このViewをトップにする
        setZOrderMediaOverlay(true);
        mPreviewSurfaceHolder.addCallback(this);
    }

    private void initCamera(SurfaceHolder holder) {
        Timber.d("initCamera");
        startBackgroundThread();
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);

        try {
            //カメラIDを取得（背面カメラを選択）
            String backCameraId = "";
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics chars
                        = manager.getCameraCharacteristics(cameraId);
                Integer facing = chars.get(CameraCharacteristics.LENS_FACING);

                if (facing != null && facing ==
                        CameraCharacteristics.LENS_FACING_BACK) {
                    backCameraId = cameraId;
                }
            }
            CameraCharacteristics characteristics
                    = manager.getCameraCharacteristics(backCameraId);
            StreamConfigurationMap map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            // For still image captures, we use the largest available size.
/*            Size largest = Collections.max(
                    Arrays.asList(map.getOutputSizes(ImageFormat.YUV_420_888)),
                    new CompareSizesByArea());*/
            Range<Integer>[] fpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            int lowerMax = 0;
            if (fpsRanges != null && fpsRanges.length > 0) {
                for (int i = 1; i < fpsRanges.length; i++) {
                    if (fpsRanges[i].getUpper() == CAMERA_FPS_RANGE_MAX) {
                        if (fpsRanges[i].getLower() > lowerMax) {
                            lowerMax = fpsRanges[i].getLower();
                            mFpsRange = Range.create(lowerMax, CAMERA_FPS_RANGE_MAX);
                        }
                    }
                    Timber.d("AvailableFpsRanges:(%d, %d)", fpsRanges[i
                            ].getLower(), fpsRanges[i].getUpper());
                }
            }
            WindowManager winMan = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            mImageReader = ImageReader.newInstance(mCapImageWidth, mCapImageHeight,
                    ImageFormat.YUV_420_888, /*maxImages*/2);
            mImageReader.setOnImageAvailableListener(
                    mOnImageAvailableListener, mBackgroundHandler);

            // Find out if we need to swap dimension to get the preview size relative to sensor
            // coordinate.
            int displayRotation = winMan.getDefaultDisplay().getRotation();
            //noinspection ConstantConditions
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            boolean swappedDimensions = false;
            switch (displayRotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                        swappedDimensions = true;
                    }
                    break;
                default:
                    Timber.e( "Display rotation is invalid: " + displayRotation);
            }
            Timber.d("Display rotation is " + displayRotation);
            Point displaySize = new Point();
            winMan.getDefaultDisplay().getSize(displaySize);
            int width = displaySize.x;
            int height = displaySize.y;
            int rotatedPreviewWidth = width;
            int rotatedPreviewHeight = height;
            int maxPreviewWidth = displaySize.x;
            int maxPreviewHeight = displaySize.y;

            if (swappedDimensions) {
                rotatedPreviewWidth = height;
                rotatedPreviewHeight = width;
                maxPreviewWidth = displaySize.y;
                maxPreviewHeight = displaySize.x;
            }

            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                maxPreviewWidth = MAX_PREVIEW_WIDTH;
            }

            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                maxPreviewHeight = MAX_PREVIEW_HEIGHT;
            }
            Size aspectSize = new Size(mCapImageWidth, mCapImageHeight);
            // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
            // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
            // garbage capture data.
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(ImageFormat.YUV_420_888),
                    rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                    maxPreviewHeight, aspectSize);
            ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) this.getLayoutParams();

            // We fit the aspect ratio of TextureView to the size of preview we picked.
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                lp.width = mPreviewSize.getWidth(); //横幅
                lp.height = mPreviewSize.getHeight(); //縦幅*/
            } else {
                lp.width = mPreviewSize.getHeight(); //縦幅*/
                lp.height = mPreviewSize.getWidth(); //横幅

            }
            //SurfaceViewは画面サイズ
            lp.width = width;
            lp.height = height;
            this.setLayoutParams(lp);
            //出力サイズ固定
            mPreviewSurfaceHolder.setFixedSize(mCapImageWidth, mCapImageHeight);
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            // カメラオープン(オープンに成功したときに第2引数のコールバッククラスが呼ばれる)
            mOpenCameraCallback = new OpenCameraCallback();
            manager.openCamera(backCameraId, mOpenCameraCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            //例外処理を記述
            Timber.e(e.toString());
            finishAdas();
            mListener.onError();
        } catch (IllegalArgumentException e){
            //例外処理を記述
            Timber.e(e.toString());
            finishAdas();
            mListener.onError();
        }
    }

    /**
     * Compares two {@code Size}s based on their areas.
     */
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    /**
     * カメラデバイス接続完了後のコールバッククラス
     */
    private class OpenCameraCallback extends CameraDevice.StateCallback {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            backCameraDevice = cameraDevice;

            try {
                // プレビューリクエストの設定（SurfaceViewをターゲットに）
                mPreviewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                mPreviewRequestBuilder.addTarget(mImageReader.getSurface());

                // キャプチャーセッションの開始(セッション開始後に第2引数のコールバッククラスが呼ばれる)
                if(mCameraView) {
                    mPreviewRequestBuilder.addTarget(mPreviewSurfaceHolder.getSurface());
                    cameraDevice.createCaptureSession(Arrays.asList(mPreviewSurfaceHolder.getSurface(), mImageReader.getSurface()), new CameraCaptureSessionCallback(), mBackgroundHandler);
                }else {
                    cameraDevice.createCaptureSession(Collections.singletonList(mImageReader.getSurface()), new CameraCaptureSessionCallback(), mBackgroundHandler);
                }
            } catch (CameraAccessException e) {
                // エラー時の処理を記載
                Timber.e(e.toString());
                finishAdas();
                mListener.onError();
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            // 切断時の処理を記載
            Timber.e("CameraDevice onDisconnected" );
            finishAdas();
            mListener.onError();
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            // エラー時の処理を記載
            Timber.e("CameraDevice onError" );
            finishAdas();
            mListener.onError();
        }
    }

    /**
     * カメラが起動し使える状態になったら呼ばれるコールバック
     */
    private class CameraCaptureSessionCallback extends CameraCaptureSession.StateCallback {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            backCameraSession = session;

            try {
                // Auto focus should be continuous for camera preview.
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                if(mDebugFps!=0){
                    mFpsRange = Range.create(mDebugFps,mDebugFps);
                }
                if (mFpsRange == null) {
                    mFpsRange = Range.create(CAMERA_FPS_RANGE_MAX, CAMERA_FPS_RANGE_MAX);
                }
                Timber.d("fpsRange:(%d, %d)", mFpsRange.getLower(), mFpsRange.getUpper());
                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, mFpsRange);
                // プレビューの開始(撮影時に第2引数のコールバッククラスが呼ばれる)
                session.setRepeatingRequest(mPreviewRequestBuilder.build(), new CaptureCallback(), mBackgroundHandler);

            } catch (CameraAccessException e) {
                Timber.e(e.toString());
                finishAdas();
                mListener.onError();
            } catch (IllegalStateException e){
                Timber.e(e.toString());
                finishAdas();
                mListener.onError();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            //失敗時の処理を記載
            Timber.e("CameraCaptureSession onConfigureFailed" );
            finishAdas();
            mListener.onError();
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        if(mBackgroundThread!=null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {
        private int frames = 0;
        private long initialTime = SystemClock.elapsedRealtimeNanos();

        @Override
        public void onImageAvailable(ImageReader reader) {
            WindowManager winMan = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = winMan.getDefaultDisplay();
            mRotation = getOrientation(display.getRotation());
            //非同期処理にする場合
/*            Image image = reader.acquireNextImage();
            try {
                byte[] bytes = YUV_420_888toNV21(image);
                new ImageRotateTask(mContext, mPhotoDir, bytes).execute();
            } catch (OutOfMemoryError e) {
                //代替の処理
                Timber.e(e.toString());
            }
            image.close();*/
            //fps出力
/*            frames++;
            if ((frames % 30) == 0) {
                long currentTime = SystemClock.elapsedRealtimeNanos();
                long fps = Math.round(frames * 1e9 / (currentTime - initialTime));
                Timber.d("frame# : " + frames + ", approximately " + fps + " fps");
                frames = 0;
                initialTime = SystemClock.elapsedRealtimeNanos();
            }*/
            Image image = reader.acquireNextImage();
            if (image != null) {
                try {
                    byte[] bytes = YUV_420_888toNV21(image);
                    //上下逆の場合画像を180度回転
                    if (mRotation == 180) {
                        bytes = rotateNV21(bytes, mCapImageWidth, mCapImageHeight, 180);
                    }
                    if (isInitialized) {
                        mListener.onProcess(bytes);
                    }
                } catch (OutOfMemoryError e) {
                    Timber.e(e.toString());
                }catch (IllegalStateException e){
                    Timber.e(e.toString());
                }
                image.close();
            }
        }
    };

    private static class ImageRotateTask extends AsyncTask<Void, Void, Boolean> {

        private File mFile;
        private byte[] mBytes;

        public ImageRotateTask(Context context, File dir, byte[] bytes) {
            mFile = new File(dir, System.currentTimeMillis() + ".jpg");
            mBytes = bytes;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            byte[] rotateBytes;
            if (mRotation == 180) {
                try {
                    rotateBytes = rotateNV21(mBytes, mCapImageWidth, mCapImageHeight, 180);
                } catch (OutOfMemoryError e) {
                    //代替の処理
                    rotateBytes = mBytes;
                }
            } else {
                rotateBytes = mBytes;
            }
            if (isInitialized) {
                mListener.onProcess(rotateBytes);
            }
            try {
                new FileOutputStream(mFile).write(NV21toJPEG(rotateBytes, mCapImageWidth, mCapImageHeight, 50));
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
        }
    }

    private static byte[] NV21toJPEG(byte[] nv21, int width, int height, int quality) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        yuv.compressToJpeg(new Rect(0, 0, width, height), quality, out);
        return out.toByteArray();
    }

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    /**
     * カメラ撮影時に呼ばれるコールバック関数
     */
    private class CaptureCallback extends CameraCaptureSession.CaptureCallback {
    }

    private static byte[] YUV_420_888toNV21(Image image) {
        byte[] nv21;
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        nv21 = new byte[ySize + uSize + vSize];

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        return nv21;
    }

    public static byte[] rotateNV21(byte[] input, int width, int height, int rotation) {
        byte[] output;
        boolean swap = (rotation == 90 || rotation == 270);
        boolean yflip = (rotation == 90 || rotation == 180);
        boolean xflip = (rotation == 270 || rotation == 180);

        output = new byte[input.length];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int xo = x, yo = y;
                int w = width, h = height;
                int xi = xo, yi = yo;
                if (swap) {
                    xi = w * yo / h;
                    yi = h * xo / w;
                }
                if (yflip) {
                    yi = h - yi - 1;
                }
                if (xflip) {
                    xi = w - xi - 1;
                }
                output[w * yo + xo] = input[w * yi + xi];
                int fs = w * h;
                int qs = (fs >> 2);
                xi = (xi >> 1);
                yi = (yi >> 1);
                xo = (xo >> 1);
                yo = (yo >> 1);
                w = (w >> 1);
                h = (h >> 1);
                // adjust for interleave here
                int ui = fs + (w * yi + xi) * 2;
                int uo = fs + (w * yo + xo) * 2;
                // and here
                int vi = ui + 1;
                int vo = uo + 1;
                output[uo] = input[ui];
                output[vo] = input[vi];
            }
        }
        return output;
    }


    /**
     * Given {@code choices} of {@code Size}s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices           The list of sizes that the camera supports for the intended output
     *                          class
     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth          The maximum width that can be chosen
     * @param maxHeight         The maximum height that can be chosen
     * @param aspectRatio       The aspect ratio
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Timber.e("Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * ADASリスナー.
     * <p>
     * ADASの処理を実施したい場合に呼ぶリスナー
     */
    public interface AdasListener {

        /**
         * 初期化
         *
         * @param cameraImageHeight カメラ画素数高さ
         * @param cameraImageWidth  カメラ画素数幅
         * @return 初期化に成功したか否か {@code true}:初期化に成功 {@code false}:初期化に失敗
         */
        boolean onInitialize(int cameraImageHeight, int cameraImageWidth);

        /**
         * 解放
         */
        void onRelease();

        /**
         * エラー
         */
        void onError();

        /**
         * 解析
         *
         * @param data 画像データ
         */
        void onProcess(byte[] data);

        /**
         * キャリブレーション設定確認
         *
         * @return キャリブレーション設定済か否か {@code true}:設定済 {@code false}:未設定
         */
        boolean isConfiguredCalibration();
    }
}
