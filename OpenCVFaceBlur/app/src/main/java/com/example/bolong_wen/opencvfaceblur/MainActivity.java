package com.example.bolong_wen.opencvfaceblur;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.view.*;
import android.view.View;
import android.view.View.OnTouchListener;
import android.graphics.Bitmap;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.android.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class MainActivity extends Activity implements OnTouchListener,CvCameraViewListener2{

    private static final String  TAG                 = "OpenCV Blur";
    private static final Scalar  FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    private Mat                    mRgba;
    private Mat                    mGray;
    private File mCascadeFile;
    private CascadeClassifier      mJavaDetector;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;
    private CameraBridgeViewBase   mOpenCvCameraView;
    private ImageView mImageView;
    private boolean isImageViewShown = false;
    Rect chooseFaceRect;
    private boolean isFaceRectChosen = false;
    private List<Point> ContourPointList;
    private MatOfPoint loadedContourPoint;
    private List<MatOfPoint> last_contour;

    static{ System.loadLibrary("opencv_java"); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_main);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mImageView = (ImageView) findViewById(R.id.cameraImageView);

        ContourPointList = new ArrayList<Point>();
        loadedContourPoint = new MatOfPoint();
        last_contour = new ArrayList<MatOfPoint>();


        //
        try {
            // load cascade file from application resources
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (mJavaDetector.empty()) {
                Log.e(TAG, "Failed to load cascade classifier");
                mJavaDetector = null;
            } else
                Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());


            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }

        mOpenCvCameraView.setOnTouchListener(MainActivity.this);
        mOpenCvCameraView.enableView();
    }


    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }

        MatOfRect faces = new MatOfRect();


        if (mJavaDetector != null)
            mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                    new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());



        Rect[] facesArray = faces.toArray();
//        for (int i = 0; i < facesArray.length; i++)
//            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        if (facesArray.length == 1)
        {
            chooseFaceRect = facesArray[0].clone();
            isFaceRectChosen = true;
        }
        return mRgba;
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {

        cameraView_OnTouch(v, event);
        isImageViewShown = true;
        return false;
    }

    private void cameraView_OnTouch(View view, MotionEvent event) {

        int rows = mRgba.rows();
        int cols = mRgba.cols();

        if(isFaceRectChosen){
            //
            ContourPointList.clear();
            last_contour.clear();

            //
            int newX = chooseFaceRect.x - chooseFaceRect.width/8;
            int newY = chooseFaceRect.y - chooseFaceRect.height/8;
            if(newX>0)
                chooseFaceRect.x = newX;
            if(newY>0)
                chooseFaceRect.y = newY;

            int newWidth = chooseFaceRect.width+chooseFaceRect.width/4;
            int newHeight= chooseFaceRect.height+chooseFaceRect.height/4;
            if(chooseFaceRect.x+newWidth<cols)
                chooseFaceRect.width = newWidth;
            if(chooseFaceRect.y+newHeight<rows)
                chooseFaceRect.height = newHeight;
            //
            for (int i = chooseFaceRect.x;i<chooseFaceRect.x+chooseFaceRect.width;i=i+10){
                Point readPoint = new Point();
                readPoint.x = Double.valueOf(i);
                readPoint.y = Double.valueOf(chooseFaceRect.y);
                ContourPointList.add(readPoint);
            }

            for (int j = chooseFaceRect.y;j<chooseFaceRect.y+chooseFaceRect.height;j=j+10){
                Point readPoint = new Point();
                readPoint.x = Double.valueOf(chooseFaceRect.x+chooseFaceRect.width);
                readPoint.y = Double.valueOf(j);
                ContourPointList.add(readPoint);
            }

            for (int i = chooseFaceRect.x+chooseFaceRect.width;i>chooseFaceRect.x;i=i-10){
                Point readPoint = new Point();
                readPoint.x = Double.valueOf(i);
                readPoint.y = Double.valueOf(chooseFaceRect.y+chooseFaceRect.height);
                ContourPointList.add(readPoint);
            }

            for (int j = chooseFaceRect.y+chooseFaceRect.height;j>chooseFaceRect.y;j=j-10){
                Point readPoint = new Point();
                readPoint.x = Double.valueOf(chooseFaceRect.x);
                readPoint.y = Double.valueOf(j);
                ContourPointList.add(readPoint);
            }
            loadedContourPoint.fromList(ContourPointList);
            last_contour.add(loadedContourPoint);

            isFaceRectChosen = false;
        }

        //
        Mat hole = new Mat(rows, cols, CvType.CV_8UC1, new Scalar(0));
        Imgproc.drawContours(hole, last_contour, last_contour.size()-1, new Scalar(255),-1);

        Mat frameBlur = new Mat(rows, cols, mRgba.type());
        Imgproc.GaussianBlur(mRgba, frameBlur, new Size(61, 61), 0);
        mRgba.copyTo(frameBlur, hole);//将原图像拷贝进遮罩图层

        mImageView.setImageBitmap(matToBitmap(frameBlur));
        // show imageView and hide cameraView
        mOpenCvCameraView.setVisibility(View.INVISIBLE);
        mImageView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        // TODO Auto-generated method stub

        if((keyCode == KeyEvent.KEYCODE_BACK)&&(event.getAction() == KeyEvent.ACTION_DOWN))
        {
            if (isImageViewShown)
            {
                mOpenCvCameraView.setVisibility(View.VISIBLE);
                mImageView.setVisibility(View.INVISIBLE);
                isImageViewShown = false;
            } else {
                finish();
                System.exit(0); //凡是非零都表示异常退出!0表示正常退出!
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private Bitmap matToBitmap(Mat image) {

        Bitmap bitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, bitmap);
        return bitmap;
    }
}
