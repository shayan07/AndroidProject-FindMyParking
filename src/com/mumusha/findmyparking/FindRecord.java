package com.mumusha.findmyparking;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class FindRecord extends FragmentActivity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	private SupportMapFragment mapFragment;
	private GoogleMap googleMap;
	private ScrollView mainScrollView;
	private ImageView transparentImageView;
	private TextView userNote;
	private ImageView userImage;
	private LocationClient mLocationClient;
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private final long interval = 1 * 1000;
	private CountDownTimer countDownTimer;
	private TextView chron;
	Geocoder geocoder;
	private CheckBox cb;

	public static class ErrorDialogFragment extends DialogFragment {

		// Global field to contain the error dialog
		private Dialog mDialog;

		// Default constructor. Sets the dialog field to null
		public ErrorDialogFragment() {
			super();
			mDialog = null;
		}

		// Set the dialog to display
		public void setDialog(Dialog dialog) {
			mDialog = dialog;
		}

		// Return a Dialog to the DialogFragment.
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return mDialog;
		}
	}
	public String formatTime(long millis) {  
	    String output = "00:00";  
	    long seconds = millis / 1000;  
	    long minutes = seconds / 60;  

	    seconds = seconds % 60;  
	    minutes = minutes % 60;  

	    String sec = String.valueOf(seconds);  
	    String min = String.valueOf(minutes);  

	    if (seconds < 10)  
	        sec = "0" + seconds;  
	    if (minutes < 10)  
	        min= "0" + minutes;  

	    output = min + " : " + sec;  
	    return output;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_record);
		chron = (TextView) findViewById(R.id.chronometer1);
		userNote = (TextView) findViewById(R.id.textViewUserInput);
		userImage = (ImageView) findViewById(R.id.imageView1);
		cb = (CheckBox) findViewById(R.id.checkbox_save);
		SharedPreferences sharedPref = getSharedPreferences("userRecord",
				MODE_PRIVATE);

		geocoder = new Geocoder(this, Locale.getDefault());
		// start retrevie
		if (!sharedPref.getBoolean("hasFastRecord", false)) {
			String restoredNote = sharedPref.getString("note", "default");

			if (restoredNote != null) {
				userNote.setText(restoredNote);
				Log.i("notegetfromFind!", restoredNote);
			} else
				Log.i("note", "null");

			String picPath = sharedPref.getString("picPath", "default");
			if (picPath.equals("default")) {

				Log.i("userPic", "default");
			} else {

				Log.i("userPic", picPath);

				Uri fileUri = Uri.parse(picPath);

				Bitmap bitmap = null;

				try {

					GetImageThumbnail getImageThumbnail = new GetImageThumbnail();
					bitmap = getImageThumbnail.getThumbnail(fileUri, this);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				userImage.setImageBitmap(bitmap);

			}
			
			
			long targetTime = sharedPref.getLong("smstimestamp",(long) 0.0);
			long restSec=targetTime-System.currentTimeMillis();
            
			Log.i("2current Mills!!!!",Long.toString(System.currentTimeMillis()) );
			
			Log.i("GET Expire Mills!!!!",Long.toString(targetTime) );
			
			Log.i("rest Mills!!!!",Long.toString(restSec) );
			countDownTimer = new MyCountDownTimer(restSec, interval);

			//chron.setText(chron.getText() + String.valueOf(restSec / 1000));
			countDownTimer.start();
		}
		// end retreive

		// google map
		mLocationClient = new LocationClient(this, this, this);
		mapFragment = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map2));
		googleMap = mapFragment.getMap();
		googleMap.setMyLocationEnabled(true);
		googleMap.getUiSettings().setMyLocationButtonEnabled(true);
		googleMap.getUiSettings().setRotateGesturesEnabled(false);

		mainScrollView = (ScrollView) findViewById(R.id.scroll2);
		transparentImageView = (ImageView) findViewById(R.id.transparent_image2);
		transparentImageView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					// Disallow ScrollView to intercept touch events.
					mainScrollView.requestDisallowInterceptTouchEvent(true);
					// Disable touch on transparent view
					return false;

				case MotionEvent.ACTION_UP:
					// Allow ScrollView to intercept touch events.
					mainScrollView.requestDisallowInterceptTouchEvent(false);
					return true;

				case MotionEvent.ACTION_MOVE:
					mainScrollView.requestDisallowInterceptTouchEvent(true);
					return false;

				default:
					return true;
				}
			}
		});
		double la = 0.0;
		double lo = 0.0;
		la = Double.parseDouble(sharedPref.getString("latitude", "0.0"));
		lo = Double.parseDouble(sharedPref.getString("longitude", "0.0"));

		googleMap.addMarker(new MarkerOptions().position(new LatLng(la, lo))
				.title("Your Car"));

		Log.i("MarkerLa", Double.toString(la));
		Log.i("MarkerLo", Double.toString(lo));
		
          cb.setOnClickListener(new OnClickListener() {
			

			@Override
			public void onClick(View arg0) {
				if (cb.isChecked()) {

				
				// get prompts.xml view
				LayoutInflater li = LayoutInflater.from(FindRecord.this);
				View promptsView = li.inflate(R.layout.prompts, null);

				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						FindRecord.this);

				alertDialogBuilder.setView(promptsView);
				final EditText userInput = (EditText) promptsView
						.findViewById(R.id.editTextDialogUserInput);

				// set dialog message
				alertDialogBuilder
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// get user input and set it to result
										// edit text
										cb.setText(userInput.getText());
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();

			}
				
			}
		});


	}

	@Override
	protected void onStart() {
		super.onStart();
		// Connect the client.
		if (isGooglePlayServicesAvailable()) {
			mLocationClient.connect();
		}

	}

	@Override
	protected void onStop() {
		// Disconnecting the client invalidates it.
		mLocationClient.disconnect();
		super.onStop();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Decide what to do based on the original request code
		switch (requestCode) {

		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			/*
			 * If the result code is Activity.RESULT_OK, try to connect again
			 */
			switch (resultCode) {
			case Activity.RESULT_OK:
				mLocationClient.connect();
				break;
			}

		}
	}

	private boolean isGooglePlayServicesAvailable() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			Log.d("Location Updates", "Google Play services is available.");
			return true;
		} else {
			// Get the error dialog from Google Play services
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
					resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

			// If Google Play services can provide an error dialog
			if (errorDialog != null) {
				// Create a new DialogFragment for the error dialog
				ErrorDialogFragment errorFragment = new ErrorDialogFragment();
				errorFragment.setDialog(errorDialog);
				// errorFragment.show(getSupportFragmentManager(),"Location Updates");
			}

			return false;
		}
	}

	@Override
	public void onConnected(Bundle dataBundle) {
		// Display the connection status
		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		Location location = mLocationClient.getLastLocation();
		LatLng latLng = new LatLng(location.getLatitude(),
				location.getLongitude());
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,
				17);
		googleMap.animateCamera(cameraUpdate);
	}

	@Override
	public void onDisconnected() {
		// Display the connection status
		Toast.makeText(this, "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			Toast.makeText(getApplicationContext(),
					"Sorry. Location services not available to you",
					Toast.LENGTH_LONG).show();
		}
	}

	public void onBackPressed() {

		super.onBackPressed();
	}

	public class MyCountDownTimer extends CountDownTimer {

		public MyCountDownTimer(long startTime, long interval) {

			super(startTime, interval);

		}

		@Override
		public void onFinish() {

			chron.setText("Time's up!");

		}

		@Override
	    public void onTick(long millisUntilFinished) 
	    {
	    	chron.setText("Time remaining: " + formatTime(millisUntilFinished));
	    }

	}

	public void onClickFinish(View v) {
		// resultField.setText("");

		
		String name = "";
		String la = "";
		String lo = "";
		List<Address> addresses;
		StringBuilder strReturnedAddress=null;
		SharedPreferences sharedPref = getSharedPreferences("userRecord",
				MODE_PRIVATE);

		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean("hasRecord", false);
		editor.putBoolean("hasFastRecord", false);
		editor.putString("note", "");
		editor.putString("picPath", null);
		editor.commit();
		final CheckBox cb = (CheckBox) findViewById(R.id.checkbox_save);
		if (cb.isChecked()) {
			name = cb.getText().toString();
			la = sharedPref.getString("latitude", "0.0");
			lo = sharedPref.getString("longitude", "0.0");
			Geocoder geocoder = new Geocoder(this, Locale.getDefault());
			try {
				addresses = geocoder.getFromLocation(Double.valueOf(la),
						Double.valueOf(lo), 1);

				if (addresses != null) {
					Address returnedAddress = addresses.get(0);
					 strReturnedAddress = new StringBuilder(
							"Address:\n");
					for (int i = 0; i < returnedAddress
							.getMaxAddressLineIndex(); i++) {
						strReturnedAddress.append(
								returnedAddress.getAddressLine(i)).append("\n");
					}
				}

			}

			catch (Exception e) {
				Log.d("geocoder error", e.getMessage());
			}
			DatabaseHandler db = new DatabaseHandler(this);
			if(strReturnedAddress==null)
			db.addArchive(new archive(la, lo, name,""));
			else
				db.addArchive(new archive(la, lo, name,strReturnedAddress.toString()));
		}
			Intent intent = new Intent(this, MainActivity.class);
			intent.setClass(FindRecord.this, MainActivity.class);
			startActivity(intent);
		

	}

	public void onClickNavi(View v) {
		SharedPreferences sharedPref = getSharedPreferences("userRecord",
				MODE_PRIVATE);
		String arg = "http://maps.google.com/maps?daddr=%3,%4&dirflg=w";
		arg = arg.replaceAll("%3", sharedPref.getString("latitude", "0.0"));
		arg = arg.replaceAll("%4", sharedPref.getString("longitude", "0.0"));
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
				Uri.parse(arg));
		startActivity(intent);

	}


	}


