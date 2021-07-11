## Changelog

### 2.9.0

- Migrate to JDA 4.0 and correct breaking functionality

### 2.2.0
- *Search* functionality leveraging trie data structure
- *Subcategories* that would act identical to *categories* but also function as children
- Update to newest version of [JDA](https://github.com/DV8FromTheWorld/JDA)
- Fixed entrance logic so bot does not follow people into AFK channels.
- Added record of uploader to sound file bean. Will not affect existing records.
- Implemented a looping play command.
- Removed watch service in lieu of manual file list updating.
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
