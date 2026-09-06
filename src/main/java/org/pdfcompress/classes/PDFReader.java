package org.pdfcompress.classes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class PDFReader {
    private final int DEFAULT_CHUNK_SIZE = 4096;
    private RandomAccessFile randomAccessFile;
    private int length;
    private byte[] buffer;
    private ByteArrayOutputStream outputStream;
    private int bytesRead;
    public byte[] bytes;

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

        setBuffer();
        readChunk();

        while(bytesRead != -1){
            outputStream.write(buffer, 0 , bytesRead);

            readChunk();
        }

        bytes = outputStream.toByteArray();
    }

    private void setBuffer(){
        this.buffer = new byte[this.length];
    }

    private void readChunk() throws IOException {
        bytesRead = randomAccessFile.read(buffer);
    }
}
