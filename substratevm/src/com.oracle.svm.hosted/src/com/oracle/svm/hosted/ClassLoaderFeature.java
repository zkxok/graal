/*
 * Copyright (c) 2018, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.svm.hosted;

import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.ImageSingletons;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.jdk.JavaLangSubstitutions.ClassLoaderSupport;
import com.oracle.svm.util.ModuleSupport;

@AutomaticFeature
public class ClassLoaderFeature implements Feature {
    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        ImageSingletons.add(ClassLoaderSupport.class, new ClassLoaderSupport());
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        ClassLoaderSupport.getInstance().createClassLoaders(ClassLoader.getSystemClassLoader());
        ClassLoaderSupport.getInstance().systemClassLoader = ClassLoaderSupport.getInstance().classLoaders.get(ClassLoader.getSystemClassLoader());
        ClassLoaderSupport.getInstance().platformClassLoader = ClassLoaderSupport.getInstance().classLoaders.get(ModuleSupport.getPlatformClassLoader());
    }

    @Override
    public void duringSetup(DuringSetupAccess access) {
        access.registerObjectReplacer(object -> {
            if (object instanceof ClassLoader) {
                ClassLoaderSupport.getInstance().createClassLoaders((ClassLoader) object);
                return ClassLoaderSupport.getInstance().classLoaders.get(object);
            }
            return object;
        });
    }
}
