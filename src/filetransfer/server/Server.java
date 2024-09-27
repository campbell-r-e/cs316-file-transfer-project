package filetransfer.server;

import filetransfer.shared.CommandID;
import filetransfer.shared.ErrorCode;
import filetransfer.shared.message.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Objects;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Server {
    private static final InetSocketAddress ADDRESS = new InetSocketAddress(3001);
    private static final File FILES_DIRECTORY = new File("server_files");
    private static final long MEBIBYTE = 1024 * 1024;
    private static final long MAX_FILE_SIZE = 1024 * MEBIBYTE;

    public static void main(String[] args) {
        try (ServerSocketChannel serveChannel = ServerSocketChannel.open()) {
            serveChannel.bind(ADDRESS);

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

        for (File file: Objects.requireNonNull(FILES_DIRECTORY.listFiles())) {
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

        Path filepath = FILES_DIRECTORY.toPath().resolve(Path.of(request.filename));
        ErrorCode errorCode = attemptDelete(filepath);

        ErrorCodeReply reply = new ErrorCodeReply(channel);
        reply.errorCode = errorCode;
        reply.writeToChannel();
    }

    private static void handleRenameRequest(SocketChannel channel) throws IOException {
        System.out.println("Received rename request");

        RenameRequest request = new RenameRequest(channel);
        request.readFromChannel();
        System.out.println("Client requested to rename " + request.oldFilename + " -> " + request.newFilename);

        Path oldFilepath = FILES_DIRECTORY.toPath().resolve(Path.of(request.oldFilename));
        Path newFilepath = FILES_DIRECTORY.toPath().resolve(Path.of(request.newFilename));

        ErrorCode error = attemptRename(oldFilepath, newFilepath);
        ErrorCodeReply reply = new ErrorCodeReply(channel);
        reply.errorCode = error;
        reply.writeToChannel();
    }

    private static void handleDownloadRequest(SocketChannel channel) throws IOException {
        System.out.println("Received download request");

        DownloadRequest request = new DownloadRequest(channel);
        request.readFromChannel();

        System.out.println("Client requesting to download " + request.filename);

        Path filepath = FILES_DIRECTORY.toPath().resolve(Path.of(request.filename));

        if (isPathOutsideFilesDirectory(filepath)) {
            System.out.println("Rejecting download request: requested file is outside server files");
            ErrorCodeReply reply = new ErrorCodeReply(channel);
            reply.errorCode = ErrorCode.PERMISSION_DENIED;
            reply.writeToChannel();
            return;
        }

        try (FileInputStream fileStream = new FileInputStream(filepath.toFile())) {
            System.out.println("Approved client download request");
            ErrorCodeReply reply = new ErrorCodeReply(channel);
            reply.errorCode = ErrorCode.SUCCESS;
            reply.writeToChannel();

            FileMessage fileMessage = new FileMessage(channel);
            fileMessage.writeFileToChannel(fileStream);

            System.out.println("File uploaded to client");
        }
        catch (FileNotFoundException exception) {
            System.out.println("Rejecting download request: file not found");
            ErrorCodeReply reply = new ErrorCodeReply(channel);
            reply.errorCode = ErrorCode.FILE_NOT_FOUND;
            reply.writeToChannel();
        }
    }

    private static void handleUploadRequest(SocketChannel channel) throws IOException {
        System.out.println("Received upload request");

        UploadRequest request = new UploadRequest(channel);
        request.readFromChannel();

        System.out.println("Client requesting to upload " + request.filename + " with a size of " + request.fileSize + " bytes");

        if (request.fileSize > MAX_FILE_SIZE) {
            System.out.println("Rejecting upload request due to file size exceeding the maximum of " + MAX_FILE_SIZE + " bytes.");

            ErrorCodeReply reply = new ErrorCodeReply(channel);
            reply.errorCode = ErrorCode.FILE_TOO_LARGE;
            reply.writeToChannel();
            return;
        }

        System.out.println("Approved client upload request");
        ErrorCodeReply reply = new ErrorCodeReply(channel);
        reply.errorCode = ErrorCode.SUCCESS;
        reply.writeToChannel();

        // Will force the file to upload to the server files directory.
        Path filepath = FILES_DIRECTORY.toPath().resolve(Path.of(request.filename).getFileName());

        try (FileOutputStream fileStream = new FileOutputStream(filepath.toFile())) {
            FileMessage fileMessage = new FileMessage(channel);
            fileMessage.readFileFromChannel(fileStream);

            System.out.println("Client upload complete");

            ErrorCodeReply uploadReply = new ErrorCodeReply(channel);
            uploadReply.errorCode = ErrorCode.SUCCESS;
            uploadReply.writeToChannel();
        }
        catch (FileNotFoundException exception) {
            System.out.println("Server error: Unable to write to " + filepath.toAbsolutePath());

            ErrorCodeReply uploadReply = new ErrorCodeReply(channel);
            uploadReply.errorCode = ErrorCode.FILE_NOT_FOUND;
            uploadReply.writeToChannel();
        }
    }

    private static ErrorCode attemptDelete(Path filepath) {
        if (isPathOutsideFilesDirectory(filepath)){
            System.out.println("Failed to delete: File outside " + FILES_DIRECTORY.getAbsolutePath());
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

    private static ErrorCode attemptRename(Path oldFilepath, Path newFilepath) {
        if (isPathOutsideFilesDirectory(oldFilepath) || isPathOutsideFilesDirectory(newFilepath)) {
            System.out.println("Failed to move file: File outside " + FILES_DIRECTORY.getAbsolutePath());
            return ErrorCode.PERMISSION_DENIED;
        }

        try {
            Files.move(oldFilepath, newFilepath, REPLACE_EXISTING);
            System.out.println("Moved " + oldFilepath + " to " + newFilepath);
            return ErrorCode.SUCCESS;
        }
        catch (NoSuchFileException exception) {
                System.out.println("Failed to move file: File not found");
                return ErrorCode.FILE_NOT_FOUND;
        }
        catch (IOException exception) {
                System.out.println("Failed to move file: IO Error");
                return ErrorCode.PERMISSION_DENIED;
        }
    }

    private static boolean isPathOutsideFilesDirectory(Path filepath) {
        Path absolutePath = filepath.toAbsolutePath().normalize();
        return !absolutePath.startsWith(FILES_DIRECTORY.toPath().toAbsolutePath());
    }
}
