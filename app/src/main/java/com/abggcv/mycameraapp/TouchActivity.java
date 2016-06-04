package com.abggcv.mycameraapp;

/**
 * Created by ABGG on 04/06/2016.
 */

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Picture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.util.List;
import java.util.ListIterator;


@SuppressWarnings("deprecation")
public class TouchActivity extends Activity {

    private String TAG = "TouchActivity";
    private PreviewSurfaceView camView;
    private CameraPreview cameraPreview;
    private DrawingView drawingView;

    private int previewWidth = 1280; //3264;1440;1280;1280;2592
    private int previewHeight = 960; //2448;1080;720;960;1944

    private String extStorageDirectory = Environment.getExternalStorageDirectory().getPath() + "/MyCameraApp";

    private String PictureFileName = extStorageDirectory;

    //Menu
    private List<Camera.Size> mResolutionList;
    private MenuItem[] mResolutionMenuItems;
    private SubMenu mResolutionMenu;
    private MenuItem[] mMultiplePicItems;
    private SubMenu mMultiplePics;
    private MenuItem[] mCameraViewParamItems;
    private SubMenu mCameraViewParam;

    private int maxNumPics;
    private int lagTime;

    private CharSequence resList;

    //final String[] option = new String[] { "Add", "View", "Change", "Delete" };

    //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.select_dialog_item, option);

    //load OpenCV libs
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("img_pro");
                    //mOpenCvCameraView.enableView();
                    //mOpenCvCameraView.setOnTouchListener(Tutorial3Activity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_touch);

        camView = (PreviewSurfaceView) findViewById(R.id.preview_surface);
        camView.setVisibility(SurfaceView.VISIBLE);
        SurfaceHolder camHolder = camView.getHolder();

        cameraPreview = new CameraPreview(previewWidth, previewHeight);
        camHolder.addCallback(cameraPreview);
        camHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        camView.setListener(cameraPreview);
        //cameraPreview.changeExposureComp(-currentAlphaAngle);
        drawingView = (DrawingView) findViewById(R.id.drawing_surface);
        camView.setDrawingView(drawingView);

        String MyDirectory_path = extStorageDirectory;

        File file = new File(MyDirectory_path);
        if (!file.exists()) {
            file.mkdirs();
            Toast.makeText(getBaseContext(), "Path not found. Creating new directory", Toast.LENGTH_SHORT).show();
        }

        maxNumPics = 5; //in numbers
        lagTime = 100;  //in milisec
    }

    @Override
    public void onPause(){
        super.onPause();
        //sensorManager.unregisterListener(this);
        //mOrientationEventListener.disable();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.e(TAG, "Menu Created");

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.touch, menu);

        mResolutionMenu = menu.addSubMenu("Camera Resolution");
        mResolutionMenuItems = new MenuItem[1];
        mResolutionMenuItems[0] = mResolutionMenu.add(1, 0, Menu.NONE,"List");

        // Multiple pics
        mMultiplePics = menu.addSubMenu("Multiple Pics");
        mMultiplePicItems = new MenuItem[2];
        mMultiplePicItems[0] = mMultiplePics.add(2,0, Menu.NONE, "MaxNumPics");
        mMultiplePicItems[1] = mMultiplePics.add(2, 1, Menu.NONE, "Time Lag");

        //Show camera settings
        mCameraViewParam = menu.addSubMenu("View Camera Parameters");
        mCameraViewParamItems = new MenuItem[2];
        mCameraViewParamItems[0] = mCameraViewParam.add(3, 0, Menu.NONE, "Show");
        mCameraViewParamItems[1] = mCameraViewParam.add(3, 1, Menu.NONE, "Write YAML");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.i(TAG, "Menu option selected");

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        else if (item.getGroupId() == 1)
        {

            mResolutionList = cameraPreview.getResolutionList();

            mResolutionMenuItems = new MenuItem[mResolutionList.size()];

            ListIterator<Camera.Size> resolutionItr = mResolutionList.listIterator();
            int idx = 0;
            while (resolutionItr.hasNext()) {
                Camera.Size element = resolutionItr.next();
                mResolutionMenuItems[idx] = mResolutionMenu.add(1, idx, Menu.NONE,
                        Integer.valueOf(element.width).toString() + "x" + Integer.valueOf(element.height).toString());
                idx++;
            }
        }

        //menu option for user input
        else if(item.getGroupId() == 2)
        {
            if(item.getItemId()==0) {
                showInputDialog0();

            }
            else if(item.getItemId()==1) {
                showInputDialog1();
            }
        }

        else if(item.getGroupId()==3)
        {
            if(item.getItemId()==0)
                showCameraParam();
            else if(item.getItemId()==1)
                WriteBrightnessToYAML(PictureFileName + "/brightness.yaml");
        }

        return super.onOptionsItemSelected(item);
    }




    protected void showInputDialog0() {
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(TouchActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TouchActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //resultText.setText("Hello, " + editText.getText());
                        Toast.makeText(TouchActivity.this, "Max no of pics: " + editText.getText(),Toast.LENGTH_SHORT).show();
                        maxNumPics = Integer.parseInt(editText.getText().toString()); //editText.getText();
                        //Intent intent = new Intent(MainActivity.this, Tutorial3Activity.class);
                        //MainActivity.this.startActivity(intent);
                        //Tutorial3Activity();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    protected void showInputDialog1() {
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(TouchActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog1, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(TouchActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(TouchActivity.this, "Time lag: " + editText.getText() + " ms",Toast.LENGTH_SHORT).show();
                        lagTime = Integer.parseInt(editText.getText().toString());
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    /*
    protected void showResListDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Resolution");

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            // TODO Auto-generated method stub
            }
        });

        final AlertDialog dialog = builder.create();

        dialog.show();

    }
*/


    public void showCameraParam(){

        Camera.Parameters parameters = null;

        try {
            parameters = cameraPreview.mCamera.getParameters();
        } catch (Exception e) {
            Log.e("CameraParameters", "Exception in camera parameters", e);
        }

        if(parameters != null){

            Toast.makeText(this, "Focal length: " + parameters.getFocalLength() + "\nExposure Compensation: " +
                    parameters.getExposureCompensation() + "\nFocus mode: " + parameters.getFocusMode(), Toast.LENGTH_LONG).show();

        }
        else
            Toast.makeText(this, "Null Camera Parameters", Toast.LENGTH_SHORT).show();
    }


    @SuppressLint("SimpleDateFormat")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
            //Do something
            Log.i(TAG, "onKey event");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String currentDateandTime = sdf.format(new Date());
            PictureFileName += "/" + currentDateandTime;

            File file = new File(PictureFileName);
            if (!file.exists()) {
                file.mkdirs();
                //Toast.makeText(getBaseContext(), "Path not found. Creating new directory", Toast.LENGTH_SHORT).show();
            }

            //int count = 0;
            /*String fileName = Environment.getExternalStorageDirectory().getPath() +
                    "/captured_picture_";*/
            //mOpenCvCameraView.takePicture(fileName, count, maxNumPics, lagTime);

            //mHandler.postDelayed(TakePicture, 300);
            cameraPreview.CameraTakePicture(PictureFileName, maxNumPics, lagTime);

        }
        return true;
    }


    @SuppressWarnings("JniMissingFunction")
    public native void WriteBrightnessToYAML(String filePath);

}
