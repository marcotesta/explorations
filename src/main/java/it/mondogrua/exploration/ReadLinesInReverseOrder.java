package it.mondogrua.exploration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ReadLinesInReverseOrder implements Runnable {

    public static interface TailListener {

        void handle(String line);

        void handle(Exception ex);
    }

    private RandomAccessFile in;

    private long currentLineStart = -1;
    private long currentLineEnd = -1;
    private long currentPos = -1;

    private final TailListener listener;
    private volatile boolean run = true;

    private String fileName;

    public ReadLinesInReverseOrder(String fileName,
            final TailListener aListener) throws FileNotFoundException {
        this.fileName = fileName;
        this.listener = aListener;
    }

    @Override
    public void run() {
        File file = new File(fileName);
        try {
            in = new RandomAccessFile(file, "r");
            currentLineStart = file.length();
            currentLineEnd = file.length();
            currentPos = currentLineEnd;

            readLines();
        } catch (final Exception e) {
            listener.handle(e);
            stop();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException ioe) {
            }
        }
    }

    public void stop() {
        this.run = false;
    }

    private long previousStartPosition(long currentTerminator)
            throws IOException {
        long pointer;
        for (pointer = currentTerminator - 1; pointer >= 0; pointer--) {

            in.seek(pointer);
            int readByte = in.readByte();

            if (readByte == '\n') {
                pointer++;
                break;
            } else if (pointer == 0) {
                break;
            }
        }
        return pointer;
    }

    private byte read() throws IOException {

        if (currentPos == currentLineEnd) {
            long previousStartPos = previousStartPosition(currentLineStart - 1);
            if (previousStartPos == currentPos) {
                return -1;
            }
            currentPos = previousStartPos;
            currentLineEnd = currentLineStart;
            currentLineStart = currentPos;
        }
        if (currentPos < 0) {
            return -1;
        }

        in.seek(currentPos);
        byte readByte = in.readByte();
        currentPos++;
        return readByte;
    }

    private void readLines() throws IOException {
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(64);

        while (run) {
            byte ch = read();
            if (ch == -1) {
                return;
            }

            if (ch == '\n') {
                listener.handle(new String(outputBuffer.toByteArray()));
                outputBuffer.reset();
            } else {
                outputBuffer.write(ch);
            }
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        String fileName = args[0];

        ReadLinesInReverseOrder reverseLineInputStream =
                new ReadLinesInReverseOrder(fileName, createTailListener());

        reverseLineInputStream.run();
    }

    private static TailListener createTailListener() {
        return new TailListener() {

            @Override
            public void handle(String line) {
                System.out.println(line);
            }

            @Override
            public void handle(Exception ex) {
                System.out.println(ex.getMessage());
            }
        };
    }
}
