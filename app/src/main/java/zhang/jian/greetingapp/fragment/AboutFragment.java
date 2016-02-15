package zhang.jian.greetingapp.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import zhang.jian.greetingapp.BuildConfig;
import zhang.jian.greetingapp.R;

public class AboutFragment extends Fragment {

    public static Fragment newInstance() {
        return new AboutFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.about_fragment, container, false);

        // setup app version
        TextView tvAppVersion = (TextView)rootView.findViewById(R.id.tv_appVersion);
        String versionName = BuildConfig.VERSION_NAME;
        tvAppVersion.setText(getString(R.string.app_version_lbl, versionName));

        // setup author name
        TextView tvAuthor = (TextView)rootView.findViewById(R.id.tv_author);
        tvAuthor.setText(getString(R.string.app_author_lbl, getString(R.string.author_name)));

        return rootView;
    }
}
