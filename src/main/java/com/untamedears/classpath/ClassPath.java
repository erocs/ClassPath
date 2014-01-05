// Derived from:
//   https://forums.bukkit.org/threads/tutorial-use-external-library-s-with-your-plugin.103781/
package com.untamedears.classpath;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.jar.JarFile;

import org.bukkit.plugin.java.JavaPlugin;

public class ClassPath extends JavaPlugin {
  public class JarFilter implements FilenameFilter {
    public JarFilter() {}
    @Override
    public boolean accept(File dir, String name) {
      return name.toLowerCase().endsWith(".jar");
    }
  }

  public ClassPath() {}

  public void onLoad() {
    loadJars();
  }

  private void loadJars() {
    final URLClassLoader sysLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
    final Method method = getAddUrl();
    File dataFolder = this.getDataFolder();
    if (!dataFolder.exists()) {
      return;
    }
    for (File jarFile : dataFolder.listFiles(new JarFilter())) {
      try {
        JarFile testJar = new JarFile(jarFile);
        testJar.entries();
      } catch (final Throwable ex) {
        ex.printStackTrace();
        throw new Error("The file " + jarFile + " is not a Jar");
      }
      try {
        final URL url = getJarUrl(jarFile);
        method.invoke(sysLoader, new Object[] { url });
      } catch (final Throwable ex) {
        ex.printStackTrace();
        throw new Error("Error adding " + jarFile + " to system classloader");
      }
    }
  }

  public Method getAddUrl() {
    try {
      final Class<URLClassLoader> sysClass = URLClassLoader.class;
      final Method method = sysClass.getDeclaredMethod("addURL", new Class[] { URL.class });
      method.setAccessible(true);
      return method;
    } catch (final Throwable ex) {
      ex.printStackTrace();
      throw new Error("Error retriving addURL from system classloader");
    }
  }

  public static URL getJarUrl(final File file) throws IOException {
    return new URL("jar:" + file.toURI().toURL().toExternalForm() + "!/");
  }
}
