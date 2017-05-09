package autonomous.clone.mobile_remote.sdk;

/**
 * Created by autonomous on 5/4/17.
 */

import android.util.Log;

import com.google.firebase.database.DatabaseReference;

public class Move {

    public static void execute(DatabaseReference mDatabase, String Product_id, String value){

        String package_data = "{\"source\":\"WebApp\",\"type\":\"clone_control\",\"data\":\"{\\\"action\\\":\\\"MOVE\\\",\\\"name\\\":\\\"" + value + "\\\"}\"}";
        if(mDatabase!=null){
            mDatabase.child(Product_id).push().setValue(package_data);
        }else {
            Log.d("Move"," MDatabase is Null");
        }

    }
}
