package com.example.sangameswaran.mapdemo;

import android.app.Activity;
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
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener,GoogleMap.OnMarkerClickListener {
    static int i=0;
    TextView distanceText,durationText;
    GoogleApiClient mGoogleApiClient;
    Handler h;
    Runnable IterateInstructions;
    GoogleMap map;
    Marker TruckMarker;
    LatLng source,destination;
    LocationRequest mLocationRequest;
    String LOCATION_REQUEST_URL="https://api.myjson.com/bins/11ds3p";
    String HIT_URL;
    String DISTANCE_MATRIX_URL;
    @Override
    protected void onResume()
    {
        if(map!=null){
        Toast.makeText(getApplicationContext(),"Runnable restarted",Toast.LENGTH_LONG).show();
        IterateInstructions=new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"Refreshing location",Toast.LENGTH_LONG).show();
                Marker marker;
                map.setOnMarkerClickListener(MainActivity.this);
                RequestQueue getCoordinatesQueue=Volley.newRequestQueue(MainActivity.this);
                JsonObjectRequest coordinatesJSON=new JsonObjectRequest(Request.Method.GET, LOCATION_REQUEST_URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject customerAddressJSON=response.getJSONObject("customerAddress");
                            JSONObject currentTruckLocationJSON=response.getJSONObject("truckLocation");
                            Double customerLocationLattitude=Double.parseDouble(customerAddressJSON.getString("lat"));
                            Double customerLocationLongitude=Double.parseDouble(customerAddressJSON.getString("long"));
                            Double currentTruckLocationLattitude=Double.parseDouble(currentTruckLocationJSON.getString("lat"));
                            Double currentTruckLocationLongitude=Double.parseDouble(currentTruckLocationJSON.getString("long"));
                            source=new LatLng(currentTruckLocationLattitude,currentTruckLocationLongitude);
                            destination=new LatLng(customerLocationLattitude,customerLocationLongitude);
                            //googleMap.clear();
                            //googleMap.addMarker(new MarkerOptions().position(source).title("Your Truck"));
                            //googleMap.addMarker(new MarkerOptions().position(destination).title("Delivery Location"));
                            getAndPrintDirectionsApi(source,destination);
                            SetTimeAndDistance(source,destination);
                            if(i==0) {
                                i++;
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(source, 10));
                                map.animateCamera(CameraUpdateFactory.zoomTo(8), 2000, null);
                            }

                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(),"Error in Parsing JSON",Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error Resopnse","error");
                       Toast.makeText(getApplicationContext(),"Network Error, Rerequesting coordinates...",Toast.LENGTH_LONG).show();

                    }
                });
                getCoordinatesQueue.add(coordinatesJSON);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                map.setMyLocationEnabled(true);
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                //googleMap.setTrafficEnabled(true);
                //googleMap.setIndoorEnabled(true);
                //googleMap.setBuildingsEnabled(true);
                map.getUiSettings().setZoomControlsEnabled(true);
                h.postDelayed(this,10000);

            }
        };
        h.postDelayed(IterateInstructions,10000);
        }
        super.onResume();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        distanceText=(TextView)findViewById(R.id.tvdisplaydistance);
        durationText=(TextView)findViewById(R.id.tvdisplaytime);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        map=googleMap;
        h=new Handler();
        IterateInstructions=new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"Refreshing location",Toast.LENGTH_LONG).show();
                map=googleMap;
                Marker marker;
                map.setOnMarkerClickListener(MainActivity.this);
                RequestQueue getCoordinatesQueue=Volley.newRequestQueue(MainActivity.this);
                JsonObjectRequest coordinatesJSON=new JsonObjectRequest(Request.Method.GET, LOCATION_REQUEST_URL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject customerAddressJSON=response.getJSONObject("customerAddress");
                            JSONObject currentTruckLocationJSON=response.getJSONObject("truckLocation");
                            Double customerLocationLattitude=Double.parseDouble(customerAddressJSON.getString("lat"));
                            Double customerLocationLongitude=Double.parseDouble(customerAddressJSON.getString("long"));
                            Double currentTruckLocationLattitude=Double.parseDouble(currentTruckLocationJSON.getString("lat"));
                            Double currentTruckLocationLongitude=Double.parseDouble(currentTruckLocationJSON.getString("long"));
                            source=new LatLng(currentTruckLocationLattitude,currentTruckLocationLongitude);
                            destination=new LatLng(customerLocationLattitude,customerLocationLongitude);
                            //googleMap.clear();
                            //googleMap.addMarker(new MarkerOptions().position(source).title("Your Truck"));
                            //googleMap.addMarker(new MarkerOptions().position(destination).title("Delivery Location"));
                            getAndPrintDirectionsApi(source,destination);
                            SetTimeAndDistance(source,destination);
                            if(i==0) {
                                i++;
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source, 10));
                                googleMap.animateCamera(CameraUpdateFactory.zoomTo(8), 2000, null);
                            }

                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(),"Error in Parsing JSON",Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(),"Network error Rerequesting coordinates",Toast.LENGTH_LONG).show();

                    }
                });
                getCoordinatesQueue.add(coordinatesJSON);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                googleMap.setMyLocationEnabled(true);
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
               //googleMap.setTrafficEnabled(true);
                //googleMap.setIndoorEnabled(true);
                //googleMap.setBuildingsEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                h.postDelayed(this,10000);

            }
        };
        h.postDelayed(IterateInstructions,2000);
    }

    private void getAndPrintDirectionsApi(LatLng source, LatLng destination) {
        final LatLng source1=source;
        final LatLng destination1=destination;
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
                                    hm.put("lat", Double.toString(((LatLng) list.get(l)).latitude));
                                    hm.put("lng", Double.toString(((LatLng) list.get(l)).longitude));
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
                            polyLineOptions.width(10);
                            polyLineOptions.color(Color.BLUE);
                        }
                        map.clear();
                        TruckMarker=map.addMarker(new MarkerOptions().position(source1).title("Your Truck").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_name)));
                        map.addMarker(new MarkerOptions().position(destination1).title("Delivery Location"));
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
        map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title("My Location"));
    }

    @Override
    protected void onPause() {
        h.removeCallbacks(IterateInstructions);
        Toast.makeText(getApplicationContext(),"Runnable stopped",Toast.LENGTH_LONG).show();
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        h.removeCallbacks(IterateInstructions);
        Toast.makeText(getApplicationContext(),"Runnable stopped",Toast.LENGTH_LONG).show();
        super.onBackPressed();

    }
    public String generateDistanceMatrixURL(LatLng source,LatLng destination)
    {
        String URL="https://maps.googleapis.com/maps/api/distancematrix/json?";
        String queryParams="origins="+source.latitude+","+source.longitude+"&"+"destinations="+destination.latitude+","+destination.longitude;
        return  URL+queryParams;
    }
    private String generateApiUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin+"&"+str_dest+"&"+sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        return url;
    }

    public void SetTimeAndDistance(LatLng source,LatLng destination)
    {
        DISTANCE_MATRIX_URL=generateDistanceMatrixURL(source,destination);
        final RequestQueue requestQueue=Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest getTimeAndDistance=new JsonObjectRequest(Request.Method.GET, DISTANCE_MATRIX_URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                JSONArray rows = null;
                JSONObject rowsJSON=null;
                JSONArray elements=null;
                JSONObject elementsJSON=null;
                JSONObject distanceJSON,durationJSON;
                try {
                    rows=response.getJSONArray("rows");
                    rowsJSON=rows.getJSONObject(0);
                    elements=rowsJSON.getJSONArray("elements");
                    elementsJSON=elements.getJSONObject(0);
                    distanceJSON=elementsJSON.getJSONObject("distance");
                    durationJSON=elementsJSON.getJSONObject("duration");
                    distanceText.setText("DISTANCE : "+distanceJSON.getString("text"));
                    durationText.setText("ETA : "+durationJSON.getString("text"));
                    distanceText.setVisibility(View.GONE);
                    durationText.setVisibility(View.GONE);
                }
                catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),"JSON Parse error",Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(),"Network error,Rerequesting coordinates",Toast.LENGTH_LONG).show();
            }
        });
        requestQueue.add(getTimeAndDistance);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(TruckMarker))
        {
            Toast.makeText(getApplicationContext(),"Stay cool your truck is on the way",Toast.LENGTH_LONG).show();
            distanceText.setVisibility(View.VISIBLE);
        }

        return false;
    }
}
