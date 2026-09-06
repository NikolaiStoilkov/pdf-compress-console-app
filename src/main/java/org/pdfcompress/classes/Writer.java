package org.pdfcompress.classes;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Writer {
    private static final byte[] MAGIC = {'P', 'C', 'M', 'P'};
    private static final int RAW_SEGMENT = 0;
    private static final int COMPRESSED_SEGMENT = 1;

    public Writer() {

    }

    public void write(String path, byte[] data) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(path)) {
            fileOutputStream.write(data);
        }
    }

    public void writeCompressed(String path, List<PdfSegment> segments, Compressor compressor) throws IOException {
        ByteArrayOutputStream container = new ByteArrayOutputStream();

        writeMagic(container);

        for (PdfSegment segment : segments) {
            writeSegment(container, segment, compressor);
        }

        write(path, container.toByteArray());
    }

    private void writeMagic(ByteArrayOutputStream container) {
        container.write(MAGIC, 0, MAGIC.length);
    }

    private void writeSegment(ByteArrayOutputStream container, PdfSegment segment, Compressor compressor) {
        byte[] data = segment.getData();

        if (!segment.isStream()) {
            writeRawSegment(container, data);
            return;
        }

        byte[] compressed = compressor.compress(data);

        if (compressed.length < data.length) {
            writeCompressedSegment(container, data.length, compressed);
        } else {
            writeRawSegment(container, data);
        }
    }

    private void writeRawSegment(ByteArrayOutputStream container, byte[] data) {
        container.write(RAW_SEGMENT);
        writeInt(container, data.length);
        container.write(data, 0, data.length);
    }

    private void writeCompressedSegment(ByteArrayOutputStream container, int originalLength, byte[] compressed) {
        container.write(COMPRESSED_SEGMENT);
        writeInt(container, originalLength);
        writeInt(container, compressed.length);
        container.write(compressed, 0, compressed.length);
    }

    private void writeInt(ByteArrayOutputStream container, int value) {
        container.write((value >> 24) & 0xff);
        container.write((value >> 16) & 0xff);
        container.write((value >> 8) & 0xff);
        container.write(value & 0xff);
    }
}
