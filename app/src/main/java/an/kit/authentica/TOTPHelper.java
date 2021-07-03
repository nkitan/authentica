package an.kit.authentica;

/*
MIT License

Copyright (c) 2021 Ankit Das

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/



import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TOTPHelper {
    public static final String SHA1 = "HmacSHA1";
    public static final String SHA256 = "HmacSHA256";
    public static final String SHA512 = "HmacSHA512";

    private static byte[] generateHash(byte[] key, byte[] data, String algorithm)
            throws NoSuchAlgorithmException, InvalidKeyException {

        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key, algorithm));

        return mac.doFinal(data);
    }

    private static int HOTP(byte[] key, long counter, String algorithm)
    {
        int result = 0;

        try {
            byte[] data = ByteBuffer.allocate(8).putLong(counter).array();
            byte[] hash = generateHash(key, data, algorithm);

            int offset = hash[hash.length - 1] & 0xF;

            int binary = (hash[offset] & 0x7F) << 0x18;
            binary |= (hash[offset + 1] & 0xFF) << 0x10;
            binary |= (hash[offset + 2] & 0xFF) << 0x08;
            binary |= (hash[offset + 3] & 0xFF);

            result = binary;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    // function to generate TOTP as integer
    private static int TOTP(byte[] key, long time, int returnDigits, int period, String algorithm)
    {
        int result = 0;
        try {
            time /= period;
            result = HOTP(key, time, algorithm);
            int div = (int) Math.pow(10, returnDigits);     // use power's of 10 to get required number of return digits (6 in this case)

            result = result % div;
        }
        catch(Exception excep){
            //Log.e("ERROR IN TOTP HELPER: ", excep.getMessage());
        }

        return result;
    }

    public static String generate(byte[] secret) {
        // generate 6 bit long integer secret using current time and return as string
        return String.format(Locale.getDefault(),"%06d", TOTP(secret, System.currentTimeMillis() / 1000, 6 , 30, SHA1));
    }

}




