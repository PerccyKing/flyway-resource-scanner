/*
 * Copyright 2010-2020 Redgate Software Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.com.pism.frc.resourcescanner;

import cn.com.pism.frc.resourcescanner.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

/**
 * Scanner for Resources and Classes.
 */
@Slf4j
public class Scanner<I> implements ResourceProvider, ClassProvider<I> {

    private final List<LoadableResource> resources = new ArrayList<>();
    private final List<Class<? extends I>> classes = new ArrayList<>();

    // Lookup maps to speed up getResource
    private final HashMap<String, LoadableResource> relativeResourceMap = new HashMap<>();
    private HashMap<String, LoadableResource> absoluteResourceMap = null;

    /*
     * Constructor. Scans the given locations for resources, and classes implementing the specified interface.
     */
    public Scanner(Class<I> implementedInterface, Collection<Location> locations, ClassLoader classLoader, Charset encoding


            , ResourceNameCache resourceNameCache
            , LocationScannerCache locationScannerCache
    ) {


        for (Location location : locations) {
            ResourceAndClassScanner<I> resourceAndClassScanner = new ClassPathScanner<>(implementedInterface, classLoader, encoding, location, resourceNameCache, locationScannerCache);
            resources.addAll(resourceAndClassScanner.scanForResources());
            classes.addAll(resourceAndClassScanner.scanForClasses());
        }

        for (LoadableResource resource : resources) {
            relativeResourceMap.put(resource.getRelativePath().toLowerCase(), resource);
        }
    }

    @Override
    public LoadableResource getResource(String name) {
        LoadableResource loadedResource = relativeResourceMap.get(name.toLowerCase());

        if (loadedResource != null) {
            return loadedResource;
        }

        // Only build the HashMap and resolve the absolute paths if an
        // absolute path is requested as this is really slow
        // Should only ever be required for sqlplus @
        if (Paths.get(name).isAbsolute()) {
            if (absoluteResourceMap == null) {
                absoluteResourceMap = new HashMap<>();
                for (LoadableResource resource : resources) {
                    absoluteResourceMap.put(resource.getAbsolutePathOnDisk().toLowerCase(), resource);
                }
            }

            loadedResource = absoluteResourceMap.get(name.toLowerCase());

            if (loadedResource != null) {
                return loadedResource;
            }
        }

        return null;
    }

    /**
     * Returns all known resources starting with the specified prefix and ending with any of the specified suffixes.
     *
     * @param prefix   The prefix of the resource names to match.
     * @param suffixes The suffixes of the resource names to match.
     * @return The resources that were found.
     */
    @Override
    public Collection<LoadableResource> getResources(String prefix, String... suffixes) {
        List<LoadableResource> result = new ArrayList<>();
        for (LoadableResource resource : resources) {
            String fileName = resource.getFilename();
            if (StringUtils.startsAndEndsWith(fileName, prefix, suffixes)) {
                result.add(resource);
            } else {
                log.debug("Filtering out resource: " + resource.getAbsolutePath() + " (filename: " + fileName + ")");
            }
        }
        return result;
    }

    /**
     * Scans the classpath for concrete classes under the specified package implementing the specified interface.
     * Non-instantiable abstract classes are filtered out.
     *
     * @return The non-abstract classes that were found.
     */
    @Override
    public Collection<Class<? extends I>> getClasses() {
        return Collections.unmodifiableCollection(classes);
    }


    public static void main(String[] args) {
        Scanner<JavaMigration> scanner = new Scanner<>(
                JavaMigration.class,
                Arrays.asList(new Location("classpath:sqls")),
                Thread.currentThread().getContextClassLoader(),
                StandardCharsets.UTF_8


                , new ResourceNameCache()
                , new LocationScannerCache()
        );
        for (LoadableResource resource : scanner.resources) {
            System.out.println(JSON.toJSONString(resource));
        }

    }
}