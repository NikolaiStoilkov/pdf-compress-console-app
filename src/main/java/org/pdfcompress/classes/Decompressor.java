package org.pdfcompress.classes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Decompressor {
    private static final byte[] MAGIC = {'P', 'C', 'M', 'P'};
    private static final int RAW_SEGMENT = 0;
    private static final int MIN_MATCH = 3;
    private byte[] input;
    private int position;
    private byte[] output;
    private int outputSize;

    public Decompressor() {

    }

    public byte[] decompress(String path) throws IOException {
        this.input = Files.readAllBytes(Path.of(path));
        this.position = 0;
        this.output = new byte[Math.max(64, this.input.length)];
        this.outputSize = 0;

        readMagic();

        while (this.position < this.input.length) {
            int type = readByte();

            if (type == RAW_SEGMENT) {
                readRawSegment();
            } else {
                readCompressedSegment();
            }
        }

        return Arrays.copyOf(this.output, this.outputSize);
    }

    private void readMagic() throws IOException {
        for (int i = 0; i < MAGIC.length; i++) {
            if (readByte() != (MAGIC[i] & 0xff)) {
                throw new IOException("Compressed format error: missing PCMP header.");
            }
        }
    }

    private void readRawSegment() {
        int length = readInt();

        for (int i = 0; i < length; i++) {
            appendByte(this.input[this.position + i]);
        }

        this.position += length;
    }

    private void readCompressedSegment() {
        readInt();
        int compressedLength = readInt();

        expand(this.position, this.position + compressedLength);

        this.position += compressedLength;
    }

    private void expand(int from, int to) {
        int index = from;

        while (index < to) {
            int flag = this.input[index++] & 0xff;

            for (int bit = 0; bit < 8 && index < to; bit++) {
                if (((flag >> bit) & 1) == 1) {
                    appendByte(this.input[index++]);
                } else {
                    int high = this.input[index++] & 0xff;
                    int low = this.input[index++] & 0xff;
                    int value = (high << 8) | low;
                    int distance = ((value >> 4) & 0x0fff) + 1;
                    int length = (value & 0x0f) + MIN_MATCH;

                    copyMatch(distance, length);
                }
            }
        }
    }

    private void copyMatch(int distance, int length) {
        for (int i = 0; i < length; i++) {
            appendByte(this.output[this.outputSize - distance]);
        }
    }

    private void appendByte(byte value) {
        ensureCapacity(1);
        this.output[this.outputSize++] = value;
    }

    private void ensureCapacity(int extra) {
        if (this.outputSize + extra <= this.output.length) {
            return;
        }

        int newCapacity = this.output.length * 2;

        while (newCapacity < this.outputSize + extra) {
            newCapacity *= 2;
        }

        this.output = Arrays.copyOf(this.output, newCapacity);
    }

    private int readByte() {
        return this.input[this.position++] & 0xff;
    }

    private int readInt() {
        int b1 = readByte();
        int b2 = readByte();
        int b3 = readByte();
        int b4 = readByte();

        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }
}
