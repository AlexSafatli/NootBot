# NootBot

A soundboard Discord bot written in Java able to be deployed to multiple servers and over multiple Bot users.

Much of the foundation code was taken from an early commit of [Darkside138/DiscordSoundboard](https://github.com/Darkside138/DiscordSoundboard) but has since diverged.

## Changelog

### 2.1.5

- Added more string literals and reflected this in code.
- Minor fix for sound uploading when category fuzzy matches.

### 2.1.0

- Improve sound attachment processing by making it clear to the user how it works. Ensure lowercase names.
- Implementation of a dynamic string service for string lookup from text files.
- Make "leave" and sound count commands available only to the owner.

### 2.0.0

- Added permission system for uploading sounds and authenticated commands. Currently based on roles (Administrator, Server Management permissions).
- Entrances no longer add to play count.
- Removed Reddit integration due to lack of popularity.
