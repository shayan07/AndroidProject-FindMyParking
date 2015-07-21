package com.mumusha.findmyparking;

import java.util.List;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ArchiveAdapter extends ArrayAdapter<archive> {

	int resource;
	String response;
	Context context;
	private List<archive> list;

	public ArchiveAdapter(Context context, int resource, List<archive> items) {
		super(context, resource, items);
		this.resource = resource;
		this.list = items;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout alertView;
		final archive al = getItem(position);
		final int pos = position;
		final DatabaseHandler db = new DatabaseHandler(context);
		if (convertView == null) {
			alertView = new LinearLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater vi;
			vi = (LayoutInflater) getContext().getSystemService(inflater);
			vi.inflate(resource, alertView, true);
		} else {
			alertView = (LinearLayout) convertView;
		}

		TextView alertText = (TextView) alertView
				.findViewById(R.id.txtAlertText);
		TextView alertDate = (TextView) alertView
				.findViewById(R.id.txtAlertDate);
		Button delButton = (Button) alertView.findViewById(R.id.delButton);
		Button dirButton = (Button) alertView.findViewById(R.id.dirButton);
		Button shareButton = (Button) alertView.findViewById(R.id.share_button);
		alertText.setText(al.getName());
		alertDate.setText(al.getAddr());
		delButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {

				AlertDialog.Builder adb = new AlertDialog.Builder(context);
				adb.setTitle("Delete?");
				adb.setMessage("Are you sure you want to delete this record?");
				adb.setNegativeButton("Cancel", null);
				adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						db.deleteArchive(al);
						list.remove(pos);

						notifyDataSetChanged();
					}
				});
				adb.show();
			}
		});

		shareButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View view) {

				SharedPreferences sharedPref = context.getSharedPreferences(
						"userRecord", context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString("gpStreetName", al.getAddr());
				editor.commit();
				Intent intent = new Intent(context, shareActivity.class);
				intent.setClass(context, shareActivity.class);
				context.startActivity(intent);
			}
		});

		dirButton.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				AlertDialog.Builder adb = new AlertDialog.Builder(context);
				adb.setTitle("Navigation?");
				adb.setMessage("Are you sure you want to navigate to this location?");
				adb.setNegativeButton("Cancel", null);
				adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String arg = "http://maps.google.com/maps?daddr=%3,%4&dirflg=d";
						arg = arg.replaceAll("%3", list.get(pos).getLa());
						arg = arg.replaceAll("%4", list.get(pos).getLo());
						Intent intent = new Intent(
								android.content.Intent.ACTION_VIEW, Uri
										.parse(arg));
						context.startActivity(intent);

						notifyDataSetChanged();
					}
				});
				adb.show();
			}
		});
		return alertView;
	}

}