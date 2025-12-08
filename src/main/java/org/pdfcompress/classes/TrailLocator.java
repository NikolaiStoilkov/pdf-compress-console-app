package main.java.org.pdfcompress.classes;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

public class TrailLocator {
    private RandomAccessFile randomAccessFile;
    private long fileLength;
    private long scanSize;
    private long startingPosition;
    private byte[] buffer;
    private String tail;
    private int eofIndex;
    private int startXrefIndex;
    private String offsetString;
    public long startXrefOffset;

    public TrailLocator(){}

    public TrailLocator(RandomAccessFile randomAccessFile){
        this.randomAccessFile = randomAccessFile;
    }

    public void locate() throws IOException {
        setFileLength();

        setScanSize();

        setStartingPosition();
        this.randomAccessFile.seek(this.startingPosition);

        locateBufferSize();
        this.randomAccessFile.readFully(this.buffer);

        setTail();

        setEofIndex();

        setStartXrefIndex();

        setOffsetString();

        try {
            this.startXrefOffset = Long.parseLong(this.offsetString);
        } catch (NumberFormatException e) {
            throw new IOException("PDF format error: Could not parse startxref offset value: '" + offsetString + "'");
        }
    }

    private void setFileLength() throws IOException {
        this.fileLength = this.randomAccessFile.length();
    }

    private void setScanSize(){
        this.scanSize = Math.min(this.fileLength, 1024);
    }

    private void setStartingPosition(){
        this.startingPosition = this.fileLength - this.scanSize;
    }

    private void locateBufferSize(){
        this.buffer = new byte[(int)this.scanSize];
    }

    private void setTail(){
        this.tail = new String(buffer, StandardCharsets.ISO_8859_1);
    }

    private void setEofIndex() throws IOException {
        int eofIndex = this.tail.lastIndexOf("%%EOF");

        if (eofIndex == -1) {
            throw new IOException("PDF format error: %%EOF marker not found.");
        }

       this.eofIndex = eofIndex;
    }

    private void setStartXrefIndex(){
        this.startXrefIndex = this.tail.lastIndexOf("startxref", eofIndex);
    }

    private void setOffsetString() {
        // 9 because we need to skip "startxref" and set the offset after this.
        this.offsetString = tail.substring(startXrefIndex + 9, eofIndex).trim();
    }
}
