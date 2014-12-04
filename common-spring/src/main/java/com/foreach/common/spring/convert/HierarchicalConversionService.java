package com.foreach.common.spring.convert;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;

/**
 * Extension of the {@link org.springframework.core.convert.support.GenericConversionService} that represents
 * an initially empty ConversionService that delegates to a parent ConversionService if no local converters
 * are registered.
 *
 * @author Arne Vandamme
 */
public class HierarchicalConversionService extends GenericConversionService
{
	private ConversionService parent;

	public HierarchicalConversionService() {
	}

	public HierarchicalConversionService( ConversionService parent ) {
		this.parent = parent;
	}

	public void setParent( ConversionService parent ) {
		this.parent = parent;
	}

	public ConversionService getParent() {
		return parent;
	}

	@Override
	public boolean canConvert( TypeDescriptor sourceType, TypeDescriptor targetType ) {
		boolean possible = super.canConvert( sourceType, targetType );

		if ( !possible && parent != null ) {
			possible = parent.canConvert( sourceType, targetType );
		}

		return possible;
	}

	@Override
	public Object convert( Object source, TypeDescriptor sourceType, TypeDescriptor targetType ) {
		if ( parent == null || super.canConvert( sourceType, targetType ) ) {
			return super.convert( source, sourceType, targetType );
		}

		return parent.convert( source, sourceType, targetType );
	}

	public static HierarchicalConversionService defaultConversionService() {
		return defaultConversionService( null );
	}

	public static HierarchicalConversionService defaultConversionService( ConversionService parent ) {
		HierarchicalConversionService conversionService = new HierarchicalConversionService( parent );
		DefaultConversionService.addDefaultConverters( conversionService );

		return conversionService;
	}
}
