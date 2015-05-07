package android.corbatodd.phototranslate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by corbatodd on 4/18/15.
 */
public class GalleryActivity extends ActionBarActivity implements View.OnClickListener{

    GoogleTranslate translator;

    private static final int SELECT_PICTURE = 1;

    protected String selectedImagePath;

    EditText edtResult;
    EditText edtTranslatedResult;
    Button btTranslate;
    TouchImageView tivBitmap;

    protected TessOCR tessOCR;

    protected Mat mrgba;

    Bitmap bitmap;

    public String result;



    public static final String dataPath = Environment.getExternalStorageDirectory().toString() + "/Photo Translate/";
    public static final String lang = "eng";

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    setContentView(R.layout.activity_gallery);

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
        setContentView(R.layout.activity_gallery);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        edtResult = (EditText) findViewById(R.id.edtResult);
        edtTranslatedResult = (EditText) findViewById(R.id.edtTranslatedResult);
        btTranslate = (Button) findViewById(R.id.btTranslate);
        tivBitmap = (TouchImageView) findViewById(R.id.tivBitmap);
        btTranslate.setOnClickListener(this);

        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_PICTURE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedImageUri = data.getData();
        selectedImagePath = getPath(selectedImageUri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        bitmap = BitmapFactory.decodeFile(selectedImagePath);
        int height = bitmap.getHeight(), width = bitmap.getWidth();

        if (height > 2000 && width > 2000){
            bitmap = BitmapFactory.decodeFile(selectedImagePath, options);
            int rotate = 0;
            if(rotate >= -180 && rotate <= 180 && rotate != 0) {
                bitmap = Tools.preRotateBitmap(bitmap, rotate);
            }
            bitmap = Bitmap.createScaledBitmap(bitmap,(int)(bitmap.getWidth()*0.8), (int)(bitmap.getHeight()*0.8), true);
            tivBitmap.setImageBitmap(bitmap);

        }else {
            int rotate = 0;
            if(rotate >= -180 && rotate <= 180 && rotate != 0) {
                bitmap = Tools.preRotateBitmap(bitmap, rotate);
            }
            tivBitmap.setImageBitmap(bitmap);
        }

        bitmap = ((BitmapDrawable)tivBitmap.getDrawable()).getBitmap();
        new doOCR().execute();

    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private class doOCR extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        protected void onError(Exception ex) {

        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                tessOCR = new TessOCR();
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(GalleryActivity.this, "Processing", "Scanning English text. Please wait !");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            progress.dismiss();
            super.onPostExecute(result);
            render();
            doOCR();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

    }

    public void render(){
        if (!OpenCVLoader.initDebug()) {

        }else {
            mrgba = new Mat();
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Utils.bitmapToMat(bitmap, mrgba);


            Imgproc.cvtColor(mrgba, mrgba, Imgproc.COLOR_BGR2GRAY);
            Size size = new Size(3,3);

            Imgproc.GaussianBlur(mrgba, mrgba,size, 0);
            Imgproc.threshold(mrgba, mrgba, 0, 255, Imgproc.THRESH_OTSU);

            Imgproc.medianBlur(mrgba, mrgba, 3);
            Imgproc.threshold(mrgba, mrgba, 0, 255, Imgproc.THRESH_OTSU);

            //Imgproc.adaptiveThreshold(mrgba, mrgba, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 61, 15);

            Utils.matToBitmap(mrgba, bitmap, false);

        }
    }

    public void doOCR() {

        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        TessBaseAPI baseAPI = new TessBaseAPI();
        baseAPI.setDebug(true);
        baseAPI.init(dataPath, lang);

        baseAPI.setImage(bitmap);

        tessOCR = new TessOCR();
        tessOCR.setWordConfidences(baseAPI.wordConfidences());
        tessOCR.setMeanConfidence(baseAPI.meanConfidence());
        tessOCR.setRegionBoundingBoxes(baseAPI.getRegions().getBoxRects());
        tessOCR.setTextLineBoundingBoxes(baseAPI.getTextlines().getBoxRects());
        tessOCR.setWordBoundingBoxes(baseAPI.getWords().getBoxRects());
        tessOCR.setStripBoundingBoxes(baseAPI.getStrips().getBoxRects());
        tessOCR.setBitmap(bitmap);

        bitmap = tessOCR.getAnnotatedBitmap();

        result = baseAPI.getUTF8Text();
        baseAPI.end();

        tivBitmap.setImageBitmap(bitmap);

        edtResult.setText(result);

    }

    @Override
    public void onClick(View v) {
        new Translate().execute();
    }

    private class Translate extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        protected void onError(Exception ex) {

        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                translator = new GoogleTranslate("AIzaSyD7aQ--zucjX7lckDRLY8dGI8d_k96AMAs");

                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(GalleryActivity.this, "Processing", "Please wait !");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            progress.dismiss();

            super.onPostExecute(result);
            translated();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

    }

    public void translated() {

        String translateText = edtResult.getText().toString();
        String result = translator.translate(translateText, "en", "vi");
        edtTranslatedResult.setText(result);

    }

}
