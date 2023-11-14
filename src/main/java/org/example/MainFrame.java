package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainFrame {
    private List<Dialog> dialogs;
    private DefaultListModel<Dialog> listModel;
    private JList<Dialog> messageList;
    private JTextArea messageTextArea;
    private JTextArea responseArea;
    private JFrame frame;
    private JPanel panel;

    public MainFrame() {
        dialogs = new ArrayList<>();
        listModel = new DefaultListModel<>();
        frame = new JFrame("Two Windows App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        panel = new JPanel(new GridLayout(1, 2));

        createMessagesWindow();
        createResponsesWindow();

        frame.add(panel);
        // Загрузка существующих сообщений из JSON
        String relativePath = "src/main/data/Dialogue.json";
        loadMessagesFromJson(relativePath);
    }
    public void loadMessagesFromJson(String filePath) {
        try {
            Gson gson = new Gson();
            FileReader reader = new FileReader(filePath);
            Type dialogListType = new TypeToken<List<Dialog>>(){}.getType();
            List<Dialog> loadedDialogs = gson.fromJson(reader, dialogListType);

            if (loadedDialogs != null) {
                dialogs.addAll(loadedDialogs);
                for (Dialog dialog : dialogs) {
                    listModel.addElement(dialog);
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createMessagesWindow() {
        messageList = new JList<>(listModel);
        messageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        messageTextArea = new JTextArea();
        messageTextArea.setEditable(true);

        JTextArea messageInputArea = new JTextArea();
        messageInputArea.setLineWrap(true); // Перенос строки при достижении конца видимой области
        JScrollPane messageInputScrollPane = new JScrollPane(messageInputArea);

        messageList.addListSelectionListener(e -> {
            Dialog selectedDialog = messageList.getSelectedValue();
            if (selectedDialog != null) {
                messageTextArea.setText(selectedDialog.getText());
                responseArea.setText("");
                responseArea.append("Ответы на сообщение ID: " + selectedDialog.getId() + "\n\n");

                List<Response> responses = selectedDialog.getResponses();
                for (Response response : responses) {
                    responseArea.append("Текст ответа: " + response.getText() +
                            ", Переменная 1: " + response.getNextMessage() +
                            ", Переменная 2: " + response.getScript() + "\n");
                }
            }
        });

        JButton addMessageButton = new JButton("Добавить сообщение");

        addMessageButton.addActionListener(e -> {
            String messageText = messageInputArea.getText();
            Dialog newDialog = new Dialog(messageText != null && !messageText.isEmpty() ? messageText : "Сообщение " + (dialogs.size() + 1));
            dialogs.add(newDialog);
            listModel.addElement(newDialog);
            messageList.setSelectedValue(newDialog, true);

            // Очищаем текстовое поле ввода
            messageInputArea.setText("");
        });

        JPanel messagesPanel = new JPanel(new BorderLayout());
        messagesPanel.add(new JLabel("Сообщения", SwingConstants.CENTER), BorderLayout.NORTH);

        // Добавляем текстовое поле для отображения текста сообщения
        messagesPanel.add(new JScrollPane(messageTextArea), BorderLayout.CENTER);

        // Добавляем список сообщений
        messagesPanel.add(new JScrollPane(messageList), BorderLayout.WEST);

        // Добавляем текстовое поле для ввода текста сообщения
        messagesPanel.add(messageInputScrollPane, BorderLayout.SOUTH);
        messagesPanel.add(addMessageButton, BorderLayout.SOUTH);

        panel.add(messagesPanel);
    }

    private void createResponsesWindow() {
        responseArea = new JTextArea(20, 30);

        JTextField nextMessageField = new JTextField();
        JTextField scriptField = new JTextField();

        JButton submitResponseButton = new JButton("Отправить ответ");
        submitResponseButton.addActionListener(e -> {
            Dialog selectedDialog = messageList.getSelectedValue();
            if (selectedDialog != null) {
                String responseText = responseArea.getText();
                try {
                    int nextMessage = Integer.parseInt(nextMessageField.getText());
                    int script = Integer.parseInt(scriptField.getText());
                    Response response = new Response(responseText, nextMessage, script);
                    selectedDialog.getResponses().add(response);

                    // Сохраняем изменения в тексте сообщения
                    selectedDialog.setText(messageTextArea.getText());

                    JOptionPane.showMessageDialog(null, "Ответ отправлен для сообщения ID: " + selectedDialog.getId());
                    // Вызываем метод сохранения в JSON после отправки ответа
                    String relativePath = "src/main/data/Dialogue.json";
                    saveMessagesToJson(relativePath);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Введите корректные значения для переменных.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Сначала выберите сообщение.");
            }
        });

        JPanel variablesPanel = new JPanel(new GridLayout(2, 2));
        variablesPanel.add(new JLabel("Next Message:"));
        variablesPanel.add(nextMessageField);
        variablesPanel.add(new JLabel("Script:"));
        variablesPanel.add(scriptField);

        JPanel responsesPanel = new JPanel(new BorderLayout());
        responsesPanel.add(new JLabel("Ответы", SwingConstants.CENTER), BorderLayout.NORTH);
        responsesPanel.add(new JScrollPane(responseArea), BorderLayout.CENTER);
        responsesPanel.add(variablesPanel, BorderLayout.WEST);
        responsesPanel.add(submitResponseButton, BorderLayout.SOUTH);

        panel.add(responsesPanel);
    }


    public void saveMessagesToJson(String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(dialogs, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void show() {
        frame.setVisible(true);
    }

    public List<Dialog> getDialogs() {
        return dialogs;
    }

    public DefaultListModel<Dialog> getListModel() {
        return listModel;
    }

    private static class Dialog {
        private static int idCounter = 1;
        private int id;
        private String text;
        private List<Response> responses;

        public Dialog(String text) {
            this.id = idCounter++;
            this.text = text;
            this.responses = new ArrayList<>();
        }

        public int getId() {
            return id;
        }

        public String getText() {
            return text;
        }

        public List<Response> getResponses() {
            return responses;
        }

        @Override
        public String toString() {
            return "Сообщение " + id;
        }
        public void setText(String text) {
            this.text = text;
        }
    }

    private static class Response {
        private String text;
        private int nextMessage;
        private int script;

        public Response(String text, int nextMessage, int script) {
            this.text = text;
            this.nextMessage = nextMessage;
            this.script = script;
        }

        public String getText() {
            return text;
        }

        public int getNextMessage() {
            return nextMessage;
        }

        public int getScript() {
            return script;
        }
    }
}
