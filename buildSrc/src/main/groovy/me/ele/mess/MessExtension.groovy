package me.ele.mess

public class MessExtension {
  Set<String> ignoreProguardComponents = new HashSet<>()

  void ignoreProguard(String component) {
    ignoreProguardComponents.add(component)
  }
}
