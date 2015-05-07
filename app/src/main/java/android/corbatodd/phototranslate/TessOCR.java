package android.corbatodd.phototranslate;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Environment;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.core.Rect;

import java.io.File;
import java.util.List;

/**
 * Created by corbatodd on 4/2/15.
 */
public class TessOCR {

    private Bitmap bitmap;
    private String result;

    private int[] wordConfidences;
    private int meanConfidence;

    private List<android.graphics.Rect> regionBoundingBoxes;
    private List<android.graphics.Rect> textLineBoundingBoxes;
    private List<android.graphics.Rect> wordBoundingBoxes;
    private List<android.graphics.Rect> stripBoundingBoxes;
    private List<android.graphics.Rect> characterBoundingBoxes;

    private Paint paint;

    public TessOCR(Bitmap bitmap, String result, int[] wordConfidences, int meanConfidence, List<android.graphics.Rect> regionBoundingBoxes, List<android.graphics.Rect> textLineBoundingBoxes, List<android.graphics.Rect> wordBoundingBoxes, List<android.graphics.Rect> stripBoundingBoxes, List<android.graphics.Rect> characterBoundingBoxes) {
        this.bitmap = bitmap;
        this.result = result;
        this.wordConfidences = wordConfidences;
        this.meanConfidence = meanConfidence;
        this.regionBoundingBoxes = regionBoundingBoxes;
        this.textLineBoundingBoxes = textLineBoundingBoxes;
        this.wordBoundingBoxes = wordBoundingBoxes;
        this.stripBoundingBoxes = stripBoundingBoxes;
        this.characterBoundingBoxes = characterBoundingBoxes;

        this.paint = new Paint();
    }

    public TessOCR() {
        this.paint = new Paint();
    }

    public Bitmap getBitmap() {
        return getAnnotatedBitmap();
    }

    public Bitmap getAnnotatedBitmap() {
        Canvas canvas = new Canvas(bitmap);

        //Draw boudning boxes around each word
        for (int i = 0; i < wordBoundingBoxes.size(); i++) {
            paint.setAlpha(0xFF);
            paint.setColor(0xFF00CCFF);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            android.graphics.Rect rect = wordBoundingBoxes.get(i);
            canvas.drawRect(rect,paint);
        }

        return bitmap;
    }

    public String getResult() {
        return result;
    }

    public int[] getWordConfidences() {
        return wordConfidences;
    }

    public int getMeanConfidence() {
        return meanConfidence;
    }

    public Point getBitmapDimensions() {
        return new Point(bitmap.getWidth(), bitmap.getHeight());
    }

    public List<android.graphics.Rect> getRegionBoundingBoxes() {
        return regionBoundingBoxes;
    }

    public List<android.graphics.Rect> getTextLineBoundingBoxes() {
        return textLineBoundingBoxes;
    }

    public List<android.graphics.Rect> getWordBoundingBoxes() {
        return wordBoundingBoxes;
    }

    public List<android.graphics.Rect> getStripBoundingBoxes() {
        return stripBoundingBoxes;
    }

    public List<android.graphics.Rect> getCharacterBoundingBoxes() {
        return characterBoundingBoxes;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setWordConfidences(int[] wordConfidences) {
        this.wordConfidences = wordConfidences;
    }

    public void setMeanConfidence(int meanConfidence) {
        this.meanConfidence = meanConfidence;
    }

    public void setRegionBoundingBoxes(List<android.graphics.Rect> regionBoundingBoxes) {
        this.regionBoundingBoxes = regionBoundingBoxes;
    }

    public void setTextLineBoundingBoxes(List<android.graphics.Rect> textLineBoundingBoxes) {
        this.textLineBoundingBoxes = textLineBoundingBoxes;
    }

    public void setWordBoundingBoxes(List<android.graphics.Rect> wordBoundingBoxes) {
        this.wordBoundingBoxes = wordBoundingBoxes;
    }

    public void setStripBoundingBoxes(List<android.graphics.Rect> stripBoundingBoxes) {
        this.stripBoundingBoxes = stripBoundingBoxes;
    }

    public void setCharacterBoundingBoxes(List<android.graphics.Rect> characterBoundingBoxes) {
        this.characterBoundingBoxes = characterBoundingBoxes;
    }

    @Override
    public String toString() {
        return result + " " + meanConfidence;
    }
    /*

    private TessBaseAPI TessAPI;

    public TessOCR(){
        TessAPI = new TessBaseAPI();
        String dataPath = Environment.getExternalStorageDirectory() + "/Photo Translate/";
        String language = "eng";
        File dir = new File(dataPath + "tessdata/");
        if (!dir.exists())
            dir.mkdirs();
        TessAPI.init(dataPath, language);
    }

    public String getOCRResult(Bitmap bitmap){

        //TessAPI.setVariable(TessAPI.VAR_CHAR_WHITELIST, "1234567890"+"qwertyuiopasdfghjklzxcvbnm");
        TessAPI.setVariable(TessAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-[]}{" + ";:'\"\\|~`,./<>?");

        TessAPI.setImage(bitmap);
        String result = TessAPI.getUTF8Text();
        return result;
    }
    */
}
