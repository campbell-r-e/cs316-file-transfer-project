package filetransfer.shared;

public enum CommandID {
    LIST((byte) 0x00),
    DELETE((byte) 0x01),
    RENAME((byte) 0x02),
    DOWNLOAD((byte) 0x03),
    UPLOAD((byte) 0x04);

    public final byte value;

    CommandID(byte value) {
        this.value = value;
    }

    public static CommandID fromByte(byte b) {
        for (CommandID code: CommandID.values()) {
            if (code.value == b) {
                return code;
            }
        }

        throw new IllegalArgumentException("The provided byte " + b + " is an invalid CommandID");
    }
}
