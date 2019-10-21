package jp.pioneer.carsync.infrastructure.repository;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.repository.ApplicationInfoRepository;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ApplicationInfoRepositoryの実装.
 */
public class ApplicationInfoRepositoryImpl implements ApplicationInfoRepository {
    @Inject PackageManager mPackageManager;

    /**
     * コンストラクタ.
     */
    @Inject
    public ApplicationInfoRepositoryImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public List<ApplicationInfo> get(@NonNull String[] packageNames) {
        checkNotNull(packageNames);

        List<ApplicationInfo> result = new ArrayList<>();
        for (String packageName : packageNames) {
            ApplicationInfo info = get(packageName);
            if (info != null) {
                result.add(info);
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public ApplicationInfo get(@NonNull String packageName) {
        checkNotNull(packageName);

        try {
            return mPackageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.d("get() %s is not installed.", packageName);
            return null;
        }
    }
}
