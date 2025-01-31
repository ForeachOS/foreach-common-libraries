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

package com.foreach.across.modules.filemanager.extensions;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.core.annotations.ModuleConfiguration;
import com.foreach.common.filemanager.business.FileManagerDomain;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.hibernate.provider.HibernatePackageConfigurer;
import com.foreach.across.modules.hibernate.provider.HibernatePackageRegistry;

@ConditionalOnAcrossModule(allOf = AcrossHibernateJpaModule.NAME)
@ModuleConfiguration(AcrossHibernateJpaModule.NAME)
public class EntityScanConfiguration implements HibernatePackageConfigurer
{
	@Override
	public void configureHibernatePackage( HibernatePackageRegistry hibernatePackageRegistry ) {
		hibernatePackageRegistry.addPackageToScan( FileManagerDomain.class );
	}
}