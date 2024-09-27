package filetransfer.shared.message;

import filetransfer.shared.CommandID;
import filetransfer.shared.FTMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class DownloadRequest extends FTMessage {
    public String filename;

    public DownloadRequest(SocketChannel channel) {
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
    }

    @Override
    public void writeToChannel() throws IOException {
        ByteBuffer commandIDBuffer = ByteBuffer.allocate(Byte.BYTES);
        commandIDBuffer.put(CommandID.DOWNLOAD.value);
        commandIDBuffer.flip();
        channel.write(commandIDBuffer);

        ByteBuffer filenameLengthBuffer = ByteBuffer.allocate(Byte.BYTES);
        filenameLengthBuffer.put((byte) filename.length());
        filenameLengthBuffer.flip();
        channel.write(filenameLengthBuffer);

        byte[] rawFilename = filename.getBytes();
        ByteBuffer filenameBuffer = ByteBuffer.wrap(rawFilename);
        channel.write(filenameBuffer);
    }
}
