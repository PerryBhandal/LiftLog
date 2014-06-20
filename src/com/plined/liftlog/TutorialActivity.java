package com.plined.liftlog;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class TutorialActivity extends Activity {
	
	private int mCurImage;
	private int mMaxImage = 12;
	private ImageView mImageView;
	private TextView mNumberTextView;
	private static String TAG = "TutorialActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_tutorial);
		mImageView = (ImageView) findViewById(R.id.a_tutorial_image);
		mNumberTextView = (TextView) findViewById(R.id.a_tutorial_imgnum);
		mCurImage = 1;
		setImage(0);
		wirePictureListener();
	}
	
	private void wirePictureListener() {
		View prevButton = findViewById(R.id.a_tutorial_prevbutton);
		prevButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setImage(-1);
			}
		});
		
		View nextButton = findViewById(R.id.a_tutorial_nextbutton);
		nextButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setImage(+1);
			}
		});
	}
	
	private void setImage(int offset) {
		int tempNum = mCurImage + offset;
		
		if (tempNum <= 0) {
			mCurImage = 1;
		} else if (tempNum >= mMaxImage) {
			mCurImage = mMaxImage;
		} else {
			mCurImage = mCurImage + offset;
		}
		
		Log.i(TAG, "Cur image is " + mCurImage);
		int imgId = R.drawable.one;
		
		switch (mCurImage) {
		case 1:
			imgId = R.drawable.one;
			break;
		case 2:
			imgId = R.drawable.two;
			break;
		case 3:
			imgId = R.drawable.three;
			break;
		case 4:
			imgId = R.drawable.four;
			break;
		case 5:
			imgId = R.drawable.five;
			break;
		case 6:
			imgId = R.drawable.six;
			break;
		case 7:
			imgId = R.drawable.seven;
			break;
		case 8:
			imgId = R.drawable.eight;
			break;
		case 9:
			imgId = R.drawable.nine;
			break;			
		case 10:
			imgId = R.drawable.ten;
			break;
		case 11:
			imgId = R.drawable.eleven;
			break;
		case 12:
			imgId = R.drawable.twelve;
			break;
		}
		
		
		mNumberTextView.setText(String.format("%d/%d",mCurImage, mMaxImage));
		mImageView.setImageResource(imgId);
		
	}
	
}
