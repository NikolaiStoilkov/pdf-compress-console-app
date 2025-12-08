package main.java.org.pdfcompress;

import main.java.org.pdfcompress.classes.PDFReader;
import main.java.org.pdfcompress.classes.TrailLocator;

import java.io.IOException;
import java.io.RandomAccessFile;

public class Main {
    public static void main(String[] args) throws IOException {
        try(RandomAccessFile randomAccessFile = new RandomAccessFile("test-pdf.pdf", "rw")){
            PDFReader reader = new PDFReader(randomAccessFile, 100);
            TrailLocator trailLocator = new TrailLocator(randomAccessFile);

            reader.read();

            trailLocator.locateTrail();

            System.out.println(trailLocator.startXrefOffset);

            byte[] allBytes = PDFReader.bytes;
        } catch (Exception e) {
            System.out.println("Error reading the file" + e);
        }
    }
}



