package autonomous.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;
import android.opengl.GLSurfaceView;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import autonomous.clone.Clone;
import autonomous.clone.Config;
import autonomous.clone.VideoSession;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks,
        Session.SessionListener,
        Publisher.PublisherListener,
        Subscriber.VideoListener {

    private static final String TAG = "tablet-app " + MainActivity.class.getSimpleName();

    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private RelativeLayout mPublisherViewContainer;
    private LinearLayout mSubscriberViewContainer;
    private Clone clone;
    private VideoSession videoSession;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPublisherViewContainer = (RelativeLayout) findViewById(R.id.publisherview);
        mSubscriberViewContainer = (LinearLayout) findViewById(R.id.subscriberview);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        requestPermissions();

        clone = new Clone(Config.PRODUCT_ID);
        videoSession = new VideoSession();
        
        
    }

    private void startSessionVideo(){

        mSession = new Session(MainActivity.this, Config.API_KEY, clone.session.sessionId);
        mSession.setSessionListener(this);
        mSession.connect(clone.session.token);

    }
    private void initSession(){


        mDatabase.child("sessions/"+Config.PRODUCT_ID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                videoSession = dataSnapshot.getValue(VideoSession.class);
                Log.d("DATA", videoSession.sessionId);
                Log.d("DATA", videoSession.token);
                if(videoSession.sessionId ==""){
                    requestNewSession();
                }else{
                    clone.session = videoSession;
                    startSessionVideo();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void requestNewSession(){

        AppController app = AppController.getInstance();

        final String PRODUCT_UPDATE_URL = Config.HOST + "/session/create";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, PRODUCT_UPDATE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);

                            Log.d("DATA", response);

                            videoSession.sessionId = obj.getString("session_id");
                            videoSession.token = obj.getString("token");

                            clone.setSession(videoSession);
                            startSessionVideo();

                        } catch (JSONException e) {

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("ERROR", error.toString());
                    }
                });
        app.addToRequestQueue(stringRequest);

    }


    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");

        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");

        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");

        super.onResume();

        if (mSession == null) {
            return;
        }
        mSession.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();

        if (mSession == null) {
            return;
        }
        mSession.onPause();

        if (isFinishing()) {
            disconnectSession();
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onPause");

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");

        disconnectSession();

        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this, getString(R.string.rationale_ask_again))
                    .setTitle(getString(R.string.title_settings_dialog))
                    .setPositiveButton(getString(R.string.setting))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .setRequestCode(RC_SETTINGS_SCREEN_PERM)
                    .build()
                    .show();
        }
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {
        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE };
        if (EasyPermissions.hasPermissions(this, perms)) {

            //All Start From Here

            initSession();


        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_video_app), RC_VIDEO_APP_PERM, perms);
        }
    }
    @Override
    public void onConnected(Session session) {
        Log.d(TAG, "onConnected: Connected to session " + session.getSessionId());

        mPublisher = new Publisher.Builder(MainActivity.this).name("publisher").build();
        mPublisher.setPublisherListener(this);
        mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);

        mPublisherViewContainer.addView(mPublisher.getView());
        if (mPublisher.getView() instanceof GLSurfaceView) {
            ((GLSurfaceView)(mPublisher.getView())).setZOrderOnTop(true);
        }

        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.d(TAG, "onDisconnected: disconnected from session " + session.getSessionId());

        mSession = null;
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.d(TAG, "onError: Error (" + opentokError.getMessage() + ") in session " + session.getSessionId());

        Toast.makeText(this, "Session error. See the logcat please.", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.d(TAG, "onStreamReceived: New stream " + stream.getStreamId() + " in session " + session.getSessionId());

        if (Config.SUBSCRIBE_TO_SELF) {
            return;
        }
        if (mSubscriber != null) {
            return;
        }

        subscribeToStream(stream);
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.d(TAG, "onStreamDropped: Stream " + stream.getStreamId() + " dropped from session " + session.getSessionId());

        if (Config.SUBSCRIBE_TO_SELF) {
            return;
        }
        if (mSubscriber == null) {
            return;
        }

        if (mSubscriber.getStream().equals(stream)) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());
            mSubscriber.destroy();
            mSubscriber = null;
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.d(TAG, "onStreamCreated: Own stream " + stream.getStreamId() + " created");

        if (!Config.SUBSCRIBE_TO_SELF) {
            return;
        }

        subscribeToStream(stream);
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.d(TAG, "onStreamDestroyed: Own stream " + stream.getStreamId() + " destroyed");
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.d(TAG, "onError: Error (" + opentokError.getMessage() + ") in publisher");

        Toast.makeText(this, "Session error. See the logcat please.", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onVideoDataReceived(SubscriberKit subscriberKit) {
        mSubscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
        mSubscriberViewContainer.addView(mSubscriber.getView());
    }

    @Override
    public void onVideoDisabled(SubscriberKit subscriberKit, String s) {

    }

    @Override
    public void onVideoEnabled(SubscriberKit subscriberKit, String s) {

    }

    @Override
    public void onVideoDisableWarning(SubscriberKit subscriberKit) {

    }

    @Override
    public void onVideoDisableWarningLifted(SubscriberKit subscriberKit) {

    }

    private void subscribeToStream(Stream stream) {
        mSubscriber = new Subscriber(MainActivity.this, stream);
        mSubscriber.setVideoListener(this);
        mSession.subscribe(mSubscriber);
    }

    private void disconnectSession() {
        if (mSession == null) {
            return;
        }

        if (mSubscriber != null) {
            mSubscriberViewContainer.removeView(mSubscriber.getView());
            mSession.unsubscribe(mSubscriber);
            mSubscriber.destroy();
            mSubscriber = null;
        }

        if (mPublisher != null) {
            mPublisherViewContainer.removeView(mPublisher.getView());
            mSession.unpublish(mPublisher);
            mPublisher.destroy();
            mPublisher = null;
        }
        mSession.disconnect();
    }
}
