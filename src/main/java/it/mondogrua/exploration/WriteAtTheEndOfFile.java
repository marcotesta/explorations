package it.mondogrua.exploration;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class WriteAtTheEndOfFile {

    private RandomAccessFile output;

    public WriteAtTheEndOfFile(RandomAccessFile output) {
        this.output = output;
    }

    public void write(String line) {
        try {
            output.seek(output.length());
            output.write(line.getBytes());
            output.write('\n');
        } catch (IOException e) {
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            System.out.println(
                    "Usage: java it.mondogrua.explorations.Echo <filename>");
            return;
        }
        String outputFileName = args[0];
        RandomAccessFile output = new RandomAccessFile(outputFileName, "rw");

        WriteAtTheEndOfFile echo = new WriteAtTheEndOfFile(output);

        Scanner input = new Scanner(System.in);

        while (input.hasNextLine()) {
            String line = input.nextLine();
            echo.write(line);
        }

        input.close();
        output.close();
    }
}
