package org.pdfcompress.classes;

public class PdfSegment {
    private boolean stream;
    private byte[] data;

    public PdfSegment() {

    }

    public PdfSegment(boolean stream, byte[] data) {
        this.stream = stream;
        this.data = data;
    }

    public boolean isStream() {
        return this.stream;
    }

    public byte[] getData() {
        return this.data;
    }
}
