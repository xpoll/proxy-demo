package cn.blmdz.proxy.protocol;

import cn.blmdz.proxy.model.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoder extends MessageToByteEncoder<Message> {

    private static final int LENGTH_SIZE = 1;
    private static final int TYPE_SIZE = 1;

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
//        System.out.println(System.currentTimeMillis() + ": " + Thread.currentThread().getStackTrace()[1]);
//        System.out.println(JSON.toJSONString(msg));
        int bodyLength = LENGTH_SIZE + TYPE_SIZE;
        byte[] paramsBytes = null;
        if (msg.getParams() != null) {
            paramsBytes = msg.getParams().getBytes();
            bodyLength += paramsBytes.length;
        }

        if (msg.getData() != null) {
            bodyLength += msg.getData().length;
        }

        out.writeInt(bodyLength);
        out.writeByte(msg.getType().value());
        if (paramsBytes != null) {
            out.writeByte((byte) paramsBytes.length);
            out.writeBytes(paramsBytes);
        } else {
            out.writeByte((byte) 0x00);
        }

        if (msg.getData() != null) {
            out.writeBytes(msg.getData());
        }
    }

}
