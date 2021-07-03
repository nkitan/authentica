package an.kit.authentica;

import android.app.Application;

// entry point
public class MyApplication extends Application {
    @Override   // annotation prevents mis-spelled function names and improves readability
    public void onCreate() {
        super.onCreate();   // tell Dalvik VM to run code alongside UI drawing
    }
}
