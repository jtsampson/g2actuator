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
package com.github.jtsampson.actuate.health

class MyHealthIndicator implements HealthIndicator {

    @Override
    Health health() {
        Health health = new Health([name: 'myIndicator'])
        int errorCode = check() // perform some specific health check
        if (errorCode != 0) {
            health.status = Health.Status.UP
        } else {
            health.status = Health.Status.DOWN
        }

        health
    }

    int check() {
        return 123
    }
}
