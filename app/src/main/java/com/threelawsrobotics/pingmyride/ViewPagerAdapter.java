package com.threelawsrobotics.pingmyride;

import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Renatius on 3/23/2016.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
	private final List<Fragment> mFragmentList = new ArrayList<>();
	private final List<Drawable> mFragmentTitleList = new ArrayList<>();

	public ViewPagerAdapter(FragmentManager manager) {
		super(manager);
	}

	@Override
	public Fragment getItem(int position) {
		return mFragmentList.get(position);
	}

	@Override
	public int getCount() {
		return mFragmentList.size();
	}

	public void addFrag(Fragment fragment) {
		mFragmentList.add(fragment);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return null;
	}
}