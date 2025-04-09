package ui;

import client.websocket.NotifHandler;
import exceptions.RespExp;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class RepL implements NotifHandler {
    private Client client;

    public RepL(String serverUrl) {
        this.client = new PreLogClient(serverUrl, this);
    }

    public void run() {
        System.out.println(WHITE_KING + "Welcome to 240 Chess by Tyler Zaugg. Type 'help' to begin." + BLACK_KING);
        Scanner scanner = new Scanner(System.in);
        String result = "";

        while (!result.equalsIgnoreCase("quit")) {
            showPrompt();
            String line = scanner.nextLine();
            result = client.eval(line);
            String[] split = result.split(";;");

            if (split[0].trim().equalsIgnoreCase("transition")) {
                if (split.length >= 3) {
                    try {
                        client = client.transition(split[1].trim());
                    } catch (RespExp e) {
                        handleWarning(new ErrorMessage("Error: Failed to transition: " + e.getMessage()));
                    }
                } else {
                    client = client.transition();
                }
            }

            System.out.print(split[split.length - 1]);
        }
    }

    private void showPrompt() {
        System.out.print("\n" + RESET + client.printState() + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    private void printPrompt() {
        System.out.print("\n" + RESET + client.printState() + ">>> " + SET_TEXT_COLOR_GREEN);
    }

    @Override
    public void handleNotif(NotificationMessage notification) {
        System.out.println("\n" + notification.getMessage());
        printPrompt();
    }

    @Override
    public void handleWarning(ErrorMessage error) {
        System.out.println("\n" + SET_TEXT_COLOR_RED + error.retrieveError());
        printPrompt();
    }

    @Override
    public void loadGame(LoadGameMessage game){
        System.out.print(client.loadGame(game.fetchGame()));
        printPrompt();
    }
}
