package org.pdfcompress.classes;

import java.io.ByteArrayOutputStream;

public class Compressor {
    private static final int WINDOW_SIZE = 4096;
    private static final int MIN_MATCH = 3;
    private static final int MAX_MATCH = 18;
    private static final int HASH_SIZE = 1 << 15;
    private static final int HASH_MASK = HASH_SIZE - 1;
    private static final int MAX_CHAIN = 128;
    private byte[] input;
    private int[] head;
    private int[] chainPrev;
    private int bestLength;
    private int bestDistance;

    public Compressor() {

    }

    public byte[] compress(byte[] input) {
        this.input = input;

        prepareChains();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ByteArrayOutputStream pending = new ByteArrayOutputStream();
        int flag = 0;
        int tokenCount = 0;
        int position = 0;

        while (position < this.input.length) {
            findLongestMatch(position);

            if (this.bestLength >= MIN_MATCH) {
                writeMatchToken(pending);
                insertPositions(position, this.bestLength);
                position += this.bestLength;
            } else {
                flag = writeLiteralToken(pending, flag, tokenCount, position);
                insertPosition(position);
                position++;
            }

            tokenCount++;

            if (tokenCount == 8) {
                flushGroup(output, pending, flag);
                flag = 0;
                tokenCount = 0;
                pending.reset();
            }
        }

        if (tokenCount > 0) {
            flushGroup(output, pending, flag);
        }

        return output.toByteArray();
    }

    private void prepareChains() {
        this.head = new int[HASH_SIZE];
        this.chainPrev = new int[this.input.length];

        for (int i = 0; i < HASH_SIZE; i++) {
            this.head[i] = -1;
        }
    }

    private int hash(int position) {
        int a = this.input[position] & 0xff;
        int b = this.input[position + 1] & 0xff;
        int c = this.input[position + 2] & 0xff;

        return ((a << 10) ^ (b << 5) ^ c) & HASH_MASK;
    }

    private void insertPosition(int position) {
        if (position + MIN_MATCH > this.input.length) {
            return;
        }

        int h = hash(position);
        this.chainPrev[position] = this.head[h];
        this.head[h] = position;
    }

    private void insertPositions(int position, int length) {
        for (int i = 0; i < length; i++) {
            insertPosition(position + i);
        }
    }

    private void findLongestMatch(int position) {
        this.bestLength = 0;
        this.bestDistance = 0;

        if (position + MIN_MATCH > this.input.length) {
            return;
        }

        int candidate = this.head[hash(position)];
        int limit = position - WINDOW_SIZE;
        int chain = 0;

        while (candidate >= 0 && candidate >= limit && chain < MAX_CHAIN) {
            int length = matchLength(position, candidate);

            if (length > this.bestLength) {
                this.bestLength = length;
                this.bestDistance = position - candidate;

                if (length >= MAX_MATCH) {
                    return;
                }
            }

            candidate = this.chainPrev[candidate];
            chain++;
        }
    }

    private int matchLength(int position, int candidate) {
        int length = 0;

        while (length < MAX_MATCH
                && position + length < this.input.length
                && this.input[candidate + length] == this.input[position + length]) {
            length++;
        }

        return length;
    }

    private void writeMatchToken(ByteArrayOutputStream pending) {
        int value = ((this.bestDistance - 1) << 4) | (this.bestLength - MIN_MATCH);

        pending.write((value >> 8) & 0xff);
        pending.write(value & 0xff);
    }

    private int writeLiteralToken(ByteArrayOutputStream pending, int flag, int tokenCount, int position) {
        pending.write(this.input[position] & 0xff);

        return flag | (1 << tokenCount);
    }

    private void flushGroup(ByteArrayOutputStream output, ByteArrayOutputStream pending, int flag) {
        output.write(flag);

        byte[] tokens = pending.toByteArray();
        output.write(tokens, 0, tokens.length);
    }
}
