package main.java.org.pdfcompress.classes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class PDFReader {
    private final int DEFAULT_CHUNK_SIZE = 4096;
    private RandomAccessFile randomAccessFile;
    private int length;
    private final byte[] buffer = new byte[length];
    private ByteArrayOutputStream outputStream;
    private int bytesRead;
    public static byte[] bytes;

    public PDFReader(){}

    public PDFReader(RandomAccessFile randomAccessFile){
        this.randomAccessFile = randomAccessFile;
        this.length = DEFAULT_CHUNK_SIZE;
    }

    public PDFReader(RandomAccessFile randomAccessFile, int length){
        this.randomAccessFile = randomAccessFile;
        this.length = length;
    }

    public void read() throws IOException {
        outputStream = new ByteArrayOutputStream();

        readChunk();

        while(bytesRead != -1){
            outputStream.write(buffer, 0 , bytesRead);

            readChunk();
        }

        bytes = outputStream.toByteArray();

        reverse(bytes);
    }

    private void readChunk() throws IOException {
        bytesRead = randomAccessFile.read(buffer);
    }

    private void reverse(byte[] array) {
        if (array == null) return;

        int i = 0;
        int j = array.length - 1;
        byte temp;

        // Move from both ends towards the middle
        while (i < j) {
            // Swap
            temp = array[i];
            array[i] = array[j];
            array[j] = temp;

            i++;
            j--;
        }
    }
}