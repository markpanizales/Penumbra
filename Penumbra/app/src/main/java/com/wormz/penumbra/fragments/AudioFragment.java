package com.wormz.penumbra.fragments;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.wormz.penumbra.GMailSender;
import com.wormz.penumbra.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by markanthonypanizales on 4/18/15.
 */
public class AudioFragment extends Fragment implements View.OnClickListener{
    private final static String TAG = "AudioFragment";
    private final static String STOP_RECORDING = "Stop Recording";
    private final static String START_RECORDING = "Record";
    private final static String START_PLAYING = "Play";
    private final static String STOP_PLAYING = "Stop Playing";
    private final static String AUDIO_FILE ="/audio_result.3gp";

    // Email variables
    private final static String EMAIL_SUBJECT = "Password Incorrect Pin";
    private final static String EMAIL_BODY = "This is body";

    private MediaRecorder mRecorder = null;

    private boolean mStartRecording = true;

    private boolean mStartPlaying = true;

    private static String mFileName = null;

    private Button mRecord;

    private Button mPlay;

    private MediaPlayer mPlayer = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio, container, false);

        mRecord = (Button) view.findViewById(R.id.button_record);
        mRecord.setOnClickListener(this);

        mPlay = (Button) view.findViewById(R.id.button_play);
        mPlay.setOnClickListener(this);

        createFileName();

        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id){
            case R.id.button_record:
                startRecord();
                break;

            case R.id.button_play:
                startPlay();
                break;

            default:
                break;
        }

    }

    private void startRecord(){
        if (mStartRecording){
            startRecording();
            mRecord.setText(STOP_RECORDING);
        }else{
            stopRecording();
            mRecord.setText(START_RECORDING);

            // Send an Email to receipient
            sendMail(mFileName);

        }

        mStartRecording = !mStartRecording;

    }

    private void startRecording(){
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try{
            mRecorder.prepare();
        }catch (IOException e){
            Log.e(TAG, "startRecording failed");
        }

        mRecorder.start();
    }

    private void stopRecording(){
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }


    private void startPlay(){
        Log.i(TAG, "Start Play: " + mStartPlaying);
        if (mStartPlaying){
            startPlaying();
            mPlay.setText(STOP_PLAYING);

        }else{
            stopPlaying();
            mPlay.setText(START_PLAYING);

        }

        mStartPlaying = !mStartPlaying;
    }

    private void startPlaying(){
        mPlayer = new MediaPlayer();
        try{
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        }catch (IOException e){
            Log.e(TAG, "startPlaying failed");

        }
    }

    private void stopPlaying(){
        mPlayer.release();
        mPlayer = null;
    }

    private void createFileName(){
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += AUDIO_FILE;
        Log.e(TAG, "File Name: " + mFileName);

    }

    @Override
    public void onPause() {
        super.onPause();

        if (mRecorder != null){
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null){
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void sendMail(String fileName){
        new SendMailTask().execute(fileName);
    }

    class SendMailTask extends AsyncTask<String, Void, Boolean> {
        GMailSender sender;

        public SendMailTask() {
            // Use properties to keep save the api keys
            Properties prop = new Properties();

            InputStream input = null;


            String emailSender = null;
            String emailSenderPassword = null;

            try {
                input = getActivity().getBaseContext().getAssets().open("penumbra.properties");

                // load a properties file
                prop.load(input);

                Log.d(TAG, "emailSender properties: " + prop.getProperty("emailSender") + ", emailSenderPassword: " + prop.getProperty("emailSenderPassword"));

                emailSender = prop.getProperty("emailSender");
                emailSenderPassword = prop.getProperty("emailSenderPassword");


            } catch (IOException io) {
                io.printStackTrace();
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

            sender = new GMailSender(emailSender, emailSenderPassword);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Log.i(TAG, "SendMailTask params: " + params[0]);

                // Attach the image of the taken user
                sender.addAttachment(params[0], "Image Attachment");

                // send the email
                sender.sendMail(EMAIL_SUBJECT, EMAIL_BODY, getResources().getString(R.string.app_name), "makspanizales@gmail.com");

            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            getActivity().finish();
        }
    }


}
