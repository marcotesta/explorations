package it.mondogrua.exploration;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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
        Writer writer = null;
        try {
            Scanner reader = new Scanner(inputStream);

            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputFileName)));

            try {
                while (reader.hasNextLine()) {
                    String line = reader.nextLine();
                    writer.write(line);
                    writer.write("\n");
                    writer.flush();
                }
            } finally {
                reader.close();
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
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
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
