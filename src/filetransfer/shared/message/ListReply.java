package filetransfer.shared.message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class ListReply extends FTMessage{
    public ArrayList<String> filenames = new ArrayList<>();

    public ListReply(SocketChannel channel) {
        super(channel);
    }

    @Override
    public void readFromChannel() throws IOException {
        ByteBuffer filenameCountBuffer = ByteBuffer.allocate(4);
        channel.read(filenameCountBuffer);
        filenameCountBuffer.flip();
        int filenameCount = filenameCountBuffer.getInt();
        filenames.ensureCapacity(filenameCount);

        for (int filenameIndex = 0; filenameIndex < filenameCount; filenameIndex++) {
            ByteBuffer filenameLengthBuffer = ByteBuffer.allocate(4);
            channel.read(filenameLengthBuffer);
            filenameLengthBuffer.flip();
            int filenameLength = filenameLengthBuffer.getInt();

            byte[] rawFilename = new byte[filenameLength];
            ByteBuffer filenameBuffer = ByteBuffer.wrap(rawFilename);
            channel.read(filenameBuffer);
            filenameBuffer.flip();
            String filename = new String(rawFilename);
            filenames.add(filename);
        }
    }

    @Override
    public void writeToChannel() throws IOException {
        ByteBuffer filenameCountBuffer = ByteBuffer.allocate(4);
        filenameCountBuffer.putInt(filenames.size());
        filenameCountBuffer.flip();
        channel.write(filenameCountBuffer);

        for (String filename : filenames) {
            byte[] filenameBytes = filename.getBytes();

            ByteBuffer filenameLengthBuffer = ByteBuffer.allocate(4);
            filenameLengthBuffer.putInt(filenameBytes.length);
            filenameLengthBuffer.flip();
            channel.write(filenameLengthBuffer);

            ByteBuffer filenameBuffer = ByteBuffer.wrap(filenameBytes);
            channel.write(filenameBuffer);
        }
    }
}
