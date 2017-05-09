package autonomous.clone.mobile_remote;

import android.*;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;

import java.util.List;

import autonomous.clone.mobile_remote.sdk.CustomVideoCapturer;
import autonomous.clone.mobile_remote.sdk.Move;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;
import autonomous.clone.mobile_remote.sdk.Config;

public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks,
        Session.SessionListener,
        Publisher.PublisherListener,
        Subscriber.VideoListener {

    private static final String TAG = "mobile-remote " + MainActivity.class.getSimpleName();

    private static final int RC_SETTINGS_SCREEN_PERM = 123;
    private static final int RC_VIDEO_APP_PERM = 124;

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private RelativeLayout mPublisherViewContainer;
    private RelativeLayout mSubscriberViewContainer;

    protected LinearLayout viewLltListActions;

    // Spinning wheel for loading subscriber view
    private ProgressBar mLoadingSub;

    private DatabaseReference mDatabase;

    //Control Move
    private RadioGroup viewControlParent;
    protected RadioButton viewDirectionLeft, viewDirectionRight, viewDirectionBottom, viewDirectionTop, viewDirectionStop;
    private CompoundButton currentActionView;

    private CustomVideoCapturer customVideoCapturer;

    private final CompoundButton.OnCheckedChangeListener onChangeDirection = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                Log.d(TAG, " onChangeDirection OK");
                String new_command = "";
                switch (buttonView.getId()) {
                    case R.id.btn_top:
                        new_command = Config.FORWARD;
                        break;
                    case R.id.btn_bottom:
                        new_command = Config.BACKWARD;
                        break;
                    case R.id.btn_left:
                        new_command = Config.LEFT;
                        break;
                    case R.id.btn_right:
                        Log.d(TAG, " onCheckedChanged camera");
                        new_command = Config.RIGHT;
                        break;
                    case R.id.btn_stop_action:
                        new_command = Config.STOP;

                        break;
                }
                if(currentActionView !=null){
                    currentActionView.setChecked(false);
                }
                currentActionView = buttonView;
                if (!TextUtils.isEmpty(new_command)) {

                    if (new_command.compareTo(Config.STOP) != 0) {
                        //String package_data = "{\"source\":\"WebApp\",\"type\":\"clone_control\",\"data\":\"{\\\"action\\\":\\\"MOVE\\\",\\\"name\\\":\\\"" + new_command + "\\\"}\"}";
                        //mDatabase.child(Config.PRODUCT_ID).push().setValue(package_data);
                        Move.execute(mDatabase, Config.PRODUCT_ID, new_command);
                    } else {
                        if(currentActionView != null){
                            currentActionView.setChecked(false);
                        }
                        //String package_data = "{\"source\":\"WebApp\",\"type\":\"clone_control\",\"data\":\"{\\\"action\\\":\\\"MOVE\\\",\\\"name\\\":\\\"STOP\\\"}\"}";
                        //mDatabase.child(Config.PRODUCT_ID).push().setValue(package_data);

                        Move.execute(mDatabase, Config.PRODUCT_ID, Config.STOP);
                    }
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPublisher = new Publisher(getApplicationContext(), "publisher");
        mPublisher.setPublisherListener(this);
        // use an external customer video capturer
        customVideoCapturer = new CustomVideoCapturer(getApplicationContext());
        mPublisher.setCapturer(customVideoCapturer);
        
        mPublisherViewContainer = (RelativeLayout) findViewById(R.id.publisherview);
        mSubscriberViewContainer = (RelativeLayout) findViewById(R.id.subscriberview);

        viewLltListActions = (LinearLayout) findViewById(R.id.layout_video_capture_llt_action);
        viewControlParent = (RadioGroup)  findViewById(R.id.control_parent);

        mLoadingSub = (ProgressBar) findViewById(R.id.loadingSpinner);



        viewDirectionTop = (RadioButton)  findViewById(R.id.btn_top);
        viewDirectionLeft = (RadioButton)  findViewById(R.id.btn_left);
        viewDirectionBottom = (RadioButton) findViewById(R.id.btn_bottom);
        viewDirectionRight = (RadioButton)  findViewById(R.id.btn_right);
        viewDirectionStop = (RadioButton) findViewById(R.id.btn_stop_action);

        viewDirectionTop.setOnCheckedChangeListener(onChangeDirection);
        viewDirectionBottom.setOnCheckedChangeListener(onChangeDirection);
        viewDirectionLeft.setOnCheckedChangeListener(onChangeDirection);
        viewDirectionRight.setOnCheckedChangeListener(onChangeDirection);
        viewDirectionStop.setOnCheckedChangeListener(onChangeDirection);


        firebaseAuthLogin();

        requestPermissions();
    }


    private void firebaseAuthLogin() {

        try {

            final FirebaseAuth auth = FirebaseAuth.getInstance();
            //Login
            auth.signInWithEmailAndPassword(Config.FBASE_EMAIL, Config.FBASE_PASSWORD)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(Task task) {
                            mDatabase = FirebaseDatabase.getInstance().getReference();

                        }
                    });

        } catch (Exception e) {

        }

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
        String[] perms = { android.Manifest.permission.INTERNET, android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO };
        if (EasyPermissions.hasPermissions(this, perms)) {
            mSession = new Session(MainActivity.this, Config.API_KEY, Config.SESSION_ID);
            mSession.setSessionListener(this);
            mSession.connect(Config.TOKEN);
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_video_app), RC_VIDEO_APP_PERM, perms);
        }
    }


    private void attachSubscriberView(Subscriber subscriber) {
        if (subscriber != null) {
            if (mSubscriberViewContainer != null && mSubscriberViewContainer.indexOfChild(mSubscriber.getView()) == -1) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                mSubscriberViewContainer.addView(mSubscriber.getView(), layoutParams);

                subscriber.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                        BaseVideoRenderer.STYLE_VIDEO_FILL);
            }
        }

    }

    private void attachPublisherView(Publisher publisher) {
        if (publisher != null) {
            publisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
                    BaseVideoRenderer.STYLE_VIDEO_FILL);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    320, 240);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
                    RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
                    RelativeLayout.TRUE);

            layoutParams.topMargin = dpToPx(getApplicationContext(), 20);
            layoutParams.rightMargin = dpToPx(getApplicationContext(), 20);

            mPublisherViewContainer.addView(publisher.getView(), layoutParams);
        }
    }


    @Override
    public void onConnected(Session session) {
        Log.d(TAG, "onConnected: Connected to session " + session.getSessionId());
//
//        mPublisher = new Publisher.Builder(MainActivity.this).name("publisher").build();
//        mPublisher.setPublisherListener(this);
//        mPublisher.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL);
//
//        mPublisherViewContainer.addView(mPublisher.getView());
//        if (mPublisher.getView() instanceof GLSurfaceView) {
//            ((GLSurfaceView)(mPublisher.getView())).setZOrderOnTop(true);
//        }
//
//        mSession.publish(mPublisher);

//
        if (mPublisher != null) {
            attachPublisherView(mPublisher);
            session.publish(mPublisher);

        }

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

        viewLltListActions.setVisibility(View.VISIBLE);
        // stop loading spinning
        mLoadingSub.setVisibility(View.GONE);
        viewControlParent.setVisibility(View.VISIBLE);

        attachSubscriberView(mSubscriber);

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

    private int dpToPx(Context context, int dp) {
        double screenDensity = context.getResources().getDisplayMetrics().density;
        return (int) (screenDensity * (double) dp);
    }
}
