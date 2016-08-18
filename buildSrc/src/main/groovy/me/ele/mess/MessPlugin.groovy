package me.ele.mess

import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.BaseVariantOutput
import org.gradle.api.Plugin
import org.gradle.api.Project

class MessPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        MessExtension ext = project.extensions.create("mess", MessExtension.class)

        project.afterEvaluate {
            project.plugins.withId('com.android.application') {
                project.android.applicationVariants.all { ApkVariant variant ->

                    variant.outputs.each { BaseVariantOutput output ->

                        String taskName = "transformClassesAndResourcesWithProguardFor${variant.name.capitalize()}"
                        def proguardTask = project.tasks.findByName(taskName)
                        if (!proguardTask) {
                            return
                        }

                        Map<String, List<String>> components

                        boolean hasProcessResourcesExecuted = false
                        output.processResources.doLast {
                            if (hasProcessResourcesExecuted) {
                                return
                            }
                            hasProcessResourcesExecuted = true

                            def rulesPath = "${project.buildDir.absolutePath}/intermediates/proguard-rules/${variant.dirName}/aapt_rules.txt"
                            File aaptRules = new File(rulesPath)
                            aaptRules.delete()
                            aaptRules << ""
                        }

                        proguardTask.doFirst {
                            ext.ignoreProguardComponents.each { String component ->
                                Util.hideProguardTxt(project, component)
                            }
                        }

                        proguardTask.doLast {
                            RewriteComponentTask rewriteTask = project.tasks.create(name: "rewriteComponentFor${variant.name.capitalize()}",
                                    type: RewriteComponentTask
                            ) {
                                apkVariant = variant
                                variantOutput = output
                            }
                            rewriteTask.execute()
                        }

                        proguardTask.doLast {
                            ext.ignoreProguardComponents.each { String component ->
                                Util.recoverProguardTxt(project, component)
                            }
                        }
                    }
                }
            }
        }
    }
}
