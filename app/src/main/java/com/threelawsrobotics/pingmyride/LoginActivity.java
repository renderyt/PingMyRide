package com.threelawsrobotics.pingmyride;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

	TextView remindLater;
	TextView appName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// get the views
		remindLater = (TextView) findViewById(R.id.remind_later);
		appName = (TextView) findViewById(R.id.app_name);
		remindLater.setOnClickListener(this);
		appName.setOnClickListener(this);

		Typeface face = Typeface.createFromAsset(getAssets(), "fonts/Overlock-Bold.ttf");
		appName.setTypeface(face);
	}

	/**
	 * Handle all click events in this activity
	 *
	 * @param v
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.remind_later: {
				SavedSharedPreferences.setUser(getApplicationContext(), "user", "", "");
				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				finish();
				break;
			}
		}
	}
}
