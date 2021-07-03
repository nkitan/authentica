package an.kit.authentica;

import android.content.Context;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;

import javax.crypto.SecretKey;

import static an.kit.authentica.Utils.readFully;
import static an.kit.authentica.Utils.writeFully;

public class SettingsHelper {
    public static final String KEY_FILE = "otp.key";            // file to store RSA Wrapped SecretKey for Long Term Storage
    public static final String SETTINGS_FILE = "secrets.dat";   // AES Encrypted SecretKey for Runtime Usage

    /* 
        function to store entry in settings file
    */
    public static void store(Context context, ArrayList<Entry> entries){
        JSONArray a = new JSONArray();

        for(Entry e: entries){
            try {
                a.put(e.toJSON());
            } catch (JSONException e1) {
            }
        }

        try {
            byte[] data = a.toString().getBytes();

            int currentApiVersion = android.os.Build.VERSION.SDK_INT;
            if (currentApiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                SecretKey key = EncryptionHelper.loadOrGenerateKeys(context, new File(context.getFilesDir() + "/" + KEY_FILE));
                data = EncryptionHelper.encrypt(key,data);
            }

            writeFully(new File(context.getFilesDir() + "/" + SETTINGS_FILE), data);

        } catch (Exception e) {
        }

    }

    /*
        function to load entries from secrets.dat   
    */
    public static ArrayList<Entry> load(Context context){
        ArrayList<Entry> entries = new ArrayList<>();

        try {
            byte[] data = readFully(new File(context.getFilesDir() + "/" + SETTINGS_FILE));

            int currentApiVersion = android.os.Build.VERSION.SDK_INT;
            if (currentApiVersion >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                SecretKey key = EncryptionHelper.loadOrGenerateKeys(context, new File(context.getFilesDir() + "/" + KEY_FILE));
                data = EncryptionHelper.decrypt(key, data);
            }
            JSONArray a = new JSONArray(new String(data));

            for(int i=0;i< a.length(); i++){
                entries.add(new Entry(a.getJSONObject(i) ));
            }
        }
        catch (Exception e) {
        }
        return entries;
    }
}
