package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

/**
 * カスタムScrollView.
 */
public class CustomScrollView extends ScrollView {
    private ScrollToBottomListener mScrollToBottomListener;
    private int mScrollBottomMargin = 0;

    /**
     * コンストラクタ.
     */
    public CustomScrollView(Context context) {
        super(context);
    }

    /**
     * コンストラクタ.
     */
    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * コンストラクタ.
     */
    public CustomScrollView(Context context, AttributeSet attrs, int defs) {
        super(context, attrs, defs);
    }

    /**
     * リスナー設定.
     * <p>
     * 一番下までスクロールした場合に呼び出されるリスナーを設定.
     *
     * @param listener リスナー
     */
    public void setScrollToBottomListener(ScrollToBottomListener listener) {
        this.mScrollToBottomListener = listener;
    }

    /**
     * マージン設定.
     * <p>
     * 一番下判定で使用するマージンの設定
     * 設定された値分のマージンで一番下かどうかを判断する
     *
     * @param value 値
     */
    public void setScrollBottomMargin(int value) {
        this.mScrollBottomMargin = value;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        View content = getChildAt(0);
        if (mScrollToBottomListener == null) return;
        if (content == null) return;
        if (y + this.getHeight() >= content.getHeight() - mScrollBottomMargin) {
            mScrollToBottomListener.onScrollToBottom(this);
        }
    }

    /**
     * リスナー.
     */
    public interface ScrollToBottomListener {
        /**
         * 一番下までスクロールされた.
         *
         * @param scrollView CustomScrollView
         */
        void onScrollToBottom(CustomScrollView scrollView);
    }
}
