package org.pdfcompress;


import org.pdfcompress.classes.*;

import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {
    private static final String INPUT_PATH = "test-pdf.pdf";
    private static final String COMPRESSED_PATH = "test-pdf.cmp";
    private static final String RESTORED_PATH = "test-pdf.restored.pdf";

    static void main(String[] args) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(INPUT_PATH, "rw")) {
            PDFReader reader = new PDFReader(randomAccessFile, 4096);
            TrailLocator trailLocator = new TrailLocator(randomAccessFile);
            XrefOffsetReader xrefOffsetReader = new XrefOffsetReader(randomAccessFile);

            reader.read();
            trailLocator.locate();

            Map<Integer, Long> objectOffsets = xrefOffsetReader.parseTable(trailLocator.startXrefOffset);

            Tokenizer tokenizer = new Tokenizer(reader.bytes);
            List<PdfSegment> segments = tokenizer.tokenize();

            Compressor compressor = new Compressor();
            Writer writer = new Writer();
            writer.writeCompressed(COMPRESSED_PATH, segments, compressor);

            Decompressor decompressor = new Decompressor();
            byte[] restored = decompressor.decompress(COMPRESSED_PATH);
            writer.write(RESTORED_PATH, restored);

            report(reader.bytes, restored, objectOffsets.size(), segments.size());
        } catch (Exception e) {
            System.out.println("Error reading the file" + e);
        }
    }

    private static void report(byte[] original, byte[] restored, int objectCount, int segmentCount) {
        long originalSize = original.length;
        long compressedSize = new java.io.File(COMPRESSED_PATH).length();
        boolean lossless = Arrays.equals(original, restored);
        double ratio = originalSize == 0 ? 0 : (double) compressedSize / originalSize * 100;

        System.out.println("Cross-reference objects: " + objectCount);
        System.out.println("Segments: " + segmentCount);
        System.out.println("Original size: " + originalSize + " bytes");
        System.out.println("Compressed size: " + compressedSize + " bytes");
        System.out.printf("Compressed to: %.2f%% of original%n", ratio);
        System.out.println("Lossless round-trip: " + lossless);
    }
}
