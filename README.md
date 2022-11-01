# LiquidRichardDiscordBot
Made with JDA

When worked on: March 2021 - April 2021, various times after to update

- - -

### Features

* Dynamic commands
  * Activate with regex
  * Imperative - Activate them by speaking to it like a sentence
    * Example: Help command could be: "Hello Richard, Can You Help Me Out Please?" or "Yo, Help Him Out Here, Richard"
  * Choose whether it's in DMs or in a server and who gets to activate it
  * They all work in Morse code too
  * Will randomly deny your command, and you have to try again
  * Natural randomized responses with Generex library
  * [Some commands it can already do (click to see full docs):](build/resources/main/docs.md)
    * Get the DM history it has with a user
    * Clever auto delete
      * Delete after a set time
      * Covers get-around censoring like "b.Ä,D_w.0.Яd"
    * Say something into any channel/DM anybody something
    * Join a voice channel
    * Help panel
    * Get info on the usage of a command
    * Slash Command utility support
      * Short-cuts for the repeat parts from creation and running phase slash command
    * Incorporation of my math library (evaluate, derive, simplify, statements, randomly generate, etc.)
    * Create you own command with custom parser language (WIP)
* Generic reactions
  * Message reactions (emojis)
  * Periodic messages
* Database

## Example usage

To add a command that just says "Hi." back to someone when they say "Hi",
you would call addCommand in [ReactionManager.java](src/main/java/com/wordpress/brancodes/messaging/reactions/ReactionManager.java) with
```java
new CommandBuilder("Hello",             // unique name
                    "^hi\\s",           // regex to activate it
                    DEFAULT,            // who can use it (here, anybody can)
                    GUILD_AND_PRIVATE)  // where it can be used
       .execute(message -> reply(message, "Hi.")) // the response code
       .helpPanel("Make A Greeting")    // add to help panel with description
       .deniable()                      // randomly choose to ignore their greeting
       .deactivated()                   // command is not on by default
       .caseInsensitive()               // like [Hh][Ii]
       .addMemberCooldown(3000L)        // 3 second cool down per person
       .build()
```
or, for a slash command,
```java
new SlashCommandBuilder("trig", "Evaluate Trig Expression.", DEFAULT, GUILD_AND_PRIVATE)
    .createSubcommandBranch(new SubcommandData("info", "Show Details."),
        event -> event.reply("Input Sin, Cos, Or Tan And A Number As Its Argument To Get A Calculation.").queue())
    .createSubcommandBranch(new SubcommandData("calc", "Execute Trig Function.")
    .addOptions(new OptionData(OptionType.STRING, "func", "function", true)
    .addChoice("sin", "sin")
    .addChoice("cos", "cos")
    .addChoice("tan", "tan"))
    .addOption(OptionType.NUMBER, "arg", "valued to be calculated upon", true),
        event -> {
            double arg = event.getOption("arg").getAsDouble();
            double res = switch (event.getOption("func").getAsString()) {
                case "sin" -> Math.sin(arg);
                case "cos" -> Math.cos(arg);
                case "tan" -> Math.tan(arg);
                default -> 0;
            };
            event.reply(String.valueOf(res)).queue();})
    .build(),
```
To add a periodic message that says "Good morning" every day plus or minus an hour,
you would add this to the list of Chats in Chats.java:

```java
new VariatedChat(() -> mainChannel.sendMessage("Good morning").queue(),
    86_400_000L,  // the period (milliseconds in a day)
     3_600_000L), // variance (milliseconds in an hour)
```

### [Custom Commands (WIP)](src/main/java/com/wordpress/brancodes/messaging/reactions/message/commands/custom/custom_command.md)

Comes with a custom script that users can use to create commands or execute once


