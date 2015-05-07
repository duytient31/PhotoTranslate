package android.corbatodd.phototranslate;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by corbatodd on 4/1/15.
 */
public class MainActivity extends Activity implements SurfaceHolder.Callback, View.OnClickListener,
        Camera.PictureCallback, Camera.ShutterCallback {

    Bitmap bitmap;

    public String result;

    Button btTake;
    Button btGallery;

    FocusBoxView mFocusBoxView;
    SurfaceView mSurfaceView;
    CameraPreview mCameraPreview;

    public static final String dataPath = Environment.getExternalStorageDirectory().toString() + "/Photo Translate/";
    public static final String lang = "eng";

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    setContentView(R.layout.activity_main);

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        if (!(new File(dataPath + "tessdata/" + lang + ".traineddata")).exists()) {
            try {
                AssetManager assetManager = getAssets();
                InputStream in = assetManager.open("tessdata/" + lang + ".traineddata");
                OutputStream out = new FileOutputStream(dataPath + "tessdata/" + lang + ".traineddata");
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (IOException e) {

            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        if (mCameraPreview != null && !mCameraPreview.isOn()) {
            mCameraPreview.start();
        }

        if (mCameraPreview != null && mCameraPreview.isOn()) {
            return;
        }

        mCameraPreview = CameraPreview.New(holder);
        mCameraPreview.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        mFocusBoxView = (FocusBoxView) findViewById(R.id.FocusBox);
        mSurfaceView = (SurfaceView) findViewById(R.id.svCameraPreview);

        btTake = (Button) findViewById(R.id.btTake);
        btGallery = (Button) findViewById(R.id.btGallery);

        btTake.setOnClickListener(this);
        btGallery.setOnClickListener(this);

        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mSurfaceView.setOnClickListener(this);


    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCameraPreview != null && mCameraPreview.isOn()) {
            mCameraPreview.stop();
        }

        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.removeCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraPreview.stop();
    }

    @Override
    public void onClick(View v) {
        if (v == btTake) {
            if(mCameraPreview != null && mCameraPreview.isOn()){

                try {
                    mCameraPreview.requestFocus();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mCameraPreview.takeShot(this, this, this);
            }
        }

        if (v == btGallery) {
            if(mCameraPreview !=null && mCameraPreview.isOn()){
                Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        if (data == null) {
            return;
        }

        bitmap = Tools.getFocusedBitmap(this, camera, data, mFocusBoxView.getBox());

        String filePath = Environment.getExternalStorageDirectory().toString() + "/Photo Translate/bitmap.JPEG";

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int rotate = 0;
        if(rotate >= -180 && rotate <= 180 && rotate != 0) {
            bitmap = Tools.preRotateBitmap(bitmap, rotate);
        }

        Intent intent = new Intent(MainActivity.this, TranslateActivity.class);
        Bundle sendBundle = new Bundle();
        intent.putExtra("resultData",sendBundle);
        intent.putExtra("Bitmap",bitmap);
        startActivity(intent);
    }

    @Override
    public void onShutter() {

    }

}