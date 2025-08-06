package com.mq.mqaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName：FileBasedChatMemory
 * Package:com.mq.mqaiagent.chatmemory
 * Description: 基于文件持久化的对话记忆
 * Author：MQQQ
 *
 * @Create:2025/6/19 - 17:51
 * @Version:v1.0
 */
public class FileBasedChatMemory implements ChatMemory {

    private final String BASE_DIR;
    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        // 设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    // 构造对象时，指定文件保存目录
    public FileBasedChatMemory(String dir) {
        this.BASE_DIR = dir;
        File baseDir = new File(dir);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> conversationMessages = getOrCreateConversation(conversationId);
        conversationMessages.addAll(messages);
        saveConversation(conversationId, conversationMessages);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<Message> allMessages = getOrCreateConversation(conversationId);
        return allMessages.stream()
                .skip(Math.max(0, allMessages.size() - lastN))
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 获取对话，如果不存在则创建一个空的对话
     * @param conversationId
     * @return
     */
    private List<Message> getOrCreateConversation(String conversationId) {
        // 根据conversationId获取对应的文件
        File file = getConversationFile(conversationId);
        // 创建一个空的Message列表
        List<Message> messages = new ArrayList<>();
        // 如果文件存在
        if (file.exists()) {
            try (Input input = new Input(new FileInputStream(file))) {
                // 使用kryo读取文件中的对象，并将其转换为ArrayList<Message>类型
                messages = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                // 打印异常信息
                e.printStackTrace();
            }
        }
        // 返回Message列表
        return messages;
    }

    /**
     * 保存对话
     *
     * @param conversationId
     * @param messages
     */
    private void saveConversation(String conversationId, List<Message> messages) {
        // 获取对话文件
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            // 使用kryo序列化消息列表
            kryo.writeObject(output, messages);
        } catch (IOException e) {
            // 打印异常信息
            e.printStackTrace();
        }
    }

    /**
     * 获取对话文件
     *
     * @param conversationId
     * @return
     */
    private File getConversationFile(String conversationId) {
        return new File(BASE_DIR, conversationId + ".kryo");
    }
}
