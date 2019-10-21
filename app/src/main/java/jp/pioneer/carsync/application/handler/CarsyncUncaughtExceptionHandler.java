package jp.pioneer.carsync.application.handler;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.pioneer.carsync.BuildConfig;
import timber.log.Timber;

/**
 * Debug用 UncaughtExceptionHandlerの実装.
 * <p>
 * 予期せぬ例外が発生した場合に
 * {@code Downloadディレクトリ/パッケージ名}以下に「CarSyncBugReport_[年月日時分秒].log」というファイル名で
 * 出力する。※[年月日時分秒]は例外発生時のもの
 */
public class CarsyncUncaughtExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler mHandler;
    private DateFormat mFileNameFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
    private DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
    private File mFile;

    /**
     * コンストラクタ.
     *
     * @param handler UncaughtExceptionHandler
     */
    public CarsyncUncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler){
        mHandler = handler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            Date date = new Date();
            Writer writer = createWriter(date);
            if (writer == null) {
                return;
            }

            writer.append(mDateFormat.format(date))
                    .append(",")
                    .append(e.toString())
                    .append("\n")
                    .append(stackTraceToString(e))
                    .append("\n")
                    .flush();
            writer.close();

        } catch (Exception ex) {
            Timber.w(e, "uncaughtException()");
        } finally {
            mHandler.uncaughtException(t, e);
        }
    }

    private String stackTraceToString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    private Writer createWriter(Date date) {
        File dir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                BuildConfig.APPLICATION_ID);
        if (!dir.exists() && !dir.mkdir()) {
            // 恐らくパーミッションが無い
            Timber.w("createWriter() mkdir(" + dir + ") failed.");
            return null;
        }

        mFile = new File(
                dir,
                String.format("CarSyncBugReport_%s.log", mFileNameFormat.format(date)));
        try {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mFile), "UTF-8"));
        } catch (IOException e) {
            // 恐らくパーミッションが無い
            Timber.w(e, "createWriter()");
            return null;
        }
    }
}
