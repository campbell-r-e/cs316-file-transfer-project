package filetransfer.shared.message;

import filetransfer.shared.CommandID;
import filetransfer.shared.FTMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class UploadRequest extends FTMessage {
    public String filename;
    public long fileSize;

    public UploadRequest(SocketChannel channel) {
        super(channel);
    }

    @Override
    public void readFromChannel() throws IOException {
        ByteBuffer filenameLengthBuffer = ByteBuffer.allocate(Byte.BYTES);
        channel.read(filenameLengthBuffer);
        filenameLengthBuffer.flip();

        byte[] rawFilename = new byte[filenameLengthBuffer.get()];
        ByteBuffer filenameBuffer = ByteBuffer.wrap(rawFilename);
        channel.read(filenameBuffer);
        filename = new String(rawFilename);

        ByteBuffer fileSizeBuffer = ByteBuffer.allocate(Long.BYTES);
        channel.read(fileSizeBuffer);
        fileSizeBuffer.flip();
        fileSize = fileSizeBuffer.getLong();
    }

    @Override
    public void writeToChannel() throws IOException {
        ByteBuffer commandIDBuffer = ByteBuffer.allocate(Byte.BYTES);
        commandIDBuffer.put(CommandID.UPLOAD.value);
        commandIDBuffer.flip();
        channel.write(commandIDBuffer);

        ByteBuffer filenameLengthBuffer = ByteBuffer.allocate(Byte.BYTES);
        filenameLengthBuffer.put((byte) filename.length());
        filenameLengthBuffer.flip();
        channel.write(filenameLengthBuffer);

        byte[] rawFilename = filename.getBytes();
        ByteBuffer filenameBuffer = ByteBuffer.wrap(rawFilename);
        channel.write(filenameBuffer);

        ByteBuffer fileSizeBuffer = ByteBuffer.allocate(Long.BYTES);
        fileSizeBuffer.putLong(fileSize);
        fileSizeBuffer.flip();
        channel.write(fileSizeBuffer);
    }
}
