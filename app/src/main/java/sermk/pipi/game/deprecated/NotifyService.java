package sermk.pipi.game.deprecated;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import sermk.pipi.game.R;
import sermk.pipi.game.Standalone;

/**
 * Created by echormonov on 18.11.17.
 */

final public class NotifyService extends Service {

    NotificationManager mNotificationManager;
    final String TAG = "NotifyService";
    private static final int NOTIFICATION = R.string.app_name;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.v("onCreate service");
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification("@@@@@@@@@@@@@@");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.v("onStartCommand service");
        return super.onStartCommand(intent, flags, startId);
    }

    private void showNotification(final CharSequence text) {
        Log.v(TAG, "showNotification:" + text);
        // Set the info for the views that show in the notification panel.
        final Notification notification = new Notification.Builder(this)
            .setSmallIcon(R.drawable.ic_launcher)  // the status icon
            .setTicker(text)  // the status text
            .setWhen(System.currentTimeMillis())  // the time stamp
            .setContentTitle(getText(R.string.app_name))  // the label of the entry
            .setContentText(text)  // the contents of the entry
            .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, Standalone.class), 0))  // The intent to send when the entry is clicked
            .build();

        startForeground(NOTIFICATION, notification);
        // Send the notification.
        mNotificationManager.notify(NOTIFICATION, notification);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    /** Command to the service to display a message */
    static final int MSG_SAY_HELLO = 1;

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Logger.v("msg.what" + String.valueOf(msg.what));

            //Setup the reply message
            Message message = Message.obtain(null, 2, 0, 0);
            try
            {
                //make the RPC invocation
                Messenger replyTo = msg.replyTo;
                replyTo.send(message);
            }
            catch(RemoteException rme)
            {
                //Show an Error Message
                //Toast.makeText(RemoteService.this, "Invocation Failed!!", Toast.LENGTH_LONG).show();
                Logger.e("Invocation Failed!!");
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Logger.v("onBind service! " + intent.toString());
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }
}
