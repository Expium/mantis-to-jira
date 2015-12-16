# Mantis to JIRA Converter

Copyright (c) 2012-2015 Expium LLC  http://expium.com/
All Rights Reserved.

	Expium LLC
	1610 Des Peres Rd. #150
	St. Louis, MO 63141
	(636) 281-8657
	info@expium.com

This software converts issues from Mantis to JIRA. We used it to convert a large
project in late 2012; it had the best features of anything we could find as of
that time, as the options were then quite lacking.

Specifically, this did (at that time) an acceptable job bringing issue history,
attachments, and other related data; the Mantis import "in the box" did not.

We updated the software in 2015, but did not compare it
to the 2015 "in the box" Mantis import capabilities in JIRA.

## Requirements:

* MantisBT 1.2.9+, though the exact version should not matter much.
* JIRA 5.1+
* JRE 1.6+

## Build

```
mvn clean package
```

This yields ```majira.jar``` in target directory. If you obtained the software is already-compiled form,
```majira.jar``` will already be present.

## Configuration

Copy the file ```config.properties.sample``` to ```config.properties```, and edit it.

The software has username mapping; to use, this, make a file ```users.properties```
(based on ```users.properties.sample```) and pass the parameter -users as below.


## Usage:

```
java -jar target/majira.jar [-conf file] [-users file] -mode (csv/ref)
```

```-conf <arg>```   Configuration file (config.properties is default)

```-users <arg>``` Users list

```-mode <arg> ```  Application mode, either csv or ref.

The software exports and imports one Mantis project to one JIRA project;
if you have multiple projects (in one instance), repeat the whole process for each.

Use a throwaway JIRA instance, not a production JIRA instance,
until you are comfortable and confident with the results!

To use the software:

### 1) Export CSV data from Mantis

```java -jar target/majira.jar -mode csv```

This accesses Mantis over its SOAP API and saves the issue data in a CSV file in the current directory,
along with task/subtask data.
Add the -users option for username mapping.

### 2) Import CSV data to JIRA

Import the CSV data using the JIRA CSV import feature.
This format supports many kinds of data for JIRA import, using an arcane format.

You will need a CSV import configuration file; create one using the provided ```csv-importer-config``` file example.

### 3) Import Task References to JIRA

The JIRA CSV import supports subtask reference, but not "duplicate" and "relates to" references. To import these, run the software:

```java -jar target/majira.jar -mode ref```

In this mode, the software reads the reference file it produces earlier,
and connects to JIRA using the JIRA API to save these references.
This is the *only* case where this software uses the JIRA API - all of the other data arrives via CSV import.


## Known Issues

1. You must disable inline PHP errors, otherwise the PHP SOAP API fails.
2. Because Mantis requires (name/password) login, JIRA will not be able, by default, to copy attachments from it. We have provided the file ```file_export.php``` - copy it to your Mantis instance, and uncomment mantis.downloadUrl property in configuration file, to make it possible for file attachments to be imported.
3. Make sure to remove ```file_export.php``` afterward, as it is an open security hole!

