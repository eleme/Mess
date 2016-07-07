# Mess


**mess** is a gradle plugin for obfuscating all code including activity, service, receiver, provider and custom view. It's really very cool to obfuscate all codes, lowers code readability after reverse engineering and ensures code's safety. 

**mess** is super easy to integrate with your app, its implementation is also clear to understand. During android gradle assemble task execution, It has a lot of tasks to do,  and can be divided to resource process & code process briefly.  

 **process\**Resources** is the last task in resource process, it will generate a merged **AndroidManifest.xml**,  **aapt_rules.txt**  and  a merged **res** directory in project build dir.  aapt_rules.txt is the output file after aapt, it contains all classes in xml files, and then this file is delivered to proguard task as a list of keeps. 
 **transformClassesAndResourcesWithProguardFor\**** is the proguard task which obfuscates code,  it generates a **mapping.txt** file which contains all mapping relation between origin and obfuscated classes. 
 
 **mess** hooks two android gradle tasks: **process\**Resources** & **package\****, hook processResources task is to clear aapt_rules.txt,  tells proguard not to obfuscate all the classes in xml;  package task is the last task before package code & resource into apk, it runs after proguard, mess hooks it just to read the obfuscation mapping relation, then rewrite these into resources again, finally execute processResources task again. If your app sets **shrinkResources** to be true, then execute shrink task one more time. 
 


## Usage

```groovy
dependencies {
   ...
   classpath 'me.ele:mess-plugin:1.0.2'
 }
  
apply plugin: 'com.android.library'
apply plugin: 'me.ele.mess'

```

that's all, just simple as that

## feel free to use, welcome issue and comment


