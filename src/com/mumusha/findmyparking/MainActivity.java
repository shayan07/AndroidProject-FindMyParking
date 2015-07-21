package com.mumusha.findmyparking;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static final String ACTION_NEW = "org.xmlvm.tutorial.intent.action.ACTION_NEW";
	private Context context;
	Button searchBtn = null;
	Intent locatorService = null;
	AlertDialog alertDialog = null;
	private Button b;
	private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		b = (Button) findViewById(R.id.fastgps);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.main, menu);
		// return true;
		MenuInflater mif = getMenuInflater();
		mif.inflate(R.menu.main_activity_action, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * This method will be invoked whenever the button is clicked in the
	 * BlueActivity. This will trigger the invocation of YellowActivity.
	 */
	public void onClickNew(View v) {
		// resultField.setText("");
		/**
		 * Construct an Intent to launch NewRecord. Note that NewRecord is
		 * referenced explicitly via its class object.
		 */
		Intent intent = new Intent(this, NewRecord.class);
		intent.setClass(MainActivity.this, NewRecord.class);
		startActivity(intent);

	}

	public void onClickFind(View v) {
		// resultField.setText("");
		/**
		 * Construct an Intent to launch NewRecord. Note that NewRecord is
		 * referenced explicitly via its class object.
		 */

		SharedPreferences sharedPref = getSharedPreferences("userRecord",
				MODE_PRIVATE);
		Boolean hasRecord = sharedPref.getBoolean("hasRecord", false);
		Boolean hasFastRecord = sharedPref.getBoolean("hasFastRecord", false);

		if (!hasRecord && !hasFastRecord) {
			Toast.makeText(this, "Don't have record", Toast.LENGTH_SHORT)
					.show();
			return;

		}

		Intent intent = new Intent(this, FindRecord.class);
		intent.setClass(MainActivity.this, FindRecord.class);
		startActivity(intent);

	}

	@Override
	protected void onDestroy() {
		if (pd != null) {
			pd.dismiss();
			b.setEnabled(true);
		}
		super.onDestroy();
	}

	public void onClickFast(View v) {

		GPSTracker tracker = GPSTracker.getInstance();
		tracker.getLocation(this);
		v.setEnabled(false);
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			private volatile boolean running = true;

			@Override
			protected void onPreExecute() {
				pd = new ProgressDialog(context);
				pd.setTitle("Processing...");
				pd.setMessage("Looking for your location.");
				pd.setCancelable(true);
				pd.setIndeterminate(true);
				pd.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						b.setEnabled(true);
						cancel(true);

					}
				});
				pd.show();
			}

			@Override
			protected void onCancelled() {
				b.setEnabled(true);
				running = false;
				SharedPreferences sharedPref = getSharedPreferences(
						"userRecord", MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putBoolean("hasFastRecord", false);
				editor.commit();
			}

			@Override
			protected Void doInBackground(Void... arg0) {

				double la = 0.0;
				double lo = 0.0;
				double ta = 0.0;
				double to = 0.0;
				int accuracy = 0;
				double abs = 0.0;
				GPSTracker tracker = GPSTracker.getInstance();
                 int loop =0;
				do {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					tracker.getLocation(MainActivity.this);
					Location location = tracker._location;
					ta = la;
					to = lo;
					la = location.getLatitude();
					lo = location.getLongitude();
					accuracy = (int) location.getAccuracy();
					abs = Math.abs((Math.abs(la) + Math.abs(lo))
							- (Math.abs(ta) + Math.abs(to)));
					loop++;
				} while ((abs >= 0.00005 || accuracy > 30) && running&&loop<8);
				// 0.0001
				SharedPreferences sharedPref = getSharedPreferences(
						"userRecord", MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putBoolean("hasFastRecord", true);
				editor.putString("latitude", Double.toString(la));
				editor.putString("longitude", Double.toString(lo));
				editor.commit();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (pd != null) {
					pd.dismiss();
					b.setEnabled(true);
				}

			}

		};
		task.execute((Void[]) null);

	}

	public void onClickArchive(View v) {

		Intent intent = new Intent(this, ArchiveActivity.class);
		intent.setClass(MainActivity.this, ArchiveActivity.class);
		startActivity(intent);

	}

}
