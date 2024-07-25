package com.burp.save.ui;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.burp.save.RepeaterSave;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

public class BurpUI {
    private JPanel root;
    private JLabel title;
    public JTextArea textArea;
    private JScrollPane JScrollPanel;
    private JRadioButton RadioButton;
    private JButton clearButton;
    private JButton saveButton;
    private JTextField hostTextField;
    private JLabel hostlabel;
    private JTextField cookieSet;
    private JButton cookieTitle;

    public BurpUI() {
        RadioButton.addActionListener(e -> {
            if (RadioButton.isSelected()) {
                RadioButton.setText("插件已激活");
                RadioButton.setForeground(Color.green);
                RepeaterSave.pluginStatus = true;
                textArea.append("\n");
            } else {
                RadioButton.setText("插件未激活");
                RadioButton.setForeground(Color.red);
                RepeaterSave.pluginStatus = false;
            }
        });
        clearButton.addActionListener(e -> textArea.setText(""));
        hostTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                RepeaterSave.filteHost = hostTextField.getText();
            }
        });
        saveButton.addActionListener(e -> {
            JFileChooser jFileChooser = new JFileChooser();
            FileNameExtensionFilter fileNameExtensionFilter = new FileNameExtensionFilter("文本类型（*.txt、*.log、*.xml）", "txt", "log", "xml");
            jFileChooser.setFileFilter(fileNameExtensionFilter);
            int option = jFileChooser.showSaveDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = jFileChooser.getSelectedFile();
                String filename = jFileChooser.getName(file);
                if (filename.contains(".txt")) {
                    file = new File(jFileChooser.getCurrentDirectory(), filename);
                } else if (filename.contains(".log")) {
                    file = new File(jFileChooser.getCurrentDirectory(), filename);
                } else if (filename.contains(".xml")) {
                    file = new File(jFileChooser.getCurrentDirectory(), filename);
                } else {
                    file = new File(jFileChooser.getCurrentDirectory(), filename + ".xml");
                }
                xmlFileSave(JSONObject.parseArray(textArea.getText()), file);
            }
        });
        cookieTitle.addActionListener(e -> {
            JSONArray jsonArray = JSONObject.parseArray(textArea.getText(), Feature.OrderedField);
            JSONArray jsonArrayResult = new JSONArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONObject headerJsonObject = jsonObject.getJSONObject("headers");
                headerJsonObject.put("Cookie", cookieSet.getText());
                jsonArrayResult.add(jsonObject);
            }
            jsonArray.clear();
            textArea.setText(JSONArray.toJSONString(jsonArrayResult, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteDateUseDateFormat));
        });
    }

    public JPanel getRoot() {
        return root;
    }

    public void xmlFileSave(JSONArray reqsJsonArray, File filePathName) {
        // 获取当前时间
        Date date = new Date();
        //创建document对象
        Document document = DocumentHelper.createDocument();
        //添加节点
        Element items = document.addElement("items");
        items.addAttribute("burpVersion", "2023.3");
        items.addAttribute("exportTime", String.valueOf(date));
        for (int i = 0; i < reqsJsonArray.size(); i++) {
            Element item = items.addElement("item");
            Element time = item.addElement("time");
            Element url = item.addElement("url");
            Element host = item.addElement("host");
            Element port = item.addElement("port");
            Element protocol = item.addElement("protocol");
            Element method = item.addElement("method");
            Element path = item.addElement("path");
            Element extension = item.addElement("extension");
            Element request = item.addElement("request");
            Element status = item.addElement("status");
            Element responselength = item.addElement("responselength");
            Element mimetype = item.addElement("mimetype");
            Element response = item.addElement("response");
            Element comment = item.addElement("comment");
            // 赋值
            JSONObject jsonObject = reqsJsonArray.getJSONObject(i);
            JSONObject headersJsonObject = jsonObject.getJSONObject("headers");
            time.setText(String.valueOf(date));
            url.addCDATA(jsonObject.getString("url"));
            host.setText(jsonObject.getString("host"));
            host.addAttribute("ip", jsonObject.getString("ip"));
            port.setText(jsonObject.getString("port"));
            protocol.setText(jsonObject.getString("protocol"));
            method.addCDATA(jsonObject.getString("method"));
            path.addCDATA(jsonObject.getString("path"));
            extension.setText("do");
            request.addAttribute("base64", "true");
            status.setText("");
            responselength.setText("");
            mimetype.setText("");
            response.addCDATA("");
            response.addAttribute("base64", "true");
            comment.setText("");
            StringBuilder stringBuilder = new StringBuilder(jsonObject.getString("method") + " " + jsonObject.get("path") + " HTTP/1.1\r\n");
            for (String key : headersJsonObject.keySet()) {
                String value = headersJsonObject.getString(key);
                stringBuilder.append(key).append(": ").append(value).append("\r\n");
            }
            stringBuilder.append("\r\n");
            stringBuilder.append(jsonObject.getString("body"));
            request.addCDATA(Base64.getEncoder().encodeToString(stringBuilder.toString().getBytes(StandardCharsets.UTF_8)));
        }
        //漂亮的格式
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        //解决声明下空行问题
        format.setNewlines(true);
        //生成xml文件
        try {
            XMLWriter xmlWriter = new XMLWriter(new FileWriter(filePathName.toString()), format);
            xmlWriter.write(document);
            xmlWriter.close();
        } catch (IOException e) {
            System.out.println("失败");
        }
//            //打开本地文件 及 所在目录
//            try {
//                Desktop.getDesktop().open(new File(String.valueOf(directoryPath)));
//                Desktop.getDesktop().open(new File(parentPath));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
    }

    public JTextArea getTextArea() {
        return textArea;
    }
}
