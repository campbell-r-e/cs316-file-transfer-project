package filetransfer.shared;

public enum ErrorCode {
    SUCCESS((byte) 0x00),
    FILE_NOT_FOUND((byte) 0x01),
    PERMISSION_DENIED((byte) 0x02);

    public final byte value;

    ErrorCode(byte value) {
        this.value = value;
    }

    public static ErrorCode fromByte(byte b) {
        for (ErrorCode code: ErrorCode.values()) {
            if (code.value == b) {
                return code;
            }
        }

        throw new IllegalArgumentException("The provided byte " + b + " is an invalid ErrorCode");
    }
}
