package our.amazing.clock;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import our.amazing.clock.R;

public abstract class notificationCenter extends NotificationListenerService {

    private NotificationManager mNotificationManager;

    public void notificationCenter(){

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        Intent intent = new Intent(getApplicationContext(), Blank.class);
        mNotificationManager.notify(12321, new Notification.Builder(this)
                .setContentTitle("").setContentText("")
                .setSmallIcon(R.drawable.ic_add_24dp)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setFullScreenIntent(PendingIntent.getService(getApplicationContext(),12321,intent,PendingIntent.FLAG_NO_CREATE), true)
                .setAutoCancel(true)
                .build());


            mNotificationManager.cancel(12321);

    }



}
