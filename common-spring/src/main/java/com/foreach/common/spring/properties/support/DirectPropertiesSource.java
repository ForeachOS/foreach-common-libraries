/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreach.common.spring.properties.support;

import com.foreach.common.spring.properties.PropertiesSource;

import java.util.Map;

/**
 * Simple PropertieSource that proxies access to an underlying Map implementation.
 *
 * @param <T> Type of the map keys.
 */
public class DirectPropertiesSource<T> implements PropertiesSource<T>
{
    private final Map<T, ?> map;

    public DirectPropertiesSource( Map<T, ?> map ) {
        this.map = map;
    }

    public Map<T, ?> getProperties() {
        return map;
    }
}
