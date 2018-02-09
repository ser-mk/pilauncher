package sermk.pipi.pilauncher.externalcooperation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashSet;

import sermk.pipi.pilauncher.GlobalController;
import sermk.pipi.pilib.ErrorCollector;
import sermk.pipi.pilib.ProblemStatusAPI;

public class ProblemReceiver extends BroadcastReceiver {

    private final String TAG = this.getClass().getName();

    private final ErrorCollector EC = new ErrorCollector();

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        EC.clear();
        Log.v(TAG, "intent: " + intent.toString());
        String action;
        try{
            action = intent.getAction().trim();
        } catch (Exception e){
            action = "wrong action!";
            EC.addError(action);
            Log.w(TAG, "action is not exist!");
        }
        Log.v(TAG, action);

        String content;
        try{
            content = intent.getStringExtra(Intent.EXTRA_TEXT).trim();
        } catch (Exception e){
            content = "wrong content!";
            EC.addError(content);
            Log.w(TAG, "content is not exist!");
        }
        Log.v(TAG, content);

        if(EC.hasError()) return;

        condsiderProblem(context, content);
    }

    private void condsiderProblem(Context context, final String problem){
        if(problem.isEmpty())
            return;

        GlobalController gb = (GlobalController)context.getApplicationContext();

        HashSet<String> problems =  gb.problem.problemSet();

        final String deleted = ProblemStatusAPI.deletedStatus(problem);
        if(deleted.isEmpty()){
            problems.add(problem);
        } else {
            problems.remove(deleted);
        }
    }
}
