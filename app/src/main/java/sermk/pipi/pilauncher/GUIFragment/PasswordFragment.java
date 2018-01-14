package sermk.pipi.pilauncher.GUIFragment;


import android.os.Bundle;
import android.app.Fragment;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import sermk.pipi.pilauncher.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PasswordFragment extends Fragment implements View.OnKeyListener {

    final String TAG = this.getClass().getName();
    private final String etalon = "741236985";


    public PasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_password, container, false);

        ((EditText)rootView.findViewById(R.id.password)).setOnKeyListener(this);

        timer.start();

        return rootView;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(event.getAction() != KeyEvent.ACTION_DOWN ||
            (keyCode != KeyEvent.KEYCODE_ENTER)){
            return false;
        }

        EditText editText = (EditText)v;
        // сохраняем текст, введенный до нажатия Enter в переменную
        String pass = editText.getText().toString();
        Log.v(TAG, pass);

        if(!etalon.equals(pass)){
            Log.v(TAG,"unknown pass!");
            return false;
        }

        timer.cancel();

        getFragmentManager().beginTransaction()
            .replace(R.id.container, new TestCV_Fragment(), TestCV_Fragment.class.getName())
            .addToBackStack("")
            .commit();

        return true;

    }

    @Override
    public void onDetach() {
        Log.v(TAG, "onDetach");
        timer.cancel();
        super.onDetach();
    }

    private final int TIMEOUT = 20*1000;

    final CountDownTimer timer = new CountDownTimer(TIMEOUT, TIMEOUT){
        @Override
        public void onTick(long millisUntilFinished) {
            Log.v(TAG, "onTick");
        }

        @Override
        public void onFinish() {
            Log.v(TAG, "finish timer");
            //todo: check current visible fragment!
            getFragmentManager().beginTransaction()
                .replace(R.id.container, new StatusFragment(), StatusFragment.class.getName())
                .commit();
        }
    };

}
