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
        System.out.println("Count: " + filenameCount);
        filenames.ensureCapacity(filenameCount);

        for (int filenameIndex = 0; filenameIndex < filenameCount; filenameIndex++) {
            ByteBuffer filenameLengthBuffer = ByteBuffer.allocate(4);
            channel.read(filenameLengthBuffer);
            filenameCountBuffer.flip();
            int filenameLength = filenameCountBuffer.getInt();
            System.out.println("Length: " + filenameCount);

            byte[] rawFilename = new byte[filenameLength];
            ByteBuffer filenameBuffer = ByteBuffer.wrap(rawFilename);
            channel.read(filenameBuffer);
            filenameBuffer.flip();
            String filename = new String(rawFilename);
            filenames.add(filename);
            System.out.println("Name: " + filename);
        }
    }

    @Override
    public void writeToChannel() throws IOException {
        ByteBuffer filenameCountBuffer = ByteBuffer.allocate(4);
        filenameCountBuffer.putInt(filenames.size());
        channel.write(filenameCountBuffer);

        for (int filenameIndex = 0; filenameIndex < filenames.size(); filenameIndex++) {
            String filename = filenames.get(filenameIndex);
            byte[] filenameBytes = filename.getBytes();

            ByteBuffer filenameLengthBuffer = ByteBuffer.allocate(4);
            filenameLengthBuffer.putInt(filenameBytes.length);
            channel.write(filenameLengthBuffer);

            ByteBuffer filenameBuffer = ByteBuffer.wrap(filenameBytes);
            channel.write(filenameBuffer);
        }
    }
}
