package com.example.myapplication.ui.Notes;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.DataManager;
import com.example.myapplication.Net;
import com.example.myapplication.R;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AudioActivity extends AppCompatActivity {
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private static final String LOG_TAG = "AudioRecording";
    private Button startbtn, stopbtn, playbtn, stopplay, saveAudioBtn;
    private File audioFile;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        audioFile = new File(getExternalFilesDir(null),
                DataManager.GetCurTimeMilli()+".3gp");

        startbtn = (Button)findViewById(R.id.btnRecord);
        stopbtn = (Button)findViewById(R.id.btnStop);
        playbtn = (Button)findViewById(R.id.btnPlay);
        stopplay = (Button)findViewById(R.id.btnStopPlay);
        saveAudioBtn = (Button)findViewById(R.id.saveAudioBtn);
        stopbtn.setEnabled(false);
        playbtn.setEnabled(false);
        stopplay.setEnabled(false);
        saveAudioBtn.setEnabled(false);

        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CheckPermissions()) {
                    stopbtn.setEnabled(true);
                    startbtn.setEnabled(false);
                    playbtn.setEnabled(false);
                    stopplay.setEnabled(false);
                    saveAudioBtn.setEnabled(false);
                    mRecorder = new MediaRecorder();
                    resetRecorder();
                    mRecorder.start();
                    Toast.makeText(getApplicationContext(), "Recording Started", Toast.LENGTH_LONG).show();
                }
                else
                {
                    RequestPermissions();
                }
            }
        });
        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopbtn.setEnabled(false);
                startbtn.setEnabled(true);
                playbtn.setEnabled(true);
                stopplay.setEnabled(true);
                saveAudioBtn.setEnabled(true);
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
                Toast.makeText(getApplicationContext(), "Recording Stopped", Toast.LENGTH_LONG).show();
            }
        });
        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopbtn.setEnabled(false);
                startbtn.setEnabled(true);
                playbtn.setEnabled(false);
                stopplay.setEnabled(true);
                saveAudioBtn.setEnabled(true);
                mPlayer = new MediaPlayer();
                try {
                    mPlayer.setDataSource(audioFile.getAbsolutePath());
                    mPlayer.prepare();
                    mPlayer.start();
                    Toast.makeText(getApplicationContext(), "Recording Started Playing", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "prepare() failed");
                }
            }
        });
        stopplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayer.release();
                mPlayer = null;
                stopbtn.setEnabled(false);
                startbtn.setEnabled(true);
                playbtn.setEnabled(true);
                stopplay.setEnabled(false);
                saveAudioBtn.setEnabled(true);
                Toast.makeText(getApplicationContext(),"Playing Audio Stopped", Toast.LENGTH_SHORT).show();
            }
        });

        saveAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Net net = new Net();
                String uploadPath = DataManager.user.LastName+"_"+DataManager.user.ID+"/ROOM_"+DataManager.record.ID+"/Audio/"+DataManager.GetCurDate();
                if(net.doUpload(audioFile, uploadPath).equals("200")){
                    Toast.makeText(getApplicationContext(), "Saved file to: "+uploadPath, Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });
    }

    private void resetRecorder() {
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setAudioEncodingBitRate(16);
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setOutputFile(audioFile.getAbsolutePath());

        try {
            mRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == REQUEST_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0) {
                boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean permissionToStore = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                if (permissionToRecord && permissionToStore) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    public boolean CheckPermissions() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }
    private void RequestPermissions() {
        ActivityCompat.requestPermissions(AudioActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }
}