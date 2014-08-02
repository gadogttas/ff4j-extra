package org.ff4j.console.controller;

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

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ff4j.console.ApplicationConstants;
import org.ff4j.console.conf.xml.Connection;
import org.ff4j.console.domain.EnvironmenBean;
import org.ff4j.console.domain.HomeBean;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * HomeController which load supervision services and display home page.
 */
@Controller
@RequestMapping("/" + ApplicationConstants.VIEW_HOME)
public class HomeController extends AbstractConsoleController {

    /**
     * Allows to display screen.
     * 
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView showPage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("Page <HOME>");

        // check First Access, still : no environment selected
        if (null == request.getSession().getAttribute(ATTR_ENVBEAN)) {
            // Single Environment means default choice;
            ArrayList<Connection> conns = new ArrayList<Connection>(conf.getMapOfConnections().values());
            if (1 == conf.getMapOfConnections().size()) {
                log.info("Selecting <" + conns.get(0).getId() + "> as default and single environement");
                request.getSession().setAttribute(ATTR_ENVBEAN, new EnvironmenBean(conns.get(0), conns));
            } else {
                log.info("No environnment selected yet >> asking to user");
                ModelAndView mav = new ModelAndView(VIEW_HOME);
                mav.addObject(ATTR_ENVBEAN, new EnvironmenBean(conns));
                return mav;
            }
        }

        // using environnment to create homebean
        return buildHomePage((EnvironmenBean) request.getSession().getAttribute(ATTR_ENVBEAN));
    }

    /**
     * Select target environnement to be connected
     */
    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView selectEnv(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String targetEnvId = request.getParameter(FORM_PARAM_ENVIRONMENT);

        log.info("Page <HOME> - Select environment " + targetEnvId);
        if (StringUtils.hasText(targetEnvId)) {
            if (conf.getMapOfConnections().containsKey(targetEnvId)) {
                ArrayList<Connection> conns = new ArrayList<Connection>(conf.getMapOfConnections().values());
                EnvironmenBean envBean = new EnvironmenBean(conf.getMapOfConnections().get(targetEnvId), conns);
                request.getSession().setAttribute(ATTR_ENVBEAN, envBean);
            }
        }

        // using environnment to create homebean
        return buildHomePage((EnvironmenBean) request.getSession().getAttribute(ATTR_ENVBEAN));
    }

    /**
     * Build target page and model from environment.
     * 
     * @param envBean
     *            environment bean
     * @return
     * 
     */
    private ModelAndView buildHomePage(EnvironmenBean envBean) {
        // Output page is HOME
        ModelAndView mav = new ModelAndView(VIEW_HOME);
        log.info("Working with environnement " + envBean.getEnvId());
        mav.addObject(ATTR_ENVBEAN, envBean);

        HomeBean homeBean = new HomeBean();
        mav.addObject(ATTR_HOMEBEAN, homeBean);
        return mav;
    }

}
