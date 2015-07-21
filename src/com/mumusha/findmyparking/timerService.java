package com.mumusha.findmyparking;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

/**
 * FibonacciService is an example of a started service. The service will
 * automatically be started whenever a client sends an intent and will also
 * automatically be terminated when the work item has been processed. Since the
 * application might have terminated while the service was still processing the
 * request, the results are posted as a notification. When the user taps on the
 * notification the corresponding application (FibonacciActivity in this case)
 * will automatically be launched.
 * 
 * FibonacciService is derived from IntentService which is a helper class to
 * implement a simple message queuing mechanism for Intents that will
 * automatically run in a background thread. IntentService is itself derived
 * from class Service.
 */
public class timerService extends IntentService {

    public timerService() {
        /*
         * The string parameter "FibonacciService" is used as the name of the
         * thread that will be created to process incoming intents.
         */
        super("timerService");
    }

    /**
     * onHandleIntent() will be called by the base class IntentService whenever
     * there is an Intent to be processed. This method will be run in its own
     * thread (automatically created by IntentService) so it can safely run long
     * running operations. Intents are delivered one after another.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        /*
         * Retrieve the parameter.
         */
        int n = intent.getExtras().getInt("n");
         
        //shorter time for test.
        int res = n*10*60;

        try {
            Thread.sleep(res);
        } catch (InterruptedException e) {
        }

        showNotification(n, res);
    }

    /**
     * showNotification() is a helper method that displays the result as a
     * status bar notification. If the user taps on the notification, the
     * corresponding application (FibonacciActivity) will automatically be
     * launched.
     */
   
	private void showNotification(int n, int res) {
        /*
         * Create a new notification with the help of the notification manager.
         * Set a title, icon and sound for the notification and make it
         * cancellable (i.e., the user can dismiss it from the status bar).
         */
        
        
        
        Intent intent = new Intent(this, FindRecord.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Build notification
        // Actions are just fake
        Notification noti = new Notification.Builder(this)
            .setContentTitle("Your Parking time is done!")
            .setContentText("Click to find your car").setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(pIntent)
            .addAction(R.drawable.ic_launcher, "Call", pIntent)
            .addAction(R.drawable.ic_launcher, "More", pIntent)
            .addAction(R.drawable.ic_launcher, "And more", pIntent).build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);
    }

}

