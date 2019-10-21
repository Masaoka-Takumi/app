package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.presentation.presenter.BackgroundImagePreviewPresenter;
import jp.pioneer.carsync.presentation.view.BackgroundImagePreviewView;
import jp.pioneer.carsync.presentation.view.activity.MainActivity;
import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;

public class BackgroundImagePreviewFragment extends AbstractDialogFragment<BackgroundImagePreviewPresenter, BackgroundImagePreviewView, BackgroundImagePreviewFragment.Callback>
        implements BackgroundImagePreviewView {
    @Inject BackgroundImagePreviewPresenter mPresenter;
    @BindView(R.id.preview_image) ImageView mPreviewImage;
    @BindView(R.id.cancel_button) RelativeLayout mCancelButton;
    @BindView(R.id.set_button) RelativeLayout mSetButton;

    /**
     * コンストラクタ
     */
    public BackgroundImagePreviewFragment() {
    }

    /**
     * 新規インスタンス取得
     *
     * @param target コールバック通知先
     * @param args   引き継ぎ情報
     * @return BackgroundImagePreviewFragment
     */
    public static BackgroundImagePreviewFragment newInstance(Fragment target, Bundle args) {
        BackgroundImagePreviewFragment fragment = new BackgroundImagePreviewFragment();
        fragment.setTargetFragment(target, 0);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.BehindScreenStyle);
        setCancelable(true);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialog_image_preview, container, false);
        ButterKnife.bind(this, view);
        try {
        	InputStream in;
            in = getActivity().openFileInput("myPhotoPreview.jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            in.close();
            mPreviewImage.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return view;
    }


    @Override
    protected void doInject(FragmentComponent fragmentComponent) {
        fragmentComponent.inject(this);
    }

    @Override
    protected boolean isInstanceOfCallback(Object callback) {
        return callback instanceof BackgroundImagePreviewFragment.Callback;
    }

    @NonNull
    @Override
    protected BackgroundImagePreviewPresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void callbackClose() {
        if (getCallback() != null) {
            getCallback().onClose(this);
        }
        this.dismiss();
    }

    /**
     * 確認ボタン押下イベント
     */
    @OnClick(R.id.cancel_button)
    public void onClickCancelButton() {
        ((MainActivity)getActivity()).getGalleryImage();
        getPresenter().cancelMyPhoto();
        callbackClose();
    }

    /**
     * 確認ボタン押下イベント
     */
    @OnClick(R.id.set_button)
    public void onClickSetButton() {
        InputStream in = null;
        FileOutputStream out1 = null;
        FileOutputStream out2 = null;
        try {
            in = getActivity().openFileInput("myPhotoPreview.jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            in.close();
            out1 =  getActivity().openFileOutput("myPhoto.jpg",MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out1);
            out1.close();
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            Timber.d("幅：" + w + " 高さ：" + h);
            // リサイズ比
            double resizeScale = 1;
            // 横長画像の場合
            int thumbnailSize = 384;
            if (bitmap.getWidth() >= bitmap.getHeight()) {
                if(bitmap.getHeight()>thumbnailSize) {
                    resizeScale = (double) thumbnailSize / bitmap.getHeight();
                }
            }
            // 縦長画像の場合
            else {
                if(bitmap.getWidth()>thumbnailSize) {
                    resizeScale = (double) thumbnailSize / bitmap.getWidth();
                }
            }
            Bitmap afterResizeBitmap = Bitmap.createScaledBitmap(bitmap,
                    (int) (bitmap.getWidth() * resizeScale),
                    (int) (bitmap.getHeight() * resizeScale),
                    true);
            out2 = getActivity().openFileOutput("myPhotoThumbnail.jpg",MODE_PRIVATE);
            afterResizeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out2);
            out2.close();
            w = afterResizeBitmap.getWidth();
            h = afterResizeBitmap.getHeight();
            Timber.d("サムネイル 幅：" + w + " 高さ：" + h);
            bitmap.recycle();
            afterResizeBitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out1 != null) {
                    out1.close();
                }
                if (out2 != null) {
                    out2.close();
                }
            } catch (IOException ignored) {
            }
        }
        getPresenter().setMyPhoto();
        if (getCallback() != null) {
            getCallback().onSet(this);
        }
        callbackClose();
    }

    /**
     * ダイアログ終了通知interface
     */
    public interface Callback {
        /**
         * ダイアログ終了通知
         *
         * @param fragment 終了ダイアログ
         */
        void onClose(BackgroundImagePreviewFragment fragment);
        /**
         * 壁紙設定通知
         *
         * @param fragment 終了ダイアログ
         */
        void onSet(BackgroundImagePreviewFragment fragment);
    }
}
