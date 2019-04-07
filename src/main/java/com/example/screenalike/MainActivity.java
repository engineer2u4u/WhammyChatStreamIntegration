package com.example.screenalike;

import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import screenAlike.ForegroundServiceHandler;
import screenAlike.NotifyImageGenerator;
import screenAlike.screenHelper;
import android.os.Process;

import static com.example.screenalike.ScreenAlike.getAppData;
import static com.example.screenalike.ScreenAlike.getProjectionManager;
import static com.example.screenalike.ScreenAlike.setMediaProjection;


public class MainActivity extends AppCompatActivity {
private screenHelper mscreenHelper;
private static MainActivity sAppInstance;
    private static final int REQUEST_CODE_SETTINGS = 2;
    private static final int REQUEST_CODE_SCREEN_CAPTURE = 1;
    private ForegroundServiceHandler mForegroundServiceTaskHandler;
        private HandlerThread mHandlerThread;
    private boolean buttonClicked = false;
    private NotifyImageGenerator mNotifyImageGenerator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sAppInstance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mscreenHelper = new screenHelper(this);
       TextView myAwesomeTextView = (TextView)findViewById(R.id.editText);
        myAwesomeTextView.setText(getAppData().getServerAddress());
        ToggleButton toggle = (ToggleButton) findViewById(R.id.btn_start_stream);
                mHandlerThread = new HandlerThread(
                ScreenAlike.class.getSimpleName(),
                Process.THREAD_PRIORITY_MORE_FAVORABLE);
        mHandlerThread.start();
        mForegroundServiceTaskHandler = new ForegroundServiceHandler(mHandlerThread.getLooper());
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    buttonClicked=true;
                    final MediaProjectionManager projectionManager = getProjectionManager();
                    if (projectionManager != null) {
                        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_CODE_SCREEN_CAPTURE);
                    }
                } else {
                    mForegroundServiceTaskHandler.obtainMessage(ForegroundServiceHandler.HANDLER_STOP_STREAMING).sendToTarget();
                }
            }
        });
    }
    @Override
    public void onPause()
    {
        super.onPause();
        if(!buttonClicked)
        {
            mForegroundServiceTaskHandler.obtainMessage(ForegroundServiceHandler.HANDLER_STOP_STREAMING).sendToTarget();
            mNotifyImageGenerator = new NotifyImageGenerator(getApplicationContext());
            mNotifyImageGenerator.addDefaultScreen();
            ToggleButton toggle = (ToggleButton) findViewById(R.id.btn_start_stream);
            toggle.setChecked(false);
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SCREEN_CAPTURE:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, getString(R.string.main_activity_toast_cast_permission_deny), Toast.LENGTH_SHORT).show();
                    ToggleButton toggle = (ToggleButton) findViewById(R.id.btn_start_stream);
                    toggle.setChecked(false);
                    return;
                }
                buttonClicked=false;
                final MediaProjectionManager projectionManager = getProjectionManager();
                if (projectionManager == null) return;
                final MediaProjection mediaProjection = projectionManager.getMediaProjection(resultCode, data);
                if (mediaProjection == null) return;
                setMediaProjection(mediaProjection);
                mForegroundServiceTaskHandler.obtainMessage(ForegroundServiceHandler.HANDLER_START_STREAMING).sendToTarget();
                break;
            default:
        }
    }




}
