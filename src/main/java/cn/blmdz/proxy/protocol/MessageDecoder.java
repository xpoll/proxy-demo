package cn.blmdz.proxy.protocol;

import cn.blmdz.proxy.enums.MessageType;
import cn.blmdz.proxy.model.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class MessageDecoder extends LengthFieldBasedFrameDecoder {

    private static final int LENGTH_SIZE = 1;
    private static final int TYPE_SIZE = 1;
    

    private static final int MAX_FRAME_LENGTH = 2 * 1024 * 1024;
    private static final int LENGTH_FIELD_OFFSET = 0;
    private static final int LENGTH_FIELD_LENGTH = 4;
    private static final int INITIAL_BYTES_TO_STRIP = 0;
    private static final int LENGTH_ADJUSTMENT = 0;

    public MessageDecoder() {
        super(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, INITIAL_BYTES_TO_STRIP, LENGTH_ADJUSTMENT);
    }

    @Override
    protected Message decode(ChannelHandlerContext ctx, ByteBuf in2) throws Exception {
        ByteBuf in = (ByteBuf) super.decode(ctx, in2);
        if (in == null) return null;

        if (in.readableBytes() < LENGTH_FIELD_LENGTH) return null;

        int length = in.readInt();
        if (in.readableBytes() < length) return null;
        
        byte type = in.readByte();

        byte paramsLength = in.readByte();
        byte[] paramsBytes = new byte[paramsLength];
        in.readBytes(paramsBytes);

        byte[] data = new byte[length - LENGTH_SIZE - TYPE_SIZE - paramsLength];
        in.readBytes(data);
        in.release();
        
        return Message.build(MessageType.conversion(type), new String(paramsBytes), data);
    }
}
