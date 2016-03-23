package com.threelawsrobotics.pingmyride;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import java.util.List;

public class MainActivity extends AppCompatActivity {

	private List<Car> cars;
	private static final int ADD_CAR_REQUEST = 1;  // The request code
	private RVAdapter adapter;
	private LinearLayout helpText;
	private TabLayout tabLayout;
	private ViewPager viewPager;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// initialize the starting state
//		initializeData();

		if (SavedSharedPreferences.getUsername(getApplicationContext()) == "") {
			Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(intent);
		}

		// tab layout
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		setupViewPager(viewPager);

		tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);
		tabLayout.getTabAt(0).setIcon(ContextCompat.getDrawable(getApplicationContext(),
				R.drawable.ic_format_list_bulleted_white_48dp));
		tabLayout.getTabAt(1).setIcon(ContextCompat.getDrawable(getApplicationContext(),
				R.drawable.ic_settings_white_48dp));
		tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(
				getApplicationContext(), R.color.white));
		tabLayout.setSelectedTabIndicatorHeight((int) (3 * getResources().getDisplayMetrics().density));
	}

	private void setupViewPager(ViewPager viewPager) {
		ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
		adapter.addFrag(new CarListFragment());
		adapter.addFrag(new SettingsFragment());
		viewPager.setAdapter(adapter);
	}

//	@Override
//	public void onResume() {
//		super.onResume();
//		if (cars.isEmpty()) {
//			// point user to add button
//		} else {
//			helpText = (LinearLayout) findViewById(R.id.help_text);
//			helpText.setVisibility(View.GONE);
//		}
//	}
}
