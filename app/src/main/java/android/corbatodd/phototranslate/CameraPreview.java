package android.corbatodd.phototranslate;

import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * Created by corbatodd on 4/1/15.
 */
public class CameraPreview {

    boolean on;
    Camera mCamera;
    SurfaceHolder mSurfaceHolder;

    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {

        }
    };

    public boolean isOn() {
        return on;
    }

    private CameraPreview(SurfaceHolder surfaceHolder){
        this.mSurfaceHolder = surfaceHolder;
    }

    static public CameraPreview New(SurfaceHolder surfaceHolder){
        return  new CameraPreview(surfaceHolder);
    }

    public void requestFocus() {
        if (mCamera == null)
            return;

        if (isOn()) {
            mCamera.autoFocus(autoFocusCallback);
        }
    }

    public void start() {
        this.mCamera = CheckCamera.getCamera();

        if (this.mCamera == null)
            return;

        try {

            this.mCamera.setPreviewDisplay(this.mSurfaceHolder);
            this.mCamera.setDisplayOrientation(90);
            this.mCamera.startPreview();

            on = true;

        } catch (IOException e) {

        }
    }

    public void stop(){

        if(mCamera != null){
            mCamera.release();
            mCamera = null;
        }

        on = false;
    }

    public void takeShot(Camera.ShutterCallback shutterCallback, Camera.PictureCallback rawPictureCallback, Camera.PictureCallback jpegPictureCallback ){
        if(isOn()){
            mCamera.takePicture(shutterCallback, rawPictureCallback, jpegPictureCallback);
        }
    }



}
