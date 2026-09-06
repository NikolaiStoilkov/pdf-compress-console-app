package org.pdfcompress.classes;

import java.io.RandomAccessFile;
import java.util.HashMap;
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

    public Map<Integer, Long> parseTable(long startXrefOffset) {
        this.objectOffsets = new HashMap<>();

        try {
            seekFromIndex(startXrefOffset);

            readLine();

            if (this.line == null || !this.line.trim().equals("xref")) {
                return this.objectOffsets;
            }

            readHeaderLine();

            while (this.headerLine != null) {
                if (this.headerLine.trim().isEmpty()) {
                    readHeaderLine();
                    continue;
                }

                if (this.headerLine.trim().startsWith("trailer")) {
                    break;
                }

                if (!isSubsectionHeader()) {
                    break;
                }

                setParts();
                setCurrentObjectId();
                setCount();

                for (int i = 0; i < this.count; i++) {
                    readEntryLine();

                    if (this.entryLine == null) {
                        break;
                    }

                    if (isInUseEntry()) {
                        setOffsetString();
                        parseOffset();
                        setObjectOffset();
                    }

                    incrementObjectId();
                }

                readHeaderLine();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return this.objectOffsets;
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

    private boolean isSubsectionHeader() {
        String[] tokens = this.headerLine.trim().split("\\s+");

        if (tokens.length != 2) {
            return false;
        }

        return isNumeric(tokens[0]) && isNumeric(tokens[1]);
    }

    private boolean isNumeric(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void setParts() {
        this.parts = this.headerLine.trim().split("\\s+");
    }

    private void setCurrentObjectId() {
        this.currentObjectId = Integer.parseInt(this.parts[0]);
    }

    private void setCount() {
        this.count = Integer.parseInt(this.parts[1]);
    }

    private void readEntryLine() {
        try {
            this.entryLine = this.randomAccessFile.readLine();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isInUseEntry() {
        return this.entryLine.trim().endsWith("n") && this.entryLine.trim().length() >= 18;
    }

    private void setOffsetString() {
        this.offsetString = this.entryLine.trim().substring(0, 10);
    }

    private void parseOffset() {
        this.parsedOffset = Long.parseLong(this.offsetString);
    }

    private void setObjectOffset() {
        this.objectOffsets.put(this.currentObjectId, this.parsedOffset);
    }

    private void incrementObjectId() {
        this.currentObjectId++;
    }
}
