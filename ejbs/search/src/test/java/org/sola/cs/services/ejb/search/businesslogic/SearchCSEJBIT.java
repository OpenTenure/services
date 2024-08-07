/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice,this list
 *       of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice,this list
 *       of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
 *       promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.sola.cs.services.ejb.search.businesslogic;

import org.sola.cs.services.ejb.search.repository.entities.BrSearchParams;
import org.sola.cs.services.ejb.search.repository.entities.BrSearchResult;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTReader;
import java.util.*;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sola.services.common.test.AbstractEJBTest;

/**
 *
 * @author manoku
 */
public class SearchCSEJBIT extends AbstractEJBTest {
    
    private static final String LANG = "en";
    private static final String LOGIN_USER = "test";
    private static final String LOGIN_PASS = "test";
    
    private static final String FOUND = "Found ";
    private static final String TESTING_QUERY = "Testing query: ";
    
    @Before
    public void setUp() throws Exception {
        login(LOGIN_USER, LOGIN_PASS);
    }

    @After
    public void tearDown() throws Exception {
        logout();
    }
    
    public SearchCSEJBIT() {
        super();
    }

    
    /** Test searching active users */
    @Test
    public void testBrSearch() throws Exception {
        if (skipIntegrationTest()) {
            return;
        }
        try {
            BrSearchParams params = new BrSearchParams();
            SearchCSEJBLocal instance = (SearchCSEJBLocal) getEJBInstance(SearchCSEJB.class.getSimpleName());
            List<BrSearchResult> result = instance.searchBr(params, LANG);

            assertNotNull(result);

            if (result != null && result.size() > 0) {
                System.out.println(FOUND + result.size() + " business rules");
            } else {
                System.out.println("Can't find any business rules.");
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
  
    private byte[] getGeometry(String wktGeometry) throws Exception {
        WKTReader wktReader = new WKTReader();
        Geometry geom = wktReader.read(wktGeometry);
        WKBWriter wkbWriter = new WKBWriter();
        return wkbWriter.write(geom);
    }

    @Test
    @Ignore
    public void testSearchOthers() throws Exception {
        if (skipIntegrationTest()) {
            return;
        }
        System.out.println("Testing other queries that return lists of entities");
        SearchCSEJBLocal instance = (SearchCSEJBLocal) getEJBInstance(SearchCSEJB.class.getSimpleName());
        this.testQueriesForResultList(instance, "CadastreObjectWithGeometry.searchByBaUnitId",
                new Object[]{"3068323"});
    }

    private void testQueriesForResultList(
            SearchCSEJBLocal instance, String queryName, Object[] params) throws Exception {
        System.out.println(TESTING_QUERY + queryName);
//        List result =
//                instance.getResultList(queryName, params);
//        if (result != null && result.size() > 0) {
//            System.out.println(FOUND + result.size() + " elements.");
//        } else {
//            System.out.println("Can't find any element.");
//        }
    }
}
