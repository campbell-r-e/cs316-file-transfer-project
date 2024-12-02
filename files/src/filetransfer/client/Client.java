package filetransfer.client;

import filetransfer.shared.ErrorCode;
import filetransfer.shared.message.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Client {
    private static final HashMap<String, Command> COMMANDS = new HashMap<>() {{
        put("list", new Command(Client::list, "list"));
        put("delete", new Command(Client::delete, "delete <filename>"));
        put("rename", new Command(Client::rename, "rename <old_filename> <new_filename>"));
        put("download", new Command(Client::download, "download <filename>"));
        put("upload", new Command(Client::upload, "upload <filename>"));
        put("quit", new Command(Client::quit, "quit"));
    }};
    private static final File CLIENT_FILES = new File("files/client_files");

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

        if (!COMMANDS.containsKey(command_args[0])){
            System.out.println("Invalid command.");
            return;
        }

        COMMANDS.get(command_args[0]).run(Arrays.copyOfRange(command_args, 1, command_args.length));
    }

    private static void list(String[] args) {
        try (SocketChannel channel = SocketChannel.open()) {
            channel.connect(serverAddress);

            System.out.println("Requesting list of files...");

            ListRequest request = new ListRequest(channel);
            request.writeToChannel();
            channel.shutdownOutput();

            ErrorCodeReply errorReply = new ErrorCodeReply(channel);
            errorReply.readFromChannel();
            if (errorReply.errorCode == ErrorCode.SERVER_SHUTDOWN) {
                System.out.println("Server is shutting down!");
                return;
            }

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

            ErrorCodeReply reply = new ErrorCodeReply(channel);
            reply.readFromChannel();

            switch (reply.errorCode) {
                case SUCCESS -> System.out.println("Successfully deleted " + args[0]);
                case FILE_NOT_FOUND -> System.out.println("Could not delete " + args[0] + ", file not found");
                case PERMISSION_DENIED -> System.out.println("Could not delete " + args[0] + ", permission denied");
                case SERVER_SHUTDOWN -> System.out.println("Server is shutting down!");
            }
        }
        catch (IOException exception) {
            System.out.println("Failed to allocate channel: " + exception.getMessage());
        }
    }

    private static void rename(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("You must provide the original filename and its new filename.");
        }

        try (SocketChannel channel = SocketChannel.open()){
            channel.connect(serverAddress);

            System.out.println("Requesting to rename " + args[0] + " to " + args[1]);

            RenameRequest request = new RenameRequest(channel);
            request.oldFilename = args[0];
            request.newFilename = args[1];
            request.writeToChannel();
            channel.shutdownOutput();

            ErrorCodeReply reply = new ErrorCodeReply(channel);
            reply.readFromChannel();

            switch (reply.errorCode) {
                case SUCCESS -> System.out.println("Successfully renamed " + args[0] + " to " + args[1]);
                case FILE_NOT_FOUND -> System.out.println("Could not rename " + args[0] + " to " + args[1] + ", file not found");
                case PERMISSION_DENIED -> System.out.println("Could not rename " + args[0] + " to " + args[1] + ", permission denied");
                case SERVER_SHUTDOWN -> System.out.println("Server is shutting down!");
            }
        }
        catch (IOException exception) {
            System.out.println("Failed to allocate channel: " + exception.getMessage());
        }
    }

    private static void download(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("You must provide the name of the file you wish to download.");
        }

        File filepath = new File(CLIENT_FILES, args[0]);
        try (FileOutputStream file = new FileOutputStream(filepath)){
            try (SocketChannel channel = SocketChannel.open()) {
                channel.connect(serverAddress);

                System.out.println("Requesting to download " + args[0]);

                DownloadRequest request = new DownloadRequest(channel);
                request.filename = args[0];
                request.writeToChannel();
                channel.shutdownOutput();

                ErrorCodeReply reply = new ErrorCodeReply(channel);
                reply.readFromChannel();

                if (reply.errorCode != ErrorCode.SUCCESS) {
                    switch (reply.errorCode) {
                        case FILE_NOT_FOUND -> System.out.println(args[0] + " doesn't exist on the server");
                        case PERMISSION_DENIED -> System.out.println("Server denied permission to download " + args[0]);
                        case SERVER_SHUTDOWN -> System.out.println("Server is shutting down!");
                    }
                    return;
                }

                System.out.println("Server approved download");

                try {
                    FileMessage fileMessage = new FileMessage(channel);
                    fileMessage.readFileFromChannel(file);
                }
                catch (IOException exception) {
                    System.out.println("Failed to write downloaded data to " + args[0]);
                    return;
                }

                System.out.println("Downloaded " + args[0]);
            }
            catch (IOException exception) {
                System.out.println("Unable to allocate socket");
            }
        }
        catch (IOException exception) {
            System.out.println("Could not write to file " + args[0]);
        }
    }

    private static void upload(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("You must provide the name of the file you wish to upload.");
        }

        File filepath = new File(CLIENT_FILES, args[0]);
        try (FileInputStream file = new FileInputStream(filepath)){
            try (SocketChannel channel = SocketChannel.open()) {
                channel.connect(serverAddress);

                System.out.println("Requesting to upload " + args[0]);

                UploadRequest request = new UploadRequest(channel);
                request.filename = args[0];
                request.fileSize = filepath.length();
                request.writeToChannel();

                ErrorCodeReply reply = new ErrorCodeReply(channel);
                reply.readFromChannel();

                if (reply.errorCode == ErrorCode.FILE_TOO_LARGE) {
                    System.out.println("Server rejected file transfer: File too large");
                    return;
                }

                System.out.println("Server authorized upload of " + args[0]);

                FileMessage fileMessage = new FileMessage(channel);
                fileMessage.writeFileToChannel(file);
                channel.shutdownOutput();

                reply.readFromChannel();
                switch (reply.errorCode) {
                    case SUCCESS -> System.out.println("Successfully uploaded " + args[0]);
                    case FILE_NOT_FOUND -> System.out.println("Server unable to write file " + args[0]);
                    case SERVER_SHUTDOWN -> System.out.println("Server is shutting down!");
                }
            }
            catch (IOException exception) {
                System.out.println("Unable to allocate socket");
            }
        }
        catch (FileNotFoundException exception) {
            System.out.println("Could not find file " + args[0]);
        }
        catch (IOException exception) {
            System.out.println("Could not access file " + args[0]);
        }
    }

    private static void quit(String[] args) {
        System.exit(0);
    }
}
