package zhang.jian.greetingapp.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import zhang.jian.greetingapp.MyApplication;
import zhang.jian.greetingapp.R;
import zhang.jian.greetingapp.Utils;
import zhang.jian.greetingapp.constants.Preference;

public class SignUpFragment extends Fragment {

    private ImageButton mBtnGo;
    private EditText mEtEmail;
    private EditText mEtFirstName;
    private EditText mEtLastName;
    private Callback mCallback;
    private Tracker mTracker;

    public interface Callback {
        void onGoButtonClicked();

        void onEmailInvalid();
    }

    public static Fragment newInstance() {
        return new SignUpFragment();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        MyApplication application = (MyApplication) getActivity().getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTracker != null) {
            mTracker.setScreenName("Greeting View");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.signup_fragment, container, false);

        initVars(rootView);
        setupGoButtonAction();
        return rootView;
    }

    private void initVars(ViewGroup rootView) {
        mBtnGo = (ImageButton) rootView.findViewById(R.id.btn_go);
        mEtEmail = (EditText) rootView.findViewById(R.id.et_email);
        mEtFirstName = (EditText) rootView.findViewById(R.id.et_firstName);
        mEtLastName = (EditText) rootView.findViewById(R.id.et_lastName);
    }

    private void setupGoButtonAction() {
        mBtnGo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String email = mEtEmail.getText().toString().trim();
                String firstName = mEtFirstName.getText().toString().trim();
                String lastName = mEtLastName.getText().toString().trim();

                if (!isValidEmail(email)) {
                    if (mCallback != null) {
                        mCallback.onEmailInvalid();
                    }
                } else if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
                    // if missing first name or last name
                    Utils.showErrorDialog(getString(R.string.fill_all_infor), getActivity());
                } else {
                    storeName(firstName, lastName);
                    if (mCallback != null) {
                        // will be implemented in the activity
                        mCallback.onGoButtonClicked();
                    }
                }
            }
        });
    }

    // store the user name and set sign up finished
    private void storeName(String firstName, String lastName) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(Preference.PREF_SIGN_UP_FINISHED, true);
        editor.putString(Preference.PREF_USER_NAME, firstName + " " + lastName);
        editor.apply();
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (Callback) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }
}
