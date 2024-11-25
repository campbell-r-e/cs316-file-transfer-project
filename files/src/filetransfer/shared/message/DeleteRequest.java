package filetransfer.shared.message;

import filetransfer.shared.CommandID;
import filetransfer.shared.FTMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class DeleteRequest extends FTMessage {
    public String filename;

    public DeleteRequest(SocketChannel channel) {
        super(channel);
    }

    @Override
    public void readFromChannel() throws IOException {
        ByteBuffer filenameLengthBuffer = ByteBuffer.allocate(1);
        channel.read(filenameLengthBuffer);
        filenameLengthBuffer.flip();
        int filenameLength = filenameLengthBuffer.get();

        byte[] rawFilename = new byte[filenameLength];
        ByteBuffer filenameBuffer = ByteBuffer.wrap(rawFilename);
        channel.read(filenameBuffer);
        filename = new String(rawFilename);
    }

    @Override
    public void writeToChannel() throws IOException {
        byte[] commandID = new byte[1];
        commandID[0] = CommandID.DELETE.value;

        ByteBuffer requestBuffer = ByteBuffer.wrap(commandID);
        channel.write(requestBuffer);

        byte[] rawFilename = filename.getBytes();
        ByteBuffer filenameLengthBuffer = ByteBuffer.allocate(1);
        filenameLengthBuffer.put((byte) rawFilename.length);
        filenameLengthBuffer.flip();
        channel.write(filenameLengthBuffer);

        ByteBuffer filenameBuffer = ByteBuffer.wrap(rawFilename);
        channel.write(filenameBuffer);

    }
}
