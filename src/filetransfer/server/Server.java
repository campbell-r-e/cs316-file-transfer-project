package filetransfer.server;

import filetransfer.shared.CommandID;
import filetransfer.shared.ErrorCode;
import filetransfer.shared.message.DeleteReply;
import filetransfer.shared.message.DeleteRequest;
import filetransfer.shared.message.ListReply;
import filetransfer.shared.message.RenameRequest;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Objects;

public class Server {
    private static final InetSocketAddress address = new InetSocketAddress(3001);
    private static final File filesDirectory = new File("server_files");

    public static void main(String[] args) {
        try (ServerSocketChannel serveChannel = ServerSocketChannel.open()) {
            serveChannel.bind(address);

            while (true){
                try (SocketChannel channel = serveChannel.accept()){
                    handleRequests(channel);
                }
            }
        }
        catch(IOException exception) {
            System.out.println("Unable to open socket at port 3001.");
        }
    }

    private static void handleRequests(SocketChannel channel) throws IOException{
        ByteBuffer commandIDBuffer = ByteBuffer.allocate(1);
        channel.read(commandIDBuffer);
        byte[] commandID = new byte[1];
        commandIDBuffer.flip();
        commandIDBuffer.get(commandID);

        switch (CommandID.fromByte(commandID[0])) {
            case LIST -> handleListRequest(channel);
            case DELETE -> handleDeleteRequest(channel);
            case RENAME -> handleRenameRequest(channel);
            case DOWNLOAD -> handleDownloadRequest(channel);
            case UPLOAD -> handleUploadRequest(channel);
        }
    }

    private static void handleListRequest(SocketChannel channel) throws IOException {
        System.out.println("Received list request");

        ListReply reply = new ListReply(channel);

        for (File file: Objects.requireNonNull(filesDirectory.listFiles())) {
            reply.filenames.add(file.getName());
        }

        reply.writeToChannel();
        channel.shutdownOutput();
        System.out.println("Handled list request");
    }

    private static void handleDeleteRequest(SocketChannel channel) throws IOException {
        System.out.println("Received delete request");

        DeleteRequest request = new DeleteRequest(channel);
        request.readFromChannel();
        System.out.println("Client requested to delete file: " + request.filename);

        Path filepath = filesDirectory.toPath().resolve(Path.of(request.filename));
        ErrorCode errorCode = attemptDelete(filepath);

        DeleteReply reply = new DeleteReply(channel);
        reply.errorCode = errorCode;
        reply.writeToChannel();
    }

    private static void handleRenameRequest(SocketChannel channel) throws IOException {
        System.out.println("Received rename request");

        RenameRequest request = new RenameRequest(channel);
        request.readFromChannel();
        System.out.println("Client requested to rename " + request.oldFilename + " -> " + request.newFilename);

        Path oldFilepath = filesDirectory.toPath().resolve(Path.of(request.oldFilename));
        Path newFilepath = filesDirectory.toPath().resolve(Path.of(request.newFilename));

        ErrorCode[] errorCodes = attemptRename(oldFilepath, newFilepath);
    }

    private static void handleDownloadRequest(SocketChannel channel) throws IOException {
        System.out.println("Received download request");
    }

    private static void handleUploadRequest(SocketChannel channel) throws IOException {
        System.out.println("Received upload request");
    }

    private static ErrorCode attemptDelete(Path filepath) {
        if (!isPathInFilesDirectory(filepath)){
            System.out.println("Failed to delete: File outside " + filesDirectory.getAbsolutePath());
            return ErrorCode.PERMISSION_DENIED;
        }

        try {
            Files.delete(filepath);
            System.out.println("Successfully deleted");
            return ErrorCode.SUCCESS;
        }
        catch (NoSuchFileException exception) {
            System.out.println("Failed to delete: File not found");
            return ErrorCode.FILE_NOT_FOUND;
        }
        catch (IOException exception) {
            System.out.println("Failed to delete: IO Error");
            return ErrorCode.PERMISSION_DENIED;
        }
    }

    private static ErrorCode[] attemptRename(Path filepath) {

    }

    private static boolean isPathInFilesDirectory(Path filepath) {
        Path absolutePath = filepath.toAbsolutePath().normalize();
        System.out.println("Path: " + absolutePath);
        return absolutePath.startsWith(filesDirectory.toPath().toAbsolutePath());
    }
}
