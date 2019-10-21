package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.content.UpdateParams;
import jp.pioneer.carsync.domain.repository.ContactRepository;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 連絡先更新.
 */
public class UpdateContact {
    @Inject @ForInfrastructure Handler mHandler;
    @Inject ContactRepository mRepository;

    /**
     * コンストラクタ
     */
    @Inject
    public UpdateContact() {
    }

    /**
     * 電話帳更新
     *
     * @param params 更新内容
     * @throws NullPointerException {@code params} がnull
     */
    public void execute(@NonNull UpdateParams params) {
        checkNotNull(params);

        mHandler.post(() ->  {
            if (mRepository.update(params) != 1) {
                Timber.e("execute() update failed.");
            }
        });
    }
}
