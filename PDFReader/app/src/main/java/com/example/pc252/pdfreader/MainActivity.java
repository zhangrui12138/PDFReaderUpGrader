package com.example.pc252.pdfreader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {


    private static final String STATE_CURRENT_PAGE_INDEX = "current_page_index";

    private static final String FILENAME = "PowerAnalysis.pdf";

    private ParcelFileDescriptor mFileDescriptor;

    private PdfRenderer.Page mCurrentPage;

    private int mPageIndex;

    private PdfRenderer mPdfRenderer;
    private ImageView mImageView;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mCurrentPage) {
            outState.putInt(STATE_CURRENT_PAGE_INDEX, mCurrentPage.getIndex());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.iv);
        mImageView.setBackgroundColor(Color.WHITE);
        mPageIndex = 0;
        if (null != savedInstanceState) {
            mPageIndex = savedInstanceState.getInt(STATE_CURRENT_PAGE_INDEX, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Log.d("zhangrui","getIntent().getAction().equals(Intent.ACTION_VIEW)="+getIntent().getAction().equals(Intent.ACTION_VIEW));
        if(getIntent().getAction().equals(Intent.ACTION_VIEW)){
//           Log.d("zhangrui","lujing="+getIntent().getDataString().substring(7));
            try {
                openRendererForRealUrl(this,getIntent().getDataString().substring(7));
                showPage(mPageIndex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            try {
                openRenderer(this);
                showPage(mPageIndex);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openRenderer(Context context) throws IOException {
        String fileName=context.getFilesDir()+FILENAME;
//        Log.d("zhangrui","fileName="+fileName);
        File file = new File(context.getFilesDir(), FILENAME);
        if (!file.exists()) {
            InputStream asset = getAssets().open(FILENAME);
            FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();
        }
        mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        if (mFileDescriptor != null) {
            mPdfRenderer = new PdfRenderer(mFileDescriptor);
        }
    }


    private void openRendererForRealUrl(Context context,String realUrl) throws IOException {
        File file = new File(realUrl);
        if (!file.exists()) {
            InputStream asset = getAssets().open(FILENAME);
            FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();
        }
        mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        if (mFileDescriptor != null) {
            mPdfRenderer = new PdfRenderer(mFileDescriptor);
        }
    }


    private void showPage(int index) {
        if (mPdfRenderer.getPageCount() <= index) {
            return;
        }

        if (null != mCurrentPage) {
            mCurrentPage.close();
        }
        mCurrentPage = mPdfRenderer.openPage(index);
        Bitmap bitmap = Bitmap.createBitmap(920, 1528,Bitmap.Config.ARGB_8888);
        mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY/*PdfRenderer.Page.RENDER_MODE_FOR_PRINT*/);
        mImageView.setImageBitmap(bitmap);
    }


    private void closeRenderer() throws IOException {
        if (null != mCurrentPage) {
            mCurrentPage.close();
        }
        mPdfRenderer.close();
        mFileDescriptor.close();
    }


    @Override
    protected void onDestroy() {
        try {
            closeRenderer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    private float x1 = 0;
    private float x2 = 0;
    private float y1 = 0;
    private float y2 = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            x1 = event.getX();
            y1 = event.getY();
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            x2 = event.getX();
            y2 = event.getY();
            if (x1 - x2 > 20) {
                showPage(mCurrentPage.getIndex() + 1);
            } else if (x2 - x1 > 20) {
                showPage(mCurrentPage.getIndex() - 1 < 0 ? 0 : (mCurrentPage.getIndex() - 1));
            }
        }
        return super.onTouchEvent(event);
    }

}