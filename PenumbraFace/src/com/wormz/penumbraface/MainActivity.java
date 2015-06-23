package com.wormz.penumbraface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnClickListener,
		CvCameraViewListener2 {

	private static final String TAG = "MainActivity";
	private static final Scalar FACE_RECT_COLOR = new Scalar(0, 255, 0, 255);
	public static final int JAVA_DETECTOR = 0;

	// control options
	public static final int TRAINING = 0;
	public static final int SEARCHING = 1;
	public static final int IDLE = 2;

	// camera
	private static final int mFrontCamera = 1;
	private static final int mBackCamera = 2;

	// The face status
	private int mFaceState = IDLE;

	private MenuItem mBackCameraMenu;
	private MenuItem mFrontCameraMenu;

	// Open CV color manipulation
	private Mat mRgba;
	private Mat mGray;
	private File mCascadeFile;
	private CascadeClassifier mJavaDetector;

	// Detection feature
	private int mDetectorType = JAVA_DETECTOR;
	private String[] mDetectorName;

	// Size for face capture
	private float mRelativeFaceSize = 0.2f;
	private int mAbsoluteFaceSize = 0;

	// The path for the file
	private String mPath = "";

	private CameraView mOpenCvCameraView;
	private int mChooseCamera = mBackCamera;

	private EditText mInputDescription;
	private TextView mFaceName;
	private ImageView mFaceRecognized;
	private Bitmap mBitmap;
	private Handler mHandler;

	private PersonRecognizer mPersonRecognizer;
	private ToggleButton mRecord;
	private ToggleButton mTrain;
	private ToggleButton mSearch;
	private Button mViewAll;
	private ImageView mSwitchCamera;

	private TextView mState;
	com.googlecode.javacv.cpp.opencv_contrib.FaceRecognizer faceRecognizer;

	private static final long MAX_IMAGE = 10;
	private int countImages = 0;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");

				// Load native library after(!) OpenCV initialization
				// System.loadLibrary("detection_based_tracker");

				mPersonRecognizer = new PersonRecognizer(mPath);
				String s = getResources().getString(R.string.training);
				Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG)
						.show();
				mPersonRecognizer.load();

				try {
					// load cascade file from application resources
					InputStream is = getResources().openRawResource(
							R.raw.lbpcascade_frontalface);
					File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
					mCascadeFile = new File(cascadeDir, "lbpcascade.xml");
					FileOutputStream os = new FileOutputStream(mCascadeFile);

					byte[] buffer = new byte[4096];
					int bytesRead;
					while ((bytesRead = is.read(buffer)) != -1) {
						os.write(buffer, 0, bytesRead);
					}
					is.close();
					os.close();

					mJavaDetector = new CascadeClassifier(
							mCascadeFile.getAbsolutePath());
					if (mJavaDetector.empty()) {
						Log.e(TAG, "Failed to load cascade classifier");
						mJavaDetector = null;
					} else
						Log.i(TAG, "Loaded cascade classifier from "
								+ mCascadeFile.getAbsolutePath());

					// mNativeDetector = new
					// DetectionBasedTracker(mCascadeFile.getAbsolutePath(), 0);

					cascadeDir.delete();

				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
				}

				mOpenCvCameraView.enableView();

			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;

			}
		}
	};

	public MainActivity() {
		mDetectorName = new String[2];
		mDetectorName[JAVA_DETECTOR] = "Java";

		Log.i(TAG, "Instantiated new " + this.getClass());

	}

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_main);

		mOpenCvCameraView = (CameraView) findViewById(R.id.camera_view);

		mOpenCvCameraView.setCvCameraViewListener(this);

		mPath = getFilesDir() + "/penumbrafaceOCV/";
		

		mFaceRecognized = (ImageView) findViewById(R.id.imageView_face_recognized);
		mFaceName = (TextView) findViewById(R.id.textView_face_name);
		mFaceName.setVisibility(View.INVISIBLE);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.obj == "IMG") {
					Canvas canvas = new Canvas();
					canvas.setBitmap(mBitmap);
					mFaceRecognized.setImageBitmap(mBitmap);
					if (countImages >= MAX_IMAGE - 1) {
						mRecord.setChecked(false);
						recordOnclick();
					}
				} else {
					mFaceName.setText(msg.obj.toString());

				}
			}
		};

		mInputDescription = (EditText) findViewById(R.id.edittext_input_description);
		mInputDescription.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((mInputDescription.getText().toString().length() > 0)
						&& (mTrain.isChecked()))
					mRecord.setVisibility(View.VISIBLE);
				else
					mRecord.setVisibility(View.INVISIBLE);

				return false;
			}
		});

		mInputDescription.setVisibility(View.INVISIBLE);

		mViewAll = (Button) findViewById(R.id.button_view_all);
		mViewAll.setOnClickListener(this);

		mRecord = (ToggleButton) findViewById(R.id.togglebutton_record);
		mRecord.setOnClickListener(this);
		mRecord.setVisibility(View.INVISIBLE);

		mSearch = (ToggleButton) findViewById(R.id.togglebutton_search);
		mSearch.setOnClickListener(this);

		mTrain = (ToggleButton) findViewById(R.id.togglebutton_train);
		mTrain.setOnClickListener(this);

		mState = (TextView) findViewById(R.id.idle);
		mSwitchCamera = (ImageView) findViewById(R.id.imagebutton_switch_camera);
		mSwitchCamera.setOnClickListener(this);

		boolean success = (new File(mPath)).mkdirs();
		if (!success) {
			Log.e("Error", "Error creating directory");
		}

	}

	private void recordOnclick() {
		if (mRecord.isChecked())
			mFaceState = TRAINING;
		else {
			countImages = 0;
			mFaceState = IDLE;
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mOpenCvCameraView.disableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		mGray = new Mat();
		mRgba = new Mat();

	}

	@Override
	public void onCameraViewStopped() {
		mGray.release();
		mRgba.release();

	}

	@Override
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

		if (mDetectorType == JAVA_DETECTOR) {
			if (mJavaDetector != null)
				mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2,
						2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
						new Size(mAbsoluteFaceSize, mAbsoluteFaceSize),
						new Size());
		} else {
			Log.e(TAG, "Detection method is not selected!");
		}

		Rect[] facesArray = faces.toArray();

		if ((facesArray.length == 1) && (mFaceState == TRAINING)
				&& (countImages < MAX_IMAGE)
				&& (!mInputDescription.getText().toString().isEmpty())) {

			Mat m = new Mat();
			Rect r = facesArray[0];

			m = mRgba.submat(r);
			mBitmap = Bitmap.createBitmap(m.width(), m.height(),
					Bitmap.Config.ARGB_8888);

			Utils.matToBitmap(m, mBitmap);

			Message msg = new Message();
			String textTochange = "IMG";
			msg.obj = textTochange;
			mHandler.sendMessage(msg);
			if (countImages < MAX_IMAGE) {
				mPersonRecognizer
						.add(m, mInputDescription.getText().toString());
				countImages++;
			}

		} else if ((facesArray.length > 0) && (mFaceState == SEARCHING)) {
			Mat m = new Mat();
			m = mGray.submat(facesArray[0]);
			mBitmap = Bitmap.createBitmap(m.width(), m.height(),
					Bitmap.Config.ARGB_8888);

			Utils.matToBitmap(m, mBitmap);
			Message msg = new Message();
			String textTochange = "IMG";
			msg.obj = textTochange;
			mHandler.sendMessage(msg);

			textTochange = mPersonRecognizer.predict(m);
			msg = new Message();
			msg.obj = textTochange;
			mHandler.sendMessage(msg);

		}
		for (int i = 0; i < facesArray.length; i++)
			Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(),
					FACE_RECT_COLOR, 3);

		return mRgba;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(TAG, "called onCreateOptionsMenu");
		if (mOpenCvCameraView.numberCameras() > 1) {
			mBackCameraMenu = menu.add(getResources().getString(
					R.string.front_camera));
			mFrontCameraMenu = menu.add(getResources().getString(
					R.string.back_camera));
		} else {
			mSwitchCamera.setVisibility(View.INVISIBLE);

		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

		mBackCameraMenu.setChecked(false);
		mFrontCameraMenu.setChecked(false);
		if (item == mBackCameraMenu) {
			mOpenCvCameraView.setCamFront();
			mChooseCamera = mFrontCamera;
		} else if (item == mFrontCameraMenu) {
			mChooseCamera = mBackCamera;
			mOpenCvCameraView.setCamBack();

		}

		item.setChecked(true);

		return true;
	}

	private void setState(String value) {
		mState.setText(value);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		switch (id) {
		case R.id.button_view_all:
			showButtonCatalog();
			break;

		case R.id.togglebutton_train:
			showTrain();
			break;

		case R.id.togglebutton_record:
			recordOnclick();
			break;

		case R.id.imagebutton_switch_camera:
			showSwitchCamera();
			break;

		case R.id.togglebutton_search:
			showSearch();
			break;

		default:
			break;
		}

	}

	private void showButtonCatalog() {
		Intent i = new Intent(MainActivity.this, ImageGallery.class);
		i.putExtra("path", mPath);
		startActivity(i);

	}

	private void showTrain() {
		if (mTrain.isChecked()) {
			setState("");

			mSearch.setVisibility(View.INVISIBLE);
			mFaceName.setVisibility(View.VISIBLE);
			mInputDescription.setVisibility(View.VISIBLE);

			mFaceName.setText(getResources().getString(R.string.face_name));

			if (mInputDescription.getText().toString().length() > 0) {
				mRecord.setVisibility(View.VISIBLE);
			}

		} else {
			setState(getResources().getString(R.string.training));
			mFaceName.setText("");
			mInputDescription.setVisibility(View.INVISIBLE);

			mSearch.setVisibility(View.VISIBLE);

			mFaceName.setText("");

			mRecord.setVisibility(View.INVISIBLE);
			mInputDescription.setVisibility(View.INVISIBLE);

			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.training),
					Toast.LENGTH_LONG).show();
			mPersonRecognizer.train();
			setState("");

		}

	}

	private void showSwitchCamera() {
		if (mChooseCamera == mFrontCamera) {
			mChooseCamera = mBackCamera;
			mOpenCvCameraView.setCamBack();
		} else {
			mChooseCamera = mFrontCamera;
			mOpenCvCameraView.setCamFront();

		}

	}

	private void showSearch() {
		if (mSearch.isChecked()) {
			if (!mPersonRecognizer.canPredict()) {
				mSearch.setChecked(false);
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.cannot_predict),
						Toast.LENGTH_LONG).show();
				return;
			}

			setState(getResources().getString(R.string.searching));

			mRecord.setVisibility(View.INVISIBLE);
			mTrain.setVisibility(View.INVISIBLE);
			mInputDescription.setVisibility(View.INVISIBLE);
			mFaceState = SEARCHING;
			mFaceName.setVisibility(View.VISIBLE);
		} else {
			mFaceState = IDLE;
			setState("");
			mRecord.setVisibility(View.INVISIBLE);
			mTrain.setVisibility(View.VISIBLE);
			mInputDescription.setVisibility(View.INVISIBLE);
			mFaceName.setVisibility(View.INVISIBLE);

		}
	}

}
