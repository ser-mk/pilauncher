package sermk.pipi.game;


import android.os.RemoteException;
import android.util.Log;


/**
 * Created by echormonov on 24.11.17.
 */

public class PInterface_Impl
    //implements Pinterface.Stub
    {

    final String TAG = "PInterface_Impl";
    //@Override
    public int getPosition() throws RemoteException {
        Log.v(TAG,"getPosition ");
        return 0;
    }
}
