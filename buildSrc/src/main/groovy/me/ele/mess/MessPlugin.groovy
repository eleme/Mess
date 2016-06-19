package me.ele.mess

import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.BaseVariantOutput
import org.gradle.api.Plugin
import org.gradle.api.Project

class MessPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.afterEvaluate {
            project.plugins.withId('com.android.application') {
                project.android.applicationVariants.all { ApkVariant variant ->
                    variant.outputs.each { BaseVariantOutput output ->

                        String taskName = "transformClassesAndResourcesWithProguardFor${variant.name.capitalize()}"
                        def proguardTask = project.tasks.findByName(taskName)
                        if (!proguardTask) {
                            return
                        }


                        boolean hasProcessResourcesExecuted = false
                        output.processResources.doLast {
                            if (hasProcessResourcesExecuted) {
                                return
                            }
                            hasProcessResourcesExecuted = true

                            def aaptRules = "build/intermediates/proguard-rules/${variant.dirName}/aapt_rules.txt"
                            project.file(aaptRules).text = ""
                        }

                        proguardTask.doLast {
                            RewriteComponentTask rewriteTask = project.tasks.create(name: "rewriteComponentFor${variant.name.capitalize()}",
                                    type: RewriteComponentTask) {
                                apkVariant = variant
                                variantOutput = output
                            }
                            rewriteTask.execute()
                        }
                    }
                }
            }
        }
    }
}
