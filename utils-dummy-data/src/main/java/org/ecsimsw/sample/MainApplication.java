package org.giggles.sample;

import org.giggles.utils.DataFileInfo;
import org.giggles.utils.MockDataFile;

import java.io.IOException;

public class MainApplication {

    public static void main(String[] args) throws IOException {
        var userData = generateUserData();
        printFileInfo(userData);

        var albumData = generateAlbumData(userData);
        printFileInfo(albumData);

        var pictureData = generatePictureData(albumData);
        printFileInfo(pictureData);
    }

    private static DataFileInfo generateUserData() throws IOException {
        return MockDataFile.generate(
            "user-data.txt",
            new UserRowFormat(),
            1_000
        );
    }

    private static DataFileInfo generateAlbumData(DataFileInfo userData) throws IOException {
        return MockDataFile.generate(
            "album-data.txt",
            new AlbumRowFormat(1L, userData.dataCount()),
            20_000
        );
    }

    private static DataFileInfo generatePictureData(DataFileInfo albumData) throws IOException {
        return MockDataFile.generate(
            "picture-data.txt",
            new PictureRowFormat(1L, albumData.dataCount()),
            10_000_000
        );
    }

    private static void printFileInfo(DataFileInfo response) {
        System.out.println("File name : " + response.fileName());
        System.out.println("Total generated rows : " + response.dataCount());
        System.out.println("Total File size : " + response.fileSizeAsMB() + " MB");
        System.out.println("Execution Time : " + response.totalTimeAsSec() + " SEC");
    }
}
