package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.FaderBalanceSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.presenter.FaderBalanceSettingPresenter;
import jp.pioneer.carsync.presentation.util.ImageViewUtil;
import jp.pioneer.carsync.presentation.view.FaderBalanceSettingView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.widget.FaderBalanceGraphView;

/**
 * Created by NSW00_906320 on 2017/07/20.
 */

public class FaderBalanceSettingFragment extends AbstractScreenFragment<FaderBalanceSettingPresenter, FaderBalanceSettingView> implements FaderBalanceSettingView, FaderBalanceGraphView.OnFaderBalanceChangedListener {
    @Inject FaderBalanceSettingPresenter mPresenter;
    @BindView(R.id.disable_layer) View mDisableLayer;
    private Unbinder mUnbinder;
    private boolean mViewCreated;
    private Handler mHandler = new Handler();
    private long mContinuousInterval = 1000 / 6; // ms
    private SparseArrayCompat<LongTouchRunnable> mLongTouchRunnables = new SparseArrayCompat<>();

    /**
     * コンストラクタ
     */
    public FaderBalanceSettingFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return EulaFragment
     */
    public static FaderBalanceSettingFragment newInstance(Bundle args) {
        FaderBalanceSettingFragment fragment = new FaderBalanceSettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.balanceText)
    TextView mBalanceText;

    @BindView(R.id.faderText)
    TextView mFaderText;

    @BindView(R.id.faderBalanceGraph)
    FaderBalanceGraphView mFaderBalanceGraph;

    @BindView(R.id.faderUpButton)
    Button mFaderUpButton;

    @BindView(R.id.faderDownButton)
    Button mFaderDownButton;

    @BindView(R.id.balanceLeftButton)
    Button mBalanceLeftButton;

    @BindView(R.id.balanceRightButton)
    Button mBalanceRightButton;

    @BindView(R.id.centerPositionButton)
    Button mCenterPositionButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fader_balance_setting, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mFaderBalanceGraph.setOnFaderBalanceChangedListener(this);
        ((ViewGroup) mFaderBalanceGraph.getParent()).setClipChildren(false); //FIXME see {@link FaderBalanceGraphView#init()}
        mViewCreated = true;
        return view;
    }

    @Override
    public void onStop() {
        for (int i = 0; i < mLongTouchRunnables.size(); i++) {
            mHandler.removeCallbacks(mLongTouchRunnables.valueAt(i));
        }
        mLongTouchRunnables.clear();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mFaderBalanceGraph.setOnFaderBalanceChangedListener(null);
        mUnbinder.unbind();
        mViewCreated = false;
        super.onDestroyView();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected FaderBalanceSettingPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.FADER_BALANCE_SETTING;
    }

    @OnTouch({R.id.faderUpButton, R.id.faderDownButton, R.id.balanceLeftButton, R.id.balanceRightButton})
    boolean onButtonTouched(Button button, MotionEvent ev) {
        int action = ev.getActionMasked();
        int id = button.getId();
        if (action == MotionEvent.ACTION_DOWN) {
            LongTouchRunnable r = new LongTouchRunnable(button);
            mLongTouchRunnables.put(id, r);
            mHandler.postDelayed(r, 500 + mContinuousInterval);
        } else if (action == MotionEvent.ACTION_UP) {
            LongTouchRunnable r = mLongTouchRunnables.get(id);
            if (r != null) {
                mHandler.removeCallbacks(r);
                // MEMO ここでOnClick処理を止めるためにtrueを返すと
                // ボタンの表示がDOWNの状態から戻らないのでfalseを返している
            }
        }
        return false;
    }

    @OnClick({R.id.centerPositionButton, R.id.faderUpButton, R.id.faderDownButton, R.id.balanceLeftButton, R.id.balanceRightButton})
    void onButtonClicked(Button button) {
        int id = button.getId();
        LongTouchRunnable r = mLongTouchRunnables.get(id);
        if (r != null && r.stop) {
            // 例えば0になってLongTouch処理が止まったあとにボタンをUPすると
            // onButtonClickedが呼ばれるのでここで止める必要がある
            mLongTouchRunnables.remove(id);
            return;
        }
        StatusHolder holder = mPresenter.getStatusHolder();
        FaderBalanceSetting setting = holder.getAudioSetting().faderBalanceSetting;
        int fader = setting.currentFader;
        int balance = setting.currentBalance;
        switch (id) {
            case R.id.centerPositionButton:
                fader = 0;
                balance = 0;
                break;
            case R.id.faderUpButton:
                fader++;
                break;
            case R.id.faderDownButton:
                fader--;
                break;
            case R.id.balanceLeftButton:
                balance--;
                break;
            case R.id.balanceRightButton:
                balance++;
                break;
            default:
                return;
        }
        fader = Math.min(Math.max(fader, setting.minimumFader), setting.maximumFader);
        balance = Math.min(Math.max(balance, setting.minimumBalance), setting.maximumBalance);
        if (setting.currentBalance == balance && setting.currentFader == fader) {
            return;
        }
        mPresenter.setFaderBalance(fader, balance);
    }
    @Override
    public void onStatusUpdated() {
        applyCarDeviceStatus();
    }

    private void applyCarDeviceStatus() {

        if (!mViewCreated) return;

        StatusHolder holder = mPresenter.getStatusHolder();
        CarDeviceSpec spec = holder.getCarDeviceSpec();
        AudioSettingStatus status = holder.getAudioSettingStatus();

        FaderBalanceSetting setting = holder.getAudioSetting().faderBalanceSetting;
        applyBalanceText(setting.currentBalance, false);
        applyFaderText(setting.currentFader, false);
        mFaderText.setVisibility(status.faderSettingEnabled ? View.VISIBLE : View.INVISIBLE);
        applyFaderBalanceGraph(holder, status.faderSettingEnabled);
        applyButtons(holder, status.faderSettingEnabled);
    }

    private void applyBalanceText(int current, boolean updateWhenDragging) {
        String text;
        if (current > 0) {
            text = getString(R.string.set_188) + " : " + current;
        } else if (current < 0) {
            text = getString(R.string.set_114) + " : " + String.valueOf(-current);
        } else {
            text = getString(R.string.set_115) + " : 0";
        }
        if (updateWhenDragging || !mFaderBalanceGraph.isDragging()) {
            mBalanceText.setText(text);
        }
    }

    private void applyFaderText(int current, boolean updateWhenDragging) {
        String text;
        if (current > 0) {
            text = getString(R.string.set_250) + " : " + current;
        } else if (current < 0) {
            text = getString(R.string.set_177) + " : " + String.valueOf(-current);
        } else {
            text = getString(R.string.set_082) + " : 0";
        }
        if (updateWhenDragging || !mFaderBalanceGraph.isDragging()) {
            mFaderText.setText(text);
        }
    }

    private void applyFaderBalanceGraph(StatusHolder holder, boolean isFaderEnabled) {
        FaderBalanceSetting setting = holder.getAudioSetting().faderBalanceSetting;

		/*
         * ドラッグ中は CarDeviceStatus の fader/balance 値を適用しない。
		 *
		 * この method は定期的なステータスの通知によっても呼ばれてしまうので、
		 * ドラッグ状態で指を動かさないでいる時にそのような通知があると、
		 * ポインタが指から外れたところに飛んでしまう。
		 * その状態で指を離しても本来の位置に戻るので実害はないが、印象がよくないのでその対策。
		 */
        if (!mFaderBalanceGraph.isDragging()) {
            mFaderBalanceGraph.beginUpdates();
            mFaderBalanceGraph.setMinBalance(setting.minimumBalance);
            mFaderBalanceGraph.setMaxBalance(setting.maximumBalance);
            mFaderBalanceGraph.setBalance(setting.currentBalance);
            mFaderBalanceGraph.setMinFader(setting.minimumFader);
            mFaderBalanceGraph.setMaxFader(setting.maximumFader);
            mFaderBalanceGraph.setFader(setting.currentFader);
            mFaderBalanceGraph.setFaderEnabled(isFaderEnabled);
            mFaderBalanceGraph.endUpdates();
        }

        AudioSettingStatus audioSettingStatus = holder.getAudioSettingStatus();
        mFaderBalanceGraph.setEnabled(
                audioSettingStatus.faderSettingEnabled
                        || audioSettingStatus.balanceSettingEnabled
        );
    }

    private void applyButtons(StatusHolder holder, boolean isFaderEnabled) {
        // 2Way Networkモードの場合はFaderボタンを表示しない
        mFaderUpButton.setVisibility(isFaderEnabled ? View.VISIBLE : View.GONE);
        mFaderDownButton.setVisibility(isFaderEnabled ? View.VISIBLE : View.GONE);

        AudioSettingStatus audioSettingStatus = holder.getAudioSettingStatus();
        mFaderUpButton.setEnabled(audioSettingStatus.faderSettingEnabled);
        mFaderDownButton.setEnabled(audioSettingStatus.faderSettingEnabled);
        mBalanceLeftButton.setEnabled(audioSettingStatus.balanceSettingEnabled);
        mBalanceRightButton.setEnabled(audioSettingStatus.balanceSettingEnabled);

        mCenterPositionButton.setEnabled(
                audioSettingStatus.faderSettingEnabled
                        || audioSettingStatus.balanceSettingEnabled
        );
    }

    @Override
    public void onFaderBalanceChanged(int fader, int balance) {
        mPresenter.setFaderBalance(fader, balance);
    }

    @Override
    public void onFaderBalanceMoved(int fader, int balance) {
        boolean dragging = mFaderBalanceGraph.isDragging();
        applyFaderText(fader, dragging);
        applyBalanceText(balance, dragging);
    }

    private class LongTouchRunnable implements Runnable {
        private boolean isFirst = true;
        private boolean stop;
        private Button button;

        private LongTouchRunnable(Button button) {
            this.button = button;
        }

        @Override
        public void run() {
            stop = !isFirst && shouldStop();
            if (!stop) {
                onButtonClicked(button);
                isFirst = false;
                mHandler.postDelayed(this, mContinuousInterval);
            }
        }

        /**
         * 設定値が0またはmin/maxに到達した場合はtrueを返してLongTouch処理を終了させる
         *
         * @return
         */
        private boolean shouldStop() {
            StatusHolder holder = mPresenter.getStatusHolder();
            FaderBalanceSetting setting = holder.getAudioSetting().faderBalanceSetting;
            int id = button.getId();
            boolean stop = false;
            switch (id) {
                case R.id.faderUpButton:
                    stop = setting.currentFader == 0 || setting.currentFader >= setting.maximumFader;
                    break;
                case R.id.faderDownButton:
                    stop = setting.currentFader == 0 || setting.currentFader <= setting.minimumFader;
                    break;
                case R.id.balanceLeftButton:
                    stop = setting.currentBalance == 0 || setting.currentBalance >= setting.maximumBalance;
                    break;
                case R.id.balanceRightButton:
                    stop = setting.currentBalance == 0 || setting.currentBalance <= setting.minimumBalance;
                    break;
                default:
                    break;
            }
            return stop;
        }
    }

    /**
     * UIColorの設定
     *
     * @param color 設定色
     */
    @Override
    public void setColor(@ColorRes int color) {

        StateListDrawable stateList = (StateListDrawable) mCenterPositionButton.getBackground();
        DrawableContainer.DrawableContainerState drawableContainerState = (DrawableContainer.DrawableContainerState) stateList.getConstantState();
        Drawable[] children = drawableContainerState.getChildren();
        LayerDrawable selectedItem = (LayerDrawable) children[0];
        selectedItem.setDrawableByLayerId(R.id.front, ImageViewUtil.setTintColor(getContext(), R.drawable.p0301_fbiconbtn_2prs, color));

        stateList = (StateListDrawable) mFaderDownButton.getBackground();
        drawableContainerState = (DrawableContainer.DrawableContainerState) stateList.getConstantState();
        children = drawableContainerState.getChildren();
        selectedItem = (LayerDrawable) children[0];
        selectedItem.setDrawableByLayerId(R.id.front, ImageViewUtil.setTintColor(getContext(), R.drawable.p0302_fbiconbtn_2prs, color));

        stateList = (StateListDrawable) mFaderUpButton.getBackground();
        drawableContainerState = (DrawableContainer.DrawableContainerState) stateList.getConstantState();
        children = drawableContainerState.getChildren();
        selectedItem = (LayerDrawable) children[0];
        selectedItem.setDrawableByLayerId(R.id.front, ImageViewUtil.setTintColor(getContext(), R.drawable.p0303_fbiconbtn_2prs, color));

        stateList = (StateListDrawable) mBalanceRightButton.getBackground();
        drawableContainerState = (DrawableContainer.DrawableContainerState) stateList.getConstantState();
        children = drawableContainerState.getChildren();
        selectedItem = (LayerDrawable) children[0];
        selectedItem.setDrawableByLayerId(R.id.front, ImageViewUtil.setTintColor(getContext(), R.drawable.p0304_fbiconbtn_2prs, color));

        stateList = (StateListDrawable) mBalanceLeftButton.getBackground();
        drawableContainerState = (DrawableContainer.DrawableContainerState) stateList.getConstantState();
        children = drawableContainerState.getChildren();
        selectedItem = (LayerDrawable) children[0];
        selectedItem.setDrawableByLayerId(R.id.front, ImageViewUtil.setTintColor(getContext(), R.drawable.p0305_fbiconbtn_2prs, color));

    }

    @Override
    public void setEnable(boolean isEnabled) {
        if(isEnabled) {
            mDisableLayer.setVisibility(View.GONE);
            mDisableLayer.setOnTouchListener(null);
        }else{
            mDisableLayer.setVisibility(View.VISIBLE);
            mDisableLayer.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            //some code....
                            break;
                        case MotionEvent.ACTION_UP:
                            v.performClick();
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
        }
    }
}
