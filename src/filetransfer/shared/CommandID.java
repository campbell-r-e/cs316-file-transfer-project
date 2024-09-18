package filetransfer.shared;

public enum CommandID {
    LIST(0x00),
    DELETE(0x01),
    RENAME(0x02),
    DOWNLOAD(0x03),
    UPLOAD(0x04);


    public final int value;

    CommandID(int value) {
        this.value = value;
    }
}
