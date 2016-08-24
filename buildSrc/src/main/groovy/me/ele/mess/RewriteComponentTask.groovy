package me.ele.mess

import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.tasks.ProcessAndroidResources
import groovy.io.FileType
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class RewriteComponentTask extends DefaultTask {

    @Input
    ApkVariant apkVariant

    @Input
    BaseVariantOutput variantOutput

    @TaskAction
    void rewrite() {
        Map<String, String> map = new LinkedHashMap<>();
        File mappingFile = apkVariant.mappingFile
        mappingFile.eachLine { line ->
            if (!line.startsWith(" ")) {
                String[] keyValue = line.split("->")
                String key = keyValue[0].trim()
                String value = keyValue[1].subSequence(0, keyValue[1].length() - 1).trim()
                if (!key.equals(value)) {
                    map.put(key, value)
                }
            }
        }

        // AndroidManifest.xml
        map.each { k, v ->
            String realPath = variantOutput.processManifest.manifestOutputFile
            writeLine(realPath, k, v)
        }

        // layout and menu xml
        long t0 = System.currentTimeMillis()
        File layoutDir = new File(getLayoutPath())
        File menuDir = new File(getMenuPath())
        // sometimes, we can use a string res for value, e.g app:behavior="@string/my_behavior"
        // <string name="my_behavior">me.ele.mess.MyBehavior</string>
        File valueDir = new File(getValuePath())
        [layoutDir, menuDir, valueDir].each {File dir ->
            if (dir.exists()) {
                dir.eachFileRecurse(FileType.FILES) { File file ->
                    String orgTxt = file.text
                    String newTxt = orgTxt
                    map.each { k, v ->
                        newTxt = newTxt.replace(k, v)
                    }
                    if (newTxt != orgTxt) {
                        println 'rewrite file: ' + file.absolutePath
                        file.text = newTxt
                    }
                }
            }
        }
        println 'write layout and menu xml spend: ' + ((System.currentTimeMillis() - t0)/ 1000) + ' s'

        ProcessAndroidResources processTask = variantOutput.processResources
        processTask.state.executed = false
        processTask.execute()

        def shrinkResourcesTask = project.tasks.findByName("transformClassesWithShrinkResFor${apkVariant.name.capitalize()}")
        if (shrinkResourcesTask) {
            shrinkResourcesTask.state.executed = false
            shrinkResourcesTask.execute()
        }
    }

    void writeLine(String path, String oldStr, String newStr) {
        File f = new File(path)

        StringBuilder builder = new StringBuilder()
        f.eachLine { line ->
            if (line.contains("\$") && oldStr.contains("\$")) {
                oldStr = oldStr.replaceAll("\\\$", "inner")
                line = line.replaceAll("\\\$", "inner").replaceAll(oldStr, newStr)
            } else {
                line = line.replaceAll(oldStr, newStr)
            }
            builder.append(line);
            builder.append("\n")
        }

        f.delete()
        f << builder.toString()
    }

    String getLayoutPath() {
        if (project.android.dataBinding.enabled) {
            return "${project.buildDir.absolutePath}/intermediates/data-binding-layout-out/${getSubResPath()}/layout"
        }
        return "${project.buildDir.absolutePath}/intermediates/res/merged/${getSubResPath()}/layout"
    }

    String getMenuPath() {
        if (project.android.dataBinding.enabled) {
            "${project.buildDir.absolutePath}/intermediates/data-binding-layout-out/${getSubResPath()}/menu"
        }
        return "${project.buildDir.absolutePath}/intermediates/res/merged/${getSubResPath()}/menu"
    }

    String getValuePath() {
        if (project.android.dataBinding.enabled) {
            return "${project.buildDir.absolutePath}/intermediates/data-binding-layout-out/${getSubResPath()}/values"
        }
        return "${project.buildDir.absolutePath}/intermediates/res/merged/${getSubResPath()}/values"
    }

    String getSubResPath() {
        String subPath
        if (apkVariant.flavorName == null) {
            subPath = apkVariant.buildType.name
        } else {
            subPath = "${apkVariant.flavorName}/${apkVariant.buildType.name}"
        }
        return subPath
    }
}
