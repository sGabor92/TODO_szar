package hu.webandmore.todo.notification;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.firebase.jobdispatcher.JobService;

import hu.webandmore.todo.R;
import hu.webandmore.todo.ui.todo.TodoActivity;
import hu.webandmore.todo.utils.Util;

public class DeadlineReminderService extends JobService {

    @Override
    public boolean onStartJob(com.firebase.jobdispatcher.JobParameters job) {

        String todoName = "";
        String deadline = "";

        Bundle bundle = job.getExtras();
        if(bundle != null) {
            Log.i("DeadlineReminderService", "Bundle is not null");
            try {
                todoName = bundle.getString("name");
                deadline = bundle.getString("deadline");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.i("DeadlineReminderService", "Bundle is null");
        }

        Intent notificationIntent = new Intent(this, TodoActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new
                NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.deadline_approaching))
                .setContentText(todoName + ": " + deadline)
                .setSmallIcon(R.drawable.ic_deadline_accent)
                .setPriority(Notification.PRIORITY_MAX)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_deadline_accent))
                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);
        // Dismiss notification once the user touches it.

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        assert mNotificationManager != null;
        mNotificationManager.notify(Util.getID(), builder.build());
        return false;
    }

    @Override
    public boolean onStopJob(com.firebase.jobdispatcher.JobParameters job) {
        return false;
    }
}
