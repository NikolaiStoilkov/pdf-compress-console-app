package org.pdfcompress.classes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tokenizer {
    private static final byte[] STREAM_KEYWORD = {'s', 't', 'r', 'e', 'a', 'm'};
    private static final byte[] ENDSTREAM_KEYWORD = {'e', 'n', 'd', 's', 't', 'r', 'e', 'a', 'm'};
    private byte[] bytes;
    private List<PdfSegment> segments;
    private int position;
    private int streamStart;
    private int streamEnd;

    public Tokenizer() {

    }

    public Tokenizer(byte[] bytes) {
        this.bytes = bytes;
    }

    public List<PdfSegment> tokenize() {
        this.segments = new ArrayList<>();
        this.position = 0;

        while (true) {
            locateStreamStart();

            if (this.streamStart == -1) {
                addRawSegment(this.position, this.bytes.length);
                break;
            }

            locateStreamEnd();

            addRawSegment(this.position, this.streamStart);
            addStreamSegment(this.streamStart, this.streamEnd);

            this.position = this.streamEnd;
        }

        return this.segments;
    }

    private void locateStreamStart() {
        int index = this.position;

        while (index <= this.bytes.length - STREAM_KEYWORD.length) {
            if (matchesAt(index, STREAM_KEYWORD) && !isPrecededByEnd(index)) {
                this.streamStart = index + STREAM_KEYWORD.length;
                return;
            }

            index++;
        }

        this.streamStart = -1;
    }

    private void locateStreamEnd() {
        int index = this.streamStart;

        while (index <= this.bytes.length - ENDSTREAM_KEYWORD.length) {
            if (matchesAt(index, ENDSTREAM_KEYWORD)) {
                this.streamEnd = index;
                return;
            }

            index++;
        }

        this.streamEnd = this.bytes.length;
    }

    private boolean matchesAt(int index, byte[] keyword) {
        for (int i = 0; i < keyword.length; i++) {
            if (this.bytes[index + i] != keyword[i]) {
                return false;
            }
        }

        return true;
    }

    private boolean isPrecededByEnd(int index) {
        if (index < 3) {
            return false;
        }

        return this.bytes[index - 3] == 'e' && this.bytes[index - 2] == 'n' && this.bytes[index - 1] == 'd';
    }

    private void addRawSegment(int from, int to) {
        if (to <= from) {
            return;
        }

        this.segments.add(new PdfSegment(false, Arrays.copyOfRange(this.bytes, from, to)));
    }

    private void addStreamSegment(int from, int to) {
        if (to <= from) {
            return;
        }

        this.segments.add(new PdfSegment(true, Arrays.copyOfRange(this.bytes, from, to)));
    }
}
