package com.byoutline.mockserver.sample;


import com.byoutline.mockserver.HttpMockServer;
import com.byoutline.mockserver.NetworkType;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;


/**
 * Created by michalp on 25.04.16.
 */
public class ConfigServer {
    static NetworkType nt = NetworkType.NONE;

    public static void main(String... args) {

        Options options = new Options();
        options.addOption("n",true,"network type(GPRS,EDGE,UMTS,VPN,NONE)")
                .addOption("h",false,"help message");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if(cmd.hasOption("n")){
                nt = NetworkType.valueOf(cmd.getOptionValue("n"));
                System.out.println("Selected network type: " + nt.name());
            }
            if (cmd.hasOption("h")) {
                System.out.println("HELP");
                displayHelp(options);
            }

            searchMockPathAndRunServer(cmd);

        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

    private static void searchMockPathAndRunServer(CommandLine cmd) {
        List targetList = cmd.getArgList();
        if(targetList.isEmpty()){
            System.out.println("Path not detected,enter the path to mock resources.");
        }else{
            String mockPath = targetList.get(0).toString();
            File fileDir = new File(mockPath);
            File configFile = new File(mockPath +"/config.json");
            if (!fileDir.isDirectory()){
                System.out.println("Please enter the directory to mock resources" +
                        " (e.g. /home/example/mockResDir )");
                return;
            }
            if (configFile.exists()){
                runServerWithPath(mockPath);
            }else {
                System.out.println(" ERR: Config file not detected.");
                return;
            }

        }
    }

    private static void displayHelp(Options options) {
        final int width = 180;
        final int descPadding = 5;
        final PrintWriter out = new PrintWriter(System.out, true);
        HelpFormatter formatter = new HelpFormatter();
        String example =" \nExample of use:\n" +
                " java -jar sample.jar /home/example/mockResPath -n EDGE -h\n " +
                " WHERE:\n" +
                " /home/example/path - path to directory with mock resources\n" +
                " EDGE - network type (possible types: NONE, EDGE,UMTS,GPRS,VPN)\n";
        formatter.setWidth(width);
        formatter.setDescPadding(descPadding);
        formatter.printUsage(out, width,ConfigServer.class.getName()+ "  /path", options);
        formatter.printWrapped(out, width, "");
        formatter.printOptions(out, width, options, formatter.getLeftPadding(), formatter.getDescPadding());
        formatter.printWrapped(out, width, example);
        formatter.printWrapped(out, width, "");
    }

    private static boolean runServerWithPath(String path) {
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
