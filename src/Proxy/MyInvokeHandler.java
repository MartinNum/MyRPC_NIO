package Proxy;

import Message.Message;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class MyInvokeHandler implements InvocationHandler {
    private Class object;
    Object result = null;
    int post = 8000;
    int bufferSize = 1024;

    public MyInvokeHandler(Class object){
        this.object = object;
    }

    public void handleWrite(SelectionKey selectionKey, Method method, Object[] args) throws IOException {
        System.out.println("客户端发送请求");
        Message message = new Message();
        message.setName(object.getName());
        message.setMethod(method.getName());
        message.setParameterstype(method.getParameterTypes());
        message.setParameters(args);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(message);
        // 发送请求
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        socketChannel.write(ByteBuffer.wrap(byteArrayOutputStream.toByteArray()));
        // 发送完请求后将通道注册为可读事件
        socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ, selectionKey.attachment());
    }

    public void handleRead(SelectionKey selectionKey) throws Exception {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
        byteBuffer.clear();
        // 接收服务端应答
        while (socketChannel.read(byteBuffer) > 0){
            System.out.println("接收服务端应答");
            byteBuffer.flip();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer.array());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            result = objectInputStream.readObject();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Selector selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", post));
        socketChannel.configureBlocking(false);
        // 将通道注册为可写事件
        socketChannel.register(selector, SelectionKey.OP_WRITE, ByteBuffer.allocate(bufferSize));

        while (true){
            selector.select();
            Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
            while (selectionKeyIterator.hasNext()){
                SelectionKey selectionKey = selectionKeyIterator.next();
                // 可写事件
                if (selectionKey.isWritable()){
                    handleWrite(selectionKey, method, args);
                }
                // 可读事件
                if (selectionKey.isReadable()){
                    handleRead(selectionKey);
                    return result;
                }
            }
        }
    }
}
