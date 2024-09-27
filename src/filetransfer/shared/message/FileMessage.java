package filetransfer.shared.message;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class FileMessage {
    private static final int FILE_CHUNK_MAX_SIZE = 1024;

    private final SocketChannel channel;

    public FileMessage(SocketChannel channel) {
        this.channel = channel;
    }

    public void writeFileToChannel(FileInputStream fileStream) throws IOException {
        byte[] fileData = new byte[FILE_CHUNK_MAX_SIZE];
        int fileBytesRead;
        while ((fileBytesRead = fileStream.read(fileData)) != -1){
            ByteBuffer fileBytesBuffer = ByteBuffer.wrap(fileData, 0, fileBytesRead);
            channel.write(fileBytesBuffer);
        }
    }

    public void readFileFromChannel(FileOutputStream fileStream) throws IOException {
        ByteBuffer fileBytesBuffer = ByteBuffer.allocate(FILE_CHUNK_MAX_SIZE);
        int fileBytesRead;
        while ((fileBytesRead = channel.read(fileBytesBuffer)) != -1) {
            fileBytesBuffer.flip();
            byte[] fileBytes = new byte[fileBytesRead];
            fileBytesBuffer.get(fileBytes);
            fileStream.write(fileBytes);
            fileBytesBuffer.clear();
        }
    }
}
