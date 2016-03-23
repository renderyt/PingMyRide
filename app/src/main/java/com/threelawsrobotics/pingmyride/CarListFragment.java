package com.threelawsrobotics.pingmyride;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class CarListFragment extends Fragment implements View.OnClickListener {

	private List<Car> cars;
	private static final int ADD_CAR_REQUEST = 1;  // The request code
	private RVAdapter adapter;
	private LinearLayout helpText;

	public CarListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_car_list, container, false);

// initialize the starting state
		initializeData();

		// recycler view
		RecyclerView rv = (RecyclerView) view.findViewById(R.id.rv);
		LinearLayoutManager llm = new LinearLayoutManager(getContext());
		rv.setLayoutManager(llm);
		adapter = new RVAdapter(cars);
		rv.setAdapter(adapter);

		// add car floating action button
		FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
		fab.setOnClickListener(this);

//		 finally check if cars is empty
		if (cars.isEmpty()) {
			// point user to add button
		} else {
			helpText = (LinearLayout) view.findViewById(R.id.help_text);
			helpText.setVisibility(View.GONE);
		}
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.fab: {
				Intent intent = new Intent(getContext(), AddActivity.class);
				startActivityForResult(intent, ADD_CAR_REQUEST);
				getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
				break;
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == ADD_CAR_REQUEST) {
			// Make sure the request was successful
			if (resultCode == Activity.RESULT_OK) {
				// a new car was successfully added
				String carName = data.getStringExtra("carName");
				String trackerSSID = data.getStringExtra("trackerSSID");
				// update the recycler view
				if (trackerSSID == null) {
					// no tracker registered
					System.out.println("new car with no tracker");
					cars.add(new Car(carName));
					adapter.notifyDataSetChanged();
				} else {
					// tracker bought
					System.out.println("new car with ssid: " + trackerSSID);
					cars.add(new Car(carName, trackerSSID));
					adapter.notifyDataSetChanged();
				}
			}
		}

		if (cars.isEmpty()) {
			// point user to add button
		} else {
			helpText = (LinearLayout) getActivity().findViewById(R.id.help_text);
			helpText.setVisibility(View.GONE);
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

