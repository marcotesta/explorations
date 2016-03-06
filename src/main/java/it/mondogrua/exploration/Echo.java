package it.mondogrua.exploration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

public class Echo implements Runnable {

    private InputStream inputStream;
    private String outputFileName;

    public Echo(InputStream inputStream, String outputFileName) {
        super();
        this.inputStream = inputStream;
        this.outputFileName = outputFileName;
    }

    @Override
    public void run() {
        Scanner input = null;
        RandomAccessFile output = null;
        try {
            input = new Scanner(inputStream);
            output = new RandomAccessFile(outputFileName, "rw");

            try {
                while (input.hasNextLine()) {

                    String line = input.nextLine();
                    output.seek(output.length());
                    output.write(line.getBytes());
                    output.write('\n');
                }
            } finally {
                input.close();
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
            }
            if (input != null) {
                input.close();
            }
        }
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println(
                    "Usage: java it.mondogrua.explorations.Echo <filename>");
            return;
        }
        Echo echo = new Echo(System.in, args[0]);
        echo.run();
    }
}
