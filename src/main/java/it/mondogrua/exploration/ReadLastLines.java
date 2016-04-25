package it.mondogrua.exploration;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ReadLastLines {

    private static final int DEFAULT_BUFFER_SIZE = 4096;

    public static interface TailListener {

        void handle(String line);

        void handle(Exception ex);
    }

    private final String fileName;
    private final TailListener listener;

    public ReadLastLines(String fileName, final TailListener aListener)
            throws FileNotFoundException {
        this.fileName = fileName;
        this.listener = aListener;
    }

    public void readLastLines(int lines) {
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(fileName, "r");
            seekLastlines(file, lines);
            readLines(file, listener);
        } catch (final Exception e) {
            listener.handle(e);
        } finally {
            try {
                if (file != null) {
                    file.close();
                }
            } catch (final IOException ioe) {
                // ignore
            }
        }
    }

    private void seekLastlines(RandomAccessFile file, int lines)
            throws IOException {
        long currentPos = previousStartPosition(file, file.length(), lines + 1);
        file.seek(currentPos);
    }

    private long previousStartPosition(RandomAccessFile file, long pointer,
            int backSteps) throws IOException {
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

    private void readLines(RandomAccessFile file, TailListener listener)
            throws IOException {
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(64);

        byte inputBuffer[] = new byte[DEFAULT_BUFFER_SIZE];
        int num = file.read(inputBuffer);
        while (num != -1) {
            for (int i = 0; i < num; i++) {
                final byte ch = inputBuffer[i];

                if (ch == '\n') {
                    listener.handle(new String(outputBuffer.toByteArray()));
                    outputBuffer.reset();
                } else {
                    outputBuffer.write(ch);
                }
            }
            num = file.read(inputBuffer);
        }
    }

    public static void main(String[] args) throws FileNotFoundException {

        if (args.length != 1) {
            System.out.println(
                    "Usage: java it.mondogrua.explorations.ReadLastLines <filename>");
            return;
        }
        ReadLastLines tail = new ReadLastLines(args[0], createTailListener());
        tail.readLastLines(2);
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
