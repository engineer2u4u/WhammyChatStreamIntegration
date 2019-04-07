package screenAlike;

import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import static android.view.Surface.ROTATION_0;
import static android.view.Surface.ROTATION_180;
import static com.example.screenalike.ScreenAlike.getAppData;
import static com.example.screenalike.ScreenAlike.getImageGenerator;
import static com.example.screenalike.ScreenAlike.getMediaProjection;



public final class ForegroundServiceHandler extends Handler {
    public static final int HANDLER_START_STREAMING = 0;
    public static final int HANDLER_STOP_STREAMING = 1;

    public static final int HANDLER_PAUSE_STREAMING = 10;
    public static final int HANDLER_RESUME_STREAMING = 11;
    public static final int HANDLER_DETECT_ROTATION = 20;

    public boolean mCurrentOrientation;

    public ForegroundServiceHandler(final Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message message) {
        ImageGenerator imageGenerator;

        switch (message.what) {
            case HANDLER_START_STREAMING:
                if (getAppData().isStreamRunning()) break;
                removeMessages(HANDLER_DETECT_ROTATION);
                mCurrentOrientation = getOrientation();
                imageGenerator = getImageGenerator();
                if (imageGenerator != null) imageGenerator.start();
                sendMessageDelayed(obtainMessage(HANDLER_DETECT_ROTATION), 250);
                getAppData().setStreamRunning(true);
                break;
            case HANDLER_PAUSE_STREAMING:
                if (!getAppData().isStreamRunning()) break;
                imageGenerator = getImageGenerator();
                if (imageGenerator != null) imageGenerator.stop();
                sendMessageDelayed(obtainMessage(HANDLER_RESUME_STREAMING), 250);
                break;
            case HANDLER_RESUME_STREAMING:
                if (!getAppData().isStreamRunning()) break;
                imageGenerator = getImageGenerator();
                if (imageGenerator != null) imageGenerator.start();
                sendMessageDelayed(obtainMessage(HANDLER_DETECT_ROTATION), 250);
                break;
            case HANDLER_STOP_STREAMING:
                removeMessages(HANDLER_DETECT_ROTATION);
                removeMessages(HANDLER_STOP_STREAMING);
                imageGenerator = getImageGenerator();
                if (imageGenerator != null) imageGenerator.stop();
                final MediaProjection mediaProjection = getMediaProjection();
                if (mediaProjection != null) mediaProjection.stop();
                getAppData().setStreamRunning(false);
                break;
            case HANDLER_DETECT_ROTATION:
                if (!getAppData().isStreamRunning()) break;
                boolean newOrientation = getOrientation();
                if (mCurrentOrientation == newOrientation) {
                    sendMessageDelayed(obtainMessage(HANDLER_DETECT_ROTATION), 250);
                    break;
                }
                mCurrentOrientation = newOrientation;
                obtainMessage(HANDLER_PAUSE_STREAMING).sendToTarget();
                break;
            default:
        }
    }

    private boolean getOrientation() {
        final int rotation = getAppData().getWindowsManager().getDefaultDisplay().getRotation();
        return rotation == ROTATION_0 || rotation == ROTATION_180;
    }
}