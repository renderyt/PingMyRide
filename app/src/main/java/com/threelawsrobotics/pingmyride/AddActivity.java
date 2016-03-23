package com.threelawsrobotics.pingmyride;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class AddActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {
	WifiManager mWifiManager;
	TextView instructionOne;
	TextView instructionTwo;
	Button continueButton;
	TextView skipButton;
	TextView nearestTracker;
	LinearLayout edit_layout;
	EditText carName;
	View border;
	//	Tracker tracker;
	private String trackerSSID;
	private String TRACKER_TRADEMARK = "PMR Tracker";
	private int MINIMUM_DISTANCE = 4;
	TextView searchStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// initialize data
		trackerSSID = null;

		// views
		nearestTracker = (TextView) findViewById(R.id.nearest_tracker);
		instructionOne = (TextView) findViewById(R.id.instruction_one);
		instructionTwo = (TextView) findViewById(R.id.instruction_two);
		searchStatus = (TextView) findViewById(R.id.search_status);
		edit_layout = (LinearLayout) findViewById(R.id.edit_layout);
		border = findViewById(R.id.border);

		carName = (EditText) findViewById(R.id.car_name);
		continueButton = (Button) findViewById(R.id.continue_button);
		skipButton = (TextView) findViewById(R.id.skip_button);
		continueButton.setOnClickListener(this);
		continueButton.setEnabled(false);
		skipButton.setOnClickListener(this);
		carName.setOnKeyListener(this);
		carName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
					continueButton.performClick();
				}
				return false;
			}
		});

		// enable wifi (is this necessary?)
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mWifiManager.setWifiEnabled(true);

		// register broadcast receiver
		registerReceiver(mWifiScanReceiver,
				new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		mWifiManager.startScan();
	}

	/**
	 * Receiver to scan nearby wifi networks and find a PMR car locator
	 */
	private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent intent) {
			if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				List<ScanResult> mScanResults = mWifiManager.getScanResults();
				// iterate through all networks found
				for (ScanResult network : mScanResults) {
					// find PMR trackers
					if (network.SSID.contains(TRACKER_TRADEMARK)) {
						// check which is closest
						if (mWifiManager.calculateSignalLevel(network.level, 9) >= MINIMUM_DISTANCE) {
							// check tracker has not been registered on this device yet
							trackerSSID = network.SSID;
							// change instructions
							searchStatus.setText("Found Tracker");
							nearestTracker.setText(trackerSSID);
							continueButton.setEnabled(true);
							continueButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
							// android sometimes unregisters receiver again
							try {
								unregisterReceiver(mWifiScanReceiver);
							} catch (Exception e) {
							}
						}
					}
				}
			}
		}
	};

	/**
	 * Handle click events
	 *
	 * @param v
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.continue_button: {
				// check carName is not empty
				if (searchStatus.getVisibility() == View.GONE) {
					if (carName.getText() != null) {
						// add car to internal storage & database

						// go back to main activity
						Intent intent = this.getIntent();
						intent.putExtra("carName", carName.getText().toString());
						if (trackerSSID != null) {
							intent.putExtra("trackerSSID", trackerSSID);
						}
						this.setResult(RESULT_OK, intent);
						finish();
						overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
					}
				} else {
					edit_layout.setVisibility(View.VISIBLE);
					carName.requestFocus();
					skipButton.setVisibility(View.GONE);
					nearestTracker.setVisibility(View.GONE);
					instructionOne.setVisibility(View.GONE);
					instructionTwo.setVisibility(View.GONE);
					border.setVisibility(View.GONE);
					searchStatus.setVisibility(View.GONE);
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(carName, InputMethodManager.SHOW_IMPLICIT);
				}
				break;
			}
			case R.id.skip_button: {
				try {
					unregisterReceiver(mWifiScanReceiver);
					edit_layout.setVisibility(View.VISIBLE);
					carName.requestFocus();
					skipButton.setVisibility(View.GONE);
					nearestTracker.setVisibility(View.GONE);
					instructionOne.setVisibility(View.GONE);
					instructionTwo.setVisibility(View.GONE);
					border.setVisibility(View.GONE);
					searchStatus.setVisibility(View.GONE);
					continueButton.setEnabled(true);
					continueButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
				} catch (Exception e) {
				}
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(carName, InputMethodManager.SHOW_IMPLICIT);
				break;
			}
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
			continueButton.callOnClick();
			return true;
		}
		return false;
	}

	@Override
	public void onPause() {
		super.onPause();
		try {
			unregisterReceiver(mWifiScanReceiver);
		} catch (Exception e) {
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		try {
			unregisterReceiver(mWifiScanReceiver);
		} catch (Exception e) {
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Respond to the action bar's Up/Home button
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
