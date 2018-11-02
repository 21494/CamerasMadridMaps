package dte.masteriot.mdp.camerasmadrid;

import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.niqdev.mjpeg.DisplayMode;
import com.github.niqdev.mjpeg.Mjpeg;
import com.github.niqdev.mjpeg.MjpegView;
import com.google.android.gms.maps.model.LatLng;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;

import dte.masteriot.mdp.R;

public class HogarDigitalCamara extends AppCompatActivity {

    private static final int TIMEOUT = 5;

    MjpegView mjpegView;
    private Button up;
    private Button down;
    private Button left;
    private Button right;
    private Button zoomout;
    private Button zoomin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hogar_digital_camara);
        MjpegView mjpegView = findViewById(R.id.video);

        up = (Button) findViewById(R.id.up);
        down = (Button) findViewById(R.id.down);
        /*left = (Button) findViewById(R.id.left);
        right = (Button) findViewById(R.id.right);
        zoomin = (Button) findViewById(R.id.zoomin);
        zoomout = (Button) findViewById(R.id.zoomout);*/

        Mjpeg.newInstance()
                .open("http://hdcamera.etsist.upm.es/axis-cgi/mjpg/video.cgi", TIMEOUT)
                .subscribe(inputStream -> {
                    mjpegView.setSource(inputStream);
                    mjpegView.setDisplayMode(DisplayMode.BEST_FIT);
                    mjpegView.showFps(true);
                });


    }

    public void onClickRight(View view){
        CameraActions task = new CameraActions();
        task.execute( "move=right" );
    }
    public void onClickLeft(View view){
        CameraActions task = new CameraActions();
        task.execute( "move=left" );
    }
    public void onClickUp(View view){
        CameraActions task = new CameraActions();
        task.execute( "move=up" );
    }
    public void onClickDown(View view){
        CameraActions task = new CameraActions();
        task.execute( "move=down" );
    }
    public void onClickZoomIn(View view){
        CameraActions task = new CameraActions();
        task.execute( "rzoom=2500" );
    }
    public void onClickZoomOut(View view){
        CameraActions task = new CameraActions();
        task.execute( "rzoom=-2500" );
    }



    @Override
    protected void onResume() {
        super.onResume();


    }
}

class CameraActions extends AsyncTask<String, Void, Void>{

    private String contentType = "";
    private boolean error = false;
    private String errorMessage;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected Void doInBackground(String... strings) {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL("http://hdcamera.etsist.upm.es/axis-cgi/com/ptz.cgi?camera=1&" + strings[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            String userCredentials = "masteriot:pasalobien";
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));

            urlConnection.setRequestProperty ("Authorization", basicAuth);
            contentType = urlConnection.getContentType();

        }catch (Exception e){

        }
        return null;
    }
}
