usage: ```Make Command:?[\s`]*([\w\W])[\s`]*+```

dynamically weakly typed, call/get with period (js), delimit statements with newline/tabs (python) OR curly brackets (C)

layout style: `return_type name(arg_type)`
* types
  * `num`
  * `str`
  * `bool`
  * `id` (long/String)
  * `url` (str)
  * `<type> gettable`
    * `static <type> get(any)`
  * `user` gettable
    * `id`, `str name`, `url|id pfp`, `url|id banner`, `role(role)`, `ban`, `kick`, `nick`
  * `channel` gettable
    * `id`, `str name`, `send(str)`
  * `message` gettable
    * `id`, `channel`, `user author`, `guild`, `list<user> mentionedUsers|channels|roles`?, `str time`, `url jump`, `react(emoji)`
      perms needed: `delete`, `pin`
  * `guild` gettable
    * `id`, `name`, `owner`
  * `emoji` gettable
  * `list<type>`
    * `foreach` varName {
  * `null`
* operators
  * programmatic
    * `.` - get field / call method on
    * `(...)` - method call
  * comparison
    * `equals` | `==` | `is`
    * `not equals` | `!=` `is not` | `isn't`
    * `less` | `<`
    * `greater`  `>`
    * `>=`, `<=`

[//]: # (  * math)

[//]: # (    * `+`, `-`, `*`, `/`, `%`, `^`)
* code control
    * `if` bool statement
    * `else`
* public functions
  * `any group(num)`
  * `user getUser(id)`
  * `channel getChannel(id)`
  * `send(str)`
  * `send(channel.id, str)`
* public vars
  * `message request`
@param input must specify the regex / periodic, name, channel, and code
no regex input: just split by spaces

examples:
```js
if request == request.guild.owner
    if (group(0) == ban)
        user.get(group(1)).ban
        send(banned 'em)
    else if (group(0) == kick)
        user.get(group(1)).kick
else failure
```
```js
if request.user == pigGuy22
    request.message.addReaction(pig)
```
