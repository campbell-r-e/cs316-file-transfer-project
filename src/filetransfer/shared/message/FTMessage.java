package filetransfer.shared.message;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public abstract class FTMessage {
    protected SocketChannel channel;

    public FTMessage(SocketChannel channel) {
        this.channel = channel;
    }

    abstract void readFromChannel() throws IOException;
    abstract void writeToChannel() throws IOException;
}
