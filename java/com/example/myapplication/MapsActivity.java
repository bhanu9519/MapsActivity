package com.example.myapplication;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
//import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.lang.Math;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import android.widget.Button;



import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback  {
    private MarkerOptions place1, place2;
    Button getDirection;
    Button clear_Marker;

    private Polyline currentPolyline;
    List<Integer> visit_order;
    private GoogleMap mMap;
    private static final int LOCATION_REQUEST = 500;
    ArrayList<LatLng> listPoints;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        listPoints = new ArrayList<>();
        //System.out.println("gjhjh")ja;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                //Reset marker when already 2
                if (listPoints.size() == 10) {
                    listPoints.clear();
                    mMap.clear();
                }
                //Save first point select
                String s=new String();
                s="";
                int n1=listPoints.size()+1;
                s= Integer.toString(n1);
                listPoints.add(latLng);
                //Create marker
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                if (listPoints.size() == 1) {
                    //Add first marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                } /*else if (listPoints.size() == 2){
                    //Add second marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }else if (listPoints.size() == 3){
                    //Add second marker to the map

                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                   // sendMessage1();
                }*/else {
                    //Add second marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

                }

                mMap.addMarker(markerOptions.title(s)).showInfoWindow();


            }
        });

    }
    public double calculate_distance(LatLng latLng,LatLng latLng1) {

        double x = latLng.latitude - latLng.latitude;
        double y = latLng.longitude - latLng1.longitude;
        double z = 0;
        z= (x*x)+(y*y);
        z=Math.sqrt(z);
        return z;



    }
    public void built_distance_matrix(){
        int n = listPoints.size();
        double [][] dist_matrix = new double[n][n];


        for (int i = 0; i < n; i++) {
            // codes inside the body of outer loop
            for (int j = 0; j <n; j++) {




                    dist_matrix[i][j] = calculate_distance(listPoints.get(i),listPoints.get(j));

            }
            // codes inside the body of outer  loop
        }
        //start tsp here

        //TSPNearestNeighbour tspNearestNeighbour = new TSPNearestNeighbour();
        //int[] visit_order = tspNearestNeighbour.tsp(dist_matrix);
        TspDynamicProgrammingRecursive solver = new TspDynamicProgrammingRecursive(dist_matrix);

         visit_order=solver.getTour();
        String s=new String();
          s="";
        for (int j = 0; j <n; j++) {
            s=s+Integer.toString(visit_order.get(j)+1)+"->";

        }
        s=s+Integer.toString(visit_order.get(n)+1);

        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();

        draw_polygon();


    }

    public void sendMessage1(){
        //built_distance_matrix();
        place1 = new MarkerOptions().position(new LatLng(27.658143, 85.3199503)).title("Location 1");
        place2 = new MarkerOptions().position(new LatLng(27.667491, 85.3208583)).title("Location 2");
        new FetchURL(MapsActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "driving"), "driving");
    }

    public void sendMessage(android.view.View view){
        //built_distance_matrix();
        //place1 = new MarkerOptions().position(new LatLng(27.658143, 85.3199503)).title("Location 1");
        //place2 = new MarkerOptions().position(new LatLng(27.667491, 85.3208583)).title("Location 2");
        //new FetchURL(MapsActivity.this).execute(getUrl(place1.getPosition(), place2.getPosition(), "driving"), "driving");
        if (listPoints.size() < 3) {
            Toast.makeText(getApplicationContext(),"Select atleat 3 destinations.",Toast.LENGTH_LONG).show();

        }
        else {
        built_distance_matrix();}
       // total_distance();
    }
    public void remove_marker(android.view.View view){


        if (listPoints.size()>0){
            listPoints.clear();
            mMap.clear();
        }
        if (visit_order !=null){
            visit_order.clear();
        }


    }
    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + "AIzaSyBod6pefdmoG6XZ759HIshOGxvdCNloZ5I";
        return url;
    }

    public void draw_polygon(){
 PolylineOptions line = new PolylineOptions();
        int n = listPoints.size();
        for (int j = 0; j <=n; j++) {
            //visit_order.get(j);
            line.add(listPoints.get(visit_order.get(j)));
        }

        line.width(5);
        line.color(Color.RED);
        mMap.addPolyline(line);


}
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }
        return (dist);
    }


    public void total_distance(android.view.View view){
        if (listPoints.size() < 3)  {
            Toast.makeText(getApplicationContext(),"Select atleat 3 destinations.",Toast.LENGTH_LONG).show();

        }

        else {
            if ((visit_order != null)== false){

                sendMessage(view);
            }
            else if (visit_order.size()==0){
                sendMessage(view);
            }
            get_dist();}

    }

    public void get_dist(){
        double t=0;
        int n = listPoints.size();
        for (int j = 0; j <n; j++) {
            visit_order.get(j);
            //line.add(listPoints.get(visit_order.get(j)));
            t+=distance(listPoints.get(visit_order.get(j)).latitude,listPoints.get(visit_order.get(j)).longitude,listPoints.get(visit_order.get(j+1)).latitude,listPoints.get(visit_order.get(j+1)).longitude,'K');
        }
        int t1 = (int)t;
        Toast.makeText(getApplicationContext(),"Distance = "+Integer.toString(t1)+" KM",Toast.LENGTH_SHORT).show();
        get_time(t);


    }

    public void get_time(double distance){
        double hours_offset = (distance/60);
        int hours_ = Math.abs(((int) distance)/60);
        double min_offset = (hours_offset - hours_)*60;

        int min = (int) min_offset;

        int sec = (int)((min_offset - min)*60);
        String s= "Time = "+hours_ + ":" + min + ":" + sec;

        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();



    }

    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }
}