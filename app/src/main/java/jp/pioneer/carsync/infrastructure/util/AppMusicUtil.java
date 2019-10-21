package jp.pioneer.carsync.infrastructure.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.SystemClock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import timber.log.Timber;

/**
 * App Music ユーティリティー.
 */
public class AppMusicUtil {
    /** 拍手歓声フォルダ名. */
    public static final String APPLAUSE_FOLDER_NAME = "applause";

    private static final float MINIMUM_STEP = -12.0f;
    private static final float MAXIMUM_STEP = 12.0f;

    /**
     * SeekCalculator生成.
     *
     * @return SeekCalculator
     */
    public static SeekCalculator createSeekCalculator() {
        return new SeekCalculatorImpl();
    }

    /**
     * 拍手歓声機能使用準備.
     * <p>
     * 拍手歓声機能を使用するために、
     * assetフォルダに存在する拍手歓声ファイルをストレージに展開する
     *
     * @param context     コンテキスト
     * @param applauseDir 展開先のdir
     * @throws IOException 展開先のディレクトリにアクセスできない
     */
    public static void prepareApplause(Context context, String applauseDir) throws IOException {
        File applauseDirectory = new File(applauseDir);
        if (!applauseDirectory.exists() && !applauseDirectory.mkdir()) {
            throw new IOException("mkdir(" + applauseDir + ") failed.");
        }

        AssetManager assetManager = context.getResources().getAssets();

        // assetからapplauseフォルダを読込み
        String[] fileList = assetManager.list(APPLAUSE_FOLDER_NAME);
        if (fileList == null || fileList.length == 0) {
            // フォルダ内に拍手歓声ファイルが存在しない
            Timber.d("prepareApplause() file not found");
            return;
        }

        File[] listFiles = applauseDirectory.listFiles();
        if (listFiles != null) {
            if(listFiles.length == fileList.length) {
                // 拍手歓声ファイルが既に展開されている
                Timber.d("prepareApplause() file exists");
                return;
            }
        }


        // 拍手歓声ファイルを展開
        for (String file : fileList) {
            InputStream input = null;
            FileOutputStream output = null;
            try {

                input = assetManager.open(APPLAUSE_FOLDER_NAME + "/" + file);
                output = new FileOutputStream(applauseDir + "/" + file);

                int DEFAULT_BUFFER_SIZE = 1024 * 4;

                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int n;
                while (-1 != (n = input.read(buffer))) {
                    output.write(buffer, 0, n);
                }
            } finally {
                if (output != null) {
                    output.close();
                }
                if (input != null) {
                    input.close();
                }
            }

        }
    }

    /**
     * STEP値変換(車載機→MLE).
     * <p>
     * 車載機のSTEP値からMLE向けのSTEP値へ変換する
     *
     * @param carDeviceMaxStep 車載機の最大STEP値
     * @param carDeviceMinStep 車載機の最小STEP値
     * @param bands            変換対象のband値
     * @return STEPを変換したband値
     */
    public static float[] convertStepValueCarDeviceToMle(int carDeviceMaxStep, int carDeviceMinStep, float[] bands) {
        float minStepRatio = MINIMUM_STEP / (float) carDeviceMinStep;
        float maxStepRatio = MAXIMUM_STEP / (float) carDeviceMaxStep;

        float[] convertBands = new float[bands.length];
        for (int index = 0; index < bands.length; index++) {
            if (bands[index] > 0) {
                convertBands[index] = bands[index] * maxStepRatio;
            } else if (bands[index] < 0) {
                convertBands[index] = bands[index] * minStepRatio;
            } else {
                convertBands[index] = 0;
            }
        }

        return convertBands;
    }

    /**
     * STEP値変換(MLE→車載機).
     * <p>
     * MLEのSTEP値から車載機向けのSTEP値へ変換する
     *
     * @param carDeviceMaxStep 車載機の最大STEP値
     * @param carDeviceMinStep 車載機の最小STEP値
     * @param bands            変換対象のband値
     * @return STEPを変換したband値
     */
    public static int[] convertStepValueMleToCarDevice(int carDeviceMaxStep, int carDeviceMinStep, float[] bands) {
        float minStepRatio = (float) carDeviceMinStep / MINIMUM_STEP;
        float maxStepRatio = (float) carDeviceMaxStep / MAXIMUM_STEP;

        int[] convertBands = new int[bands.length];
        for (int index = 0; index < bands.length; index++) {
            if (bands[index] > 0) {
                BigDecimal bd = new BigDecimal(bands[index] * maxStepRatio);
                convertBands[index] = bd.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            } else if (bands[index] < 0) {
                BigDecimal bd = new BigDecimal(bands[index] * minStepRatio);
                convertBands[index] = bd.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            } else {
                convertBands[index] = 0;
            }
        }

        return convertBands;
    }

    /**
     * シークカリキュレーター.
     * <p>
     * 早送り、巻き戻しのシーク計算に使用する
     */
    public interface SeekCalculator {

        /**
         * 早送りの初回か否か取得.
         *
         * @param now 現在の時間
         * @return 早送りの初回か否か
         */
        boolean isFirstFastForward(long now);

        /**
         * 巻き戻しの初回か否か取得.
         *
         * @param now 現在の時間
         * @return 巻き戻しの初回か否か
         */
        boolean isFirstRewind(long now);

        /**
         * 早送り.
         *
         * @return 加算するシーク時間(ミリ秒)
         */
        int fastForward();

        /**
         * 巻き戻し.
         *
         * @return 減算するシーク時間(ミリ秒)
         */
        int rewind();
    }

    /**
     * SeekCalculatorの実装.
     */
    public static class SeekCalculatorImpl implements SeekCalculator {
        private int mInitialStep = 1000;
        private int mMaxStep = 10000;
        private int mContinuousTime = 1000;
        private double mStepMultiplier = 1.5;

        private int mFastForwardCount;
        private int mRewindCount;

        private long mLastFastForward;
        private long mLastRewind;

        /**
         * コンストラクタ.
         */
        public SeekCalculatorImpl() {

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isFirstFastForward(long now) {
            return mLastFastForward < now - mContinuousTime;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isFirstRewind(long now) {
            return mLastRewind < now - mContinuousTime;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int fastForward() {
            long now = getSystemCurrentTimeMillis();
            if (isFirstFastForward(now)) {
                mFastForwardCount = 0; // Fast Forwardが途切れたとみなす
            }
            mFastForwardCount++;
            mLastFastForward = now;

            int step = (int) (mInitialStep * Math.pow(mStepMultiplier, (double) (mFastForwardCount - 1)));
            return Math.min(step, mMaxStep);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int rewind() {
            long now = getSystemCurrentTimeMillis();
            if (isFirstRewind(now)) {
                mRewindCount = 0; // Fast Forwardが途切れたとみなす
            }
            mRewindCount++;
            mLastRewind = now;

            int step = (int) (mInitialStep * Math.pow(mStepMultiplier, (double) (mRewindCount - 1)));
            return Math.min(step, mMaxStep);
        }

        long getSystemCurrentTimeMillis() {
            return SystemClock.elapsedRealtime();
        }
    }
}
