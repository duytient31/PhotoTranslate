package android.corbatodd.phototranslate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by corbatodd on 4/2/15.
 */
public class TranslateActivity extends ActionBarActivity implements View.OnClickListener{

    GoogleTranslate translator;

    TouchImageView tivCroppedBitmap;
    EditText edtResult;
    TextView tvTranslatedResult;
    Button btTranslate;

    Bitmap bitmap;

    public String result;

    private Mat mrgba;

    protected TessOCR tessOCR;

    public static final String dataPath = Environment.getExternalStorageDirectory().toString() + "/Photo Translate/";
    public static final String lang = "eng";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        tivCroppedBitmap = (TouchImageView)findViewById(R.id.ivCroppedBitmap);
        edtResult = (EditText)findViewById(R.id.edtResult);
        tvTranslatedResult = (TextView)findViewById(R.id.tvTranslatedResult);
        btTranslate = (Button)findViewById(R.id.btTranslate);
        btTranslate.setOnClickListener(this);

        Bundle receiveBundle = getIntent().getBundleExtra("resultData");
        Intent intent = getIntent();
        bitmap = (Bitmap) intent.getParcelableExtra("Bitmap");
        tivCroppedBitmap.setImageBitmap(bitmap);
        new doOCR().execute();

    }

    private class doOCR extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        protected void onError(Exception ex) {

        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                tessOCR = new TessOCR();
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
            progress = ProgressDialog.show(TranslateActivity.this, "Processing", "Scanning English text. Please wait !");
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

        tivCroppedBitmap.setImageBitmap(bitmap);

        edtResult.setText(result);

    }

    @Override
    public void onClick(View v) {

        if (v == btTranslate) {
            new Translate().execute();
        }

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
            progress = ProgressDialog.show(TranslateActivity.this, "Processing", "Please wait !");
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
        tvTranslatedResult.setText(result);

    }

}
