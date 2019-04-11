package screenAlike;

import android.os.Bundle;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import com.github.nkzawa.socketio.client.IO;

import static com.example.screenalike.ScreenAlike.getAppData;

final class ImageDispatcher {
    private final Object mLock = new Object();
    private JpegStreamerThread mJpegStreamerThread;
    private volatile boolean isThreadRunning;

    private class JpegStreamerThread extends Thread {
        private byte[] mCurrentJpeg;
        private byte[] mLastJpeg;
        private int mSleepCount;

        JpegStreamerThread() {
            super(JpegStreamerThread.class.getSimpleName());
        }

        public void run() {

            while (!isInterrupted()) {
                if (!isThreadRunning) break;
                mCurrentJpeg = getAppData().getImageQueue().poll();
                if (mCurrentJpeg == null) {
                    try {
                        sleep(24);
                    } catch (InterruptedException ignore) {
                        continue;
                    }
                    mSleepCount++;
                    if (mSleepCount >= 20) sendLastJPEGToClients();
                } else {
                    mLastJpeg = mCurrentJpeg;
                    getAppData().getSocketConnection().emit("imageStream", getAppData().getTwitchID(),mLastJpeg);
                }
                }
            }
        }

        private void sendLastJPEGToClients() {
//            mSleepCount = 0;
//            synchronized (mLock) {
//                if (!isThreadRunning) return;
//                for (final Client currentClient : getAppData().getClientQueue()) {
//                    currentClient.sendClientData(Client.CLIENT_IMAGE, mLastJpeg, false);
//                }
//            }
//        }
    }

    void addClient(final Socket clientSocket) {
        synchronized (mLock) {
            if (!isThreadRunning) return;
            try {
                final Client newClient = new Client(clientSocket);
                newClient.sendClientData(Client.CLIENT_HEADER, null, false);
                getAppData().getClientQueue().add(newClient);
                getAppData().setClients(getAppData().getClientQueue().size());
            } catch (IOException e) {
            }
        }
    }

    void start() {
        synchronized (mLock) {
            if (isThreadRunning) return;
            mJpegStreamerThread = new JpegStreamerThread();
            mJpegStreamerThread.start();
            isThreadRunning = true;
        }
    }

    void stop(final byte[] clientNotifyImage) {
        synchronized (mLock) {
            if (!isThreadRunning) return;
            isThreadRunning = false;
            mJpegStreamerThread.interrupt();

            for (Client currentClient : getAppData().getClientQueue()) {
                currentClient.sendClientData(Client.CLIENT_IMAGE, clientNotifyImage, true);
            }

            getAppData().getClientQueue().clear();
            getAppData().setClients(0);
        }
    }
}