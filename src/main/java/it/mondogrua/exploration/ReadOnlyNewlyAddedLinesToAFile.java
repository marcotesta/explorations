package it.mondogrua.exploration;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ReadOnlyNewlyAddedLinesToAFile implements Runnable {

    private static final int DEFAULT_RETRY_DELAY = 1000;
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    public interface TailListener {

        void fileNotFound();

        void handle(String line);

        void handle(Exception ex);
    }

    private final String fileName;
    private final TailListener listener;

    private volatile boolean run = true;

    public ReadOnlyNewlyAddedLinesToAFile(final String aFileName,
            final TailListener aListener) {
        this.fileName = aFileName;
        this.listener = aListener;
    }

    public void stop() {
        this.run = false;
    }

    @Override
    public void run() {
        RandomAccessFile file = null;
        try {
            file = openFile();
            while (run) {
                if (moreToRead(file)) {
                    readLines(file);
                }
                Thread.sleep(DEFAULT_RETRY_DELAY);
            }
        } catch (final Exception e) {
            listener.handle(e);
            stop();
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

    private boolean moreToRead(RandomAccessFile file) throws IOException {
        return file.length() > file.getFilePointer();
    }

    private RandomAccessFile openFile() throws InterruptedException,
            IOException {
        while (run) {
            try {
                RandomAccessFile file = new RandomAccessFile(fileName, "r");
                if (file != null) {
                    file.seek(file.length());
                    return file;
                }
            } catch (final FileNotFoundException e) {
                listener.fileNotFound();
                Thread.sleep(DEFAULT_RETRY_DELAY);
            }
        }
        return null;
    }

    private void readLines(final RandomAccessFile file) throws IOException {
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(64);

        byte inputBuffer[] = new byte[DEFAULT_BUFFER_SIZE];
        int num = file.read(inputBuffer);
        while (run && num != -1) {
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

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println(
                    "Usage: java it.mondogrua.explorations.Tail <filename>");
            return;
        }

        ReadOnlyNewlyAddedLinesToAFile tail =
                new ReadOnlyNewlyAddedLinesToAFile(args[0],
                        createTailListener());
        tail.run();
    }

    private static TailListener createTailListener() {
        return new TailListener() {

            @Override
            public void fileNotFound() {
                System.out.println(".");
            }

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
