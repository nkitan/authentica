package an.kit.authentica;

import android.net.Uri;

import org.apache.commons.codec.binary.Base32;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

public class Entry {
    public static final String JSON_SECRET = "secret";
    public static final String JSON_LABEL = "label";

    private byte[] secret;
    private String label;
    private String currentOTP;

    public Entry (){
        // empty function, placeholder for when Entry is called without parameters, stops app from crashing upon exception
    }

    public Entry(String contents) throws Exception {
        contents = contents.replaceFirst("otpauth", "http");
        Uri uri = Uri.parse(contents);
        URL url = new URL(contents);

        if(!url.getProtocol().equals("http")){
            throw new Exception("Invalid Protocol");
        }

        if(!url.getHost().equals("totp")){
            throw new Exception("Invalid OTP Protocol");
        }

        String secret = uri.getQueryParameter("secret");
        String label = uri.getPath().substring(1);
        label = label.replace(":"," : ");
        String issuer = uri.getQueryParameter("issuer");

        if(!label.contains(issuer)){
            label = issuer + " : " + label;
        }

        this.label = label;
        this.secret = new Base32().decode(secret.toUpperCase(Locale.getDefault()));
    }

    public Entry (JSONObject jsonObj ) throws JSONException {
        this.setSecret(new Base32().decode(jsonObj.getString(JSON_SECRET)));
        this.setLabel(jsonObj.getString(JSON_LABEL));
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put(JSON_SECRET, new String(new Base32().encode(getSecret())));
        jsonObj.put(JSON_LABEL, getLabel());

        return jsonObj;
    }

    public byte[] getSecret() {
        return secret;
    }

    public void setSecret(byte[] secret) {
        this.secret = secret;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getCurrentOTP() {
        return currentOTP;
    }

    public void setCurrentOTP(String currentOTP) {
        this.currentOTP = currentOTP;
    }

    @Override
    public boolean equals(Object otpee) {
        if (this == otpee) return true;
        if (otpee == null || getClass() != otpee.getClass()) return false;

        Entry entry = (Entry) otpee;

        if (!Arrays.equals(secret, entry.secret)) return false;
        return !(label != null ? !label.equals(entry.label) : entry.label != null);

    }

    @Override
    public int hashCode() {
        int result = (secret != null ? Arrays.hashCode(secret) : 0);  // if secret is not null, return hashcode of the secret otherwise return 0
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }
}
