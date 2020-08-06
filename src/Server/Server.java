package Server;

import Message.Message;

import java.io.*;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;

public class Server implements Runnable{
    private static int port = 8000;
    private static int bufferSize = 1024;
    private static int timeOut = 3000;
    private static HashMap<String, Class> registryCenter = new HashMap<>();
    private static Object result;

    public void registry(String classInterfaceName, Class classImp){
        registryCenter.put(classInterfaceName, classImp);
    }

    public static void handleAccept(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
        socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ, byteBuffer);
    }

    public static void handleRead(SelectionKey selectionKey) throws Exception {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
        byteBuffer.clear();
        while (socketChannel.read(byteBuffer) > 0){
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer.array());
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Message message = (Message) objectInputStream.readObject();
            result = getResultByParams(message);
            socketChannel.register(selectionKey.selector(), SelectionKey.OP_WRITE, byteBuffer);
        }
    }

    public static void handleWrite(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
//        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
//        byteBuffer.clear();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(result);
//        byteBuffer.put(byteArrayOutputStream.toByteArray());
        socketChannel.write(ByteBuffer.wrap(byteArrayOutputStream.toByteArray()));
        socketChannel.close();
    }

    public static Object getResultByParams(Message message) throws Exception{
        Class serverClass = registryCenter.get(message.getName());
        Method method = serverClass.getMethod(message.getMethod(), message.getParameterstype());
        Object result = method.invoke(serverClass.newInstance(), message.getParameters());
        return result;
    }


    @Override
    public void run() {
        try {
            // 1.创建Selector
            Selector selector = Selector.open();
            // 2.通过ServerSocketChannel创建channel
            ServerSocketChannel serverSocketChannel =ServerSocketChannel.open();
            // 3.为channel绑定监听端口
            serverSocketChannel.bind(new InetSocketAddress(port));
            // 4.设置channel为非阻塞
            serverSocketChannel.configureBlocking(false);
            // 5.将channel注册到Selector上，监听连接事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务器启动成功！");
            while (true){
//                if (selector.select(timeOut) == 0)
//                    continue;
                selector.select();
                Iterator<SelectionKey> selectionKeyIterator =selector.selectedKeys().iterator();
                while (selectionKeyIterator.hasNext()){
                    SelectionKey selectionKey = selectionKeyIterator.next();
                    selectionKeyIterator.remove();
                    if (selectionKey.isAcceptable()){
                        // 建立连接
                        System.out.println("建立连接");
                        handleAccept(selectionKey);
                    }
                    if (selectionKey.isReadable()){
                        // 接收请求
                        System.out.println("接收请求");
                        handleRead(selectionKey);
                    }
                    if (selectionKey.isWritable()){
                        // 返回结果
                        System.out.println("返回结果");
                        handleWrite(selectionKey);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
