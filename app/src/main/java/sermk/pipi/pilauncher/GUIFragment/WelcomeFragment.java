package sermk.pipi.pilauncher.GUIFragment;

import android.app.FragmentManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import sermk.pipi.pilauncher.R;


public class WelcomeFragment extends Fragment implements View.OnClickListener {

    private static final int COUNT_MAX = 11;
    private final String TAG = this.getClass().getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    static int count = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView =  inflater.inflate(R.layout.fragment_welcome, container, false);

        ((ImageView)rootView.findViewById(R.id.image_welcome)).setOnClickListener(this);
        count = 0;

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Log.v(TAG,"onBackStackChanged");
            }
        });

        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        Log.v(TAG,"onClick");
        count+=1;
        if(count < COUNT_MAX) return;
        Log.v(TAG,"replace");
        getFragmentManager().beginTransaction()
            .replace(R.id.container, new PasswordFragment(), PasswordFragment.class.getName())
            .addToBackStack("")
            .commit();
    }
}
