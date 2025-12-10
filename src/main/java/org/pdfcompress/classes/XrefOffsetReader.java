package main.java.org.pdfcompress.classes;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

public class XrefOffsetReader {
    private RandomAccessFile randomAccessFile;
    private Map<Integer, Long> objectOffsets;
    private String line;
    private String headerLine;
    private String[] parts;
    private int currentObjectId;
    private int count;
    private String entryLine;
    private String offsetString;
    private long parsedOffset;

    public XrefOffsetReader() {

    }

    public XrefOffsetReader(RandomAccessFile randomAccessFile) {
        this.randomAccessFile = randomAccessFile;
    }

    public  Map<Integer, Long> praseTable(long startXrefOffset) {
        try {
            seekFromIndex(startXrefOffset);

            readLine();

            if (this.line == null || !this.line.trim().equals("xref")) {
                throw new IOException("This simple parser cannot read it.");
            }

            while (true) {
                readHeaderLine();
                //Here i am facing the problem. i have to read the file
                // backwards so i can access
                // those lines "0000147086 00000 n
                //0000173213 00000 n
                //0000176286 00000 n
                //0000176332 00000 n
                //trailer
                //<</Size 198/Root 1 0 R/Info 49 0 R/ID[<754963A46EED8B45B944136AE609A06F><754963A46EED8B45B944136AE609A06F>] >>
                //startxref"
                // The solution for this is createiing input stream class that can reverse the stream
                // and reading ith normally

                // Skipping empty lines
                while (this.headerLine != null && this.headerLine.trim().isEmpty()) {
                    readHeaderLine();
                }

                if (this.headerLine == null || this.headerLine.trim().startsWith("trailer")) {
                    break;
                }

                setParts();
                setCurrentObjectId();
                setCount();

                for (int i = 0; i < this.count + 20; i++) {
                    readEntryLine();

                    if (this.entryLine == null) {
                        break;
                    }

                    if (this.entryLine.endsWith("n") && this.entryLine.contains(" n")) {
                        setOffsetString();

                        parseOffset();


                        if(isCompressed()){
                            //TODO: something
                        }

                        setObjectOffset();
                    }

                    incrementObjectId();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return objectOffsets;
    }

    private void seekFromIndex(long startXrefOffset) {
        try {
            this.randomAccessFile.seek(startXrefOffset);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void readLine() {
        try {
            this.line = this.randomAccessFile.readLine();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void readHeaderLine() {
        try {
            this.headerLine = this.randomAccessFile.readLine();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setParts() {
        this.parts = this.headerLine.trim().split("\\s+");
    }

    private void setCurrentObjectId() {
        this.currentObjectId = Integer.parseInt(this.parts[0]);
    }

    private void setCount() {
        this.count = Integer.parseInt(parts[1]);
    }

    private void readEntryLine() {
        try {
            this.entryLine = this.randomAccessFile.readLine();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setOffsetString(){
        this.offsetString = this.entryLine.substring(0,10);
    }

    private void parseOffset(){
        this.parsedOffset = Long.parseLong(this.offsetString);
    }

    private void setObjectOffset(){
        this.objectOffsets.put(this.currentObjectId, this.parsedOffset);
    }

    private void incrementObjectId(){
        this.currentObjectId++;
    }

    private Boolean isCompressed(){
        return Integer.parseInt(this.parts[0]) == 0 && Integer.parseInt(this.parts[1]) == 0;
    }
}
