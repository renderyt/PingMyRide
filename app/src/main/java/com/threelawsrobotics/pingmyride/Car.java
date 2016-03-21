package com.threelawsrobotics.pingmyride;

import android.graphics.drawable.Drawable;

class Car {
	private String name;
	private String time;
	private String location;
	private String trackerSSID;
	private Drawable photo;

	Car(String name) {
		this.name = name;
		this.trackerSSID = "";
	}

	Car(String name, String trackerSSID) {
		this.name = name;
		this.trackerSSID = trackerSSID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getTrackerSSID() {
		return trackerSSID;
	}

	public void setTrackerSSID(String trackerSSID) {
		this.trackerSSID = trackerSSID;
	}

	public Drawable getPhoto() {
		return photo;
	}

	public void setPhoto(Drawable photo) {
		this.photo = photo;
	}
}
