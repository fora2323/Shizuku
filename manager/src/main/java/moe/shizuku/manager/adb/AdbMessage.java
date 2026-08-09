package moe.shizuku.manager.adb;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class AdbMessage {

    public final int command;
    public final int arg0;
    public final int arg1;
    public final int data_length;
    public final int data_crc32;
    public final int magic;
    public final byte[] data;

    public AdbMessage(int command, int arg0, int arg1, int data_length, int data_crc32, int magic, byte[] data) {
        this.command = command;
        this.arg0 = arg0;
        this.arg1 = arg1;
        this.data_length = data_length;
        this.data_crc32 = data_crc32;
        this.magic = magic;
        this.data = data;
    }

    public AdbMessage(int command, int arg0, int arg1, String data) {
        this(command, arg0, arg1, ("" + data + "\u0000").getBytes());
    }

    public AdbMessage(int command, int arg0, int arg1, byte[] data) {
        this(command,
                arg0,
                arg1,
                data != null ? data.length : 0,
                crc32(data),
                (int) ( (long)command ^ 0xFFFFFFFFL ),
                data);
    }

    public boolean validate() {
        if (command != (magic ^ -0x1)) return false;
        if (data_length != 0 && crc32(data) != data_crc32) return false;
        return true;
    }

    public void validateOrThrow() {
        if (!validate()) throw new IllegalArgumentException("bad message " + toStringShort());
    }

    public byte[] toByteArray() {
        int length = HEADER_LENGTH + (data != null ? data.length : 0);
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(command);
        buffer.putInt(arg0);
        buffer.putInt(arg1);
        buffer.putInt(data_length);
        buffer.putInt(data_crc32);
        buffer.putInt(magic);
        if (data != null) buffer.put(data);
        return buffer.array();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        AdbMessage o = (AdbMessage) other;
        if (command != o.command) return false;
        if (arg0 != o.arg0) return false;
        if (arg1 != o.arg1) return false;
        if (data_length != o.data_length) return false;
        if (data_crc32 != o.data_crc32) return false;
        if (magic != o.magic) return false;
        if (!Arrays.equals(data, o.data)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = command;
        result = 31 * result + arg0;
        result = 31 * result + arg1;
        result = 31 * result + data_length;
        result = 31 * result + data_crc32;
        result = 31 * result + magic;
        result = 31 * result + (data != null ? Arrays.hashCode(data) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AdbMessage(" + toStringShort() + ")";
    }

    public String toStringShort() {
        String commandString;
        switch (command) {
            case AdbProtocol.A_SYNC: commandString = "A_SYNC"; break;
            case AdbProtocol.A_CNXN: commandString = "A_CNXN"; break;
            case AdbProtocol.A_AUTH: commandString = "A_AUTH"; break;
            case AdbProtocol.A_OPEN: commandString = "A_OPEN"; break;
            case AdbProtocol.A_OKAY: commandString = "A_OKAY"; break;
            case AdbProtocol.A_CLSE: commandString = "A_CLSE"; break;
            case AdbProtocol.A_WRTE: commandString = "A_WRTE"; break;
            case AdbProtocol.A_STLS: commandString = "A_STLS"; break;
            default: commandString = Integer.toString(command); break;
        }
        return "command=" + commandString + ", arg0=" + arg0 + ", arg1=" + arg1 + ", data_length=" + data_length + ", data_crc32=" + data_crc32 + ", magic=" + magic + ", data=" + (data != null ? Arrays.toString(data) : "null");
    }

    public static final int HEADER_LENGTH = 24;

    private static int crc32(byte[] data) {
        if (data == null) return 0;
        int res = 0;
        for (byte b : data) {
            if (b >= 0) res += b;
            else res += b + 256;
        }
        return res;
    }
}
