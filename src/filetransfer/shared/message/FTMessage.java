package filetransfer.shared.message;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public abstract class FTMessage {
    protected SocketChannel channel;

    public FTMessage(SocketChannel channel) {
        this.channel = channel;
    }

    public abstract void readFromChannel() throws IOException;
    public abstract void writeToChannel() throws IOException;
}
