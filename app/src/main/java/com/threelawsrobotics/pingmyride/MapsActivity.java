package com.threelawsrobotics.pingmyride;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	private GoogleMap mMap;
	private Button actionButton;
	private LatLng myLatLng;
	private LatLng carLatLng;
	private MarkerOptions carMarker;
	private String carName;
	private String carLocation;
	private RelativeLayout actionTab;
	private GoogleApiClient mGoogleApiClient;
	private Location mLastLocation;

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

		// Change activity name
		getSupportActionBar().setTitle(carName);

		// Initializing views
		actionButton = (Button) findViewById(R.id.action_button);
		actionButton.setOnClickListener(this);
		actionTab = (RelativeLayout) findViewById(R.id.action_tab);
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
			// Get LocationManager object from System Service LOCATION_SERVICE
			LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
			// Create a criteria object to retrieve provider
			Criteria criteria = new Criteria();
			// Get the name of the best provider
			String provider = locationManager.getBestProvider(criteria, true);
			// Get Current Location
			Location myLocation = locationManager.getLastKnownLocation(provider);
			// Get latitude of the current location
			double latitude = myLocation.getLatitude();
			// Get longitude of the current location
			double longitude = myLocation.getLongitude();
			// Create a LatLng object for the current location
			myLatLng = new LatLng(latitude, longitude);
			// Show the current location in Google Map
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 16f));
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
				if (actionButton.getText().equals("Park Here") && carMarker == null) {
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
						carMarker = new MarkerOptions().position(carLatLng).title("carName").
								icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
						mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(carLatLng, 16f));
						mMap.addMarker(carMarker);


						// remove park button
						final Animation in = new AlphaAnimation(0.0f, 1.0f);
						in.setDuration(150);
						final Animation out = new AlphaAnimation(1.0f, 0.0f);
						out.setDuration(150);
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
					collapse(actionTab, 300, 0);

					// move to car marker
					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(carLatLng, 16f));

					// begin wifi search
				}
			}
		}
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
}
