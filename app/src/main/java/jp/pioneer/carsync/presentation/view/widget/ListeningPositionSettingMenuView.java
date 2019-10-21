package jp.pioneer.carsync.presentation.view.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.model.ListeningPositionSetting;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import jp.pioneer.carsync.presentation.view.adapter.ListeningPositionAdapter;

/**
 * Advanced Menuのポップアップメニュー用のView
 * Created by tsuyosh on 2016/02/23.
 */
public class ListeningPositionSettingMenuView extends FrameLayout {
    @BindView(R.id.titleText)
    TextView mTitleText;

    @BindView(R.id.downButton)
    ImageView mDownButton;

    @BindView(R.id.list)
    ListView mListView;

    private Animation mSlideUpAnimation;
    private Animation mSlideDownAnimation;

    private ListeningPositionSetting mCurrentSetting;
    private boolean mIsClosed = false;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ListeningPositionSettingMenuView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    public ListeningPositionSettingMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public ListeningPositionSettingMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListeningPositionSettingMenuView(Context context) {
        this(context, null);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        View root = inflate(context, R.layout.widget_listening_position_setting_menu, this);
        ButterKnife.bind(this, root);

        mSlideUpAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_bottom);
        mSlideUpAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mSlideDownAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_out_bottom);
        mSlideDownAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    public void slideUp() {
        startAnimation(mSlideUpAnimation);
        mIsClosed=false;
        mDownButton.setEnabled(true);
        mListView.setEnabled(true);
    }

    public void slideDown() {
        if(!mIsClosed) {
            startAnimation(mSlideDownAnimation);
            //×ボタン連続押下での反応を防ぐ
            mListView.setEnabled(false);
            mDownButton.setEnabled(false);
        }
        mIsClosed = true;
    }

    @OnClick(R.id.downButton)
    void onDownButtonClicked() {
        // MEMO Listenerを作ってFragment側で処理させたほうがいいかもしれないけどとりあえずこれで実装
        slideDown();
    }

    public void setAdapter(ListeningPositionAdapter adapter) {
        mListView.setAdapter(adapter);
    }

    public void setCurrentListeningPosition(ListeningPositionSetting current) {
        /*
         * #604 Speaker Menu 表示=>ListeningPosition Menu時、ListeningPosition Menu は Checked 項目が消える問題対策
         * 再現手順（こんな感じでよく再現する、UI タイミングの要因があるかも）：
         * 　端末の向きを変える
         * 　ListeningPosition Menuを表示し設定を変える
         * 　Speaker Menu 表示
         * 　ListeningPosition Menu： Checked 項目が消えた　
         * UI上原因の探しは困難なため、下記 == を外す。　
         */
        // if (mCurrentSetting == current) return;
        mCurrentSetting = current;

        ListeningPositionAdapter adapter = (ListeningPositionAdapter) mListView.getAdapter();
        if (adapter == null) return;

        int pos = adapter.getPosition(mCurrentSetting);
        mListView.setItemChecked(pos, true);
        mListView.setSelection(pos);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mListView.setOnItemClickListener(listener);
    }

    public void setListViewSelector(@DrawableRes int selector) {
        mListView.setSelector(selector);
    }
}
