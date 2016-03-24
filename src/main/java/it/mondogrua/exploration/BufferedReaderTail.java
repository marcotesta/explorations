package it.mondogrua.exploration;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class BufferedReaderTail {

    private static final int DEFAULT_RETRY_DELAY = 1000;

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

    private volatile boolean run = true;

    public void stop() {
        this.run = false;
    }

    public void tail(final BufferedReader aReader, final TailListener aListener) {

        try {
            while (run) {
                readLines(aReader, aListener);
                Thread.sleep(DEFAULT_RETRY_DELAY);
            }
        } catch (final Exception e) {
            aListener.handle(e);
            stop();
        }
    }

    private void readLines(final BufferedReader aFile, TailListener aListener) throws IOException {
        while (run) {
            String string = aFile.readLine();
            if (string == null) {
                break;
            }
            aListener.handle(string);
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

        try {
            BufferedReaderTail tail = new BufferedReaderTail();
            tail.tail(new BufferedReader(fileReader), new TailListener() {

                @Override
                public void handle(String line) {
                    System.out.println(line);
                }

                @Override
                public void handle(Exception ex) {
                    System.out.println(ex.getMessage());
                }
            });
        } finally {
            try {
                fileReader.close();
            } catch (IOException e) {
            }
        }
    }
}
