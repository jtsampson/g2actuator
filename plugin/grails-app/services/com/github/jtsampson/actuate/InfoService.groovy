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

import grails.util.Environment
import grails.util.Holders
import grails.util.Metadata
import groovy.json.JsonSlurper

import javax.annotation.PostConstruct

/**
 * A service to expose informational data
 * currently:
 *  - application
 *  - grails (plugins)
 *  - scm (build info)
 */
class InfoService {

    def grailsApplication
    def infoContributor

    @PostConstruct
    def init(){
        infoContributor = grailsApplication.config?.g2actuate?.infoContributor
    }

    def collectInfo() {
        TreeMap info = [:]

        info << app()
        info << grails()
        info << scm()

        // Allow owning applications to add information.
        if (infoContributor instanceof Closure) {
            info = infoContributor(info)
        }

        info
    }

    private def app() {
        def app = [:]
        def description = Metadata.getCurrent().getProperty('app.description') ?: 'app.description not set'
        app['name'] = Metadata.getCurrent().getApplicationName()
        app['description'] = description
        app['version'] = Metadata.getCurrent().getApplicationVersion()
        [app: app]
    }

    // TODO consider a grails service?
    private def grails() {
        def grails = [:]
        def metadata = grails.util.Metadata.getCurrent()
        //grails['application-version'] = metadata.getApplicationVersion()
        grails['grails-version'] = metadata.getCurrent().getGrailsVersion()
        grails['groovy-version'] = GroovySystem.version
        grails['jvm-version'] = System.getProperty('java.version')
        grails['reloading-active'] = Environment.reloadingAgentEnabled
        grails['controllers'] = grailsApplication.controllerClasses.size()
        grails['domains'] = grailsApplication.domainClasses.size()
        grails['services'] = grailsApplication.serviceClasses.size()
        grails['tag-libraries'] = grailsApplication.tagLibClasses.size()

        // plugins
        TreeMap plugins = [:] // maintain insert order by key
        Holders.pluginManager.allPlugins.each { plugin ->
            plugins[plugin.name] = plugin.version
        }
        grails['plugins'] = plugins
        ['grails': grails]
    }

    private def scm() {
        //TODO read from file scm provider places this is application.properties, but should have probably been added to config.properties or injected
        Properties properties = new Properties()
        File propertiesFile = new File('application.properties')
        propertiesFile.withInputStream {
            properties.load(it)
        }

        //def description = Metadata.getCurrent().getProperty('app.description') ?: 'app.description not set'
        def scm = new JsonSlurper().parseText(properties?.scm ?: "{\"message\":\"Nothing found: did you implement g2actuate.scmInfo in buildConfig.groovy?\"}")

        ['scm': scm]
    }

}