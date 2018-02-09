package com.gerasin.oleg.semanticsearch;

import com.gerasin.oleg.semanticsearch.model.Publication;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author geras
 */
@Named
@ViewScoped
public class SearchBean
        implements Serializable
{
    private static org.apache.logging.log4j.Logger log = LogManager.getLogger(SearchBean.class.getName());

    @Inject
    private SparqlHelper sparqlHelper;
    @Inject
    private DbHelper dbHelper;

    private String keyword;
    private Boolean cachedSearch = true;
    private String[] selectedSources;

    private List<Publication> publications;
    private Publication selected;

    public Publication getSelected() {
        return selected;
    }
     public void setSelected(Publication selected) {
        this.selected = selected;
    }

    @PostConstruct
    protected void init()
    {
         publications = new ArrayList<>();

         Map<String, String> parameterMap =
                 FacesContext.getCurrentInstance()
                         .getExternalContext()
                         .getRequestParameterMap();

         setKeyword(parameterMap.get("keyword"));
         setCachedSearch(Boolean.parseBoolean(parameterMap.get("cachedSearch")));
         String sources = parameterMap.get("selectedSources");
         if (sources != null && !sources.isEmpty())
            setSelectedSources(parameterMap.get("selectedSources").split(","));
    }
    public String getKeyword()
    {
        log.info("getKeyword with value: {}", keyword);
        return keyword;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
        log.info("setKeyword with value: {}", keyword);
    }

    public Boolean getCachedSearch()
    {
        return cachedSearch;
    }

    public void setCachedSearch(Boolean cachedSearch)
    {
        this.cachedSearch = cachedSearch;
    }

    public String[] getSelectedSources()
    {
        return selectedSources;
    }

    public void setSelectedSources(String[] selectedSources)
    {
        log.info("selectedSources = {}", Arrays.toString(selectedSources));
        this.selectedSources = selectedSources;
    }

    public List<Publication> getPublications()
    {
        return publications;
    }

    private String output = "";

    public String getOutput()
    {
        log.info("getOutput");
        if (keyword == null || keyword.isEmpty())
        {
            output = "Your query is empty";
            return output;
        }
        else
        {
            searchPublications();
            output ="There are " + publications.size() + " publications for your query:";
        }
        return output;
    }

    private void searchPublications()
    {
        List<String> selectedSourcesList = Arrays.asList(selectedSources);
        List<Publication> cachedPublications = null;
        if (cachedSearch)
        {
            cachedPublications = dbHelper.getCachedPublications(keyword);
        }
        if (cachedPublications == null)
        {
            log.info("cachedPublications == null ");
            publications = sparqlHelper.execSelect(keyword, selectedSourcesList);
            dbHelper.createLog(keyword, publications);
        }
        else
        {
            log.info("size of cachedPublications == {}", cachedPublications.size());
            publications = cachedPublications.stream()
                    .filter(p -> selectedSourcesList.contains(p.getSource()))
                    .collect(Collectors.toList());
        }
    }

}
