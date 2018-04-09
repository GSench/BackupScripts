# BackupScripts
Scripts to automate backup

Project to automate backup process. It includes programs in Java, WindowsCMD and Unix Shell scripts.

## Backup
  - **backup.bat** - for Windows CMD
  - **backup.sh** for Unix Shell - requires rsync to be installed

### Usage:
  1) Drop backup script to the folder you want to backup
  2) Run script

### Description:
These scripts contais path to the folder (destination folder) to be synchronized with the current one (initial folder).
Script will move modyfied files and folders from the initial folder to the destination one (with priority on initial folder). It will remove extra files and folders in the destination folder.

## Included Backup
  - **IncludeForBackup.java** - Java source code
  - **IncludeForBackup.class** - Compilled Java program (requires Java to be installed)
  - **backupInclude.bat** - CMD example

### Usage:
  1) Drop **IncludeForBackup.class** and **backupInclude.bat** to the folder you want to backup
  2) Edit the destination path and included folders in the **backupInclude.bat**.
  3) Run **backupInclude.bat**

### Description:
The format of **IncludeForBackup.class**:
```bat
java IncludeForBackup [destination full path] [names of folders & files to be copied]
```
Example:
```bat
java IncludeForBackup E:\ folder1 folder2 file.txt folder3
```
**backupInclude.bat** contais path to the folder (destination folder) to be synchronized with the current one (initial folder) and names of folders & files in the initial folder, which will fall into the destination folder.

Script will move modyfied files and folders from the initial folder to the destination one (with priority on initial folder). It will remove extra files and folders in the destination folder.

Note that **IncludeForBackup.class** creates an auxiliary .bat file (cmdexec.bat for Windows or shellexec.sh for *nix) near itself.

Note that include works only with the 1st nesting level: with folders & files from list of files of the initial folder.
If you include some subfolder of initial folder, all files and subfolder of it will be included.
Include for C:\path_to\initial_folder\folder1 if you include folder1 will proceed.
Include for C:\path_to\initial_folder\folder2\folder3 if you include folder3 won't proceed.
Include for C:\path_to\initial_folder\folder2\folder3 if you include folder2 will proceed; it also copies everything from folder2

## Backup Git Repositories
  - **BackupGitRepos.java** - Java source code
  - **BackupGitRepos.class**, **BackupGitRepos$Pair.class** - Compilled Java program (requires Java and Git to be installed)
  - **backupGitRepos.bat** - CMD example

### Usage:
  1) Drop **BackupGitRepos.class**, **BackupGitRepos$Pair.class** and **backupGitRepos.bat** to the folder in which you want to find urls to the Git Repositories
  2) Run **backupGitRepos.bat**

### Description:
The format of **BackupGitRepos.class**:
```bat
java BackupGitRepos
```
**BackupGitRepos.class**
  1) finds all files of ".url" on the current path (path from which **backupGitRepos.bat** started)
  2) extracts urls of the github.com and bitbucket.org from these files like:
```url
[{00000000-0000-0000-*000-000000000000}]
Prop3=*,*
[InternetShortcut]
IDList=
URL=https://github.com/Author/Repository
```
  3) clones this repositories near .url file
  4) creates zip file near .url file and zip folders of cloned repositories
  5) Deletes folders of cloned repositories

As the result for all .url files of git repositories, these repositories will be compressed to zip files and placed next to .url files.
