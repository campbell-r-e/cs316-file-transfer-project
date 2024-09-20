package filetransfer.shared.message;

import filetransfer.shared.CommandID;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ListRequest extends FTMessage {
    public ListRequest(SocketChannel channel) {
        super(channel);
    }

    @Override
    public void readFromChannel() throws IOException{

    }

    @Override
    public void writeToChannel() throws IOException {
        byte[] contents = new byte[1];
        contents[0] = CommandID.LIST.value;

        ByteBuffer requestBuffer = ByteBuffer.wrap(contents);
        channel.write(requestBuffer);
    }
}
