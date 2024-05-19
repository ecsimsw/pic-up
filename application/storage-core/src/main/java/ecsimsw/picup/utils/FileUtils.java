package ecsimsw.picup.utils;

import ecsimsw.picup.exception.StorageException;

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
        if (fileName == null || fileName.isBlank()) {
            throw new StorageException("Invalid file name");
        }
        var indexOfExtension = fileName.lastIndexOf(".");
        return fileName.substring(indexOfExtension + 1);
    }

    public static String changeExtensionTo(String originFileName, String newExtension) {
        var extension = getExtensionFromName(originFileName);
        var sb = new StringBuilder(originFileName);
        var extensionIndex = originFileName.lastIndexOf(extension);
        sb.replace(extensionIndex, originFileName.length() + extensionIndex, newExtension);
        return sb.toString();
    }
}
