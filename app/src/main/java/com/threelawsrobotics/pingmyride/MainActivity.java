package com.threelawsrobotics.pingmyride;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

	private List<Car> cars;
	private static final int ADD_CAR_REQUEST = 1;  // The request code
	private RVAdapter adapter;
	private LinearLayout helpText;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// initialize the starting state
		initializeData();

		// recycler view
		RecyclerView rv = (RecyclerView) findViewById(R.id.rv);
		LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
		rv.setLayoutManager(llm);
		adapter = new RVAdapter(cars);
		rv.setAdapter(adapter);

		// add car floating action button
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(this);

		// finally check if cars is empty
		if (cars.isEmpty()) {
			// point user to add button
		} else {
			helpText = (LinearLayout) findViewById(R.id.help_text);
			helpText.setVisibility(View.GONE);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (cars.isEmpty()) {
			// point user to add button
		} else {
			helpText = (LinearLayout) findViewById(R.id.help_text);
			helpText.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.fab: {
				Intent intent = new Intent(getApplicationContext(), AddActivity.class);
				startActivityForResult(intent, ADD_CAR_REQUEST);
				overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
				break;
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == ADD_CAR_REQUEST) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				// a new car was successfully added
				String carName = data.getStringExtra("carName");
				String trackerSSID = data.getStringExtra("trackerSSID");
				System.out.println(carName);
				System.out.println(trackerSSID);
				// update the recycler view
				if (trackerSSID == "") {
					// no tracker registered
					cars.add(new Car(carName));
					adapter.notifyDataSetChanged();
				} else {
					// tracker bought
					cars.add(new Car(carName, trackerSSID));
					adapter.notifyDataSetChanged();
				}
			}
		}
	}

	/**
	 * Method to initialize the recycler view
	 */
	private void initializeData() {
		cars = new ArrayList<>();
		// check if there are already registered cars
		if (true) {

		}
	}
}
