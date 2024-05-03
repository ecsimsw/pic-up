package ecsimsw.picup.album.utils;

import ecsimsw.picup.album.exception.AlbumException;

import java.text.DecimalFormat;

public class FileUtils {

    public static String fileStringSize(long size) {
        var df = new DecimalFormat("0.00");
        var sizeKb = 1024.0f;
        var sizeMb = sizeKb * sizeKb;
        var sizeGb = sizeMb * sizeKb;
        var sizeTerra = sizeGb * sizeKb;
        if (size < sizeMb) {
            return df.format(size / sizeKb) + " Kb";
        } else if (size < sizeGb) {
            return df.format(size / sizeMb) + " Mb";
        } else if (size < sizeTerra) {
            return df.format(size / sizeGb) + " Gb";
        }
        return size + "b";
    }

    public static String getExtensionFromName(String fileName) {
        if(fileName == null || fileName.isBlank()) {
            throw new AlbumException("Invalid file name");
        }
        var indexOfExtension = fileName.lastIndexOf(".");
        return fileName.substring(indexOfExtension + 1);
    }
}
