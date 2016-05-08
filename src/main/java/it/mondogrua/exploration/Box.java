package it.mondogrua.exploration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.mondogrua.count.Count;
import it.mondogrua.count.DateCount;

public class Box {
    private static final int DEFAULT_RETRY_DELAY = 1000;
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println(
                    "Usage: java it.mondogrua.explorations.Box <filename>");
            return;
        }
        String fileName = args[0];

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    listenFile(fileName);
                }
            });
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    listenConsole(fileName);
                }
            });


        } finally {
            executor.shutdown();
        }
    }

    private static void listenConsole(String fileName) {
        Scanner input = new Scanner(System.in);
        String newLine = inputLine(input);
        RandomAccessFile outputFile  =null;
        try {
            outputFile = new RandomAccessFile(fileName, "rw");
            seekEndOfFile(outputFile);
            write(outputFile, newLine);
        } catch (Exception e) {
            try {
                if (outputFile != null) {
                    outputFile.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static void listenFile(String fileName) {
        RandomAccessFile inputFile = null;
        RandomAccessFile outputFile  =null;
        Writer console = null ;
        try {
            console = new OutputStreamWriter(System.out);
            inputFile = new RandomAccessFile(fileName, "r");
            outputFile = new RandomAccessFile(fileName, "rw");
            Count count = new DateCount();
            seekEndOfFile(inputFile);
            String line = waitNewLine(inputFile);
            if (line.startsWith("+")) {
                count.increment();
            }
            String outputLine = count.getCountValue().toString();
            seekEndOfFile(outputFile);
            write(console, outputLine);
            write(outputFile, outputLine);
        } catch (Exception e) {
            try {
                if (inputFile != null) {
                    inputFile.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                if (outputFile != null) {
                    outputFile.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                console.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static String waitNewLine(RandomAccessFile file) throws Exception {
        while (true) {
            if (moreToRead(file)) {
                String line = readLine(file);
                if (line != null) {
                    return line;
                }
            }
            Thread.sleep(DEFAULT_RETRY_DELAY);
        }
    }

    private static boolean moreToRead(RandomAccessFile file) throws IOException {
        return file.length() > file.getFilePointer();
    }

    private static String readLine(final RandomAccessFile file) throws IOException {
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream(64);

        byte inputBuffer[] = new byte[DEFAULT_BUFFER_SIZE];
        int num = file.read(inputBuffer);
        while (num != -1) {
            for (int i = 0; i < num; i++) {
                byte ch = inputBuffer[i];
                if (ch == '\n') {
                    break;
                } else {
                    outputBuffer.write(ch);
                }
            }
            num = file.read(inputBuffer);
        }
        return new String(outputBuffer.toByteArray());
    }

    private static void seekEndOfFile(RandomAccessFile file) throws IOException {
        file.seek(file.length());
    }

    private static void write(RandomAccessFile output, String line) {
        try {
            output.write(line.getBytes());
            output.write('\n');
        } catch (IOException e) {
        }
    }

    private static void write(Writer output, String line) {
        try {
            output.write(line);
            output.write('\n');
            output.flush();
        } catch (IOException e) {
        }
    }

    private static String inputLine(Scanner input) {
        if (input.hasNextLine()) {
            String inputLine = input.nextLine();
            return inputLine;
        }
        return null;
    }

}
