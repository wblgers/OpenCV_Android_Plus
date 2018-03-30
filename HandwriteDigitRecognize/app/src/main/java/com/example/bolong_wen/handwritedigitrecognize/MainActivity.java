package com.example.bolong_wen.handwritedigitrecognize;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bolong_wen.handwritedigitrecognize.HandWriteView;
import org.opencv.android.Utils;

import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvSVM;
import org.opencv.objdetect.CascadeClassifier;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends Activity {
    private static final String  TAG                 = "Hand writing";
    HandWriteView mHandWriteView;
    CvSVM mClassifier;
    File mSvmModel;
    TextView mResultView;

    static{ System.loadLibrary("opencv_java"); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandWriteView = (HandWriteView) findViewById(R.id.handWriteView);
        mResultView = (TextView) findViewById(R.id.resultShow);


        Button mRecognizeBtn = (Button) findViewById(R.id.btnRecognize);
        mRecognizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonRecognizeOnClick(view);
            }
        });

        Button mClearDrawBtn = (Button) findViewById(R.id.btnClear);
        mClearDrawBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonClearDrawOnClick(view);
            }
        });

        mClassifier = new CvSVM();

        //
        try {
            // load cascade file from application resources
            InputStream is = getResources().openRawResource(R.raw.mnist);
            File mnist_modelDir = getDir("mnist_model", Context.MODE_PRIVATE);
            mSvmModel = new File(mnist_modelDir, "mnist.xml");
            FileOutputStream os = new FileOutputStream(mSvmModel);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            mClassifier.load(mSvmModel.getAbsolutePath());

            mnist_modelDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
    }

    private void buttonRecognizeOnClick(View v){
        Bitmap tmpBitmap = mHandWriteView.returnBitmap();
        if(null == tmpBitmap)
            return;
        Mat tmpMat = new Mat(tmpBitmap.getHeight(),tmpBitmap.getWidth(),CvType.CV_8UC3);
        Mat saveMat = new Mat(tmpBitmap.getHeight(),tmpBitmap.getWidth(),CvType.CV_8UC1);

        Utils.bitmapToMat(tmpBitmap,tmpMat);

        Imgproc.cvtColor(tmpMat, saveMat, Imgproc.COLOR_RGBA2GRAY);

//        final String galleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
//        String picturePath = galleryPath+"/handwrite.bmp";
//        if(!Highgui.imwrite(picturePath,saveMat)){
//            Toast.makeText(getApplicationContext(),"Failed to save the picture",Toast.LENGTH_SHORT).show();
//        }
//        else
//        {
//            Toast.makeText(getApplicationContext(),"Succeed to save the picture",Toast.LENGTH_SHORT).show();
//        }
        //
        int imgVectorLen = 28 * 28;
        Mat dstMat = new Mat(28,28,CvType.CV_8UC1);
        Mat tempFloat = new Mat(28,28,CvType.CV_32FC1);

        Imgproc.resize(saveMat,dstMat,new Size(28,28));
        dstMat.convertTo(tempFloat, CvType.CV_32FC1);

        Mat predict_mat = tempFloat.reshape(0,1).clone();
        Core.normalize(predict_mat,predict_mat,0.0,1.0,Core.NORM_MINMAX);

        int response = (int)mClassifier.predict(predict_mat);
        mResultView.setText(mResultView.getText()+String.valueOf(response));
        //Toast.makeText(getApplicationContext(),"The predict label is "+String.valueOf(response),Toast.LENGTH_SHORT).show();

    }

    private void buttonClearDrawOnClick(View v){
        mResultView.setText("The recognition result is: ");
        mHandWriteView.clearDraw();
    }
}
