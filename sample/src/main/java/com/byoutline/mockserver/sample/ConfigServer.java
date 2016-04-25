package com.byoutline.mockserver.sample;


import com.byoutline.mockserver.HttpMockServer;
import com.byoutline.mockserver.NetworkType;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.PrintWriter;


/**
 * Created by michalp on 25.04.16.
 */
public class ConfigServer {
    static NetworkType nt = NetworkType.NONE;

    public static void main(String... args) {

        Options options = new Options();
        options.addOption("c", true, "start server with custom config path (press ctrl+c to quit)");
        options.addOption("n", true, "network type");
        options.addOption("help",false, "help info");

        HelpFormatter formatter = new HelpFormatter();

        parseCmd(options, formatter, args);
    }

    private static void parseCmd(Options options, HelpFormatter formatter, String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("n")) {
                selectNetworkType(cmd.getOptionValue("n"));
                System.out.println("Selected network type: " + nt.name());
            }

            if (cmd.hasOption("c")) {
                runServerWithPath(cmd);
            }else if(cmd.hasOption("help")){

                helpInfo(options, formatter);

            }else {
                formatter.printHelp("ConfigServer help", options);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static void helpInfo(Options options, HelpFormatter formatter) {
        final int width = 180;
        final int descPadding = 5;
        final PrintWriter out = new PrintWriter(System.out, true);
        String example =" \nExample of use:\n" +
                " java -jar sample.jar -c /home/example/path -n EDGE\n " +
                " WHERE:\n" +
                " /home/example/path - path to directory with mock resources\n" +
                " EDGE - network type (possible types: NONE, EDGE,UMTS,GPRS,VPN)\n";
        formatter.setWidth(width);
        formatter.setDescPadding(descPadding);
        formatter.printUsage(out, width,ConfigServer.class.getName(), options);
        formatter.printWrapped(out, width, "");
        formatter.printOptions(out, width, options, formatter.getLeftPadding(), formatter.getDescPadding());
        formatter.printWrapped(out, width, example);
        formatter.printWrapped(out, width, "");
    }

    private static void selectNetworkType(String n) {
        switch (n) {
            case "NONE":
                nt = NetworkType.NONE;
                break;
            case "GPRS":
                nt = NetworkType.GPRS;
                break;
            case "EDGE":
                nt = NetworkType.EDGE;
                break;
            case "UMTS":
                nt = NetworkType.UMTS;
                break;
            case "VPN":
                nt = NetworkType.VPN;
                break;
            default:
                break;
        }
    }

    private static boolean runServerWithPath(CommandLine cmd) {
        String path = cmd.getOptionValue("c");
        final HttpMockServer httpMockServer = HttpMockServer.startMockApiServer(new SampleReader(path), NetworkType.NONE);
        try {
            synchronized (httpMockServer) {
                httpMockServer.wait();
            }
        } catch (InterruptedException e) {
            // Interrupting is expected way to stop this sleep.
            try {
                System.out.println("Shouting server down ...");
                httpMockServer.shutdown();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
}
