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

package an.kit.authentica;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class Utils {
    public static void writeFully(File file, byte[] data) throws IOException {
        final OutputStream outStream = new FileOutputStream(file);
        try {
            outStream.write(data);
        } finally {
            outStream.close();
        }
    }

    public static byte[] readFully(File file) throws IOException {
        final InputStream inStream = new FileInputStream(file);
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = ByteBuffer.allocate(1024).array();
            int count;
            while ((count = inStream.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        } finally {
            inStream.close();
        }
    }
}
