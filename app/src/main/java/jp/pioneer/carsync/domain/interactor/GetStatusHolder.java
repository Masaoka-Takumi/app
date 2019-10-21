package jp.pioneer.carsync.domain.interactor;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.repository.StatusHolderRepository;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * 車載機のステータスやアプリ内の再生楽曲情報取得.
 */
public class GetStatusHolder {
    @Inject StatusHolderRepository mStatusHolderRepository;

    /**
     * コンストラクタ.
     */
    @Inject
    public GetStatusHolder() {
    }

    /**
     * 実行.
     * <p>
     * 本メソッドが返すインスタンスはアプリ終了まで存在するため、保持しても構わない。
     * 同一のインスタンスとなるため、ミュータブルな値の変化をチェックする場合、
     * クローンを作成し比較する必要がある。
     *
     * @return StatusHolder
     */
    public StatusHolder execute() {
        return mStatusHolderRepository.get();
    }
}
