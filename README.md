<h1 align="center">
  SbControl
</h1>

SbControl is packet-based scoreboard API for Bukkit plugins, inspired on [ProtocolSidebar](https://github.com/CatCoderr/ProtocolSidebar)
that supports 1.12 to 1.20.4. The purpose of this API is unleashed all the power of scoreboards, so you can use it however you want.
Either by using the scoreboard packets or with an already built-in scoreboard system. It also includes a simple and fast Sidebar
system to display whatever you want to the player.

## Features

- Create and send scoreboard packets
- Supports all minecraft versions from 1.12 to 1.20.4
- Very easy to use
- Can be used asynchronously
- Built-in scoreboard system
- Built-in sidebar system
- Automatically converts color codes to chat format
- Supports hex colors on 1.16 and higher
- Everything is at the packet level, so it works with other plugins using scoreboard and/or teams
- Minimized NMS interaction, means that packets are constructed at the byte buffer level and then sent directly to the player's channel.

## Index

- [Usage](#usage)
  - [Scoreboard](#built-in-scoreboard-system)
  - [Sidebar](#built-in-sidebar-system)
  - [Packets](#working-with-packets)
- [Installation](#installation)
  - [Maven](#maven)
  - [Gradle](#gradle)
- [Libraries Used](#libraries-used)
- [License](#license)

## Usage

### Built-in Scoreboard System

Creating a packet-based scoreboard.

```java
// with no parameters
Board board = new Board();

// specifying players
Board board = new Board(player1, player2);

// using an array
Board board = new Board(Player[]);

// using a collection
Board board = new Board(Collection<Player>);
```

Creating a packet-based objective.

```java
Objective obj = board.createObjective("sidebar");
obj.setDisplayName("&9&lTitle");
obj.setRenderType(RenderType.INTEGER);
obj.setPosition(Position.SIDEBAR);

Score score = obj.getScore(player.getName());
score.setScore(10);
```

Creating a packet-based team.

```java
Team team = board.createTeam("red");
team.setPrefix("&aTeam Red");
team.setColor(ChatColor.RED);
team.addEntity(player.getName());
```

### Built-in Sidebar System

Creating a packet-based sidebar.

```java
Sidebar sidebar = new Sidebar(player);
sidebar.setTitle("&#232aef&lTitle");
sidebar.setLine(1, "&4spaghetti");
sidebar.setLine(0, "&aHello there");
```

### Working with packets.
We first need and instance of the versioned PacketManager class.

```java
PacketManager packetManager = SbControl.getPacketManager();
```

Sending a PacketTeam.

```java
PacketTeam packetTeam = packetManager.createPacketTeam();
packetTeam.setTeamName("Red");
packetTeam.setMode(PacketTeam.Mode.CREATE);
packetTeam.setTeamDisplayName("&aTeam Red");
packetTeam.setNameTagVisibility(NameTagVisbility.HIDE_FOR_OTHER_TEAMS);
packetTeam.setTeamColor(ChatColor.RED);
packetTeam.setTeamPrefix("&aTeam Red");
packetTeam.setEntities(Collections.singleton(player.getName));

packetManager.sendPacket(player, packetTeam);
```

## Installation

### Maven
Add the repository and dependency to your pom.xml

```maven
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

```maven
<dependencies>
    <dependency>
        <groupId>me.eliab</groupId>
        <artifactId>sbcontrol</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

### Gradle
Add the repository and dependency to your build.gradle

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```

```gradle
dependencies {
    implementation 'me.eliab:sbcontrol:1.0.0'
}
```

## Libraries Used

- [nbt](https://github.com/BitBuf/nbt)

## License

[MIT](LICENSE)

## Donations

[Paypal](https://www.paypal.me/eliabcuadros1)