package zhang.jian.greetingapp.fragment;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import zhang.jian.greetingapp.MyApplication;
import zhang.jian.greetingapp.R;
import zhang.jian.greetingapp.Utils;
import zhang.jian.greetingapp.constants.Preference;

public class MapFragment extends SupportMapFragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Callback mCallback;
    private Tracker mTracker;
    private static final int PERMISSIONS_REQUEST_LOCATION = 110;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    public interface Callback {
        void onGetCurrentAddress();

        void onNetworkFailed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getMapAsync(this);
        MyApplication application = (MyApplication) getActivity().getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTracker != null) {
            mTracker.setScreenName("Location View");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            onNoPermissionAction();
        } else {
            setUpMapActions();
        }
    }

    private void onNoPermissionAction(){
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            // if the user deny the permission before
            Toast.makeText(getActivity(), getString(R.string.request_location_permission), Toast.LENGTH_LONG).show();
        } else {
            // first time no permission, then request it
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // if the user agree the permission, then set up the map action
                    setUpMapActions();
                } else {
                    // if the user deny the permission
                    Toast.makeText(getActivity(), getString(R.string.request_location_permission), Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    private void setUpMapActions() {
        try {
            mMap.setMyLocationEnabled(true);
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            // get last known current location
            Location myLocation = getLastKnownLocation();
            double latitude = myLocation.getLatitude();
            double longitude = myLocation.getLongitude();
            LatLng currentLoc = new LatLng(latitude, longitude);
            zoomToCurrentLocation(currentLoc);

            if (!Utils.networkConnected(getActivity())) {
                if (mCallback != null) {
                    mCallback.onNetworkFailed();
                }
            } else {
                new GetAddressTask().execute(currentLoc);
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    private Location getLastKnownLocation() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        Location location = null;
        // loop through providers to get the best location
        for (String provider : providers) {
            try {
                location = locationManager.getLastKnownLocation(provider);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            if (location == null) {
                continue;
            }
            // get a location with better accuracy
            if (bestLocation == null || location.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = location;
            }
        }
        return bestLocation;
    }

    public class GetAddressTask extends AsyncTask<LatLng, Void, String> {
        private LatLng currentLoc;

        @Override
        protected String doInBackground(LatLng... params) {
            String addressText = "";
            currentLoc = params[0];

            Geocoder geoCoder = new Geocoder(getActivity(), Locale.getDefault());
            try {
                List<Address> addresses = geoCoder.getFromLocation(currentLoc.latitude,
                        currentLoc.longitude, 1);
                // get address from the address list
                addressText = getAddress(addresses);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addressText;
        }

        @Override
        protected void onPostExecute(String addressText) {
            super.onPostExecute(addressText);
            setUpMakerWithAddress(currentLoc, addressText);
            storeAddress(addressText);

            if (mCallback != null) {
                mCallback.onGetCurrentAddress();
            }
        }
    }

    private String getAddress(List<Address> addresses) {
        String addressText = "";
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            addressText = String.format(
                    "%s, %s, %s",
                    address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                    address.getLocality(),
                    address.getCountryName());
        }
        return addressText;
    }

    private void zoomToCurrentLocation(LatLng currentLoc) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currentLoc)      // Sets the center of the map to LatLng (refer to previous snippet)
                .zoom(17)                   // Sets the zoom
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    // set red maker with address text
    private void setUpMakerWithAddress(LatLng currentLoc, String addressText) {
        mMap.addMarker(
                new MarkerOptions()
                        .position(currentLoc)
                        .draggable(true)
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .title(addressText))
                .showInfoWindow();
    }

    // store current address into shared preference
    private void storeAddress(String addressText) {
        if (getActivity() != null) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(Preference.PREF_CURRENT_ADDRESS, addressText);
            editor.apply();
        }
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
