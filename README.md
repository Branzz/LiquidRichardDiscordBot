# LiquidRichardDiscordBot
Made with JDA

When worked on: March 2021 - April 2021, March 2022

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
  * Some commands it can already do:
    * Get the DM history it has with a user
    * Clever auto purge
      * Delete after a set time
      * Covers get-around censoring like "b.Ä,D_w.0.Яd"
    * Say something into any channel/DM anybody something
    * Join a voice channel
    * Help panel
    * Get info on the usage of a command
    * Create you own command with custom parser language (WIP)
* Generic reactions
  * Message reactions (emojis)
  * Periodic messages
* Database

### Example usage

To add a command that just says "Hi." back to someone when they say "Hi",
you would add this to the list of all commands in Commands.java:
```java
new Command.Builder("Hello",            // unique name
                    "[Hh][Ii]\\s",      // regex to activate it
                    DEFAULT,            // who can use it (anybody can here)
                    GUILD_AND_PRIVATE)  // where it can be used
       .execute(message -> reply(message, "Hi.")) // the response code
       .helpPanel("Make A Greeting")    // add to help panel with description
       .deniable()                      // randomly choose to ignore their greeting
       .deactivated()                   // command is not on by default
       .build()
```

To add a periodic message that says "Good morning" every day plus or minus an hour,
you would add this to the list of Chats in Chats.java:
```java
new VariatedChat(() -> mainChannel.sendMessage("Good morning").queue(),
86_400_000L,  // the period (milliseconds in a day)
 3_600_000L), // variance (milliseconds in an hour)
```
