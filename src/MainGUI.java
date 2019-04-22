import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;

public class MainGUI extends JDialog {
    private static final String[] TOKEN = {
            "ac5d61e2-fddb-44da-bf8d-2aceec0578af",
            "0914316b-fafb-4c86-a29b-791c639da5b2",
            "ba117244-42d8-4bcf-82f4-d8fb80a0e516",
            "f9dcf6ed-4455-449b-9e7d-e202d407eea4",
            "77701529-3a21-4603-9aec-d8c28d630dff",
            "4d753a2e-ad40-473f-a254-3107f83abcd7",
            "179d415c-7116-4e54-91e8-6731fcf781de",
            "a7dab5fe-7a47-4c17-84ea-46facb7d19fe",
    };
    private static final String[] LANG = {
            "russian",
            "qazaq",
            "ukrainian"
    };
    private static final String[] THEME = {
            "com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
            "javax.swing.plaf.nimbus.NimbusLookAndFeel",
            "com.sun.java.swing.plaf.motif.MotifLookAndFeel",
    };
    private static final String[] SEPARATOR = {
            ";",
            ":",
            ".",
            ",",
            " ",
            "   ",
            "\n",
            "",
    };
    private static final String[] EXTENTION = {
            ".csv",
            ".txt",
            ".cfg",
    };
    private static final String BASE_URL = "https://ws3.morpher.ru/";
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonExport;
    private JButton addToDictionaryButton;
    private JTextPane textPane1;
    private JTextField textField_token;
    private JTextPane textPane2;
    private JComboBox comboBox1;
    private JButton remainderButton;
    private JTextField textField_value;
    private JButton clearValuesButton;
    private JTextField textField1;
    private JTextField textField2;
    private JComboBox comboBox2;
    private JCheckBox additionalFieldCheckBox;
    private JTabbedPane tabbedPane1;
    private JTextField token_static_text;
    private JTextField value_static_text;
    private JProgressBar progressBar1;
    private JComboBox separatorComboBox;
    private JComboBox themeComboBox;
    private JComboBox extensionComboBox;
    private StyledDocument console;
    private StyledDocument main_panel;
    private StringBuilder stringBuilderOutput = new StringBuilder();
    //    private Set<String> stringSet = new HashSet<>();
    private ArrayList<String> JSONOutputArray = new ArrayList<>();
    private Set<String> stringSet;
    private SimpleAttributeSet errorKey;
    private SimpleAttributeSet successKey;
    private SimpleAttributeSet regularBold;
    private SimpleAttributeSet warningKey;
    private String value_field_text;
    private String text_to_produce;
    private String used_token;
    private static Separator separator = new Separator(";");
    private static Extention extention = new Extention(".csv");
    private static Theme theme = new Theme("UIManager.getSystemLookAndFeelClassName()");



    private MainGUI() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(addToDictionaryButton);

        initUI();


        buttonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainGUI.this.onOK();
            }
        });

        remainderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainGUI.this.requestRemainder();
            }
        });

        buttonExport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (stringBuilderOutput.toString().equals("")) {
                    MainGUI.this.consoleLog(console.getLength(), "\n Обнаружен пустой вывод!", errorKey);
                } else {
                    MainGUI.this.exportCSV();
                }
            }
        });

        addToDictionaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainGUI.this.onOK();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MainGUI.this.onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        clearValuesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearData();
            }

            private void clearData() {
                textPane1.setText(null);
                textField_token.setText(null);
                textField_value.setText(null);
                textField1.setText(null);
                textField2.setText(null);
                try {
                    stringSet.clear();
                    stringBuilderOutput.setLength(0);
                    consoleLog(console.getLength(), "\nДанные очищены!", successKey);
                } catch (NullPointerException e) {
                    consoleLog(console.getLength(), "\nНе смог очистить переменные: " + e.getMessage(), errorKey);
                    e.printStackTrace();
                }
            }
        });
        additionalFieldCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getAdditionalState();
            }
        });
        separatorComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                System.out.println(separatorComboBox.getSelectedItem().toString());
                switch (separatorComboBox.getSelectedIndex()) {
                    case 0: separator.setSeparator(SEPARATOR[0]);
                        consoleLog(console.getLength(), "\nИзменил разделитель на: 'Точка с запятой'", null);
                        break;
                    case 1: separator.setSeparator(SEPARATOR[1]);
                        consoleLog(console.getLength(), "\nИзменил разделитель на: 'Двоеточие'", null);
                        break;
                    case 2: separator.setSeparator(SEPARATOR[2]);
                        consoleLog(console.getLength(), "\nИзменил разделитель на: 'Точка'", null);
                        break;
                    case 3: separator.setSeparator(SEPARATOR[3]);
                        consoleLog(console.getLength(), "\nИзменил разделитель на: 'Запятая'", null);
                        break;
                    case 4: separator.setSeparator(SEPARATOR[4]);
                        consoleLog(console.getLength(), "\nИзменил разделитель на: 'Пробел'", null);
                        break;
                    case 5: separator.setSeparator(SEPARATOR[5]);
                        consoleLog(console.getLength(), "\nИзменил разделитель на: 'Табуляция'", null);
                        break;
                    case 6: separator.setSeparator(SEPARATOR[6]);
                        consoleLog(console.getLength(), "\nИзменил разделитель на: 'Новая линия'", null);
                        break;
                    case 7: separator.setSeparator(SEPARATOR[7]);
                        consoleLog(console.getLength(), "\nИзменил разделитель на: 'Без разделителя'", null);
                        break;
                }
            }
        });
        themeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                switch (themeComboBox.getSelectedIndex()) {
                    case 0:
                        theme.setTheme(THEME[0]);
                        try {
                            UIManager.setLookAndFeel(theme.getTheme());
                            new MainGUI();
                        } catch (ClassNotFoundException e1) {
                            consoleLog(console.getLength(), "\nУпс, тема упала: " + e1.getMessage(), errorKey);
                            e1.printStackTrace();
                        } catch (InstantiationException e1) {
                            consoleLog(console.getLength(), "\nУпс, тема упала: " + e1.getMessage(), errorKey);
                            e1.printStackTrace();
                        } catch (IllegalAccessException e1) {
                            consoleLog(console.getLength(), "\nУпс, тема упала: " + e1.getMessage(), errorKey);
                            e1.printStackTrace();
                        } catch (UnsupportedLookAndFeelException e1) {
                            consoleLog(console.getLength(), "\nУпс, тема упала: " + e1.getMessage(), errorKey);
                            e1.printStackTrace();
                        }
                        break;
                    case 1:
                        theme.setTheme(THEME[1]);
                        try {
                            UIManager.setLookAndFeel(theme.getTheme());
                            new MainGUI();
                        } catch (ClassNotFoundException e1) {
                            consoleLog(console.getLength(), "\nУпс, тема упала: " + e1.getMessage(), errorKey);
                            e1.printStackTrace();
                        } catch (InstantiationException e1) {
                            consoleLog(console.getLength(), "\nУпс, тема упала: " + e1.getMessage(), errorKey);
                            e1.printStackTrace();
                        } catch (IllegalAccessException e1) {
                            consoleLog(console.getLength(), "\nУпс, тема упала: " + e1.getMessage(), errorKey);
                            e1.printStackTrace();
                        } catch (UnsupportedLookAndFeelException e1) {
                            consoleLog(console.getLength(), "\nУпс, тема упала: " + e1.getMessage(), errorKey);
                            e1.printStackTrace();
                        }
                        break;
                    case 2:
                        theme.setTheme(THEME[2]);
                        try {
                            UIManager.setLookAndFeel(theme.getTheme());
                            new MainGUI();
                        } catch (ClassNotFoundException e1) {
                            consoleLog(console.getLength(), "\nУпс, тема упала: " + e1.getMessage(), errorKey);
                            e1.printStackTrace();
                        } catch (InstantiationException e1) {
                            consoleLog(console.getLength(), "\nУпс, тема упала: " + e1.getMessage(), errorKey);
                            e1.printStackTrace();
                        } catch (IllegalAccessException e1) {
                            consoleLog(console.getLength(), "\nУпс, тема упала: " + e1.getMessage(), errorKey);
                            e1.printStackTrace();
                        } catch (UnsupportedLookAndFeelException e1) {
                            consoleLog(console.getLength(), "\nУпс, тема упала: " + e1.getMessage(), errorKey);
                            e1.printStackTrace();
                        }
                        break;
                }
            }
        });
        extensionComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                switch (extensionComboBox.getSelectedIndex()) {
                    case 0:
                        extention.setExtention(EXTENTION[0]);
                        break;
                    case 1:
                        extention.setExtention(EXTENTION[1]);
                        break;
                    case 2:
                        extention.setExtention(EXTENTION[2]);
                        break;
                }
            }
        });
    }

    private void initUI() {
        textField1.setBackground(Color.lightGray);
        textField2.setBackground(Color.lightGray);

        for (int i = 1; i <= TOKEN.length; i++) {
            comboBox1.addItem(new ComboBoxItem("Token: " + i, TOKEN[i-1]));
        }
        for (String th : THEME) {
            themeComboBox.addItem(th);
        }
        for (String lang: LANG) {
            comboBox2.addItem(lang);
        }

        for (String ex : EXTENTION) {
            extensionComboBox.addItem(ex);
        }
        separatorComboBox.addItem(new ComboBoxItem("Точка с запятой", ";"));
        separatorComboBox.addItem(new ComboBoxItem("Двоеточие", ":"));
        separatorComboBox.addItem(new ComboBoxItem("Точка", "."));
        separatorComboBox.addItem(new ComboBoxItem("Запятая", ","));
        separatorComboBox.addItem(new ComboBoxItem("Пробел", " "));
        separatorComboBox.addItem(new ComboBoxItem("Табуляция", "    "));
        separatorComboBox.addItem(new ComboBoxItem("Новая линия", "\n"));
        separatorComboBox.addItem(new ComboBoxItem("Без разделителя", ""));


        console = textPane2.getStyledDocument();
        main_panel = textPane1.getStyledDocument();

        errorKey = new SimpleAttributeSet();
        StyleConstants.setForeground(errorKey, Color.RED);
        StyleConstants.setBold(errorKey, false);

        successKey = new SimpleAttributeSet();
        StyleConstants.setForeground(successKey, Color.GREEN);
        StyleConstants.setBold(successKey, false);

        warningKey = new SimpleAttributeSet();
        StyleConstants.setForeground(warningKey, Color.YELLOW);
        StyleConstants.setBold(warningKey, false);


        regularBold = new SimpleAttributeSet();
        StyleConstants.setForeground(regularBold, Color.WHITE);
        StyleConstants.setBold(regularBold, true);

        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        StyleConstants.setBold(keyWord, true);

        consoleLog(0, "Вас приветствует консоль: ", keyWord);
    }

    private boolean getAdditionalState() {
        if (additionalFieldCheckBox.isSelected()) {
            textField1.setEnabled(true);
            textField2.setEnabled(true);
            textField1.setBackground(Color.white);
            textField2.setBackground(Color.white);
            return true;
        } else {
            textField1.setText(null);
            textField2.setText(null);
            textField1.setEnabled(false);
            textField2.setEnabled(false);
            textField1.setBackground(Color.lightGray);
            textField2.setBackground(Color.lightGray);
            return false;
        }
    }


    private void onAddToDictionaryClicked() {
        getUserToken();

        value_field_text = textField_value.getText();
        text_to_produce = textField_token.getText().toLowerCase();

        if (stringSet != null) {
            stringSet.clear();
        }

        try {
            String JSONstr = readJSON(text_to_produce, "declension");
            parseJSON(JSONstr);

            stringSet = new TreeSet<>(JSONOutputArray);
            if (getAdditionalState()) {
                for (Object aStringSet : stringSet) {
                    stringBuilderOutput.append(aStringSet).append(separator.getSeparator()).append(value_field_text).append(separator.getSeparator())
                            .append(textField2.getText()).append("\n");
                }
            } else {
                for (Object aStringSet : stringSet) {
                    stringBuilderOutput.append(aStringSet).append(separator.getSeparator()).append(value_field_text).append("\n");
                }
            }
            main_panel.insertString(main_panel.getLength(), stringSet.toString() + "\n", null);
            consoleLog(console.getLength(), "\nЗапись добавлена!", successKey);
            System.out.println("Запись добавлена");

        } catch (IOException e) {
            consoleLog(console.getLength(), "\nПроизошло злое зло, убиваемся", errorKey);
            consoleLog(console.getLength(), "\n" + e.getMessage(), errorKey);
            e.getMessage();
        } catch (BadLocationException e) {
            consoleLog(console.getLength(), "\nПоздравляю, ты сломал консоль", successKey);
            e.getMessage();
        }
        textField_token.setText(null);
        textField_value.setText(null);
    }

    private String getUserToken() { //ПЕРЕПИСАТЬ ЧЕРЕЗ КЛАССЫ!
        return (comboBox1.getSelectedIndex() == 0) ? TOKEN[0] :
                (comboBox1.getSelectedIndex() == 1) ? TOKEN[1] :
                (comboBox1.getSelectedIndex() == 2) ? TOKEN[2] :
                (comboBox1.getSelectedIndex() == 3) ? TOKEN[3] :
                (comboBox1.getSelectedIndex() == 4) ? TOKEN[4] :
                (comboBox1.getSelectedIndex() == 5) ? TOKEN[5] :
                (comboBox1.getSelectedIndex() == 6) ? TOKEN[6] : TOKEN[7];
    }

    private void parseJSON(String json) {
        JSONParser parser = new JSONParser();
        JSONOutputArray.clear();

        if (getLanguage().equals("russian")) {
            try {
                Object obj = parser.parse(json);
                JSONObject jsonObject = (JSONObject) obj;
                System.out.println(jsonObject);
                String Declension_1 = (String) jsonObject.get("Р");
                String Declension_2 = (String) jsonObject.get("Д");
                String Declension_3 = (String) jsonObject.get("В");
                String Declension_4 = (String) jsonObject.get("Т");
                String Declension_5 = (String) jsonObject.get("П");
                System.out.println(Declension_1);
                System.out.println(Declension_2);
                System.out.println(Declension_3);
                System.out.println(Declension_4);
                System.out.println(Declension_5);
                JSONOutputArray.add(Declension_1);
                JSONOutputArray.add(Declension_2);
                JSONOutputArray.add(Declension_3);
                JSONOutputArray.add(Declension_4);
                JSONOutputArray.add(Declension_5);
//            ArrayList arr = (ArrayList) parser.get("idArr"); // Получаем массив
                if ((JSONObject) jsonObject.get("множественное") != null) {
                    JSONObject plural = (JSONObject) jsonObject.get("множественное"); // Получаем сложную JSON структуру

                    String Declension_plural_1 = (String) plural.get("Р");
                    String Declension_plural_2 = (String) plural.get("Д");
                    String Declension_plural_3 = (String) plural.get("В");
                    String Declension_plural_4 = (String) plural.get("Т");
                    String Declension_plural_5 = (String) plural.get("П");

                    System.out.println(Declension_plural_1);
                    System.out.println(Declension_plural_2);
                    System.out.println(Declension_plural_3);
                    System.out.println(Declension_plural_4);
                    System.out.println(Declension_plural_5);

                    JSONOutputArray.add(Declension_plural_1);
                    JSONOutputArray.add(Declension_plural_2);
                    JSONOutputArray.add(Declension_plural_3);
                    JSONOutputArray.add(Declension_plural_4);
                    JSONOutputArray.add(Declension_plural_5);
                }

            } catch (ParseException e) {
                consoleLog(console.getLength(), "\nУпс, злое зло!", errorKey);
                e.printStackTrace();
            }
        } else if (getLanguage().equals("qazaq")) {
            try {
                Object obj = parser.parse(json);
                JSONObject jsonObject = (JSONObject) obj;
                System.out.println(jsonObject);
                String Declension_1 = (String) jsonObject.get("І");
                String Declension_2 = (String) jsonObject.get("Б");
                String Declension_3 = (String) jsonObject.get("Т");
                String Declension_4 = (String) jsonObject.get("Ш");
                String Declension_5 = (String) jsonObject.get("Ж");
                String Declension_6 = (String) jsonObject.get("К");
                System.out.println(Declension_1);
                System.out.println(Declension_2);
                System.out.println(Declension_3);
                System.out.println(Declension_4);
                System.out.println(Declension_5);
                System.out.println(Declension_6);
                JSONOutputArray.add(Declension_1);
                JSONOutputArray.add(Declension_2);
                JSONOutputArray.add(Declension_3);
                JSONOutputArray.add(Declension_4);
                JSONOutputArray.add(Declension_5);
                JSONOutputArray.add(Declension_6);
                if ((JSONObject) jsonObject.get("көпше") != null) {
                    JSONObject plural = (JSONObject) jsonObject.get("көпше");

                    String Declension_plural_1 = (String) plural.get("A");
                    String Declension_plural_2 = (String) plural.get("І");
                    String Declension_plural_3 = (String) plural.get("Б");
                    String Declension_plural_4 = (String) plural.get("Т");
                    String Declension_plural_5 = (String) plural.get("Ш");
                    String Declension_plural_6 = (String) plural.get("Ж");
                    String Declension_plural_7 = (String) plural.get("К");

                    System.out.println(Declension_plural_1);
                    System.out.println(Declension_plural_2);
                    System.out.println(Declension_plural_3);
                    System.out.println(Declension_plural_4);
                    System.out.println(Declension_plural_5);
                    System.out.println(Declension_plural_6);
                    System.out.println(Declension_plural_7);

                    JSONOutputArray.add(Declension_plural_1);
                    JSONOutputArray.add(Declension_plural_2);
                    JSONOutputArray.add(Declension_plural_3);
                    JSONOutputArray.add(Declension_plural_4);
                    JSONOutputArray.add(Declension_plural_5);
                    JSONOutputArray.add(Declension_plural_6);
                    JSONOutputArray.add(Declension_plural_7);
                }

            } catch (ParseException e) {
                consoleLog(console.getLength(), "\nУпс, злое зло!", errorKey);
                e.printStackTrace();
            }
        } else if (getLanguage().equals("ukrainian")) {
            try {
                Object obj = parser.parse(json);
                JSONObject jsonObject = (JSONObject) obj;
                System.out.println(jsonObject);
                String Declension_1 = (String) jsonObject.get("Р");
                String Declension_2 = (String) jsonObject.get("Д");
                String Declension_3 = (String) jsonObject.get("З");
                String Declension_4 = (String) jsonObject.get("О");
                String Declension_5 = (String) jsonObject.get("М");
                String Declension_6 = (String) jsonObject.get("К");
                System.out.println(Declension_1);
                System.out.println(Declension_2);
                System.out.println(Declension_3);
                System.out.println(Declension_4);
                System.out.println(Declension_5);
                System.out.println(Declension_6);
                JSONOutputArray.add(Declension_1);
                JSONOutputArray.add(Declension_2);
                JSONOutputArray.add(Declension_3);
                JSONOutputArray.add(Declension_4);
                JSONOutputArray.add(Declension_5);
                JSONOutputArray.add(Declension_6);
            } catch (ParseException e) {
                consoleLog(console.getLength(), "\nУпс, злое зло!", errorKey);
                e.printStackTrace();
            }
        }
    }

    private String readJSON(String str, String request) throws IOException {
        BufferedReader reader = null;
        try {
            URL url;
            url = request.equals("declension") ?
                    new URL(BASE_URL + getLanguage() + "/declension?format=json&s=" + str + "&token=" + getUserToken()) :
                    request.equals("remainder") ?
                            new URL(BASE_URL + "get_queries_left_for_today?format=json&token=" + getUserToken()) :
                            null;
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1) {
                buffer.append(chars, 0, read);
            }
            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }

    private String getLanguage() { //ПЕРЕПИСАТЬ ЧЕРЕЗ КЛАССЫ!
        return (comboBox2.getSelectedIndex() == 0) ? LANG[0] :
                (comboBox2.getSelectedIndex() == 1) ? LANG[1]: LANG[2];
    }

    private void exportCSV() {

        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                OutputStreamWriter osw = new OutputStreamWriter(
                        new FileOutputStream(fc.getSelectedFile() + extention.getExtention()), "UTF-8");
                if (getAdditionalState()) {
                    osw.write("token" + separator.getSeparator() + "value" + separator.getSeparator() + textField1.getText() + "\n");
                } else {
                    osw.write("token" + separator.getSeparator() + "value\n");
                }
                osw.write(String.valueOf(stringBuilderOutput));
                osw.close();
                consoleLog(console.getLength(), "\nФайл успешно сохранен по пути: ", successKey);
                consoleLog(console.getLength(), "\n " + fc.getSelectedFile(), regularBold);
            } catch (Exception e) {
                consoleLog(console.getLength(), "\nЧто то пошло не так ...", errorKey);
                System.out.println("Что-то пошло не так...");
            }
        }

    }

    private void requestRemainder() {
        try {
            String remainder = readJSON(text_to_produce, "remainder");
            try {
                consoleLog(console.getLength(), "\nОстаток запросов на сегодня: " + Integer.parseInt(remainder), null);
            } catch (NumberFormatException e) {
                consoleLog(console.getLength(), "\nКажется неверный токен!", errorKey);
                e.printStackTrace();
            }
        } catch (IOException e) {
            consoleLog(console.getLength(), "\nНе получилось спарсить json по остатку!", errorKey);
            e.printStackTrace();
        }
    }

    private void consoleLog(int offset, String str, AttributeSet a) { //ПЕРЕПИСАТЬ ЧЕРЕЗ КЛАССЫ!
        try {
            console.insertString(offset, str, a);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void onOK() {
        if (validate_input(textField_token.getText().toLowerCase()))
            onAddToDictionaryClicked();
        else consoleLog(console.getLength(), "\nВведенные данные не прошли валидацию", errorKey);
    }

    private boolean validate_input(String input) {
//        for (String str: stringBuilderOutput.toString().split(";")) {
//            if (str.equals(input)) {
//                return true;
//            }
//        }
        return !(input == null) && input.matches("^[а-яәғқңөұүһіґєії]+$");
//        return true;
    }

    private void onCancel() {
        dispose();
    }

    public static void main(String[] args) {
        MainGUI dialog = new MainGUI();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}