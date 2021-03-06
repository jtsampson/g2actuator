/*
 *  Copyright 2016-2017 John Sampson
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.github.jtsampson.actuate

import org.codehaus.groovy.grails.commons.UrlMappingsArtefactHandler
import org.codehaus.groovy.grails.web.mapping.DefaultUrlMappingEvaluator
import org.codehaus.groovy.grails.web.mapping.UrlMapping

/**
 * A service to expose the application's URL mappings.
 *
 * @author jsampson
 */
class MappingService {

    def grailsApplication
    def servletContext

    def collectMappings() {

        def mappings = [:]

        for (urlMapping in getAllMappings()) {
            String path = urlMapping.urlData.urlPattern

            def controllerName = urlMapping.controllerName

            // TODO ignoring controllers w/o names for now.
            if (controllerName) {
                def details = [:]

                details['bean'] = controllerName
                details['allows'] = urlMapping.httpMethod

                def artefact = grailsApplication.getArtefactByLogicalPropertyName("Controller", controllerName)
                def bean = grailsApplication.mainContext.getBean(artefact.clazz.name)
                def methods = bean.getClass().methods
                for (def m in methods) {
                    if (m.name == urlMapping.actionName) {
                        details['method'] = "public ${bean.getClass()}.${urlMapping.actionName}()"
                    }
                }

                // RestControllers use 'responseFormats', lets add them to the results if they exist
                def fields = bean.getClass().declaredFields
                for (def field in fields) {
                    if (field.name == 'responseFormats') {
                        field.setAccessible(true) // Avoid IllegalAccessException if not public
                        details['produces'] = field.get(bean)
                    }
                }

                mappings."${path}" = details
            }
        }

        mappings.sort() // sort by path

    }

    /**
     * Returns a list of defined UrlMappings.
     * @return
     */
    private def getAllMappings() {
        def mappings = grailsApplication.getArtefacts(UrlMappingsArtefactHandler.TYPE)

        DefaultUrlMappingEvaluator evaluator = new DefaultUrlMappingEvaluator(servletContext)
        List<UrlMapping> allMappings = []

        List<UrlMapping> grailsClassMappings
        for (mapping in mappings) {
            if (Script.isAssignableFrom(mapping.getClazz())) {
                grailsClassMappings = evaluator.evaluateMappings(mapping.getClazz())
            } else {
                grailsClassMappings = evaluator.evaluateMappings(mapping.getMappingsClosure())
            }
            allMappings.addAll(grailsClassMappings)
        }

        allMappings.sort()
    }
}
