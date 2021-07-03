/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

package an.kit.authentica;

import android.annotation.SuppressLint;
import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;


/*
  Wraps SecretKey instances using a public/private key pair stored in
  the platform KeyStore. This allows us to protect symmetric keys with
  hardware-backed crypto, if provided by the device.
 */
public class SecretKeyWrapper {
    private final Cipher mCipher;
    private final KeyPair mPair;

    /*
     * Create a wrapper using the public/private key pair with the given alias.
     * If no pair with that alias exists, it will be generated.
     */
    @SuppressLint("GetInstance")
    public SecretKeyWrapper(Context context, String alias)
            throws GeneralSecurityException, IOException {
        
        // generate blank RSA Key
        mCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        
        // add it to the keystore
        // get instance of the keyStore        
        final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");

        // load null into the keystore
        keyStore.load(null);
        
        // if entry doesn't exist, generate a key pair for the entry
        if (!keyStore.containsAlias(alias)) {
            generateKeyPair(alias);
        }

        // Even if we just generated the key, always read it back to ensure we
        // can read it successfully.
        final KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);

        // store pair in mPair variable
        mPair = new KeyPair(entry.getCertificate().getPublicKey(), entry.getPrivateKey());
    }

    private static void generateKeyPair(String alias)
            throws GeneralSecurityException {
        final Calendar start = new GregorianCalendar();
        final Calendar end = new GregorianCalendar();
        end.add(Calendar.YEAR, 100);
        
        // define specs for the KeyPairGenerator for RSA encryption of SecretKey, Store Private Key in AndroidKeyStore
        final KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
        generator.initialize(
                new KeyGenParameterSpec.Builder(alias,KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setDigests(KeyProperties.DIGEST_SHA512)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                        .build());
        
        // generate RSA ( with SHA 512 digest ) keypair to store QR obtained value
        generator.generateKeyPair();
    }

    /*
      Wrap a SecretKey using the public key assigned to this wrapper.
      Use unwrap(byte[]) to later recover the original
     */
    public byte[] wrap(SecretKey key) throws GeneralSecurityException {
        mCipher.init(Cipher.WRAP_MODE, mPair.getPublic());
        return mCipher.wrap(key);
    }

    /*
      Unwrap a SecretKey using the private key assigned to this
      wrapper wrapped by wrap(SecretKey)
     */
    public SecretKey unwrap(byte[] blob) throws GeneralSecurityException {
        mCipher.init(Cipher.UNWRAP_MODE, mPair.getPrivate());

        return (SecretKey) mCipher.unwrap(blob, "AES", Cipher.SECRET_KEY);
    }
}
