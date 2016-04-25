package it.mondogrua.exploration;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ReadLinesInReverseOrder {

    public static interface TailListener {

        void handle(String line);

        void handle(Exception ex);
    }

    private final String fileName;
    private final TailListener listener;
    private RandomAccessFile file;
    private long currentPos = -1;

    public ReadLinesInReverseOrder(String fileName,
            final TailListener aListener) throws FileNotFoundException {
        this.fileName = fileName;
        this.listener = aListener;
    }

    public void readLinesInReversOrder() {
        try {
            file = new RandomAccessFile(fileName, "r");
            currentPos = file.length();

            readLines();
        } catch (final Exception e) {
            listener.handle(e);
        } finally {
            try {
                if (file != null) {
                    file.close();
                }
            } catch (final IOException ioe) {
            }
        }
    }

    private long previousStartPosition(long pointer, int backSteps)
            throws IOException {
        int terminators = 0;

        if (pointer == file.length()) {
            pointer--;
            terminators++;
        }

        for (; pointer >= 0; pointer--) {

            file.seek(pointer);
            int ch = file.readByte();

            if (ch == '\n') {
                terminators++;
            }
            if (terminators == backSteps + 1) {
                pointer++;
                break;
            }

            if (pointer == 0 && terminators == backSteps) {
                break;
            }
        }
        return pointer;
    }

    private byte read() throws IOException {
        if (currentPos == file.length()) {
            currentPos = previousStartPosition(currentPos, 2);
        }

        if (currentPos < 0) {
            return -1;
        }

        file.seek(currentPos);
        byte ch = file.readByte();

        if (ch == '\n') {
            currentPos = previousStartPosition(currentPos, 2);
        } else {
            currentPos++;
        }

        return ch;
    }

    private void readLines() throws IOException {
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(64);

        while (true) {
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

        reverseLineInputStream.readLinesInReversOrder();
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
