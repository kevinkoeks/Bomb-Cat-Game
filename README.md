# Software Design
This is our team project for the Software Design course at the Vrije Universiteit Amsterdam.

## Setting up the development environment

### JDK 11
This project should be developed with JDK 11.
Make sure that your [project settings](https://www.jetbrains.com/help/idea/configure-project-settings.html) are configured:
- to use JDK 11 as **Project SDK**; and
- **Project language level** set to 11 which has, as a description, "_Local variable syntax for lambda parameters_".

Make sure you configure the language level for all the modules of this project.

### Lombok
To use Lombok in IntelliJ, make sure you:
- [enable annotation processing](https://www.jetbrains.com/help/idea/annotation-processors-support.html) by just ticking the checkbox in the settings
- [install the Lombok plugin for IntelliJ](https://plugins.jetbrains.com/plugin/6317-lombok) from Settings > Plugins > Marketplace

## Generating the JAR
To build the artifact (JAR) in IntelliJ: Build > Build Artifacts > software-design-vu-2020 > Build

To run the JAR, from the folder in which you have the JAR execute:
```bash
$ java -cp software-design-vu-2020.jar nl.vu.group2.kittens.ExplodingKittens
```

## Bonus Features
We implemented some bonus features for our game.

### Networking
The game can be played over the network. Currently, we hardcoded host and port (localhost, port 8080).
To play a networked game, first start the hosting application, then join with the other instances of the application as shown in the demo.

### Custom Deck
Our application supports custom decks of cards.
You can find valid and invalid sample decks in `src/main/resources/decks/` (the name of invalid samples is clearly distinguishable).
If you want to create your own deck, just write a JSON file with the following format:
```json
{
  "EXPLODING_KITTEN": 5,
  "DEFUSE": 5,
  "NOPE": 5,
  "TACOCAT": 5000,
  "<CARD_NAME>": <CARD_COUNT>,
  ...
}
```
That is, a JSON object whose keys are the card names and the respective values are the amounts of cards of a given type.


### Scoreboard
We implemented a simple scoreboard so that at the end of the game we print out to the user interface the scores (who won, who came second, ...).
Also, the scoreboard is persisted to a `scoreboard.json` file.
