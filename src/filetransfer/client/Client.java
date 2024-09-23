package filetransfer.client;

import filetransfer.shared.message.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
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
        put("quit", new Command(Client::quit, "quit"));
    }};

    private static InetSocketAddress serverAddress;

    public static void main(String[] args) {
        readServerAddress(args);

        System.out.println("Welcome! Please enter a command.");

        try (Scanner keyboardScanner = new Scanner(System.in)){
            //noinspection InfiniteLoopStatement
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
        try (SocketChannel channel = SocketChannel.open()) {
            channel.connect(serverAddress);

            System.out.println("Requesting list of files...");

            ListRequest request = new ListRequest(channel);
            request.writeToChannel();
            channel.shutdownOutput();

            ListReply reply = new ListReply(channel);
            reply.readFromChannel();

            System.out.println("Got filenames: ");
            for (String filename: reply.filenames) {
                System.out.println("\t" + filename);
            }
        }
        catch (IOException exception) {
            System.out.println("Failed to allocate channel: " + exception.getMessage());
        }
    }

    private static void delete(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("You must provide the name of the file you wish to delete.");
        }

        try (SocketChannel channel = SocketChannel.open()) {
            channel.connect(serverAddress);

            System.out.println("Requesting to delete " + args[0]);

            DeleteRequest request = new DeleteRequest(channel);
            request.filename = args[0];
            request.writeToChannel();
            channel.shutdownOutput();

            DeleteReply reply = new DeleteReply(channel);
            reply.readFromChannel();

            switch (reply.errorCode) {
                case SUCCESS -> System.out.println("Successfully deleted " + args[0]);
                case FILE_NOT_FOUND -> System.out.println("Could not delete " + args[0] + ", file not found");
                case PERMISSION_DENIED -> System.out.println("Could not delete " + args[0] + ", permission denied");
            }
        }
        catch (IOException exception) {
            System.out.println("Failed to allocate channel: " + exception.getMessage());
        }
    }

    private static void rename(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException();
        }

        try (SocketChannel channel = SocketChannel.open()){
            channel.connect(serverAddress);

            System.out.println("Requesting to rename " + args[0] " to " + args[1]);

            RenameRequest request = new RenameRequest(channel);
            request.oldFilename = args[0];
            request.newFilename = args[1];
            request.writeToChannel();


        }
        catch (IOException exception) {
            System.out.println("Failed to allocate channel: " + exception.getMessage());
        }
    }

    private static void download(String[] args) {
        System.out.println(Arrays.toString(args));
    }

    private static void upload(String[] args) {
        System.out.println(Arrays.toString(args));
    }

    private static void quit(String[] args) {
        System.exit(0);
    }
}
