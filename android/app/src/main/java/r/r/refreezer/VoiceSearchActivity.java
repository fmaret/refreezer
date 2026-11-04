package r.r.refreezer;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Entry point for voice commands such as "play X on Flowzer".
 *
 * Google Assistant only offers an app as a target when it declares an activity
 * for android.media.action.MEDIA_PLAY_FROM_SEARCH; otherwise it falls back to
 * its default music service. This invisible activity connects to the media
 * session of the audio service and forwards the query to playFromSearch(),
 * which the Dart AudioPlayerHandler handles.
 */
public class VoiceSearchActivity extends Activity {
    private static final String TAG = "VoiceSearch";
    private static final String AUDIO_SERVICE = "com.ryanheise.audioservice.AudioService";

    private MediaBrowser browser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        final String query = intent != null ? intent.getStringExtra(SearchManager.QUERY) : null;
        final Bundle extras = (intent != null && intent.getExtras() != null)
                ? new Bundle(intent.getExtras()) : new Bundle();
        Log.i(TAG, "Voice search request: \"" + query + "\"");

        browser = new MediaBrowser(this,
                new ComponentName(getPackageName(), AUDIO_SERVICE),
                new MediaBrowser.ConnectionCallback() {
                    @Override
                    public void onConnected() {
                        try {
                            MediaController controller = new MediaController(
                                    VoiceSearchActivity.this, browser.getSessionToken());
                            controller.getTransportControls()
                                    .playFromSearch(query == null ? "" : query, extras);
                        } catch (Exception e) {
                            Log.w(TAG, "Could not forward voice search: " + e.getMessage());
                        }
                        // Give the command time to be delivered before closing
                        new Handler(Looper.getMainLooper()).postDelayed(
                                VoiceSearchActivity.this::finish, 500);
                    }

                    @Override
                    public void onConnectionFailed() {
                        Log.w(TAG, "Could not connect to the audio service");
                        finish();
                    }
                }, null);
        browser.connect();
    }

    @Override
    protected void onDestroy() {
        if (browser != null && browser.isConnected()) browser.disconnect();
        super.onDestroy();
    }
}
