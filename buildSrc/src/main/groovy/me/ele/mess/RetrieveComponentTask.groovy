package me.ele.mess

import com.android.build.gradle.api.ApkVariant
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class RetrieveComponentTask extends DefaultTask {

    @Input
    ApkVariant apkVariant

    Map<String, List<String>> components = new HashMap<>()

    @TaskAction
    void retrieve() {
        def rulesPath = "${project.buildDir.absolutePath}/intermediates/proguard-rules/${getSubPath()}/aapt_rules.txt"
        File aaptRules = new File(rulesPath)

        String component
        List<String> layouts = new ArrayList<>()

        aaptRules.eachLine { String line ->
            if (line.startsWith("# view ")) {
//              # view res/layout/abc_screen_toolbar.xml #generated:27
//              # view AndroidManifest.xml #generated:18
                String file = line.split(" ")[2]
                if (!layouts.contains(file)) {
                    layouts.add(file)
                }
            } else if (line.startsWith("-keep class")) {
//              -keep class android.support.v7.view.menu.ListMenuItemView { <init>(...); }
                component = line.split(" ")[2];
                components.put(component, layouts)
                layouts = new ArrayList<>()
            }
        }
        aaptRules.delete()
        aaptRules << ""
    }


    Map<String, List<String>> getComponents() {
        return components
    }

    String getSubPath() {
        String subPath
        if (apkVariant.flavorName == null) {
            subPath = apkVariant.buildType.name
        } else {
            subPath = "${apkVariant.flavorName}/${apkVariant.buildType.name}"
        }
        return subPath
    }

}
