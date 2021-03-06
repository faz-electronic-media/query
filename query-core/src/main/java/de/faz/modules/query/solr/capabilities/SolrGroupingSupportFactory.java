package de.faz.modules.query.solr.capabilities;

import de.faz.modules.query.GroupingSearchOption;
import de.faz.modules.query.capabilities.SearchOptionFactory;
import de.faz.modules.query.fields.FieldDefinitionGenerator;

import javax.annotation.Nonnull;

/** @author Andreas Kaubisch <a.kaubisch@faz.de> */
public class SolrGroupingSupportFactory implements SearchOptionFactory<GroupingSearchOption> {

	@Override
	public GroupingSearchOption createInstance(@Nonnull final FieldDefinitionGenerator generator) {
		if(generator == null) { throw new IllegalArgumentException("a FieldDefinitionGenerator is required"); }
		return new SolrGroupingSearchOption(generator);
	}
}
