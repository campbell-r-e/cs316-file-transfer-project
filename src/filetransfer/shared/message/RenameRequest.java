package filetransfer.shared.message;

import filetransfer.shared.CommandID;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class RenameRequest extends FTMessage{
    public String oldFilename;
    public String newFilename;

    public RenameRequest(SocketChannel channel) {
        super(channel);
    }

    @Override
    public void readFromChannel() throws IOException {
        ByteBuffer oldFilenameLengthBuffer = ByteBuffer.allocate(1);
        channel.read(oldFilenameLengthBuffer);
        oldFilenameLengthBuffer.flip();

        byte[] rawOldFilename = new byte[oldFilenameLengthBuffer.get()];
        ByteBuffer oldFilenameBuffer = ByteBuffer.wrap(rawOldFilename);
        channel.read(oldFilenameBuffer);
        oldFilename = new String(rawOldFilename);

        ByteBuffer newFilenameLengthBuffer = ByteBuffer.allocate(1);
        channel.read(newFilenameLengthBuffer);
        newFilenameLengthBuffer.flip();

        byte[] rawNewFilename = new byte[newFilenameLengthBuffer.get()];
        ByteBuffer newFilenameBuffer = ByteBuffer.wrap(rawNewFilename);
        channel.read(newFilenameBuffer);
        newFilename = new String(rawNewFilename);
    }

    @Override
    public void writeToChannel() throws IOException {
        ByteBuffer commandIDBuffer = ByteBuffer.allocate(1);
        commandIDBuffer.put(CommandID.RENAME.value);
        commandIDBuffer.flip();
        channel.write(commandIDBuffer);

        byte[] rawOldFilename = oldFilename.getBytes();

        ByteBuffer oldFilenameLengthBuffer = ByteBuffer.allocate(1);
        oldFilenameLengthBuffer.put((byte) rawOldFilename.length);
        oldFilenameLengthBuffer.flip();
        channel.write(oldFilenameLengthBuffer);

        ByteBuffer oldFilenameBuffer = ByteBuffer.wrap(rawOldFilename);
        channel.write(oldFilenameBuffer);

        byte[] rawNewFilename = newFilename.getBytes();

        ByteBuffer newFilenameLengthBuffer = ByteBuffer.allocate(1);
        newFilenameLengthBuffer.put((byte) rawNewFilename.length);
        newFilenameLengthBuffer.flip();
        channel.write(newFilenameLengthBuffer);

        ByteBuffer newFilenameBuffer = ByteBuffer.wrap(rawNewFilename);
        channel.write(newFilenameBuffer);
    }
}
