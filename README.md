This is a fork of Java Modbus Library (jamod) - http://jamod.sourceforge.net/. Original SVN revision at the time of fork was 26 according to https://svn.code.sf.net/p/jamod/svn/trunk/.

Commits from openhab1-addons (modbus binding) and openhab2-addons (modbus transport) bundle have been rebased on top of clean jamod SVN checkout. Further details are documented in [openHAB2 Modbus binding PR](https://github.com/openhab/openhab2-addons/pull/2246#issuecomment-341983287).

## Local development in Sympower
Maven blocks HTTP repositories since 3.8.1. To use our internal repository, create the following configuration in `~/.m2/settings.xml`:
```
<settings>
  <mirrors>
    <mirror>
      <id>nexus.symp</id>
      <mirrorOf>*</mirrorOf>
      <url>http://nexus.symp/repository/maven-public/</url>
      <name>nexus.symp</name>
    </mirror>
  </mirrors>
  <servers>
    <server>
      <id>nexus.symp</id>
      <username><!-- username --></username>
      <password><!-- password --></password>
    </server>
  </servers>
</settings>
```


## About

This project represents a Modbus implementation in 100% Java. It can be used to implement Modbus masters and slaves in various flavors:

- Serial: ASCII, RTU (Master only), BIN
- IP: TCP, UDP

The design of this library is fully object oriented, based on abstractions which should support easy understanding, reusability and extensibility.

One important goal of this project is a codebase that is easily usable on a variety of Java Platforms (and devices). Many limited resource devices do not provide Java 5 and Java 6 environments, and there are only limited possibilities for logging.
