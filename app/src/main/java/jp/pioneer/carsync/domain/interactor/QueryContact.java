package jp.pioneer.carsync.domain.interactor;

import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.content.ContactsContract;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.repository.ContactRepository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 連絡先のクエリー.
 * <p>
 * 連絡先に関する情報を取得する際に使用する。
 */
public class QueryContact {
    @Inject ContactRepository mRepository;

    /**
     * コンストラクタ.
     */
    @Inject
    public QueryContact() {
    }

    /**
     * 実行.
     *
     * @param params クエリーパラメータ
     *               {@link ContactsContract.QueryParamsBuilder}のメソッドを使用して生成する
     * @return {@link CursorLoader}
     * @throws NullPointerException {@code params}がnull
     */
    @NonNull
    public CursorLoader execute(@NonNull QueryParams params) {
        checkNotNull(params);

        return mRepository.get(params);
    }
}
