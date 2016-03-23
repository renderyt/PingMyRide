package com.threelawsrobotics.pingmyride;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
		View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	private GoogleMap mMap;
	private Button actionButton;
	private LatLng myLatLng;
	private LatLng carLatLng;
	private MarkerOptions carMarker;
	private String carName;
	private String carLocation;
	private RelativeLayout trackTab;
	private GoogleApiClient mGoogleApiClient;
	private Location mLastLocation;
	private String trackerSSID;
	private WifiManager wifi;
	private WifiInfo wifiInfo;
	private int MAX__WIFI_SIGNAL = 100;
	private TextView signalStrengthUpdate;
	private View progressBar;
	private TextView signalStrength;
	private static final int PROGRESS = 0x1;
	private int level;
	private View signalBar;
	final int INCREASE = 1;
	private View progressBarLine;
	final int DECREASE = 2;
	private int netId;
	private int oldNetId;

	final Animation in = new AlphaAnimation(0.0f, 1.0f);
	final Animation out = new AlphaAnimation(1.0f, 0.0f);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		// Google Api Client
		if (mGoogleApiClient == null) {
			// ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
			// See https://g.co/AppIndexing/AndroidStudio for more information.
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.addApi(AppIndex.API).build();
		}

		// Get data from intent
		Intent intent = getIntent();
		carName = intent.getStringExtra("carName");
		carLocation = intent.getStringExtra("carLocation");
		trackerSSID = intent.getStringExtra("trackerSSID");
		System.out.println(carName + "   " + carLocation + "   " + trackerSSID);

		// wifi setup
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiInfo = wifi.getConnectionInfo();

		// Change activity name
		getSupportActionBar().setTitle(carName);

		// Initializing views
		actionButton = (Button) findViewById(R.id.action_button);
		actionButton.setOnClickListener(this);
		trackTab = (RelativeLayout) findViewById(R.id.track_tab);
		signalStrength = (TextView) findViewById(R.id.signal_strength);
		signalStrengthUpdate = (TextView) findViewById(R.id.signal_strength_update);
		progressBarLine = findViewById(R.id.progress_bar_line);
		signalBar = findViewById(R.id.signal_bar);

		// initialize progress bar
		// mProgress = (ProgressBar) findViewById(R.id.progress_bar);
		level = 0;

		in.setDuration(150);
		out.setDuration(150);
	}

	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		// enable usual map functions and buttons
		mMap.setMyLocationEnabled(true);
		mMap.getUiSettings().setZoomControlsEnabled(true);
		mMap.getUiSettings().setMapToolbarEnabled(true);

		// Where should the map go?
		if (true) {
			// if the car is not parked, show current location
			mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
					mGoogleApiClient);
			if (mLastLocation != null) {
				myLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 16f));
			}
		} else {
			// if the car is parked, show the car
//			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(carLatLng, 16f));
		}
		// add a marker for the car using it's name and color


		// animate the camera to the location of the car
//		LatLng sydney = new LatLng(-34, 151);
//		mMap.addMarker(new MarkerOptions().position(sydney).title("carName").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//		mMap.animateCamera(CameraUpdateFactory.newLatLng(sydney));
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.action_button: {
				// Car has not been parked yet
				if (actionButton.getText().equals("Park Here")) {
					// add a marker for the car using it's name and color
					if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
						// TODO: Consider calling
						//    ActivityCompat#requestPermissions
						// here to request the missing permissions, and then overriding
						//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
						//                                          int[] grantResults)
						// to handle the case where the user grants the permission. See the documentation
						// for ActivityCompat#requestPermissions for more details.
						return;
					}
					mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
							mGoogleApiClient);
					if (mLastLocation != null) {
						// if we can get the location
						carLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
						carMarker = new MarkerOptions().position(carLatLng).title(carName).
								icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(carLatLng, 16f));
						mMap.addMarker(carMarker);

						// remove park button
						actionButton.startAnimation(out);
						out.setAnimationListener(new Animation.AnimationListener() {
							@Override
							public void onAnimationStart(Animation animation) {
							}

							@Override
							public void onAnimationEnd(Animation animation) {
								actionButton.setText("Find Car");
								actionButton.startAnimation(in);
							}

							@Override
							public void onAnimationRepeat(Animation animation) {

							}
						});
					} else {
						// show location dialog
					}
				} else if (actionButton.getText().equals("Find Car") && carMarker != null) {
					// collapse find car button
//					collapse(actionTab, 300, 0);
					expand(trackTab, 300, 192 * 3);
					signalStrengthUpdate.setText("Searching For Car");
					signalStrength.setVisibility(View.VISIBLE);
					signalStrengthUpdate.setVisibility(View.VISIBLE);
					progressBarLine.setVisibility(View.VISIBLE);
					actionButton.setVisibility(View.GONE);
					// move to car marker
					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(carLatLng, 16f));
					registerReceiver(wifiScanReceiver,
							new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
					wifi.startScan();
					// begin wifi search
//					WifiConfiguration wifiConfig = new WifiConfiguration();
//					wifiConfig.SSID = "\"" + trackerSSID + "\"";
//					String networkPass = "savetheworld";
//					wifiConfig.preSharedKey = "\"" + networkPass + "\"";
//					netId = wifi.addNetwork(wifiConfig);
////					wifi.disconnect();
//					wifi.enableNetwork(netId, true);
//					wifi.reconnect();
//					wifi.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, "WIFI_SCAN_MODE").acquire();
//					registerReceiver(wifiScanReceiver,
//							new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
//					wifi.startScan();
//					wifi.reconnect();
//					registerReceiver(wifiScanReceiver,
//							new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//					wifi.startScan();
				}
			}
		}
	}

	private final BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent intent) {
			if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				// when an access point scan has been completed
				System.out.println("Changing WiFi");
				oldNetId = wifi.getConnectionInfo().getNetworkId();
				wifi.disableNetwork(oldNetId);
				unregisterReceiver(wifiScanReceiver);
				// connect to tracker wifi
				WifiConfiguration wifiConfig = new WifiConfiguration();
				wifiConfig.SSID = "\"" + trackerSSID + "\"";
				String networkPass = "savetheworld";
				wifiConfig.preSharedKey = "\"" + networkPass + "\"";
				netId = wifi.addNetwork(wifiConfig);
				wifi.disconnect();
				wifi.enableNetwork(netId, true);
				wifi.reconnect();

//				 inform user that the tracker has been detected
				registerReceiver(wifiScanReceiver,
						new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
			} else if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {
				System.out.println("RSSI CHANGED ACTION");
				wifiInfo = wifi.getConnectionInfo();
				// signal strength has changed
				level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), MAX__WIFI_SIGNAL);

				new SignalTask().execute(level, Integer.valueOf(
						signalStrength.getText().toString().substring(0, signalStrength.getText().length() - 1)));

			}
		}
	};

	/**
	 * Update the signal strength bar
	 */
	private class SignalTask extends AsyncTask<Integer, Void, int[]> {
		@Override
		protected int[] doInBackground(Integer... data) {
			int response[] = {0, 0};
			// level bar height
			int barHeight = 192;

			try {
				int currentLevel = data[0];
				int previousLevel = data[1];

				// calculate level change
				int newBarLevel = (int) (currentLevel / 100.0 * barHeight);
				response[1] = newBarLevel;

				System.out.println(currentLevel +
						"- " + previousLevel + " = " + newBarLevel);

				if (currentLevel > previousLevel) {
					response[0] = INCREASE;
				} else if (currentLevel < previousLevel) {
					response[0] = DECREASE;
				}
			} catch (Exception e) {
				System.out.println(e);
			}
			return response;
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(int[] response) {
			if (response[0] == INCREASE) {
				System.out.println("Target height up to : " + response[1]);
				expand(signalBar, 300, response[1] * 3);
			} else if (response[0] == DECREASE) {
				System.out.println("Target height down to : " + response[1]);
				collapse(signalBar, 300, response[1] * 3);
			}
			if (response[1] >= 96 && response[1] < 170) {
				signalStrengthUpdate.setText("You're Almost There");
			} else if (response[1] >= 180) {
				signalStrengthUpdate.setText("You Have Arrived");
				// revert back to old network
				wifi.disableNetwork(wifi.getConnectionInfo().getNetworkId());
				unregisterReceiver(wifiScanReceiver);
				wifi.disconnect();
				wifi.enableNetwork(oldNetId, true);
				wifi.reconnect();

				AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
				alertDialog.setTitle("You Have Arrived");
				alertDialog.setMessage("Would you like to unpark your car?");
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								signalStrength.setVisibility(View.GONE);
								signalStrengthUpdate.setVisibility(View.GONE);
								progressBarLine.setVisibility(View.GONE);
								collapse(signalBar, 300, 0);
								signalBar.setVisibility(View.GONE);
								actionButton.setVisibility(View.VISIBLE);
								collapse(trackTab, 300, 48 * 3);
								signalStrength.setText("00%");
								actionButton.setText("Park Here");
								mMap.clear();

							}
						});
				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								signalStrength.setVisibility(View.GONE);
								signalStrengthUpdate.setVisibility(View.GONE);
								progressBarLine.setVisibility(View.GONE);
								signalStrength.setText("00%");
								collapse(signalBar, 300, 0);
								signalBar.setVisibility(View.GONE);
								actionButton.setVisibility(View.VISIBLE);
								collapse(trackTab, 300, 48 * 3);
							}
						});
				alertDialog.show();


//				timer = new Timer();
//				timer.schedule(new RemindTask(), seconds*1000);
//				signalStrength.setVisibility(View.GONE);
//				signalStrengthUpdate.setVisibility(View.GONE);
//				progressBarLine.setVisibility(View.GONE);
//				actionButton.setVisibility(View.VISIBLE);
//				collapse(trackTab, 300, 48 * 3);
			}
			if (level < 10) {
				signalStrength.setText(level + "0%");
			} else if (level > 95) {
				signalStrength.setText("100%");
			} else {
				signalStrength.setText(level + "%");
			}
		}

	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
				mGoogleApiClient);
		if (mLastLocation != null) {
			myLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 16f));
		}
	}

	@Override
	public void onConnectionSuspended(int i) {
	}

	protected void onStart() {
		mGoogleApiClient.connect();
		super.onStart();
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Maps Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app URL is correct.
				Uri.parse("android-app://com.threelawsrobotics.pingmyride/http/host/path")
		);
		AppIndex.AppIndexApi.start(mGoogleApiClient, viewAction);
	}

	protected void onStop() {
		mGoogleApiClient.disconnect();
		super.onStop();
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		Action viewAction = Action.newAction(
				Action.TYPE_VIEW, // TODO: choose an action type.
				"Maps Page", // TODO: Define a title for the content shown.
				// TODO: If you have web page content that matches this app activity's content,
				// make sure this auto-generated web page URL is correct.
				// Otherwise, set the URL to null.
				Uri.parse("http://host/path"),
				// TODO: Make sure this auto-generated app URL is correct.
				Uri.parse("android-app://com.threelawsrobotics.pingmyride/http/host/path")
		);
		AppIndex.AppIndexApi.end(mGoogleApiClient, viewAction);
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
	}

	public static void expand(final View v, int duration, int targetHeight) {

		int prevHeight = v.getHeight();

		v.setVisibility(View.VISIBLE);
		ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
		valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				v.getLayoutParams().height = (int) animation.getAnimatedValue();
				v.requestLayout();
			}
		});
		valueAnimator.setInterpolator(new DecelerateInterpolator());
		valueAnimator.setDuration(duration);
		valueAnimator.start();
	}

	public static void collapse(final View v, int duration, int targetHeight) {
		int prevHeight = v.getHeight();
		ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
		valueAnimator.setInterpolator(new DecelerateInterpolator());
		valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				v.getLayoutParams().height = (int) animation.getAnimatedValue();
				v.requestLayout();
			}
		});
		valueAnimator.setInterpolator(new DecelerateInterpolator());
		valueAnimator.setDuration(duration);
		valueAnimator.start();
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
