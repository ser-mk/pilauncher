package sermk.pipi.game;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.orhanobut.logger.Logger;

/**
 * Created by echormonov on 24.11.17.
 */

public final class PiHandler extends Handler implements CVResolver.ICallbackPosition {

    /** Command to the service to display a message */
    static final int MSG_SAY_HELLO = 1;

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger;// = new Messenger(new IncomingHandler());
    private Messenger replyTo = null;

    public PiHandler() {
        super();
        mMessenger = new Messenger(this);
    }

    public Messenger getMessenger() {
        return mMessenger;
    }

    @Override
    public void handleMessage(Message msg) {
        Logger.v("msg.what" + String.valueOf(msg.what));

        //Setup the reply message
        Message message = Message.obtain(null, 2, 0, 0);
        try
        {
            //make the RPC invocation
            replyTo = msg.replyTo;
            replyTo.send(message);
        }
        catch(RemoteException rme)
        {
            //Show an Error Message
            //Toast.makeText(RemoteService.this, "Invocation Failed!!", Toast.LENGTH_LONG).show();
            Logger.e("Invocation Failed!!");
        }
    }

    @Override
    public boolean callbackPosition(int pos, CVResolver cvr) {
        /**/
        Message message = Message.obtain(null, 2, 0, 0);
        if(replyTo==null){
            return false;
        }
        try
        {
            replyTo.send(message);
        }
        catch(RemoteException rme)
        {
            //Show an Error Message
            //Toast.makeText(RemoteService.this, "Invocation Failed!!", Toast.LENGTH_LONG).show();
            Logger.e(rme,"replyTo.send Failed!!");
        }

        return true;
    }
}
