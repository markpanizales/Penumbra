package com.wormz.penumbra.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.wormz.penumbra.GMailSender;
import com.wormz.penumbra.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;


/**
 * Created by macx-mini on 3/9/15.
 */
public class CameraFragment extends Fragment {
    private final static String TAG = "CameraFragment";
    private final static String EMAIL_SUBJECT = "Password Incorrect Pin";
    private final static String EMAIL_BODY = "This is body";

    // The size and content for image resize
    private final int SIZE_DP = 500;
    private Context mContext;
    private static final int IMAGE_MAX_SIZE = 640;


    // Camera Layouts
    private FrameLayout frameLayout;
    private CameraPreview cPreview;
    private Camera mCamera;

    // Camera orientation handler
    private OrientationEventListener mOrientationEventListener;
    private int mOrientation = -1;

    // Values for camera shutter button rotation
    private static final int ORIENTATION_PORTRAIT_NORMAL = 1;
    private static final int ORIENTATION_PORTRAIT_INVERTED = 2;
    private static final int ORIENTATION_LANDSCAPE_NORMAL = 3;
    private static final int ORIENTATION_LANDSCAPE_INVERTED = 4;

    // The variable to handle the image taken
    private Bitmap takenBitmap;

    // Camera focus value. Front facing camera - as of now - doesn't have focus
    // function.
    private boolean cameraFocusOn = true;

    private boolean isFrontCamera = false;

    private View mView;

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback(){

        @Override
        public void onShutter() {
            //mProgressContainer.setVisibility(View.VISIBLE);
            Log.i(TAG, "Picture Taken...");
        }
    };

    /**
     * The call back for taking a picture
     */
    Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.e(TAG, "mPicture/onPictureTaken");

            initializeTakePicture(data);

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_camera, container, false);

        // The content for handling the image size in full screen mode
        mContext = getActivity();

        // initialize the camera feature
        isFrontCamera = !isFrontCamera;
        cameraFocusOn = !cameraFocusOn;
        createCamera(isFrontCamera);

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyCamera();

    }

    /**
     * The class object for camera handling
     *
     */
    public class CameraPreview extends SurfaceView implements
            SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        @SuppressWarnings("deprecation")
        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;
            //setDisplayOrientation(mCamera, 90);

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw
            // the preview.
            try {

//                mCamera.setPreviewDisplay(holder);
//                mCamera.startPreview();

                if (mCamera != null){
                    //mCamera.setPreviewDisplay(holder);
                    SurfaceTexture surfaceTexture = new SurfaceTexture(10);
                    mCamera.setPreviewTexture(surfaceTexture);

                }

            } catch (IOException e) {
                Log.d("TAG", "Error setting camera preview: " + e.getMessage());
            }

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {

            // surface changed is empty on the original app
            if (mCamera == null)
                return;

            try{
                mCamera.setDisplayOrientation(90);
                mCamera.startPreview();

                // Delay first before taking a picture. Allow the camera to be fully open
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        executeTakePicture();
                    }
                }, 700);


            }catch (Exception e){
                Log.e(TAG, "Could not start preview", e);
                mCamera.release();
                mCamera = null;

            }


        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // If your preview can change or rotate, take care of those events
            // here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {

                if (mCamera != null) {
                    // mCamera.release();
                    // mCamera = null;
                    mCamera.stopPreview();

                }

            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
                Log.d(TAG, "Error stoping camera: " + e.getMessage());
            }

        }


    }

    // TODO:initialize taken picture
    private void initializeTakePicture(byte[] data) {
        boolean success = true;

        // create a bitmap from the camera
        takenBitmap = createBitmap(data);

        // create a filename
        String filename = UUID.randomUUID().toString() + ".jpg";

        FileOutputStream fos = null;

        try {


            // call the method to compute the full screen
            int p = getFullScreen();

            takenBitmap = Bitmap.createScaledBitmap(takenBitmap, p, p,
                    false);

            // Compress the bitmap to fit in 100kb
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            Boolean result = takenBitmap.compress(
                    Bitmap.CompressFormat.JPEG, 90, stream);

            Log.d(TAG, "onPictureTaken result compress: " + result);

            // create the bitmap to file
            //fos = new FileOutputStream(pictureFile);
            fos = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(stream.toByteArray());
            fos.close();

            // assign the value of byte array for resizing the image
            byte[] byteArray = stream.toByteArray();

            // close the byte array
            stream.close();
            stream = null;

            // resize the image for bigger view
            Bitmap b = BitmapFactory.decodeByteArray(byteArray, 0,
                    byteArray.length);

            b.recycle();
            b = null;

            //addElement(takenBitmap);

            takenBitmap.recycle();
            takenBitmap = null;



            Log.d(TAG, " File: " + filename);

        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error writing to file ", e);
            success = false;
        } catch (IOException e) {
            Log.e(TAG, "Error writing to file " + filename, e);
            success = false;

        }finally {
            try{
                if (fos != null)
                    fos.close();

            }catch (Exception e){
                Log.e(TAG, "Error closing file " + filename, e);
                success = false;
            }
        }

        if (success){
            Log.i(TAG, "JPEG saved at " + filename);
//            Intent i = new Intent();
//            i.putExtra(EXTRA_PHOTO_FILENAME, filename);
//            getActivity().setResult(Activity.RESULT_OK, i);

            String path = getActivity().getFileStreamPath(filename).getAbsolutePath();
            Log.i(TAG, "Your Complete path: " + path);

            // Send an Email to receipient
            sendMail(path);
        }

//        else{
//            getActivity().setResult(Activity.RESULT_CANCELED);
//        }

    }


    /**
     * @param data
     * @return The method to create a bitmap from taking a picture
     */
    private Bitmap createBitmap(byte[] data) {
        Log.e(TAG, "createBitmap");
        int orientation = -1;

        // Create a bitmap file
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        // Transform the captured image into a byte array for manipulation
        // of its scale and size
        BitmapFactory.decodeByteArray(data, 0, data.length, o);

        Log.e(TAG, "createBitmap : org : height = " + o.outHeight
                + " / width = " + o.outWidth);

        // Find the correct scale value. It should be the power of 2.
        int scale = 1;
        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
            scale = (int) Math.pow(
                    2,
                    (int) Math.round(Math.log(IMAGE_MAX_SIZE
                            / (double) Math.max(o.outHeight, o.outWidth))
                            / Math.log(0.5)));
        }

        Log.e(TAG, "Scale: " + scale);

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;

        Log.e(TAG, "o2: " + o2);

        Bitmap bMap = BitmapFactory.decodeByteArray(data, 0, data.length, o2);

        Log.e(TAG, "bMap: " + bMap);

        // Front camera got some issues on orientation. This is some hack
        if (isFrontCamera) {
            if (mOrientation == 1) {
                orientation = 270;

            } else if (mOrientation == 4) {
                orientation = 180;

            } else {
                // inspect for rotation
                if (mOrientation < 3) {
                    orientation = 90;

                } else {
                    orientation = 0;

                }

            }

        } else {
            // /back camera picture taken...inspect for rotation
            if (mOrientation < 3) {
                orientation = 90;

            } else {
                orientation = 0;
            }
        }

        Log.d(TAG, "Orientation: " + orientation + " camera status: "
                + mOrientation);
        Log.e(TAG, "createBitmap : dest : height = " + bMap.getHeight()
                + " / width = " + bMap.getWidth());
        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        Bitmap bMapRotate = Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(),
                bMap.getHeight(), matrix, true);

        Log.d(TAG, "Releas the memory of bmap");

        if (mOrientation < 3) {
            bMap.recycle();
            bMap = null;
        }

        return bMapRotate;

    }


    /**
     * @return
     * The method to compute the full screen of the phone
     */
    private int getFullScreen(){
        final float scale = mContext.getResources().getDisplayMetrics().density;
        int p = (int) (SIZE_DP * scale + 0.5f);

        return p;
    }

    /**
     * @param frontFace
     *            The method for camera handling
     */
    private void createCamera(boolean frontFace) {
        Log.d(TAG, "Create camera method initialized...Orientation changed");
        createOrientation();

        Log.d(TAG, "Instantiate the camera");
        mCamera = getCameraInstance(frontFace);

        Log.d(TAG, "Get the camera preview");
        // Create our Preview view and set it as the content of our activity.
        cPreview = new CameraPreview(getActivity(), mCamera);

        Log.d(TAG, "Set the camera layout features");
        frameLayout = (FrameLayout) mView.findViewById(R.id.camera_preview);
        frameLayout.addView(cPreview);

    }

    /**
     * The method to determine the phone orientation
     */
    private void createOrientation() {
        if (mOrientationEventListener == null) {

            mOrientationEventListener = new OrientationEventListener(
                    getActivity(), SensorManager.SENSOR_DELAY_NORMAL) {

                @Override
                public void onOrientationChanged(int orientation) {
                    // determine our orientation based on sensor response
                    int lastOrientation = mOrientation;

                    if (orientation >= 315 || orientation < 45) {
                        if (mOrientation != ORIENTATION_PORTRAIT_NORMAL) {
                            mOrientation = ORIENTATION_PORTRAIT_NORMAL;
                        }
                    } else if (orientation < 315 && orientation >= 225) {
                        if (mOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
                            mOrientation = ORIENTATION_LANDSCAPE_NORMAL;
                        }
                    } else if (orientation < 225 && orientation >= 135) {
                        if (mOrientation != ORIENTATION_PORTRAIT_INVERTED) {
                            mOrientation = ORIENTATION_PORTRAIT_INVERTED;
                        }
                    } else { // orientation <135 && orientation > 45
                        if (mOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
                            mOrientation = ORIENTATION_LANDSCAPE_INVERTED;
                        }
                    }

                }
            };

        }

        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }

    }

    /**
     * @param frontFace
     * @return A safe way to get an instance of the Camera object.
     */
    @SuppressLint({ "InlinedApi", "NewApi" })
    public static Camera getCameraInstance(boolean frontFace) {
        Camera c = null;
        try {

            // Open the default i.e. the first rear facing camera.
            if (frontFace) {
                c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

            } else {
                c = Camera.open(); // attempt to get a Camera instance
            }

        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.d("CameraFragment", "Error in camera instance: " + e.toString());

        }
        return c; // returns null if camera is unavailable
    }

    /**
     * The method to take a picture of the subject
     */
    private void executeTakePicture(){
        Log.e(TAG, "Capture camera");

//        mCamera.autoFocus(new Camera.AutoFocusCallback() {
//            @Override
//            public void onAutoFocus(boolean arg0, Camera arg1) {
//                mCamera.takePicture(mShutterCallback, null, mPicture);
//
//            }
//        });

        mCamera.takePicture(mShutterCallback, null, mPicture);
    }

    /**
     * Release the camera
     */
    private void destroyCamera() {
        Log.d(TAG, "Destroy Camera");
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;

            frameLayout.removeView(cPreview);

            Log.d(TAG, "Camera has been destroyed");

        }

    }


    private void sendMail(String fileName){
        new SendMailTask().execute(fileName);
    }

    class SendMailTask extends AsyncTask<String, Void, Boolean> {
        GMailSender sender;

        public SendMailTask() {
            // Use properties to keep save the api keys
            Properties prop = new Properties();

            InputStream input = null;


            String emailSender = null;
            String emailSenderPassword = null;

            try {
                input = getActivity().getBaseContext().getAssets().open("penumbra.properties");

                // load a properties file
                prop.load(input);

                Log.d(TAG, "emailSender properties: " + prop.getProperty("emailSender") + ", emailSenderPassword: " + prop.getProperty("emailSenderPassword"));

                emailSender = prop.getProperty("emailSender");
                emailSenderPassword = prop.getProperty("emailSenderPassword");


            } catch (IOException io) {
                io.printStackTrace();
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            sender = new GMailSender(emailSender, emailSenderPassword);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Log.i(TAG, "SendMailTask params: " + params[0]);

                // Attach the image of the taken user
                sender.addAttachment(params[0], "Image Attachment");

                // send the email
                sender.sendMail(EMAIL_SUBJECT, EMAIL_BODY, getResources().getString(R.string.app_name), "makspanizales@gmail.com");

            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            getActivity().finish();
        }
    }


}
