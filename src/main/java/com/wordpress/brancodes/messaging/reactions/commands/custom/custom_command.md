# Custom Command Usage Details

Match Usage: ```(Alias\\s+)?(Create|Make|Add)\\s+(The\\s+)?(Custom\\s+)?Command\\s+When\\s+(.+)\\s+(Named|Called)\\s+(.+)\n?+(\`\`\`)?+(.+)(\`\`\`)?+``` Must specify the event, name, and code, optional: a regex to match user input

Dynamically weakly typed, call/get with period
delimit statements with newline/tabs (python) OR curly brackets (C)
remaining expression is returned (Scala)

### Script examples

Prepend with "Make Command" or "Run" with the desired Event to listen to
```js
if request equals request.server.owner // attempt to cast request to a user (the user that sent the request)
    if (group(0) == ban) // access to request message's regex match groups
        user.get(group(1)).ban
        send(banned them)
    else if (group(0) is kick)
        user.get(group(1)).kick
```
```js
if request.user == pigGuy22
    request.message.addReaction(pig)
```
"Run `for users if user name startswith z ban user`"

### Language details

Methods layout style: `return_type name(arg_type)`

* [Types](types)
  * `num`
  * `str`
  * `bool`
  * `url` (str)
  * `<type> gettable`
    * `static <type> get(any)`
  * `user`
    * `id`, `str name`, `url|id pfp`, `url|id banner`, `role(role)`, `ban()`, `kick()`, `nick(str)`
  * `channel`
    * `id`, `str name`, `send(str)`
  * `message`
    * `id`, `channel`, `user author`, `guild`, `list<user> mentionedUsers|channels|roles`?, `str time`, `url jump`, `react(emoji)`, `delete`, `pin`
  * `server`
    * `id`, `name`, `owner`
  * `emoji`
  * `list<type>`
    * `foreach`
  * `null`
  * `void`
* Operators
  * [Programmatic](tokens/ControlToken.java)
    * `.` - get field / call method on
    * `(...)` - method call
  * [Comparison](tokens/Operator.java)
    * `equals` | `==` | `is`
    * `===` (typed equals)
    * `not equals` | `!=` | `is not` | `isn't`
    * `!==` (typed not equals)
    * `less` | `<`
    * `greater`  `>`
    * `>=`
    * `<=`
  * [Boolean / Bitwise](tokens/Operator.java)
    * `not` | `!` | `~`
    * `and` | `&&` | `&`
[//]: # (  * math)

[//]: # (    * `+`, `-`, `*`, `/`, `%`, `^`)
* [Code control](tokens/ControlToken.java)
    * `if`
    * `else`
    * `return`
* Public functions
  * `any group(num)`
  * `user getUser(id)`
  * `channel getChannel(id)`
  * `send(str)`
  * `send(channel.id, str)`
* Public vars
  * `request` - the specified event that triggers this command
  * `bot` - if no event, then access to basic jda bot commands
