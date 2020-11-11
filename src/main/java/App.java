import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import model.AttemptEntry;
import model.Question;
import model.User;
import service.GameManager;
import service.LoginService;
import service.PrizesService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class App {
    private Window window;
    private WindowBasedTextGUI textGUI;
    private Screen screen;
    private final PrizesService prizesService = new PrizesService();
    private final GameManager gameManager = new GameManager();
    private List<Question> questions;
    private int questionCount;
    private final LoginService loginService = new LoginService();
    private User user;

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
            Optional<User> userOpt = loginService.attemptLogin(username, password);
            if (userOpt.isPresent()) {
                user = userOpt.get();
                showMainMenu();

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

            welcomeDialog();

            authView();

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

    private void showMainMenu() {
        window.setHints(Arrays.asList(Window.Hint.CENTERED));

        Panel panel = new Panel();
        panel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        Button playButton = new Button("PLAY");
        playButton.addListener(button -> {
            playGame();
        });
        panel.addComponent(playButton);

        Button statisticsButton = new Button("STATISTICS");
        statisticsButton.addListener(button -> {
            showStatistics();
        });
        panel.addComponent(statisticsButton);

        Button exitButton = new Button("CLOSE");
        exitButton.addListener(button -> {
            textGUI.removeWindow(window);
        });
        panel.addComponent(exitButton);

        window.setComponent(panel);
    }

    private void playGame() {
        questions = gameManager.getQuestions();

        Panel panel = new Panel();


        panel.addComponent(new EmptySpace(new TerminalSize(0, 2)));

        Label questionTitle = new Label("Question number " + (questionCount + 1));
        panel.addComponent(questionTitle);

        panel.addComponent(new EmptySpace(new TerminalSize(0, 1)));

        Question question = questions.get(questionCount);
        Label questionContent = new Label(question.getQuestion());
        panel.addComponent(questionContent);

        RadioBoxList<String> radioBoxList = new RadioBoxList<String>(new TerminalSize(50, 10));

        List<String> answers = question.getIncorrectAnswers();
        answers.add(question.getCorrectAnswer());
        Collections.shuffle(answers);
        answers.forEach(radioBoxList::addItem);
        panel.addComponent(radioBoxList);

        Button submit = new Button("Submit");
        submit.addListener(button -> {
            processAnswer(radioBoxList.getCheckedItem());
        });
        panel.addComponent(submit);

        window.setHints(Arrays.asList(Window.Hint.FULL_SCREEN));
        window.setComponent(panel);
    }

    private void processAnswer(String checkedItem) {
        if (questions.get(questionCount).getCorrectAnswer().equals(checkedItem)) {
            if (questionCount < questions.size() - 1) {
                questionCount++;
                new MessageDialogBuilder()
                        .setTitle("Success")
                        .setText("Correct answer! Continue to next question :)")
                        .addButton(MessageDialogButton.OK)
                        .build()
                        .showDialog(textGUI);
                playGame();
            } else {
                showWinnerDialog();
                gameManager.saveAttempt(user, prizesService.getObtainedPrize(questionCount));
            }
        } else {
            gameManager.saveAttempt(user, prizesService.getObtainedPrize(questionCount));
            new MessageDialogBuilder()
                    .setTitle("Wrong")
                    .setText("Wrong answer. End game :(. You won: " + prizesService.getObtainedPrize(questionCount))
                    .addButton(MessageDialogButton.OK)
                    .build()
                    .showDialog(textGUI);
            resetGame();
        }
    }

    private void showWinnerDialog() {
        Panel panel = new Panel();
        int prize = prizesService.getPrize(questionCount).getValue();
        Label prizeLabel = new Label("You won: " + prize + " PLN!");
        panel.addComponent(prizeLabel);

        Button okButton = new Button("OK");
        okButton.addListener(button -> {
            resetGame();
            showMainMenu();
        });
        panel.addComponent(new EmptySpace(new TerminalSize(0, 2)));
        panel.addComponent(okButton);

        window.setComponent(panel);
    }

    private void resetGame() {
        showMainMenu();
        questionCount = 0;
    }


    private void showStatistics() {
        List<AttemptEntry> userStats = gameManager.getUserStats(user.getUsername());

        Panel panel = new Panel();
        Table<String> table = new Table<>("Nick", "Prize", "Date");

        userStats.stream()
                .forEach(attemptEntry -> {
                    table.getTableModel().addRow(user.getNick(), attemptEntry.getPrize().toString(), attemptEntry.getDate().toString());
                });
        panel.addComponent(table);

        Button okButton = new Button("OK");
        okButton.addListener(button -> {
            showMainMenu();
        });
        panel.addComponent(okButton);
        window.setComponent(panel);
    }

    public static void main(String[] args) {
        App app = new App();
        app.start();
    }
}