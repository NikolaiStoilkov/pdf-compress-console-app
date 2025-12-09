package main.java.org.pdfcompress;

import main.java.org.pdfcompress.classes.PDFReader;
import main.java.org.pdfcompress.classes.Tokenizer;
import main.java.org.pdfcompress.classes.TrailLocator;
import main.java.org.pdfcompress.classes.XrefOffsetReader;

import java.io.RandomAccessFile;
import java.util.Arrays;

public class Main {
     static void main(String[] args) {
        try(RandomAccessFile randomAccessFile = new RandomAccessFile("test-pdf.pdf", "rw")){
            PDFReader reader = new PDFReader(randomAccessFile, 4096);
            TrailLocator trailLocator = new TrailLocator(randomAccessFile);
            XrefOffsetReader xrefOffsetReader = new XrefOffsetReader(randomAccessFile);
            Tokenizer tokenizer = new Tokenizer(randomAccessFile);

            reader.read();
            trailLocator.locate();

            System.out.println("PDF Reader bytes " + Arrays.toString(reader.bytes));
            System.out.println("Read from here " + trailLocator.startXrefOffset); // Read from here

            // Reading cross-reference table
            xrefOffsetReader.praseTable(trailLocator.startXrefOffset);




        } catch (Exception e) {
            System.out.println("Error reading the file" + e);
        }
    }
}



