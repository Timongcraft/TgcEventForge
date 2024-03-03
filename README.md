# TgcEventForge
TgcEventForge is an asynchronous event manager usable in any application.

## Getting started
You can find the latest version [here](https://repo.skyblocksquad.de/#/repo/de/timongcraft/TgcEventForge).

### Maven

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.5.1</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <relocations>
                    <relocation>
                        <pattern>de.timongcraft.eventforge</pattern>
                        <!-- Replace 'com.yourpackage' with the package of your project ! -->
                        <shadedPattern>com.yourpackage.eventforge</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
        </plugin>
    </plugins>
</build>

<repositories>
    <repository>
        <id>skyblocksquad-repo</id>
        <url>https://repo.skyblocksquad.de/repo</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>de.timongcraft</groupId>
        <artifactId>TgcEventForge</artifactId>
        <version>LATEST_VERSION</version>
    </dependency>
</dependencies>
```

When using Maven, make sure to build directly with Maven and not with your IDE configuration (on IntelliJ IDEA: in the `Maven` tab on the right, in `Lifecycle`, use `package`).

### Gradle

```groovy
plugins {
    id 'com.github.johnrengelman.shadow' version '8.1.1'
}

repositories {
    maven {
        url "https://repo.skyblocksquad.de/repo"
    }
}

dependencies {
    implementation 'de.timongcraft:TgcEventForge:LATEST_VERSION'
}

shadowJar {
    // Replace 'com.yourpackage' with the package of your project 
    relocate 'de.timongcraft.eventforge', 'com.yourpackage.eventforge'
}
```