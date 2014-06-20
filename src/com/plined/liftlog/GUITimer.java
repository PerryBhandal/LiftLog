package com.plined.liftlog;

import junit.framework.Test;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GUITimer {

	// The entire layout containing our timer, both the timer and the time
	// selector.
	private View mRootLayout;
	private View mTimerLayout;
	private View mTimeSelectorLayout;
	private ProgressBar mProgressBar;
	private TextView mTimerText;
	private Vibrator mVibrator;

	private TimingInstance mTimingInstance;

	// The time between counter ticks.
	private static final int COUNTDOWN_INTERVAL = 1000;

	// Amount of time to vibrate for when the timer expires.
	private static final int VIBRATE_TIME = 1500;

	private static String TAG = "GUITimer";

	protected Context mAppContext;

	/*
	 * Identifies which layout we're referring to.
	 */
	private static final int LAYOUTIDENT_TIMER = 1;
	private static final int LAYOUTIDENT_TIMESELECTOR = 2;

	public GUITimer(View timerRootLayout, Vibrator vibrator, Context appContext) {
		mRootLayout = timerRootLayout;
		mVibrator = vibrator;
		mAppContext = appContext;
		getAttributes();
		wireButtons();
		setVisible(LAYOUTIDENT_TIMESELECTOR);
	}

	/*
	 * Disposes of all active timers.
	 */
	public void dispose() {
		Log.v(TAG, "Disposing of our active timer if it exists.");
		if (mTimingInstance != null) {
			mTimingInstance.dispose();
		}
	}

	/*
	 * Sets the onclick listeners for all of our buttons.
	 */
	private void wireButtons() {
		// Wire stop
		View stopButton = mRootLayout.findViewById(R.id.t_timer_bar_stop);
		stopButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				stopTimer();
			}
		});

		// Wire numbers
		wireNumber(R.id.t_timer_bar_thirty, 30);
		wireNumber(R.id.t_timer_bar_fourtyfive, 45);
		wireNumber(R.id.t_timer_bar_sixty, 60);
		wireNumber(R.id.t_timer_bar_ninety, 90);
		wireNumber(R.id.t_timer_bar_onetwenty, 120);
	}

	/*
	 * Wires timer button to create a timer for the duration timeValue when hit.
	 */
	private void wireNumber(int timerId, int timeValue) {
		mRootLayout.findViewById(timerId).setOnClickListener(
				new TimerClickListener(timeValue));
	}

	/*
	 * Sets our visible layout, sets all others to gone.
	 */
	private void setVisible(int selectorId) {
		// Set our progress bar to be at 100% to start so there's no jarring
		// when it starts.
		mProgressBar.setProgress(100);

		if (selectorId == LAYOUTIDENT_TIMER) {
			// Set the timer as visible, and the time selector as gone.
			mTimeSelectorLayout.setVisibility(View.GONE);
			mTimerLayout.setVisibility(View.VISIBLE);
		} else {
			mTimerLayout.setVisibility(View.GONE);
			mTimeSelectorLayout.setVisibility(View.VISIBLE);
		}
	}

	/*
	 * Populates our attributes from the view.
	 */
	private void getAttributes() {
		mTimeSelectorLayout = mRootLayout
				.findViewById(R.id.t_timer_bar_lay_timeSelector);
		mTimerLayout = mRootLayout.findViewById(R.id.t_timer_bar_lay_timer);
		mProgressBar = (ProgressBar) mRootLayout
				.findViewById(R.id.t_timer_bar_progressBar);
		mTimerText = (TextView) mRootLayout
				.findViewById(R.id.t_timer_bar_progressText);
	}

	/*
	 * Begins our timer for the provided amount of time.
	 */
	private void beginTimer(int time) {
		// Dispose of our existing timer
		dispose();

		// Create and satrt our new timing instance.
		mTimingInstance = new TimingInstance(time);

		// Set the progress bar to be visible
		setVisible(LAYOUTIDENT_TIMER);
	}

	/*
	 * Stops our current timer and returns to the timer selector.
	 */
	private void stopTimer() {
		// Dispose of our timer
		mTimingInstance.dispose();

		// Set our time selector
		setVisible(LAYOUTIDENT_TIMESELECTOR);
	}

	private class TimerClickListener implements View.OnClickListener {

		private int mTime;

		public TimerClickListener(int time) {
			mTime = time;
		}

		@Override
		public void onClick(View v) {
			beginTimer(mTime);
		}

	}

	private class TimingInstance {

		private long mTotalTime;
		private CountDownTimer mCounter;

		public TimingInstance(int time) {
			mTotalTime = time * 1000;
			startTimer();
		}

		/*
		 * Creates our timer and starts it.
		 */
		private void startTimer() {
			// Create our countdown timer
			mCounter = new CountDownTimer(mTotalTime, COUNTDOWN_INTERVAL) {
				public void onTick(long millisUntilFinished) {
					mProgressBar.setProgress(getCompletionPct(
							millisUntilFinished, mTotalTime));
					mTimerText.setText((millisUntilFinished / 1000) + "");
				}

				public void onFinish() {

					mVibrator.vibrate(VIBRATE_TIME);
					
					
					// TODO: Make this conditional
					if (isRestSoundEnabled()) {
						playSound();
					}
					
					setVisible(LAYOUTIDENT_TIMESELECTOR);
				}
			};
			mCounter.start();
		}

		private void playSound() {
			MediaPlayer mp = MediaPlayer.create(mAppContext, R.raw.timer_finished);
			
			mp.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
					mp.release();
				}

			});
			mp.start();
		}

		/*
		 * Cancels and dispoes of our timer instance.
		 */
		public void dispose() {
			if (mCounter != null) {
				mCounter.cancel();
				mCounter = null;
			}
		}

		/*
		 * Gets our preference for whether we play a timer on rest expiration.
		 */
		public boolean isRestSoundEnabled() {		
			return mAppContext.getSharedPreferences(SettingsFragment.SETTINGS_PREF, Context.MODE_PRIVATE).getBoolean(SettingsFragment.SPREF_EXPIRE_SOUND, false);
		}
		
		private int getCompletionPct(long timeRemaining, double totalTime) {
			return (int) Math.round((timeRemaining / totalTime) * 100);
		}

	}

}
