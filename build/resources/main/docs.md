## Reactions and Command docs
### Shut Down
Shut Me Off
#### User
Bot Owner
#### Location
Guild And DM
#### Docs
Complete the queued auto-deletes, then shut down the bot and the server
#### RegEx
(?:\\s\*((Yo|Hey|Ok|Alright|All\\s+Right|Hi|Hello)\\s\*(,|\\.+|!+|\\s)?\\s\*)?((null\\s\*(\\?+|\\.+|,|!+)?\\s+(((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*(((Turn)?\\s\*Off)|(Shut\\s\*(Down|Off|Up))))|((((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*(((Turn)?\\s\*Off)|(Shut\\s\*(Down|Off|Up)))\\s\*,?\\s+null))(\\s+(Please)+)\\s\*(\\?+|\\.+|,|!+)?\\s\*(\\s+(Thanks|Thank\\s+You)\\s\*(\\.+|!+)?)?\\s\*)
### Restart
Restart Me
#### User
Bot Owner
#### Location
Guild And DM
#### RegEx
(?:\\s\*((Yo|Hey|Ok|Alright|All\\s+Right|Hi|Hello)\\s\*(,|\\.+|!+|\\s)?\\s\*)?((null\\s\*(\\?+|\\.+|,|!+)?\\s+(((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*Restart)|((((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*Restart\\s\*,?\\s+null))(\\s+(Please)+)\\s\*(\\?+|\\.+|,|!+)?\\s\*(\\s+(Thanks|Thank\\s+You)\\s\*(\\.+|!+)?)?\\s\*)
### Help
Help On Commands (This Panel)
#### User
Public
#### Location
Guild And DM
#### Cooldown
10 Seconds For Message Channels
#### Docs
Send compilation of any commands that have a help panel option by sections of "(Role) in (Channel)"
#### RegEx
(?:\\s\*((Yo|Hey|Ok|Alright|All\\s+Right|Hi|Hello)\\s\*(,|\\.+|!+|\\s)?\\s\*)?((null\\s\*(\\?+|\\.+|,|!+)?\\s+(((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*Help(\\s+(Me|Him|Her|It|Us|Every(one|body|\\s+(One|Body))|Them)(\\s+Out)?)?(\\s+Here)?(\\s+Right\\s+Now)?)|((((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*Help(\\s+(Me|Him|Her|It|Us|Every(one|body|\\s+(One|Body))|Them)(\\s+Out)?)?(\\s+Here)?(\\s+Right\\s+Now)?\\s\*,?\\s+null))(\\s+(Please)+)\\s\*(\\?+|\\.+|,|!+)?\\s\*(\\s+(Thanks|Thank\\s+You)\\s\*(\\.+|!+)?)?\\s\*)
### Say In
#### User
Moderator
#### Location
Guild And DM
#### RegEx
!s\\s\*(\\d{18,20})(\\D[\\S\\s]+)
### Say Here
#### User
Moderator
#### Location
Guild
#### Docs
Send a matching message
#### RegEx
!s[\\S\\s]+
### Say Proper
#### User
Moderator
#### Location
Guild
#### Docs
Send matching message converted to Proper Case
#### RegEx
^!p[\\S\\s]+
### DM
#### User
Moderator
#### Location
Guild And DM
#### Docs
Send message to user
#### Example
!D 885535338415556989 Yo
#### RegEx
^!d\\s\*(\\d{17,20})(\\D[\\S\\s]+)
### Join Voice
#### User
Moderator
#### Location
Guild And DM
#### Docs
Join a voice channel by ID
#### Example
!J 439422552544123982
#### RegEx
^!j\\s\*(\\d{18,20})[\\S\\s]\*
### Disconnect
#### User
Moderator
#### Location
Guild
#### Docs
Leave its current voice channel in that guild
#### Example
!di
#### RegEx
!di|Disconnect\\.?
### Servers
#### User
Bot Owner
#### Location
DM
#### RegEx
!v\\s\*
### Get Channels
#### User
Bot Owner
#### Location
Guild
#### RegEx
!c\\s\*
### Get Commands
#### User
Moderator
#### Location
Guild And DM
#### Docs
Show all the commands
#### Example
Yo Pimp Show Us All The Commands
#### Example
Pimp Get Commands
#### RegEx
!m|(?:\\s\*((Yo|Hey|Ok|Alright|All\\s+Right|Hi|Hello)\\s\*(,|\\.+|!+|\\s)?\\s\*)?((null\\s\*(\\?+|\\.+|,|!+)?\\s+(((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*(Get|Tell|Show|Give)\\s+(Me|Him|Her|It|Us|Every(one|body|\\s+(One|Body))|Them)?((Every|All)\\s+)?(The\\s+)?Commands)|((((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*(Get|Tell|Show|Give)\\s+(Me|Him|Her|It|Us|Every(one|body|\\s+(One|Body))|Them)?((Every|All)\\s+)?(The\\s+)?Commands\\s\*,?\\s+null))(\\s+(Please)+)\\s\*(\\?+|\\.+|,|!+)?\\s\*(\\s+(Thanks|Thank\\s+You)\\s\*(\\.+|!+)?)?\\s\*)
### Disable Command
#### User
Moderator
#### Location
Guild
#### RegEx
!dc\\s+([\\W\\w]+)
### Enable Command
#### User
Bot Owner
#### Location
Guild
#### RegEx
!ec\\s+([\\W\\w]+)
### Get Role
#### User
Public
#### Location
Guild
#### RegEx
(!r)|((?:\\s\*((Yo|Hey|Ok|Alright|All\\s+Right|Hi|Hello)\\s\*(,|\\.+|!+|\\s)?\\s\*)?((null\\s\*(\\?+|\\.+|,|!+)?\\s+(((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*((Get|Tell|Show|Give)\\s+(Me|Him|Her|It|Us|Every(one|body|\\s+(One|Body))|Them)?|What(\\s+I|')s)\\s+(My\\s+)?(Role|Position))|((((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*((Get|Tell|Show|Give)\\s+(Me|Him|Her|It|Us|Every(one|body|\\s+(One|Body))|Them)?|What(\\s+I|')s)\\s+(My\\s+)?(Role|Position)\\s\*,?\\s+null))(\\s+(Please)+)\\s\*(\\?+|\\.+|,|!+)?\\s\*(\\s+(Thanks|Thank\\s+You)\\s\*(\\.+|!+)?)?\\s\*))
### DM History
#### User
Bot Owner
#### Location
Guild And DM
#### RegEx
!h\\s+\\d{1,20}\\s+\\d+\\s\*
### Nick All
#### User
Moderator
#### Location
Guild
#### Docs
Give nick name to all mentioned members
#### Example
Nick "Spammers" @THEg#1534 @bottttt#9999
#### RegEx
Nick\\s\*"([\\w]{2,32})"[\\s\\S]+
### Kick All
#### User
Moderator
#### Location
Guild
#### Docs
Kick all mentioned members
#### Example
Kick @hijkl89#1508 @conor#0009 @aoeu#4035
#### RegEx
Kick[\\s\\S]+
### Info
Guide On Activating A Command
#### User
Moderator
#### Location
Guild
#### Docs
Send info about a command, i.e. its required role, location, RegEx (the message that activates it), any cooldowns, and info from the help panel, docs, or examples
#### Example
Ok Pimp, Tell Me About The Greeting Command. Thanks.
#### Example
Pimp Define Command Info
#### RegEx
(?:\\s\*((Yo|Hey|Ok|Alright|All\\s+Right|Hi|Hello)\\s\*(,|\\.+|!+|\\s)?\\s\*)?((null\\s\*(\\?+|\\.+|,|!+)?\\s+(((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*(Tell\\s+(Me|Him|Her|It|Us|Every(one|body|\\s+(One|Body))|Them)(\\s+What|\\s+About)?|Define|What\\s+Is)\\s+(The\\s+)?(Command\\s+(.{1,16})|(.{1,16})\\s+Command)(\\s+Is)?)|((((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*(Tell\\s+(Me|Him|Her|It|Us|Every(one|body|\\s+(One|Body))|Them)(\\s+What|\\s+About)?|Define|What\\s+Is)\\s+(The\\s+)?(Command\\s+(.{1,16})|(.{1,16})\\s+Command)(\\s+Is)?\\s\*,?\\s+null))(\\s+(Please)+)\\s\*(\\?+|\\.+|,|!+)?\\s\*(\\s+(Thanks|Thank\\s+You)\\s\*(\\.+|!+)?)?\\s\*)
### Example
Give Example On How To Activate A Command
#### User
Public
#### Location
Guild And DM
#### RegEx
(?:\\s\*((Yo|Hey|Ok|Alright|All\\s+Right|Hi|Hello)\\s\*(,|\\.+|!+|\\s)?\\s\*)?((null\\s\*(\\?+|\\.+|,|!+)?\\s+(((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*(Tell|Show|Give)\\s+((Me|Him|Her|It|Us|Every(one|body|\\s+(One|Body))|Them)\\s+)?(An?\\s+)?Example\\s+((For|Of)\\s+)?(The\\s+)?(Command\\s+(.{1,16})|(.{1,16})\\s+Command))|((((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*(Tell|Show|Give)\\s+((Me|Him|Her|It|Us|Every(one|body|\\s+(One|Body))|Them)\\s+)?(An?\\s+)?Example\\s+((For|Of)\\s+)?(The\\s+)?(Command\\s+(.{1,16})|(.{1,16})\\s+Command)\\s\*,?\\s+null))(\\s+(Please)+)\\s\*(\\?+|\\.+|,|!+)?\\s\*(\\s+(Thanks|Thank\\s+You)\\s\*(\\.+|!+)?)?\\s\*)
### Custom Command
#### User
Moderator
#### Location
Guild And DM
#### Deactivated
‎
#### RegEx
(Pimp\\s+)?(Create|Make|Add)\\s+(The\\s+)?(Custom\\s+)?Command\\s+When\\s+(.+)\\s+(Named|Called)\\s+(.+)
?+(```)?+(.+)(```)?+
### Greeting
#### User
Public
#### Location
Guild And DM
#### Cooldown
5 Seconds For Message Channels
#### RegEx
(null\\s\*(\\?+|\\.+|,|!+)?\\s+(Greetings|Sup|Hi|Hey|Hello|Yo))|((Greetings|Sup|Hi|Hey|Hello|Yo)\\s\*[,.]?\\s+null)\\s\*[.!\\s]\*
### Auto Delete
#### User
Liquid Richard
#### Location
Guild
#### RegEx
^?(((?!(\\d){5})[aA5\\u0410\\u1EA0\\u0104\\u00C4\\u00C0\\u00C1\\u0104\\u0430\\u1EA1\\u0105\\u00E4\\u00E0\\u00E1\\u0105]+[!@#$%^&\*()\\[\\]/=\\-\\\\;',.{}?+|S_:"\\s]\*[rR\\u044F\\u042F]+[!@#$%^&\*()\\[\\]/=\\-\\\\;',.{}?+|S_:"\\s]\*[yY\\u0423\\u00DD\\u0443\\u00FD]+[!@#$%^&\*()\\[\\]/=\\-\\\\;',.{}?+|S_:"\\s]\*[aA5\\u0410\\u1EA0\\u0104\\u00C4\\u00C0\\u00C1\\u0104\\u0430\\u1EA1\\u0105\\u00E4\\u00E0\\u00E1\\u0105]+[!@#$%^&\*()\\[\\]/=\\-\\\\;',.{}?+|S_:"\\s]\*[nN\\u0548\\u0578\\u0438\\u0418\\u0439\\u0419]+)|((?!(\\d){6})[bB8\\u042C\\u044C\\u0431\\u0411\\u0432\\u0412]+[!@#$%^&\*()\\[\\]/=\\-\\\\;',.{}?+|S_:"\\s]\*[eE3\\u0415\\u1EB8\\u0116\\u00C9\\u00C8\\u00CA\\u0435\\u1EB9\\u0117\\u00E9\\u00E8\\u00EA]+[!@#$%^&\*()\\[\\]/=\\-\\\\;',.{}?+|S_:"\\s]\*[aA5\\u0410\\u1EA0\\u0104\\u00C4\\u00C0\\u00C1\\u0104\\u0430\\u1EA1\\u0105\\u00E4\\u00E0\\u00E1\\u0105]+[!@#$%^&\*()\\[\\]/=\\-\\\\;',.{}?+|S_:"\\s]\*[nN\\u0548\\u0578\\u0438\\u0418\\u0439\\u0419]+[!@#$%^&\*()\\[\\]/=\\-\\\\;',.{}?+|S_:"\\s]\*[eE3\\u0415\\u1EB8\\u0116\...
### Censor Japanese
#### User
Public
#### Location
Guild
#### RegEx
[一-龠ぁ-ゔァ-ヴー々〆〤]
### Purge All
Purge Censor All Channels
#### User
Moderator
#### Location
Guild
#### RegEx
(?:\\s\*((Yo|Hey|Ok|Alright|All\\s+Right|Hi|Hello)\\s\*(,|\\.+|!+|\\s)?\\s\*)?((null\\s\*(\\?+|\\.+|,|!+)?\\s+(((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*Purge(\\s+(Every)\\s+(Chat|Channel))?)|((((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*Purge(\\s+(Every)\\s+(Chat|Channel))?\\s\*,?\\s+null))(\\s+(Please)+)\\s\*(\\?+|\\.+|,|!+)?\\s\*(\\s+(Thanks|Thank\\s+You)\\s\*(\\.+|!+)?)?\\s\*)
### Purge Current
Purge Censor Current Channel
#### User
Moderator
#### Location
Guild
#### RegEx
(?:\\s\*((Yo|Hey|Ok|Alright|All\\s+Right|Hi|Hello)\\s\*(,|\\.+|!+|\\s)?\\s\*)?((null\\s\*(\\?+|\\.+|,|!+)?\\s+(((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*Purge\\s+(The|This)?\\s+(Chat|Channel|(Right )?Here))|((((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*Purge\\s+(The|This)?\\s+(Chat|Channel|(Right )?Here)\\s\*,?\\s+null))(\\s+(Please)+)\\s\*(\\?+|\\.+|,|!+)?\\s\*(\\s+(Thanks|Thank\\s+You)\\s\*(\\.+|!+)?)?\\s\*)
### Prune
#### User
Moderator
#### Location
Guild
#### RegEx
pimp\\s+prune
### Convert Units
#### User
Public
#### Location
Guild And DM
#### Cooldown
1 Seconds For Message Channels
#### RegEx
(?<!^[?.!]mute\\s{1,5}\\S{1,30}\\s{1,5}\\d{0,10})(?<!https://\\S{0,1990})(?<negs>-\*)(?<!\\$)(?:(?<feetInch>(?<base>(?<feet>\\d+)(?:['‘’]|\\s\*(?:foot|feet|ft\\.?)\\s\*))(?:(?<inch>(?<whole>\\d+)(?:\\.(?<inchDec1>\\d\*))?|\\.(?<inchDec2>\\d+))|[^'‘’s]))|(?<value>(?<valWhole>\\d+)(?:\\.(?<valDec1>\\d\*))?|\\.(?<valDec2>\\d+))\\s\*(?:(?:something|ish|~)\\s\*)?(?<unit>(kg|lb|meter|cm)s?([\\W]+|$)|kilo([sg\\s]|$)\\w\*|(['‘’]{2}|["“”″]|in(\\.|ch)|pound)\\w\*)?)
### Delete Ping
#### User
Censor List
#### Location
Guild And DM
#### Deactivated
‎
#### RegEx
.\*
### Birthday
Celebrate The Princess's Birthday
#### User
Public
#### Location
Guild
#### RegEx
It'?s Time To Celebrate\\.?
### As Image
Add Speech Bubble To Image
#### User
Public
#### Location
Guild And DM
#### Docs
Resend any image, predicted in order by (if multiple: the first of):attachment, sticker, emote, (same search, but on a) message link, (same search, but on the) replied message, pfp of referenced member, most recent attachment in chat
#### RegEx
^(pimp\\s\*image|pi$)
### Speech Bubble
Add Speech Bubble To Image
#### User
Public
#### Location
Guild And DM
#### Docs
See "As Image" docs, but adds scaled transparent speech bubble
#### Example
Speech Bubble
#### Example
sb
#### RegEx
^(speech\\s\*bubble|sb$)
### Harita
#### User
Public
#### Location
Guild
#### RegEx
haritard
### Embed Fail
#### User
Public
#### Location
Guild
#### RegEx
https://(www\\.)?tenor\\.com/view/(.)+
### Get Mods
Get Mods In Server
#### User
Moderator
#### Location
Guild
#### Deactivated
‎
#### RegEx
(?:\\s\*((Yo|Hey|Ok|Alright|All\\s+Right|Hi|Hello)\\s\*(,|\\.+|!+|\\s)?\\s\*)?((null\\s\*(\\?+|\\.+|,|!+)?\\s+((What|Who)\\s+(Are|Is)|(Whose|Who'?s|Who're))\\s+(The|A|An)\\s+(Mod|Moderator)s?\\s\*(\\s((In\\s+(This|The)\\s+(Server|Guild|Place))|Here))?[?.]\*\\s\*)|(((What|Who)\\s+(Are|Is)|(Whose|Who'?s|Who're))\\s+(The|A|An)\\s+(Mod|Moderator)s?\\s\*(\\s((In\\s+(This|The)\\s+(Server|Guild|Place))|Here))?[?.]\*\\s\*\\s\*,?\\s+null))(\\s+(Please)+)\\s\*(\\?+|\\.+|,|!+)?\\s\*(\\s+(Thanks|Thank\\s+You)\\s\*(\\.+|!+)?)?\\s\*)
### Give Mod
Give Moderator To User
#### User
Bot Owner
#### Location
Guild
#### Deactivated
‎
#### RegEx
(?:\\s\*((Yo|Hey|Ok|Alright|All\\s+Right|Hi|Hello)\\s\*(,|\\.+|!+|\\s)?\\s\*)?((null\\s\*(\\?+|\\.+|,|!+)?\\s+(((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*((((Make|Set|Give)\\s\*)@.{1,32}\\s\*(A\\s+)?(Mod|Moderator))|(((Make|Set|Give)\\s\*)?(Mod|Moderator)\\s+@.{1,32})))|((((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*((((Make|Set|Give)\\s\*)@.{1,32}\\s\*(A\\s+)?(Mod|Moderator))|(((Make|Set|Give)\\s\*)?(Mod|Moderator)\\s+@.{1,32}))\\s\*,?\\s+null))(\\s+(Please)+)\\s\*(\\?+|\\.+|,|!+)?\\s\*(\\s+(Thanks|Thank\\s+You)\\s\*(\\.+|!+)?)?\\s\*)
### Remove Mod
Remove A Moderator
#### User
Bot Owner
#### Location
Guild
#### Deactivated
‎
#### RegEx
(?:\\s\*((Yo|Hey|Ok|Alright|All\\s+Right|Hi|Hello)\\s\*(,|\\.+|!+|\\s)?\\s\*)?((null\\s\*(\\?+|\\.+|,|!+)?\\s+(((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*(((Remove|Re Move|Take)\\s\*@.{1,32}\\s\*('?s\\s+)?(Mod|Moderator))|(((Remove|Re Move|Take)\\s\*)?(Mod|Moderator)\\s\*@.{1,32})))|((((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*(((Remove|Re Move|Take)\\s\*@.{1,32}\\s\*('?s\\s+)?(Mod|Moderator))|(((Remove|Re Move|Take)\\s\*)?(Mod|Moderator)\\s\*@.{1,32}))\\s\*,?\\s+null))(\\s+(Please)+)\\s\*(\\?+|\\.+|,|!+)?\\s\*(\\s+(Thanks|Thank\\s+You)\\s\*(\\.+|!+)?)?\\s\*)
### Main Channel
Set Main Channel
#### User
Moderator
#### Location
Guild
#### Deactivated
‎
#### RegEx
(?:\\s\*((Yo|Hey|Ok|Alright|All\\s+Right|Hi|Hello)\\s\*(,|\\.+|!+|\\s)?\\s\*)?((null\\s\*(\\?+|\\.+|,|!+)?\\s+(((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*((((Make|Set)\\s\*)#.{1,32}\\s\*((The|A)\\s+)?(Main\\s+Channel))|((Make|Set)\\s+((The|A)\\s+)?(Main\\s+Channel)\\s\*#.{1,32})))|((((Can\\s+|Could\\s+)You\\s+)|(You\\s+Should\\s+))?(Please\\s+)\*((((Make|Set)\\s\*)#.{1,32}\\s\*((The|A)\\s+)?(Main\\s+Channel))|((Make|Set)\\s+((The|A)\\s+)?(Main\\s+Channel)\\s\*#.{1,32}))\\s\*,?\\s+null))(\\s+(Please)+)\\s\*(\\?+|\\.+|,|!+)?\\s\*(\\s+(Thanks|Thank\\s+You)\\s\*(\\.+|!+)?)?\\s\*)
### Change Log
#### User
Bot Owner
#### Location
Guild And DM
#### Docs
Change where the log output is channeled
#### RegEx
!l\\s+([\\w\\d]+)
### Censor AAVE
#### User
Public
#### Location
Guild
#### Deactivated
‎
#### RegEx
(^|\\s)(chile|asf|purr+|bae|boo|sus|hella|deadass|headass|doe|cap|shook|lit|finna|aggy|fam|ion|ghetto|go off)($|\\s)
### Me&Whom
#### User
Public
#### Location
Guild
#### Deactivated
‎
#### RegEx
(me\\s\*and\\s\*who)[^Mm][.,;:!?\\s]
### Yawn
#### User
Yawn List
#### Location
Guild And DM
#### Docs
Yawn
#### RegEx
.\*
### Reddit
#### User
Public
#### Location
Guild
#### RegEx
(reddit.com/|\\s+|/|^)r/[\\w\\d]+
### Emoji Reaction
#### User
Moderator
#### Location
Guild
#### Docs
Add emoji to referenced message
#### RegEx
Pimp\\s+(.+)
### Questions
#### User
Public
#### Location
Guild And DM
#### Cooldown
10000 Seconds For Guilds
#### Docs
Detect when user asks a question
#### Deactivated
‎
#### RegEx
^((((wh(ich|o+|a+t|ere|en+|y+))|ho+w)('?s|is|are)?([\\s+].\*|[,;:<>&^%$#@!{}\\[\\]=/\\-.\*+()_\\s])?)|(real+y+[.?;:\\s]\*))
### Evaluate
#### User
Public
#### Location
Guild And DM
#### RegEx
Evaluate`\*+(.+)`\*+
### Domain
#### User
Public
#### Location
Guild And DM
#### RegEx
Domain(\\s+((And\\s+)?(Then\\s+)?)?Simplify)?`\*+(.+)`\*+
### Derive
#### User
Public
#### Location
Guild And DM
#### RegEx
Deriv(e|ative)(\\s+((And\\s+)?(Then\\s+)?)?Simplify)?`\*+(.+)`\*+
### Inverse
#### User
Public
#### Location
Guild And DM
#### RegEx
Inverse(\\s+((And\\s+)?(Then\\s+)?)?Simplify)?`\*+(.+)`\*+
### Truth
#### User
Public
#### Location
Guild And DM
#### Deactivated
‎
#### RegEx
Truth(.+)
### Truth Table
#### User
Public
#### Location
Guild And DM
#### RegEx
Truth\\s+Table`\*+(.+)`\*+
### Simplify
#### User
Public
#### Location
Guild And DM
#### RegEx
Simpl(if)?y`\*+(.+)`\*+
### Random
#### User
Public
#### Location
Guild And DM
#### RegEx
Random\\s\*(\\d+)
### Post On MyBB
#### User
User On MyBB
#### Location
Guild And DM
#### RegEx
^Pimp Post\\s+(.+)?
### Repost On MyBB
#### User
User On MyBB
#### Location
Guild And DM
#### RegEx
^Pimp Repost\\s+(.+)
### Upsert Slash
#### User
Bot Owner
#### Location
Guild And DM
#### RegEx
^!us\\s+(.+)$
### Delete Slash
#### User
Bot Owner
#### Location
Guild And DM
#### RegEx
^!ds\\s+(.+)$
### kill
#### User
Public
#### Location
Guild And DM
### trig
#### User
Public
#### Location
Guild And DM
#### Docs
This is just a test slash command
### custom-command
#### User
Moderator
#### Location
Guild And DM
### test
#### User
Public
#### Location
Guild
#### Deactivated
‎
