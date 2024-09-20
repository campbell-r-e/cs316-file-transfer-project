package filetransfer.server;

import filetransfer.shared.message.ListReply;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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

        switch (commandID[0]) {
            case (byte) 0x00 -> handleListRequest(channel);
            case (byte) 0x01 -> handleDeleteRequest(channel);
            case (byte) 0x02 -> handleRenameRequest(channel);
            case (byte) 0x03 -> handleDownloadRequest(channel);
            case (byte) 0x04 -> handleUploadRequest(channel);
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
    }

    private static void handleRenameRequest(SocketChannel channel) throws IOException {
        System.out.println("Received rename request");
    }

    private static void handleDownloadRequest(SocketChannel channel) throws IOException {
        System.out.println("Received download request");
    }

    private static void handleUploadRequest(SocketChannel channel) throws IOException {
        System.out.println("Received upload request");
    }
}
