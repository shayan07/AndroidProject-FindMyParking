package com.mumusha.findmyparking;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusShare;

public class shareActivity extends Activity implements
GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  /* Request code used to invoke sign in user interactions. */
  private static final int RC_SIGN_IN = 0;

  /* Client used to interact with Google APIs. */
  private GoogleApiClient mGoogleApiClient;

  /* A flag indicating that a PendingIntent is in progress and prevents
   * us from starting further intents.
   */
  private boolean mIntentInProgress;
  private SharedPreferences sharedPref;
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_share);
    mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(Plus.API, null)
        .build();
     sharedPref = getSharedPreferences("userRecord",
			MODE_PRIVATE);
    TextView x=(TextView) findViewById(R.id.txtAlertText);
    x.setText("I found a new parking location around "+sharedPref.getString("gpStreetName", ""));
    Button shareButton = (Button) findViewById(R.id.share_button1);
    shareButton.setOnClickListener(new OnClickListener() {
        @Override
        
        public void onClick(View v) {
          // Launch the Google+ share dialog with attribution to your app.
          Intent shareIntent = new PlusShare.Builder(shareActivity.this)
              .setType("text/plain")
              .setText("I found a new parking location around \n"+sharedPref.getString("gpStreetName", ""))
              
              .getIntent();

          startActivityForResult(shareIntent, 0);
        }
    });
  }
  //.setContentUrl(Uri.parse("https://developers.google.com/+/"))
  protected void onStart() {
    super.onStart();
    mGoogleApiClient.connect();
  }

  protected void onStop() {
    super.onStop();

    if (mGoogleApiClient.isConnected()) {
      mGoogleApiClient.disconnect();
    }
  }
  public void onConnectionSuspended(int cause) {
	  mGoogleApiClient.connect();
	}
  public void onConnectionFailed(ConnectionResult result) {
	  if (!mIntentInProgress && result.hasResolution()) {
	    try {
	      mIntentInProgress = true;
	      result.startResolutionForResult(this, // your activity
                  RC_SIGN_IN);
	    } catch (SendIntentException e) {
	      // The intent was canceled before it was sent.  Return to the default
	      // state and attempt to connect to get an updated ConnectionResult.
	      mIntentInProgress = false;
	      mGoogleApiClient.connect();
	    }
	  }
	}

	public void onConnected(Bundle connectionHint) {
	  // We've resolved any connection errors.  mGoogleApiClient can be used to
	  // access Google APIs on behalf of the user.
	}
	
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		  if (requestCode == RC_SIGN_IN) {
		    mIntentInProgress = false;

		    if (!mGoogleApiClient.isConnecting()) {
		      mGoogleApiClient.connect();
		    }
		  }
		}
}