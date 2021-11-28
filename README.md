# LiquidRichardDiscordBot
with JDA

When worked on: March 2021 - April 2021

- - -

### Features

* Add commands dynamically
  * Activate with regex
  * Imperitave - Activate them by speaking to it like a sentence
    * Example: Help command could be: "Hello Richard, Can You Help Me Out Please?" or "Yo Help Him Out Here Richard"
  * Choose whether it's in DMs or in a server and who gets to activate it
  * They all work in Morse code too
  * Will randomly deny your command and you have to try again
  * Natural randomized responses with Generex library
  * Some commands it can already do:
    * Get the DM history it has with a user
    * Say something into any channel/DM anybody something
    * Join a voice channel
    * Help panel
* Generic reactions
  * Message reactions
  * Periodic messages
* Database

### Example usage

To add a command that just says "Hi." back to someone when they say "Hi",
you would add this to the list of all commands in Commands.java:
```java
new Command("\\s*[Hh][Ii]\\s*",    // regex to activate it
"Hello",                           // name
"Make A Greeting",                 // description (for help panel)
DEFAULT,                           // who can use it (anybody can here)
GUILD_AND_PRIVATE,                 // where it can be used
message -> reply(message, "Hi.")), // the actual code for it
```

To add a periodic message that says "Good morning" every day plus or minus an hour,
you would add this to the list of Chats in Chats.java:
```java
new VariatedChat(() -> mainChannel.sendMessage("Good morning").queue(),
86_400_000L,  // the period (milliseconds in a day)
 3_600_000L), // variance (milliseconds in an hour)
```
