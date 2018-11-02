package dte.masteriot.mdp.camerasmadrid;

import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.niqdev.mjpeg.DisplayMode;
import com.github.niqdev.mjpeg.Mjpeg;
import com.github.niqdev.mjpeg.MjpegView;


import dte.masteriot.mdp.R;

public class HogarDigitalCamara extends AppCompatActivity {

    private static final int TIMEOUT = 5;

    MjpegView mjpegView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hogar_digital_camara);
        MjpegView mjpegView = findViewById(R.id.video);

        Mjpeg.newInstance()
                .open("http://hdcamera.etsist.upm.es/axis-cgi/mjpg/video.cgi", TIMEOUT)
                .subscribe(inputStream -> {
                    mjpegView.setSource(inputStream);
                    mjpegView.setDisplayMode(DisplayMode.BEST_FIT);
                    mjpegView.showFps(true);
                });


    }

    @Override
    protected void onResume() {
        super.onResume();


    }
}
