package com.example.sangameswaran.mapdemo;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {
    static int i=0;
    GoogleApiClient mGoogleApiClient;
    GoogleMap map;
    LatLng source,destination;
    LocationRequest mLocationRequest;
    String HIT_URL="https://maps.googleapis.com/maps/api/directions/json?origin=8.526304,77.867730&destination=9.526304,77.867730&sensor=false&mode=driving";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }
    @Override
    public void onMapReady(final GoogleMap googleMap) {

       final Handler h=new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                googleMap.clear();
                map=googleMap;
                buildGoogleClientApi();
                Toast.makeText(getApplicationContext(),"RunnableThread"+i,Toast.LENGTH_LONG).show();
                Marker marker;
                source=new LatLng(8.526304, 77.867730);
                marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(8.526304, 77.867730)).title("myMarker1"));
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                googleMap.setMyLocationEnabled(true);
                googleMap.setTrafficEnabled(true);
                googleMap.setIndoorEnabled(true);
                googleMap.setBuildingsEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(8.526304, 77.867730),10));
                //googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
                i++;
                getAndPrintDirectionsApi(source,destination);
                h.postDelayed(this,8000);
            }
        },2000);
    }

    private void getAndPrintDirectionsApi(LatLng source, LatLng destination) {
        HIT_URL="";
        HIT_URL=generateApiUrl(source,destination);
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest=new JsonObjectRequest(Request.Method.GET, HIT_URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
                JSONArray jRoutes = null;
                JSONArray jLegs = null;
                JSONArray jSteps = null;
                try {
                    jRoutes = response.getJSONArray("routes");
                    for (int i = 0; i < jRoutes.length(); i++) {
                        jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                        List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();
                        for (int j = 0; j < jLegs.length(); j++) {
                            jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                            for (int k = 0; k < jSteps.length(); k++) {
                                String polyline = "";
                                polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                                List<LatLng> list = PolyUtil.decode(polyline);
                                for (int l = 0; l < list.size(); l++) {
                                    HashMap<String, String> hm = new HashMap<String, String>();
                                    hm.put("lat",
                                            Double.toString(((LatLng) list.get(l)).latitude));
                                    hm.put("lng",
                                            Double.toString(((LatLng) list.get(l)).longitude));
                                    path.add(hm);
                                }
                            }
                            routes.add(path);
                        }
                        ArrayList<LatLng> points = null;
                        PolylineOptions polyLineOptions = null;

                        // traversing through routes
                        for (i = 0; i < routes.size(); i++) {
                            points = new ArrayList<LatLng>();
                            polyLineOptions = new PolylineOptions();
                            List<HashMap<String, String>> pathi = routes.get(i);

                            for (int j = 0; j < pathi.size(); j++) {
                                HashMap<String, String> point = pathi.get(j);

                                double lat = Double.parseDouble(point.get("lat"));
                                double lng = Double.parseDouble(point.get("lng"));
                                LatLng position = new LatLng(lat, lng);

                                points.add(position);
                            }

                            polyLineOptions.addAll(points);
                            polyLineOptions.width(4);
                            polyLineOptions.color(Color.BLUE);
                        }

                        map.addPolyline(polyLineOptions);
                    }


                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Parse Error", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Volley Error resopnse",Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(jsonObjectRequest);


    }

    private String generateApiUrl(LatLng source, LatLng destination) {

        return null;
    }

    protected void buildGoogleClientApi()
    {
        mGoogleApiClient=new GoogleApiClient.Builder(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).addConnectionCallbacks(this).build();
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        destination=new LatLng(location.getLatitude(),location.getLongitude());
        map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title("My Location"));
    }
}
