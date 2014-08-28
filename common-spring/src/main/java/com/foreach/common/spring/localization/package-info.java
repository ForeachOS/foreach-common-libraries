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

