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

import com.github.jtsampson.actuate.health.Health
import com.github.jtsampson.actuate.health.HealthIndicator
import grails.transaction.Transactional
import grails.util.Holders
import org.springframework.beans.factory.BeanFactoryUtils

import javax.annotation.PostConstruct

/**
 * A Service to expose the service's health.
 *
 * @author jsampson
 */
@Transactional
class HealthService {

    def grailsApplication
    def healthAggregator
    def defaultsEnabled = true

    @PostConstruct
    init() {
        defaultsEnabled = grailsApplication.config.g2actuate.management.health.defaults.enabled
    }

    def collectHealth() {

        // find all health indicators.
        def healths = BeanFactoryUtils.beansOfTypeIncludingAncestors(Holders.applicationContext, HealthIndicator.class)

        def checkResults = []

        if (defaultsEnabled) {
            healths.each { k, v ->
                // perform the health check/get the results
                def h = v.health()
                h.name = k - "HealthIndicator"
                checkResults.add(h)
            }
        } else {
            // Use a fake indicator names 'application.
            checkResults.add(new Health([name: 'application', status: Health.Status.UP]))
        }

        def overall = healthAggregator.aggregate(checkResults)

        def resultAsMapList = []
        checkResults.each { resultAsMapList.add(it.asMap()) }

        def result = [:]
        result << [status: overall.name()]
        resultAsMapList.each {
            result << it
        }

        result

    }
}
