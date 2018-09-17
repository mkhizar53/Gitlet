package gitlet;

import java.io.IOException;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Mohammad Khizar
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) throws IOException {
        Command control = new Command();
        try {
            control.parseLine(args);
        } catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }

    }
}

