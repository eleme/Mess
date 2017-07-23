package me.ele.mess

import com.android.builder.model.MavenCoordinates
import com.google.common.io.Files
import groovy.io.FileType
import org.gradle.api.Project

public class Util {

  public static MavenCoordinates parseMavenString(String component) {
    String[] arrays = component.split(":")
    return new MavenCoordinates() {
      @Override
      String getGroupId() {
        return arrays[0]
      }

      @Override
      String getArtifactId() {
        return arrays[1]
      }

      @Override
      String getVersion() {
        return arrays[2]
      }

      @Override
      String getPackaging() {
        return null
      }

      @Override
      String getClassifier() {
        return null
      }

      String getVersionlessId() {
        return null
      }
    }
  }

  public static void hideProguardTxt(Project project, String component) {
    renameProguardTxt(project, component, 'proguard.txt', 'proguard.txt~')
  }

  public static void recoverProguardTxt(Project project, String component) {
    renameProguardTxt(project, component, 'proguard.txt~', 'proguard.txt')
  }

  private static void renameProguardTxt(Project project, String component, String orgName,
      String newName) {
    MavenCoordinates mavenCoordinates = parseMavenString(component)
    File bundlesDir = new File(project.buildDir, "intermediates/exploded-aar")
    File bundleDir = new File(bundlesDir,
        "${mavenCoordinates.groupId}/${mavenCoordinates.artifactId}")
    if (!bundleDir.exists()) return
    bundleDir.eachFileRecurse(FileType.FILES) { File f ->
      if (f.name == orgName) {
        File targetFile = new File(f.parentFile.absolutePath, newName)
        println "rename file ${f.absolutePath} to ${targetFile.absolutePath}"
        Files.move(f, targetFile)
      }
    }
  }

  public static Map<String, String> sortMapping(Map<String, String> map) {
    List<Map.Entry<String, String>> list = new LinkedList<>(map.entrySet());
    Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
      public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
        return o2.key.length() - o1.key.length()
      }
    });

    Map<String, String> result = new LinkedHashMap<>();
    for (Iterator<Map.Entry<String, String>> it = list.iterator(); it.hasNext();) {
      Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
      result.put(entry.getKey(), entry.getValue());
    }

    return result;
  }
}
