package org.ff4j.console.client;

/*
 * #%L
 * ff4j-console
 * %%
 * Copyright (C) 2013 - 2014 Ff4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.ObjectMapper;
import org.ff4j.console.domain.HomeBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mapping from ff4j root url api to {@link HomeBean}.
 *
 * @author <a href="mailto:cedrick.lunven@gmail.com">Cedrick LUNVEN</a>
 */
@Provider
public class HomeBeanMsgBodyReader implements MessageBodyReader<HomeBean> {

    /** logger for the class. */
    protected Logger log = LoggerFactory.getLogger(getClass());

    /** {@inheritDoc} */
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return (type == HomeBean.class);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public HomeBean readFrom(Class<HomeBean> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> myMap = new HashMap<String, Object>();
        myMap = objectMapper.readValue(entityStream, HashMap.class);

        HomeBean hb = new HomeBean();
        
        // uptime
        hb.setUptime((String) myMap.get("uptime"));
        
        // Version
        hb.setVersion((String) myMap.get("version"));
        
        // Monitoring
        if (myMap.containsKey("eventRepository")) {
            Map<String, Object> evtMap = (Map<String, Object>) myMap.get("eventRepository");
            if (evtMap != null) {
                String classType = (String) evtMap.get("type");
                hb.setMonitoring(classType.substring(classType.lastIndexOf(".") + 1));
                if (evtMap.containsKey("hitCount")) {
                    hb.setNbEvents((Integer) evtMap.get("hitCount"));
                }
                // Pie
                if (evtMap.containsKey("eventsPie")) {
                    Map<String, Object> pieMap = (Map<String, Object>) evtMap.get("eventsPie");
                    StringBuilder sbData = new StringBuilder("[");
                    StringBuilder sbColor = new StringBuilder("[");
                    List < LinkedHashMap< String , Object > > listOfSectory = (ArrayList<LinkedHashMap< String , Object >>) pieMap.get("sectors");
                    boolean first = true;
                    for (LinkedHashMap< String , Object > sector : listOfSectory) {
                        if (!first) {
                            sbData.append(",");
                            sbColor.append(",");
                        }
                        sbData.append("['" + (String) sector.get("label") + "', " + String.valueOf(sector.get("value")) + "]");
                        sbColor.append("\"#" + (String) sector.get("color") + "\"");
                        first = false;
                    }
                    sbData.append("]");
                    hb.setTodayHitCountData(sbData.toString());
                    sbColor.append("]");
                    hb.setTodayHitCountColors(sbColor.toString());
                }
            }
        } else {
            hb.setMonitoring("n/a");
        }

        // Security
        if (myMap.containsKey("authorizationsManager")) {
            Map<String, Object> secMap = (Map<String, Object>) myMap.get("authorizationsManager");
            if (secMap != null) {
                String classType = (String) secMap.get("type");
                hb.setSecurity(classType.substring(classType.lastIndexOf(".") + 1));
            } else {
                hb.setSecurity("---");
            }
        } else {
            hb.setSecurity("---");
        }
        
        // Store (+ caching)
        if (myMap.containsKey("featuresStore")) {
            Map<String, Object> featMap = (Map<String, Object>) myMap.get("featuresStore");
            if (featMap != null) {
                String classType = (String) featMap.get("type");
                hb.setStore(classType.substring(classType.lastIndexOf(".") + 1));
                if (featMap.containsKey("numberOfFeatures")) {
                    hb.setNbFeature((Integer) featMap.get("numberOfFeatures"));
                }
                if (featMap.containsKey("numberOfGroups")) {
                    hb.setNbGroup((Integer) featMap.get("numberOfGroups"));
                }
                
                if (featMap.containsKey("cache") && featMap.get("cache") != null) {
                    Map<String, Object> cacheMap = (Map<String, Object>) featMap.get("cache");
                    String classStore= (String) cacheMap.get("cacheStore");
                    hb.setStore(classStore.substring(classStore.lastIndexOf(".") + 1) + " (+ CacheProxy)");
                    String cacheProvider = (String) cacheMap.get("cacheProvider");
                    hb.setCaching(cacheProvider);
                    hb.setCache(true);
                    if (cacheMap.containsKey("featureNames")) {
                        hb.setCacheFeature((List<String>) cacheMap.get("featureNames"));
                    }
                } else {
                    hb.setCaching("---");
                }
            }
        }
        
        log.debug("ff4j mapped to HomeBean");
        return hb;
    }

}
