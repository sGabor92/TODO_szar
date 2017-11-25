package hu.webandmore.todo.geo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

import hu.webandmore.todo.R;
import hu.webandmore.todo.ui.todo.TodoActivity;
import hu.webandmore.todo.utils.Util;

public class GeofenceReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "GeofenceReceiver called");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.i(TAG, String.valueOf(geofencingEvent.getErrorCode()));

        } else {
            Log.i(TAG, geofencingEvent.getTriggeringLocation().getLatitude() + ", " + geofencingEvent.getTriggeringLocation().getLongitude());
            int geofenceTransition = geofencingEvent.getGeofenceTransition();

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

                String geofenceTransitionDetails = getGeofenceTransitionDetails(context,
                        geofenceTransition,
                        triggeringGeofences);

                sendNotification(context, geofenceTransitionDetails);
                Log.i(TAG, geofenceTransitionDetails);
            } else {
                Log.e(TAG, context.getString(R.string.invalid_type));
            }
        }
    }

    private String getGeofenceTransitionDetails(Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(context, geofenceTransition);

        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    private void sendNotification(Context context, String notificationDetails) {
        Intent notificationIntent = new Intent(context, TodoActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(context,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(R.drawable.ic_my_location_accent)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.ic_my_location_accent))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText(context.getString(R.string.there_is_a_todo_for_you))
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        assert mNotificationManager != null;
        mNotificationManager.notify(Util.getID(), builder.build());
    }

    private String getTransitionString(Context context, int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return context.getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return context.getString(R.string.geofence_transition_exited);
            default:
                return context.getString(R.string.unknown_geofence_transition);
        }
    }
}
