package com.mumusha.findmyparking;

import java.util.ArrayList;
import com.mumusha.findmyparking.ShakeDetector.OnShakeListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

public class ArchiveActivity extends Activity {

	ArchiveAdapter arrayAdapter;
	ArrayList<archive> alrts = null;
	ArrayList<archive> arc = null;
	ListView lstTest;

	// for shake detection
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private ShakeDetector mShakeDetector;

	private ProgressDialog pd;
	private Context context;
	double la = 0.0;
	double lo = 0.0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_archive);
		lstTest = (ListView) findViewById(R.id.list);
		context = this;
		alrts = new ArrayList<archive>();
		arc = new ArrayList<archive>();
		final DatabaseHandler db = new DatabaseHandler(this);
		arrayAdapter = new ArchiveAdapter(ArchiveActivity.this,
				R.layout.archive_item, arc);
		lstTest.setAdapter(arrayAdapter);

		// ShakeDetector initialization
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mShakeDetector = new ShakeDetector();
		mShakeDetector.setOnShakeListener(new OnShakeListener() {

			@Override
			public void onShake(int count) {
				handleShakeEvent(count);

			}
		});

		try {

			alrts = db.getAllArchive();
			arrayAdapter.notifyDataSetChanged();
			for (archive l : alrts) {
				arc.add(l);
			}

		}

		catch (Exception e) {
			Log.d("Error", e.getMessage());
		}

	}

	public void vibrate() {

		try {
			mShakeDetector.wait(2000);
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		Vibrator v = (Vibrator) this.context
				.getSystemService(Context.VIBRATOR_SERVICE);
		// Vibrate for 500 milliseconds
		v.vibrate(500);

	}

	public void handleShakeEvent(int count) {

		GPSTracker tracker = GPSTracker.getInstance();
		tracker.getLocation(this);
		// vibrate();

		if (arc.size() == 0) {
			Toast.makeText(this, "Don't have records", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		AsyncTask<ArchiveActivity, Void, Void> task = new AsyncTask<ArchiveActivity, Void, Void>() {
			private volatile boolean running = true;
			private ArchiveActivity arcActivity;

			double la = 0.0;
			double lo = 0.0;

			@Override
			protected void onPreExecute() {
				pd = new ProgressDialog(context);
				pd.setTitle("Navigate to nearest parking location...");
				pd.setMessage("Looking for your location.");
				pd.setCancelable(true);
				pd.setIndeterminate(true);
				pd.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						cancel(true);
					}
				});
				pd.show();
			}

			@Override
			protected void onCancelled() {
				running = false;
			}

			@Override
			protected Void doInBackground(ArchiveActivity... x) {
				arcActivity = x[0];
				la = 0.0;
				lo = 0.0;
				double ta = 0.0;
				double to = 0.0;
				int accuracy = 0;
				double abs = 0.0;
				GPSTracker tracker = GPSTracker.getInstance();

				do {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					tracker.getLocation(context);
					Location location = tracker._location;
					ta = la;
					to = lo;
					la = location.getLatitude();
					lo = location.getLongitude();
					accuracy = (int) location.getAccuracy();
					Log.i("!!!Shake-GPSLA", Double.toString(la));
					Log.i("!!!Shake-GPSLO", Double.toString(lo));
					abs = Math.abs((Math.abs(la) + Math.abs(lo))
							- (Math.abs(ta) + Math.abs(to)));
				} while ((abs >= 0.00005 || accuracy > 30) && running);

				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				if (pd != null) {
					pd.dismiss();
				}
				arcActivity.update(la, lo);
				arcActivity.compareDis();
			}

		};
		task.execute(this);

	}

	@Override
	protected void onDestroy() {
		if (pd != null) {
			pd.dismiss();

		}
		super.onDestroy();
	}

	public void compareDis() {

		double x, y;
		double fx=0.0,fy=0.0;
		double distance = 1000000.0;
		double temp = 0.0;
		if (arc != null) {

			for (archive l : arc) {
				x = Double.valueOf(l.getLa());
				y = Double.valueOf(l.getLo());
				temp = (la - x) * (la - x) + (lo - y) * (lo - y);
				if (temp < distance){
					distance = temp;
					fx=x;
					fy=y;
				}
			}

			String arg = "http://maps.google.com/maps?daddr=%3,%4&dirflg=d";

			arg = arg.replaceAll("%3", Double.toString(fx));
			arg = arg.replaceAll("%4", Double.toString(fy));
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
					Uri.parse(arg));
			context.startActivity(intent);

		} else {
			Toast.makeText(this, "Don't have records", Toast.LENGTH_SHORT)
					.show();

		}

	}

	protected void update(double la, double lo) {

		this.la = la;
		this.lo = lo;
	}

	@Override
	public void onResume() {
		super.onResume();
		mSensorManager.registerListener(mShakeDetector, mAccelerometer,
				SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	public void onPause() {
		mSensorManager.unregisterListener(mShakeDetector);
		super.onPause();
	}

}
