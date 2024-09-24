package fr.umontpellier.evo;

import picocli.CommandLine;

public class Start {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new EvoCommand()).execute(args);
        System.exit(exitCode);
    }

}
