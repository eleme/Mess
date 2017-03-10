# Mess


**Mess** is a gradle plugin for obfuscating all code including activity, service, receiver, provider and custom view. It's really very cool to obfuscate all codes, lowers code readability after reverse engineering and ensures code's safety.

**Mess** is super easy to integrate with your app, its implementation is also clear to understand. During android gradle assemble task execution, It has a lot of tasks to do, and can be divided to resource process & code process briefly.

__process**Resources__ is the last task in resource process, it will generate a merged **AndroidManifest.xml**, **aapt_rules.txt** and a merged **res** directory in project build dir. **aapt_rules.txt** is the output file after aapt, it contains all classes in xml files, and then this file is delivered to proguard task as a list of keeps. __transformClassesAndResourcesWithProguardFor**__ is the proguard task which obfuscates code, it generates a **mapping.txt** file which contains all mapping relation between origin and obfuscated classes.

**Mess** hooks two android gradle tasks: __process**Resources__ & __package**__, hook __process**Resources__ task is to clear **aapt_rules.txt**, tells proguard not to obfuscate all the classes in xml. __package**__ task is the last task before package code & resource into apk, it runs after proguard, Mess hooks it just to read the obfuscation mapping relation, then rewrite these into resources again, finally execute __process**Resources__ task again. If your app sets **shrinkResources** to be true, then execute shrink task one more time.


## Usage

``` groovy

dependencies {
   ...
   classpath 'me.ele:mess-plugin:1.0.1'
 }
  
apply plugin: 'com.android.library'
apply plugin: 'me.ele.mess'

```

In some cases, you want to ignore some proguard configuration provided by aar. E.g. latest Butter Knife is an aar which contains **proguard.txt**, so users do not need to configure its proguard manually.

However, we would like to still obfuscate classes used with Butter Knife. So we provide an extension for such scenario.

```groovy
mess {
    ignoreProguard 'com.jakewharton:butterknife'
}
```

As a result, the Butter Knife's proguard configuration will be ignored. And those activities, views, fragments will be obfuscated by Mess.

That's all, just simple as that.

## Note

As almost every Android project uses [Butter Knife](https://jakewharton.github.io/butterknife/) for view injection. And Butter Knife has its own proguard rules which keeps every class using Butter Knife. As as result, almost every android activity, fragment, custom view would be kept. And out Mess plugin is useless.

But good news is that we studied Butter Knife source code and figured it out. And the solution is also a gradle plugin [ButterMess](https://github.com/peacepassion/ButterMess) which has been a submodule of this project.

## Feel free to use, welcome issue and comment


