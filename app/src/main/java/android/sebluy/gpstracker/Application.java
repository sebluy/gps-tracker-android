package android.sebluy.gpstracker;

import android.content.Context;

public class Application extends android.app.Application {

    private static Application sInstance;

    public static Context getContext() {
        return sInstance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

}
