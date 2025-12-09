package main.java.org.pdfcompress.classes;

import java.io.RandomAccessFile;

public class Compressor {
    private RandomAccessFile randomAccessFile;

    public Compressor(){

    }

    public Compressor(RandomAccessFile randomAccessFile){
        this.randomAccessFile = randomAccessFile;
    }


}
