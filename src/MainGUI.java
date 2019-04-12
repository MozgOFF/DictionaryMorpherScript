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
    private static final String TOKEN_1 = "ac5d61e2-fddb-44da-bf8d-2aceec0578af";   //мой
    private static final String TOKEN_2 = "0914316b-fafb-4c86-a29b-791c639da5b2";   //мой второй
    private static final String TOKEN_3 = "a7dab5fe-7a47-4c17-84ea-46facb7d19fe";   //неверный
    private static final String BASE_URL = "https://ws3.morpher.ru/";
    private static final String LANG_RU = "russian";   //борщ
    private static final String LANG_KK = "qazaq";   //кумыс
    private static final String LANG_UK = "ukrainian";   //сало
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonExport;
    private JButton addToDictionaryButton;
    private JTextPane textPane1;
    private JTextField textField_token;
    private JTextField token_static_text;
    private JTextPane textPane2;
    private JComboBox comboBox1;
    private JButton remainderButton;
    private JTextField textField_value;
    private JButton clearValuesButton;
    private JTextField value_static_text;
    private JComboBox comboBox2;
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


    private MainGUI() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(addToDictionaryButton);

        comboBox1.addItem(TOKEN_1);
        comboBox1.addItem(TOKEN_2);
        comboBox1.addItem(TOKEN_3);

        comboBox2.addItem(LANG_RU);
        comboBox2.addItem(LANG_KK);
        comboBox2.addItem(LANG_UK);

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
                try {
                    stringSet.clear();
                    stringBuilderOutput.setLength(0);
                    textPane1.setText("");
                    textField_token.setText(null);
                    textField_value.setText(null);
                    consoleLog(console.getLength(), "\nДанные очищены!", successKey);
                } catch (NullPointerException e) {
                    consoleLog(console.getLength(), "\nНе смог очистить данные: " + e.getMessage(), errorKey);
                    e.getMessage();
                }
            }
        });
    }


    private void onAddToDictionaryClicked() {
        getUserToken();

        value_field_text = textField_value.getText().toLowerCase();
        text_to_produce = textField_token.getText().toLowerCase();


        if (stringSet != null) {
            stringSet.clear();
        }

        try {
            String JSONstr = readJSON(text_to_produce, "declension");
            parseJSON(JSONstr);

            stringSet = new TreeSet<>(JSONOutputArray);
            for (Object aStringSet : stringSet) {
                stringBuilderOutput.append(aStringSet).append(";").append(value_field_text).append("\n");
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

    private String getUserToken() {
        return (comboBox1.getSelectedIndex() == 0) ? TOKEN_1 :
               (comboBox1.getSelectedIndex() == 1) ? TOKEN_2 : TOKEN_3;
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

    private String getLanguage() {
        return (comboBox2.getSelectedIndex() == 0) ? LANG_RU :
                (comboBox2.getSelectedIndex() == 1) ? LANG_KK: LANG_UK;
    }

    private void exportCSV() {

        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                OutputStreamWriter osw = new OutputStreamWriter(
                        new FileOutputStream(fc.getSelectedFile() + ".csv"), "UTF-8");
                osw.write("token;value\n");
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

    private void consoleLog(int offset, String str, AttributeSet a) {
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
