package com.wormz.penumbraface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
//import android.provider.MediaStore.Files;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class ImageGallery extends Activity implements
		AdapterView.OnItemSelectedListener, ViewSwitcher.ViewFactory,
		OnClickListener {

	private Labels mLabels;
	private int count = 0;
	private Bitmap mBitmapList[];
	private String mNameList[];
	private String mPath = "";
	private TextView mDescription;
	private Button mDelete;
	private ImageButton mBack;
	private Gallery g;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.catalog_view);
		mDescription = (TextView) findViewById(R.id.textview_description);
		mDelete = (Button) findViewById(R.id.button_delete);
		mDelete.setOnClickListener(this);

		mBack = (ImageButton) findViewById(R.id.imagebutton_back);
		mBack.setOnClickListener(this);

		mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
		mSwitcher.setFactory(this);
		mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_in));
		mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_out));

		Bundle bundle = getIntent().getExtras();
		mPath = bundle.getString("path");

		mLabels = new Labels(mPath);
		mLabels.Read();

		processImages();

		g = (Gallery) findViewById(R.id.gallery1);
		g.setAdapter(new ImageAdapter(this));
		g.setOnItemSelectedListener(this);

	}

	private void processImages() {
		count = 0;
		int max = mLabels.max();

		for (int i = 0; i <= max; i++) {
			if (mLabels.get(i) != "") {
				count++;
			}
		}

		mBitmapList = new Bitmap[count];
		mNameList = new String[count];
		count = 0;
		for (int i = 0; i <= max; i++) {
			if (mLabels.get(i) != "") {
				File root = new File(mPath);
				final String fname = mLabels.get(i);
				FilenameFilter pngFilter = new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.toLowerCase().startsWith(
								fname.toLowerCase() + "-");

					};
				};
				File[] imageFiles = root.listFiles(pngFilter);
				if (imageFiles.length > 0) {
					InputStream is;
					try {
						is = new FileInputStream(imageFiles[0]);

						mBitmapList[count] = BitmapFactory.decodeStream(is);
						mNameList[count] = mLabels.get(i);

					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						Log.e("File erro", e.getMessage() + " " + e.getCause());
						e.printStackTrace();
					}

				}
				count++;
			}
		}
	}

	public void refresh() {
		g.setAdapter(new ImageAdapter(this));
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int position,
			long id) {
		mSwitcher.setImageDrawable(new BitmapDrawable(getResources(),
				mBitmapList[position]));
		mDescription.setText(mNameList[position]);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public View makeView() {
		ImageView i = new ImageView(this);
		i.setBackgroundColor(0xFF000000);
		i.setScaleType(ImageView.ScaleType.FIT_CENTER);
		i.setLayoutParams(new ImageSwitcher.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		return i;
	}

	private ImageSwitcher mSwitcher;

	public class ImageAdapter extends BaseAdapter {
		public ImageAdapter(Context c) {
			mContext = c;
		}

		@Override
		public int getCount() {

			return count;
		}

		@Override
		public Object getItem(int position) {
			return mBitmapList[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = new ImageView(mContext);
			i.setImageBitmap(mBitmapList[position]);

			i.setAdjustViewBounds(true);
			i.setLayoutParams(new Gallery.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

			return i;
		}

		private Context mContext;

	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		switch (id) {
		case R.id.imagebutton_back:
			finish();
			break;

		case R.id.button_delete:
			deleteImages();
			break;
		}

	}

	private void deleteImages() {
		File root = new File(mPath);

		FilenameFilter pngFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String n) {
				String s = mDescription.getText().toString();
				return n.toLowerCase().startsWith(s.toLowerCase() + "-");

			};
		};
		File[] imageFiles = root.listFiles(pngFilter);
		for (File image : imageFiles) {
			image.delete();
			int i;
			for (i = 0; i < count; i++) {
				if (mNameList[i].equalsIgnoreCase(mDescription.getText()
						.toString())) {
					int j;
					for (j = i; j < count - 1; j++) {
						mNameList[j] = mNameList[j + 1];
						mBitmapList[j] = mBitmapList[j + 1];
					}
					count--;
					refresh();
					break;
				}
			}
		}
	}

}