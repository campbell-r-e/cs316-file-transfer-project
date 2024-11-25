package filetransfer.shared.message;

import filetransfer.shared.FTMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class ListReply extends FTMessage {
    public ArrayList<String> filenames = new ArrayList<>();

    public ListReply(SocketChannel channel) {
        super(channel);
    }

    @Override
    public void readFromChannel() throws IOException {
        ByteBuffer filenameCountBuffer = ByteBuffer.allocate(1);
        channel.read(filenameCountBuffer);
        filenameCountBuffer.flip();
        int filenameCount = filenameCountBuffer.get();
        filenames.ensureCapacity(filenameCount);

        for (int filenameIndex = 0; filenameIndex < filenameCount; filenameIndex++) {
            ByteBuffer filenameLengthBuffer = ByteBuffer.allocate(1);
            channel.read(filenameLengthBuffer);
            filenameLengthBuffer.flip();
            int filenameLength = filenameLengthBuffer.get();

            byte[] rawFilename = new byte[filenameLength];
            ByteBuffer filenameBuffer = ByteBuffer.wrap(rawFilename);
            // Consider using a while, read can read a *maximum* of the buffer size.
            channel.read(filenameBuffer);
            filenameBuffer.flip();
            String filename = new String(rawFilename);
            filenames.add(filename);
        }
    }

    @Override
    public void writeToChannel() throws IOException {
        ByteBuffer filenameCountBuffer = ByteBuffer.allocate(1);
        filenameCountBuffer.put((byte) filenames.size());
        filenameCountBuffer.flip();
        channel.write(filenameCountBuffer);

        for (String filename : filenames) {
            byte[] filenameBytes = filename.getBytes();

            ByteBuffer filenameLengthBuffer = ByteBuffer.allocate(1);
            filenameLengthBuffer.put((byte) filenameBytes.length);
            filenameLengthBuffer.flip();
            channel.write(filenameLengthBuffer);

            ByteBuffer filenameBuffer = ByteBuffer.wrap(filenameBytes);
            channel.write(filenameBuffer);
        }
    }
}
