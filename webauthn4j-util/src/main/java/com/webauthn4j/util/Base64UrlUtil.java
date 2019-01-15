/*
 * Copyright 2002-2018 the original author or authors.
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

package com.webauthn4j.util;

import java.util.Base64;

/**
 * A Utility class for base64url manipulation
 */
public class Base64UrlUtil {

    private static java.util.Base64.Decoder decoder = Base64.getUrlDecoder();
    private static java.util.Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    private Base64UrlUtil() {
    }

    public static byte[] decode(String source) {
        return decoder.decode(source);
    }

    public static byte[] decode(byte[] source) {
        return decoder.decode(source);
    }

    public static byte[] encode(byte[] source) {
        return encoder.encode(source);
    }

    public static String encodeToString(byte[] source) {
        return encoder.encodeToString(source);
    }
}
