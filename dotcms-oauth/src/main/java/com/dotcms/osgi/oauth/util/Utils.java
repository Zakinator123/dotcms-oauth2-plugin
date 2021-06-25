package com.dotcms.osgi.oauth.util;

import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.util.stream.Collectors;

public class Utils {
    public static String getResourceFileAsString(String fileName) throws IOException {
        InputStream inputStream = new ClassPathResource(fileName).getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
}
