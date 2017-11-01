package sermk.pipi.game;

import android.app.Fragment;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ToggleButton;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
class TestCV_Fragment extends Fragment {

    private final String TAG  = "TestCV_Fragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private ToggleButton mPreviewButton;
    private ImageView mImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_test_cv_, container, false);

        mPreviewButton = (ToggleButton)rootView.findViewById(R.id.start_test);
        setPreviewButton(false);
        mPreviewButton.setOnCheckedChangeListener(mOnCheckedChangeListener);

        mImageView = (ImageView)rootView.findViewById(R.id.capture_view);

        // Inflate the layout for this fragment
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

    private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            Log.v(TAG, "Test CV enable " + isChecked);
            GlobalController app = (GlobalController)getActivity().getApplication();
            if (isChecked) {
                CVResolver.Settings settings = new CVResolver.Settings();
                settings.captureView = mImageView;
                app.getUVCReciver().startCapture(settings);
            } else {
                app.getUVCReciver().exitRun();
            }
        }
    };

    private void setPreviewButton(final boolean onoff) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPreviewButton.setOnCheckedChangeListener(null);
                try {
                    mPreviewButton.setChecked(onoff);
                } catch (Exception e) {
                    Log.w(TAG, e);
                }
            }
        });
    }

}
