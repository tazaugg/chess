# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5T9qBACu2GADEaMBUljAASij2SKoWckgQaIEA7gAWSGBiiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9D4GUAA6aADeAETtlMEAtih9pX0wfQA0U7jqydAc45MzUyjDwEgIK1MAvpjCJTAFrOxclOX9g1AjYxNTs33zqotQyw9rfRtbO58HbE43FgpyOonKUCiMUyUAAFJForFKJEAI4+NRgACUh2KohOhVk8iUKnU5XsKDAAFUOrCbndsYTFMo1Kp8UYdOUAGJITgwamURkwHRhOnAUaYRnElknUG4lTlNA+BAIHEiFRsyXM0kwECQuQoflwm6MhnaKXqNnGcoKDgcPkdRmqqh406akmqcq6lD6hQ+MCpWHAP2pE0Ss1a1mnK0wG1233+x1g9W5U6Ai5lCJQpFQSKqJVYNPAmWFI6XGDXDp3SblVZPIP++oQADW6GrU32TsoxfgyHM5QATE4nN0K0MxWMYDXHlN66lGy20G3Vgd0BxPN4-IFoOxyTAADIQaJJAJpDJZXt5EvFMvVOpNVoGdQJNAjgaV8e7L4vN4fZedkGFIWZajrcH6fE835LJ+ALnEWspqig5QIIePKwgeR6ouisTYkmhiuuG7pkhShq0u+oymkSEaWhyMDcryhqCsKMCiuKbrSimV4IeUDHaE6Lqcc68owAAasAyBaJkMCQe8fHJgSBEsp6eqZPGAazqGbEWlGNGxjGwaJnKeGprBZboTyub5pgQEgvBJRXG+Y6jEu059LO86tpO7Z9AcpY2YU2R9jAg7Dr0DmgU5nm1jOwbuYukUdqu66+P4AReCg6D7oevjMCe6SZJgAWXkU1A3tIACie5lfUZXNC0j6qM+3Ruc26D-my1nlM1C5WSZAHFYJiEwMh9jZYGMUtWgOGGRqCnahwKDcCpwZjQ2E0UUy7rUeUkTDBANB6QmvG4d2HWZSNfoWQgBa9d2vkDkOMA1AA0m1HE9jkYD3U4j0vYlmBeMlgSQnae7QjAADi46srlZ4FRezC2Te4NVbV9jjk143db57W9Z1mOtdZbK4Uh0KQ6MqgrXOa2yXh8mUYRMBUGJSASSgqmU7F63mpGhTRnRcb6bxmmRrZ4IHSGR3Tfh9OKTA5JgAoypk2osJc1R2lchSICpDAYkIBD46OsLt2GeUyuMqDsQ0yduMG6MltgJd11Al2b13eWfRo+T4yVC4fvND515+e9gXBa+XtqD7FR+y4AdmJwSWbgE2A+FA2DcPAymGMrKR5eeH1E0H5S3g0qPo8E+MvqFEcAHJgX+2PGS7GZdegsy1+OPXN4XXE6lnyuwnA-fjphGJTQhM0y9qTPiT6y2t5NYZTzz7JcjyAuHfIS8bexotCapBkT9LO-al6+oDxHGmzSv0a6ebktH036blEP3qZMrTtd+mJslRm-QRwASWkM5byr1TiFU+kFIc4dxxAJASuBO-0NwpUsAtZCyQYAACkIA8jtoYAIOgECgCbHDAuiMMzVEpPeFoEcMarQXK+dOwBUFQDgBAZCUBPxPEAcA-4r1AK2wXrMJhLC2EcPbrA6QX84ICTFgAKxwWgAe2CeQfxQGiMe1tj7c3KDPFmc9-Qc2psbDWtF17iyNtfH+A1rSCy3sdbREYiJgAvpItWm1TH8zwYxMIPDt7c2sWLe+9ipayKEqJWekkkAADMpJENEew6AMAYi6yZtsXQ3AtF0xPh6Zi2BWauNGFfZeW0+T5P1N4h+A0bbN3KCopRI81CWUJm7IuUDvrPTAf5eGX0frxzXEgwGqUoDMJ7N6WAChsDp0IPERIudYYQJ7nZSo5VKrVVqsYfhZxaljLwDIbQegDCZg0VbFpYTBogG4HgRkhyUCqyyfs5enorlQEVggQ0FN3HsV5jRHae1DB63tAKKp-FHk5OeeMt5ytPn+PVj87aGx-m62VJUkJR9sk6J1C8t5qlYowpMfCzMu19qAoPiC5MT9gQvxeWQHwEZpGu3Ib0zpjdwE9PaX0xKQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
