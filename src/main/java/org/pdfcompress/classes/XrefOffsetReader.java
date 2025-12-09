package main.java.org.pdfcompress.classes;

import java.io.RandomAccessFile;

public class XrefOffsetReader {
    private RandomAccessFile randomAccessFile;

    public XrefOffsetReader(){

    }

    public XrefOffsetReader(RandomAccessFile randomAccessFile){
        this.randomAccessFile = randomAccessFile;
    }


}
