package main.java.org.pdfcompress;

import main.java.org.pdfcompress.repository.PDFReader;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Stack;

public class Main {
    public static void main(String[] args) throws IOException {
        try(RandomAccessFile randomAccessFile = new RandomAccessFile("test-pdf.pdf", "rw")){
            PDFReader reader = new PDFReader(randomAccessFile, 100);

            reader.read();

            byte[] allBytes = PDFReader.bytes;
        } catch (Exception e) {
            System.out.println("Error reading the file" + e);
        }
    }
}



class Compressor {

}