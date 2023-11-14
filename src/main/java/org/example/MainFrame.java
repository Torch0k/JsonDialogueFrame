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
        // �������� ������������ ��������� �� JSON
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
        messageInputArea.setLineWrap(true); // ������� ������ ��� ���������� ����� ������� �������
        JScrollPane messageInputScrollPane = new JScrollPane(messageInputArea);

        messageList.addListSelectionListener(e -> {
            Dialog selectedDialog = messageList.getSelectedValue();
            if (selectedDialog != null) {
                messageTextArea.setText(selectedDialog.getText());
                responseArea.setText("");
                responseArea.append("������ �� ��������� ID: " + selectedDialog.getId() + "\n\n");

                List<Response> responses = selectedDialog.getResponses();
                for (Response response : responses) {
                    responseArea.append("����� ������: " + response.getText() +
                            ", ���������� 1: " + response.getNextMessage() +
                            ", ���������� 2: " + response.getScript() + "\n");
                }
            }
        });

        JButton addMessageButton = new JButton("�������� ���������");

        addMessageButton.addActionListener(e -> {
            String messageText = messageInputArea.getText();
            Dialog newDialog = new Dialog(messageText != null && !messageText.isEmpty() ? messageText : "��������� " + (dialogs.size() + 1));
            dialogs.add(newDialog);
            listModel.addElement(newDialog);
            messageList.setSelectedValue(newDialog, true);

            // ������� ��������� ���� �����
            messageInputArea.setText("");
        });

        JPanel messagesPanel = new JPanel(new BorderLayout());
        messagesPanel.add(new JLabel("���������", SwingConstants.CENTER), BorderLayout.NORTH);

        // ��������� ��������� ���� ��� ����������� ������ ���������
        messagesPanel.add(new JScrollPane(messageTextArea), BorderLayout.CENTER);

        // ��������� ������ ���������
        messagesPanel.add(new JScrollPane(messageList), BorderLayout.WEST);

        // ��������� ��������� ���� ��� ����� ������ ���������
        messagesPanel.add(messageInputScrollPane, BorderLayout.SOUTH);
        messagesPanel.add(addMessageButton, BorderLayout.SOUTH);

        panel.add(messagesPanel);
    }

    private void createResponsesWindow() {
        responseArea = new JTextArea(20, 30);

        JTextField nextMessageField = new JTextField();
        JTextField scriptField = new JTextField();

        JButton submitResponseButton = new JButton("��������� �����");
        submitResponseButton.addActionListener(e -> {
            Dialog selectedDialog = messageList.getSelectedValue();
            if (selectedDialog != null) {
                String responseText = responseArea.getText();
                try {
                    int nextMessage = Integer.parseInt(nextMessageField.getText());
                    int script = Integer.parseInt(scriptField.getText());
                    Response response = new Response(responseText, nextMessage, script);
                    selectedDialog.getResponses().add(response);

                    // ��������� ��������� � ������ ���������
                    selectedDialog.setText(messageTextArea.getText());

                    JOptionPane.showMessageDialog(null, "����� ��������� ��� ��������� ID: " + selectedDialog.getId());
                    // �������� ����� ���������� � JSON ����� �������� ������
                    String relativePath = "src/main/data/Dialogue.json";
                    saveMessagesToJson(relativePath);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "������� ���������� �������� ��� ����������.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "������� �������� ���������.");
            }
        });

        JPanel variablesPanel = new JPanel(new GridLayout(2, 2));
        variablesPanel.add(new JLabel("Next Message:"));
        variablesPanel.add(nextMessageField);
        variablesPanel.add(new JLabel("Script:"));
        variablesPanel.add(scriptField);

        JPanel responsesPanel = new JPanel(new BorderLayout());
        responsesPanel.add(new JLabel("������", SwingConstants.CENTER), BorderLayout.NORTH);
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
            return "��������� " + id;
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
