package de.faz.modules.query.solr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.util.Iterator;

import net.sf.cglib.proxy.Callback;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.google.common.base.Optional;

import de.faz.modules.query.Query;
import de.faz.modules.query.SearchContext;
import de.faz.modules.query.SearchDecorator;
import de.faz.modules.query.TestMapping;
import de.faz.modules.query.fields.FieldDefinitionGenerator;
import de.faz.modules.query.fields.Mapping;

/** @author Andreas Kaubisch <a.kaubisch@faz.de> */
@RunWith(MockitoJUnitRunner.class)
public class SolrQueryExecutorTest {


    @Mock(answer = Answers.RETURNS_DEEP_STUBS) QueryResponse solrResponse;
	@Mock(answer = Answers.RETURNS_DEEP_STUBS) SolrSearchSettings settings;

	@Mock Query q;
    @Mock FieldDefinitionGenerator generator;
    @Mock HttpSolrServer httpSolrServer;
    private SolrQueryExecutor executor;

    @Before
    public void setUp() throws SolrServerException {
	    when(settings.getOffset()).thenReturn(Optional.<Integer>absent());
        executor = new SolrQueryExecutor(httpSolrServer, generator);
    }

    @Test
    public void executeQuery_withoutSearchClient_returnsDefaultResult() {
        executor = new SolrQueryExecutor(null, generator);
        when(settings.getPageSize()).thenReturn(10);

        SearchContext.SearchResult result = executor.executeQuery(q, settings);
        Iterator it = result.getResultsForMapping(Mapping.class);
        assertFalse(it.hasNext());
    }

    @Test(expected = NullPointerException.class)
    public void executeQuery_withoutQuery_throwsIllegalArgumentException() {
        executor.executeQuery(null, settings);
    }

    @Test(expected = NullPointerException.class)
    public void executeQuery_withoutSettings_throwsIllegalArgumentException() {
        executor.executeQuery(q, null);
    }

    @Test
    public void executeQuery_withEmptyQuery_returnsDefaultResult() throws SolrServerException, IOException {
        when(q.isEmpty()).thenReturn(true);
        executor.executeQuery(q, settings);
        verify(httpSolrServer, times(0)).query(any(SolrQuery.class));
    }

    @Test
    public void executeQuery_withQueryAndSettings_callSearchClient() throws SolrServerException, IOException {
        executor.executeQuery(q, settings);
        verify(httpSolrServer).query(any(SolrQuery.class));
    }

    @Test
    public void executeQuery_withQueryAndSettings_verifySolrQueryHasQueryString() throws SolrServerException, IOException {
        executor.executeQuery(q, settings);
        ArgumentCaptor<SolrQuery> queryArg = ArgumentCaptor.forClass(SolrQuery.class);
        verify(httpSolrServer).query(queryArg.capture());
        Assert.assertEquals(queryArg.getValue().getQuery(), q.toString());
    }

    @Test
    public void executeQuery_withSettings_verifyEnrichisCalled() throws SolrServerException, IOException {
        executor.executeQuery(q, settings);
        ArgumentCaptor<SolrQuery> queryArg = ArgumentCaptor.forClass(SolrQuery.class);
        verify(httpSolrServer).query(queryArg.capture());
        verify(settings.getQueryExecutor()).enrich(queryArg.getValue());
    }

    @Test
    public void executeQuery_withResult_createIteratorWithEnhancement() throws SolrServerException, IOException {
        when(httpSolrServer.query(any(SolrQuery.class))).thenReturn(solrResponse);
        SearchContext.SearchResult searchResult = executor.executeQuery(q, settings);
        SolrDocument doc = mock(SolrDocument.class);
        when(solrResponse.getResults().iterator().hasNext()).thenReturn(true);
        when(solrResponse.getResults().iterator().next()).thenReturn(doc);


        searchResult.getResultsForMapping(TestMapping.class).next();
        verify(generator).enhanceWithInterceptor(eq(TestMapping.class), any(Callback.class));
    }

    @Test
    public void executeQuery_withResult_verifyFactoryIsGetFromSettings() {
        executor.executeQuery(q, settings);
        verify(settings).getCustomCallbackFactory();
    }

	private SearchDecorator createSearchDecoratorMock() {
		return mock(SearchDecorator.class, withSettings().defaultAnswer(new Answer() {
				@Override
				public Object answer(final InvocationOnMock invocation) throws Throwable {
					return invocation.getArguments()[0];
				}
			}));
	}


}
