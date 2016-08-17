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

    @Input
    Map<String, List<String>> allComponents

    @TaskAction
    void rewrite() {
        Map<String, String> map = new HashMap<>();
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

        map.each { k, v ->
            if (allComponents.containsKey(k)) {
                List<String> layouts = allComponents.get(k)
                layouts.each { layout ->
                    if (!layout.startsWith('res')) {
                        String realPath = variantOutput.processManifest.manifestOutputFile
                        writeLine(realPath, k, v)
                    }
                }
            }
        }

        File layoutDir = new File(getLayoutPath())
        File menuDir = new File(getMenuPath())
        [layoutDir, menuDir].each {File dir ->
            if (dir.exists()) {
                map.each { k, v ->
                    dir.eachFileRecurse(FileType.FILES) { File file ->
                        boolean hasWritten = false
                        file.eachLine { String line ->
                            if (line.contains(k) && !hasWritten) {
                                hasWritten = true
                                writeLine(file.absolutePath, k, v)
                            }
                        }
                    }
                }
            }
        }

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

    String getResPath(String layout) {
        if (project.android.dataBinding.enabled) {
            return "${project.buildDir.absolutePath}/intermediates/data-binding-layout-out/${getSubResPath()}/${layout.substring("res/".length())}"
        }
        return "${project.buildDir.absolutePath}/intermediates/res/merged/${getSubResPath()}/${layout.substring("res/".length())}"
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
