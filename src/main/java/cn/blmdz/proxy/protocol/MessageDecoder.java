package cn.blmdz.proxy.protocol;

import cn.blmdz.proxy.enums.MessageType;
import cn.blmdz.proxy.model.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class MessageDecoder extends LengthFieldBasedFrameDecoder {

    private static final byte HEADER_SIZE = 4;
    private static final int TYPE_SIZE = 1;
//    private static final int SERIAL_NUMBER_SIZE = 8;
    private static final int URI_LENGTH_SIZE = 1;

    public MessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
            int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    public MessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
            int initialBytesToStrip, boolean failFast) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }


    @Override
    protected Message decode(ChannelHandlerContext ctx, ByteBuf in2) throws Exception {
        ByteBuf in = (ByteBuf) super.decode(ctx, in2);
        if (in == null) {
            return null;
        }

        if (in.readableBytes() < HEADER_SIZE) {
            return null;
        }

        int frameLength = in.readInt();
        if (in.readableBytes() < frameLength) {
            return null;
        }
        Message msg = new Message();
        byte type = in.readByte();
//        long sn = in.readLong();
//        msg.setSerialNumber(sn);

        msg.setType(MessageType.conversion(type));

        byte uriLength = in.readByte();
        byte[] uriBytes = new byte[uriLength];
        in.readBytes(uriBytes);
        msg.setParams(new String(uriBytes));

        byte[] data = new byte[frameLength - TYPE_SIZE /*- SERIAL_NUMBER_SIZE */- URI_LENGTH_SIZE - uriLength];
        in.readBytes(data);
        msg.setData(data);

        in.release();
        return msg;
    }
}
