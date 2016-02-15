package zhang.jian.greetingapp.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import zhang.jian.greetingapp.MyApplication;
import zhang.jian.greetingapp.R;
import zhang.jian.greetingapp.constants.Constant;
import zhang.jian.greetingapp.constants.Preference;
import zhang.jian.greetingapp.fragment.AboutFragment;
import zhang.jian.greetingapp.fragment.MapFragment;
import zhang.jian.greetingapp.fragment.SignUpFragment;

public class MainActivity extends AppCompatActivity implements SignUpFragment.Callback, MapFragment.Callback {

    public static final String[] sMenuItems = {"Menu", "Home", "About"};
    public static final String[] sMenuItemsWithLogout = {"Menu", "Home", "About", "Logout"};
    private final static int BANNER_TIME_OUT = 3000;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private TextView mTvTitle;
    private TextView mTvBanner;
    private ListView mDrawerList;
    private MenuAdapter mAdapter;
    private SharedPreferences mSharedPrefs;
    private RelativeLayout mBannerLayout;
    private ActionBar mActionBar;
    private ImageButton mBtnBannerClose;
    private RelativeLayout mMenuLayout;
    private FragmentManager mFragmentManager;
    private RelativeLayout mAddressLayout;
    private TextView mTvCurrentAddress;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        MyApplication application = (MyApplication) getApplication();
        mTracker = application.getDefaultTracker();
        trackAppLaunchedFromUrl();

        initVars();
        initToolBar();
        initDrawerListener();
        initView();
        setupBannerAction();
    }

    private void trackAppLaunchedFromUrl() {
        Intent intent = getIntent();
        String url = null;
        if (intent.getData() != null) {
            url = intent.getData().toString();
        }
        // if the activity is launched from the url, then track the event
        if (mTracker != null && url != null && url.equals(Constant.APP_URL)) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("App launched from url")
                    .build());
        }
    }

    private void initView() {
        if (mSharedPrefs.getBoolean(Preference.PREF_SIGN_UP_FINISHED, false)) {
            initAsMapView();
        } else {
            initAsSignUpView();
        }
        mDrawerList.setAdapter(mAdapter);
    }

    private void initDrawerListener() {
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerLayout.setDrawerListener((mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0)));
    }

    private void setupBannerAction() {
        mBtnBannerClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissBanner();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTracker != null) {
            mTracker.setScreenName("MainActivity");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        switch (position) {
            case 1:
                if (mSharedPrefs.getBoolean(Preference.PREF_SIGN_UP_FINISHED, false)) {
                    displayMapView();
                } else {
                    displaySignUpView();
                }
                break;
            case 2:
                displayAboutView();
                break;
            case 3:
                showLogoutDialog();
                break;
        }

        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mMenuLayout);
    }

    private void initVars() {
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mAdapter = new MenuAdapter(this);
        mTvTitle = (TextView) findViewById(R.id.toolbar_title);
        mTvBanner = (TextView) findViewById(R.id.banner_title);
        mBannerLayout = (RelativeLayout) findViewById(R.id.banner_layout);
        mBtnBannerClose = (ImageButton) findViewById(R.id.banner_close_button);
        mMenuLayout = (RelativeLayout) findViewById(R.id.menu_layout);
        mFragmentManager = getSupportFragmentManager();
        mAddressLayout = (RelativeLayout) findViewById(R.id.address_layout);
        mTvCurrentAddress = (TextView) findViewById(R.id.current_address);
    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
            mActionBar.setDisplayShowCustomEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void initAsMapView() {
        addMapView();
        mAdapter.addAll(sMenuItemsWithLogout);
        mTvTitle.setText(mSharedPrefs.getString(Preference.PREF_USER_NAME, ""));
    }

    private void initAsSignUpView() {
        addSignUpView();
        mAdapter.addAll(sMenuItems);
        mTvTitle.setText("");
    }

    @Override
    public void onEmailInvalid() {
        displayBanner(getString(R.string.email_invalid));
        dismissBannerWithDelay();

    }

    private void displayBanner(String bannerText) {
        mBannerLayout.setVisibility(View.VISIBLE);
        mActionBar.setDisplayHomeAsUpEnabled(false);
        mActionBar.setHomeButtonEnabled(false);
        mTvBanner.setText(bannerText);
    }

    private void dismissBanner() {
        mBannerLayout.setVisibility(View.GONE);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
    }


    private void dismissBannerWithDelay() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissBanner();
            }
        }, BANNER_TIME_OUT);
    }

    @Override
    public void onGoButtonClicked() {
        // add logout item
        updateDrawListData(sMenuItemsWithLogout);
        mTvTitle.setText(mSharedPrefs.getString(Preference.PREF_USER_NAME, ""));
        displayMapView();
    }

    private void updateDrawListData(String[] data) {
        if (mAdapter != null) {
            mAdapter.clear();
            mAdapter.addAll(data);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onGetCurrentAddress() {
        mTvCurrentAddress.setText(mSharedPrefs.getString(Preference.PREF_CURRENT_ADDRESS, ""));
    }

    @Override
    public void onNetworkFailed() {
        displayBanner(getString(R.string.no_network));
        dismissBannerWithDelay();
    }

    private static class ViewHolder {
        TextView title;
        ImageView image;
    }

    private class MenuAdapter extends ArrayAdapter<String> {

        public MenuAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            String title = getItem(position);

            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.menu_item, parent, false);
                viewHolder.title = (TextView) convertView.findViewById(R.id.menu_title);
                viewHolder.image = (ImageView) convertView.findViewById(R.id.menu_image);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.title.setText(title);
            if (position == 0) {
                viewHolder.image.setImageResource(R.drawable.menu_back);
                viewHolder.title.setTextColor(getResources().getColor(R.color.menuLblColor));
            }

            return convertView;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mActionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }


    private void logout() {
        setSignUpFinishedFalse();
        // remove the logout item
        updateDrawListData(sMenuItems);
        mTvTitle.setText("");
        displaySignUpView();
    }

    private void showLogoutDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.alert_lbl))
                .setMessage(getString(R.string.alert_logout))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logout();
                    }
                }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
        alertDialog.setCanceledOnTouchOutside(false);

    }

    private void setSignUpFinishedFalse() {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putBoolean(Preference.PREF_SIGN_UP_FINISHED, false);
        editor.apply();
    }

    private void addMapView() {
        addFragment("MapFragment", MapFragment.newInstance(), View.VISIBLE);
    }

    private void addSignUpView() {
        addFragment("SignUpFragment", SignUpFragment.newInstance(), View.GONE);
    }

    private void displayMapView() {
        replaceFragment("MapFragment", MapFragment.newInstance(), View.VISIBLE);
    }

    private void displayAboutView() {
        replaceFragment("AboutFragment", AboutFragment.newInstance(), View.GONE);
    }

    private void displaySignUpView() {
        replaceFragment("SignUpFragment", SignUpFragment.newInstance(), View.GONE);
    }

    private void addFragment(String tag, Fragment fragment, int visibility) {
        if (mFragmentManager.findFragmentByTag(tag) == null) {
            mFragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, fragment, tag)
                    .commit();
            mAddressLayout.setVisibility(visibility);
        }
    }

    private void replaceFragment(String tag, Fragment fragment, int visibility) {
        if (mFragmentManager.findFragmentByTag(tag) == null) {
            mFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment, tag)
                    .commit();
            mAddressLayout.setVisibility(visibility);
        }
    }
}
