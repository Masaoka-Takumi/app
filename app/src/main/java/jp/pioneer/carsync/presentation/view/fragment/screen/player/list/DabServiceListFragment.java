package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.domain.content.TunerContract;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.presentation.presenter.DabServiceListPresenter;
import jp.pioneer.carsync.presentation.view.DabServiceListView;
import jp.pioneer.carsync.presentation.view.adapter.ServiceListAdapter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import timber.log.Timber;

public class DabServiceListFragment extends AbstractScreenFragment<DabServiceListPresenter, DabServiceListView>
        implements DabServiceListView {
    @Inject DabServiceListPresenter mPresenter;
    @BindView(R.id.list_view) ListView mListView;
    @BindView(R.id.abc_search_bar) ImageView mAbcSearchBar;
    @BindView(R.id.abc_search_popup) RelativeLayout mAbcSearchPopup;
    @BindView(R.id.search_text) TextView mSearchText;
    @BindView(R.id.touch_view) View mTouchView;
    private ServiceListAdapter mServiceListAdapter;
    private Unbinder mUnbinder;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private boolean mIsFirstDrawn = false;
    private float mBarYLength; //Barの縦幅
    private float mBarYStart; //Bar上端Y座標
    private float mBarYEnd; //Bar下端Y座標
    private float mBarXStart; //Bar左端X座標
    private float mBarXEnd; //Bar右端X座標
    private float mBarXLength; //Barの横幅
    private float mBarMarginY; //Barの縦幅画像枠とのマージン
    private int mOrientation;
    private final Handler mHandler = new Handler();

    /**
     * コンストラクタ
     */
    public DabServiceListFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param args 引き継ぎ情報
     * @return DabServiceListFragment
     */
    public static DabServiceListFragment newInstance(Bundle args) {
        DabServiceListFragment fragment = new DabServiceListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_service_list, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        Configuration config = getResources().getConfiguration();
        mOrientation = config.orientation;
        mServiceListAdapter = new ServiceListAdapter(getContext(), null, false);
        mListView.setVisibility(View.VISIBLE);
        mListView.setAdapter(mServiceListAdapter);
        mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        mAbcSearchBar.setVisibility(View.INVISIBLE);
        mAbcSearchPopup.setVisibility(View.INVISIBLE);
        mTouchView.setEnabled(false);
        mIsFirstDrawn = false;
        mGlobalLayoutListener = () -> {
            Timber.i("OnGlobalLayoutListener#onGlobalLayout() " +
                    "Width = " + String.valueOf(mTouchView.getWidth()) + ", " +
                    "Height = " + String.valueOf(mTouchView.getHeight()));
            if (!mIsFirstDrawn) {
                if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    mBarMarginY = getResources().getDimension(R.dimen.dab_abc_search_bar_portrait_margin);
                } else {
                    mBarMarginY = getResources().getDimension(R.dimen.dab_abc_search_bar_landscape_margin);
                }

                mBarXStart = mAbcSearchBar.getX();
                mBarXLength = mAbcSearchBar.getWidth();
                mBarXEnd = mBarXStart + mBarXLength;
                mBarYStart = mAbcSearchBar.getY() + mBarMarginY;
                mBarYLength = mAbcSearchBar.getHeight() - mBarMarginY * 2;
                mBarYEnd = mBarYStart + mBarYLength;
                Timber.d("mBarXStart=" + mBarXStart + ",mBarXEnd=" + mBarXEnd + ",mBarXLength=" + mBarXLength + ",mBarYStart=" + mBarYStart + ",mBarYEnd=" + mBarYEnd + ",mBarYLength=" + mBarYLength);
                DragViewListener listener = new DragViewListener();
                mTouchView.setOnTouchListener(listener);
                mIsFirstDrawn = true;
            }
            // removeOnGlobalLayoutListener()の削除
            mTouchView.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        };
        mTouchView.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getPresenter().setLoaderManager(LoaderManager.getInstance(this));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTouchView.getViewTreeObserver().removeOnGlobalLayoutListener(mGlobalLayoutListener);
        mUnbinder.unbind();
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.DAB_SERVICE_LIST;
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected DabServiceListPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void setCursor(Cursor cursor, ListType listType, boolean isSph) {
        if (mServiceListAdapter != null) {
            mServiceListAdapter.swapCursor(cursor);
            mServiceListAdapter.notifyDataSetChanged();
        }
        Timber.d("cursor.getCount()=" + cursor.getCount() + ",listType=" + listType);
        if (listType == ListType.SERVICE_LIST) {
            mAbcSearchPopup.setVisibility(View.INVISIBLE);
            mListView.setVerticalScrollBarEnabled(false);
            mListView.setFastScrollEnabled(false);
            if(cursor.getCount() > 0&&isSph){
                mAbcSearchBar.setVisibility(View.VISIBLE);
                mTouchView.setEnabled(true);
            }else{
                mAbcSearchBar.setVisibility(View.INVISIBLE);
                mTouchView.setEnabled(false);
            }
        }else{
            mAbcSearchBar.setVisibility(View.INVISIBLE);
            mListView.setVerticalScrollBarEnabled(true);
            mListView.setFastScrollEnabled(true);
            mAbcSearchPopup.setVisibility(View.INVISIBLE);
            mTouchView.setEnabled(false);
        }
    }

    @Override
    public void setSelectedPositionNotScroll(int position) {
        mAbcSearchPopup.setVisibility(View.INVISIBLE);
        mListView.setItemChecked(position, true);
    }

    @Override
    public void setAbcSearchResult(boolean result) {
        if(!result){
            mAbcSearchPopup.setVisibility(View.INVISIBLE);
            Toast.makeText(getActivity(), R.string.ply_107, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 項目リスト選択
     *
     * @param parent   AdapterView
     * @param view     View
     * @param position int
     * @param id       選択ID
     */
    @OnItemClick(R.id.list_view)
    public void onClickListItem(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) mServiceListAdapter.getItem(position);
        int selectIndex = TunerContract.ListItemContract.Dab.getListIndex(cursor);
        getPresenter().onSelectList(selectIndex, cursor);
    }

    private class DragViewListener implements View.OnTouchListener {
        private static final int ALPHABET_COUNT = 27; //Alphabetの数
        private boolean isTouch = false;//タッチ中か
        private final String[] alphabet = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};

        private DragViewListener() {
        }

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            // タッチしているTouchView内の位置取得
            float x = event.getX();
            float y = event.getY();

            float positionY;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (x >= mBarXStart && x <= mBarXEnd
                            && y >= mBarYStart-mBarMarginY && y <= mBarYEnd+mBarMarginY) {
                        isTouch = true;
                        positionY = y - mBarYStart;
                        int index = Math.round((ALPHABET_COUNT - 1) * positionY / mBarYLength);
                        if (index < 0) {
                            index = 0;
                        } else if (index > ALPHABET_COUNT - 1) {
                            index = ALPHABET_COUNT - 1;
                        }
                        mSearchText.setText(alphabet[index]);
                        mAbcSearchPopup.clearAnimation();
                        mAbcSearchPopup.setVisibility(View.VISIBLE);
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isTouch) {
                        positionY = y - mBarYStart;
                        int index = Math.round((ALPHABET_COUNT - 1) * positionY / mBarYLength);
                        if (index < 0) {
                            index = 0;
                        } else if (index > ALPHABET_COUNT - 1) {
                            index = ALPHABET_COUNT - 1;
                        }
                        //Timber.d("positionY=" + positionY + ",index=" + index);
                        mSearchText.setText(alphabet[index]);
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    //設定値から座標を求める
                    if (isTouch) {
                        getPresenter().executeAbcSearch(mSearchText.getText().toString());
                        isTouch = false;
                        return true;
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
