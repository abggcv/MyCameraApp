package com.abggcv.mycameraapp;

/**
 * Created by ABGG on 04/06/2016.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.text.method.Touch;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class CameraPreview
        implements
        SurfaceHolder.Callback, Camera.PreviewCallback {

    public Camera mCamera;
    public Camera.Parameters params;
    private SurfaceHolder sHolder;

    public List<Camera.Size> supportedSizes;

    public int isCamOpen = 0;
    public boolean isSizeSupported = false;
    private int previewWidth, previewHeight, picWidth, picHeight;

    private Boolean TakePicture = false;

    private String NowPictureFileName;
    private int mTimeLag;
    private int mNumMaxPics;
    private int mPicId;

    private final static String TAG = "CameraPreview";

    private byte[][] images;

    android.os.Handler mHandler = new android.os.Handler();


    public CameraPreview(int width, int height) {
        Log.i("campreview", "Width = " + String.valueOf(width));
        Log.i("campreview", "Height = " + String.valueOf(height));
        previewWidth = width;
        previewHeight = height;

    }

    public List<Camera.Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPictureSizes();    //getSupportedPreviewSizes();
    }

    public Camera.Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }

    public void setResolution(Camera.Size resolution) {
        //disconnectCamera();
        picWidth = resolution.width;
        picHeight = resolution.height;
        params.setPictureSize(picWidth, picHeight);
        mCamera.setParameters(params);
        //connectCamera(getWidth(), getHeight());
    }

    @Override
    public void onPreviewFrame(byte[] arg0, Camera arg1)
    {
        // At preview mode, the frame data will push to here.
        // But we do not want these data.
    }

    public int isCamOpen() {
        return isCamOpen;
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        isCamOpen = 0;
    }



    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
    {
        mCamera.stopPreview();
        Camera.Parameters parameters = mCamera.getParameters();
        // Set the camera preview size
        parameters.setPreviewSize(previewWidth, previewHeight);
        // Set the take picture size, you can set the large size of the camera supported.
        parameters.setPictureSize(picWidth, picHeight);

        //mCamera.setParameters(parameters);

        mCamera.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0)
    {
        Log.e(TAG, "Surface Created");

        if(mCamera== null) {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);

            params = mCamera.getParameters();
            params.setPreviewSize(previewWidth, previewHeight);
            params.setPictureSize(picWidth, picHeight);

            params.setRotation(0);
            mCamera.setDisplayOrientation(90);
        }

        try {
            mCamera.setParameters(params);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        mCamera.startPreview();

        try
        {
            // If did not set the SurfaceHolder, the preview area will be black.
            mCamera.setPreviewDisplay(arg0);
            mCamera.setPreviewCallback(this);
        }
        catch (IOException e)
        {
            mCamera.release();
            mCamera = null;
        }
    }






    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();

    }

    /**
     * Called from PreviewSurfaceView to set touch focus.
     *
     * @param - Rect - new area for auto focus
     */
    public void doTouchFocus(final Rect tfocusRect) {
        Log.i(TAG, "TouchFocus");
        try {
            final List<Camera.Area> focusList = new ArrayList<Camera.Area>();
            Camera.Area focusArea = new Camera.Area(tfocusRect, 1000);
            focusList.add(focusArea);

            Camera.Parameters para = mCamera.getParameters();
            para.setFocusAreas(focusList);
            para.setMeteringAreas(focusList);
            mCamera.setParameters(para);

            mCamera.autoFocus(myAutoFocusCallback);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Unable to autofocus");
        }

    }

    /**
     * AutoFocus callback
     */
    Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback(){

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            if (arg0){
                //mCamera.cancelAutoFocus();
            }
        }
    };


    // Take picture interface
    public void CameraTakePicture(String FileName, int maxNumPics, int lagtime)
    {
        mNumMaxPics = maxNumPics;
        images = new byte[maxNumPics][];
        mTimeLag = lagtime;
        TakePicture = false;
        NowPictureFileName = FileName + "/MyPic";
        //mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
        mPicId = 0;
        //submitFocusAreaRect(focusRect, meteringRect);
        //mCamera.autoFocus(myAutoFocusCallback);
        mCamera.takePicture(shutterCallback, rawPictureCallback, jpegPictureCallback);


    }


    // Take picture callback

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback()
    {
        public void onShutter()
        {
            // Just do nothing.
        }
    };

    Camera.PictureCallback rawPictureCallback = new Camera.PictureCallback()
    {
        public void onPictureTaken(byte[] arg0, Camera arg1)
        {
            // Just do nothing.
        }
    };


    public Runnable runTakePicture() {
        return new Runnable() {
            public void run() {
                mCamera.takePicture(shutterCallback, rawPictureCallback, jpegPictureCallback);
                //PictureCallback mjpeg = ;
                //mCamera.takePicture(new Camera.ShutterCallback() { @Override public void onShutter() { } },null, jpegPictureCallback);
                //mCamera.takePicture(null, null, jpegCallBack);
            }
        };
    }

    Camera.PictureCallback jpegPictureCallback = new Camera.PictureCallback()
    {
        public void onPictureTaken(byte[] data, Camera arg1)
        {
            //Toast.makeText(, "save picture to file", Toast.LENGTH_SHORT).show();
            // Save the picture.

            try {
                Log.i(TAG, "Saving the picture");
                images[mPicId] = new byte[1];
                images[mPicId] = Arrays.copyOf(data, data.length);
            }
            catch (Exception e)
            {
                Log.e("PictureDemo", "Exception in photoCallback", e);
                e.printStackTrace();
            }

            mPicId++;

            if (mPicId < mNumMaxPics) {
                mCamera.startPreview();
                mHandler.postDelayed(runTakePicture(), mTimeLag);
                //mCamera.takePicture(shutterCallback, rawPictureCallback, jpegPictureCallback);
            }

            else {
                //Toast.makeText(, "Multiple image capture finished", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Multiple image capture finished");

                mCamera.stopPreview();

                for (int i = 0; i < mPicId; i++) {

                    String picName = NowPictureFileName + "_" + i + "_of_" + mNumMaxPics + "_at_" + mTimeLag + "ms.jpg";

                    if(images[i].length > 0) {

                        Log.i(TAG, "Computing brightness for pic-" + i);
                        Log.i(TAG, "Size of byte array: " + images[i].length);
                        //call compute brightness
                        ComputeBrightness(picWidth, picHeight, images[i], picName);

                        try {
                            Log.i(TAG, "Writing image-" + i + " to file: " + picName);
                            FileOutputStream out = new FileOutputStream(picName);
                            out.write(images[i]); //original picture
                            out.close();
                            System.gc();
                        } catch (IOException e) {
                            Log.e("PictureDemo", "Exception in photoCallback", e);
                            e.printStackTrace();
                        }

                    }
                    else
                        Log.i(TAG, "Skipping empty image-" + i);
                }

                mCamera.startPreview();
            }

        }
    };

    @SuppressWarnings("JniMissingFunction")
    public native void ComputeBrightness(int w, int h, byte[] byteArray, String filePath);


}
