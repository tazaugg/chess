package ui;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class RepL {
    private Client client;

    public RepL(String serverUrl) {
        this.client = new PreLogClient(serverUrl);
    }

    public void run() {
        System.out.println(WHITE_KING + "Welcome to 240 Chess by Tyler Zaugg. Type 'help' to begin." + BLACK_KING);
        Scanner scanner = new Scanner(System.in);
        String userInput = "";

        while (!userInput.equalsIgnoreCase("quit")) {
            displayPrompt();
            userInput = scanner.nextLine();
            String response = client.eval(userInput);
            String[] parts = response.split(";;");

            if (parts[0].trim().equalsIgnoreCase("transition")) {
                client = (parts.length >= 3) ? client.transition(parts[1].trim()) : client.transition();
            }
            System.out.print(parts[parts.length - 1]);
        }
    }

    private void displayPrompt() {
        System.out.print("\n" + RESET + client.printState() + ">>> " + SET_TEXT_COLOR_GREEN);
    }
}
