package screenAlike;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.projection.MediaProjectionManager;
import android.preference.PreferenceManager;
import com.example.screenalike.R;
public class screenHelper {
    private final Context mContext;
    private final SharedPreferences mSharedPreferences;
    private volatile int mSeverPort;
    private String DEFAULT_SERVER_PORT = "8080";
    private boolean mIsStreaming;
    private static screenHelper sServiceInstance;
    private MediaProjectionManager mMediaProjectionManager;
    private static final int REQUEST_CODE_SCREEN_CAPTURE = 1;
    public Activity activity;
    public screenHelper(final Context context){
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSeverPort = Integer.parseInt(mSharedPreferences.getString(mContext.getString(R.string.pref_key_server_port), DEFAULT_SERVER_PORT));
    }
}
