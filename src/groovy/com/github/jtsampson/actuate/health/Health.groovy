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

/**
 * @author jsampson
 * @since 6/26/2017.
 */
class Health {

    enum Status {
        UP, DOWN, UNKNOWN, OUT_OF_SERVICE
    }


    String name = 'Unnamed'
    Status status = Status.UNKNOWN
    Map details = [:]


    Map asMap() {

        def values = [:]
        values << [status: this.status.name()]
        values << this.details

       return  [(this.name): values]

    }
}
