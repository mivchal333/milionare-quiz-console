import api.MillionaireApi;
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
import service.ConfigurationService;
import service.GameManager;
import service.LoginService;
import service.PrizesService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class App {
    private Window window;
    private WindowBasedTextGUI textGUI;
    private final PrizesService prizesService = new PrizesService();
    private final GameManager gameManager = new GameManager();
    private final ConfigurationService configurationService = new ConfigurationService();
    private List<Question> questions;
    Screen screen;
    private final LoginService loginService = new LoginService();
    private User user;
    private int questionCount = 0;

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

    void showAuthView() {

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

        leftPanel.addComponent(new EmptySpace(new TerminalSize(0, 1)));
        new Button("CLOSE", this::handleClose).addTo(leftPanel);


        window.setComponent(mainPanel.withBorder(Borders.singleLine("Please login or register")));

    }

    private Runnable handleRegisterAttempt(Panel rightPanel, TextBox usernameRegister, TextBox passwordRegister, TextBox nickRegister, Label registerResult) {
        return () -> {
            String username = usernameRegister.getText();
            String password = passwordRegister.getText();
            String nick = nickRegister.getText();
            boolean result = loginService.attemptRegister(username, password, nick);
            if (result) {
                new MessageDialogBuilder()
                        .setTitle("Successful")
                        .setText("Now you can sign in :)")
                        .addButton(MessageDialogButton.Continue)
                        .build()
                        .showDialog(textGUI);
                showAuthView();
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

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.start();
    }

    public void start() throws IOException {
        String port = configurationService.readPort();
        MillionaireApi.setApiPort(port);
        try {
            setUpWindow();

            welcomeDialog();

            showAuthView();

            textGUI.addWindowAndWait(window);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showMainMenu() {
        window.setHints(Collections.singletonList(Window.Hint.CENTERED));

        Panel panel = new Panel();
        panel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        Button playButton = new Button("PLAY");
        playButton.addListener(button -> playGame());
        panel.addComponent(playButton);

        Button statisticsButton = new Button("STATISTICS");
        statisticsButton.addListener(button -> showStatistics());
        panel.addComponent(statisticsButton);

        Button addQuestion = new Button("ADD QUESTION");
        addQuestion.addListener(button -> showAddQuestionForm());
        panel.addComponent(addQuestion);

        Button exitButton = new Button("CLOSE");
        exitButton.addListener(button -> handleClose());
        panel.addComponent(exitButton);

        window.setComponent(panel);
    }

    private void showAddQuestionForm() {

        Panel panel = new Panel();

        panel.addComponent(new Label("Question"));
        TextBox question = new TextBox().addTo(panel);

        panel.addComponent(new Label("Correct Answer"));
        TextBox correctAnswer = new TextBox().addTo(panel);

        panel.addComponent(new Label("Incorrect Answer 1"));
        TextBox incorrectAnswer1 = new TextBox().addTo(panel);

        panel.addComponent(new Label("Incorrect Answer 2"));
        TextBox incorrectAnswer2 = new TextBox().addTo(panel);

        panel.addComponent(new Label("Incorrect Answer 3"));
        TextBox incorrectAnswer3 = new TextBox().addTo(panel);


        new Button("SUBMIT", () -> this.handlePostNewFormAction(question.getText(), correctAnswer.getText(), incorrectAnswer1.getText(), incorrectAnswer2.getText(), incorrectAnswer3.getText())).addTo(panel);

        new Button("CLOSE", this::showMainMenu).addTo(panel);

        window.setComponent(panel);
    }

    private void handleClose() {
        try {
            screen.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        RadioBoxList<String> radioBoxList = new RadioBoxList<>(new TerminalSize(50, 10));

        List<String> answers = question.getIncorrectAnswers();
        answers.add(question.getCorrectAnswer());
        Collections.shuffle(answers);
        answers.forEach(radioBoxList::addItem);
        panel.addComponent(radioBoxList);

        Button submit = new Button("Submit");
        submit.addListener(button -> processAnswer(radioBoxList.getCheckedItem()));
        panel.addComponent(submit);

        window.setHints(Collections.singletonList(Window.Hint.FULL_SCREEN));
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

        userStats
                .forEach(attemptEntry -> table.getTableModel().addRow(user.getNick(), attemptEntry.getPrize().toString(), attemptEntry.getDate().toString()));
        panel.addComponent(table);

        Button okButton = new Button("OK");
        okButton.addListener(button -> showMainMenu());
        panel.addComponent(okButton);
        window.setComponent(panel);
    }

    void handlePostNewFormAction(String questionContent, String currentAnswer, String incorrectAnswer1, String incorrectAnswer2, String incorrectAnswer3) {
        gameManager.postNewQuestion(questionContent, currentAnswer, incorrectAnswer1, incorrectAnswer2, incorrectAnswer3);
        new MessageDialogBuilder()
                .setTitle("Success")
                .setText("New Question Saved!")
                .addButton(MessageDialogButton.OK)
                .build()
                .showDialog(textGUI);
        showMainMenu();
    }
}
