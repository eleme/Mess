package me.ele.mess

import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.tasks.ProcessAndroidResources
import groovy.xml.QName
import groovy.xml.XmlUtil
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

        replaceManifest(map, variantOutput.processManifest.manifestOutputFile)

        getResDir().listFiles().each { layoutFile -> replaceLayout(map, layoutFile) }

        getMenuDir().listFiles().each { menuFile -> replaceLayout(map, menuFile) }


        ProcessAndroidResources processTask = variantOutput.processResources
        processTask.state.executed = false
        processTask.execute()
    }

    void replaceLayout(Map<String, String> map, File layoutFile) {
        Node oldNode = (new XmlParser()).parse(layoutFile)
        Node newNode = null
        map.each { oldStr, newStr ->
            if (oldNode.name().equals(oldStr)) {
                newNode = new Node(oldNode.parent(), newStr, oldNode.attributes(), oldNode.value);
            }
            newNode?.attributes().each { k, v ->
                if (v.equals(oldStr)) {
                    newNode.attributes().put(k, newStr);
                }
            }
        }

        if (!newNode) {
            newNode = oldNode
        }

        map.each { oldStr, newStr ->
            getAllNode(newNode).each { Node child ->

                child.attributes().each { k, v ->
                    if (v.equals(oldStr)) {
                        child.attributes().put(k, newStr);
                    }
                }

                if (child.name().equals(oldStr)) {
                    Node newChild = new Node(child.parent(), newStr, child.attributes(), child.value);
                    newChild.attributes().each { k, v ->
                        if (v.equals(oldStr)) {
                            newChild.attributes().put(k, newStr);
                        }
                    }

                    child.parent().remove(child)
                }
            }
        }

        if (newNode) {
            layoutFile.text = XmlUtil.serialize(newNode)
        }
    }

    List<Node> getAllNode(Node node) {
        List<Node> list = new ArrayList<>()
        node?.children()?.each { Node child ->
            if (!child.children().empty) {
                list.addAll(getAllNode(child))
            }
            list.add(child)
        }
        return list
    }

    void replaceManifest(Map<String, String> map, File manifestFile) {
        Node node = (new XmlParser()).parse(manifestFile)

        Node application = null

        node.getAt(new QName('application')).each { Node child ->
            application = child
            child.attributes().each { k, v ->
                map.each { oldStr, newStr ->
                    if (v.equals(oldStr)) {
                        child.attributes().put(k, newStr);
                    }
                }
            }
        }


        String[] array = ['activity', 'receiver', 'service', 'provider', 'activity-alias']
        array.each { tag ->
            application.getAt(new QName(tag)).each { Node child ->
                child.attributes().each { k, v ->
                    map.each { oldStr, newStr ->
                        if (v.equals(oldStr)) {
                            child.attributes().put(k, newStr)
                        }
                    }
                }
            }
        }

        manifestFile.text = XmlUtil.serialize(node)
    }

    File getResDir() {
        if (project.android.dataBinding.enabled) {
            return project.file("build/intermediates/data-binding-layout-out/${apkVariant.dirName}/layout")
        }
        return project.file("build/intermediates/res/merged/${apkVariant.dirName}/layout")
    }

    File getMenuDir() {
        if (project.android.dataBinding.enabled) {
            return project.file("build/intermediates/data-binding-layout-out/${apkVariant.dirName}/menu")
        }
        return project.file("build/intermediates/res/merged/${apkVariant.dirName}/menu")
    }
}
