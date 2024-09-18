package filetransfer.client;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Client {
    private static final HashMap<String, Command> commands = new HashMap<>() {{
        put("list", new Command(Client::list, "list"));
        put("delete", new Command(Client::delete, "delete <filename>"));
        put("rename", new Command(Client::rename, "rename <old_filename> <new_filename>"));
        put("download", new Command(Client::download, "download <filename>"));
        put("upload", new Command(Client::upload, "upload <filename>"));
    }};

    private static InetSocketAddress serverAddress;

    public static void main(String[] args) {
        readServerAddress(args);

        System.out.println("Welcome! Please enter a command.");

        try (Scanner keyboardScanner = new Scanner(System.in)){
            while (true) {
                awaitCommand(keyboardScanner);
            }
        }
    }

    private static void readServerAddress(String[] args) {
        int port = Integer.parseInt(args[1]);
        serverAddress = new InetSocketAddress(args[0], port);
    }

    private static void awaitCommand(Scanner keyboardScanner) {
        String[] command_args = keyboardScanner.nextLine().split(" ");

        if (command_args[0].isEmpty()) {
            System.out.println("Please enter a command.");
            return;
        }

        if (!commands.containsKey(command_args[0])){
            System.out.println("Invalid command.");
            return;
        }

        commands.get(command_args[0]).run(Arrays.copyOfRange(command_args, 1, command_args.length));
    }

    private static void list(String[] args) {
        System.out.println(Arrays.toString(args));
    }

    private static void delete(String[] args) {
        System.out.println(Arrays.toString(args));
    }

    private static void rename(String[] args) {
        System.out.println(Arrays.toString(args));
    }

    private static void download(String[] args) {
        System.out.println(Arrays.toString(args));
    }

    private static void upload(String[] args) {
        System.out.println(Arrays.toString(args));
    }
}
