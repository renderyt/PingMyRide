package com.threelawsrobotics.pingmyride;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrackActivity extends FragmentActivity implements View.OnClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	private GoogleMap mMap;
	private GoogleApiClient mGoogleApiClient;
	private Location mLastLocation;
	private LatLng myLatLng;
	private FloatingActionButton gpsSearchButton;
	private FloatingActionButton fmSearchButton;
	private Date date;
	MarkerOptions myMarker;
	private TextView signalStrength;
	protected static final int REQUEST_CHECK_SETTINGS = 0x1;
	private String trackerSSID;

	// Saved Location & Time
	private TextView carLocation;
	private LatLng carLatLng;
	private Calendar timeParked;
	private TextView lastParked;
	MarkerOptions carMarker;

	private WifiManager wifi;
	private WifiInfo wifiInfo;
	private int prevNetwork;
	private int MAX__WIFI_SIGNAL = 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
		mapFragment.setMenuVisibility(true);

		// Google Api Client
		if (mGoogleApiClient == null) {
			mGoogleApiClient = new GoogleApiClient.Builder(this)
					.addConnectionCallbacks(this)
					.addOnConnectionFailedListener(this)
					.addApi(LocationServices.API)
					.build();
		}

		// Button
		gpsSearchButton = (FloatingActionButton) findViewById(R.id.gps_search_button);
		gpsSearchButton.setOnClickListener(this);

		fmSearchButton = (FloatingActionButton) findViewById(R.id.fm_search_button);
		fmSearchButton.setOnClickListener(this);

		signalStrength = (TextView) findViewById(R.id.signal_strength);

		// Hardcoded Stuff
		carLatLng = new LatLng(43.7732823, -79.5073874);
		carLocation = (TextView) findViewById(R.id.car_location);

		Calendar current = Calendar.getInstance();

//		timeParked = Calendar.getInstance();
//		timeParked.set(2016, 12, 3, 12, 0, 0);
//
		lastParked = (TextView) findViewById(R.id.last_parked);
//		long timeDifference = current.getTimeInMillis() - timeParked.getTimeInMillis();
		lastParked.setText("23 Minutes Ago");

		// Tracker stuff
		trackerSSID = "";
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.gps_search_button: {
//				if (mLastLocation != null) {
//					myLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
//					mMap.addMarker(myMarker);
//					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));
//				} else {
//					showSettingsAlert();
//				}
				if (carLatLng != null) {
					CameraUpdate cu = CameraUpdateFactory.newLatLng(carLatLng);
					mMap.animateCamera(cu);

					// ask if user would like to navigate with google maps
					showSettingsAlert();

					// send user to google maps
					Uri gmmIntentUri = Uri.parse("google.navigation:q=" +
							carLatLng.longitude + "," + carLatLng.latitude);
					Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
					mapIntent.setPackage("com.google.android.apps.maps");
					startActivity(mapIntent);
				}
				break;
			}
			case R.id.fm_search_button: {
				if (mLastLocation != null) {
					// Start looking for wifi connection
					wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
					registerReceiver(wifiScanReceiver,
							new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
					wifi.startScan();

					// alert user if car has been found


					// timeout after a few seconds

					// ask user to continue


//					registerReceiver(wifiScanReceiver,
//							new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
				} else {
					showSettingsAlert();
				}
				break;
			}
		}
	}

	private final BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context c, Intent intent) {
			if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				// when an access point scan has been completed
				List<ScanResult> scanResults = wifi.getScanResults();
				for (ScanResult result : scanResults) {
					if (result.SSID.equals(trackerSSID)) {
						unregisterReceiver(wifiScanReceiver);
						// connect to tracker wifi
						WifiConfiguration wifiConfig = new WifiConfiguration();
						wifiConfig.SSID = trackerSSID;
						wifiConfig.preSharedKey = "\"savetheworld\"";
						wifiConfig.status = WifiConfiguration.Status.ENABLED;
						wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
						wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
						wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
						wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
						wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
						wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

////						remember id
//						prevNetwork = wifi.getConnectionInfo().getNetworkId();
//						wifi.removeNetwork(prevNetwork);

						int netId = wifi.addNetwork(wifiConfig);
						wifi.disconnect();
						wifi.enableNetwork(netId, true);
						wifi.reconnect();

						// inform user that the tracker has been detected
						Toast.makeText(TrackActivity.this, "Wifi has been connected to car tracker",
								Toast.LENGTH_SHORT).show();
						registerReceiver(wifiScanReceiver,
								new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
						wifiInfo = wifi.getConnectionInfo();
						int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 10);
						signalStrength.setText(level + "");
					}
				}
			} else if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {
				// signal strength has changed
				wifiInfo = wifi.getConnectionInfo();
//				if (tracker.getSsid().equals(wifiInfo.getSSID())) {
				wifiInfo = wifi.getConnectionInfo();
				int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 10);
				signalStrength.setText(level + "");
				if (level == 9) {
					showCarAlert();
					unregisterReceiver(wifiScanReceiver);
				}
//				}
			}
		}
	};

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
		if (mLastLocation != null && mMap != null) {
			Log.i("Location", mLastLocation.getLatitude() + "    " + mLastLocation.getLongitude());
			System.out.println(mLastLocation.getLatitude() + "    " + mLastLocation.getLongitude());
			myLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

			myMarker = new MarkerOptions()
					.position(myLatLng);
			carMarker = new MarkerOptions()
					.position(new LatLng(carLatLng.latitude, carLatLng.longitude));

			mMap.addMarker(myMarker);
			mMap.addMarker(carMarker);
			try {

				Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
				// long and lat below
				List<Address> addresses = geo.getFromLocation(carLatLng.latitude, carLatLng.longitude, 1);
				if (addresses.isEmpty()) {
					carLocation.setText("Waiting for Location");
				} else {
					if (addresses.size() > 0) {
						carLocation.setText(addresses.get(0).getFeatureName() + ", " + addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15));
		}
	}

	@Override
	public void onConnectionSuspended(int i) {
// Display the connection status
		Toast.makeText(this, "Connection suspended. Please re-connect.",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
// Display the connection status
		Toast.makeText(this, "Connection failed. Please re-connect.",
				Toast.LENGTH_SHORT).show();
	}

	public boolean canGetLocation() {
		// Test network
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		if (!wifi.isWifiEnabled()) {
			Toast.makeText(getApplicationContext(), "WIFI has been enabled", Toast.LENGTH_LONG).show();
			wifi.setWifiEnabled(true);
		}
		// Test gps
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		return locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)
				|| locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);
	}

	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		// Setting Dialog Title
		alertDialog.setTitle("Location services not enabled");
		// Setting Dialog Message
		alertDialog.setMessage("Would you like to change your location settings?");
		// On pressing Settings button
		alertDialog.setPositiveButton(
				getResources().getString(R.string.button_ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivity(intent);
					}
				});
		alertDialog.setNegativeButton(
				getResources().getString(R.string.button_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		alertDialog.show();
	}

	public void showCarAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		// Setting Dialog Title
		alertDialog.setTitle("You have arrived at your car");
		// Setting Dialog Message
		alertDialog.setMessage("Would you like to unpark the car?");
		// On pressing Settings button
		alertDialog.setPositiveButton(
				getResources().getString(R.string.button_ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						carLocation.setText("Not parked");
						lastParked.setText("");
					}
				});
		alertDialog.setNegativeButton(
				getResources().getString(R.string.button_cancel),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		alertDialog.show();
	}

	@Override
	protected void onStart() {
		mGoogleApiClient.connect();
		super.onStart();
	}

	@Override
	protected void onStop() {
		mGoogleApiClient.disconnect();
//		unregisterReceiver(wifiScanReceiver);
		super.onStop();
	}

	@Override
	protected void onResume() {
		mGoogleApiClient.connect();
		super.onResume();
	}
}