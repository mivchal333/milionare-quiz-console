import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import service.LoginService;

import java.io.IOException;

public class App {
    private Window window;
    private WindowBasedTextGUI textGUI;
    private Screen screen;

    private final LoginService loginService = new LoginService();

    void welcomeDialog() {
        new MessageDialogBuilder()
                .setTitle("Welcome")
                .setText("Welcome in Millionaire Quiz!")
                .addButton(MessageDialogButton.Continue)
                .build()
                .showDialog(textGUI);
    }

    void setUpWindow() throws IOException {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();

        screen = terminalFactory.createScreen();
        screen.startScreen();

        textGUI = new MultiWindowTextGUI(screen);

        window = new BasicWindow("Millionaire Quiz");
    }

    void authView() {

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        Panel leftPanel = new Panel();
        mainPanel.addComponent(leftPanel.withBorder(Borders.singleLine("Login")));
        leftPanel.addComponent(new Label("Username"));
        final TextBox usernameLogin = new TextBox().addTo(leftPanel);
        leftPanel.addComponent(new Label("Password"));
        final TextBox passwordLogin = new TextBox().addTo(leftPanel);
        Label resultLabel = new Label("");
        leftPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        new Button("Login", handleLoginAttempt(leftPanel, usernameLogin, passwordLogin, resultLabel)).addTo(leftPanel);

        Panel rightPanel = new Panel();
        mainPanel.addComponent(rightPanel.withBorder(Borders.singleLine("Register")));
        rightPanel.addComponent(new Label("Username"));
        final TextBox usernameRegister = new TextBox().addTo(rightPanel);

        rightPanel.addComponent(new Label("Nick"));
        final TextBox nickRegister = new TextBox().addTo(rightPanel);

        rightPanel.addComponent(new Label("Password"));
        final TextBox passwordRegister = new TextBox().addTo(rightPanel);
        Label registerResult = new Label("");
        rightPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        new Button("Register", handleRegisterAttempt(rightPanel, usernameRegister, passwordRegister, nickRegister, registerResult)).addTo(rightPanel);


        window.setComponent(mainPanel.withBorder(Borders.singleLine("Please login or register")));

    }

    private Runnable handleRegisterAttempt(Panel rightPanel, TextBox usernameRegister, TextBox passwordRegister, TextBox nickRegister, Label registerResult) {
        return () -> {
            String username = usernameRegister.getText();
            String password = passwordRegister.getText();
            String nick = nickRegister.getText();
            boolean result = loginService.attemptRegister(username, password, nick);
            if (result) {
                textGUI.removeWindow(window);
            } else {
                registerResult.setText("Try again!");
                registerResult.setForegroundColor(TextColor.ANSI.RED);
                rightPanel.addComponent(registerResult);
            }
        };
    }

    private Runnable handleLoginAttempt(Panel leftPanel, TextBox usernameLogin, TextBox passwordLogin, Label resultLabel) {
        return () -> {
            String username = usernameLogin.getText();
            String password = passwordLogin.getText();
            boolean result = loginService.attemptLogin(username, password);
            if (result) {
                textGUI.removeWindow(window);
            } else {
                resultLabel.setText("Try again!");
                resultLabel.setForegroundColor(TextColor.ANSI.RED);
                leftPanel.addComponent(resultLabel);
            }
        };
    }

    public void start() {
        Screen screen = null;

        try {
            setUpWindow();

//            welcomeDialog();

            authView();

            System.out.println("END");

            textGUI.addWindowAndWait(window);


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (screen != null) {
                try {
                    screen.stopScreen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {

        App app = new App();
        app.start();
        return;
    }
}