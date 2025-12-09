package main.java.org.pdfcompress.classes;

import java.io.RandomAccessFile;

public class Writer {
    private RandomAccessFile randomAccessFile;

    public Writer(){

    }

    public Writer(RandomAccessFile randomAccessFile){
        this.randomAccessFile = randomAccessFile;
    }

}
