# RuneLite Bot
An automation plugin for RuneLite. Designed solely for 
use in private servers due to some design choices detailed below.

A few scripts are included under [src/main/kotlin/net/tacobuddies/bot/scripts](src/main/kotlin/net/tacobuddies/bot/scripts)
to test a few of the APIs. These were mainly developed for OldSchool RSPS and 
are focused around their AFK area or their method of teleportation.

## How it works
A java agent is attached to the JVM at startup and patches
`net.runelite.client.externalplugins.ExternalPluginManager` to remove
the developer mode requirement for side-loading plugins and adds itself
to the queue for loading. Because you'll often be in and out of developer
mode writing scripts, this also removes the assertion test inside `net.runelite.client.RuneLite.main()`
(see #2 below)  
See: [AssertionTransformer.java](src/main/java/net/tacobuddies/bot/AssertionTransformer.java)

Some private servers opt to use the RuneLite launcher, but this java agent
also supports patching the `JvmLauncher`. It will pass along the `-javaagent:`
JVM argument as well as any optional properties needed to enhance the automation
like auto-login, smart mouse, or redirecting the cache folder. It will also increase
the max heap size to 2G just in case. `ReflectionLauncher` and `ForkLauncher` will still
need to be patched.  
See: [LauncherTransformer.java](src/main/java/net/tacobuddies/bot/LauncherTransformer.java)

## Reasons to not use in Live RuneScape
1) The mouse teleports directly to its target. "Natural" behavior is 
available by adding `-Dnet.tacobuddies.smartMouse=true` to your JVM 
arguments, however I do not care enough to tune it to be human-like.  
2) Widgets will often be out of sync with what is on screen. Widgets
require access solely from the client thread in order to keep data in 
sync with what is being rendered. To get around this, a java agent was 
used to remove the assertion checks in RuneLite, so you can happily run 
in developer mode without complaint.  
3) An async API will need to be written to request Widget data from the client
thread and pass it back and forth.
4) While there is some support for randomization and other human-like behavior,
it has been mostly designed to target the center of a hitbox with no variation.
5) A formal script API has not been completed yet. Banking, Store interaction, and the
Grand Exchange have not yet been implemented or tested.
6) There is no break handling of any kind. The script will run forever, until
you tell it to stop.

## Features
### Auto Login
An accounts.json file can be placed in the working directory with the following
schema (both bankPin and secret can be excluded, but 2FA or bank pins will not be solved
automatically):
```json
[
  {
      "username": "",
      "password": "",
      "bankPin": "",
      "secret": ""
  },
  {
    "username": "",
    "password": "",
    "bankPin": "",
    "secret": ""
  }
]
```

A default account can be set with a JVM parameter `-Dnet.tacobuddies.login=<username>`
and the client will automatically login on first start up and any time it disconnects.
Scripting access was planned for a future date but is not currently implemented.

Secret refers to the HMAC-SHA1 key that some private servers provide for 2FA. The client
will automatically generate the correct time-based key and enter it for you as well.  
See: [TOTP.kt](src/main/kotlin/net/tacobuddies/bot/utils/TOTP.kt) and
[Account.kt](src/main/kotlin/net/tacobuddies/bot/account/Account.kt)

### "Smart" Mouse
Natural mouse movement can be enabled by setting the JVM parameter `-Dnet.tacobuddies.smartMouse=true`.
This enables [JoonasVali/NaturalMouseMotion](https://github.com/JoonasVali/NaturalMouseMotion) in
a decently normal movement pattern.

### Cache Redirection
Most private servers store their cache under `$RUNELITE_DIR/jagexcache/oldschool/LIVE`.
However, some have decided to move this. This can be overridden with the `-Dnet.tacobuddies.cache=<path>`
JVM parameter. This will still be relative to `$RUNELITE_DIR`.