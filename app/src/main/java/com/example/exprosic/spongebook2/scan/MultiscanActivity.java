package com.example.exprosic.spongebook2.scan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.exprosic.spongebook2.MyApplication;
import com.example.exprosic.spongebook2.R;
import com.example.exprosic.spongebook2.URLManager;
import com.example.exprosic.spongebook2.book.BookItem;
import com.example.exprosic.spongebook2.book.BookProvider;
import com.example.exprosic.spongebook2.booklist.BookListAdapter;
import com.example.exprosic.spongebook2.booklist.BookListProvider;
import com.example.exprosic.spongebook2.scan.camera.AmbientLightManager;
import com.example.exprosic.spongebook2.scan.camera.CameraManager;
import com.example.exprosic.spongebook2.scan.camera.CaptureHandler;
import com.example.exprosic.spongebook2.scan.camera.ScanAction;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;


public class MultiscanActivity extends AppCompatActivity {
    private static final String TAG = MultiscanActivity.class.getSimpleName();
    private static final long BULK_MODE_SCAN_DELAY_MS = 2000L;

    @Bind(R.id.the_surfaceview) SurfaceView mSurfaceView;
    @Bind(R.id.mask_image) ImageView mMaskImage;
    @Bind(R.id.the_recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.the_button) Button mButton;

    private List<BookItem> mBookItems;

    // camera
    private boolean hasSurface;
    private CameraManager mCameraManager;
    private CaptureHandler mCaptureHandler;
    private AmbientLightManager mAmbientLightManager;

    public static void start(Context context) {
        Intent intent = new Intent(context, MultiscanActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiscan);
        ButterKnife.bind(this);

        mBookItems = Collections.synchronizedList(new ArrayList<BookItem>());
        mRecyclerView.setAdapter(new BookListAdapter(this, mBookItems) {
            // 不然的话每个item都有整屏宽
            @Override
            protected View inflateBookItemView(ViewGroup parent) {
                View view = super.inflateBookItemView(parent);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)view.getLayoutParams();
                params.width = getResources().getDimensionPixelSize(R.dimen.item_book_image_width);
                view.setLayoutParams(params);
                return view;
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hasSurface = false;
        mAmbientLightManager = new AmbientLightManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCaptureHandler = null;
        mCameraManager = new CameraManager(getApplication());
        mAmbientLightManager.start(mCameraManager);

        if (hasSurface) {
            initCamera(mSurfaceView.getHolder());
        } else {
            mSurfaceView.getHolder().addCallback(mSurfaceHolderCallback);
        }
    }

    @Override
    protected void onPause() {
        if (mCaptureHandler != null) {
            mCaptureHandler.quitSynchronously();
            mCaptureHandler = null;
        }
        mAmbientLightManager.stop();
        mCameraManager.closeDriver();
        if (!hasSurface)
            mSurfaceView.getHolder().removeCallback(mSurfaceHolderCallback);
        Log.d(TAG, "paused");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "stopped");
        super.onStop();
    }

    public Handler getHandler() {
        return mCaptureHandler;
    }

    public CameraManager getCameraManager() {
        return mCameraManager;
    }

    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        handleISBN(rawResult.getText());
        // Wait a moment or else it will scan the same barcode continuously about 3 times
        restartPreviewAfterDelay(BULK_MODE_SCAN_DELAY_MS);
    }

    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (holder == null) {
                Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
            }
            if (!hasSurface) {
                hasSurface = true;
                initCamera(holder);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG, "surfaceChanged");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "surfaceDestroyed");
            hasSurface = false;
        }
    };

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (mCameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }

        try {
            mCameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (mCaptureHandler== null) {
                Map<DecodeHintType,Object> decodeHints = new HashMap<>();
//                decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                ArrayList<BarcodeFormat> decodeFormats = new ArrayList<>();
                decodeFormats.add(BarcodeFormat.EAN_13);
                mCaptureHandler = new CaptureHandler(this, decodeFormats, decodeHints, null, mCameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mMaskImage.getLayoutParams();
        Rect rect = mCameraManager.getFramingRect();
        // 原来的rect不够小，会虚焦
        int dWidth = rect.width()/3;
        int dHeight = rect.height()/3;
        params.leftMargin = rect.left + dWidth/2;
        params.topMargin = rect.top + dHeight/2;
        params.width = rect.width() - dWidth;
        params.height = rect.height() - dHeight;
        mMaskImage.setLayoutParams(params);
    }

    private void restartPreviewAfterDelay(long delayMS) {
        if (mCaptureHandler != null) {
            mCaptureHandler.sendEmptyMessageDelayed(ScanAction.RESTART_PREVIEW.ordinal(), delayMS);
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        Toast.makeText(this, "something was wrong", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void handleISBN(String isbn) {
        Toast.makeText(this, isbn, Toast.LENGTH_SHORT).show();
        final int idx = mBookItems.size();
        BookItem placeHolder = new BookItem(null, getResources().getString(R.string.book_loading), null, null);
        mBookItems.add(placeHolder);
        mRecyclerView.getAdapter().notifyItemInserted(idx);
        MyApplication.getBookProvider().fetchBookByIsbn(this, isbn, new BookProvider.OnBookFetchedListener() {
            @Override
            public void onBookFetched(BookItem bookItem) {
                mBookItems.set(idx, bookItem);
                mRecyclerView.getAdapter().notifyItemChanged(idx);
            }
        });
    }

    @OnClick(R.id.the_button)
    void updateBookList() {
        List<String> bookIds = new ArrayList<>(mBookItems.size());
        for (int i=0; i<mBookItems.size(); ++i)
            bookIds.set(i, mBookItems.get(i).getBookId());
        MyApplication.getBookListProvider().postBookList(this, bookIds, new BookListProvider.OnBookListUpdatedListener() {
            @Override
            public void onBookListUpdated(int insertedCount, int ignoredCount) {
                Toast.makeText(MultiscanActivity.this,
                        String.format(Locale.CHINESE, getResources().getString(R.string.format__book_list_inserted_ignored), insertedCount, ignoredCount),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
