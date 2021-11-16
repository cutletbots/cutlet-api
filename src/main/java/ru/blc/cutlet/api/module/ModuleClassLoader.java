package ru.blc.cutlet.api.module;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import ru.blc.cutlet.api.Cutlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ModuleClassLoader extends URLClassLoader {
    static {
        ClassLoader.registerAsParallelCapable();
    }

    protected static final Set<ModuleClassLoader> loaders = new HashSet<>();

    private final Cutlet cutlet;
    private final JarFile jar;
    private final ModuleDescription moduleDescription;
    private final Manifest manifest;
    private final URL url;

    private Module module;

    public ModuleClassLoader(Cutlet cutlet, File file, ModuleDescription moduleDescription) throws IOException {
        super(new URL[]{file.toURI().toURL()});
        this.cutlet = cutlet;
        this.jar = new JarFile(file);
        this.manifest = this.jar.getManifest();
        this.url = file.toURI().toURL();
        this.moduleDescription = moduleDescription;
        loaders.add(this);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass0(name, true);
    }

    protected Class<?> loadClass0(String name, boolean checkOther) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {
            if (checkOther) {
                for (ModuleClassLoader loader : loaders) {
                    if (loader == this) continue;
                    try {
                        return (loader.loadClass0(name, false));
                    } catch (ClassNotFoundException ignore) {
                    }
                }
            }
            ;
        }
        throw new ClassNotFoundException(name);
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/').concat(".class");
        JarEntry entry = this.jar.getJarEntry(path);
        if (entry != null) {
            byte[] classBytes;
            try {
                InputStream is = this.jar.getInputStream(entry);
                Throwable var6 = null;

                try {
                    classBytes = ByteStreams.toByteArray(is);
                } catch (Throwable var17) {
                    var6 = var17;
                    throw var17;
                } finally {
                    if (is != null) {
                        if (var6 != null) {
                            try {
                                is.close();
                            } catch (Throwable var16) {
                                var6.addSuppressed(var16);
                            }
                        } else {
                            is.close();
                        }
                    }

                }
            } catch (IOException var19) {
                throw new ClassNotFoundException(name, var19);
            }

            int dot = name.lastIndexOf(46);
            if (dot != -1) {
                String pkgName = name.substring(0, dot);
                if (this.getPackage(pkgName) == null) {
                    try {
                        if (this.manifest != null) {
                            this.definePackage(pkgName, this.manifest, this.url);
                        } else {
                            this.definePackage(pkgName, (String) null, (String) null, (String) null, (String) null, (String) null, (String) null, (URL) null);
                        }
                    } catch (IllegalArgumentException var20) {
                        if (this.getPackage(pkgName) == null) {
                            throw new IllegalStateException("Cannot find package " + pkgName);
                        }
                    }
                }
            }

            CodeSigner[] signers = entry.getCodeSigners();
            CodeSource source = new CodeSource(this.url, signers);
            return this.defineClass(name, classBytes, 0, classBytes.length, source);
        } else {
            return super.findClass(name);
        }
    }

    void init(Module module) {
        Preconditions.checkArgument(module != null, "module");
        Preconditions.checkArgument(module.getClass().getClassLoader() == this, "Module has incorrect ClassLoader");
        if (this.module != null) {
            throw new IllegalArgumentException("Module already initialized!");
        } else {
            this.module = module;
            module.init(this.cutlet, this.moduleDescription);
        }
    }

    public String toString() {
        return "ModuleClassloader(desc=" + this.moduleDescription + ")";
    }
}
