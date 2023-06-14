package com.arthur.authz.util;

import org.apache.commons.lang3.StringUtils;

public class MaskUtil {

    private MaskUtil() {}

    public static String mask(String value) {
        if (StringUtils.isEmpty(value) || value.length() <= 8) {
            return value;
        }
        return new StringBuilder(value)
                .replace(4, value.length() - 4, new String(new char[value.length() - 4])
                        .replace("\0", "x")).toString();
    }
}
