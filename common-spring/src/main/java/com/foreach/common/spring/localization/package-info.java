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

/**
 * <p>
 *     Base classes and interfaces to facilitate objects with localized fields.
 *     To use these classes you would usually:
 *     <ul>
 *         <li>Implement your own specific {@link Language}.  Often an enum.</li>
 *         <li>Create a localized entity extending {@link AbstractLocalizedFieldsObject} that uses
 *         an extension of {@link BaseLocalizedFields} for its fields implementation.</li>
 *     </ul>
 * </p>
 * <p>Examples of use can be found in the RestoBookings and Resto projects.</p>
 */
package com.foreach.common.spring.localization;

