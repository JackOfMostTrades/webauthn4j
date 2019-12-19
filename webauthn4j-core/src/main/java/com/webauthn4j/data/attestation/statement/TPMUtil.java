/*
 * Copyright 2018 the original author or authors.
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

package com.webauthn4j.data.attestation.statement;

import com.webauthn4j.converter.exception.DataConversionException;
import com.webauthn4j.util.UnsignedNumberUtil;

import java.io.IOException;
import java.io.OutputStream;

class TPMUtil {

    private TPMUtil() {
    }

    static void writeSizedArray(OutputStream stream, byte[] value) throws IOException {
        if (value.length > UnsignedNumberUtil.UNSIGNED_SHORT_MAX) {
            throw new DataConversionException("too large data to write");
        }
        stream.write(UnsignedNumberUtil.toBytes(value.length));
        stream.write(value);
    }
}
