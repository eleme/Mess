package me.ele.mess

import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.tasks.ProcessAndroidResources
import groovy.io.FileType
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import proguard.obfuscate.MappingProcessor
import proguard.obfuscate.MappingReader

import java.lang.reflect.Field
import java.nio.charset.StandardCharsets

class RewriteComponentTask extends DefaultTask {

    static final String CHARSET = StandardCharsets.UTF_8.name()

    @Input
    ApkVariant apkVariant

    @Input
    BaseVariantOutput variantOutput

    @TaskAction
    void rewrite() {
        println "start rewrite task"

        Map<String, String> map = new LinkedHashMap<>();
        MappingReader reader = new MappingReader(apkVariant.mappingFile)
        reader.pump(new MappingProcessor() {
            @Override
            boolean processClassMapping(String className, String newClassName) {
                map.put(className, newClassName)
                return false
            }

            @Override
            void processFieldMapping(String className, String fieldType, String fieldName, String newClassName, String newFieldName) {

            }

            @Override
            void processMethodMapping(String className, int firstLineNumber, int lastLineNumber, String methodReturnType, String methodName, String methodArguments, String newClassName, int newFirstLineNumber, int newLastLineNumber, String newMethodName) {

            }
        })

        // AndroidManifest.xml
        String realPath = "${project.buildDir.absolutePath}/intermediates/manifests/full/${getSubResPath()}/AndroidManifest.xml"
        def f = new File(realPath)
        def parser = new XmlParser(false, false).parse(f)
        parser.application.each {
            if (map.containsKey(it.@"android:name")) {
                it.@"android:name" = map.get(it.@"android:name")
            }
            it.children().each {
                if (map.containsKey(it.@"android:name")) {
                    it.@"android:name" = map.get(it.@"android:name")
                }
            }
        }
        new XmlNodePrinter(new PrintWriter(new FileWriter(f))).print(parser)

        long t0 = System.currentTimeMillis()
        File resDir = new File(getResPath())
        resDir.eachFile { File dir ->
            if (dir.isFile() && dir.name.endsWith(".flat")) {
                throw new Exception("please disable AAPT2 by setting `android.enableAapt2=false` in your gradle.properties file.")
            }
            if (dir.exists() && dir.isDirectory() && isLayoutsDir(dir.name)) {
                dir.eachFileRecurse(FileType.FILES) { File file ->
                    String orgTxt = file.getText(CHARSET)
                    String newTxt = orgTxt
                    map.each { k, v ->
                        newTxt = newTxt.replace(k, v)
                    }
                    if (newTxt != orgTxt) {
                        println 'rewrite file: ' + file.absolutePath
                        file.setText(newTxt, CHARSET)
                    }
                }
            }
        }

        println 'write layout and menu xml spend: ' + ((System.currentTimeMillis() - t0) / 1000) + ' s'


        ProcessAndroidResources processAndroidResourcesTask = variantOutput.processResources
        try {
            //this is for Android gradle 2.3.3 & above
            Field outcomeField = processAndroidResourcesTask.state.getClass().getDeclaredField("outcome")
            outcomeField.setAccessible(true)
            outcomeField.set(processAndroidResourcesTask.state, null)
        } catch (Throwable e) {
            processAndroidResourcesTask.state.executed = false
        }
        processAndroidResourcesTask.execute()

        println "execute process resources again"

        println "end rewrite task"
    }

    String getResPath() {
        String resPath = "${project.buildDir.absolutePath}/intermediates/res/merged/${getSubResPath()}"
        if (project.android.dataBinding.enabled) {

            project.rootProject.buildscript.configurations.classpath.resolvedConfiguration.firstLevelModuleDependencies.each {
                if (it.moduleGroup == "com.android.tools.build" && it.moduleName == "gradle") {
                    if (it.moduleVersion.startsWith("1") || it.moduleVersion.startsWith("2")) {
                        resPath = "${project.buildDir.absolutePath}/intermediates/data-binding-layout-out/${getSubResPath()}"
                        return
                    }
                }
            }

        }
        return resPath
    }

    boolean isLayoutsDir(String name) {
        // layout and menu xml
        // sometimes, we can use a string res for value, e.g app:behavior="@string/my_behavior"
        // <string name="my_behavior">me.ele.mess.MyBehavior</string>
        if (name.startsWith("layout") || name.startsWith("menu") || name.startsWith("values")) {
            return true
        }
        return false
    }

    String getSubResPath() {
        return apkVariant.getDirName();
    }
}
