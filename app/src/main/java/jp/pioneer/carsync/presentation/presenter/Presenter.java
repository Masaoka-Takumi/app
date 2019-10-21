package jp.pioneer.carsync.presentation.presenter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import jp.pioneer.carsync.presentation.view.activity.AbstractActivity;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AbstractDialogFragment;
import jp.pioneer.carsync.presentation.view.fragment.preference.AbstractPreferenceFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.service.AbstractService;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * プレゼンター.
 * <p>
 * {@link AbstractActivity}、{@link AbstractScreenFragment}、{@link AbstractDialogFragment}、
 * {@link AbstractPreferenceFragment}、{@link AbstractService}に適合するプレゼンター。
 * 事実上、全てのプレゼンターのスーパークラスとなる。<br>
 * ライフサイクル関係の呼び出しを全て実装しており、サブクラスは必要であればライフサイクルに対応する{@code onXXX}
 * メソッドをオーバーライドし、プレゼンターが提供するメソッドを実装すれば良い。<br>
 * プレゼンターのインスタンスは、画面の回転や環境変更（Configuration Change）時に破棄されないが、Low Memory Killer時は
 * 破棄されるため、状態の保存（{@link #onSaveInstanceState(Bundle)}）と復元{@link #onRestoreInstanceState(Bundle)}を
 * 必要に応じて実装すること。サービスの場合は不要。（そもそも呼ばれない）
 * <p>
 * プレゼンターのライフサイクル:<br>
 * 画面生成～破棄は通常以下のようになる。
 * <ol>
 * <li>{@link #onTakeView()}</li>
 * <li>{@link #onInitialize()} </li>
 * <li>{@link #onResume()}</li>
 * <li>{@link #onPause()} </li>
 * <li>{@link #onDropView()} </li>
 * </ol>
 * 画面回転やLow Memory Killerにより画面が破棄される場合、{@link #onPause()} 後に{@link #onSaveInstanceState(Bundle)}
 * が呼ばれ、復元に伴う{@link #onTakeView()} が呼ばれた後に、{@link #onRestoreInstanceState(Bundle)} が呼ばれる。
 * （{@link #onInitialize()} は呼ばれない）
 *
 * @param <T> プレゼンターが対象とするビュー
 */
public class Presenter<T> {
    private T mView;

    /**
     * ビュー受け入れ.
     * <p>
     * 必要であれば、{@link #onTakeView()} をオーバーライドする。
     * 本メソッドをオーバーライドしないこと。（finalメソッド相当）
     *
     * @param view ビュー
     * @throws NullPointerException {@code view}がnull
     */
    public void takeView(@NonNull T view) {
        mView = checkNotNull(view);
        onTakeView();
    }

    /**
     * 初期化.
     * <p>
     * 必要であれば、{@link #onInitialize()} をオーバーライドする。
     * 本メソッドをオーバーライドしないこと。（finalメソッド相当）
     */
    public void initialize() {
        onInitialize();
    }

    /**
     * 状態復元.
     * <p>
     * 必要であれば、{@link #onSaveInstanceState(Bundle)} をオーバーライドする。
     * 本メソッドをオーバーライドしないこと。（finalメソッド相当）
     *
     * @param savedInstanceState {@link #saveInstanceState(Bundle)}で保存したBundle
     * @throws NullPointerException {@code savedInstanceState}がnull
     */
    public void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        checkNotNull(savedInstanceState);
        onRestoreInstanceState(savedInstanceState);
    }

    /**
     * 再開.
     * <p>
     * 必要であれば、{@link #onResume()} をオーバーライドする。
     * 本メソッドをオーバーライドしないこと。（finalメソッド相当）
     */
    public void resume() {
        onResume();
    }

    /**
     * 一時停止.
     * <p>
     * 必要であれば、{@link #onPause()} をオーバーライドする。
     * 本メソッドをオーバーライドしないこと。（finalメソッド相当）
     */
    public void pause() {
        onPause();
    }

    /**
     * 状態保存.
     * 必要であれば、{@link #onSaveInstanceState(Bundle)}  をオーバーライドする。
     * 本メソッドをオーバーライドしないこと。（finalメソッド相当）
     *
     * @param outState 保存先のBundle
     * @throws NullPointerException {@code outState}がnull
     */
    public void saveInstanceState(@NonNull Bundle outState) {
        checkNotNull(outState);
        onSaveInstanceState(outState);
    }

    /**
     * ビューの放棄.
     * <p>
     * 必要であれば、{@link #onDropView()} をオーバーライドする。
     * 本メソッドをオーバーライドしないこと。（finalメソッド相当）
     */
    public void dropView() {
        mView = null;
        onDropView();
    }

    /**
     * 終了
     * <p>
     * 必要であれば、{@link #onDestroy()} をオーバーライドする。
     * 本メソッドをオーバーライドしないこと。（finalメソッド相当）
     */
    public void destroy() {
        onDestroy();
    }

    /**
     * ビュー受け入れハンドラ.
     * <p>
     * 必要であればオーバーライドする。
     * {@code super.onTakeView()}は不要。
     */
    void onTakeView() {
    }

    /**
     * 初期化ハンドラ.
     * <p>
     * 必要であればオーバーライドする。
     * {@code super.onInitialize()}は不要。
     */
    void onInitialize() {
    }

    /**
     * 状態復元ハンドラ.
     * <p>
     * 必要であればオーバーライドする。
     * {@code super.onRestoreInstanceState(savedInstanceState)}は不要。
     */
    void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
    }

    /**
     * 再開ハンドラ.
     * <p>
     * 必要であればオーバーライドする。
     * {@code super.onResume()}は不要。
     */
    void onResume() {
    }

    /**
     * 一時停止ハンドラ.
     * <p>
     * 必要であればオーバーライドする。
     * {@code super.onPause()}は不要。
     */
    void onPause() {
    }

    /**
     * 状態保存ハンドラ.
     * <p>
     * 必要であればオーバーライドする。
     * {@code super.onSaveInstanceState(outState)}は不要。
     */
    void onSaveInstanceState(@NonNull Bundle outState) {
    }

    /**
     * ビューの放棄ハンドラ.
     * <p>
     * 必要であればオーバーライドする。
     * {@code super.onDropView()}は不要。
     */
    void onDropView() {
    }

    /**
     * 終了ハンドラ
     * <p>
     * 必要であればオーバーライドする。
     * {@code super.onDestroy()}は不要。
     */
    void onDestroy() {
    }

    /**
     * ビュー取得.
     *
     * @return ビュー。{@link #onTakeView()} 前と{@link #onDropView()} 後はnull。
     */
    @Nullable
    T getView() {
        return mView;
    }
}
