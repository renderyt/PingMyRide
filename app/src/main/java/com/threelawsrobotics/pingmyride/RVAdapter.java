package com.threelawsrobotics.pingmyride;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.CarViewHolder> {
	private List<Car> cars;
	private String trackerSSID;
	private Resources res;

	RVAdapter(List<Car> cars) {
		this.cars = cars;
	}

	@Override
	public CarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.car_card, parent, false);
		CarViewHolder pvh = new CarViewHolder(v);

		// initialize resources
		res = parent.getContext().getResources();

		return pvh;
	}

	@Override
	public void onBindViewHolder(CarViewHolder holder, int position) {
		// card object
		holder.currentItem = cars.get(position);
		// set the car name
		holder.carName.setText(cars.get(position).getName());

		if (cars.get(position).getTime() != null) {
			holder.carTime.setText(cars.get(position).getTime());
		} else {
		}
		if (cars.get(position).getLocation() != null) {
			holder.carLocation.setText(cars.get(position).getLocation());
		} else {
		}
		if (cars.get(position).getPhoto() != null) {
			holder.carPhoto.setImageDrawable(cars.get(position).getPhoto());
		} else {
			// add default photo
			Bitmap src = BitmapFactory.decodeResource(res, R.drawable.login_background);
			RoundedBitmapDrawable dr =
					RoundedBitmapDrawableFactory.create(res, src);
			dr.setCornerRadius(Math.min(dr.getMinimumWidth(), dr.getMinimumHeight()));
			holder.carPhoto.setImageDrawable(dr);
		}
	}

	public class CarViewHolder extends RecyclerView.ViewHolder {
		View view;
		Car currentItem;
		CardView cv;
		TextView carName;
		TextView carTime;
		TextView carLocation;
		ImageView carPhoto;

		CarViewHolder(View itemView) {
			super(itemView);
			view = itemView;
			cv = (CardView) itemView.findViewById(R.id.cv);
			carPhoto = (ImageView) itemView.findViewById(R.id.car_photo);
			carName = (TextView) itemView.findViewById(R.id.car_name);
			carTime = (TextView) itemView.findViewById(R.id.car_time);
			carLocation = (TextView) itemView.findViewById(R.id.car_location);

			// Set up click listeners
			cv.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(v.getContext(), MapsActivity.class);
					// Pass car information to track activity
					intent.putExtra("carName", carName.getText().toString());
					intent.putExtra("carLocation", carLocation.getText().toString());
					v.getContext().startActivity(intent);
					((Activity) v.getContext()).overridePendingTransition(
							R.anim.slide_right_in, R.anim.slide_left_out);
				}
			});
			cv.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
					final CharSequence[] selectList = {
							"Edit Car",
							"Delete Car",
					};
					builder.setItems(selectList, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogInterface, int item) {
							if (selectList[item] == "Delete Car") {
								// Confirm with alert dialog
								removeAt(getAdapterPosition());
							}
						}
					});
					builder.create().show();
					return true;
				}
			});
		}
	}

	@Override
	public void onAttachedToRecyclerView(RecyclerView recyclerView) {
		super.onAttachedToRecyclerView(recyclerView);
	}

	@Override
	public int getItemCount() {
		return cars.size();
	}

	private void removeAt(int position) {
		cars.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, cars.size());
	}
}