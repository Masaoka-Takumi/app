package jp.pioneer.carsync.presentation.view.fragment.screen.home;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.gifdecoder.GifDecoder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.OpeningPresenter;
import jp.pioneer.carsync.presentation.view.OpeningView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

/**
 * 起動画面
 */

public class OpeningFragment extends  AbstractScreenFragment<OpeningPresenter, OpeningView> implements OpeningView{

    @Inject OpeningPresenter mPresenter;
    @BindView(R.id.splash_view) ImageView mSplashView;
    private Unbinder mUnbinder;

    public OpeningFragment() {
    }

    public static OpeningFragment newInstance(Bundle args) {
        OpeningFragment fragment = new OpeningFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_opening, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void startAnimation(){
        int orientation = getContext().getResources().getConfiguration().orientation;
        Glide
                .with(this)
                .load(orientation == ORIENTATION_LANDSCAPE ? R.raw.splash_001 : R.raw.splash_002)
                .error(orientation == ORIENTATION_LANDSCAPE ? R.drawable.splash_001 : R.drawable.splash_002)
                .fitCenter()
                .crossFade().diskCacheStrategy(DiskCacheStrategy.NONE)
                .listener(new RequestListener<Integer, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, Integer model, Target<GlideDrawable> target, boolean isFirstResource) {
                        Glide.with(getContext())
                                .load(orientation == ORIENTATION_LANDSCAPE ? R.drawable.splash_001 : R.drawable.splash_002)
                                .into(mSplashView);

                        getPresenter().onSendAction(500);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, Integer model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        GifDrawable gifDrawable;
                        if (resource instanceof GifDrawable) {
                            gifDrawable = (GifDrawable) resource;

                            int duration = 0;
                            GifDecoder decoder = gifDrawable.getDecoder();
                            for (int i = 0; i < gifDrawable.getFrameCount(); i++) {
                                duration += decoder.getDelay(i);
                            }

                            getPresenter().onSendAction(duration + 300);
                        }

                        return false;
                    }
                })
                .into(new GlideDrawableImageViewTarget(mSplashView, 1));
    }

    @Override
    public void stopAnimation(){
        int orientation = getContext().getResources().getConfiguration().orientation;
        Glide.with(getContext())
                .load(orientation == ORIENTATION_LANDSCAPE ? R.drawable.splash_001 : R.drawable.splash_002)
                .fitCenter()
                .into(mSplashView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        getPresenter().onSendAction();
    }

    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @NonNull
    @Override
    protected OpeningPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ScreenId getScreenId() {
        return ScreenId.OPENING;
    }
}
