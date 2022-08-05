package com.example.androidopencvprac;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity {
    private CameraBridgeViewBase cameraBridgeViewBase;

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                Log.v("OpenCV Log", "OpenCV initialized");
                cameraBridgeViewBase.enableView();
            } else {
                super.onManagerConnected(status);
            }
        }
    };

    public Mat Canny(Mat img) {
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(img, img, new Size(5, 5), 0);
        Imgproc.medianBlur(img,img,3);
        Imgproc.Canny(img, img, 100, 200);
        return img;
    }


    public void ROI( Mat img, double w, double h) {

        Mat mask = Mat.zeros(img.rows(), img.cols(), img.type());


        Point[] rook_points = new Point[4];
        rook_points[0] = new Point(0, h * 1.0); //start drawing from 0 to 1 to 2 to3
        rook_points[1] = new Point(w * 0.45, h * 0.6);
        rook_points[2] = new Point(w * 0.55, h * 0.6);
        rook_points[3] = new Point(w * 1.0, h * 1.0);

        MatOfPoint matPt = new MatOfPoint();
        matPt.fromArray(rook_points);
        List<MatOfPoint> ppt = new ArrayList<MatOfPoint>();
        ppt.add(matPt);
        Imgproc.fillPoly(mask, ppt, new Scalar( 255 ));

        Core.bitwise_and(img, mask, img);
    }

    public void draw_the_line(Mat img, Mat img2) {

        Mat linesP = new Mat();

        Imgproc.HoughLinesP(img, linesP,6, Math.PI/180, 160, 40, 25);

        for (int x = 0; x < linesP.rows(); x++) {
            double[] l = linesP.get(x, 0);
            Imgproc.line(img2, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(0,255,0), 10, Imgproc.LINE_AA, 0);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(cvCameraViewListener);
    }

    @Override
    protected List<?extends CameraBridgeViewBase> getCameraViewList() { //override the method to get all camera views for opencv camera
        return Collections.singletonList(cameraBridgeViewBase);
    }

    private CameraBridgeViewBase.CvCameraViewListener2 cvCameraViewListener = new CameraBridgeViewBase.CvCameraViewListener2() {
        @Override
        public void onCameraViewStarted(int width, int height) {

        }

        @Override
        public void onCameraViewStopped() {

        }

        @Override
        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
            Mat frame = inputFrame.rgba();
            Mat rgbFrame = frame.clone();
            Canny(frame);
            ROI(frame, frame.size().width, frame.size().height);
            draw_the_line(frame, rgbFrame);

            return rgbFrame;
        }
    };

    @Override
    public void onPause() {  //override onPause method which will disable the camera view when app is paused
        super.onPause();
        if(cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    public void onResume() { //override onResume method which will check pass success if opencv is initialized
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV Log", "OpenCV not found, Initializing");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        } else {
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }
}









