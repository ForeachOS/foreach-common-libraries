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

package com.foreach.across.modules.filemanager.it;

import com.foreach.across.core.AcrossConfigurationException;
import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.properties.PropertiesModule;
import com.foreach.across.test.AcrossTestContext;
import org.junit.Test;

import static com.foreach.across.test.support.AcrossTestBuilders.web;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
public class ITFileManageModuleBootstrap
{
	@Test
	public void fileManagerModule() {
		try (AcrossTestContext ctx = web().modules( FileManagerModule.NAME )
		                                  .build()) {
			assertThat( ctx.contextInfo().isBootstrapped() ).isTrue();
		}
	}

	@Test
	public void fileManagerModuleAndPropertiesModule() {
		try (AcrossTestContext ctx = web().modules( FileManagerModule.NAME, PropertiesModule.NAME )
		                                  .build()) {
			assertThat( ctx.contextInfo().isBootstrapped() ).isTrue();
		}
	}

	@Test
	public void fileManagerModuleAndHibernateModule() {
		try (AcrossTestContext ctx = web().modules( FileManagerModule.NAME, AcrossHibernateJpaModule.NAME )
		                                  .build()) {
			ctx.contextInfo().isBootstrapped();
		}
		catch ( AcrossConfigurationException e ) {
			assertThat( e.getMessage() ).isEqualTo( FileManagerModule.NAME + " requires " + PropertiesModule.NAME
					                                        + " to be present when " + AcrossHibernateJpaModule.NAME + " is configured." );
		}
	}

	@Test
	public void fileManagerModuleAndHibernateModuleAndPropertiesModule() {
		try (AcrossTestContext ctx = web().modules( FileManagerModule.NAME, AcrossHibernateJpaModule.NAME, PropertiesModule.NAME )
		                                  .build()) {
			assertThat( ctx.contextInfo().isBootstrapped() ).isTrue();
		}
	}
}
