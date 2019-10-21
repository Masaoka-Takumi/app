package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.AdasManualPresenter;
import jp.pioneer.carsync.presentation.view.AdasManualView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

public class AdasManualFragment extends AbstractScreenFragment<AdasManualPresenter, AdasManualView> implements AdasManualView{

    @Inject AdasManualPresenter mPresenter;
    private Unbinder mUnbinder;
    @BindView(R.id.scroll_view) ScrollView mScrollView;
    @BindView(R.id.camera_position_image) ImageView mCameraPositionImage;

    /**
     * コンストラクタ
     */
    public AdasManualFragment() {
    }

    /**
     * 新規インスタンス取得
     * @param args 引き継ぎ情報
     * @return AdasManualFragment
     */
    public static AdasManualFragment newInstance(Bundle args) {
        AdasManualFragment fragment = new AdasManualFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adas_manual, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        String srcStr = "manual_screenshot_" + getString(R.string.com_012).toLowerCase().replace("-","_");
        int src = getResources().getIdentifier(srcStr, "drawable", getActivity().getPackageName());
        if(src!=0){
            mCameraPositionImage.setImageResource(src);
        }else{
            mCameraPositionImage.setImageResource(R.drawable.manual_screenshot_ja_jp);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected AdasManualPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.ADAS_MANUAL;
    }

}