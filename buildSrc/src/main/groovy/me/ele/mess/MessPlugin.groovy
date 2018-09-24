package me.ele.mess

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactCollection

import static com.android.build.gradle.internal.publishing.AndroidArtifacts.ArtifactScope.EXTERNAL
import static com.android.build.gradle.internal.publishing.AndroidArtifacts.ArtifactType.CLASSES
import static com.android.build.gradle.internal.publishing.AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH

class MessPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        MessExtension ext = project.extensions.create("mess", MessExtension.class)

        project.afterEvaluate {
            project.plugins.withId('com.android.application') {
                project.android.applicationVariants.all { ApplicationVariant variant ->

                    def artifactMap = [:]
                    ArtifactCollection artifactCollection = variant.variantData.scope.getArtifactCollection(
                            RUNTIME_CLASSPATH, EXTERNAL, CLASSES)
                    artifactCollection.artifacts.each {
                        artifactMap.put(it.id.componentIdentifier.displayName, it.file)
                    }

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

                            def rulesPath = "${project.buildDir.absolutePath}/intermediates/proguard-rules/${variant.dirName}/aapt_rules.txt"
                            File aaptRules = new File(rulesPath)
                            aaptRules.delete()
                            aaptRules.createNewFile()
                            aaptRules << ""
                        }

                        proguardTask.doFirst {
                            println "start ignore proguard components"
                            ext.ignoreProguardComponents.each { String component ->
                                Util.hideProguardTxt((artifactMap.get(component) as File).parentFile.parentFile)
                            }
                        }

                        proguardTask.doLast {
                            println "proguard finish, ready to execute rewrite"
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
                                Util.recoverProguardTxt((artifactMap.get(component) as File).parentFile.parentFile)
                            }
                        }
                    }
                }
            }
        }
    }
}
