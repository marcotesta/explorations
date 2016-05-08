package it.mondogrua.exploration;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ReadNewlyAddedLinesToAFile {

    private static final int DEFAULT_RETRY_DELAY = 1000;
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    public interface TailListener {

        void handle(String line);

        void handle(Exception ex);
    }

    public static interface ReaderOpenerListener {

        void fileNotFound();
    }

    private final RandomAccessFile file;
    private final TailListener listener;
    private volatile boolean run = true;

    public ReadNewlyAddedLinesToAFile(RandomAccessFile file,
            final TailListener aListener) {
        this.listener = aListener;
        this.file = file;
    }

    public void readNewLines() {
        try {

            while (run) {
                if (moreToRead(file)) {
                    readLines(file);
                }
                Thread.sleep(DEFAULT_RETRY_DELAY);
            }
        } catch (final Exception e) {
            listener.handle(e);
        }
    }

    public void stopReading() {
        run = false;
    }

    private boolean moreToRead(RandomAccessFile file) throws IOException {
        return file.length() > file.getFilePointer();
    }

    private void readLines(final RandomAccessFile file) throws IOException {
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

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println(
                    "Usage: java it.mondogrua.explorations.ReadOnlyNewlyAddedLinesToAFile <filename>");
            return;
        }
        String fileName = args[0];

        RandomAccessFile file = openFile(fileName,
                createReaderOpenerListener());
        try {
            seekEndOfFile(file);

            ReadNewlyAddedLinesToAFile tail = new ReadNewlyAddedLinesToAFile(file,
                    createTailListener());
            tail.readNewLines();
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

    static RandomAccessFile openFile(String fileName,
            ReaderOpenerListener aOpenerListener) throws InterruptedException,
                    IOException {
        while (true) {
            try {
                RandomAccessFile file = new RandomAccessFile(fileName, "r");
                if (file != null) {
                    return file;
                }
            } catch (final FileNotFoundException e) {
                aOpenerListener.fileNotFound();
                Thread.sleep(DEFAULT_RETRY_DELAY);
            }
        }
    }

    private static void seekEndOfFile(RandomAccessFile file)
            throws IOException {
        file.seek(file.length());
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

    static ReaderOpenerListener createReaderOpenerListener() {
        return new ReaderOpenerListener() {

            @Override
            public void fileNotFound() {
                System.out.println(".");
            }
        };
    }
}
