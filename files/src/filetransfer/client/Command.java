package filetransfer.client;

import java.util.function.Consumer;

public class Command {
    public Consumer<String[]> commandMethod;
    public String usageString;

    public Command(Consumer<String[]> commandMethod, String usageString) {
        this.commandMethod = commandMethod;
        this.usageString = usageString;
    }

    public void run(String[] args) {
        try {
            commandMethod.accept(args);
        }
        catch (Exception exception) {
            System.out.println(exception.getMessage());
            System.out.println("Usage: " + usageString);
        }
    }
}
