package it.mondogrua.exploration;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

public class ReaderTail implements Runnable {

    private static final int DEFAULT_RETRY_DELAY = 1000;
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    public static interface TailListener {

        void handle(String line);

        void handle(Exception ex);
    }

    public static interface ReaderOpenerListener {

        void fileNotFound();
    }

    public static class ReaderOpener {
        private volatile boolean run = true;
        public FileReader openFileReader(String aFileName, ReaderOpenerListener aOpenerListener) {
            while (run) {
                try {
                    FileReader file = new FileReader(aFileName);
                    if (file != null) {
                        return file;
                    }
                } catch (final FileNotFoundException e) {
                    aOpenerListener.fileNotFound();
                    try {
                        Thread.sleep(DEFAULT_RETRY_DELAY);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
            return null;
        }
        public void stop() {
            this.run = false;
        }
    }

    private final Reader reader;
    private final TailListener listener;

    private volatile boolean run = true;

    public ReaderTail(final Reader aReader, final TailListener aListener) {
        this.reader = aReader;
        this.listener = aListener;
    }

    public void stop() {
        this.run = false;
    }

    @Override
    public void run() {

        try {
            while (run) {
                readLines(reader, listener);
                Thread.sleep(DEFAULT_RETRY_DELAY);
            }
        } catch (final Exception e) {
            listener.handle(e);
            stop();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (final IOException ioe) {
                // ignore
            }
        }
    }

    private void readLines(final Reader aFile, TailListener aListener) throws IOException {
        char inputBuffer[] = new char[DEFAULT_BUFFER_SIZE];
        while (run) {
            int num = aFile.read(inputBuffer);
            if (num == -1) {
                break;
            }
            StringWriter outputBuffer = new StringWriter(64);
            for (int i = 0; i < num; i++) {
                char ch = inputBuffer[i];
                if (ch == '\n') {
                    aListener.handle(outputBuffer.toString());
                    outputBuffer = new StringWriter(64);
                } else {
                    outputBuffer.write(ch);
                }
            }
        }
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println(
                    "Usage: java it.mondogrua.explorations.Tail <filename>");
            return;
        }
        String aFileName = args[0];

        FileReader fileReader = new ReaderOpener().openFileReader(aFileName, new ReaderOpenerListener(){

            @Override
            public void fileNotFound() {
                System.out.println(".");
            }});


        ReaderTail tail = new ReaderTail(fileReader, new TailListener() {

            @Override
            public void handle(String line) {
                System.out.println(line);
            }

            @Override
            public void handle(Exception ex) {
                System.out.println(ex.getMessage());
            }

        });
        tail.run();
    }
}
