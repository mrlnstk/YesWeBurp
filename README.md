# YesWeBurp

## Description

YesWeBurp is an extension for BurpSuite allowing you to access all your https://yeswehack.com/ bug bounty programs directly inside Burp.

YesWeBurp also help you to instantly configure Burp according to the program rules.

![](https://i.imgur.com/uzBycc6.png)
![](https://i.imgur.com/0ZELF17.png)

## Installation

### From release
- Download https://github.com/yeswehack/yesweburp/releases/latest
- Open Burp on Extender / Extensions
- Click `Add`
- Set Extension type as Java
- Set Extension file to `YesWeBurp.jar`
- Click `Next`
- The addon is now installed, a new tab named `YesWeBurp` should appear

### From source

Requires JDK 17 (or newer) and Maven.

- `git clone 'https://github.com/yeswehack/YesWeBurp.git' <git_folder>`
- `cd <git_folder> && mvn package`
- Open Burp on Extensions / Installed
- Click `Add`
- Set Extension type as Java
- Set Extension file to `<git_folder>/target/YesWeBurp.jar`
- Click `Next`
- The addon is now installed, a new tab named `YesWeBurp` should appear

`mvn package` produces a single self-contained jar: the Montoya API is `provided` by Burp at
runtime, and every other dependency is shaded in.

### From BApp Store

YesWeBurp is also available on the BApp store, the BApp store version might be outdated.

## Configuration

The configuration options are available in the tab YesWeHack / Options


| option | description | default |
|--------|-------------|---------|
| Authentication | Choose between Anonymous or authenticated connection.<br>Authenticated mode allows you to access all you private programs. | Anonymous |
| Email | Email used for connecting to your YesWeHack account. | - |
| Password | Password used for connecting to your YesWeHack account. | - |
| OTP | OTP token used for connecting to your YesWeHack account (ex: 000000). | - |
| Remember password | Choose to keep a plaintext copy of your password inside Burp settings | - |


## Changelog
- v3.0.0
    - Migrated from the legacy Burp Extender API to the Burp Montoya API
      (requires Burp 2023.1 or newer and JDK 17+)
    - Settings are now stored via Montoya's preferences API
    - Network calls run off the Swing event dispatch thread
    - Fixed `Anonymous` authentication never (re)setting the session
    - Maven now actually builds the Kotlin sources and produces a shaded `target/YesWeBurp.jar`
- v2.0.2 - Change in yeswehack api
- v2.0.1 - Fixed html rendering
- v2.0.0 
    - Total rewrite in Kotlin
    - Allow preview of scopes rules
    - Programs are now cached for the session
    - Better Faster Stronger
- v1.0.2 - Basic support for TOTP
- v1.0.1 - Bugfix
- v1.0.0 - Initial release
