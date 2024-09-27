package filetransfer.shared.message;

import filetransfer.shared.ErrorCode;
import filetransfer.shared.FTMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ErrorCodeReply extends FTMessage {
    public ErrorCode errorCode;

    public ErrorCodeReply(SocketChannel channel) {
        super(channel);
    }

    @Override
    public void readFromChannel() throws IOException {
        byte[] rawErrorCode = new byte[1];
        ByteBuffer errorBuffer = ByteBuffer.wrap(rawErrorCode);
        channel.read(errorBuffer);
        errorCode = ErrorCode.fromByte(rawErrorCode[0]);
    }

    @Override
    public void writeToChannel() throws IOException {
        ByteBuffer errorBuffer = ByteBuffer.allocate(1);
        errorBuffer.put(errorCode.value);
        errorBuffer.flip();
        channel.write(errorBuffer);
    }
}
