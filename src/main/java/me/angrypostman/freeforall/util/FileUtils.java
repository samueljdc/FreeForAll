package me.angrypostman.freeforall.util;

import java.io.File;

public class FileUtils {

    public static String getFileExtension(File file) {
        String absolutePath = file.getAbsolutePath();
        int lastIndexOf = absolutePath.lastIndexOf(".");
        return absolutePath.substring(lastIndexOf + 1);
    }

}
