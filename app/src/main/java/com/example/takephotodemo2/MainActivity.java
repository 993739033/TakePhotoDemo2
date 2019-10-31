package com.example.takephotodemo2;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private ImageView img;
    private SurfaceView sv;
    private SurfaceHolder svHolder;
    Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img = findViewById(R.id.img);
        sv = findViewById(R.id.sv);
        svHolder = this.sv.getHolder();
        svHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        svHolder.addCallback(this);
    }

    Camera.PictureCallback callback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            if (data != null) {
                ByteArrayInputStream isBm = new ByteArrayInputStream(data);
                BitmapFactory.Options newOpts = new BitmapFactory.Options();
                //开始读入图片，此时把options.inJustDecodeBounds 设回true了
                newOpts.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(isBm, null, newOpts);
                newOpts.inJustDecodeBounds = false;
                //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
                isBm.reset();
                isBm = new ByteArrayInputStream(data);
                final Bitmap preBitmap = BitmapFactory.decodeStream(isBm, null, newOpts);

                final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        img.setImageBitmap(bitmap);
                        mCamera.startPreview();
                    }
                });
            }
        }
    };

    public void ontakeP(View view) {
        try {
            mCamera.setPreviewDisplay(sv.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.setDisplayOrientation(0);
        mCamera.startPreview();
        new Thread(new Runnable() {
            @Override
            public void run() {
                mCamera.takePicture(null, null, callback);
            }
        }).start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mCamera=Camera.open(0);
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
            mCamera.release();// release camera
            mCamera = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters params = mCamera.getParameters();
        params.setPictureFormat(ImageFormat.JPEG);// 设置图片格式
        mCamera.setDisplayOrientation(90);//设置成竖拍
        mCamera.setParameters(params);
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }
}
