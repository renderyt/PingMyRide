package com.threelawsrobotics.pingmyride;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class AddActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {
	WifiManager mWifiManager;
	TextView nearestTracker;
	TextView instructionOne;
	TextView instructionTwo;
	Button continueButton;
	Button skipButton;
	EditText carName;
	//	Tracker tracker;
	private String trackerSSID;
	private String TRACKER_TRADEMARK = "Rogers";
	private int MINIMUM_DISTANCE = 4;

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

		carName = (EditText) findViewById(R.id.car_name);
		continueButton = (Button) findViewById(R.id.continue_button);
		skipButton = (Button) findViewById(R.id.skip_button);
		continueButton.setOnClickListener(this);
		skipButton.setOnClickListener(this);
		carName.setOnKeyListener(this);

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
							instructionOne.setText("PMR Tracker detected \n" + trackerSSID);
							instructionTwo.setText("Give your car a name");
							skipButton.setVisibility(View.GONE);
							continueButton.setVisibility(View.VISIBLE);
							carName.setVisibility(View.VISIBLE);
							carName.requestFocus();
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.showSoftInput(carName, InputMethodManager.SHOW_IMPLICIT);
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
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.continue_button: {
				// check carName is not empty
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
				break;
			}
			case R.id.skip_button: {
				try {
					unregisterReceiver(mWifiScanReceiver);
				} catch (Exception e) {
				}
				instructionOne.setText("");
				instructionTwo.setText("Give your car a name");
				skipButton.setVisibility(View.GONE);
				continueButton.setVisibility(View.VISIBLE);
				carName.setVisibility(View.VISIBLE);
				carName.requestFocus();
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
