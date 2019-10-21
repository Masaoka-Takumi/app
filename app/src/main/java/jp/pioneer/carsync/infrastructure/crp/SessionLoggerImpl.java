package jp.pioneer.carsync.infrastructure.crp;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

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

import javax.inject.Inject;
import javax.inject.Provider;

import jp.pioneer.carsync.BuildConfig;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Debug版用SessionLoggerの実装.
 * <p>
 * {@code Downloadディレクトリ/パッケージ名}以下に「CarSyncSession_[年月日時分秒].log」というファイル名で
 * 出力する。※[年月日時分秒]はセッション開始時のもの
 */
public class SessionLoggerImpl extends SessionLogger {
    @Inject Provider<Date> mDateProvider;
    private DateFormat mFileNameFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
    private DateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
    private File mFile;
    private Writer mWriter;

    /**
     * コンストラクタ.
     */
    @Inject
    public SessionLoggerImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(@NonNull String type, @NonNull String format, Object... args) {
        checkNotNull(type);
        checkNotNull(format);

        String message = String.format("%s,%s", type, String.format(format, args));
        Timber.d(message);

        if (mWriter == null) {
            return;
        }

        try {
            mWriter.append(mDateFormat.format(mDateProvider.get()))
                    .append(",")
                    .append(message)
                    .append("\n")
                    .flush();
        } catch (IOException e) {
            Timber.e(e, "Failed to write to log file.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doInitialize() {
        mWriter = createWriter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doTerminate() {
        if (mWriter == null) {
            return;
        }

        try {
            mWriter.close();
        } catch (IOException e) {
            Timber.w(e, "doTerminate()");
        } finally {
            mWriter = null;
        }
    }

    /**
     * Writer生成.
     * <p>
     * UnitTest用。
     *
     * @return Writer。作成出来ない場合null。
     */
    @VisibleForTesting
    Writer createWriter() {
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
                String.format("CarSyncSession_%s.log", mFileNameFormat.format(mDateProvider.get())));
        try {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mFile), "UTF-8"));
        } catch (IOException e) {
            // 恐らくパーミッションが無い
            Timber.w(e, "createWriter()");
            return null;
        }
    }
}
