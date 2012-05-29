/*
 *    Copyright (c) 2008-2011 Graham Allan
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.mutabilitydetector.cli;

import static com.google.classpath.RegExpResourceFilter.ANY;
import static com.google.classpath.RegExpResourceFilter.ENDS_WITH_CLASS;
import static com.google.common.collect.Lists.newArrayList;
import static org.mutabilitydetector.AnalysisSession.createWithGivenClassPath;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import org.mutabilitydetector.AnalysisClassLoader;
import org.mutabilitydetector.CheckerRunnerFactory;
import org.mutabilitydetector.Configuration;
import org.mutabilitydetector.IAnalysisSession;
import org.mutabilitydetector.MutabilityCheckerFactory;
import org.mutabilitydetector.locations.ClassNameConvertor;

import com.google.classpath.ClassPath;
import com.google.classpath.ClassPathFactory;
import com.google.classpath.RegExpResourceFilter;
import com.google.common.collect.ImmutableList;

/**
 * Runs an analysis configured by the given classpath and options.
 * 
 */
public final class RunMutabilityDetector implements Runnable, Callable<String> {

    private final ClassPath classpath;
    private final BatchAnalysisOptions options;

    public RunMutabilityDetector(ClassPath classpath, BatchAnalysisOptions options) {
        this.classpath = classpath;
        this.options = options;
    }

    /**
     * Runs mutability detection, printing the results to System.out.
     */
    @Override
    public void run() {
        StringBuilder output = getResultString();
        System.out.println(output);
    }

    /**
     * Runs mutability detection, returning the results as a String.
     */
    @Override
    public String call() throws Exception {
        return getResultString().toString();
    }

    private StringBuilder getResultString() {
        AnalysisClassLoader fallbackClassLoader = new URLFallbackClassLoader(getCustomClassLoader());
        RegExpResourceFilter regExpResourceFilter = new RegExpResourceFilter(ANY, ENDS_WITH_CLASS);
        String[] findResources = classpath.findResources("", regExpResourceFilter);

        IAnalysisSession session = createWithGivenClassPath(classpath, 
                                                            new CheckerRunnerFactory(classpath), 
                                                            new MutabilityCheckerFactory(), 
                                                            fallbackClassLoader,
                                                            Configuration.JDK);
        
        List<String> filtered = getNamesOfClassesToAnalyse(options, findResources);
        
        session.runAnalysis(filtered);
        ClassListReaderFactory readerFactory = new ClassListReaderFactory(options.classListFile());
        StringBuilder output = new SessionResultsFormatter(options, readerFactory).format(session);
        return output;
    }

    private URLClassLoader getCustomClassLoader() {
        String[] classPathUrls = options.classpath().split(":");

        List<URL> urlList = new ArrayList<URL>(classPathUrls.length);

        for (String classPathUrl : classPathUrls) {
            try {
                URL toAdd = new File(classPathUrl).toURI().toURL();
                urlList.add(toAdd);
            } catch (MalformedURLException e) {
                System.err.printf("Classpath option %s is invalid.", classPathUrl);
            }
        }
        return new URLClassLoader(urlList.toArray(new URL[urlList.size()]));
    }

    private static List<String> getNamesOfClassesToAnalyse(BatchAnalysisOptions options, String[] findResources) {
        List<String> filtered = newArrayList();
        List<String> classNames = newArrayList();
        classNames.addAll(Arrays.asList(findResources));
        String matcher = options.match();
        for (String className : classNames) {
            String dottedClassName = new ClassNameConvertor().dotted(className);
            if (Pattern.matches(matcher, dottedClassName)) {
                filtered.add(className);
            }
        }
        return ImmutableList.<String>copyOf(filtered);
    }

    public static void main(String[] args) {
        BatchAnalysisOptions options = createOptionsFromArgs(args);
        ClassPath classpath = new ClassPathFactory().createFromPath(options.classpath());

        new RunMutabilityDetector(classpath, options).run();
    }

    private static BatchAnalysisOptions createOptionsFromArgs(String[] args) {
        try {
            BatchAnalysisOptions options = new CommandLineOptions(System.err, args);
            return options;
        } catch (Throwable e) {
            System.out.println("Exiting...");
            System.exit(1);
            return null; // impossible statement
        }
    }

}
